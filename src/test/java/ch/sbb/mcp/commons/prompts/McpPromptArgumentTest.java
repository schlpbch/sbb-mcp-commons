package ch.sbb.mcp.commons.prompts;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for McpPromptArgument record.
 */
class McpPromptArgumentTest {
    
    @Test
    void shouldCreateValidArgument() {
        // When
        McpPromptArgument arg = new McpPromptArgument("latitude", "Latitude coordinate", true);
        
        // Then
        assertThat(arg.name()).isEqualTo("latitude");
        assertThat(arg.description()).isEqualTo("Latitude coordinate");
        assertThat(arg.required()).isTrue();
    }
    
    @Test
    void shouldRejectNullName() {
        // When/Then
        assertThatThrownBy(() -> 
            new McpPromptArgument(null, "description", true)
        ).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("name is required");
    }
    
    @Test
    void shouldRejectBlankName() {
        // When/Then
        assertThatThrownBy(() -> 
            new McpPromptArgument("  ", "description", true)
        ).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("name is required");
    }
    
    @Test
    void shouldRejectNullDescription() {
        // When/Then
        assertThatThrownBy(() -> 
            new McpPromptArgument("name", null, true)
        ).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("description is required");
    }
    
    @Test
    void shouldRejectBlankDescription() {
        // When/Then
        assertThatThrownBy(() -> 
            new McpPromptArgument("name", "  ", true)
        ).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("description is required");
    }
    
    @Test
    void shouldAllowOptionalArgument() {
        // When
        McpPromptArgument arg = new McpPromptArgument("radius_km", "Search radius", false);
        
        // Then
        assertThat(arg.required()).isFalse();
    }
}
