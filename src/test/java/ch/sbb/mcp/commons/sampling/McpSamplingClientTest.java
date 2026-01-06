package ch.sbb.mcp.commons.sampling;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for McpSamplingClient interface.
 */
class McpSamplingClientTest {
    
    @Test
    void testMockSamplingClient_IsAvailable() {
        McpSamplingClient client = new MockSamplingClient(true);
        
        assertTrue(client.isAvailable());
    }
    
    @Test
    void testMockSamplingClient_IsNotAvailable() {
        McpSamplingClient client = new MockSamplingClient(false);
        
        assertFalse(client.isAvailable());
    }
    
    @Test
    void testMockSamplingClient_CreateMessage_Success() {
        McpSamplingClient client = new MockSamplingClient(true);
        
        SamplingRequest request = SamplingRequest.withSystemPrompt(
            List.of(SamplingMessage.user("Explain this journey")),
            "You are a travel expert"
        );
        
        StepVerifier.create(client.createMessage(request))
            .assertNext(response -> {
                assertNotNull(response);
                assertNotNull(response.content());
                assertTrue(response.content().contains("mock"));
            })
            .verifyComplete();
    }
    
    @Test
    void testMockSamplingClient_CreateMessage_WhenNotAvailable_ThrowsException() {
        McpSamplingClient client = new MockSamplingClient(false);
        
        SamplingRequest request = SamplingRequest.simple(
            List.of(SamplingMessage.user("test"))
        );
        
        StepVerifier.create(client.createMessage(request))
            .expectError(IllegalStateException.class)
            .verify();
    }
    
    /**
     * Mock implementation of McpSamplingClient for testing.
     */
    private static class MockSamplingClient implements McpSamplingClient {
        private final boolean available;
        
        MockSamplingClient(boolean available) {
            this.available = available;
        }
        
        @Override
        public Mono<SamplingResponse> createMessage(SamplingRequest request) {
            if (!available) {
                return Mono.error(new IllegalStateException("Sampling not available"));
            }
            
            // Generate a mock response
            String mockContent = "This is a mock response to: " + 
                request.messages().get(0).content();
            
            return Mono.just(SamplingResponse.withModel(mockContent, "mock-model"));
        }
        
        @Override
        public boolean isAvailable() {
            return available;
        }
    }
}
