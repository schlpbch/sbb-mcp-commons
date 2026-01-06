package ch.sbb.mcp.commons.sampling;

/**
 * Response from an MCP sampling request.
 * 
 * <p>Contains the generated content from the LLM, along with metadata about
 * the model used, stop reason, and token usage.</p>
 * 
 * @param content The generated text content
 * @param model The model that generated the response
 * @param stopReason Why the generation stopped (e.g., "end_turn", "max_tokens")
 * @param usage Token usage statistics
 */
public record SamplingResponse(
    String content,
    String model,
    String stopReason,
    TokenUsage usage
) {
    /**
     * Token usage statistics for a sampling response.
     * 
     * @param promptTokens Number of tokens in the prompt
     * @param completionTokens Number of tokens in the completion
     * @param totalTokens Total tokens used (prompt + completion)
     */
    public record TokenUsage(
        int promptTokens,
        int completionTokens,
        int totalTokens
    ) {
        /**
         * Creates a new TokenUsage with validation.
         * 
         * @throws IllegalArgumentException if any token count is negative
         */
        public TokenUsage {
            if (promptTokens < 0) {
                throw new IllegalArgumentException("promptTokens cannot be negative");
            }
            if (completionTokens < 0) {
                throw new IllegalArgumentException("completionTokens cannot be negative");
            }
            if (totalTokens < 0) {
                throw new IllegalArgumentException("totalTokens cannot be negative");
            }
            if (totalTokens != promptTokens + completionTokens) {
                throw new IllegalArgumentException(
                    "totalTokens must equal promptTokens + completionTokens");
            }
        }
    }
    
    /**
     * Creates a new SamplingResponse with validation.
     * 
     * @throws IllegalArgumentException if content is null or blank
     */
    public SamplingResponse {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content cannot be null or blank");
        }
    }
    
    /**
     * Creates a simple sampling response with just content.
     * 
     * @param content The generated content
     * @return A new SamplingResponse instance
     */
    public static SamplingResponse simple(String content) {
        return new SamplingResponse(content, null, null, null);
    }
    
    /**
     * Creates a sampling response with content and model.
     * 
     * @param content The generated content
     * @param model The model that generated the response
     * @return A new SamplingResponse instance
     */
    public static SamplingResponse withModel(String content, String model) {
        return new SamplingResponse(content, model, null, null);
    }
}
