package ch.sbb.mcp.commons.transformation;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class BaseResponseTransformerTest {

    // Concrete implementation for testing
    private static class TestTransformer extends BaseResponseTransformer<String, String> {
        @Override
        public String transform(String source) {
            return source;
        }
    }

    private final TestTransformer transformer = new TestTransformer();

    @Test
    void formatDateTime_WithValidIso_ShouldFormatCorrectly() {
        String input = "2026-01-05T14:30:00";
        String expected = "05.01.2026 14:30";
        assertEquals(expected, transformer.formatDateTime(input));
    }

    @Test
    void formatDateTime_WithInvalidIso_ShouldReturnOriginal() {
        String input = "invalid-date";
        assertEquals(input, transformer.formatDateTime(input));
    }

    @Test
    void formatDateTime_WithNull_ShouldReturnNull() {
        assertNull(transformer.formatDateTime(null));
    }

    @Test
    void formatDuration_WithMinutes_ShouldFormatCorrectly() {
        assertEquals("45m", transformer.formatDuration(45));
        assertEquals("1h", transformer.formatDuration(60));
        assertEquals("1h 30m", transformer.formatDuration(90));
        assertEquals("2h 5m", transformer.formatDuration(125));
    }

    @Test
    void formatDuration_WithDurationObject_ShouldFormatCorrectly() {
        assertEquals("1h 30m", transformer.formatDuration(Duration.ofMinutes(90)));
        assertNull(transformer.formatDuration((Duration) null));
    }

    @Test
    void sanitizeText_WithExtraWhitespace_ShouldNormalize() {
        assertEquals("hello world", transformer.sanitizeText("  hello   world  "));
        assertNull(transformer.sanitizeText(null));
    }

    @Test
    void toIsoString_WithInstant_ShouldConvert() {
        Instant now = Instant.now();
        assertEquals(now.toString(), transformer.toIsoString(now));
        assertNull(transformer.toIsoString((Instant) null));
    }

    @Test
    void toIsoString_WithEpochMillis_ShouldConvert() {
        long millis = 1736083200000L; // 05.01.2025
        assertTrue(transformer.toIsoString(millis).contains("2025-01-05"));
    }

    @Test
    void getOrDefault_WithNull_ShouldReturnDefault() {
        assertEquals("default", transformer.getOrDefault(null, "default"));
        assertEquals("value", transformer.getOrDefault("value", "default"));
    }
}
