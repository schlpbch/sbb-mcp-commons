package ch.sbb.mcp.commons.validation;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ValidatorsTest {

    @Test
    void requireNonEmpty_WithValidString_ShouldSucceed() {
        assertDoesNotThrow(() -> Validators.requireNonEmpty("valid", "field"));
    }

    @Test
    void requireNonEmpty_WithNull_ShouldThrowException() {
        ValidationException ex = assertThrows(ValidationException.class, 
            () -> Validators.requireNonEmpty(null, "field"));
        assertEquals("field cannot be empty", ex.getMessage());
    }

    @Test
    void requireNonEmpty_WithEmptyString_ShouldThrowException() {
        ValidationException ex = assertThrows(ValidationException.class, 
            () -> Validators.requireNonEmpty("", "field"));
        assertEquals("field cannot be empty", ex.getMessage());
    }

    @Test
    void requireNonEmpty_WithWhitespace_ShouldThrowException() {
        ValidationException ex = assertThrows(ValidationException.class, 
            () -> Validators.requireNonEmpty("   ", "field"));
        assertEquals("field cannot be empty", ex.getMessage());
    }

    @Test
    void requirePositive_WithPositiveInt_ShouldSucceed() {
        assertDoesNotThrow(() -> Validators.requirePositive(1, "field"));
    }

    @Test
    void requirePositive_WithZero_ShouldThrowException() {
        ValidationException ex = assertThrows(ValidationException.class, 
            () -> Validators.requirePositive(0, "field"));
        assertTrue(ex.getMessage().contains("must be positive"));
    }

    @Test
    void requirePositive_WithNegativeInt_ShouldThrowException() {
        ValidationException ex = assertThrows(ValidationException.class, 
            () -> Validators.requirePositive(-1, "field"));
        assertTrue(ex.getMessage().contains("must be positive"));
    }

    @Test
    void requireNonNegative_WithZero_ShouldSucceed() {
        assertDoesNotThrow(() -> Validators.requireNonNegative(0, "field"));
    }

    @Test
    void requireNonNegative_WithNegativeInt_ShouldThrowException() {
        ValidationException ex = assertThrows(ValidationException.class, 
            () -> Validators.requireNonNegative(-1, "field"));
        assertTrue(ex.getMessage().contains("cannot be negative"));
    }

    @Test
    void requireValidDate_WithValidIsoDate_ShouldSucceed() {
        assertDoesNotThrow(() -> Validators.requireValidDate("2026-01-05", "date"));
    }

    @Test
    void requireValidDate_WithInvalidFormat_ShouldThrowException() {
        ValidationException ex = assertThrows(ValidationException.class, 
            () -> Validators.requireValidDate("05.01.2026", "date"));
        assertTrue(ex.getMessage().contains("must be in yyyy-MM-dd format"));
    }

    @Test
    void requireValidDate_WithNull_ShouldThrowException() {
        assertThrows(ValidationException.class, () -> Validators.requireValidDate(null, "date"));
    }

    @Test
    void requireValidDateTime_WithValidIsoDateTime_ShouldSucceed() {
        assertDoesNotThrow(() -> Validators.requireValidDateTime("2026-01-05T14:10:00", "dateTime"));
    }

    @Test
    void requireValidLanguage_WithSupportedLanguage_ShouldSucceed() {
        assertDoesNotThrow(() -> Validators.requireValidLanguage("de"));
        assertDoesNotThrow(() -> Validators.requireValidLanguage("DE"));
        assertDoesNotThrow(() -> Validators.requireValidLanguage("en"));
    }

    @Test
    void requireValidLanguage_WithUnsupportedLanguage_ShouldThrowException() {
        assertThrows(ValidationException.class, () -> Validators.requireValidLanguage("es"));
    }

    @Test
    void requireInRange_WithinRange_ShouldSucceed() {
        assertDoesNotThrow(() -> Validators.requireInRange(5, 1, 10, "field"));
        assertDoesNotThrow(() -> Validators.requireInRange(1, 1, 10, "field"));
        assertDoesNotThrow(() -> Validators.requireInRange(10, 1, 10, "field"));
    }

    @Test
    void requireInRange_OutOfRange_ShouldThrowException() {
        assertThrows(ValidationException.class, () -> Validators.requireInRange(0, 1, 10, "field"));
        assertThrows(ValidationException.class, () -> Validators.requireInRange(11, 1, 10, "field"));
    }

    @Test
    void requireValidEmail_WithValidEmail_ShouldSucceed() {
        assertDoesNotThrow(() -> Validators.requireValidEmail("test@sbb.ch", "email"));
        assertDoesNotThrow(() -> Validators.requireValidEmail("user.name+alias@example.com", "email"));
    }

    @Test
    void requireValidEmail_WithInvalidEmail_ShouldThrowException() {
        assertThrows(ValidationException.class, () -> Validators.requireValidEmail("invalid-email", "email"));
        assertThrows(ValidationException.class, () -> Validators.requireValidEmail("test@", "email"));
        assertThrows(ValidationException.class, () -> Validators.requireValidEmail(null, "email"));
    }

    @Test
    void requireNonEmptyList_WithValidList_ShouldSucceed() {
        assertDoesNotThrow(() -> Validators.requireNonEmptyList(List.of("item"), "list"));
    }

    @Test
    void requireNonEmptyList_WithEmptyList_ShouldThrowException() {
        assertThrows(ValidationException.class, () -> Validators.requireNonEmptyList(List.of(), "list"));
    }

    @Test
    void requireValidLatitude_WithinRange_ShouldSucceed() {
        assertDoesNotThrow(() -> Validators.requireValidLatitude(45.0));
        assertDoesNotThrow(() -> Validators.requireValidLatitude(-90.0));
        assertDoesNotThrow(() -> Validators.requireValidLatitude(90.0));
    }

    @Test
    void requireValidLatitude_OutOfRange_ShouldThrowException() {
        assertThrows(ValidationException.class, () -> Validators.requireValidLatitude(90.1));
        assertThrows(ValidationException.class, () -> Validators.requireValidLatitude(-90.1));
    }

    @Test
    void requireValidLongitude_WithinRange_ShouldSucceed() {
        assertDoesNotThrow(() -> Validators.requireValidLongitude(100.0));
        assertDoesNotThrow(() -> Validators.requireValidLongitude(-180.0));
        assertDoesNotThrow(() -> Validators.requireValidLongitude(180.0));
    }

    @Test
    void requireValidLongitude_OutOfRange_ShouldThrowException() {
        assertThrows(ValidationException.class, () -> Validators.requireValidLongitude(180.1));
        assertThrows(ValidationException.class, () -> Validators.requireValidLongitude(-180.1));
    }
}
