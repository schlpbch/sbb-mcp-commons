package ch.sbb.mcp.commons.util;

import java.util.Map;

/**
 * Utility class for extracting and converting arguments from MCP tool invocations.
 * 
 * <p>Provides type-safe extraction of common argument types with default values
 * and automatic type conversion. Handles both direct types and string representations.</p>
 * 
 * <p>This utility eliminates code duplication across MCP tools by centralizing
 * argument extraction logic.</p>
 */
public final class ArgumentExtractor {
    
    private ArgumentExtractor() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Extracts an integer value from arguments.
     * 
     * <p>Handles:</p>
     * <ul>
     *   <li>Direct Number instances (converted to int)</li>
     *   <li>String representations of numbers</li>
     *   <li>Missing values (returns default)</li>
     *   <li>Invalid values (returns default)</li>
     * </ul>
     * 
     * @param arguments The arguments map
     * @param key The key to extract
     * @param defaultValue The default value if key is missing or invalid
     * @return The extracted integer value or default
     */
    public static int extractInt(Map<String, Object> arguments, String key, int defaultValue) {
        Object value = arguments.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String str) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * Extracts a double value from arguments.
     * 
     * <p>Handles:</p>
     * <ul>
     *   <li>Direct Number instances (converted to double)</li>
     *   <li>String representations of numbers</li>
     *   <li>Missing values (returns default)</li>
     *   <li>Invalid values (returns default)</li>
     * </ul>
     * 
     * @param arguments The arguments map
     * @param key The key to extract
     * @param defaultValue The default value if key is missing or invalid
     * @return The extracted double value or default
     */
    public static double extractDouble(Map<String, Object> arguments, String key, double defaultValue) {
        Object value = arguments.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String str) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * Extracts a Double value from arguments (nullable version).
     * 
     * <p>Returns null if the key is missing, allowing distinction between
     * "not provided" and "provided with default value".</p>
     * 
     * @param arguments The arguments map
     * @param key The key to extract
     * @return The extracted Double value or null if not present
     */
    public static Double extractDoubleNullable(Map<String, Object> arguments, String key) {
        Object value = arguments.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String str) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * Extracts a string value from arguments.
     * 
     * <p>Handles:</p>
     * <ul>
     *   <li>Direct String instances</li>
     *   <li>String conversion of other types via toString()</li>
     *   <li>Missing values (returns default)</li>
     * </ul>
     * 
     * @param arguments The arguments map
     * @param key The key to extract
     * @param defaultValue The default value if key is missing
     * @return The extracted string value or default
     */
    public static String extractString(Map<String, Object> arguments, String key, String defaultValue) {
        Object value = arguments.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof String str) {
            return str;
        }
        return value.toString();
    }
    
    /**
     * Extracts a string value from arguments (nullable version).
     * 
     * @param arguments The arguments map
     * @param key The key to extract
     * @return The extracted string value or null if not present
     */
    public static String extractStringNullable(Map<String, Object> arguments, String key) {
        Object value = arguments.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof String str) {
            return str;
        }
        return value.toString();
    }
    
    /**
     * Extracts a boolean value from arguments.
     * 
     * <p>Handles:</p>
     * <ul>
     *   <li>Direct Boolean instances</li>
     *   <li>String representations ("true", "false", case-insensitive)</li>
     *   <li>Missing values (returns default)</li>
     *   <li>Invalid values (returns default)</li>
     * </ul>
     * 
     * @param arguments The arguments map
     * @param key The key to extract
     * @param defaultValue The default value if key is missing or invalid
     * @return The extracted boolean value or default
     */
    public static boolean extractBoolean(Map<String, Object> arguments, String key, boolean defaultValue) {
        Object value = arguments.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String str) {
            return Boolean.parseBoolean(str);
        }
        return defaultValue;
    }
    
    /**
     * Extracts a long value from arguments.
     * 
     * <p>Handles:</p>
     * <ul>
     *   <li>Direct Number instances (converted to long)</li>
     *   <li>String representations of numbers</li>
     *   <li>Missing values (returns default)</li>
     *   <li>Invalid values (returns default)</li>
     * </ul>
     * 
     * @param arguments The arguments map
     * @param key The key to extract
     * @param defaultValue The default value if key is missing or invalid
     * @return The extracted long value or default
     */
    public static long extractLong(Map<String, Object> arguments, String key, long defaultValue) {
        Object value = arguments.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String str) {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}
