package ch.sbb.mcp.commons.prompts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for McpPromptRegistry.
 */
class McpPromptRegistryTest {
    
    private ApplicationContext applicationContext;
    private McpPromptRegistry registry;
    
    @BeforeEach
    void setUp() {
        applicationContext = mock(ApplicationContext.class);
        registry = new McpPromptRegistry(applicationContext);
    }
    
    @Test
    void shouldDiscoverProvidersOnInit() {
        // Given
        McpPromptProvider provider1 = () -> List.of(
            new McpPrompt("prompt1", "Description 1", List.of(), "Template 1")
        );
        McpPromptProvider provider2 = () -> List.of(
            new McpPrompt("prompt2", "Description 2", List.of(), "Template 2")
        );
        
        when(applicationContext.getBeansOfType(McpPromptProvider.class))
            .thenReturn(Map.of(
                "provider1", provider1,
                "provider2", provider2
            ));
        
        // When
        registry.init();
        
        // Then
        assertThat(registry.listPrompts()).hasSize(2);
        assertThat(registry.hasPrompt("prompt1")).isTrue();
        assertThat(registry.hasPrompt("prompt2")).isTrue();
    }
    
    @Test
    void shouldHandleProviderWithMultiplePrompts() {
        // Given
        McpPromptProvider provider = () -> List.of(
            new McpPrompt("prompt1", "Description 1", List.of(), "Template 1"),
            new McpPrompt("prompt2", "Description 2", List.of(), "Template 2"),
            new McpPrompt("prompt3", "Description 3", List.of(), "Template 3")
        );
        
        when(applicationContext.getBeansOfType(McpPromptProvider.class))
            .thenReturn(Map.of("provider", provider));
        
        // When
        registry.init();
        
        // Then
        assertThat(registry.listPrompts()).hasSize(3);
    }
    
    @Test
    void shouldRejectDuplicatePromptNames() {
        // Given
        McpPromptProvider provider1 = () -> List.of(
            new McpPrompt("duplicate", "Description 1", List.of(), "Template 1")
        );
        McpPromptProvider provider2 = () -> List.of(
            new McpPrompt("duplicate", "Description 2", List.of(), "Template 2")
        );
        
        when(applicationContext.getBeansOfType(McpPromptProvider.class))
            .thenReturn(Map.of(
                "provider1", provider1,
                "provider2", provider2
            ));
        
        // When/Then
        assertThatThrownBy(() -> registry.init())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Prompt already registered: duplicate");
    }
    
    @Test
    void shouldGetPromptByName() {
        // Given
        McpPrompt expectedPrompt = new McpPrompt("test", "Description", List.of(), "Template");
        McpPromptProvider provider = () -> List.of(expectedPrompt);
        
        when(applicationContext.getBeansOfType(McpPromptProvider.class))
            .thenReturn(Map.of("provider", provider));
        
        registry.init();
        
        // When
        Optional<McpPrompt> result = registry.getPrompt("test");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedPrompt);
    }
    
    @Test
    void shouldReturnEmptyForNonExistentPrompt() {
        // Given
        when(applicationContext.getBeansOfType(McpPromptProvider.class))
            .thenReturn(Map.of());
        
        registry.init();
        
        // When
        Optional<McpPrompt> result = registry.getPrompt("non-existent");
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    void shouldCheckPromptExistence() {
        // Given
        McpPromptProvider provider = () -> List.of(
            new McpPrompt("existing", "Description", List.of(), "Template")
        );
        
        when(applicationContext.getBeansOfType(McpPromptProvider.class))
            .thenReturn(Map.of("provider", provider));
        
        registry.init();
        
        // When/Then
        assertThat(registry.hasPrompt("existing")).isTrue();
        assertThat(registry.hasPrompt("non-existent")).isFalse();
    }
    
    @Test
    void shouldReturnImmutablePromptList() {
        // Given
        McpPromptProvider provider = () -> List.of(
            new McpPrompt("test", "Description", List.of(), "Template")
        );
        
        when(applicationContext.getBeansOfType(McpPromptProvider.class))
            .thenReturn(Map.of("provider", provider));
        
        registry.init();
        
        // When
        List<McpPrompt> prompts = registry.listPrompts();
        
        // Then
        assertThatThrownBy(() -> 
            prompts.add(new McpPrompt("new", "New", List.of(), "Template"))
        ).isInstanceOf(UnsupportedOperationException.class);
    }
    
    @Test
    void shouldHandleNoProviders() {
        // Given
        when(applicationContext.getBeansOfType(McpPromptProvider.class))
            .thenReturn(Map.of());
        
        // When
        registry.init();
        
        // Then
        assertThat(registry.listPrompts()).isEmpty();
    }
    
    @Test
    void shouldPreservePromptOrder() {
        // Given
        McpPromptProvider provider = () -> List.of(
            new McpPrompt("prompt1", "Description 1", List.of(), "Template 1"),
            new McpPrompt("prompt2", "Description 2", List.of(), "Template 2"),
            new McpPrompt("prompt3", "Description 3", List.of(), "Template 3")
        );
        
        when(applicationContext.getBeansOfType(McpPromptProvider.class))
            .thenReturn(Map.of("provider", provider));
        
        registry.init();
        
        // When
        List<McpPrompt> prompts = registry.listPrompts();
        
        // Then - order should be preserved (LinkedHashMap)
        assertThat(prompts).extracting(McpPrompt::name)
            .containsExactly("prompt1", "prompt2", "prompt3");
    }
}
