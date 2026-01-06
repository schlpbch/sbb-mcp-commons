package ch.sbb.mcp.commons.ratelimit;

import ch.sbb.mcp.commons.exception.McpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SimpleRateLimiter Tests")
class SimpleRateLimiterTest {

    private SimpleRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new SimpleRateLimiter(5, Duration.ofSeconds(1));
    }

    @Nested
    @DisplayName("Rate Limiting Tests")
    class RateLimitingTests {

        @Test
        @DisplayName("Should allow requests within limit")
        void checkRateLimit_WithinLimit_ShouldSucceed() {
            // When/Then - should not throw for first 5 requests
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 5; i++) {
                    rateLimiter.checkRateLimit("client1");
                }
            });
        }

        @Test
        @DisplayName("Should throw McpException when limit exceeded")
        void checkRateLimit_ExceedsLimit_ShouldThrowException() {
            // Given - consume all tokens
            for (int i = 0; i < 5; i++) {
                rateLimiter.checkRateLimit("client1");
            }

            // When/Then - 6th request should fail
            McpException ex = assertThrows(McpException.class, 
                () -> rateLimiter.checkRateLimit("client1"));

            assertEquals("Rate limit exceeded. Please try again later.", ex.getMessage());
            assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getStatus());
            assertEquals("RATE_LIMIT_EXCEEDED", ex.getErrorCode());
        }

        @Test
        @DisplayName("Should track different clients independently")
        void checkRateLimit_DifferentClients_ShouldTrackIndependently() {
            // Given - client1 exhausts their limit
            for (int i = 0; i < 5; i++) {
                rateLimiter.checkRateLimit("client1");
            }

            // When/Then - client2 should still have full quota
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 5; i++) {
                    rateLimiter.checkRateLimit("client2");
                }
            });

            // And client1 should still be limited
            assertThrows(McpException.class, 
                () -> rateLimiter.checkRateLimit("client1"));
        }
    }

    @Nested
    @DisplayName("Token Refill Tests")
    class TokenRefillTests {

        @Test
        @DisplayName("Should refill tokens after refill interval")
        void checkRateLimit_AfterRefillInterval_ShouldAllowRequests() throws InterruptedException {
            // Given - exhaust all tokens
            for (int i = 0; i < 5; i++) {
                rateLimiter.checkRateLimit("client1");
            }
            assertThrows(McpException.class, 
                () -> rateLimiter.checkRateLimit("client1"));

            // When - wait for refill interval
            Thread.sleep(1100); // Wait slightly more than 1 second

            // Then - should be able to make requests again
            assertDoesNotThrow(() -> rateLimiter.checkRateLimit("client1"));
        }

        @Test
        @DisplayName("Should refill tokens proportionally to elapsed time")
        void checkRateLimit_PartialRefill_ShouldWorkCorrectly() throws InterruptedException {
            // Given - use 3 tokens
            for (int i = 0; i < 3; i++) {
                rateLimiter.checkRateLimit("client1");
            }

            // When - wait for refill
            Thread.sleep(1100);

            // Then - should have at least 3 tokens available (refill restores to max)
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 3; i++) {
                    rateLimiter.checkRateLimit("client1");
                }
            });
        }
    }

    @Nested
    @DisplayName("Status Check Tests")
    class StatusCheckTests {

        @Test
        @DisplayName("isRateLimited should return false when tokens available")
        void isRateLimited_WithTokensAvailable_ShouldReturnFalse() {
            assertFalse(rateLimiter.isRateLimited("client1"));
        }

        @Test
        @DisplayName("isRateLimited should return true when no tokens available")
        void isRateLimited_WithNoTokens_ShouldReturnTrue() {
            // Given - exhaust all tokens
            for (int i = 0; i < 5; i++) {
                rateLimiter.checkRateLimit("client1");
            }

            // Then
            assertTrue(rateLimiter.isRateLimited("client1"));
        }

        @Test
        @DisplayName("isRateLimited should not consume tokens")
        void isRateLimited_ShouldNotConsumeTokens() {
            // When - check status multiple times
            for (int i = 0; i < 10; i++) {
                rateLimiter.isRateLimited("client1");
            }

            // Then - should still be able to make requests
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 5; i++) {
                    rateLimiter.checkRateLimit("client1");
                }
            });
        }

        @Test
        @DisplayName("isRateLimited should return false for unknown client")
        void isRateLimited_UnknownClient_ShouldReturnFalse() {
            assertFalse(rateLimiter.isRateLimited("unknownClient"));
        }
    }

    @Nested
    @DisplayName("Clear Operations Tests")
    class ClearOperationsTests {

        @Test
        @DisplayName("clearRateLimit should reset specific client")
        void clearRateLimit_ShouldResetClient() {
            // Given - exhaust tokens
            for (int i = 0; i < 5; i++) {
                rateLimiter.checkRateLimit("client1");
            }
            assertTrue(rateLimiter.isRateLimited("client1"));

            // When
            rateLimiter.clearRateLimit("client1");

            // Then - should have full quota again
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 5; i++) {
                    rateLimiter.checkRateLimit("client1");
                }
            });
        }

        @Test
        @DisplayName("clearAll should reset all clients")
        void clearAll_ShouldResetAllClients() {
            // Given - exhaust tokens for multiple clients
            for (int i = 0; i < 5; i++) {
                rateLimiter.checkRateLimit("client1");
                rateLimiter.checkRateLimit("client2");
            }
            assertTrue(rateLimiter.isRateLimited("client1"));
            assertTrue(rateLimiter.isRateLimited("client2"));

            // When
            rateLimiter.clearAll();

            // Then - both should have full quota
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 5; i++) {
                    rateLimiter.checkRateLimit("client1");
                    rateLimiter.checkRateLimit("client2");
                }
            });
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Should handle concurrent requests from same client safely")
        void checkRateLimit_ConcurrentSameClient_ShouldBeSafe() throws InterruptedException {
            // Given
            int threadCount = 10;
            int requestsPerThread = 2;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            // When - multiple threads make requests for same client
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < requestsPerThread; j++) {
                            try {
                                rateLimiter.checkRateLimit("sharedClient");
                                successCount.incrementAndGet();
                            } catch (McpException e) {
                                failureCount.incrementAndGet();
                            }
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            executor.shutdown();

            // Then - exactly 5 should succeed (the token limit)
            assertEquals(5, successCount.get());
            assertEquals(15, failureCount.get()); // 20 total - 5 successful = 15 failures
        }

        @Test
        @DisplayName("Should handle concurrent requests from different clients safely")
        void checkRateLimit_ConcurrentDifferentClients_ShouldBeSafe() throws InterruptedException {
            // Given
            int clientCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(clientCount);
            CountDownLatch latch = new CountDownLatch(clientCount);
            List<Exception> exceptions = new ArrayList<>();

            // When - each thread represents a different client
            for (int i = 0; i < clientCount; i++) {
                final String clientId = "client" + i;
                executor.submit(() -> {
                    try {
                        // Each client makes 5 requests (their full quota)
                        for (int j = 0; j < 5; j++) {
                            rateLimiter.checkRateLimit(clientId);
                        }
                    } catch (Exception e) {
                        synchronized (exceptions) {
                            exceptions.add(e);
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            executor.shutdown();

            // Then - all should succeed (each client has independent quota)
            assertTrue(exceptions.isEmpty(), "Expected no exceptions but got: " + exceptions);
        }
    }

    @Nested
    @DisplayName("Boundary Condition Tests")
    class BoundaryConditionTests {

        @Test
        @DisplayName("Should handle zero tokens correctly")
        void checkRateLimit_WithZeroTokens_ShouldImmediatelyFail() {
            // Given
            SimpleRateLimiter zeroTokenLimiter = new SimpleRateLimiter(0, Duration.ofSeconds(1));

            // When/Then
            assertThrows(McpException.class, 
                () -> zeroTokenLimiter.checkRateLimit("client1"));
        }

        @Test
        @DisplayName("Should handle single token correctly")
        void checkRateLimit_WithSingleToken_ShouldAllowOneRequest() {
            // Given
            SimpleRateLimiter singleTokenLimiter = new SimpleRateLimiter(1, Duration.ofSeconds(1));

            // When/Then - first request succeeds
            assertDoesNotThrow(() -> singleTokenLimiter.checkRateLimit("client1"));

            // And second request fails
            assertThrows(McpException.class, 
                () -> singleTokenLimiter.checkRateLimit("client1"));
        }

        @Test
        @DisplayName("Should handle very short refill interval")
        void checkRateLimit_WithShortRefillInterval_ShouldWork() throws InterruptedException {
            // Given
            SimpleRateLimiter fastRefillLimiter = new SimpleRateLimiter(2, Duration.ofMillis(100));

            // When - exhaust tokens
            fastRefillLimiter.checkRateLimit("client1");
            fastRefillLimiter.checkRateLimit("client1");
            assertThrows(McpException.class, 
                () -> fastRefillLimiter.checkRateLimit("client1"));

            // Wait for refill
            Thread.sleep(150);

            // Then - should work again
            assertDoesNotThrow(() -> fastRefillLimiter.checkRateLimit("client1"));
        }
    }
}
