package ch.sbb.mcp.commons.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for McpRequestContext.
 */
class McpRequestContextTest {

    @AfterEach
    void tearDown() {
        // Always clear context after each test to prevent interference
        McpRequestContext.clear();
    }

    @Test
    void setSessionId_shouldStoreSessionId() {
        // When
        McpRequestContext.setSessionId("session123");

        // Then
        assertThat(McpRequestContext.getSessionId()).isPresent();
        assertThat(McpRequestContext.getSessionId().get()).isEqualTo("session123");
    }

    @Test
    void getSessionId_whenNotSet_shouldReturnEmpty() {
        // When
        var sessionId = McpRequestContext.getSessionId();

        // Then
        assertThat(sessionId).isEmpty();
    }

    @Test
    void setCorrelationId_shouldStoreCorrelationId() {
        // When
        McpRequestContext.setCorrelationId("correlation123");

        // Then
        assertThat(McpRequestContext.getCorrelationId()).isPresent();
        assertThat(McpRequestContext.getCorrelationId().get()).isEqualTo("correlation123");
    }

    @Test
    void getCorrelationId_whenNotSet_shouldReturnEmpty() {
        // When
        var correlationId = McpRequestContext.getCorrelationId();

        // Then
        assertThat(correlationId).isEmpty();
    }

    @Test
    void clear_shouldRemoveAllContext() {
        // Given
        McpRequestContext.setSessionId("session123");
        McpRequestContext.setCorrelationId("correlation123");

        // When
        McpRequestContext.clear();

        // Then
        assertThat(McpRequestContext.getSessionId()).isEmpty();
        assertThat(McpRequestContext.getCorrelationId()).isEmpty();
    }

    @Test
    void hasSessionId_shouldReturnCorrectStatus() {
        // When - not set
        assertThat(McpRequestContext.hasSessionId()).isFalse();

        // When - set
        McpRequestContext.setSessionId("session123");
        assertThat(McpRequestContext.hasSessionId()).isTrue();

        // When - cleared
        McpRequestContext.clear();
        assertThat(McpRequestContext.hasSessionId()).isFalse();
    }

    @Test
    void threadLocal_shouldIsolateContextBetweenThreads() throws InterruptedException {
        // Given
        CountDownLatch latch = new CountDownLatch(2);
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        // When - set context in thread 1
        Thread thread1 = new Thread(() -> {
            McpRequestContext.setSessionId("session1");
            McpRequestContext.setCorrelationId("correlation1");
            
            try {
                Thread.sleep(100); // Ensure threads overlap
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            results.add(McpRequestContext.getSessionId().orElse("none"));
            results.add(McpRequestContext.getCorrelationId().orElse("none"));
            McpRequestContext.clear();
            latch.countDown();
        });

        // When - set different context in thread 2
        Thread thread2 = new Thread(() -> {
            McpRequestContext.setSessionId("session2");
            McpRequestContext.setCorrelationId("correlation2");
            
            try {
                Thread.sleep(100); // Ensure threads overlap
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            results.add(McpRequestContext.getSessionId().orElse("none"));
            results.add(McpRequestContext.getCorrelationId().orElse("none"));
            McpRequestContext.clear();
            latch.countDown();
        });

        thread1.start();
        thread2.start();
        latch.await(5, TimeUnit.SECONDS);

        // Then - each thread should see its own context
        assertThat(results).contains("session1", "correlation1", "session2", "correlation2");
        assertThat(results).hasSize(4);
    }

    @Test
    void concurrentAccess_shouldHandleMultipleThreads() throws InterruptedException {
        // Given
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Boolean> results = Collections.synchronizedList(new ArrayList<>());

        // When - multiple threads set and read context
        for (int i = 0; i < threadCount; i++) {
            final int threadNum = i;
            executor.submit(() -> {
                try {
                    String sessionId = "session" + threadNum;
                    String correlationId = "correlation" + threadNum;
                    
                    McpRequestContext.setSessionId(sessionId);
                    McpRequestContext.setCorrelationId(correlationId);
                    
                    // Verify context is correct for this thread
                    boolean sessionCorrect = McpRequestContext.getSessionId()
                        .map(id -> id.equals(sessionId))
                        .orElse(false);
                    boolean correlationCorrect = McpRequestContext.getCorrelationId()
                        .map(id -> id.equals(correlationId))
                        .orElse(false);
                    
                    results.add(sessionCorrect && correlationCorrect);
                    
                    McpRequestContext.clear();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Then - all threads should have seen correct context
        assertThat(results).hasSize(threadCount);
        assertThat(results).allMatch(result -> result);
    }

    @Test
    void clear_shouldNotAffectOtherThreads() throws InterruptedException {
        // Given
        CountDownLatch latch = new CountDownLatch(2);
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        // Thread 1 - sets and clears
        Thread thread1 = new Thread(() -> {
            McpRequestContext.setSessionId("session1");
            McpRequestContext.clear();
            results.add(McpRequestContext.getSessionId().orElse("cleared"));
            latch.countDown();
        });

        // Thread 2 - sets and keeps
        Thread thread2 = new Thread(() -> {
            McpRequestContext.setSessionId("session2");
            try {
                Thread.sleep(50); // Wait for thread1 to clear
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            results.add(McpRequestContext.getSessionId().orElse("none"));
            McpRequestContext.clear();
            latch.countDown();
        });

        thread1.start();
        thread2.start();
        latch.await(5, TimeUnit.SECONDS);

        // Then
        assertThat(results).contains("cleared", "session2");
    }

    @Test
    void multipleSetCalls_shouldOverwritePreviousValue() {
        // When
        McpRequestContext.setSessionId("session1");
        McpRequestContext.setSessionId("session2");

        // Then
        assertThat(McpRequestContext.getSessionId().get()).isEqualTo("session2");
    }

    @Test
    void nullValues_shouldBeHandledGracefully() {
        // When
        McpRequestContext.setSessionId(null);
        McpRequestContext.setCorrelationId(null);

        // Then
        assertThat(McpRequestContext.getSessionId()).isEmpty();
        assertThat(McpRequestContext.getCorrelationId()).isEmpty();
        assertThat(McpRequestContext.hasSessionId()).isFalse();
    }
}
