package ch.sbb.mcp.commons.session;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("McpSession Domain Model Tests")
class McpSessionTest {
    
    @Test
    @DisplayName("Should create session with valid UUID")
    void shouldCreateSessionWithValidUUID() {
        // Given
        String sessionId = java.util.UUID.randomUUID().toString();
        Instant now = Instant.now();
        
        // When
        McpSession session = new McpSession(
                sessionId,
                now,
                new AtomicReference<>(now),
                new ConcurrentHashMap<>()
        );
        
        // Then
        assertThat(session.sessionId()).isEqualTo(sessionId);
        assertThat(session.createdAt()).isEqualTo(now);
        assertThat(session.lastAccessedAt().get()).isEqualTo(now);
        assertThat(session.attributes()).isEmpty();
    }
    
    @Test
    @DisplayName("Should update lastAccessedAt when touched")
    void shouldUpdateLastAccessedAtWhenTouched() throws InterruptedException {
        // Given
        Instant createdAt = Instant.now();
        McpSession session = new McpSession(
                "test-session",
                createdAt,
                new AtomicReference<>(createdAt),
                new ConcurrentHashMap<>()
        );
        
        // Wait a bit to ensure timestamp difference
        Thread.sleep(10);
        
        // When
        session.touch();
        
        // Then
        assertThat(session.lastAccessedAt().get()).isAfter(createdAt);
    }
    
    @Test
    @DisplayName("Should correctly identify expired sessions")
    void shouldCorrectlyIdentifyExpiredSessions() throws InterruptedException {
        // Given
        Instant pastTime = Instant.now().minus(Duration.ofHours(2));
        McpSession session = new McpSession(
                "test-session",
                pastTime,
                new AtomicReference<>(pastTime),
                new ConcurrentHashMap<>()
        );
        
        Duration ttl = Duration.ofHours(1);
        
        // When
        boolean expired = session.isExpired(ttl);
        
        // Then
        assertThat(expired).isTrue();
    }
    
    @Test
    @DisplayName("Should correctly identify non-expired sessions")
    void shouldCorrectlyIdentifyNonExpiredSessions() {
        // Given
        Instant now = Instant.now();
        McpSession session = new McpSession(
                "test-session",
                now,
                new AtomicReference<>(now),
                new ConcurrentHashMap<>()
        );
        
        Duration ttl = Duration.ofHours(1);
        
        // When
        boolean expired = session.isExpired(ttl);
        
        // Then
        assertThat(expired).isFalse();
    }
    
    @Test
    @DisplayName("Should add and retrieve attributes")
    void shouldAddAndRetrieveAttributes() {
        // Given
        McpSession session = new McpSession(
                "test-session",
                Instant.now(),
                new AtomicReference<>(Instant.now()),
                new ConcurrentHashMap<>()
        );
        
        // When
        session.withAttribute("key1", "value1");
        session.withAttribute("key2", 42);
        
        // Then
        assertThat(session.getAttribute("key1")).isEqualTo("value1");
        assertThat(session.getAttribute("key2")).isEqualTo(42);
        assertThat(session.getAttribute("nonexistent")).isNull();
    }
    
    @Test
    @DisplayName("Should remove attributes")
    void shouldRemoveAttributes() {
        // Given
        McpSession session = new McpSession(
                "test-session",
                Instant.now(),
                new AtomicReference<>(Instant.now()),
                new ConcurrentHashMap<>()
        );
        session.withAttribute("key1", "value1");
        
        // When
        Object removed = session.removeAttribute("key1");
        
        // Then
        assertThat(removed).isEqualTo("value1");
        assertThat(session.getAttribute("key1")).isNull();
    }
    
    @Test
    @DisplayName("Should calculate session age correctly")
    void shouldCalculateSessionAgeCorrectly() throws InterruptedException {
        // Given
        Instant createdAt = Instant.now();
        McpSession session = new McpSession(
                "test-session",
                createdAt,
                new AtomicReference<>(createdAt),
                new ConcurrentHashMap<>()
        );
        
        // Wait a bit
        Thread.sleep(100);
        
        // When
        Duration age = session.getAge();
        
        // Then
        assertThat(age.toMillis()).isGreaterThanOrEqualTo(100);
    }
    
    @Test
    @DisplayName("Should calculate idle time correctly")
    void shouldCalculateIdleTimeCorrectly() throws InterruptedException {
        // Given
        Instant now = Instant.now();
        McpSession session = new McpSession(
                "test-session",
                now,
                new AtomicReference<>(now),
                new ConcurrentHashMap<>()
        );
        
        // Wait a bit
        Thread.sleep(100);
        
        // When
        Duration idleTime = session.getIdleTime();
        
        // Then
        assertThat(idleTime.toMillis()).isGreaterThanOrEqualTo(100);
    }
    
    @Test
    @DisplayName("Should reset idle time when touched")
    void shouldResetIdleTimeWhenTouched() throws InterruptedException {
        // Given
        Instant now = Instant.now();
        McpSession session = new McpSession(
                "test-session",
                now,
                new AtomicReference<>(now),
                new ConcurrentHashMap<>()
        );
        
        // Wait and touch
        Thread.sleep(100);
        session.touch();
        
        // When
        Duration idleTime = session.getIdleTime();
        
        // Then
        assertThat(idleTime.toMillis()).isLessThan(50); // Should be very small
    }
    
    @Test
    @DisplayName("Should handle concurrent touch() calls safely")
    void shouldHandleConcurrentTouchCallsSafely() throws InterruptedException {
        // Given
        McpSession session = new McpSession(
                "test-session",
                Instant.now(),
                new AtomicReference<>(Instant.now()),
                new ConcurrentHashMap<>()
        );
        
        int threadCount = 10;
        int touchesPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        // When - Multiple threads touching concurrently
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < touchesPerThread; j++) {
                        session.touch();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Then - Should complete without exceptions
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(session.lastAccessedAt().get()).isNotNull();
        
        executor.shutdown();
    }
    
    @Test
    @DisplayName("Should handle concurrent attribute operations safely")
    void shouldHandleConcurrentAttributeOperationsSafely() throws InterruptedException {
        // Given
        McpSession session = new McpSession(
                "test-session",
                Instant.now(),
                new AtomicReference<>(Instant.now()),
                new ConcurrentHashMap<>()
        );
        
        int threadCount = 10;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        // When - Multiple threads modifying attributes concurrently
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        String key = "key-" + threadId + "-" + j;
                        session.withAttribute(key, "value-" + j);
                        session.getAttribute(key);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Then - Should complete without exceptions
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(session.attributes()).hasSize(threadCount * operationsPerThread);
        
        executor.shutdown();
    }
    
    @Test
    @DisplayName("Should support method chaining with withAttribute")
    void shouldSupportMethodChainingWithWithAttribute() {
        // Given
        McpSession session = new McpSession(
                "test-session",
                Instant.now(),
                new AtomicReference<>(Instant.now()),
                new ConcurrentHashMap<>()
        );
        
        // When
        McpSession result = session
                .withAttribute("key1", "value1")
                .withAttribute("key2", "value2")
                .withAttribute("key3", "value3");
        
        // Then
        assertThat(result).isSameAs(session);
        assertThat(session.attributes()).hasSize(3);
        assertThat(session.getAttribute("key1")).isEqualTo("value1");
        assertThat(session.getAttribute("key2")).isEqualTo("value2");
        assertThat(session.getAttribute("key3")).isEqualTo("value3");
    }
}
