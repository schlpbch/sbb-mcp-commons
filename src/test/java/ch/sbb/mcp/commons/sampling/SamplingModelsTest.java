package ch.sbb.mcp.commons.sampling;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MCP sampling model classes.
 */
class SamplingModelsTest {
    
    // ========== SamplingMessage Tests ==========
    
    @Test
    void testSamplingMessage_ValidUserMessage() {
        SamplingMessage message = new SamplingMessage("user", "Hello, world!");
        
        assertEquals("user", message.role());
        assertEquals("Hello, world!", message.content());
    }
    
    @Test
    void testSamplingMessage_ValidAssistantMessage() {
        SamplingMessage message = new SamplingMessage("assistant", "Hi there!");
        
        assertEquals("assistant", message.role());
        assertEquals("Hi there!", message.content());
    }
    
    @Test
    void testSamplingMessage_UserFactoryMethod() {
        SamplingMessage message = SamplingMessage.user("Test content");
        
        assertEquals("user", message.role());
        assertEquals("Test content", message.content());
    }
    
    @Test
    void testSamplingMessage_AssistantFactoryMethod() {
        SamplingMessage message = SamplingMessage.assistant("Response content");
        
        assertEquals("assistant", message.role());
        assertEquals("Response content", message.content());
    }
    
    @Test
    void testSamplingMessage_NullRole_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new SamplingMessage(null, "content"));
    }
    
    @Test
    void testSamplingMessage_BlankRole_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new SamplingMessage("", "content"));
    }
    
    @Test
    void testSamplingMessage_NullContent_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new SamplingMessage("user", null));
    }
    
    @Test
    void testSamplingMessage_BlankContent_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new SamplingMessage("user", "   "));
    }
    
    @Test
    void testSamplingMessage_InvalidRole_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new SamplingMessage("system", "content"));
    }
    
    // ========== ModelPreferences Tests ==========
    
    @Test
    void testModelPreferences_AllFieldsNull() {
        ModelPreferences prefs = new ModelPreferences(null, null, null);
        
        assertNull(prefs.hints());
        assertNull(prefs.temperature());
        assertNull(prefs.maxTokens());
    }
    
    @Test
    void testModelPreferences_WithHints() {
        List<String> hints = List.of("claude-3-5-sonnet", "gpt-4");
        ModelPreferences prefs = new ModelPreferences(hints, null, null);
        
        assertEquals(hints, prefs.hints());
        assertNull(prefs.temperature());
        assertNull(prefs.maxTokens());
    }
    
    @Test
    void testModelPreferences_WithTemperature() {
        ModelPreferences prefs = new ModelPreferences(null, 0.7, null);
        
        assertNull(prefs.hints());
        assertEquals(0.7, prefs.temperature());
        assertNull(prefs.maxTokens());
    }
    
    @Test
    void testModelPreferences_WithMaxTokens() {
        ModelPreferences prefs = new ModelPreferences(null, null, 500);
        
        assertNull(prefs.hints());
        assertNull(prefs.temperature());
        assertEquals(500, prefs.maxTokens());
    }
    
    @Test
    void testModelPreferences_TemperatureTooLow_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new ModelPreferences(null, -0.1, null));
    }
    
    @Test
    void testModelPreferences_TemperatureTooHigh_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new ModelPreferences(null, 1.1, null));
    }
    
    @Test
    void testModelPreferences_MaxTokensZero_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new ModelPreferences(null, null, 0));
    }
    
    @Test
    void testModelPreferences_MaxTokensNegative_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new ModelPreferences(null, null, -100));
    }
    
    @Test
    void testModelPreferences_WithHintsFactory() {
        List<String> hints = List.of("claude-3-5-sonnet");
        ModelPreferences prefs = ModelPreferences.withHints(hints);
        
        assertEquals(hints, prefs.hints());
        assertNull(prefs.temperature());
        assertNull(prefs.maxTokens());
    }
    
    @Test
    void testModelPreferences_WithTemperatureFactory() {
        ModelPreferences prefs = ModelPreferences.withTemperature(0.5);
        
        assertNull(prefs.hints());
        assertEquals(0.5, prefs.temperature());
        assertNull(prefs.maxTokens());
    }
    
    @Test
    void testModelPreferences_WithMaxTokensFactory() {
        ModelPreferences prefs = ModelPreferences.withMaxTokens(1000);
        
        assertNull(prefs.hints());
        assertNull(prefs.temperature());
        assertEquals(1000, prefs.maxTokens());
    }
    
    // ========== SamplingRequest Tests ==========
    
    @Test
    void testSamplingRequest_ValidRequest() {
        List<SamplingMessage> messages = List.of(
            SamplingMessage.user("Explain this journey ranking")
        );
        SamplingRequest request = new SamplingRequest(messages, "You are a travel expert", 500, null);
        
        assertEquals(messages, request.messages());
        assertEquals("You are a travel expert", request.systemPrompt());
        assertEquals(500, request.maxTokens());
        assertNull(request.modelPreferences());
    }
    
    @Test
    void testSamplingRequest_NullMessages_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new SamplingRequest(null, "prompt", 100, null));
    }
    
    @Test
    void testSamplingRequest_EmptyMessages_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new SamplingRequest(List.of(), "prompt", 100, null));
    }
    
    @Test
    void testSamplingRequest_MaxTokensZero_ThrowsException() {
        List<SamplingMessage> messages = List.of(SamplingMessage.user("test"));
        assertThrows(IllegalArgumentException.class, () -> 
            new SamplingRequest(messages, null, 0, null));
    }
    
    @Test
    void testSamplingRequest_MaxTokensNegative_ThrowsException() {
        List<SamplingMessage> messages = List.of(SamplingMessage.user("test"));
        assertThrows(IllegalArgumentException.class, () -> 
            new SamplingRequest(messages, null, -50, null));
    }
    
    @Test
    void testSamplingRequest_SimpleFactory() {
        List<SamplingMessage> messages = List.of(SamplingMessage.user("test"));
        SamplingRequest request = SamplingRequest.simple(messages);
        
        assertEquals(messages, request.messages());
        assertNull(request.systemPrompt());
        assertNull(request.maxTokens());
        assertNull(request.modelPreferences());
    }
    
    @Test
    void testSamplingRequest_WithSystemPromptFactory() {
        List<SamplingMessage> messages = List.of(SamplingMessage.user("test"));
        SamplingRequest request = SamplingRequest.withSystemPrompt(messages, "You are helpful");
        
        assertEquals(messages, request.messages());
        assertEquals("You are helpful", request.systemPrompt());
        assertNull(request.maxTokens());
        assertNull(request.modelPreferences());
    }
    
    @Test
    void testSamplingRequest_WithMaxTokensFactory() {
        List<SamplingMessage> messages = List.of(SamplingMessage.user("test"));
        SamplingRequest request = SamplingRequest.withMaxTokens(messages, "prompt", 200);
        
        assertEquals(messages, request.messages());
        assertEquals("prompt", request.systemPrompt());
        assertEquals(200, request.maxTokens());
        assertNull(request.modelPreferences());
    }
    
    // ========== SamplingResponse Tests ==========
    
    @Test
    void testSamplingResponse_ValidResponse() {
        var usage = new SamplingResponse.TokenUsage(10, 20, 30);
        SamplingResponse response = new SamplingResponse(
            "Generated content", 
            "claude-3-5-sonnet", 
            "end_turn", 
            usage
        );
        
        assertEquals("Generated content", response.content());
        assertEquals("claude-3-5-sonnet", response.model());
        assertEquals("end_turn", response.stopReason());
        assertEquals(usage, response.usage());
    }
    
    @Test
    void testSamplingResponse_NullContent_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new SamplingResponse(null, "model", "stop", null));
    }
    
    @Test
    void testSamplingResponse_BlankContent_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new SamplingResponse("   ", "model", "stop", null));
    }
    
    @Test
    void testSamplingResponse_SimpleFactory() {
        SamplingResponse response = SamplingResponse.simple("Test content");
        
        assertEquals("Test content", response.content());
        assertNull(response.model());
        assertNull(response.stopReason());
        assertNull(response.usage());
    }
    
    @Test
    void testSamplingResponse_WithModelFactory() {
        SamplingResponse response = SamplingResponse.withModel("Content", "gpt-4");
        
        assertEquals("Content", response.content());
        assertEquals("gpt-4", response.model());
        assertNull(response.stopReason());
        assertNull(response.usage());
    }
    
    // ========== TokenUsage Tests ==========
    
    @Test
    void testTokenUsage_ValidUsage() {
        var usage = new SamplingResponse.TokenUsage(100, 50, 150);
        
        assertEquals(100, usage.promptTokens());
        assertEquals(50, usage.completionTokens());
        assertEquals(150, usage.totalTokens());
    }
    
    @Test
    void testTokenUsage_NegativePromptTokens_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new SamplingResponse.TokenUsage(-1, 50, 49));
    }
    
    @Test
    void testTokenUsage_NegativeCompletionTokens_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new SamplingResponse.TokenUsage(100, -1, 99));
    }
    
    @Test
    void testTokenUsage_NegativeTotalTokens_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new SamplingResponse.TokenUsage(100, 50, -1));
    }
    
    @Test
    void testTokenUsage_TotalDoesNotMatchSum_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new SamplingResponse.TokenUsage(100, 50, 200));
    }
}
