package ch.sbb.mcp.commons.prompts;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.*;

/**
 * Registry for MCP prompts with auto-discovery.
 * 
 * <p>Automatically discovers all McpPromptProvider beans in the application
 * context and registers their prompts at startup.</p>
 * 
 * @see McpPromptProvider
 * @see McpPrompt
 */
public class McpPromptRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(McpPromptRegistry.class);
    
    private final ApplicationContext applicationContext;
    private final Map<String, McpPrompt> prompts = new LinkedHashMap<>();
    
    public McpPromptRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @PostConstruct
    public void init() {
        discoverAndRegisterPrompts();
        log.info("Initialized MCP Prompt Registry with {} prompts", prompts.size());
    }
    
    /**
     * Auto-discover all McpPromptProvider beans and register their prompts.
     */
    private void discoverAndRegisterPrompts() {
        Map<String, McpPromptProvider> providers = 
            applicationContext.getBeansOfType(McpPromptProvider.class);
        
        log.info("Discovered {} prompt providers", providers.size());
        
        for (Map.Entry<String, McpPromptProvider> entry : providers.entrySet()) {
            String beanName = entry.getKey();
            McpPromptProvider provider = entry.getValue();
            
            List<McpPrompt> providerPrompts = provider.getPrompts();
            log.debug("Provider '{}' provides {} prompts", beanName, providerPrompts.size());
            
            for (McpPrompt prompt : providerPrompts) {
                registerPrompt(prompt);
            }
        }
    }
    
    /**
     * Register a prompt in the registry.
     * 
     * @param prompt The prompt to register
     * @throws IllegalArgumentException if a prompt with the same name already exists
     */
    private void registerPrompt(McpPrompt prompt) {
        if (prompts.containsKey(prompt.name())) {
            throw new IllegalArgumentException("Prompt already registered: " + prompt.name());
        }
        prompts.put(prompt.name(), prompt);
        log.debug("Registered prompt: {}", prompt.name());
    }
    
    /**
     * List all registered prompts.
     * 
     * @return Unmodifiable list of all prompts
     */
    public List<McpPrompt> listPrompts() {
        return List.copyOf(prompts.values());
    }
    
    /**
     * Get a specific prompt by name.
     * 
     * @param name The prompt name
     * @return Optional containing the prompt if found
     */
    public Optional<McpPrompt> getPrompt(String name) {
        return Optional.ofNullable(prompts.get(name));
    }
    
    /**
     * Check if a prompt exists.
     * 
     * @param name The prompt name
     * @return true if the prompt exists
     */
    public boolean hasPrompt(String name) {
        return prompts.containsKey(name);
    }
}
