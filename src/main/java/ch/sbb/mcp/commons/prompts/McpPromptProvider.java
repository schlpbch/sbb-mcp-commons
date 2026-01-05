package ch.sbb.mcp.commons.prompts;

import java.util.List;

/**
 * Provider interface for MCP prompts.
 * 
 * <p>Implementations should be Spring components (@Component, @Service) that
 * provide one or more MCP prompts. The McpPromptRegistry will auto-discover
 * all providers at application startup.</p>
 * 
 * <p>Example:</p>
 * <pre>
 * @Component
 * public class JourneyPrompts implements McpPromptProvider {
 *     @Override
 *     public List&lt;McpPrompt&gt; getPrompts() {
 *         return List.of(
 *             new McpPrompt("find-nearby-stations", ...),
 *             new McpPrompt("monitor-station", ...)
 *         );
 *     }
 * }
 * </pre>
 * 
 * @see McpPrompt
 * @see McpPromptRegistry
 */
public interface McpPromptProvider {
    
    /**
     * Get all prompts provided by this provider.
     * 
     * @return List of prompts (never null, may be empty)
     */
    List<McpPrompt> getPrompts();
}
