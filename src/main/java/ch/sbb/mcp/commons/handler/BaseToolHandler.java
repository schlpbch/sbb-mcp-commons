package ch.sbb.mcp.commons.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Base class for MCP tool handlers with built-in validation, logging, and error handling.
 * Compatible with McpTool interface - tools can extend this for additional functionality.
 *
 * @param <INPUT> the input type after parsing and validation
 * @param <OUTPUT> the output type from business logic
 */
public abstract class BaseToolHandler<INPUT, OUTPUT> {
    
    private static final Logger log = LoggerFactory.getLogger(BaseToolHandler.class);
    
    /**
     * Execute tool with automatic validation, logging, and error handling.
     * Compatible with McpTool.invoke() signature.
     *
     * @param arguments the arguments from MCP client as Map
     * @return Mono containing the output
     */
    public final Mono<OUTPUT> execute(Map<String, Object> arguments) {
        String toolName = getToolName();
        long startTime = System.currentTimeMillis();
        
        return Mono.fromCallable(() -> {
            log.debug("[{}] Validating arguments: {}", toolName, arguments);
            return validateAndParse(arguments);
        })
        .flatMap(input -> {
            log.debug("[{}] Executing with input: {}", toolName, input);
            return executeInternal(input);
        })
        .doOnSuccess(output -> {
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("[{}] Completed successfully in {}ms", toolName, elapsed);
        })
        .doOnError(error -> {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[{}] Failed after {}ms: {}", toolName, elapsed, error.getMessage(), error);
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
     * Subclasses define the tool name for logging.
     *
     * @return the tool name (e.g., "getWeather", "findTrips")
     */
    protected abstract String getToolName();
    
    /**
     * Subclasses define input validation and parsing logic.
     * Use Validators from ch.sbb.mcp.commons.validation for validation.
     *
     * @param arguments the raw arguments as Map
     * @return the validated and parsed input object
     * @throws IllegalArgumentException if validation fails
     */
    protected abstract INPUT validateAndParse(Map<String, Object> arguments);
    
    // ========== Helper Methods for Argument Extraction ==========
    
    /**
     * Get a required string field from arguments.
     *
     * @param args the arguments map
     * @param key the field name
     * @return the field value
     * @throws IllegalArgumentException if field is missing
     */
    protected String getRequiredString(Map<String, Object> args, String key) {
        Object value = args.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required parameter: " + key);
        }
        return value.toString();
    }
    
    /**
     * Get an optional string field from arguments.
     *
     * @param args the arguments map
     * @param key the field name
     * @param defaultValue the default value if field is missing
     * @return the field value or default
     */
    protected String getOptionalString(Map<String, Object> args, String key, String defaultValue) {
        Object value = args.get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    /**
     * Get a required double field from arguments.
     *
     * @param args the arguments map
     * @param key the field name
     * @return the field value
     * @throws IllegalArgumentException if field is missing or invalid
     */
    protected double getRequiredDouble(Map<String, Object> args, String key) {
        Object value = args.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required parameter: " + key);
        }
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number for " + key + ": " + value);
        }
    }
    
    /**
     * Get an optional double field from arguments.
     *
     * @param args the arguments map
     * @param key the field name
     * @param defaultValue the default value if field is missing
     * @return the field value or default
     */
    protected double getOptionalDouble(Map<String, Object> args, String key, double defaultValue) {
        Object value = args.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Get a required integer field from arguments.
     *
     * @param args the arguments map
     * @param key the field name
     * @return the field value
     * @throws IllegalArgumentException if field is missing or invalid
     */
    protected int getRequiredInt(Map<String, Object> args, String key) {
        Object value = args.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required parameter: " + key);
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer for " + key + ": " + value);
        }
    }
    
    /**
     * Get an optional integer field from arguments.
     *
     * @param args the arguments map
     * @param key the field name
     * @param defaultValue the default value if field is missing
     * @return the field value or default
     */
    protected int getOptionalInt(Map<String, Object> args, String key, int defaultValue) {
        Object value = args.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Get an optional integer field with bounds validation.
     *
     * @param args the arguments map
     * @param key the field name
     * @param defaultValue the default value if field is missing
     * @param min the minimum allowed value (inclusive)
     * @param max the maximum allowed value (inclusive)
     * @return the field value or default
     * @throws IllegalArgumentException if value is outside bounds
     */
    protected int getOptionalIntBounded(Map<String, Object> args, String key, int defaultValue, int min, int max) {
        int value = getOptionalInt(args, key, defaultValue);
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                String.format("Parameter '%s' must be between %d and %d, got: %d", key, min, max, value)
            );
        }
        return value;
    }
    
    /**
     * Get a required integer field with bounds validation.
     *
     * @param args the arguments map
     * @param key the field name
     * @param min the minimum allowed value (inclusive)
     * @param max the maximum allowed value (inclusive)
     * @return the field value
     * @throws IllegalArgumentException if field is missing or outside bounds
     */
    protected int getRequiredIntBounded(Map<String, Object> args, String key, int min, int max) {
        int value = getRequiredInt(args, key);
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                String.format("Parameter '%s' must be between %d and %d, got: %d", key, min, max, value)
            );
        }
        return value;
    }
    
    /**
     * Get an optional double field with bounds validation.
     *
     * @param args the arguments map
     * @param key the field name
     * @param defaultValue the default value if field is missing
     * @param min the minimum allowed value (inclusive)
     * @param max the maximum allowed value (inclusive)
     * @return the field value or default
     * @throws IllegalArgumentException if value is outside bounds
     */
    protected double getOptionalDoubleBounded(Map<String, Object> args, String key, double defaultValue, double min, double max) {
        double value = getOptionalDouble(args, key, defaultValue);
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                String.format("Parameter '%s' must be between %.2f and %.2f, got: %.2f", key, min, max, value)
            );
        }
        return value;
    }
    
    /**
     * Get a required double field with bounds validation.
     *
     * @param args the arguments map
     * @param key the field name
     * @param min the minimum allowed value (inclusive)
     * @param max the maximum allowed value (inclusive)
     * @return the field value
     * @throws IllegalArgumentException if field is missing or outside bounds
     */
    protected double getRequiredDoubleBounded(Map<String, Object> args, String key, double min, double max) {
        double value = getRequiredDouble(args, key);
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                String.format("Parameter '%s' must be between %.2f and %.2f, got: %.2f", key, min, max, value)
            );
        }
        return value;
    }
    
    /**
     * Get a required boolean field from arguments.
     *
     * @param args the arguments map
     * @param key the field name
     * @return the field value
     * @throws IllegalArgumentException if field is missing
     */
    protected boolean getRequiredBoolean(Map<String, Object> args, String key) {
        Object value = args.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required parameter: " + key);
        }
        if (value instanceof Boolean b) {
            return b;
        }
        return Boolean.parseBoolean(value.toString());
    }
    
    /**
     * Get an optional boolean field from arguments.
     *
     * @param args the arguments map
     * @param key the field name
     * @param defaultValue the default value if field is missing
     * @return the field value or default
     */
    protected boolean getOptionalBoolean(Map<String, Object> args, String key, boolean defaultValue) {
        Object value = args.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        try {
            return Boolean.parseBoolean(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
