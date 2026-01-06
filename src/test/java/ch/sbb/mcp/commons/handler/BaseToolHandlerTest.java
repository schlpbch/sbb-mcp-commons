package ch.sbb.mcp.commons.handler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BaseToolHandler Tests")
class BaseToolHandlerTest {

    @Nested
    @DisplayName("Execute Flow Tests")
    class ExecuteFlowTests {

        @Test
        @DisplayName("Should validate, parse, and execute successfully")
        void execute_WithValidInput_ShouldSucceed() {
            // Given
            TestToolHandler handler = new TestToolHandler();
            Map<String, Object> args = Map.of(
                "name", "test",
                "value", 42
            );

            // When
            Mono<String> result = handler.execute(args);

            // Then
            StepVerifier.create(result)
                .expectNext("Processed: test with value 42")
                .verifyComplete();
        }

        @Test
        @DisplayName("Should propagate validation errors")
        void execute_WithInvalidInput_ShouldPropagateValidationError() {
            // Given
            TestToolHandler handler = new TestToolHandler();
            Map<String, Object> args = Map.of("value", 42); // missing 'name'

            // When
            Mono<String> result = handler.execute(args);

            // Then
            StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof IllegalArgumentException 
                    && e.getMessage().contains("Missing required parameter: name"))
                .verify();
        }

