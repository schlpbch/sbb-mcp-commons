package ch.sbb.mcp.commons.controller;

import ch.sbb.mcp.commons.handler.McpResourceHandler;
import ch.sbb.mcp.commons.prompts.McpPromptHandler;
import ch.sbb.mcp.commons.protocol.McpRequest;
import ch.sbb.mcp.commons.protocol.McpResponse;
import ch.sbb.mcp.commons.registry.McpToolRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Base controller for MCP servers providing standardized endpoint mappings.
 *
 * <p>This abstract controller provides common MCP protocol functionality including:</p>
 * <ul>
 *   <li>REST discovery endpoints for tools, resources, and prompts</li>
 *   <li>Core MCP protocol request handling</li>
 *   <li>Protocol version validation</li>
 *   <li>Standardized error handling</li>
 * </ul>
 *
 * <p>Server implementations should extend this class and implement the abstract methods
 * to provide server-specific behavior (e.g., session management, tool invocation).</p>
 *
 * <p><strong>Example usage:</strong></p>
 * <pre>
 * {@code @RestController}
 * {@code @RequestMapping("/mcp")}
 * public class MyMcpController extends BaseMcpController {
 *     {@code @Override}
 *     protected Mono&lt;McpResponse&gt; handleToolsCall(McpRequest request) {
 *         // Server-specific tool invocation logic
 *     }
 *
 *     {@code @Override}
 *     protected String getServerName() {
 *         return "my-mcp-server";
 *     }
 *
 *     {@code @Override}
 *     protected String getServerVersion() {
 *         return "1.0.0";
 *     }
 * }
 * </pre>
 *
 * @since 1.9.0
 */
@RestController
@RequestMapping("/mcp")
public abstract class BaseMcpController {

    private static final Logger log = LoggerFactory.getLogger(BaseMcpController.class);

    /**
     * MCP session header name as per MCP 2025-03-26 specification.
     */
    protected static final String SESSION_HEADER = "Mcp-Session-Id";

    /**
     * MCP protocol version header name as per MCP v2025-06-18 specification.
     */
    protected static final String PROTOCOL_VERSION_HEADER = "MCP-Protocol-Version";

    /**
     * Supported MCP protocol version.
     */
    protected static final String SUPPORTED_PROTOCOL_VERSION = "2025-03-26";

    protected final McpToolRegistry toolRegistry;
    protected final McpResourceHandler resourceHandler;
    protected final McpPromptHandler promptHandler;
    protected final ObjectMapper objectMapper;

    /**
     * Constructor for base MCP controller.
     *
     * @param toolRegistry Tool registry for tool discovery and invocation
     * @param resourceHandler Handler for resource operations
     * @param promptHandler Handler for prompt operations
     * @param objectMapper JSON object mapper
     */
    protected BaseMcpController(
            McpToolRegistry toolRegistry,
            McpResourceHandler resourceHandler,
            McpPromptHandler promptHandler,
            ObjectMapper objectMapper) {
        this.toolRegistry = toolRegistry;
        this.resourceHandler = resourceHandler;
        this.promptHandler = promptHandler;
        this.objectMapper = objectMapper;
    }

    /**
     * REST endpoint to list all available MCP tools.
     * Provides a simple HTTP GET interface for tool discovery.
     *
     * @return Map containing list of tools with their schemas
     */
    @GetMapping(value = "/tools", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> listToolsRest() {
        log.debug("REST: Listing MCP tools");
        McpRequest request = new McpRequest("2.0", "rest", "tools/list", Map.of());
        return handleToolsList(request)
            .map(response -> {
                if (response.result() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) response.result();
                    @SuppressWarnings("unchecked")
                    List<?> tools = (List<?>) result.get("tools");
                    log.info("REST: Returning {} tools", tools != null ? tools.size() : 0);
                    return result;
                }
                return Map.<String, Object>of("tools", List.of());
            });
    }

