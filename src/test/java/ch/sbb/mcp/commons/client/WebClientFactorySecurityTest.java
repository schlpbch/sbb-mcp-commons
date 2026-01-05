package ch.sbb.mcp.commons.client;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Security tests for WebClientFactory SSRF prevention.
 */
class WebClientFactorySecurityTest {
    
    @Test
    void shouldRejectNullUrl() {
        assertThrows(IllegalArgumentException.class, 
            () -> WebClientFactory.createDefault(null),
            "Should reject null URL");
    }
    
    @Test
    void shouldRejectEmptyUrl() {
        assertThrows(IllegalArgumentException.class, 
            () -> WebClientFactory.createDefault(""),
            "Should reject empty URL");
    }
    
    @Test
    void shouldRejectLoopbackAddress() {
        assertThrows(IllegalArgumentException.class, 
            () -> WebClientFactory.createDefault("http://localhost:8080"),
            "Should reject localhost");
        
        assertThrows(IllegalArgumentException.class, 
            () -> WebClientFactory.createDefault("http://127.0.0.1:8080"),
            "Should reject 127.0.0.1");
    }
    
    @Test
    void shouldRejectLinkLocalAddress() {
        assertThrows(IllegalArgumentException.class, 
            () -> WebClientFactory.createDefault("http://169.254.169.254"),
            "Should reject cloud metadata endpoint");
    }
    
    @Test
    void shouldRejectPrivateNetworkAddresses() {
        assertThrows(IllegalArgumentException.class, 
            () -> WebClientFactory.createDefault("http://192.168.1.1"),
            "Should reject 192.168.x.x");
        
        assertThrows(IllegalArgumentException.class, 
            () -> WebClientFactory.createDefault("http://10.0.0.1"),
            "Should reject 10.x.x.x");
        
        assertThrows(IllegalArgumentException.class, 
            () -> WebClientFactory.createDefault("http://172.16.0.1"),
            "Should reject 172.16.x.x");
    }
    
    @Test
    void shouldRejectNonHttpProtocols() {
        assertThrows(IllegalArgumentException.class, 
            () -> WebClientFactory.createDefault("file:///etc/passwd"),
            "Should reject file:// protocol");
        
        assertThrows(IllegalArgumentException.class, 
            () -> WebClientFactory.createDefault("ftp://example.com"),
            "Should reject ftp:// protocol");
        
        assertThrows(IllegalArgumentException.class, 
            () -> WebClientFactory.createDefault("gopher://example.com"),
            "Should reject gopher:// protocol");
    }
    
    @Test
    void shouldAcceptValidHttpsUrl() {
        assertDoesNotThrow(() -> {
            // Use a real domain that will resolve
            WebClient client = WebClientFactory.createDefault("https://www.google.com");
            assertNotNull(client);
        }, "Should accept valid HTTPS URL");
    }
    
    @Test
    void shouldAcceptValidHttpUrl() {
        assertDoesNotThrow(() -> {
            // Use a real domain that will resolve
            WebClient client = WebClientFactory.createDefault("http://www.google.com");
            assertNotNull(client);
        }, "Should accept valid HTTP URL");
    }
    
    @Test
    void shouldValidateUrlInAllFactoryMethods() {
        String maliciousUrl = "http://localhost:6379";
        
        assertThrows(IllegalArgumentException.class, 
            () -> WebClientFactory.createDefault(maliciousUrl));
        
        assertThrows(IllegalArgumentException.class, 
            () -> WebClientFactory.createWithTimeouts(maliciousUrl, null, null));
        
        assertThrows(IllegalArgumentException.class, 
            () -> WebClientFactory.createWithBearerToken(maliciousUrl, "token"));
        
        assertThrows(IllegalArgumentException.class, 
            () -> WebClientFactory.createWithApiKey(maliciousUrl, "X-API-Key", "key"));
        
        assertThrows(IllegalArgumentException.class, 
            () -> WebClientFactory.builder(maliciousUrl));
    }
}
