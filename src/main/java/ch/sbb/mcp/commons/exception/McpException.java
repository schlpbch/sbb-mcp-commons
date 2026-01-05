package ch.sbb.mcp.commons.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception class for MCP-specific errors.
 * 
 * <p>Provides structured error information including HTTP status codes
 * and error codes for client-side error handling.</p>
 */
public class McpException extends RuntimeException {
    
    private final HttpStatus status;
    private final String errorCode;
    
    public McpException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
    
    public McpException(String message, HttpStatus status, String errorCode, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }
    
    public HttpStatus getStatus() {
        return status;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    // Common exception factory methods
    
    /**
     * Creates an exception for resource not found errors.
     */
    public static McpException notFound(String resourceType, String identifier) {
        return new McpException(
            String.format("%s not found: %s", resourceType, identifier),
            HttpStatus.NOT_FOUND,
            "RESOURCE_NOT_FOUND"
        );
    }
    
    /**
     * Creates an exception for validation errors.
     */
    public static McpException validation(String message) {
        return new McpException(message, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
    }
    
    /**
     * Creates an exception for external service errors.
     */
    public static McpException externalService(String service, String message) {
        return new McpException(
            String.format("%s service error: %s", service, message),
            HttpStatus.BAD_GATEWAY,
            "EXTERNAL_SERVICE_ERROR"
        );
    }
    
    /**
     * Creates an exception for external service timeout.
     */
    public static McpException timeout(String service) {
        return new McpException(
            String.format("%s service timeout", service),
            HttpStatus.GATEWAY_TIMEOUT,
            "SERVICE_TIMEOUT"
        );
    }
    
    /**
     * Creates an exception for rate limiting.
     */
    public static McpException rateLimited() {
        return new McpException(
            "Rate limit exceeded. Please try again later.",
            HttpStatus.TOO_MANY_REQUESTS,
            "RATE_LIMITED"
        );
    }
    
    /**
     * Creates an exception for circuit breaker open state.
     */
    public static McpException circuitOpen(String service) {
        return new McpException(
            String.format("%s service is temporarily unavailable", service),
            HttpStatus.SERVICE_UNAVAILABLE,
            "CIRCUIT_OPEN"
        );
    }
}
