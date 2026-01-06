package ch.sbb.mcp.commons.sampling;

import java.util.List;

/**
 * Request for MCP sampling (LLM completion).
 * 
 * <p>Represents a request to the client to generate an LLM completion based on
 * the provided messages and system prompt. This follows the MCP sampling specification.</p>
 * 
 * @param messages List of conversation messages (required, must not be empty)
 * @param systemPrompt Optional system prompt to guide the LLM
 * @param maxTokens Optional maximum number of tokens to generate
 * @param modelPreferences Optional preferences for model selection and parameters
 */
public record SamplingRequest(
    List<SamplingMessage> messages,
    String systemPrompt,
    Integer maxTokens,
    ModelPreferences modelPreferences
) {
    /**
     * Creates a new SamplingRequest with validation.
     * 
     * @throws IllegalArgumentException if messages is null/empty or maxTokens is invalid
     */
    public SamplingRequest {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("messages cannot be null or empty");
        }
        if (maxTokens != null && maxTokens <= 0) {
            throw new IllegalArgumentException("maxTokens must be positive");
        }
    }
    
    /**
     * Creates a simple sampling request with just messages.
     * 
     * @param messages List of conversation messages
     * @return A new SamplingRequest instance
     */
    public static SamplingRequest simple(List<SamplingMessage> messages) {
        return new SamplingRequest(messages, null, null, null);
    }
    
    /**
     * Creates a sampling request with messages and system prompt.
     * 
     * @param messages List of conversation messages
     * @param systemPrompt System prompt to guide the LLM
     * @return A new SamplingRequest instance
     */
    public static SamplingRequest withSystemPrompt(List<SamplingMessage> messages, String systemPrompt) {
        return new SamplingRequest(messages, systemPrompt, null, null);
    }
    
    /**
     * Creates a sampling request with messages, system prompt, and max tokens.
     * 
     * @param messages List of conversation messages
     * @param systemPrompt System prompt to guide the LLM
     * @param maxTokens Maximum tokens to generate
     * @return A new SamplingRequest instance
     */
    public static SamplingRequest withMaxTokens(
            List<SamplingMessage> messages, 
            String systemPrompt, 
            int maxTokens) {
        return new SamplingRequest(messages, systemPrompt, maxTokens, null);
    }
}
