package ch.sbb.mcp.commons.prompts;

/**
 * Represents an argument for an MCP prompt.
 * 
 * @param name The argument name (e.g., "latitude", "radius_km")
 * @param description Human-readable description of the argument
 * @param required Whether this argument is mandatory
 */
public record McpPromptArgument(
    String name,
    String description,
    boolean required
) {
    public McpPromptArgument {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Argument name is required");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Argument description is required");
        }
    }
}
