package ch.sbb.mcp.commons.context;

/**
 * Thread-local context holder for MCP request context.
 * 
 * <p>Provides access to request-scoped information such as session ID
 * without needing to pass it through every method call.</p>
 * 
 * <p>This is similar to Spring's SecurityContextHolder pattern.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * // In controller/handler
 * McpRequestContext.setSessionId(sessionId);
 * try {
 *     // Process request
 * } finally {
 *     McpRequestContext.clear();
 * }
 * 
 * // In tool
 * String sessionId = McpRequestContext.getSessionId().orElse(null);
 * }</pre>
 */
public class McpRequestContext {
    
    private static final ThreadLocal<String> SESSION_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CORRELATION_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> PROGRESS_TOKEN = new ThreadLocal<>();
    
    /**
     * Set the session ID for the current request.
     * 
     * @param sessionId Session identifier
     */
    public static void setSessionId(String sessionId) {
        SESSION_ID.set(sessionId);
    }
    
    /**
     * Get the session ID for the current request.
     * 
     * @return Session ID, or empty if not set
     */
    public static java.util.Optional<String> getSessionId() {
        return java.util.Optional.ofNullable(SESSION_ID.get());
    }
    
    /**
     * Set the correlation ID for the current request.
     * 
     * @param correlationId Correlation identifier
     */
    public static void setCorrelationId(String correlationId) {
        CORRELATION_ID.set(correlationId);
    }
    
    /**
     * Get the correlation ID for the current request.
     * 
     * @return Correlation ID, or empty if not set
     */
    public static java.util.Optional<String> getCorrelationId() {
        return java.util.Optional.ofNullable(CORRELATION_ID.get());
    }

    /**
     * Set the progress token for the current request.
     *
     * @param progressToken Progress token
     */
    public static void setProgressToken(String progressToken) {
        PROGRESS_TOKEN.set(progressToken);
    }

    /**
     * Get the progress token for the current request.
     *
     * @return Progress token, or empty if not set
     */
    public static java.util.Optional<String> getProgressToken() {
        return java.util.Optional.ofNullable(PROGRESS_TOKEN.get());
    }
    
    /**
     * Clear all context for the current thread.
     * 
     * <p>Should be called in a finally block to prevent memory leaks.</p>
     */
    public static void clear() {
        SESSION_ID.remove();
        CORRELATION_ID.remove();
        PROGRESS_TOKEN.remove();
    }
    
    /**
     * Check if a session ID is set for the current request.
     * 
     * @return True if session ID is set
     */
    public static boolean hasSessionId() {
        return SESSION_ID.get() != null;
    }
}
