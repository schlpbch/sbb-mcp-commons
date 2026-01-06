package ch.sbb.mcp.commons.sampling;

import reactor.core.publisher.Mono;

/**
 * Client interface for MCP sampling operations.
 * 
 * <p>This interface defines the contract for requesting LLM completions from MCP clients.
 * Implementations can be session-based (communicating with the connected client) or
 * mock-based (for testing).</p>
 * 
 * <p>The MCP sampling capability allows servers to request LLM completions from clients,
 * enabling sophisticated agentic behaviors like generating natural language explanations,
 * context-aware advice, and dynamic content synthesis.</p>
 * 
 * @see <a href="https://modelcontextprotocol.io/specification/2024-11-05/server/sampling">MCP Sampling Specification</a>
 */
public interface McpSamplingClient {
    
    /**
     * Request an LLM completion from the client.
     * 
     * <p>This method sends a sampling request to the MCP client, which will use its
     * configured LLM to generate a completion based on the provided messages and
     * system prompt.</p>
     * 
     * <p>The client may present a user approval dialog before executing the sampling
     * request, depending on its configuration.</p>
     * 
     * @param request The sampling request containing messages and preferences
     * @return A Mono emitting the sampling response with generated content
     * @throws IllegalStateException if sampling is not available
     */
    Mono<SamplingResponse> createMessage(SamplingRequest request);
    
    /**
     * Check if sampling is available.
     * 
     * <p>Sampling may not be available if:
     * <ul>
     *   <li>The client does not support the sampling capability</li>
     *   <li>No active session exists</li>
     *   <li>The client has disabled sampling</li>
     * </ul>
     * 
     * @return true if sampling requests can be made, false otherwise
     */
    boolean isAvailable();
}