        @Test
        @DisplayName("Should propagate execution errors")
        void execute_WithExecutionError_ShouldPropagateError() {
            // Given
            FailingToolHandler handler = new FailingToolHandler();
            Map<String, Object> args = Map.of("name", "test", "value", 42);

            // When
            Mono<String> result = handler.execute(args);

            // Then
            StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof RuntimeException 
                    && e.getMessage().equals("Execution failed"))
                .verify();
        }
    }

    @Nested
    @DisplayName("String Argument Extraction Tests")
    class StringArgumentTests {

        @Test
        @DisplayName("getRequiredString should return value when present")
        void getRequiredString_WithValue_ShouldReturnValue() {
            TestToolHandler handler = new TestToolHandler();
            Map<String, Object> args = Map.of("field", "value");

            String result = handler.getRequiredString(args, "field");

            assertEquals("value", result);
        }

        @Test
        @DisplayName("getRequiredString should throw when missing")
        void getRequiredString_WhenMissing_ShouldThrow() {
            TestToolHandler handler = new TestToolHandler();
            Map<String, Object> args = Map.of();

            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> handler.getRequiredString(args, "field")
            );

            assertEquals("Missing required parameter: field", ex.getMessage());
        }

        @Test
        @DisplayName("getOptionalString should return value when present")
        void getOptionalString_WithValue_ShouldReturnValue() {
            TestToolHandler handler = new TestToolHandler();
            Map<String, Object> args = Map.of("field", "value");

            String result = handler.getOptionalString(args, "field", "default");

            assertEquals("value", result);
        }

        @Test
        @DisplayName("getOptionalString should return default when missing")
        void getOptionalString_WhenMissing_ShouldReturnDefault() {
            TestToolHandler handler = new TestToolHandler();
            Map<String, Object> args = Map.of();

            String result = handler.getOptionalString(args, "field", "default");

            assertEquals("default", result);
        }
    }

    @Nested
    @DisplayName("Double Argument Extraction Tests")
    class DoubleArgumentTests {

        @Test
        @DisplayName("getRequiredDouble should return value from Number")
        void getRequiredDouble_WithNumber_ShouldReturnValue() {
            TestToolHandler handler = new TestToolHandler();
            Map<String, Object> args = Map.of("field", 42.5);

            double result = handler.getRequiredDouble(args, "field");

            assertEquals(42.5, result, 0.001);
        }

        @Test
        @DisplayName("getRequiredDouble should parse string value")
        void getRequiredDouble_WithString_ShouldParseValue() {
            TestToolHandler handler = new TestToolHandler();
            Map<String, Object> args = Map.of("field", "42.5");

            double result = handler.getRequiredDouble(args, "field");

            assertEquals(42.5, result, 0.001);
        }

        @Test
        @DisplayName("getRequiredDouble should throw when missing")
        void getRequiredDouble_WhenMissing_ShouldThrow() {
            TestToolHandler handler = new TestToolHandler();
            Map<String, Object> args = Map.of();

            assertThrows(
                IllegalArgumentException.class,
                () -> handler.getRequiredDouble(args, "field")
            );
        }

        @Test
        @DisplayName("getRequiredDouble should throw on invalid format")
        void getRequiredDouble_WithInvalidFormat_ShouldThrow() {
            TestToolHandler handler = new TestToolHandler();
            Map<String, Object> args = Map.of("field", "not-a-number");

            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> handler.getRequiredDouble(args, "field")
            );

            assertTrue(ex.getMessage().contains("Invalid number for field"));
        }

        @Test
        @DisplayName("getOptionalDouble should return default when missing")
        void getOptionalDouble_WhenMissing_ShouldReturnDefault() {
            TestToolHandler handler = new TestToolHandler();
            Map<String, Object> args = Map.of();

            double result = handler.getOptionalDouble(args, "field", 99.9);

            assertEquals(99.9, result, 0.001);
        }

        @Test
        @DisplayName("getOptionalDouble should return default on parse error")
        void getOptionalDouble_WithInvalidFormat_ShouldReturnDefault() {
            TestToolHandler handler = new TestToolHandler();
            Map<String, Object> args = Map.of("field", "invalid");

            double result = handler.getOptionalDouble(args, "field", 99.9);

            assertEquals(99.9, result, 0.001);
        }
    }

    @Nested
    @DisplayName("Integer Argument Extraction Tests")
    class IntegerArgumentTests {

        @Test
        @DisplayName("getRequiredInt should return value from Number")
        void getRequiredInt_WithNumber_ShouldReturnValue() {
            TestToolHandler handler = new TestToolHandler();
            Map<String, Object> args = Map.of("field", 42);

            int result = handler.getRequiredInt(args, "field");

            assertEquals(42, result);
        }

        @Test
        @DisplayName("getRequiredInt should parse string value")
        void getRequiredInt_WithString_ShouldParseValue() {
            TestToolHandler handler = new TestToolHandler();
            Map<String, Object> args = Map.of("field", "42");

            int result = handler.getRequiredInt(args, "field");

            assertEquals(42, result);
        }

        @Test
        @DisplayName("getRequiredInt should throw when missing")
        void getRequiredInt_WhenMissing_ShouldThrow() {
            TestToolHandler handler = new TestToolHandler();
            Map<String, Object> args = Map.of();

            assertThrows(
                IllegalArgumentException.class,
                () -> handler.getRequiredInt(args, "field")
            );
        }

        @Test
        @DisplayName("getOptionalInt should return default when missing")
        void getOptionalInt_WhenMissing_ShouldReturnDefault() {
            TestToolHandler handler = new TestToolHandler();
            Map<String, Object> args = Map.of();

            int result = handler.getOptionalInt(args, "field", 99);

            assertEquals(99, result);
        }
    }

    @Nested
    @DisplayName("Boolean Argument Extraction Tests")
    class BooleanArgumentTests {

        @Test
        @DisplayName("getRequiredBoolean should return value from Boolean")
        void getRequiredBoolean_WithBoolean_ShouldReturnValue() {
            TestToolHandler handler = new TestToolHandler();
            Map<String, Object> args = Map.of("field", true);

            boolean result = handler.getRequiredBoolean(args, "field");

            assertTrue(result);
        }

        @Test
        @DisplayName("getRequiredBoolean should parse string value")
        void getRequiredBoolean_WithString_ShouldParseValue() {
            TestToolHandler handler = new TestToolHandler();
            Map<String, Object> args = Map.of("field", "true");

            boolean result = handler.getRequiredBoolean(args, "field");

            assertTrue(result);
        }

        @Test
        @DisplayName("getRequiredBoolean should throw when missing")
        void getRequiredBoolean_WhenMissing_ShouldThrow() {
            TestToolHandler handler = new TestToolHandler();
            Map<String, Object> args = Map.of();

            assertThrows(
                IllegalArgumentException.class,
                () -> handler.getRequiredBoolean(args, "field")
            );
        }

        @Test
        @DisplayName("getOptionalBoolean should return default when missing")
        void getOptionalBoolean_WhenMissing_ShouldReturnDefault() {
            TestToolHandler handler = new TestToolHandler();
            Map<String, Object> args = Map.of();

            boolean result = handler.getOptionalBoolean(args, "field", true);

            assertTrue(result);
        }
    }

    // Test implementation of BaseToolHandler
    static class TestToolHandler extends BaseToolHandler<TestInput, String> {

        @Override
        protected Mono<String> executeInternal(TestInput input) {
            return Mono.just("Processed: " + input.name + " with value " + input.value);
        }

        @Override
        protected String getToolName() {
            return "testTool";
        }

        @Override
        protected TestInput validateAndParse(Map<String, Object> arguments) {
            String name = getRequiredString(arguments, "name");
            int value = getRequiredInt(arguments, "value");
            return new TestInput(name, value);
        }
    }

    // Test input class
    static class TestInput {
        final String name;
        final int value;

        TestInput(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }

    // Failing tool handler for error testing
    static class FailingToolHandler extends BaseToolHandler<TestInput, String> {

        @Override
        protected Mono<String> executeInternal(TestInput input) {
            return Mono.error(new RuntimeException("Execution failed"));
        }

        @Override
        protected String getToolName() {
            return "failingTool";
        }

        @Override
        protected TestInput validateAndParse(Map<String, Object> arguments) {
            String name = getRequiredString(arguments, "name");
            int value = getRequiredInt(arguments, "value");
            return new TestInput(name, value);
        }
    }
}
