package ch.sbb.mcp.commons.handler;

import ch.sbb.mcp.commons.protocol.McpRequest;
import ch.sbb.mcp.commons.protocol.McpResponse;
import ch.sbb.mcp.commons.resource.McpResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Handler for MCP resource operations.
 * 
 * <p>Handles resource listing and reading operations according to the
 * Model Context Protocol specification.</p>
 * 
 * @since 1.8.0
 */
@Component
public class McpResourceHandler {
    
    private static final Logger log = LoggerFactory.getLogger(McpResourceHandler.class);
    
    private final List<McpResource> mcpResources;
    private final ObjectMapper objectMapper;
    
    public McpResourceHandler(
            List<McpResource> mcpResources,
            ObjectMapper objectMapper) {
        this.mcpResources = mcpResources;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Handles the resources/list request.
     * 
     * @param request The MCP request
     * @return Mono containing the response with list of resources
     */
    public Mono<McpResponse> handleResourcesList(McpRequest request) {
        List<Map<String, Object>> resources = mcpResources.stream()
            .filter(resource -> resource.getResourceUri() != null)
            .map(resource -> Map.<String, Object>of(
                "uri", resource.getResourceUri(),
                "name", resource.getResourceName() != null ? resource.getResourceName() : "Unknown",
                "description", resource.getResourceDescription() != null ? resource.getResourceDescription() : "",
                "mimeType", "application/json"
            ))
            .toList();
            
        log.info("Listing {} resources", resources.size());
        return Mono.just(McpResponse.success(request.id(), Map.of("resources", resources)));
    }
    
    /**
     * Handles the resources/read request.
     * 
     * @param request The MCP request
     * @return Mono containing the response with resource content
     */
    public Mono<McpResponse> handleResourcesRead(McpRequest request) {
        if (!(request.params() instanceof Map)) {
            return Mono.just(McpResponse.error(request.id(), McpResponse.McpError.invalidRequest("Missing or invalid params")));
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) request.params();
        String uri = (String) params.get("uri");
        
        if (uri == null) {
            return Mono.just(McpResponse.error(request.id(), McpResponse.McpError.invalidParams("Missing uri parameter")));
        }
        
        Optional<McpResource> matchingResource = mcpResources.stream()
            .filter(r -> r.getResourceUri() != null && r.getResourceUri().equals(uri))
            .findFirst();
            
        return matchingResource
            .map(resource -> resource.readResource()
                .map(content -> McpResponse.success(request.id(), Map.of(
                    "contents", List.of(Map.of(
                        "uri", uri,
                        "mimeType", "application/json",
                        "text", serializeToJson(content)
                    ))
                )))
                .onErrorResume(error -> {
                    log.error("Failed to read resource {}: {}", uri, error.getMessage(), error);
                    return Mono.just(McpResponse.error(
                        request.id(),
                        McpResponse.McpError.internalError("Failed to read resource: " + error.getMessage())
                    ));
                })
            )
            .orElseGet(() -> Mono.just(McpResponse.error(request.id(), McpResponse.McpError.invalidParams("Resource not found: " + uri))));
    }
    
    /**
     * Handles the resources/templates/list request.
     * 
     * @param request The MCP request
     * @return Mono containing the response with list of resource templates
     */
    public Mono<McpResponse> handleResourcesTemplatesList(McpRequest request) {
        List<Map<String, Object>> templates = List.of(
            Map.of(
                "uriTemplate", "co2://factors/{mode}",
                "name", "CO2 Emission Factor by Mode",
                "description", "Get emission factor for a specific transport mode",
                "mimeType", "application/json"
            )
        );
        
        log.info("Listing {} resource templates", templates.size());
        return Mono.just(McpResponse.success(request.id(), Map.of("resourceTemplates", templates)));
    }
    
    /**
     * Serializes an object to JSON string.
     * 
     * @param obj The object to serialize
     * @return JSON string representation
     */
    private String serializeToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize resource content to JSON", e);
            return "{}";
        }
    }
}
