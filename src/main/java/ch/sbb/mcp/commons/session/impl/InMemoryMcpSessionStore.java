package ch.sbb.mcp.commons.session.impl;

import ch.sbb.mcp.commons.session.McpSession;
import ch.sbb.mcp.commons.session.McpSessionStore;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * In-memory implementation of {@link McpSessionStore} for single-instance deployments.
 * 
 * <p>This implementation uses a {@link ConcurrentHashMap} for thread-safe session storage
 * and a scheduled cleanup job to remove expired sessions. It is activated when Redis is
 * not available (fallback implementation).
 * 
 * <p><strong>Activation:</strong> This bean is created only when {@link RedisConnectionFactory}
 * is not available in the application context.
 * 
 * <p><strong>Limitations:</strong>
 * <ul>
 *   <li>Sessions are not shared across multiple application instances</li>
 *   <li>Sessions are lost on application restart</li>
 *   <li>Memory usage grows with session count (bounded by TTL and cleanup)</li>
 * </ul>
 * 
 * <p><strong>Performance:</strong> All operations complete in < 1ms (P99).
 */
@Component
@ConditionalOnMissingBean(RedisConnectionFactory.class)
public class InMemoryMcpSessionStore implements McpSessionStore {
    
    private static final Logger log = LoggerFactory.getLogger(InMemoryMcpSessionStore.class);
    
    private final ConcurrentHashMap<String, McpSession> sessions = new ConcurrentHashMap<>();
    private final Duration ttl;
    private final Counter sessionsCreated;
    private final Counter sessionsExpired;
    private final Counter sessionsDeleted;
    
    public InMemoryMcpSessionStore(
            @Value("${mcp.session.ttl:PT1H}") Duration ttl,
            MeterRegistry meterRegistry) {
        this.ttl = ttl;
        
        // Register metrics
        this.sessionsCreated = Counter.builder("mcp.sessions.created")
                .description("Total number of sessions created")
                .tag("store", "in-memory")
                .register(meterRegistry);
        
        this.sessionsExpired = Counter.builder("mcp.sessions.expired")
                .description("Total number of sessions expired")
                .tag("store", "in-memory")
                .register(meterRegistry);
        
        this.sessionsDeleted = Counter.builder("mcp.sessions.deleted")
                .description("Total number of sessions explicitly deleted")
                .tag("store", "in-memory")
                .register(meterRegistry);
        
        Gauge.builder("mcp.sessions.active", sessions, ConcurrentHashMap::size)
                .description("Current number of active sessions")
                .tag("store", "in-memory")
                .register(meterRegistry);
        
        log.info("InMemoryMcpSessionStore initialized with TTL: {}", ttl);
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
            
            sessions.put(sessionId, session);
            sessionsCreated.increment();
            
            log.debug("Created session: {}", sessionId);
            return session;
        });
    }
    
    @Override
    public Mono<McpSession> getSession(String sessionId) {
        return Mono.fromCallable(() -> sessions.get(sessionId));
    }
    
    @Override
    public Mono<Void> touchSession(String sessionId) {
        return Mono.fromRunnable(() -> {
            McpSession session = sessions.get(sessionId);
            if (session != null) {
                session.touch();
                log.trace("Touched session: {}", sessionId);
            }
        });
    }
    
    @Override
    public Mono<Void> deleteSession(String sessionId) {
        return Mono.fromRunnable(() -> {
            McpSession removed = sessions.remove(sessionId);
            if (removed != null) {
                sessionsDeleted.increment();
                log.debug("Deleted session: {}", sessionId);
            }
        });
    }
    
    @Override
    public Mono<Boolean> isValidSession(String sessionId) {
        return Mono.fromCallable(() -> {
            McpSession session = sessions.get(sessionId);
            if (session == null) {
                return false;
            }
            
            if (session.isExpired(ttl)) {
                // Remove expired session immediately
                sessions.remove(sessionId);
                sessionsExpired.increment();
                log.debug("Session expired during validation: {}", sessionId);
                return false;
            }
            
            return true;
        });
    }
    
    @Override
    public Mono<Long> getActiveSessionCount() {
        return Mono.fromCallable(() -> (long) sessions.size());
    }

    @Override
    public Mono<Void> saveSession(McpSession session) {
        return Mono.fromRunnable(() -> {
            sessions.put(session.sessionId(), session);
            log.debug("Saved session manually: {}", session.sessionId());
        });
    }
    
    /**
     * Scheduled cleanup job to remove expired sessions.
     * 
     * <p>Runs every 5 minutes by default (configurable via {@code mcp.session.cleanup-interval}).
     * This prevents memory leaks from abandoned sessions that are never explicitly deleted.
     */
    @Scheduled(fixedDelayString = "${mcp.session.cleanup-interval:300000}")
    public void cleanupExpiredSessions() {
        int initialSize = sessions.size();
        int removed = 0;
        
        for (var entry : sessions.entrySet()) {
            if (entry.getValue().isExpired(ttl)) {
                sessions.remove(entry.getKey());
                sessionsExpired.increment();
                removed++;
            }
        }
        
        if (removed > 0) {
            log.info("Cleaned up {} expired sessions (total: {} -> {})", 
                    removed, initialSize, sessions.size());
        } else {
            log.debug("Cleanup job completed, no expired sessions found (total: {})", sessions.size());
        }
    }
}