    /**
     * REST endpoint to list all available MCP resources.
     * Provides a simple HTTP GET interface for resource discovery.
     *
     * @return Map containing list of resources with their URIs
     */
    @GetMapping(value = "/resources", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> listResourcesRest() {
        log.debug("REST: Listing MCP resources");
        McpRequest request = new McpRequest("2.0", "rest", "resources/list", Map.of());
        return resourceHandler.handleResourcesList(request)
            .map(response -> {
                if (response.result() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) response.result();
                    @SuppressWarnings("unchecked")
                    List<?> resources = (List<?>) result.get("resources");
                    log.info("REST: Returning {} resources", resources != null ? resources.size() : 0);
                    return result;
                }
                return Map.<String, Object>of("resources", List.of());
            });
    }

    /**
     * REST endpoint to list all available MCP prompts.
     * Provides a simple HTTP GET interface for prompt discovery.
     *
     * @return Map containing list of prompts with their arguments
     */
    @GetMapping(value = "/prompts", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> listPromptsRest() {
        log.debug("REST: Listing MCP prompts");
        McpRequest request = new McpRequest("2.0", "rest", "prompts/list", Map.of());
        return promptHandler.handlePromptsList(request)
            .map(response -> {
                if (response.result() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) response.result();
                    log.info("REST: Returning prompts");
                    return result;
                }
                return Map.<String, Object>of("prompts", List.of());
            });
    }

    /**
     * Handles the initialize request.
     *
     * @param request The MCP request
     * @return Mono containing the initialize response
     */
    protected Mono<McpResponse> handleInitialize(McpRequest request) {
        Map<String, Object> result = Map.of(
            "protocolVersion", SUPPORTED_PROTOCOL_VERSION,
            "serverInfo", Map.of(
                "name", getServerName(),
                "version", getServerVersion()
            ),
            "capabilities", Map.of(
                "tools", Map.of(),
                "resources", Map.of(),
                "prompts", Map.of()
            )
        );

        return Mono.just(McpResponse.success(request.id(), result));
    }

    /**
     * Handles the tools/list request.
     *
     * @param request The MCP request
     * @return Mono containing the tools list response
     */
    protected Mono<McpResponse> handleToolsList(McpRequest request) {
        var tools = toolRegistry.listTools();
        return Mono.just(McpResponse.success(request.id(), Map.of("tools", tools)));
    }

    /**
     * Core MCP protocol request processing.
     *
     * <p>Routes requests to appropriate handlers based on the method name.</p>
     *
     * @param request The MCP request
     * @return Mono containing the MCP response
     */
    protected Mono<McpResponse> processRequest(McpRequest request) {
        if (!request.isValid()) {
            return Mono.just(McpResponse.error(
                request.id(),
                McpResponse.McpError.invalidRequest("Invalid JSON-RPC 2.0 request")
            ));
        }

        return switch (request.method()) {
            case "initialize" -> handleInitialize(request);
            case "tools/list" -> handleToolsList(request);
            case "tools/call" -> handleToolsCall(request);
            case "resources/list" -> resourceHandler.handleResourcesList(request);
            case "resources/read" -> resourceHandler.handleResourcesRead(request);
            case "resources/templates/list" -> resourceHandler.handleResourcesTemplatesList(request);
            case "prompts/list" -> promptHandler.handlePromptsList(request);
            case "prompts/get" -> promptHandler.handlePromptsGet(request);
            default -> Mono.just(McpResponse.error(
                request.id(),
                McpResponse.McpError.methodNotFound(request.method())
            ));
        };
    }

    /**
     * Handles tool invocation requests.
     *
     * <p>Server implementations must override this method to provide
     * server-specific tool invocation logic (e.g., with or without session context).</p>
     *
     * @param request The MCP request containing tool name and arguments
     * @return Mono containing the tool invocation response
     */
    protected abstract Mono<McpResponse> handleToolsCall(McpRequest request);

    /**
     * Returns the server name for the initialize response.
     *
     * @return The server name (e.g., "journey-service-mcp")
     */
    protected abstract String getServerName();

    /**
     * Returns the server version for the initialize response.
     *
     * @return The server version (e.g., "1.0.0")
     */
    protected abstract String getServerVersion();
}
