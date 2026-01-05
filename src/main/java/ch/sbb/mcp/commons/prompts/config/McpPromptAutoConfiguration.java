package ch.sbb.mcp.commons.prompts.config;

import ch.sbb.mcp.commons.prompts.McpPromptHandler;
import ch.sbb.mcp.commons.prompts.McpPromptRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for MCP prompt infrastructure.
 * 
 * <p>Automatically registers the {@link McpPromptRegistry} and {@link McpPromptHandler}
 * beans, enabling prompt discovery and handling without requiring component scanning
 * of the commons package in host applications.
 */
@AutoConfiguration
public class McpPromptAutoConfiguration {
    
    private static final Logger log = LoggerFactory.getLogger(McpPromptAutoConfiguration.class);
    
    /**
     * Creates the MCP Prompt Registry bean.
     * 
     * <p>The registry automatically discovers all {@link ch.sbb.mcp.commons.prompts.McpPromptProvider}
     * beans in the application context and registers their prompts at startup.
     * 
     * @param applicationContext The Spring application context
     * @return The configured prompt registry
     */
    @Bean
    public McpPromptRegistry mcpPromptRegistry(ApplicationContext applicationContext) {
        log.info("Auto-configuring McpPromptRegistry");
        return new McpPromptRegistry(applicationContext);
    }
    
    /**
     * Creates the MCP Prompt Handler bean.
     * 
     * <p>The handler provides prompt listing and retrieval operations for MCP clients.
     * 
     * @param promptRegistry The prompt registry
     * @return The configured prompt handler
     */
    @Bean
    public McpPromptHandler mcpPromptHandler(McpPromptRegistry promptRegistry) {
        log.info("Auto-configuring McpPromptHandler");
        return new McpPromptHandler(promptRegistry);
    }
}
