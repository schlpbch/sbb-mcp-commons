package ch.sbb.mcp.commons.prompts;

import ch.sbb.mcp.commons.protocol.McpRequest;
import ch.sbb.mcp.commons.protocol.McpResponse;
import ch.sbb.mcp.commons.prompts.McpPromptRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Handler for MCP prompt operations.
 * 
 * <p>Handles prompt listing and retrieval operations.</p>
 * 
 * <p>This class is automatically configured by {@link ch.sbb.mcp.commons.prompts.config.McpPromptAutoConfiguration}.</p>
 */
public class McpPromptHandler {
    
    private static final Logger log = LoggerFactory.getLogger(McpPromptHandler.class);
    
    private final McpPromptRegistry promptRegistry;
    
    public McpPromptHandler(McpPromptRegistry promptRegistry) {
        this.promptRegistry = promptRegistry;
    }
    
    /**
     * Handles the prompts/list request.
     * 
     * @param request The MCP request
     * @return Mono containing the response with list of prompts
     */
    public Mono<McpResponse> handlePromptsList(McpRequest request) {
        // If promptRegistry is null (e.g., in tests), return empty list
        if (promptRegistry == null) {
            log.warn("PromptRegistry is null, returning empty prompts list");
            return Mono.just(McpResponse.success(request.id(), Map.of("prompts", List.of())));
        }
        
        List<Map<String, Object>> promptsResponse = promptRegistry.listPrompts().stream()
            .map(prompt -> Map.<String, Object>of(
                "name", prompt.name(),
                "description", prompt.description(),
                "arguments", prompt.arguments().stream()
                    .map(arg -> Map.of(
                        "name", arg.name(),
                        "description", arg.description(),
                        "required", arg.required()
                    ))
                    .toList()
            ))
            .toList();
        
        log.info("Listing {} prompts", promptsResponse.size());
        return Mono.just(McpResponse.success(request.id(), Map.of("prompts", promptsResponse)));
    }
    
    /**
     * Handles the prompts/get request.
     * 
     * @param request The MCP request
     * @return Mono containing the response with prompt details
     */
    public Mono<McpResponse> handlePromptsGet(McpRequest request) {
        // If promptRegistry is null (e.g., in tests), return error
        if (promptRegistry == null) {
            log.warn("PromptRegistry is null, cannot get prompt");
            return Mono.just(McpResponse.error(
                request.id(),
                McpResponse.McpError.internalError("Prompt registry not available")
            ));
        }
        
        // Validate params
        if (!(request.params() instanceof Map)) {
            return Mono.just(McpResponse.error(
                request.id(),
                McpResponse.McpError.invalidParams("Missing or invalid params")
            ));
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) request.params();
        String name = (String) params.get("name");
        @SuppressWarnings("unchecked")
        Map<String, Object> args = (Map<String, Object>) params.get("arguments");
        
        // Validate prompt name
        if (name == null || name.isBlank()) {
            return Mono.just(McpResponse.error(
                request.id(),
                McpResponse.McpError.invalidParams("Prompt name is required and cannot be blank")
            ));
        }
        
        // Get prompt from registry
        return promptRegistry.getPrompt(name)
            .map(prompt -> {
                String template = prompt.template();
                String promptText = template;
                
                // Simple template substitution
                if (args != null) {
                    for (Map.Entry<String, Object> entry : args.entrySet()) {
                        promptText = promptText.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
                    }
                }

                Map<String, Object> result = Map.of(
                    "name", prompt.name(),
                    "description", prompt.description(),
                    "arguments", prompt.arguments().stream()
                        .map(arg -> Map.of(
                            "name", arg.name(),
                            "description", arg.description(),
                            "required", arg.required()
                        ))
                        .toList(),
                    "messages", List.of(Map.of(
                        "role", "user",
                        "content", Map.of(
                            "type", "text",
                            "text", promptText
                        )
                    ))
                );
                log.info("Retrieved prompt: {}", name);
                return McpResponse.success(request.id(), result);
            })
            .map(Mono::just)
            .orElseGet(() -> {
                log.warn("Prompt not found: {}", name);
                return Mono.just(McpResponse.error(
                    request.id(),
                    McpResponse.McpError.invalidParams("Prompt not found: " + name)
                ));
            });
    }
}
