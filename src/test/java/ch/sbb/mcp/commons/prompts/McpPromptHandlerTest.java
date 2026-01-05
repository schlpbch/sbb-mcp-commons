package ch.sbb.mcp.commons.prompts;

import ch.sbb.mcp.commons.protocol.McpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for McpPromptHandler.
 */
class McpPromptHandlerTest {
    
    private McpPromptRegistry promptRegistry;
    private McpPromptHandler handler;
    private ApplicationContext applicationContext;
    
    @BeforeEach
    void setUp() {
        // Use real registry instead of mocking to avoid Java 25 Mockito issues
        applicationContext = mock(ApplicationContext.class);
        promptRegistry = new McpPromptRegistry(applicationContext);
        handler = new McpPromptHandler(promptRegistry);
    }
    
    private void setupPromptsInRegistry(McpPrompt... prompts) {
        McpPromptProvider provider = () -> List.of(prompts);
        when(applicationContext.getBeansOfType(McpPromptProvider.class))
            .thenReturn(Map.of("testProvider", provider));
        promptRegistry.init();
    }
    
    @Test
    void shouldHandlePromptsListRequest() {
        // Given
        setupPromptsInRegistry(
            new McpPrompt(
                "test-prompt",
                "Test description",
                List.of(new McpPromptArgument("arg1", "Argument 1", true)),
                "Template"
            )
        );
        
        McpRequest request = new McpRequest("2.0", "req-1", "prompts/list", null);
        
        // When/Then
        StepVerifier.create(handler.handlePromptsList(request))
            .assertNext(response -> {
                assertThat(response.id()).isEqualTo("req-1");
                assertThat(response.result()).isNotNull();
                
                @SuppressWarnings("unchecked")
                Map<String, Object> result = (Map<String, Object>) response.result();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> promptsList = (List<Map<String, Object>>) result.get("prompts");
                
                assertThat(promptsList).hasSize(1);
                assertThat(promptsList.get(0).get("name")).isEqualTo("test-prompt");
                assertThat(promptsList.get(0).get("description")).isEqualTo("Test description");
            })
            .verifyComplete();
    }
    
