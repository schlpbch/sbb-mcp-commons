package ch.sbb.mcp.commons.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseApiClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec getSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private TestApiClient apiClient;

    private static class TestApiClient extends BaseApiClient<RuntimeException> {
        protected TestApiClient(WebClient webClient) {
            super(webClient);
        }

        @Override
        protected Throwable mapError(Throwable error) {
            return new RuntimeException("mapped error", error);
        }

        @Override
        public Mono<Boolean> checkHealth() {
            return Mono.just(true);
        }

        // Expose protected methods for testing
        public <T> Mono<T> testGet(String uri, Class<T> responseType) {
            return get(uri, responseType);
        }

        public <T> Mono<T> testWithRetry(Mono<T> op, int retries) {
            return withRetry(op, retries);
        }
        
        @Override
        public boolean isRetryableError(Throwable error) {
            return super.isRetryableError(error);
        }
    }

    @BeforeEach
    void setUp() {
        apiClient = new TestApiClient(webClient);
    }

    @Test
    void get_WhenSuccessful_ShouldReturnResponse() {
        String expectedResponse = "success";
        when(webClient.get()).thenReturn(getSpec);
        when(getSpec.uri(anyString())).thenReturn(getSpec);
        when(getSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(expectedResponse));

        StepVerifier.create(apiClient.testGet("/test", String.class))
            .expectNext(expectedResponse)
            .verifyComplete();
    }

    @Test
    void get_WhenError_ShouldMapError() {
        when(webClient.get()).thenReturn(getSpec);
        when(getSpec.uri(anyString())).thenReturn(getSpec);
        when(getSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new IOException("network error")));

        StepVerifier.create(apiClient.testGet("/test", String.class))
            .expectErrorMatches(e -> e instanceof RuntimeException && e.getMessage().equals("mapped error"))
            .verify();
    }

    @Test
    void isRetryableError_ShouldReturnCorrectValues() {
        assertTrue(apiClient.isRetryableError(new IOException("io")));
        assertTrue(apiClient.isRetryableError(new java.net.ConnectException("connect")));
        
        WebClientResponseException serverError = WebClientResponseException.create(
            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", null, null, null);
        assertTrue(apiClient.isRetryableError(serverError));

        WebClientResponseException tooManyRequests = WebClientResponseException.create(
            HttpStatus.TOO_MANY_REQUESTS.value(), "Too Many Requests", null, null, null);
        assertTrue(apiClient.isRetryableError(tooManyRequests));

        WebClientResponseException badRequest = WebClientResponseException.create(
            HttpStatus.BAD_REQUEST.value(), "Bad Request", null, null, null);
        assertFalse(apiClient.isRetryableError(badRequest));
    }

    @Test
    void withRetry_ShouldRetryOnFailure() {
        java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger();
        Mono<String> failingMono = Mono.defer(() -> {
            counter.incrementAndGet();
            return Mono.error(new IOException("retryable"));
        });
        
        // maxRetries = 2 means 1 initial try + 2 retries = 3 calls total
        StepVerifier.create(apiClient.testWithRetry(failingMono, 2))
            .expectErrorMatches(e -> e.getClass().getSimpleName().equals("RetryExhaustedException") 
                || e.getCause() instanceof IOException)
            .verify(Duration.ofSeconds(5));

        assertEquals(3, counter.get());
    }
}
