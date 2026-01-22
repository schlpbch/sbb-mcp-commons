package ch.sbb.mcp.commons.session.health;

import ch.sbb.mcp.commons.session.McpSessionStore;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Health indicator for MCP session store.
 * 
 * <p>Reports the health status of the session management system, including:
 * <ul>
 *   <li>Active session count</li>
 *   <li>Store type (Redis or In-Memory)</li>
 *   <li>Circuit breaker state (for Redis)</li>
 * </ul>
 */
public class McpSessionHealthIndicator implements ReactiveHealthIndicator {
    
    private static final Logger log = LoggerFactory.getLogger(McpSessionHealthIndicator.class);
    
    private final McpSessionStore sessionStore;
    private final CircuitBreaker circuitBreaker;
    
    public McpSessionHealthIndicator(
            McpSessionStore sessionStore,
            CircuitBreaker circuitBreaker) {
        this.sessionStore = sessionStore;
        this.circuitBreaker = circuitBreaker;
    }
    
    @Override
    public Mono<Health> health() {
        return sessionStore.getActiveSessionCount()
                .timeout(Duration.ofSeconds(2))
                .map(activeCount -> {
                    // Determine store type
                    String storeType = sessionStore.getClass().getSimpleName()
                            .replace("McpSessionStore", "");
                    
                    // Get circuit breaker state
                    String circuitBreakerState = circuitBreaker.getState().name();
                    
                    // Build health details
                    Health.Builder builder = Health.up()
                            .withDetail("activeSessionCount", activeCount != null ? activeCount : 0)
                            .withDetail("storeType", storeType)
                            .withDetail("circuitBreakerState", circuitBreakerState);
                    
                    // Add warning if circuit breaker is open
                    if ("OPEN".equals(circuitBreakerState) || "FORCED_OPEN".equals(circuitBreakerState)) {
                        builder.withDetail("warning", "Circuit breaker is open - using fallback store");
                    }
                    
                    return builder.build();
                })
                .onErrorResume(e -> {
                    log.error("Error checking session store health", e);
                    return Mono.just(Health.down()
                            .withDetail("error", e.getMessage())
                            .build());
                });
    }
}
