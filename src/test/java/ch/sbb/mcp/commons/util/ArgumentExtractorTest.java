package ch.sbb.mcp.commons.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ArgumentExtractorTest {
    
    @Test
    void extractInt_withIntegerValue_returnsValue() {
        Map<String, Object> args = Map.of("key", 42);
        assertThat(ArgumentExtractor.extractInt(args, "key", 0)).isEqualTo(42);
    }
    
    @Test
    void extractInt_withLongValue_convertsToInt() {
        Map<String, Object> args = Map.of("key", 42L);
        assertThat(ArgumentExtractor.extractInt(args, "key", 0)).isEqualTo(42);
    }
    
    @Test
    void extractInt_withDoubleValue_convertsToInt() {
        Map<String, Object> args = Map.of("key", 42.7);
        assertThat(ArgumentExtractor.extractInt(args, "key", 0)).isEqualTo(42);
    }
    
    @Test
    void extractInt_withStringValue_parsesInt() {
        Map<String, Object> args = Map.of("key", "42");
        assertThat(ArgumentExtractor.extractInt(args, "key", 0)).isEqualTo(42);
    }
    
    @Test
    void extractInt_withInvalidString_returnsDefault() {
        Map<String, Object> args = Map.of("key", "not-a-number");
        assertThat(ArgumentExtractor.extractInt(args, "key", 99)).isEqualTo(99);
    }
    
    @Test
    void extractInt_withMissingKey_returnsDefault() {
        Map<String, Object> args = Map.of();
        assertThat(ArgumentExtractor.extractInt(args, "key", 99)).isEqualTo(99);
    }
    
    @Test
    void extractInt_withNullValue_returnsDefault() {
        Map<String, Object> args = new HashMap<>();
        args.put("key", null);
        assertThat(ArgumentExtractor.extractInt(args, "key", 99)).isEqualTo(99);
    }
    
    @Test
    void extractDouble_withDoubleValue_returnsValue() {
        Map<String, Object> args = Map.of("key", 3.14);
        assertThat(ArgumentExtractor.extractDouble(args, "key", 0.0)).isEqualTo(3.14);
    }
    
    @Test
    void extractDouble_withIntegerValue_convertsToDouble() {
        Map<String, Object> args = Map.of("key", 42);
        assertThat(ArgumentExtractor.extractDouble(args, "key", 0.0)).isEqualTo(42.0);
    }
    
    @Test
    void extractDouble_withStringValue_parsesDouble() {
        Map<String, Object> args = Map.of("key", "3.14");
        assertThat(ArgumentExtractor.extractDouble(args, "key", 0.0)).isEqualTo(3.14);
    }
    
    @Test
    void extractDouble_withInvalidString_returnsDefault() {
        Map<String, Object> args = Map.of("key", "not-a-number");
        assertThat(ArgumentExtractor.extractDouble(args, "key", 99.9)).isEqualTo(99.9);
    }
    
    @Test
    void extractDouble_withMissingKey_returnsDefault() {
        Map<String, Object> args = Map.of();
        assertThat(ArgumentExtractor.extractDouble(args, "key", 99.9)).isEqualTo(99.9);
    }
    
    @Test
    void extractDoubleNullable_withValue_returnsValue() {
        Map<String, Object> args = Map.of("key", 3.14);
        assertThat(ArgumentExtractor.extractDoubleNullable(args, "key")).isEqualTo(3.14);
    }
    
    @Test
    void extractDoubleNullable_withMissingKey_returnsNull() {
        Map<String, Object> args = Map.of();
        assertThat(ArgumentExtractor.extractDoubleNullable(args, "key")).isNull();
    }
    
    @Test
    void extractDoubleNullable_withInvalidString_returnsNull() {
        Map<String, Object> args = Map.of("key", "not-a-number");
        assertThat(ArgumentExtractor.extractDoubleNullable(args, "key")).isNull();
    }
    
    @Test
    void extractString_withStringValue_returnsValue() {
        Map<String, Object> args = Map.of("key", "hello");
        assertThat(ArgumentExtractor.extractString(args, "key", "default")).isEqualTo("hello");
    }
    
    @Test
    void extractString_withNumberValue_convertsToString() {
        Map<String, Object> args = Map.of("key", 42);
        assertThat(ArgumentExtractor.extractString(args, "key", "default")).isEqualTo("42");
    }
    
    @Test
    void extractString_withMissingKey_returnsDefault() {
        Map<String, Object> args = Map.of();
        assertThat(ArgumentExtractor.extractString(args, "key", "default")).isEqualTo("default");
    }
    
    @Test
    void extractStringNullable_withValue_returnsValue() {
        Map<String, Object> args = Map.of("key", "hello");
        assertThat(ArgumentExtractor.extractStringNullable(args, "key")).isEqualTo("hello");
    }
    
    @Test
    void extractStringNullable_withMissingKey_returnsNull() {
        Map<String, Object> args = Map.of();
        assertThat(ArgumentExtractor.extractStringNullable(args, "key")).isNull();
    }
    
    @Test
    void extractBoolean_withBooleanValue_returnsValue() {
        Map<String, Object> args = Map.of("key", true);
        assertThat(ArgumentExtractor.extractBoolean(args, "key", false)).isTrue();
    }
    
    @Test
    void extractBoolean_withStringTrue_returnsTrue() {
        Map<String, Object> args = Map.of("key", "true");
        assertThat(ArgumentExtractor.extractBoolean(args, "key", false)).isTrue();
    }
    
    @Test
    void extractBoolean_withStringFalse_returnsFalse() {
        Map<String, Object> args = Map.of("key", "false");
        assertThat(ArgumentExtractor.extractBoolean(args, "key", true)).isFalse();
    }
    
    @Test
    void extractBoolean_withStringTrueCaseInsensitive_returnsTrue() {
        Map<String, Object> args = Map.of("key", "TRUE");
        assertThat(ArgumentExtractor.extractBoolean(args, "key", false)).isTrue();
    }
    
    @Test
    void extractBoolean_withInvalidString_returnsFalse() {
        // Boolean.parseBoolean returns false for any string that isn't "true" (case-insensitive)
        Map<String, Object> args = Map.of("key", "not-a-boolean");
        assertThat(ArgumentExtractor.extractBoolean(args, "key", true)).isFalse();
    }
    
    @Test
    void extractBoolean_withMissingKey_returnsDefault() {
        Map<String, Object> args = Map.of();
        assertThat(ArgumentExtractor.extractBoolean(args, "key", true)).isTrue();
    }
    
    @Test
    void extractLong_withLongValue_returnsValue() {
        Map<String, Object> args = Map.of("key", 9876543210L);
        assertThat(ArgumentExtractor.extractLong(args, "key", 0L)).isEqualTo(9876543210L);
    }
    
    @Test
    void extractLong_withIntegerValue_convertsToLong() {
        Map<String, Object> args = Map.of("key", 42);
        assertThat(ArgumentExtractor.extractLong(args, "key", 0L)).isEqualTo(42L);
    }
    
    @Test
    void extractLong_withStringValue_parsesLong() {
        Map<String, Object> args = Map.of("key", "9876543210");
        assertThat(ArgumentExtractor.extractLong(args, "key", 0L)).isEqualTo(9876543210L);
    }
    
    @Test
    void extractLong_withInvalidString_returnsDefault() {
        Map<String, Object> args = Map.of("key", "not-a-number");
        assertThat(ArgumentExtractor.extractLong(args, "key", 99L)).isEqualTo(99L);
    }
    
    @Test
    void extractLong_withMissingKey_returnsDefault() {
        Map<String, Object> args = Map.of();
        assertThat(ArgumentExtractor.extractLong(args, "key", 99L)).isEqualTo(99L);
    }
}
