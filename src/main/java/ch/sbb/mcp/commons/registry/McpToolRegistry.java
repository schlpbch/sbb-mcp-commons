package ch.sbb.mcp.commons.registry;

import ch.sbb.mcp.commons.core.McpTool;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Registry for auto-discovering and managing MCP tools.
 * 
 * <p>Automatically discovers all Spring beans implementing {@link McpTool}
 * and provides methods for listing tools and invoking them.</p>
 */
@Service
public class McpToolRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(McpToolRegistry.class);
    
    private final ApplicationContext applicationContext;
    private Map<String, McpTool<?>> tools;
    
    public McpToolRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @PostConstruct
    public void init() {
        // Auto-discover all McpTool beans
        tools = applicationContext.getBeansOfType(McpTool.class).values().stream()
            .collect(Collectors.toMap(
                McpTool::name,
                tool -> tool
            ));
        
        log.info("Discovered {} MCP tools: {}", tools.size(), tools.keySet());
    }
    
    /**
     * Get all registered tools.
     */
    public List<ToolInfo> listTools() {
        return tools.values().stream()
            .map(tool -> new ToolInfo(
                tool.name(),
                tool.summary(),
                tool.description(),
                tool.inputSchema()
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * Invoke a tool by name.
     */
    public Mono<?> invokeTool(String toolName, Map<String, Object> arguments) {
        McpTool<?> tool = tools.get(toolName);
        if (tool == null) {
            return Mono.error(new IllegalArgumentException("Tool not found: " + toolName));
        }
        
        return tool.invoke(arguments);
    }
    
    /**
     * Check if a tool exists.
     */
    public boolean hasTool(String toolName) {
        return tools.containsKey(toolName);
    }
    
    /**
     * Tool information for MCP clients.
     */
    public record ToolInfo(
        String name,
        String summary,
        String description,
        @com.fasterxml.jackson.annotation.JsonRawValue
        String inputSchema
    ) {}
}
