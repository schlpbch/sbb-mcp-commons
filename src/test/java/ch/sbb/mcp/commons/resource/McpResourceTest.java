package ch.sbb.mcp.commons.resource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("McpResource Tests")
class McpResourceTest {

    @Test
    @DisplayName("Default isAvailable should return true")
    void isAvailable_Default_ShouldReturnTrue() {
        // Given
        TestResource resource = new TestResource("Test Resource", "Test description");

        // When/Then
        assertTrue(resource.isAvailable());
    }

    @Test
    @DisplayName("Default getResourceUri should generate correct URI from name")
    void getResourceUri_Default_ShouldGenerateCorrectUri() {
        // Given
        TestResource resource = new TestResource("Service Calendar", "Calendar description");

        // When
        String uri = resource.getResourceUri();

        // Then
        assertEquals("resource://service-calendar", uri);
    }

    @Test
    @DisplayName("getResourceUri should handle spaces correctly")
    void getResourceUri_WithSpaces_ShouldConvertToHyphens() {
        // Given
        TestResource resource = new TestResource("My Test Resource", "Description");

        // When
        String uri = resource.getResourceUri();

        // Then
        assertEquals("resource://my-test-resource", uri);
    }

    @Test
    @DisplayName("getResourceUri should handle uppercase correctly")
    void getResourceUri_WithUppercase_ShouldConvertToLowercase() {
        // Given
        TestResource resource = new TestResource("UPPERCASE RESOURCE", "Description");

        // When
        String uri = resource.getResourceUri();

        // Then
        assertEquals("resource://uppercase-resource", uri);
    }

    @Test
    @DisplayName("getResourceUri should handle mixed case and spaces")
    void getResourceUri_WithMixedCaseAndSpaces_ShouldNormalize() {
        // Given
        TestResource resource = new TestResource("Travel Class Info", "Description");

        // When
        String uri = resource.getResourceUri();

        // Then
        assertEquals("resource://travel-class-info", uri);
    }

    @Test
    @DisplayName("getResourceUri should handle single word names")
    void getResourceUri_WithSingleWord_ShouldWork() {
        // Given
        TestResource resource = new TestResource("Calendar", "Description");

        // When
        String uri = resource.getResourceUri();

        // Then
        assertEquals("resource://calendar", uri);
    }

    @Test
    @DisplayName("All abstract methods should be callable")
    void abstractMethods_ShouldBeCallable() {
        // Given
        TestResource resource = new TestResource("Test", "Description");

        // When/Then
        assertNotNull(resource.getResourceName());
        assertNotNull(resource.getResourceDescription());
        assertNotNull(resource.getResourceEndpoint());
        assertNotNull(resource.getResourceDataModel());
        assertNotNull(resource.readResource());
    }

    @Test
    @DisplayName("readResource should return Mono with data")
    void readResource_ShouldReturnMonoWithData() {
        // Given
        TestResource resource = new TestResource("Test", "Description");
        resource.setData("test-data");

        // When
        Mono<Object> result = resource.readResource();

        // Then
        StepVerifier.create(result)
            .expectNext("test-data")
            .verifyComplete();
    }

    @Test
    @DisplayName("Custom isAvailable implementation should work")
    void isAvailable_CustomImplementation_ShouldWork() {
        // Given
        ConditionalResource resource = new ConditionalResource(false);

        // When/Then
        assertFalse(resource.isAvailable());

        // And when enabled
        resource.setEnabled(true);
        assertTrue(resource.isAvailable());
    }

    @Test
    @DisplayName("Custom getResourceUri implementation should work")
    void getResourceUri_CustomImplementation_ShouldWork() {
        // Given
        CustomUriResource resource = new CustomUriResource();

        // When
        String uri = resource.getResourceUri();

        // Then
        assertEquals("custom://my-special-resource", uri);
    }

    // Test implementation of McpResource
    static class TestResource implements McpResource {
        private final String name;
        private final String description;
        private Object data;

        TestResource(String name, String description) {
            this.name = name;
            this.description = description;
        }

        void setData(Object data) {
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
            return "/mcp/" + name.toLowerCase().replace(" ", "-");
        }

        @Override
        public String getResourceDataModel() {
            return name.replace(" ", "");
        }

        @Override
        public Mono<Object> readResource() {
            return Mono.justOrEmpty(data);
        }
    }

    // Test resource with conditional availability
    static class ConditionalResource implements McpResource {
        private boolean enabled;

        ConditionalResource(boolean enabled) {
            this.enabled = enabled;
        }

        void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public String getResourceName() {
            return "Conditional Resource";
        }

        @Override
        public String getResourceDescription() {
            return "A resource with conditional availability";
        }

        @Override
        public String getResourceEndpoint() {
            return "/mcp/conditional";
        }

        @Override
        public String getResourceDataModel() {
            return "ConditionalData";
        }

        @Override
        public boolean isAvailable() {
            return enabled;
        }

        @Override
        public Mono<Object> readResource() {
            return Mono.just("conditional-data");
        }
    }

    // Test resource with custom URI
    static class CustomUriResource implements McpResource {

        @Override
        public String getResourceName() {
            return "Custom Resource";
        }

        @Override
        public String getResourceDescription() {
            return "A resource with custom URI";
        }

        @Override
        public String getResourceEndpoint() {
            return "/mcp/custom";
        }

        @Override
        public String getResourceDataModel() {
            return "CustomData";
        }

        @Override
        public String getResourceUri() {
            return "custom://my-special-resource";
        }

        @Override
        public Mono<Object> readResource() {
            return Mono.just("custom-data");
        }
    }
}
