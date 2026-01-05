package ch.sbb.mcp.commons.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for McpException.
 */
@DisplayName("McpException Tests")
class McpExceptionTest {
    
    @Test
    @DisplayName("Should create exception with message, status, and error code")
    void shouldCreateExceptionWithAllFields() {
        McpException exception = new McpException("Test message", HttpStatus.BAD_REQUEST, "TEST_ERROR");
        
        assertThat(exception.getMessage()).isEqualTo("Test message");
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getErrorCode()).isEqualTo("TEST_ERROR");
    }
    
    @Test
    @DisplayName("Should create exception with cause")
    void shouldCreateExceptionWithCause() {
        RuntimeException cause = new RuntimeException("Original error");
        McpException exception = new McpException("Wrapped error", HttpStatus.INTERNAL_SERVER_ERROR, "WRAPPED", cause);
        
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getMessage()).isEqualTo("Wrapped error");
    }
    
    @Test
    @DisplayName("notFound should create NOT_FOUND exception")
    void shouldCreateNotFoundExceptionViaFactory() {
        McpException exception = McpException.notFound("Place", "8503000");
        
        assertThat(exception.getMessage()).isEqualTo("Place not found: 8503000");
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
    }
    
    @Test
    @DisplayName("validation should create BAD_REQUEST exception")
    void shouldCreateValidationExceptionViaFactory() {
        McpException exception = McpException.validation("Invalid input");
        
        assertThat(exception.getMessage()).isEqualTo("Invalid input");
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getErrorCode()).isEqualTo("VALIDATION_ERROR");
    }
    
    @Test
    @DisplayName("externalService should create BAD_GATEWAY exception")
    void shouldCreateExternalServiceExceptionViaFactory() {
        McpException exception = McpException.externalService("SBB", "Connection timeout");
        
        assertThat(exception.getMessage()).isEqualTo("SBB service error: Connection timeout");
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(exception.getErrorCode()).isEqualTo("EXTERNAL_SERVICE_ERROR");
    }
    
    @Test
    @DisplayName("timeout should create GATEWAY_TIMEOUT exception")
    void shouldCreateTimeoutExceptionViaFactory() {
        McpException exception = McpException.timeout("SBB");
        
        assertThat(exception.getMessage()).isEqualTo("SBB service timeout");
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
        assertThat(exception.getErrorCode()).isEqualTo("SERVICE_TIMEOUT");
    }
    
    @Test
    @DisplayName("rateLimited should create TOO_MANY_REQUESTS exception")
    void shouldCreateRateLimitedExceptionViaFactory() {
        McpException exception = McpException.rateLimited();
        
        assertThat(exception.getMessage()).contains("Rate limit exceeded");
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(exception.getErrorCode()).isEqualTo("RATE_LIMITED");
    }
    
    @Test
    @DisplayName("circuitOpen should create SERVICE_UNAVAILABLE exception")
    void shouldCreateCircuitOpenExceptionViaFactory() {
        McpException exception = McpException.circuitOpen("SBB");
        
        assertThat(exception.getMessage()).isEqualTo("SBB service is temporarily unavailable");
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(exception.getErrorCode()).isEqualTo("CIRCUIT_OPEN");
    }
}
