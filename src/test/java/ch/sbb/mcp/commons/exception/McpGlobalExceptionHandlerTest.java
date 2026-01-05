package ch.sbb.mcp.commons.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for McpGlobalExceptionHandler.
 */
@DisplayName("McpGlobalExceptionHandler Tests")
class McpGlobalExceptionHandlerTest {
    
    private TestGlobalExceptionHandler handler;
    
    @BeforeEach
    void setUp() {
        handler = new TestGlobalExceptionHandler(new ObjectMapper());
    }
    
    static class TestGlobalExceptionHandler extends McpGlobalExceptionHandler {
        public TestGlobalExceptionHandler(ObjectMapper objectMapper) {
            super(objectMapper);
        }
        
        @Override
        protected ErrorResponse buildDomainErrorResponse(ServerWebExchange exchange, Throwable ex) {
            return null; // Default behavior
        }
    }
    
    @Test
    @DisplayName("Should handle McpException with correct status and error code")
    void shouldHandleMcpException() {
        McpException exception = McpException.notFound("Place", "8503000");
        MockServerWebExchange exchange = createExchange("/api/places/8503000");
        
        StepVerifier.create(handler.handle(exchange, exception))
            .verifyComplete();
        
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    }
    
    @Test
    @DisplayName("Should handle IllegalArgumentException as BAD_REQUEST")
    void shouldHandleIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid parameter");
        MockServerWebExchange exchange = createExchange("/api/places");
        
        StepVerifier.create(handler.handle(exchange, exception))
            .verifyComplete();
        
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
    
    @Test
    @DisplayName("Should handle SecurityException as FORBIDDEN")
    void shouldHandleSecurityException() {
        SecurityException exception = new SecurityException("Access denied");
        MockServerWebExchange exchange = createExchange("/api/admin");
        
        StepVerifier.create(handler.handle(exchange, exception))
            .verifyComplete();
        
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
    
    @Test
    @DisplayName("Should handle generic exception as INTERNAL_SERVER_ERROR")
    void shouldHandleGenericException() {
        RuntimeException exception = new RuntimeException("Something went wrong");
        MockServerWebExchange exchange = createExchange("/api/test");
        
        StepVerifier.create(handler.handle(exchange, exception))
            .verifyComplete();
        
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    private MockServerWebExchange createExchange(String path) {
        MockServerHttpRequest request = MockServerHttpRequest.get(path).build();
        return MockServerWebExchange.from(request);
    }
}
