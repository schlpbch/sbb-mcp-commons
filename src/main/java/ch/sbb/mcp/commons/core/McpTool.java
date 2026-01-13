package ch.sbb.mcp.commons.core;

import reactor.core.publisher.Mono;
import java.util.Map;

/**
 * Base interface for all MCP tools.
 *
 * <p>Each tool represents a specific capability exposed to AI agents via
 * the Model Context Protocol. Tools follow the MCP specification for
 * tool definitions and invocations.</p>
 *
 * <p>All tool implementations must be:</p>
 * <ul>
 *   <li>Reactive (return Mono&lt;?&gt; or Flux&lt;?&gt;)</li>
 *   <li>Thread-safe</li>
 *   <li>Idempotent for read operations</li>
 * </ul>
 *
 * @param <T> the result type of this tool
 * @see <a href="https://modelcontextprotocol.io/docs/concepts/tools">MCP Tools</a>
 */
public interface McpTool<T> {

    /**
     * Returns the unique name of this tool.
     *
     * <p>Tool names follow the pattern: {@code verbNoun} (e.g., getPlaceInfo,
     * searchPlaces, calculateRoute).</p>
     *
     * @return the tool name
     */
    String name();

    /**
     * Returns a brief one-line summary of what this tool does.
     *
     * <p>This summary provides a quick overview for AI agents to
     * understand the tool's primary purpose at a glance.</p>
     *
     * @return the tool summary
     */
    String summary();

    /**
     * Returns a human-readable description of what this tool does.
     *
     * <p>This description is exposed to AI agents to help them understand
     * when and how to use this tool.</p>
     *
     * @return the tool description
     */
    String description();

    /**
     * Returns the JSON Schema for the tool's input parameters.
     *
     * <p>The schema follows JSON Schema Draft 7 format and is used by
     * AI agents to construct valid tool invocations.</p>
     *
     * @return the input schema as a JSON string
     */
    String inputSchema();

    /**
     * Invokes the tool with the given arguments.
     *
     * <p>This method is called by the MCP runtime when an AI agent
     * invokes this tool. Arguments are validated against the input
     * schema before this method is called.</p>
     *
     * @param arguments the tool arguments as a map
     * @return a Mono containing the tool result
     */
    Mono<T> invoke(Map<String, Object> arguments);

    /**
     * Invokes the tool with the given arguments and optional session context.
     *
     * <p>This method allows tools to optionally use session context for
     * enhanced functionality like progress tracking or multi-turn operations.
     * The default implementation delegates to {@link #invoke(Map)} and ignores
     * the session ID.</p>
     *
     * <p>Tools that need session context should override this method.</p>
     *
     * @param arguments the tool arguments as a map
     * @param sessionId optional session ID for session-aware tools
     * @return a Mono containing the tool result
     */
    default Mono<T> invoke(Map<String, Object> arguments, java.util.Optional<String> sessionId) {
        return invoke(arguments);
    }

    /**
     * Returns whether this tool can modify state.
     *
     * <p>Read-only tools (returning false) can be safely cached and
     * retried. State-modifying tools (returning true) require additional
     * confirmation from the user in some MCP clients.</p>
     *
     * @return true if this tool modifies state
     */
    default boolean isStateModifying() {
        return false;
    }

    /**
     * Returns the tool's category for grouping in the UI.
     *
     * @return the category name
     */
    default String category() {
        return "general";
    }
}
