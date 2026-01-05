package ch.sbb.mcp.commons.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

/**
 * Abstract global exception handler for MCP reactive applications.
 * 
 * <p>Handles generic McpException and standard runtime exceptions.
 * Subclasses should provide handling for domain-specific exceptions.</p>
 */
public abstract class McpGlobalExceptionHandler implements ErrorWebExceptionHandler {
    
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final ObjectMapper objectMapper;
    
    public McpGlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        return Mono.defer(() -> {
            ErrorResponse errorResponse = buildErrorResponse(exchange, ex);
            logError(exchange, ex, errorResponse);
            return writeResponse(exchange, errorResponse);
        });
    }
    
    protected ErrorResponse buildErrorResponse(ServerWebExchange exchange, Throwable ex) {
        // Allow subclasses to handle domain-specific exceptions first
        ErrorResponse domainResponse = buildDomainErrorResponse(exchange, ex);
        if (domainResponse != null) {
            return domainResponse;
        }

        HttpStatus status = determineStatus(ex);
        String errorCode = determineErrorCode(ex);
        String message = determineMessage(ex);
        String path = exchange.getRequest().getPath().value();
        
        return new ErrorResponse(
            status.value(),
            errorCode,
            message,
            path,
            Instant.now().toString(),
            null
        );
    }
    
    /**
     * Hook for subclasses to handle domain exceptions.
     * @return ErrorResponse if handled, null otherwise
     */
    protected abstract ErrorResponse buildDomainErrorResponse(ServerWebExchange exchange, Throwable ex);
    
    protected HttpStatus determineStatus(Throwable ex) {
        if (ex instanceof McpException mcpEx) {
            return mcpEx.getStatus();
        }
        if (ex instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (ex instanceof SecurityException) {
            return HttpStatus.FORBIDDEN;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
    
    protected String determineErrorCode(Throwable ex) {
        if (ex instanceof McpException mcpEx) {
            return mcpEx.getErrorCode();
        }
        if (ex instanceof IllegalArgumentException) {
            return "VALIDATION_ERROR";
        }
        if (ex instanceof SecurityException) {
            return "ACCESS_DENIED";
        }
        return "INTERNAL_ERROR";
    }
    
    protected String determineMessage(Throwable ex) {
        String rawMessage;
        
        if (ex instanceof McpException) {
            rawMessage = ex.getMessage();
        } else if (ex instanceof IllegalArgumentException) {
            rawMessage = ex.getMessage();
        } else {
            return "An unexpected error occurred";
        }
        
        // Sanitize message to prevent information disclosure
        return sanitizeErrorMessage(rawMessage);
    }
    
    /**
     * Sanitizes error messages to prevent information disclosure.
     * 
     * <p>Removes:</p>
     * <ul>
     *   <li>File paths (e.g., /home/user/app/config/database.properties)</li>
     *   <li>IP addresses (e.g., 192.168.1.1)</li>
     *   <li>Hostnames (e.g., internal-db.company.com)</li>
     *   <li>Port numbers in connection strings</li>
     *   <li>Quoted identifiers (table/column names)</li>
     * </ul>
     * 
     * @param message the raw error message
     * @return sanitized error message safe for client consumption
     */
    protected String sanitizeErrorMessage(String message) {
        if (message == null) {
            return "An error occurred";
        }
        
        String sanitized = message;
        
        // Remove absolute file paths (Unix and Windows)
        sanitized = sanitized.replaceAll("/[\\w/.\\-]+", "[PATH]");
        sanitized = sanitized.replaceAll("[A-Z]:\\\\[\\w\\\\/.\\-]+", "[PATH]");
        
        // Remove IP addresses
        sanitized = sanitized.replaceAll("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b", "[IP]");
        
        // Remove hostnames (e.g., internal-db.company.com:5432)
        sanitized = sanitized.replaceAll("\\b[a-z0-9\\-]+\\.[a-z0-9\\-.]+:[0-9]+\\b", "[HOST]");
        
        // Remove quoted identifiers (table/column names)
        sanitized = sanitized.replaceAll("'[\\w_]+'", "'[REDACTED]'");
        sanitized = sanitized.replaceAll("\"[\\w_]+\"", "\"[REDACTED]\"");
        
        // Remove connection strings
        sanitized = sanitized.replaceAll("jdbc:[\\w:/@.\\-]+", "jdbc:[REDACTED]");
        sanitized = sanitized.replaceAll("redis://[\\w:/@.\\-]+", "redis://[REDACTED]");
        
        return sanitized;
    }
    
    protected void logError(ServerWebExchange exchange, Throwable ex, ErrorResponse errorResponse) {
        String requestId = exchange.getRequest().getId();
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();
        
        String traceIdPart = "";
        if (errorResponse.data() != null && errorResponse.data().containsKey("traceId")) {
            traceIdPart = " [Trace: " + errorResponse.data().get("traceId") + "]";
        }
        
        if (errorResponse.status() >= 500) {
            log.error("Request {} {} {} failed with status {}{}: {}",
                requestId, method, path, errorResponse.status(), traceIdPart, ex.getMessage(), ex);
        } else {
            log.warn("Request {} {} {} failed with status {}{}: {}",
                requestId, method, path, errorResponse.status(), traceIdPart, ex.getMessage());
        }
    }
    
    protected Mono<Void> writeResponse(ServerWebExchange exchange, ErrorResponse errorResponse) {
        exchange.getResponse().setStatusCode(HttpStatus.valueOf(errorResponse.status()));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize error response", e);
            byte[] fallback = "{\"error\":\"Internal server error\"}".getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(fallback);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }
    }
    
    public record ErrorResponse(
        int status,
        String errorCode,
        String message,
        String path,
        String timestamp,
        Map<String, Object> data
    ) {}
}
