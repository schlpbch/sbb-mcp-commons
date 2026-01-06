package ch.sbb.mcp.commons.sampling;

/**
 * Represents a message in an MCP sampling conversation.
 * 
 * <p>Messages are used to construct the conversation context for LLM sampling requests.
 * Each message has a role (either "user" or "assistant") and content.</p>
 * 
 * @param role The role of the message sender ("user" or "assistant")
 * @param content The message content
 */
public record SamplingMessage(
    String role,
    String content
) {
    /**
     * Creates a new SamplingMessage with validation.
     * 
     * @throws IllegalArgumentException if role or content is null or blank
     */
    public SamplingMessage {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("role cannot be null or blank");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content cannot be null or blank");
        }
        if (!role.equals("user") && !role.equals("assistant")) {
            throw new IllegalArgumentException("role must be 'user' or 'assistant'");
        }
    }
    
    /**
     * Creates a user message.
     * 
     * @param content The message content
     * @return A new SamplingMessage with role "user"
     */
    public static SamplingMessage user(String content) {
        return new SamplingMessage("user", content);
    }
    
    /**
     * Creates an assistant message.
     * 
     * @param content The message content
     * @return A new SamplingMessage with role "assistant"
     */
    public static SamplingMessage assistant(String content) {
        return new SamplingMessage("assistant", content);
    }
}
