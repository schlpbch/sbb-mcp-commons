package ch.sbb.mcp.commons.session;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents an MCP session with thread-safe state management.
 * 
 * <p>This record implements the MCP 2025-03-26 specification for session lifecycle management.
 * Sessions are identified by a cryptographically secure UUID and track creation time,
 * last access time, and custom attributes.
 * 
 * <p><strong>Thread Safety:</strong> This class is thread-safe. The {@code lastAccessedAt}
 * field uses {@link AtomicReference} for lock-free updates, and {@code attributes} uses
 * {@link ConcurrentHashMap} for concurrent access.
 * 
 * @param sessionId Unique session identifier (UUID v4)
 * @param createdAt Timestamp when the session was created
 * @param lastAccessedAt Atomic reference to the last access timestamp (thread-safe)
 * @param attributes Concurrent map for storing session-scoped data
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public record McpSession(
    String sessionId,
    Instant createdAt,
    AtomicReference<Instant> lastAccessedAt,
    ConcurrentHashMap<String, Object> attributes
) {
    /**
     * Internal attribute key for tracking if the session has received the 'notifications/initialized' message.
     */
    public static final String INITIALIZED_ATTRIBUTE = "mcp:initialized";
    
    /**
     * Checks if the session has been officially initialized by the client.
     * 
     * @return {@code true} if initialized, {@code false} otherwise
     */
    public boolean isInitialized() {
        return Boolean.TRUE.equals(attributes.get(INITIALIZED_ATTRIBUTE));
    }

    /**
     * Marks the session as initialized.
     */
    public void setInitialized() {
        attributes.put(INITIALIZED_ATTRIBUTE, true);
    }
    
    /**
     * Updates the last accessed timestamp to the current time.
     * 
     * <p>This method is thread-safe and uses atomic operations to ensure
     * concurrent calls don't result in race conditions.
     */
    public void touch() {
        lastAccessedAt.set(Instant.now());
    }
    
    /**
     * Checks if the session has expired based on the given TTL.
     * 
     * @param ttl Time-to-live duration for the session
     * @return {@code true} if the session has expired, {@code false} otherwise
     */
    public boolean isExpired(Duration ttl) {
        Instant lastAccess = lastAccessedAt.get();
        Instant expirationTime = lastAccess.plus(ttl);
        return Instant.now().isAfter(expirationTime);
    }
    
    /**
     * Creates a new session with the given attribute added.
     * 
     * <p>This is a convenience method for adding attributes in a functional style.
     * Note that this modifies the existing attributes map (not creating a new session).
     * 
     * @param key Attribute key
     * @param value Attribute value
     * @return This session instance (for method chaining)
     */
    public McpSession withAttribute(String key, Object value) {
        attributes.put(key, value);
        return this;
    }
    
    /**
     * Gets an attribute value by key.
     * 
     * @param key Attribute key
     * @return Attribute value, or {@code null} if not found
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
    
    /**
     * Removes an attribute by key.
     * 
     * @param key Attribute key
     * @return Previous value, or {@code null} if not found
     */
    public Object removeAttribute(String key) {
        return attributes.remove(key);
    }
    
    /**
     * Gets the age of the session (time since creation).
     * 
     * @return Duration since session creation
     */
    public Duration getAge() {
        return Duration.between(createdAt, Instant.now());
    }
    
    /**
     * Gets the idle time (time since last access).
     * 
     * @return Duration since last access
     */
    public Duration getIdleTime() {
        return Duration.between(lastAccessedAt.get(), Instant.now());
    }
}