    @Test
    void shouldHandleEmptyPromptsList() {
        // Given
        when(applicationContext.getBeansOfType(McpPromptProvider.class))
            .thenReturn(Map.of());
        promptRegistry.init();
        
        McpRequest request = new McpRequest("2.0", "req-1", "prompts/list", null);
        
        // When/Then
        StepVerifier.create(handler.handlePromptsList(request))
            .assertNext(response -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> result = (Map<String, Object>) response.result();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> promptsList = (List<Map<String, Object>>) result.get("prompts");
                
                assertThat(promptsList).isEmpty();
            })
            .verifyComplete();
    }
    
    @Test
    void shouldHandleNullRegistryInList() {
        // Given
        handler = new McpPromptHandler(null);
        McpRequest request = new McpRequest("2.0", "req-1", "prompts/list", null);
        
        // When/Then
        StepVerifier.create(handler.handlePromptsList(request))
            .assertNext(response -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> result = (Map<String, Object>) response.result();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> promptsList = (List<Map<String, Object>>) result.get("prompts");
                
                assertThat(promptsList).isEmpty();
            })
            .verifyComplete();
    }
    
    @Test
    void shouldHandlePromptsGetRequest() {
        // Given
        setupPromptsInRegistry(
            new McpPrompt(
                "test-prompt",
                "Test description",
                List.of(new McpPromptArgument("location", "Location name", true)),
                "Find places near {location}"
            )
        );
        
        Map<String, Object> params = Map.of(
            "name", "test-prompt",
            "arguments", Map.of("location", "Zurich")
        );
        McpRequest request = new McpRequest("2.0", "req-1", "prompts/get", params);
        
        // When/Then
        StepVerifier.create(handler.handlePromptsGet(request))
            .assertNext(response -> {
                assertThat(response.id()).isEqualTo("req-1");
                
                @SuppressWarnings("unchecked")
                Map<String, Object> result = (Map<String, Object>) response.result();
                assertThat(result.get("name")).isEqualTo("test-prompt");
                assertThat(result.get("description")).isEqualTo("Test description");
                
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> messages = (List<Map<String, Object>>) result.get("messages");
                assertThat(messages).hasSize(1);
                
                @SuppressWarnings("unchecked")
                Map<String, Object> content = (Map<String, Object>) messages.get(0).get("content");
                assertThat(content.get("text")).isEqualTo("Find places near Zurich");
            })
            .verifyComplete();
    }
    
    @Test
    void shouldHandleTemplateSubstitution() {
        // Given
        setupPromptsInRegistry(
            new McpPrompt(
                "multi-arg",
                "Test",
                List.of(),
                "Hello {name}, you are {age} years old"
            )
        );
        
        Map<String, Object> params = Map.of(
            "name", "multi-arg",
            "arguments", Map.of("name", "Alice", "age", "30")
        );
        McpRequest request = new McpRequest("2.0", "req-1", "prompts/get", params);
        
        // When/Then
        StepVerifier.create(handler.handlePromptsGet(request))
            .assertNext(response -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> result = (Map<String, Object>) response.result();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> messages = (List<Map<String, Object>>) result.get("messages");
                @SuppressWarnings("unchecked")
                Map<String, Object> content = (Map<String, Object>) messages.get(0).get("content");
                
                assertThat(content.get("text")).isEqualTo("Hello Alice, you are 30 years old");
            })
            .verifyComplete();
    }
    
    @Test
    void shouldHandleGetWithoutArguments() {
        // Given
        setupPromptsInRegistry(
            new McpPrompt(
                "no-args",
                "Test",
                List.of(),
                "Static template"
            )
        );
        
        Map<String, Object> params = Map.of("name", "no-args");
        McpRequest request = new McpRequest("2.0", "req-1", "prompts/get", params);
        
        // When/Then
        StepVerifier.create(handler.handlePromptsGet(request))
            .assertNext(response -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> result = (Map<String, Object>) response.result();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> messages = (List<Map<String, Object>>) result.get("messages");
                @SuppressWarnings("unchecked")
                Map<String, Object> content = (Map<String, Object>) messages.get(0).get("content");
                
                assertThat(content.get("text")).isEqualTo("Static template");
            })
            .verifyComplete();
    }
    
    @Test
    void shouldReturnErrorForNonExistentPrompt() {
        // Given
        when(applicationContext.getBeansOfType(McpPromptProvider.class))
            .thenReturn(Map.of());
        promptRegistry.init();
        
        Map<String, Object> params = Map.of("name", "non-existent");
        McpRequest request = new McpRequest("2.0", "req-1", "prompts/get", params);
        
        // When/Then
        StepVerifier.create(handler.handlePromptsGet(request))
            .assertNext(response -> {
                assertThat(response.error()).isNotNull();
                assertThat(response.error().message()).contains("Prompt not found");
            })
            .verifyComplete();
    }
    
    @Test
    void shouldReturnErrorForMissingParams() {
        // Given
        McpRequest request = new McpRequest("2.0", "req-1", "prompts/get", null);
        
        // When/Then
        StepVerifier.create(handler.handlePromptsGet(request))
            .assertNext(response -> {
                assertThat(response.error()).isNotNull();
                assertThat(response.error().code()).isEqualTo(-32602);
                assertThat(response.error().message()).contains("Missing or invalid params");
            })
            .verifyComplete();
    }
    
    @Test
    void shouldReturnErrorForInvalidParams() {
        // Given
        McpRequest request = new McpRequest("2.0", "req-1", "prompts/get", "invalid");
        
        // When/Then
        StepVerifier.create(handler.handlePromptsGet(request))
            .assertNext(response -> {
                assertThat(response.error()).isNotNull();
                assertThat(response.error().message()).contains("Missing or invalid params");
            })
            .verifyComplete();
    }
    
    @Test
    void shouldReturnErrorForMissingName() {
        // Given
        Map<String, Object> params = Map.of("arguments", Map.of());
        McpRequest request = new McpRequest("2.0", "req-1", "prompts/get", params);
        
        // When/Then
        StepVerifier.create(handler.handlePromptsGet(request))
            .assertNext(response -> {
                assertThat(response.error()).isNotNull();
                assertThat(response.error().message()).contains("Prompt name is required");
            })
            .verifyComplete();
    }
    
    @Test
    void shouldReturnErrorForBlankName() {
        // Given
        Map<String, Object> params = Map.of("name", "  ");
        McpRequest request = new McpRequest("2.0", "req-1", "prompts/get", params);
        
        // When/Then
        StepVerifier.create(handler.handlePromptsGet(request))
            .assertNext(response -> {
                assertThat(response.error()).isNotNull();
                assertThat(response.error().message()).contains("Prompt name is required");
            })
            .verifyComplete();
    }
    
    @Test
    void shouldHandleNullRegistryInGet() {
        // Given
        handler = new McpPromptHandler(null);
        Map<String, Object> params = Map.of("name", "test");
        McpRequest request = new McpRequest("2.0", "req-1", "prompts/get", params);
        
        // When/Then
        StepVerifier.create(handler.handlePromptsGet(request))
            .assertNext(response -> {
                assertThat(response.error()).isNotNull();
                assertThat(response.error().message()).contains("Prompt registry not available");
            })
            .verifyComplete();
    }
    
    @Test
    void shouldIncludeArgumentsInResponse() {
        // Given
        setupPromptsInRegistry(
            new McpPrompt(
                "with-args",
                "Test",
                List.of(
                    new McpPromptArgument("arg1", "First arg", true),
                    new McpPromptArgument("arg2", "Second arg", false)
                ),
                "Template"
            )
        );
        
        Map<String, Object> params = Map.of("name", "with-args");
        McpRequest request = new McpRequest("2.0", "req-1", "prompts/get", params);
        
        // When/Then
        StepVerifier.create(handler.handlePromptsGet(request))
            .assertNext(response -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> result = (Map<String, Object>) response.result();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> arguments = (List<Map<String, Object>>) result.get("arguments");
                
                assertThat(arguments).hasSize(2);
                assertThat(arguments.get(0).get("name")).isEqualTo("arg1");
                assertThat(arguments.get(0).get("required")).isEqualTo(true);
                assertThat(arguments.get(1).get("name")).isEqualTo("arg2");
                assertThat(arguments.get(1).get("required")).isEqualTo(false);
            })
            .verifyComplete();
    }
}
