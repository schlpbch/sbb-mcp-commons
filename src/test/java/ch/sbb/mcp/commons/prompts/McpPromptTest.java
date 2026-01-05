package ch.sbb.mcp.commons.prompts;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for McpPrompt record.
 */
class McpPromptTest {
    
    @Test
    void shouldCreateValidPrompt() {
        // Given
        List<McpPromptArgument> args = List.of(
            new McpPromptArgument("arg1", "First argument", true)
        );
        
        // When
        McpPrompt prompt = new McpPrompt("test-prompt", "Test description", args, "Template text");
        
        // Then
        assertThat(prompt.name()).isEqualTo("test-prompt");
        assertThat(prompt.description()).isEqualTo("Test description");
        assertThat(prompt.arguments()).hasSize(1);
        assertThat(prompt.template()).isEqualTo("Template text");
    }
    
    @Test
    void shouldRejectNullName() {
        // When/Then
        assertThatThrownBy(() -> 
            new McpPrompt(null, "description", List.of(), "template")
        ).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("name is required");
    }
    
    @Test
    void shouldRejectBlankName() {
        // When/Then
        assertThatThrownBy(() -> 
            new McpPrompt("  ", "description", List.of(), "template")
        ).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("name is required");
    }
    
    @Test
    void shouldRejectNullDescription() {
        // When/Then
        assertThatThrownBy(() -> 
            new McpPrompt("name", null, List.of(), "template")
        ).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("description is required");
    }
    
    @Test
    void shouldRejectNullArguments() {
        // When/Then
        assertThatThrownBy(() -> 
            new McpPrompt("name", "description", null, "template")
        ).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Arguments list cannot be null");
    }
    
    @Test
    void shouldAcceptEmptyArgumentsList() {
        // When
        McpPrompt prompt = new McpPrompt("name", "description", List.of(), "template");
        
        // Then
        assertThat(prompt.arguments()).isEmpty();
    }
    
    @Test
    void shouldMakeArgumentsImmutable() {
        // Given
        List<McpPromptArgument> args = List.of(
            new McpPromptArgument("arg1", "First argument", true)
        );
        McpPrompt prompt = new McpPrompt("test", "description", args, "template");
        
        // When/Then
        assertThatThrownBy(() -> 
            prompt.arguments().add(new McpPromptArgument("arg2", "Second", false))
        ).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldRejectNullTemplate() {
        // When/Then
        assertThatThrownBy(() -> 
            new McpPrompt("name", "description", List.of(), null)
        ).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Prompt template is required");
    }

    @Test
    void shouldRejectBlankTemplate() {
        // When/Then
        assertThatThrownBy(() -> 
            new McpPrompt("name", "description", List.of(), "  ")
        ).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Prompt template is required");
    }
}
