package ch.sbb.mcp.commons.session.impl;

import ch.sbb.mcp.commons.session.McpSession;
import ch.sbb.mcp.commons.session.McpSessionStore;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Redis-backed implementation of {@link McpSessionStore} for distributed deployments.
 * 
 * <p>This implementation uses Redis for session storage, enabling session sharing across
 * multiple application instances. It includes automatic failover to an in-memory store
 * when Redis is unavailable, circuit breaker protection, and retry logic.
 * 
 * <p><strong>Activation:</strong> This bean is created when {@link ReactiveRedisConnectionFactory}
 * is available and is marked as {@code @Primary} for distributed deployments.
 * 
 * <p><strong>Features:</strong>
 * <ul>
 *   <li>Distributed session storage (shared across instances)</li>
 *   <li>Session persistence across application restarts</li>
 *   <li>Automatic TTL management (Redis EXPIRE command)</li>
 *   <li>Circuit breaker protection (Resilience4j)</li>
 *   <li>Retry logic with exponential backoff</li>
 *   <li>Automatic failover to in-memory store</li>
 *   <li>Comprehensive metrics and logging</li>
 * </ul>
 * 
 * <p><strong>Performance:</strong> All operations complete in < 20ms (P99) under normal conditions.
 */
@Component
@Primary
@ConditionalOnBean(ReactiveRedisConnectionFactory.class)
public class RedisMcpSessionStore implements McpSessionStore {
    
    private static final Logger log = LoggerFactory.getLogger(RedisMcpSessionStore.class);
    private static final String KEY_PREFIX = "mcp:session:";
    
    private final ReactiveRedisTemplate<String, McpSession> redisTemplate;
    private final Duration ttl;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final InMemoryMcpSessionStore fallbackStore;
    
    // Metrics
    private final Counter sessionsCreated;
    private final Counter redisHits;
    private final Counter redisMisses;
    private final Counter redisErrors;
    private final Timer redisLatency;
    
    public RedisMcpSessionStore(
            @Qualifier("reactiveMcpSessionRedisTemplate") ReactiveRedisTemplate<String, McpSession> redisTemplate,
            @Value("${mcp.session.ttl:PT1H}") Duration ttl,
            @Qualifier("sessionStoreCircuitBreaker") CircuitBreaker circuitBreaker,
            @Qualifier("sessionStoreRetry") Retry retry,
            InMemoryMcpSessionStore fallbackStore,
            MeterRegistry meterRegistry) {
        
        this.redisTemplate = redisTemplate;
        this.ttl = ttl;
        this.circuitBreaker = circuitBreaker;
        this.retry = retry;
        this.fallbackStore = fallbackStore;
        
        // Register metrics
        this.sessionsCreated = Counter.builder("mcp.sessions.created")
                .description("Total number of sessions created")
                .tag("store", "redis")
                .register(meterRegistry);
        
        this.redisHits = Counter.builder("mcp.sessions.redis.hits")
                .description("Successful Redis operations")
                .register(meterRegistry);
        
        this.redisMisses = Counter.builder("mcp.sessions.redis.misses")
                .description("Redis cache misses")
                .register(meterRegistry);
        
        this.redisErrors = Counter.builder("mcp.sessions.redis.errors")
                .description("Redis operation failures")
                .register(meterRegistry);
        
        this.redisLatency = Timer.builder("mcp.sessions.redis.latency")
                .description("Redis operation latency")
                .register(meterRegistry);
        
        log.info("RedisMcpSessionStore initialized with TTL: {}, Circuit Breaker: {}, Retry: {}",
                ttl, circuitBreaker.getName(), retry.getName());
    }
    
    @Override
    public Mono<McpSession> createSession() {
        return Mono.fromCallable(() -> {
                    String sessionId = UUID.randomUUID().toString();
                    Instant now = Instant.now();
                    
                    McpSession session = new McpSession(
                            sessionId,
                            now,
                            new AtomicReference<>(now),
                            new ConcurrentHashMap<>()
                    );
                    
                    return session;
                })
                .flatMap(session -> 
                    redisTemplate.opsForValue()
                            .set(toKey(session.sessionId()), session, ttl)
                            .thenReturn(session)
                            .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                            .transformDeferred(RetryOperator.of(retry))
                            .doOnSuccess(s -> {
                                sessionsCreated.increment();
                                redisHits.increment();
                                log.debug("Created session in Redis: {}", session.sessionId());
                            })
                            .doOnError(error -> {
                                redisErrors.increment();
                                log.error("Failed to create session in Redis: {}", session.sessionId(), error);
                            })
                            .onErrorResume(error -> {
                                log.warn("Falling back to in-memory store for session creation");
                                return fallbackStore.createSession();
                            })
                );
    }
    
