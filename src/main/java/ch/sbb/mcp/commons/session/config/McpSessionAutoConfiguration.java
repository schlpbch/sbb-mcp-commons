package ch.sbb.mcp.commons.session.config;

import ch.sbb.mcp.commons.session.McpSession;
import ch.sbb.mcp.commons.session.impl.InMemoryMcpSessionStore;
import ch.sbb.mcp.commons.session.impl.RedisMcpSessionStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Auto-configuration for MCP session management.
 * 
 * <p>Automatically registers the appropriate {@link ch.sbb.mcp.commons.session.McpSessionStore}
 * implementation based on the availability of Redis Dependencies and configuration.
 * 
 * <p>Also provides necessary infrastructure beans like Redis templates, Circuit Breakers,
 * and Retry polices if they are needed.
 */
@AutoConfiguration
@Import({InMemoryMcpSessionStore.class, RedisMcpSessionStore.class})
public class McpSessionAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(McpSessionAutoConfiguration.class);

    /**
     * Creates a ReactiveRedisTemplate specifically for McpSession objects.
     * 
     * <p>Uses Jackson serialization with polymorphic type handling to preserve
     * type information during serialization/deserialization.
     */
    @Bean
    @ConditionalOnBean(ReactiveRedisConnectionFactory.class)
    public ReactiveRedisTemplate<String, McpSession> reactiveMcpSessionRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory,
            @Value("${mcp.session.ttl:PT1H}") Duration sessionTtl) {
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.findAndRegisterModules();
        
        // Enable polymorphic type handling for McpSession
        objectMapper.activateDefaultTyping(
            objectMapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL,
            com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
        );
        
        Jackson2JsonRedisSerializer<McpSession> jsonSerializer =
            new Jackson2JsonRedisSerializer<>(objectMapper, McpSession.class);
        
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        
        RedisSerializationContext<String, McpSession> context = RedisSerializationContext
            .<String, McpSession>newSerializationContext(stringSerializer)
            .key(stringSerializer)
            .value(jsonSerializer)
            .hashKey(stringSerializer)
            .hashValue(jsonSerializer)
            .build();
        
        log.info("Created ReactiveRedisTemplate for McpSession with TTL: {}", sessionTtl);
        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
    
    /**
     * Creates a circuit breaker for session store operations.
     */
    @Bean
    @ConditionalOnClass(CircuitBreaker.class)
    public CircuitBreaker sessionStoreCircuitBreaker(
            @Value("${mcp.session.circuit-breaker.failure-rate-threshold:50}") float failureRateThreshold,
            @Value("${mcp.session.circuit-breaker.wait-duration:60s}") Duration waitDuration) {
        
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(failureRateThreshold)
                .waitDurationInOpenState(waitDuration)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .permittedNumberOfCallsInHalfOpenState(3)
                .build();
        
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        CircuitBreaker circuitBreaker = registry.circuitBreaker("sessionStore");
        
        // Log circuit breaker state changes
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> 
                    log.warn("Circuit breaker state transition: {} -> {}", 
                            event.getStateTransition().getFromState(),
                            event.getStateTransition().getToState())
                )
                .onError(event -> 
                    log.error("Circuit breaker recorded error: {}", event.getThrowable().getMessage())
                );
        
        return circuitBreaker;
    }
    
    /**
     * Creates a retry policy for session store operations.
     */
    @Bean
    @ConditionalOnClass(Retry.class)
    public Retry sessionStoreRetry(
            @Value("${mcp.session.retry.max-attempts:3}") int maxAttempts,
            @Value("${mcp.session.retry.wait-duration:100ms}") Duration waitDuration) {
        
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(maxAttempts)
                .intervalFunction(io.github.resilience4j.core.IntervalFunction
                        .ofExponentialBackoff(waitDuration.toMillis(), 2))
                .build();
                
        // Note: Specific exceptions are hard to reference without direct dependency on Spring Data Redis
        // in the method signature, but we can rely on default retryable exceptions or configure strictly
        // if needed. For now, default behavior is acceptable or we can add exceptions if classes are available.
        
        RetryRegistry registry = RetryRegistry.of(config);
        Retry retry = registry.retry("sessionStore");
        
        return retry;
    }
}
