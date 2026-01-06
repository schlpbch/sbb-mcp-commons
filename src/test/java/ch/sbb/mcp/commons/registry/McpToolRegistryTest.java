package ch.sbb.mcp.commons.registry;

import ch.sbb.mcp.commons.core.McpTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("McpToolRegistry Tests")
class McpToolRegistryTest {

    private ApplicationContext mockContext;
    private McpToolRegistry registry;

    @BeforeEach
    void setUp() {
        mockContext = mock(ApplicationContext.class);
        registry = new McpToolRegistry(mockContext);
    }

    @Test
    @DisplayName("Should auto-discover McpTool beans from Spring context")
    void init_ShouldDiscoverTools() {
        // Given
        Map<String, McpTool<?>> toolBeans = new HashMap<>();
        toolBeans.put("testTool1", createMockTool("getTripInfo", "Get trip information", "Retrieves trip details", "{\"type\":\"object\"}"));
        toolBeans.put("testTool2", createMockTool("searchPlaces", "Search places", "Searches for places", "{\"type\":\"object\"}"));

        when(mockContext.getBeansOfType(McpTool.class)).thenReturn((Map) toolBeans);

        // When
        registry.init();

        // Then
        assertTrue(registry.hasTool("getTripInfo"));
        assertTrue(registry.hasTool("searchPlaces"));
        assertFalse(registry.hasTool("nonExistentTool"));
    }

    @Test
    @DisplayName("Should list all registered tools with correct metadata")
    void listTools_ShouldReturnToolInfo() {
        // Given
        Map<String, McpTool<?>> toolBeans = new HashMap<>();
        toolBeans.put("tool1", createMockTool("getWeather", "Get weather", "Retrieves weather data", "{\"type\":\"object\",\"properties\":{}}"));
        toolBeans.put("tool2", createMockTool("getStation", "Get station", "Retrieves station info", "{\"type\":\"object\"}"));

        when(mockContext.getBeansOfType(McpTool.class)).thenReturn((Map) toolBeans);
        registry.init();

        // When
        List<McpToolRegistry.ToolInfo> tools = registry.listTools();

        // Then
        assertEquals(2, tools.size());
        
        McpToolRegistry.ToolInfo weatherTool = tools.stream()
            .filter(t -> t.name().equals("getWeather"))
            .findFirst()
            .orElseThrow();
        
        assertEquals("getWeather", weatherTool.name());
        assertEquals("Get weather", weatherTool.summary());
        assertEquals("Retrieves weather data", weatherTool.description());
        assertEquals("{\"type\":\"object\",\"properties\":{}}", weatherTool.inputSchema());
    }

    @Test
    @DisplayName("Should invoke tool successfully with arguments")
    void invokeTool_WithValidTool_ShouldInvoke() {
        // Given
        McpTool<String> mockTool = createMockTool("testTool", "Test", "Test tool", "{}");
        Map<String, Object> args = Map.of("param1", "value1");
        when(mockTool.invoke(args)).thenReturn(Mono.just("result"));

        Map<String, McpTool<?>> toolBeans = Map.of("testTool", mockTool);
        when(mockContext.getBeansOfType(McpTool.class)).thenReturn((Map) toolBeans);
        registry.init();

        // When
        Mono<?> result = registry.invokeTool("testTool", args);

        // Then
        assertEquals("result", result.block());
        verify(mockTool).invoke(args);
    }

    @Test
    @DisplayName("Should return error for non-existent tool")
    void invokeTool_WithNonExistentTool_ShouldReturnError() {
        // Given
        when(mockContext.getBeansOfType(McpTool.class)).thenReturn(Map.of());
        registry.init();

        // When
        Mono<?> result = registry.invokeTool("nonExistentTool", Map.of());

        // Then
        assertThrows(IllegalArgumentException.class, result::block);
    }

    @Test
    @DisplayName("Should correctly identify registered tools")
    void hasTool_ShouldReturnCorrectStatus() {
        // Given
        Map<String, McpTool<?>> toolBeans = Map.of(
            "tool1", createMockTool("existingTool", "Summary", "Description", "{}")
        );
        when(mockContext.getBeansOfType(McpTool.class)).thenReturn((Map) toolBeans);
        registry.init();

        // Then
        assertTrue(registry.hasTool("existingTool"));
        assertFalse(registry.hasTool("nonExistentTool"));
        assertFalse(registry.hasTool(null));
    }

    @Test
    @DisplayName("Should handle empty context with no tools")
    void init_WithNoTools_ShouldHandleGracefully() {
        // Given
        when(mockContext.getBeansOfType(McpTool.class)).thenReturn(Map.of());

        // When
        registry.init();
        List<McpToolRegistry.ToolInfo> tools = registry.listTools();

        // Then
        assertTrue(tools.isEmpty());
        assertFalse(registry.hasTool("anyTool"));
    }

    @Test
    @DisplayName("Should throw exception on duplicate tool names")
    void init_WithDuplicateNames_ShouldThrowException() {
        // Given
        McpTool<?> tool1 = createMockTool("duplicateName", "First", "First tool", "{}");
        McpTool<?> tool2 = createMockTool("duplicateName", "Second", "Second tool", "{}");
        
        Map<String, McpTool<?>> toolBeans = new HashMap<>();
        toolBeans.put("bean1", tool1);
        toolBeans.put("bean2", tool2);

        when(mockContext.getBeansOfType(McpTool.class)).thenReturn((Map) toolBeans);

        // When/Then - should throw IllegalStateException due to duplicate keys
        assertThrows(IllegalStateException.class, () -> registry.init());
    }

    @Test
    @DisplayName("ToolInfo should serialize inputSchema as raw JSON")
    void toolInfo_ShouldSerializeInputSchemaAsRawJson() {
        // Given
        String jsonSchema = "{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"}}}";
        McpToolRegistry.ToolInfo toolInfo = new McpToolRegistry.ToolInfo(
            "testTool",
            "Test Summary",
            "Test Description",
            jsonSchema
        );

        // Then
        assertEquals("testTool", toolInfo.name());
        assertEquals("Test Summary", toolInfo.summary());
        assertEquals("Test Description", toolInfo.description());
        assertEquals(jsonSchema, toolInfo.inputSchema());
    }

    // Helper method to create mock tools
    private McpTool<String> createMockTool(String name, String summary, String description, String inputSchema) {
        McpTool<String> tool = mock(McpTool.class);
        when(tool.name()).thenReturn(name);
        when(tool.summary()).thenReturn(summary);
        when(tool.description()).thenReturn(description);
        when(tool.inputSchema()).thenReturn(inputSchema);
        return tool;
    }
}
