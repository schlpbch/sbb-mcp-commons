package ch.sbb.mcp.commons.prompts.config;

import ch.sbb.mcp.commons.prompts.McpPrompt;
import ch.sbb.mcp.commons.prompts.McpPromptHandler;
import ch.sbb.mcp.commons.prompts.McpPromptProvider;
import ch.sbb.mcp.commons.prompts.McpPromptRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for McpPromptAutoConfiguration.
 */
@SpringBootTest
@ContextConfiguration(classes = {
    McpPromptAutoConfiguration.class,
    McpPromptAutoConfigurationTest.TestConfig.class
})
class McpPromptAutoConfigurationTest {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private McpPromptRegistry promptRegistry;
    
    @Autowired
    private McpPromptHandler promptHandler;
    
    @Test
    void shouldAutoConfigurePromptRegistry() {
        // Then
        assertThat(promptRegistry).isNotNull();
        assertThat(applicationContext.getBean(McpPromptRegistry.class)).isSameAs(promptRegistry);
    }
    
    @Test
    void shouldAutoConfigurePromptHandler() {
        // Then
        assertThat(promptHandler).isNotNull();
        assertThat(applicationContext.getBean(McpPromptHandler.class)).isSameAs(promptHandler);
    }
    
    @Test
    void shouldDiscoverPromptProviders() {
        // When
        List<McpPrompt> prompts = promptRegistry.listPrompts();
        
        // Then - should discover the test provider
        assertThat(prompts).hasSize(2);
        assertThat(prompts).extracting(McpPrompt::name)
            .containsExactlyInAnyOrder("test-prompt-1", "test-prompt-2");
    }
    
    @Test
    void shouldWireHandlerWithRegistry() {
        // When - handler should be able to access prompts from registry
        boolean hasPrompt = promptRegistry.hasPrompt("test-prompt-1");
        
        // Then
        assertThat(hasPrompt).isTrue();
    }
    
    /**
     * Test configuration providing a sample prompt provider.
     */
    @Configuration
    static class TestConfig {
        
        @Bean
        public McpPromptProvider testPromptProvider() {
            return () -> List.of(
                new McpPrompt(
                    "test-prompt-1",
                    "First test prompt",
                    List.of(),
                    "Template for test prompt 1"
                ),
                new McpPrompt(
                    "test-prompt-2",
                    "Second test prompt",
                    List.of(),
                    "Template for test prompt 2"
                )
            );
        }
    }
}
