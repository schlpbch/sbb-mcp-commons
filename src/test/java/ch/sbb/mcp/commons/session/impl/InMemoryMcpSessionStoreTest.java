package ch.sbb.mcp.commons.session.impl;

import ch.sbb.mcp.commons.session.McpSession;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InMemoryMcpSessionStore Integration Tests")
class InMemoryMcpSessionStoreTest {
    
    private InMemoryMcpSessionStore sessionStore;
    private MeterRegistry meterRegistry;
    
    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        sessionStore = new InMemoryMcpSessionStore(Duration.ofSeconds(10), meterRegistry);
    }
    
    @Test
    @DisplayName("Should create session with unique ID")
    void shouldCreateSessionWithUniqueId() {
        StepVerifier.create(sessionStore.createSession())
                .assertNext(session -> {
                    assertThat(session).isNotNull();
                    assertThat(session.sessionId()).isNotNull();
                    assertThat(session.sessionId()).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
                    assertThat(session.createdAt()).isNotNull();
                    assertThat(session.lastAccessedAt()).isNotNull();
                    assertThat(session.attributes()).isEmpty();
                })
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should create multiple sessions with different IDs")
    void shouldCreateMultipleSessionsWithDifferentIds() {
        StepVerifier.create(
                sessionStore.createSession()
                        .zipWith(sessionStore.createSession())
        )
                .assertNext(tuple -> {
                    assertThat(tuple.getT1().sessionId())
                            .isNotEqualTo(tuple.getT2().sessionId());
                })
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should retrieve existing session")
    void shouldRetrieveExistingSession() {
        StepVerifier.create(
                sessionStore.createSession()
                        .flatMap(created -> sessionStore.getSession(created.sessionId())
                                .map(retrieved -> new Object[]{created, retrieved}))
        )
                .assertNext(sessions -> {
                    McpSession created = (McpSession) sessions[0];
                    McpSession retrieved = (McpSession) sessions[1];
                    assertThat(retrieved.sessionId()).isEqualTo(created.sessionId());
                    assertThat(retrieved.createdAt()).isEqualTo(created.createdAt());
                })
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should return empty for non-existent session")
    void shouldReturnEmptyForNonExistentSession() {
        StepVerifier.create(sessionStore.getSession("non-existent-id"))
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should touch session and update last accessed time")
    void shouldTouchSessionAndUpdateLastAccessedTime() throws InterruptedException {
        StepVerifier.create(
                sessionStore.createSession()
                        .flatMap(session -> sessionStore.getSession(session.sessionId())
                                .flatMap(before -> {
                                    try {
                                        Thread.sleep(10);  // Small delay
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                    return sessionStore.touchSession(session.sessionId())
                                            .then(sessionStore.getSession(session.sessionId()))
                                            .map(after -> new Object[]{before, after});
                                }))
        )
                .assertNext(sessions -> {
                    McpSession before = (McpSession) sessions[0];
                    McpSession after = (McpSession) sessions[1];
                    assertThat(after.lastAccessedAt().get())
                            .isAfterOrEqualTo(before.lastAccessedAt().get());
                })
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should delete session")
    void shouldDeleteSession() {
        StepVerifier.create(
                sessionStore.createSession()
                        .flatMap(session -> sessionStore.deleteSession(session.sessionId())
                                .then(sessionStore.getSession(session.sessionId())))
        )
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should validate existing non-expired session")
    void shouldValidateExistingNonExpiredSession() {
        StepVerifier.create(
                sessionStore.createSession()
                        .flatMap(session -> sessionStore.isValidSession(session.sessionId()))
        )
                .expectNext(true)
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should invalidate non-existent session")
    void shouldInvalidateNonExistentSession() {
        StepVerifier.create(sessionStore.isValidSession("non-existent-id"))
                .expectNext(false)
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should invalidate expired session")
    void shouldInvalidateExpiredSession() {
        // Create store with very short TTL
        InMemoryMcpSessionStore shortTtlStore = new InMemoryMcpSessionStore(
                Duration.ofMillis(100), 
                meterRegistry
        );
        
        StepVerifier.create(
                shortTtlStore.createSession()
                        .delayElement(Duration.ofMillis(150))
                        .flatMap(session -> shortTtlStore.isValidSession(session.sessionId()))
        )
                .expectNext(false)
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should count active sessions")
    void shouldCountActiveSessions() {
        StepVerifier.create(
                sessionStore.createSession()
                        .then(sessionStore.createSession())
                        .then(sessionStore.createSession())
                        .then(sessionStore.getActiveSessionCount())
        )
                .expectNext(3L)
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should handle concurrent session creation")
    void shouldHandleConcurrentSessionCreation() {
        StepVerifier.create(
                reactor.core.publisher.Flux.range(1, 10)
                        .flatMap(i -> sessionStore.createSession())
                        .collectList()
                        .flatMap(sessions -> sessionStore.getActiveSessionCount()
                                .map(count -> new Object[]{sessions, count}))
        )
                .assertNext(result -> {
                    @SuppressWarnings("unchecked")
                    java.util.List<McpSession> sessions = (java.util.List<McpSession>) result[0];
                    Long count = (Long) result[1];
                    
                    // All sessions should have unique IDs
                    assertThat(sessions).hasSize(10);
                    assertThat(sessions.stream().map(McpSession::sessionId).distinct().count())
                            .isEqualTo(10);
                    assertThat(count).isEqualTo(10L);
                })
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should cleanup expired sessions on scheduled job")
    void shouldCleanupExpiredSessionsOnScheduledJob() throws InterruptedException {
        // Create store with very short TTL
        InMemoryMcpSessionStore shortTtlStore = new InMemoryMcpSessionStore(
                Duration.ofMillis(100), 
                meterRegistry
        );
        
        // Create 3 sessions
        StepVerifier.create(
                shortTtlStore.createSession()
                        .then(shortTtlStore.createSession())
                        .then(shortTtlStore.createSession())
                        .then(shortTtlStore.getActiveSessionCount())
        )
                .expectNext(3L)
                .verifyComplete();
        
        // Wait for sessions to expire
        Thread.sleep(150);
        
        // Run cleanup manually
        shortTtlStore.cleanupExpiredSessions();
        
        // Verify all sessions are cleaned up
        StepVerifier.create(shortTtlStore.getActiveSessionCount())
                .expectNext(0L)
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should increment metrics on session creation")
    void shouldIncrementMetricsOnSessionCreation() {
        StepVerifier.create(sessionStore.createSession())
                .expectNextCount(1)
                .verifyComplete();
        
        assertThat(meterRegistry.counter("mcp.sessions.created", "store", "in-memory").count())
                .isEqualTo(1.0);
    }
    
    @Test
    @DisplayName("Should track active session count in metrics")
    void shouldTrackActiveSessionCountInMetrics() {
        StepVerifier.create(
                sessionStore.createSession()
                        .then(sessionStore.createSession())
        )
                .expectNextCount(1)  // Expect the second session
                .verifyComplete();
        
        Double gaugeValue = meterRegistry.find("mcp.sessions.active")
                .tag("store", "in-memory")
                .gauge()
                .value();
        
        assertThat(gaugeValue).isEqualTo(2.0);
    }
}
