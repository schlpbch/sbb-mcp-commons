package ch.sbb.mcp.commons.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Base class for MCP tool handlers with common logic for validation, logging, and error handling.
 *
 * @param <INPUT> the input type after parsing and validation
 * @param <OUTPUT> the output type from business logic
 */
public abstract class BaseToolHandler<INPUT, OUTPUT> {
    
    private static final Logger log = LoggerFactory.getLogger(BaseToolHandler.class);
    
    /**
     * Execute tool with automatic validation, logging, and error handling.
     *
     * @param arguments the raw JSON arguments from MCP client
     * @return Mono containing the tool result
     */
    public final Mono<ToolResult> execute(JsonNode arguments) {
        String toolName = getToolName();
        String requestId = UUID.randomUUID().toString();
        
        return Mono.defer(() -> {
            log.info("[{}] Executing tool: {} with requestId: {}", toolName, toolName, requestId);
            log.debug("[{}] Arguments: {}", toolName, arguments);
            
            try {
                // 1. Validate and parse input
                INPUT input = validateAndParse(arguments);
                log.debug("[{}] Validated input: {}", toolName, input);
                
                // 2. Execute business logic
                return executeInternal(input)
                    .doOnNext(output -> log.info("[{}] Tool completed successfully", toolName))
                    .doOnNext(output -> log.debug("[{}] Output: {}", toolName, output))
                    .map(this::formatOutput)
                    .onErrorResume(error -> handleError(error, toolName, requestId));
                    
            } catch (Exception e) {
                log.error("[{}] Validation failed: {}", toolName, e.getMessage());
                return handleError(e, toolName, requestId);
            }
        });
    }
    
    /**
     * Subclasses implement tool-specific business logic.
     *
     * @param input the validated and parsed input
     * @return Mono containing the output
     */
    protected abstract Mono<OUTPUT> executeInternal(INPUT input);
    
    /**
     * Subclasses define the tool name.
     *
     * @return the tool name (e.g., "findTrips", "getPricing")
     */
    protected abstract String getToolName();
    
    /**
     * Subclasses define input validation and parsing logic.
     *
     * @param arguments the raw JSON arguments
     * @return the validated and parsed input object
     * @throws Exception if validation fails
     */
    protected abstract INPUT validateAndParse(JsonNode arguments) throws Exception;
    
    /**
     * Subclasses define output formatting logic.
     *
     * @param output the business logic output
     * @return the formatted tool result
     */
    protected abstract ToolResult formatOutput(OUTPUT output);
    
    /**
     * Common error handling logic.
     *
     * @param error the error that occurred
     * @param toolName the tool name
     * @param requestId the request ID
     * @return Mono containing error tool result
     */
    private Mono<ToolResult> handleError(Throwable error, String toolName, String requestId) {
        log.error("[{}] Tool failed with requestId: {}", toolName, requestId, error);
        
        String errorMessage = error.getMessage() != null 
            ? error.getMessage() 
            : "An unexpected error occurred";
            
        return Mono.just(ToolResult.error(errorMessage, error.getClass().getSimpleName()));
    }
    
    /**
     * Helper method to get a required string field from JSON.
     *
     * @param node the JSON node
     * @param fieldName the field name
     * @return the field value
     * @throws IllegalArgumentException if field is missing or not a string
     */
    protected String getRequiredString(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName)) {
            throw new IllegalArgumentException("Missing required field: " + fieldName);
        }
        JsonNode fieldNode = node.get(fieldName);
        if (!fieldNode.isTextual()) {
            throw new IllegalArgumentException("Field " + fieldName + " must be a string");
        }
        return fieldNode.asText();
    }
    
    /**
     * Helper method to get an optional string field from JSON.
     *
     * @param node the JSON node
     * @param fieldName the field name
     * @param defaultValue the default value if field is missing
     * @return the field value or default
     */
    protected String getOptionalString(JsonNode node, String fieldName, String defaultValue) {
        if (node == null || !node.has(fieldName)) {
            return defaultValue;
        }
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode.isTextual() ? fieldNode.asText() : defaultValue;
    }
    
    /**
     * Helper method to get a required integer field from JSON.
     *
     * @param node the JSON node
     * @param fieldName the field name
     * @return the field value
     * @throws IllegalArgumentException if field is missing or not an integer
     */
    protected int getRequiredInt(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName)) {
            throw new IllegalArgumentException("Missing required field: " + fieldName);
        }
        JsonNode fieldNode = node.get(fieldName);
        if (!fieldNode.isInt()) {
            throw new IllegalArgumentException("Field " + fieldName + " must be an integer");
        }
        return fieldNode.asInt();
    }
    
    /**
     * Helper method to get an optional integer field from JSON.
     *
     * @param node the JSON node
     * @param fieldName the field name
     * @param defaultValue the default value if field is missing
     * @return the field value or default
     */
    protected int getOptionalInt(JsonNode node, String fieldName, int defaultValue) {
        if (node == null || !node.has(fieldName)) {
            return defaultValue;
        }
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode.isInt() ? fieldNode.asInt() : defaultValue;
    }
}
