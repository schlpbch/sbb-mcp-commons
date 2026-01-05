package ch.sbb.mcp.commons.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for DateTimeUtil.
 */
class DateTimeUtilTest {

    @Test
    void parseIsoDateTime_validInput_returnsLocalDateTime() {
        // Given
        String validDateTime = "2025-12-26T10:00:00";
        
        // When
        Optional<LocalDateTime> result = DateTimeUtil.parseIsoDateTime(validDateTime);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(LocalDateTime.of(2025, 12, 26, 10, 0, 0));
    }

    @Test
    void parseIsoDateTime_nullInput_returnsEmpty() {
        // When
        Optional<LocalDateTime> result = DateTimeUtil.parseIsoDateTime(null);
        
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void parseIsoDateTime_blankInput_returnsEmpty() {
        // When
        Optional<LocalDateTime> result = DateTimeUtil.parseIsoDateTime("   ");
        
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void parseIsoDateTime_invalidFormat_returnsEmpty() {
        // Given
        String invalidDateTime = "2025-12-26 10:00:00"; // Space instead of 'T'
        
        // When
        Optional<LocalDateTime> result = DateTimeUtil.parseIsoDateTime(invalidDateTime);
        
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void parseIsoDateTime_invalidDate_returnsEmpty() {
        // Given
        String invalidDateTime = "2025-13-32T10:00:00"; // Invalid month and day
        
        // When
        Optional<LocalDateTime> result = DateTimeUtil.parseIsoDateTime(invalidDateTime);
        
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void parseAndValidate_validInput_returnsLocalDateTime() {
        // Given
        String validDateTime = "2025-12-26T14:30:00";
        
        // When
        LocalDateTime result = DateTimeUtil.parseAndValidate(validDateTime, "dateTime");
        
        // Then
        assertThat(result).isEqualTo(LocalDateTime.of(2025, 12, 26, 14, 30, 0));
    }

    @Test
    void parseAndValidate_nullInput_throwsException() {
        // When / Then
        assertThatThrownBy(() -> DateTimeUtil.parseAndValidate(null, "dateTime"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("The 'dateTime' parameter is required");
    }

    @Test
    void parseAndValidate_blankInput_throwsException() {
        // When / Then
        assertThatThrownBy(() -> DateTimeUtil.parseAndValidate("  ", "dateTime"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("The 'dateTime' parameter is required");
    }

    @Test
    void parseAndValidate_invalidFormat_throwsException() {
        // Given
        String invalidDateTime = "2025-12-26 10:00:00";
        
        // When / Then
        assertThatThrownBy(() -> DateTimeUtil.parseAndValidate(invalidDateTime, "dateTime"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(DateTimeUtil.INVALID_FORMAT_MESSAGE)
            .hasMessageContaining(invalidDateTime);
    }

    @Test
    void parseAndValidate_invalidDate_throwsException() {
        // Given
        String invalidDateTime = "2025-13-32T10:00:00";
        
        // When / Then
        assertThatThrownBy(() -> DateTimeUtil.parseAndValidate(invalidDateTime, "departureTime"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(DateTimeUtil.INVALID_FORMAT_MESSAGE);
    }

    @Test
    void toSbbFormat_validInput_returnsSbbDateTime() {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2025, 12, 26, 14, 30, 45);
        
        // When
        DateTimeUtil.SbbDateTime result = DateTimeUtil.toSbbFormat(dateTime);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.date()).isEqualTo("2025-12-26");
        assertThat(result.time()).isEqualTo("14:30");
    }

    @Test
    void toSbbFormat_nullInput_returnsNull() {
        // When
        DateTimeUtil.SbbDateTime result = DateTimeUtil.toSbbFormat(null);
        
        // Then
        assertThat(result).isNull();
    }

    @Test
    void toSbbFormat_midnightTime_formatsCorrectly() {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
        
        // When
        DateTimeUtil.SbbDateTime result = DateTimeUtil.toSbbFormat(dateTime);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.date()).isEqualTo("2025-01-01");
        assertThat(result.time()).isEqualTo("00:00");
    }

    @Test
    void toSbbFormat_endOfDayTime_formatsCorrectly() {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2025, 12, 31, 23, 59, 59);
        
        // When
        DateTimeUtil.SbbDateTime result = DateTimeUtil.toSbbFormat(dateTime);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.date()).isEqualTo("2025-12-31");
        assertThat(result.time()).isEqualTo("23:59");
    }

    @Test
    void sbbDateTime_record_isImmutable() {
        // Given
        DateTimeUtil.SbbDateTime sbbDateTime = new DateTimeUtil.SbbDateTime("2025-12-26", "14:30");
        
        // Then
        assertThat(sbbDateTime.date()).isEqualTo("2025-12-26");
        assertThat(sbbDateTime.time()).isEqualTo("14:30");
    }

    @Test
    void invalidFormatMessage_isConsistent() {
        // Then
        assertThat(DateTimeUtil.INVALID_FORMAT_MESSAGE)
            .contains("ISO-8601")
            .contains("2025-12-26T10:00:00");
    }
}
