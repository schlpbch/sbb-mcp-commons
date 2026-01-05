package ch.sbb.mcp.commons.prompts;

import java.util.List;

/**
 * Represents an MCP prompt template.
 * 
 * <p>MCP prompts are pre-defined templates that guide AI assistants through
 * common travel scenarios by orchestrating multiple tools in intelligent workflows.</p>
 * 
 * @param name Unique prompt identifier (e.g., "find-nearby-stations")
 * @param description Human-readable description of the prompt's purpose
 * @param arguments List of arguments this prompt accepts
 */
public record McpPrompt(
    String name,
    String description,
    List<McpPromptArgument> arguments,
    String template
) {
    public McpPrompt {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Prompt name is required");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Prompt description is required");
        }
        if (arguments == null) {
            throw new IllegalArgumentException("Arguments list cannot be null (use empty list if no arguments)");
        }
        if (template == null || template.isBlank()) {
            throw new IllegalArgumentException("Prompt template is required");
        }
        // Make arguments immutable
        arguments = List.copyOf(arguments);
    }
}
