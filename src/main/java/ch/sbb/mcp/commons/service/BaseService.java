package ch.sbb.mcp.commons.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Base class for service layer with common patterns for metrics, caching, and error handling.
 */
public abstract class BaseService {
    
    private static final Logger log = LoggerFactory.getLogger(BaseService.class);
    
    /**
     * Execute an operation with automatic metrics collection.
     *
     * @param operationName the name of the operation (for logging/metrics)
     * @param operation the operation to execute
     * @param <T> the result type
     * @return Mono containing the result
     */
    protected <T> Mono<T> withMetrics(String operationName, Mono<T> operation) {
        long startTime = System.currentTimeMillis();
        
        return operation
            .doOnSubscribe(sub -> log.debug("[{}] Starting operation", operationName))
            .doOnSuccess(result -> {
                long duration = System.currentTimeMillis() - startTime;
                log.info("[{}] Operation completed successfully in {}ms", operationName, duration);
                recordSuccess(operationName, duration);
            })
            .doOnError(error -> {
                long duration = System.currentTimeMillis() - startTime;
                log.error("[{}] Operation failed after {}ms: {}", 
                    operationName, duration, error.getMessage());
                recordFailure(operationName, duration, error);
            });
    }
    
    /**
     * Execute an operation with retry logic.
     *
     * @param operationName the name of the operation
     * @param operation the operation to execute
     * @param maxRetries maximum number of retries
     * @param <T> the result type
     * @return Mono containing the result
     */
    protected <T> Mono<T> withRetry(String operationName, Mono<T> operation, int maxRetries) {
        return operation
            .retry(maxRetries)
            .doOnError(error -> 
                log.error("[{}] Operation failed after {} retries", operationName, maxRetries)
            );
    }
    
    /**
     * Record successful operation (subclasses can override to implement actual metrics).
     *
     * @param operationName the operation name
     * @param durationMs the duration in milliseconds
     */
    protected void recordSuccess(String operationName, long durationMs) {
        // Default implementation: just log
        // Subclasses can override to send to metrics system (Prometheus, etc.)
    }
    
    /**
     * Record failed operation (subclasses can override to implement actual metrics).
     *
     * @param operationName the operation name
     * @param durationMs the duration in milliseconds
     * @param error the error that occurred
     */
    protected void recordFailure(String operationName, long durationMs, Throwable error) {
        // Default implementation: just log
        // Subclasses can override to send to metrics system (Prometheus, etc.)
    }
    
    /**
     * Get the service name (for logging).
     *
     * @return the service name
     */
    protected String getServiceName() {
        return this.getClass().getSimpleName();
    }
}
