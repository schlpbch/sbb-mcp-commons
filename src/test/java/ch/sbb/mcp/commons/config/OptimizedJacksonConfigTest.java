package ch.sbb.mcp.commons.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link OptimizedJacksonConfig}.
 */
class OptimizedJacksonConfigTest {

    @Test
    void shouldDisablePrettyPrinting() {
        // Given
        OptimizedJacksonConfig config = new OptimizedJacksonConfig();
        ObjectMapper mapper = config.objectMapper();

        // Then
        assertThat(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT))
                .as("Pretty-printing should be disabled for minified output")
                .isFalse();
    }

    @Test
    void shouldProduceMinifiedJson() throws Exception {
        // Given
        OptimizedJacksonConfig config = new OptimizedJacksonConfig();
        ObjectMapper mapper = config.objectMapper();

        Map<String, Object> testData = Map.of(
                "name", "Test Station",
                "id", "8507000",
                "coordinates", Map.of("lat", 46.9479, "lon", 7.4474)
        );

        // When
        String json = mapper.writeValueAsString(testData);

        // Then - should be compact (no newlines or extra spaces)
        assertThat(json)
                .as("JSON should be minified without newlines")
                .doesNotContain("\n")
                .doesNotContain("\r");

        assertThat(json)
                .as("JSON should not have extra spacing")
                .doesNotContain("  "); // No double spaces

        assertThat(json.length())
                .as("Minified JSON should be shorter than pretty-printed")
                .isLessThan(150); // Typical minified size

        // Verify it's still valid JSON
        Map<?, ?> parsed = mapper.readValue(json, Map.class);
        assertThat(parsed).isEqualTo(testData);
    }

    @Test
    void shouldIgnoreUnknownProperties() throws Exception {
        // Given
        OptimizedJacksonConfig config = new OptimizedJacksonConfig();
        ObjectMapper mapper = config.objectMapper();

        String jsonWithExtraFields = """
                {
                    "name": "Bern",
                    "unknownField": "should be ignored",
                    "futureFeature": "for API evolution"
                }
                """;

        // When/Then - should not throw exception
        @SuppressWarnings("unchecked")
        Map<String, Object> result = mapper.readValue(jsonWithExtraFields, Map.class);

        assertThat(result)
                .containsEntry("name", "Bern")
                .containsEntry("unknownField", "should be ignored")
                .containsEntry("futureFeature", "for API evolution");
    }

    @Test
    void shouldComparePrettyVsMinifiedSize() throws Exception {
        // Given
        ObjectMapper prettyMapper = new ObjectMapper();
        prettyMapper.enable(SerializationFeature.INDENT_OUTPUT);

        ObjectMapper minifiedMapper = new OptimizedJacksonConfig().objectMapper();

        Map<String, Object> complexData = Map.of(
                "trips", Map.of(
                        "origin", Map.of("name", "Zurich HB", "id", "8503000"),
                        "destination", Map.of("name", "Bern", "id", "8507000"),
                        "legs", java.util.List.of(
                                Map.of("mode", "TRAIN", "duration", 3600),
                                Map.of("mode", "WALK", "duration", 300)
                        )
                )
        );

        // When
        String prettyJson = prettyMapper.writeValueAsString(complexData);
        String minifiedJson = minifiedMapper.writeValueAsString(complexData);

        // Then
        int prettySizeBytes = prettyJson.getBytes().length;
        int minifiedSizeBytes = minifiedJson.getBytes().length;
        double savingsPercent = ((prettySizeBytes - minifiedSizeBytes) / (double) prettySizeBytes) * 100;

        assertThat(minifiedSizeBytes)
                .as("Minified JSON should be smaller than pretty-printed")
                .isLessThan(prettySizeBytes);

        assertThat(savingsPercent)
                .as("Minification should save 5-50% depending on nesting depth")
                .isBetween(5.0, 50.0); // Higher for deeply nested structures with indentation

        System.out.printf("Pretty-printed: %d bytes%n", prettySizeBytes);
        System.out.printf("Minified: %d bytes%n", minifiedSizeBytes);
        System.out.printf("Savings: %.1f%%%n", savingsPercent);
    }
}
