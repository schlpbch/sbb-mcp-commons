package ch.sbb.mcp.commons.session;

import reactor.core.publisher.Mono;

/**
 * Interface for managing MCP sessions with reactive operations.
 * 
 * <p>This interface defines the contract for session storage implementations,
 * supporting both in-memory and distributed (Redis) backends. All operations
 * are non-blocking and return Project Reactor types.
 * 
 * <p><strong>Implementations:</strong>
 * <ul>
 *   <li>{@link ch.sbb.mcp.commons.session.impl.InMemoryMcpSessionStore} - Single-instance, in-memory storage</li>
 *   <li>{@link ch.sbb.mcp.commons.session.impl.RedisMcpSessionStore} - Distributed, Redis-backed storage</li>
 * </ul>
 * 
 * <p><strong>Thread Safety:</strong> Implementations must be thread-safe and
 * support concurrent access from multiple threads.
 */
public interface McpSessionStore {
    
    /**
     * Creates a new session with a unique identifier.
     * 
     * <p>The session ID is generated using a cryptographically secure UUID v4.
     * The session is initialized with the current timestamp for both creation
     * and last access times.
     * 
     * @return A {@link Mono} emitting the newly created session
     */
    Mono<McpSession> createSession();
    
    /**
     * Retrieves a session by its identifier.
     * 
     * @param sessionId The unique session identifier
     * @return A {@link Mono} emitting the session if found, or empty if not found
     */
    Mono<McpSession> getSession(String sessionId);
    
    /**
     * Updates the last accessed timestamp for a session.
     * 
     * <p>This operation is called on every request to keep the session alive
     * and prevent expiration. For Redis implementations, this renews the TTL.
     * 
     * @param sessionId The unique session identifier
     * @return A {@link Mono} that completes when the touch operation is done
     */
    Mono<Void> touchSession(String sessionId);
    
    /**
     * Deletes a session by its identifier.
     * 
     * <p>This is called when a client explicitly terminates a session via
     * the DELETE endpoint, or when sessions are cleaned up due to expiration.
     * 
     * @param sessionId The unique session identifier
     * @return A {@link Mono} that completes when the deletion is done
     */
    Mono<Void> deleteSession(String sessionId);
    
    /**
     * Checks if a session is valid (exists and not expired).
     * 
     * <p>This is used for session validation on every request. A session is
     * considered valid if it exists and has not exceeded its TTL.
     * 
     * @param sessionId The unique session identifier
     * @return A {@link Mono} emitting {@code true} if valid, {@code false} otherwise
     */
    Mono<Boolean> isValidSession(String sessionId);
    
    /**
     * Gets the count of currently active sessions.
     * 
     * <p>This is used for monitoring and metrics collection. The count includes
     * only non-expired sessions.
     * 
     * @return A {@link Mono} emitting the number of active sessions
     */
    Mono<Long> getActiveSessionCount();

    /**
     * Explicitly saves a modified session back to the store.
     * 
     * @param session The session to save
     * @return A {@link Mono} that completes when the save operation is done
     */
    Mono<Void> saveSession(McpSession session);
}
