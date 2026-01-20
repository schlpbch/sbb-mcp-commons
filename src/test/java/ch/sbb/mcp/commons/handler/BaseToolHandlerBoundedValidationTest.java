package ch.sbb.mcp.commons.handler;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test bounded validation methods in BaseToolHandler.
 */
class BaseToolHandlerBoundedValidationTest {

    private static class TestHandler extends BaseToolHandler<String, String> {
        @Override
        protected String getToolName() {
            return "test-tool";
        }

        @Override
        protected String validateAndParse(Map<String, Object> arguments) {
            return "validated";
        }

        @Override
        protected Mono<String> executeInternal(String input) {
            return Mono.just("result");
        }

        // Expose protected methods for testing
        public int testGetOptionalIntBounded(Map<String, Object> args, String key, int defaultValue, int min, int max) {
            return getOptionalIntBounded(args, key, defaultValue, min, max);
        }

        public int testGetRequiredIntBounded(Map<String, Object> args, String key, int min, int max) {
            return getRequiredIntBounded(args, key, min, max);
        }

        public double testGetOptionalDoubleBounded(Map<String, Object> args, String key, double defaultValue, double min, double max) {
            return getOptionalDoubleBounded(args, key, defaultValue, min, max);
        }

        public double testGetRequiredDoubleBounded(Map<String, Object> args, String key, double min, double max) {
            return getRequiredDoubleBounded(args, key, min, max);
        }
    }

    private final TestHandler handler = new TestHandler();

    @Test
    void testGetOptionalIntBounded_ValidValue() {
        Map<String, Object> args = Map.of("limit", 25);
        int result = handler.testGetOptionalIntBounded(args, "limit", 10, 1, 50);
        assertEquals(25, result);
    }

    @Test
    void testGetOptionalIntBounded_DefaultValue() {
        Map<String, Object> args = new HashMap<>();
        int result = handler.testGetOptionalIntBounded(args, "limit", 10, 1, 50);
        assertEquals(10, result);
    }

    @Test
    void testGetOptionalIntBounded_NegativeValue() {
        Map<String, Object> args = Map.of("limit", -1);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> handler.testGetOptionalIntBounded(args, "limit", 10, 1, 50));
        assertEquals("Parameter 'limit' must be between 1 and 50, got: -1", ex.getMessage());
    }

    @Test
    void testGetOptionalIntBounded_ZeroValue() {
        Map<String, Object> args = Map.of("limit", 0);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> handler.testGetOptionalIntBounded(args, "limit", 10, 1, 50));
        assertEquals("Parameter 'limit' must be between 1 and 50, got: 0", ex.getMessage());
    }

    @Test
    void testGetOptionalIntBounded_ExceedsMax() {
        Map<String, Object> args = Map.of("limit", 100);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> handler.testGetOptionalIntBounded(args, "limit", 10, 1, 50));
        assertEquals("Parameter 'limit' must be between 1 and 50, got: 100", ex.getMessage());
    }

    @Test
    void testGetOptionalIntBounded_MinValue() {
        Map<String, Object> args = Map.of("limit", 1);
        int result = handler.testGetOptionalIntBounded(args, "limit", 10, 1, 50);
        assertEquals(1, result);
    }

    @Test
    void testGetOptionalIntBounded_MaxValue() {
        Map<String, Object> args = Map.of("limit", 50);
        int result = handler.testGetOptionalIntBounded(args, "limit", 10, 1, 50);
        assertEquals(50, result);
    }

    @Test
    void testGetRequiredIntBounded_ValidValue() {
        Map<String, Object> args = Map.of("limit", 25);
        int result = handler.testGetRequiredIntBounded(args, "limit", 1, 50);
        assertEquals(25, result);
    }

    @Test
    void testGetRequiredIntBounded_MissingValue() {
        Map<String, Object> args = new HashMap<>();
        assertThrows(IllegalArgumentException.class,
            () -> handler.testGetRequiredIntBounded(args, "limit", 1, 50));
    }

    @Test
    void testGetRequiredIntBounded_OutOfBounds() {
        Map<String, Object> args = Map.of("limit", -5);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> handler.testGetRequiredIntBounded(args, "limit", 1, 50));
        assertEquals("Parameter 'limit' must be between 1 and 50, got: -5", ex.getMessage());
    }

    @Test
    void testGetOptionalDoubleBounded_ValidValue() {
        Map<String, Object> args = Map.of("radius", 25.5);
        double result = handler.testGetOptionalDoubleBounded(args, "radius", 1.0, 0.01, 50.0);
        assertEquals(25.5, result, 0.001);
    }

    @Test
    void testGetOptionalDoubleBounded_DefaultValue() {
        Map<String, Object> args = new HashMap<>();
        double result = handler.testGetOptionalDoubleBounded(args, "radius", 1.0, 0.01, 50.0);
        assertEquals(1.0, result, 0.001);
    }

    @Test
    void testGetOptionalDoubleBounded_NegativeValue() {
        Map<String, Object> args = Map.of("radius", -1.5);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> handler.testGetOptionalDoubleBounded(args, "radius", 1.0, 0.01, 50.0));
        assertEquals("Parameter 'radius' must be between 0.01 and 50.00, got: -1.50", ex.getMessage());
    }

    @Test
    void testGetOptionalDoubleBounded_ExceedsMax() {
        Map<String, Object> args = Map.of("radius", 100.0);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> handler.testGetOptionalDoubleBounded(args, "radius", 1.0, 0.01, 50.0));
        assertEquals("Parameter 'radius' must be between 0.01 and 50.00, got: 100.00", ex.getMessage());
    }

    @Test
    void testGetRequiredDoubleBounded_ValidValue() {
        Map<String, Object> args = Map.of("radius", 10.5);
        double result = handler.testGetRequiredDoubleBounded(args, "radius", 0.01, 50.0);
        assertEquals(10.5, result, 0.001);
    }

    @Test
    void testGetRequiredDoubleBounded_MissingValue() {
        Map<String, Object> args = new HashMap<>();
        assertThrows(IllegalArgumentException.class,
            () -> handler.testGetRequiredDoubleBounded(args, "radius", 0.01, 50.0));
    }

    @Test
    void testGetRequiredDoubleBounded_OutOfBounds() {
        Map<String, Object> args = Map.of("radius", -2.5);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> handler.testGetRequiredDoubleBounded(args, "radius", 0.01, 50.0));
        assertEquals("Parameter 'radius' must be between 0.01 and 50.00, got: -2.50", ex.getMessage());
    }
}
