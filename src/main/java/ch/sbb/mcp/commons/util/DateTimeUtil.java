package ch.sbb.mcp.commons.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for handling Date/Time parsing and formatting for SBB API interactions.
 * 
 * <p>Provides consistent date/time parsing and formatting across all MCP tools,
 * ensuring uniform error messages and validation.</p>
 */
public class DateTimeUtil {

    private static final Logger log = LoggerFactory.getLogger(DateTimeUtil.class);

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ISO_TIME_HM = DateTimeFormatter.ofPattern("HH:mm");
    
    /**
     * Standard error message for invalid date/time format.
     */
    public static final String INVALID_FORMAT_MESSAGE = 
        "Invalid date/time format. Expected ISO-8601 (e.g., '2025-12-26T10:00:00')";

    private DateTimeUtil() {
        // Utility class
    }

    /**
     * Parses an ISO-8601 date-time string (e.g., "2024-12-16T10:00:00").
     *
     * @param dateTimeStr The date-time string to parse.
     * @return Optional containing LocalDateTime if successful, empty if invalid/null.
     */
    public static Optional<LocalDateTime> parseIsoDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME));
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse DateTime: '{}' - {}", dateTimeStr, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Parses an ISO-8601 date-time string and throws an exception if invalid.
     * 
     * <p>Use this method when you need to fail fast with a descriptive error message.</p>
     *
     * @param dateTimeStr The date-time string to parse.
     * @param parameterName The name of the parameter (for error messages).
     * @return LocalDateTime if successful.
     * @throws IllegalArgumentException if the date-time string is invalid.
     */
    public static LocalDateTime parseAndValidate(String dateTimeStr, String parameterName) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            throw new IllegalArgumentException(
                String.format("The '%s' parameter is required", parameterName)
            );
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse DateTime for '{}': '{}' - {}", parameterName, dateTimeStr, e.getMessage());
            throw new IllegalArgumentException(
                String.format("%s. Got: '%s'", INVALID_FORMAT_MESSAGE, dateTimeStr)
            );
        }
    }

    /**
     * Formats a LocalDateTime to various SBB API required formats.
     * 
     * <p>Contains separate date (yyyy-MM-dd) and time (HH:mm) strings
     * as required by many SBB API endpoints.</p>
     */
    public record SbbDateTime(String date, String time) {}

    /**
     * Converts a LocalDateTime to the separate date (yyyy-MM-dd) and time (HH:mm) strings
     * required by many SBB API endpoints.
     * 
     * @param dateTime The LocalDateTime to convert.
     * @return SbbDateTime record with date and time strings, or null if input is null.
     */
    public static SbbDateTime toSbbFormat(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return new SbbDateTime(
            dateTime.format(ISO_DATE),
            dateTime.format(ISO_TIME_HM)
        );
    }
}