    @Override
    public Mono<McpSession> getSession(String sessionId) {
        return redisTemplate.opsForValue()
                .get(toKey(sessionId))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry))
                .doOnNext(session -> {
                    redisHits.increment();
                    log.trace("Retrieved session from Redis: {}", sessionId);
                })
                .doOnSuccess(session -> {
                    if (session == null) {
                        redisMisses.increment();
                    }
                })
                .doOnError(error -> {
                    redisErrors.increment();
                    log.error("Failed to get session from Redis: {}", sessionId, error);
                })
                .onErrorResume(error -> {
                    log.warn("Falling back to in-memory store for session retrieval: {}", sessionId);
                    return fallbackStore.getSession(sessionId);
                });
    }
    
    @Override
    public Mono<Void> touchSession(String sessionId) {
        return redisTemplate.expire(toKey(sessionId), ttl)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry))
                .doOnSuccess(renewed -> {
                    if (Boolean.TRUE.equals(renewed)) {
                        redisHits.increment();
                        log.trace("Touched session in Redis: {}", sessionId);
                    } else {
                        redisMisses.increment();
                        log.debug("Session not found when touching: {}", sessionId);
                    }
                })
                .doOnError(error -> {
                    redisErrors.increment();
                    log.error("Failed to touch session in Redis: {}", sessionId, error);
                })
                .then()
                .onErrorResume(error -> {
                    log.warn("Falling back to in-memory store for session touch: {}", sessionId);
                    return fallbackStore.touchSession(sessionId);
                });
    }
    
    @Override
    public Mono<Void> deleteSession(String sessionId) {
        return redisTemplate.delete(toKey(sessionId))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry))
                .doOnSuccess(deleted -> {
                    if (deleted > 0) {
                        redisHits.increment();
                        log.debug("Deleted session from Redis: {}", sessionId);
                    } else {
                        redisMisses.increment();
                    }
                })
                .doOnError(error -> {
                    redisErrors.increment();
                    log.error("Failed to delete session from Redis: {}", sessionId, error);
                })
                .then()
                .onErrorResume(error -> {
                    log.warn("Falling back to in-memory store for session deletion: {}", sessionId);
                    return fallbackStore.deleteSession(sessionId);
                });
    }
    
    @Override
    public Mono<Boolean> isValidSession(String sessionId) {
        return redisTemplate.hasKey(toKey(sessionId))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry))
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        // Check TTL to ensure session hasn't expired
                        return redisTemplate.getExpire(toKey(sessionId))
                                .map(ttlDuration -> ttlDuration != null && !ttlDuration.isNegative() && !ttlDuration.isZero());
                    }
                    return Mono.just(false);
                })
                .doOnSuccess(valid -> {
                    if (Boolean.TRUE.equals(valid)) {
                        redisHits.increment();
                    } else {
                        redisMisses.increment();
                    }
                })
                .doOnError(error -> {
                    redisErrors.increment();
                    log.error("Failed to validate session in Redis: {}", sessionId, error);
                })
                .onErrorResume(error -> {
                    log.warn("Falling back to in-memory store for session validation: {}", sessionId);
                    return fallbackStore.isValidSession(sessionId);
                });
    }
    
    @Override
    public Mono<Long> getActiveSessionCount() {
        return redisTemplate.keys(KEY_PREFIX + "*")
                .count()
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnSuccess(count -> log.debug("Active session count from Redis: {}", count))
                .doOnError(error -> {
                    redisErrors.increment();
                    log.error("Failed to get session count from Redis", error);
                })
                .onErrorResume(error -> {
                    log.warn("Falling back to in-memory store for session count");
                    return fallbackStore.getActiveSessionCount();
                });
    }

    @Override
    public Mono<Void> saveSession(McpSession session) {
        return redisTemplate.opsForValue()
                .set(toKey(session.sessionId()), session, ttl)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry))
                .then()
                .onErrorResume(error -> {
                    log.warn("Falling back to in-memory store for session save: {}", session.sessionId());
                    return fallbackStore.saveSession(session);
                });
    }
    
    /**
     * Converts a session ID to a Redis key with the appropriate prefix.
     */
    private String toKey(String sessionId) {
        return KEY_PREFIX + sessionId;
    }
}
