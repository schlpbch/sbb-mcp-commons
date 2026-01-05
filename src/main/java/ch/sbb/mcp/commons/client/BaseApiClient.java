package ch.sbb.mcp.commons.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.function.Function;

/**
 * Base class for API clients with common patterns for HTTP communication.
 * Provides WebClient integration, error handling, retry logic, and caching.
 *
 * @param <ERROR_TYPE> the type of exception to throw for API errors
 */
public abstract class BaseApiClient<ERROR_TYPE extends RuntimeException> {
    
    private static final Logger log = LoggerFactory.getLogger(BaseApiClient.class);
    
    protected final WebClient webClient;
    
    protected BaseApiClient(WebClient webClient) {
        this.webClient = webClient;
    }
    
    /**
     * Execute a GET request with error handling and optional retry.
     *
     * @param uri the URI to call
     * @param responseType the response class type
     * @param <T> the response type
     * @return Mono containing the response
     */
    protected <T> Mono<T> get(String uri, Class<T> responseType) {
        log.debug("[{}] GET {}", getClientName(), uri);
        
        return webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(responseType)
            .doOnSuccess(response -> log.debug("[{}] GET {} - Success", getClientName(), uri))
            .onErrorMap(this::mapError);
    }
    
    /**
     * Execute a GET request with query parameters.
     *
     * @param uri the URI to call
     * @param responseType the response class type
     * @param uriFunction function to add query parameters
     * @param <T> the response type
     * @return Mono containing the response
     */
    protected <T> Mono<T> get(String uri, Class<T> responseType, 
                              Function<WebClient.RequestHeadersUriSpec<?>, WebClient.RequestHeadersSpec<?>> uriFunction) {
        log.debug("[{}] GET {}", getClientName(), uri);
        
        return Mono.defer(() -> {
            WebClient.RequestHeadersUriSpec<?> spec = webClient.get();
            WebClient.RequestHeadersSpec<?> headersSpec = uriFunction.apply(spec);
            
            return headersSpec
                .retrieve()
                .bodyToMono(responseType)
                .doOnSuccess(response -> log.debug("[{}] GET {} - Success", getClientName(), uri))
                .onErrorMap(this::mapError);
        });
    }
    
    /**
     * Execute a POST request with request body.
     *
     * @param uri the URI to call
     * @param requestBody the request body
     * @param responseType the response class type
     * @param <T> the response type
     * @param <R> the request type
     * @return Mono containing the response
     */
    protected <T, R> Mono<T> post(String uri, R requestBody, Class<T> responseType) {
        log.debug("[{}] POST {}", getClientName(), uri);
        
        return webClient.post()
            .uri(uri)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(responseType)
            .doOnSuccess(response -> log.debug("[{}] POST {} - Success", getClientName(), uri))
            .onErrorMap(this::mapError);
    }
    
    /**
     * Execute a PUT request with request body.
     *
     * @param uri the URI to call
     * @param requestBody the request body
     * @param responseType the response class type
     * @param <T> the response type
     * @param <R> the request type
     * @return Mono containing the response
     */
    protected <T, R> Mono<T> put(String uri, R requestBody, Class<T> responseType) {
        log.debug("[{}] PUT {}", getClientName(), uri);
        
        return webClient.put()
            .uri(uri)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(responseType)
            .doOnSuccess(response -> log.debug("[{}] PUT {} - Success", getClientName(), uri))
            .onErrorMap(this::mapError);
    }
    
    /**
     * Execute a DELETE request.
     *
     * @param uri the URI to call
     * @param responseType the response class type
     * @param <T> the response type
     * @return Mono containing the response
     */
    protected <T> Mono<T> delete(String uri, Class<T> responseType) {
        log.debug("[{}] DELETE {}", getClientName(), uri);
        
        return webClient.delete()
            .uri(uri)
            .retrieve()
            .bodyToMono(responseType)
            .doOnSuccess(response -> log.debug("[{}] DELETE {} - Success", getClientName(), uri))
            .onErrorMap(this::mapError);
    }
    
    /**
     * Execute a request with retry logic.
     *
     * @param operation the operation to execute
     * @param maxRetries maximum number of retries
     * @param <T> the response type
     * @return Mono containing the response
     */
    protected <T> Mono<T> withRetry(Mono<T> operation, int maxRetries) {
        return operation.retryWhen(
            Retry.fixedDelay(maxRetries, Duration.ofMillis(500))
                .filter(this::isRetryableError)
                .doBeforeRetry(signal -> 
                    log.warn("[{}] Retrying after error: {}", 
                        getClientName(), signal.failure().getMessage())
                )
        );
    }
    
    /**
     * Execute a request with exponential backoff retry.
     *
     * @param operation the operation to execute
     * @param maxRetries maximum number of retries
     * @param <T> the response type
     * @return Mono containing the response
     */
    protected <T> Mono<T> withExponentialBackoff(Mono<T> operation, int maxRetries) {
        return operation.retryWhen(
            Retry.backoff(maxRetries, Duration.ofMillis(100))
                .maxBackoff(Duration.ofSeconds(5))
                .filter(this::isRetryableError)
                .doBeforeRetry(signal -> 
                    log.warn("[{}] Retrying (attempt {}) after error: {}", 
                        getClientName(), signal.totalRetries() + 1, signal.failure().getMessage())
                )
        );
    }
    
    /**
     * Map WebClient errors to domain-specific exceptions.
     * Subclasses must implement this to provide custom error handling.
     *
     * @param error the error from WebClient
     * @return the mapped domain exception
     */
    protected abstract Throwable mapError(Throwable error);
    
    /**
     * Determine if an error is retryable.
     * Default implementation retries on 5xx errors and network issues.
     *
     * @param error the error
     * @return true if retryable
     */
    protected boolean isRetryableError(Throwable error) {
        if (error instanceof WebClientResponseException webClientError) {
            int statusCode = webClientError.getStatusCode().value();
            // Retry on 5xx server errors and 429 Too Many Requests
            return statusCode >= 500 || statusCode == 429;
        }
        // Retry on network errors
        return error instanceof java.net.ConnectException 
            || error instanceof java.net.SocketTimeoutException
            || error instanceof java.io.IOException;
    }
    
    /**
     * Get the client name for logging.
     *
     * @return the client name
     */
    protected String getClientName() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * Health check - subclasses should implement specific logic.
     *
     * @return Mono containing health status
     */
    public abstract Mono<Boolean> checkHealth();
}
