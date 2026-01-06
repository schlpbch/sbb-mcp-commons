package ch.sbb.mcp.commons.sampling;

import java.util.List;

/**
 * Model preferences for MCP sampling requests.
 * 
 * <p>Allows the server to provide hints about preferred models and sampling parameters
 * to the client. All fields are optional.</p>
 * 
 * @param hints Optional list of model hints (e.g., ["claude-3-5-sonnet", "gpt-4"])
 * @param temperature Optional temperature for sampling (0.0-1.0)
 * @param maxTokens Optional maximum number of tokens to generate
 */
public record ModelPreferences(
    List<String> hints,
    Double temperature,
    Integer maxTokens
) {
    /**
     * Creates a new ModelPreferences with validation.
     * 
     * @throws IllegalArgumentException if temperature is out of range or maxTokens is invalid
     */
    public ModelPreferences {
        if (temperature != null && (temperature < 0.0 || temperature > 1.0)) {
            throw new IllegalArgumentException("temperature must be between 0.0 and 1.0");
        }
        if (maxTokens != null && maxTokens <= 0) {
            throw new IllegalArgumentException("maxTokens must be positive");
        }
    }
    
    /**
     * Creates ModelPreferences with only model hints.
     * 
     * @param hints List of model hints
     * @return A new ModelPreferences instance
     */
    public static ModelPreferences withHints(List<String> hints) {
        return new ModelPreferences(hints, null, null);
    }
    
    /**
     * Creates ModelPreferences with temperature.
     * 
     * @param temperature Sampling temperature (0.0-1.0)
     * @return A new ModelPreferences instance
     */
    public static ModelPreferences withTemperature(double temperature) {
        return new ModelPreferences(null, temperature, null);
    }
    
    /**
     * Creates ModelPreferences with max tokens.
     * 
     * @param maxTokens Maximum tokens to generate
     * @return A new ModelPreferences instance
     */
    public static ModelPreferences withMaxTokens(int maxTokens) {
        return new ModelPreferences(null, null, maxTokens);
    }
}
