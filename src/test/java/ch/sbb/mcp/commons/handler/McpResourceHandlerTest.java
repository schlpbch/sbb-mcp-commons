package ch.sbb.mcp.commons.handler;

import ch.sbb.mcp.commons.protocol.McpRequest;
import ch.sbb.mcp.commons.protocol.McpResponse;
import ch.sbb.mcp.commons.resource.McpResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for McpResourceHandler.
 */
@DisplayName("MCP Resource Handler Tests")
class McpResourceHandlerTest {

    private McpResourceHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        
        // Create mock resources
        McpResource resource1 = new TestResource(
            "Test Resource 1",
            "First test resource",
            "/test/resource1",
            "TestModel1",
            "resource://test-resource-1",
            Map.of("data", "value1")
        );
        
        McpResource resource2 = new TestResource(
            "Test Resource 2",
            "Second test resource",
            "/test/resource2",
            "TestModel2",
            "resource://test-resource-2",
            Map.of("data", "value2")
        );
        
        handler = new McpResourceHandler(List.of(resource1, resource2), objectMapper);
    }

    @Test
    @DisplayName("should list all resources")
    void shouldListAllResources() {
        McpRequest request = new McpRequest("2.0", "1", "resources/list", Map.of());

        StepVerifier.create(handler.handleResourcesList(request))
            .assertNext(response -> {
                assertThat(response.jsonrpc()).isEqualTo("2.0");
                assertThat(response.id()).isEqualTo("1");
                assertThat(response.error()).isNull();
                
                @SuppressWarnings("unchecked")
                Map<String, Object> result = (Map<String, Object>) response.result();
                assertThat(result).containsKey("resources");
                
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> resources = (List<Map<String, Object>>) result.get("resources");
                assertThat(resources).hasSize(2);
                
                assertThat(resources.get(0)).containsEntry("name", "Test Resource 1");
                assertThat(resources.get(0)).containsEntry("uri", "resource://test-resource-1");
                assertThat(resources.get(0)).containsEntry("mimeType", "application/json");
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("should read resource by URI")
    void shouldReadResourceByUri() {
        Map<String, Object> params = Map.of("uri", "resource://test-resource-1");
        McpRequest request = new McpRequest("2.0", "2", "resources/read", params);

        StepVerifier.create(handler.handleResourcesRead(request))
            .assertNext(response -> {
                assertThat(response.error()).isNull();
                
                @SuppressWarnings("unchecked")
                Map<String, Object> result = (Map<String, Object>) response.result();
                
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> contents = (List<Map<String, Object>>) result.get("contents");
                assertThat(contents).hasSize(1);
                
                Map<String, Object> content = contents.get(0);
                assertThat(content.get("uri")).isEqualTo("resource://test-resource-1");
                assertThat(content.get("mimeType")).isEqualTo("application/json");
                assertThat(content.get("text")).isNotNull();
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("should return error for non-existent resource")
    void shouldReturnErrorForNonExistentResource() {
        Map<String, Object> params = Map.of("uri", "resource://non-existent");
        McpRequest request = new McpRequest("2.0", "3", "resources/read", params);

        StepVerifier.create(handler.handleResourcesRead(request))
            .assertNext(response -> {
                assertThat(response.error()).isNotNull();
                assertThat(response.error().code()).isEqualTo(McpResponse.McpError.INVALID_PARAMS);
                assertThat(response.error().message()).contains("Resource not found");
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("should return error for missing uri parameter")
    void shouldReturnErrorForMissingUri() {
        McpRequest request = new McpRequest("2.0", "4", "resources/read", Map.of());

        StepVerifier.create(handler.handleResourcesRead(request))
            .assertNext(response -> {
                assertThat(response.error()).isNotNull();
                assertThat(response.error().code()).isEqualTo(McpResponse.McpError.INVALID_PARAMS);
                assertThat(response.error().message()).contains("Missing uri parameter");
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("should handle resource templates list")
    void shouldHandleResourceTemplatesList() {
        McpRequest request = new McpRequest("2.0", "5", "resources/templates/list", Map.of());

        StepVerifier.create(handler.handleResourcesTemplatesList(request))
            .assertNext(response -> {
                assertThat(response.error()).isNull();
                
                @SuppressWarnings("unchecked")
                Map<String, Object> result = (Map<String, Object>) response.result();
                assertThat(result).containsKey("resourceTemplates");
            })
            .verifyComplete();
    }

    /**
     * Test implementation of McpResource.
     */
    private static class TestResource implements McpResource {
        private final String name;
        private final String description;
        private final String endpoint;
        private final String dataModel;
        private final String uri;
        private final Object data;

        TestResource(String name, String description, String endpoint, 
                    String dataModel, String uri, Object data) {
            this.name = name;
            this.description = description;
            this.endpoint = endpoint;
            this.dataModel = dataModel;
            this.uri = uri;
            this.data = data;
        }

        @Override
        public String getResourceName() {
            return name;
        }

        @Override
        public String getResourceDescription() {
            return description;
        }

        @Override
        public String getResourceEndpoint() {
            return endpoint;
        }

        @Override
        public String getResourceDataModel() {
            return dataModel;
        }

        @Override
        public String getResourceUri() {
            return uri;
        }

        @Override
        public Mono<Object> readResource() {
            return Mono.just(data);
        }
    }
}
