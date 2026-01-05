package ch.sbb.mcp.commons.validation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Reusable validation utilities for MCP tools.
 * Provides common validation methods to ensure input data integrity.
 */
public class Validators {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final List<String> VALID_LANGUAGES = List.of("de", "fr", "it", "en");
    
    /**
     * Validates that a string is not null or empty.
     *
     * @param value the value to validate
     * @param fieldName the name of the field (for error messages)
     * @throws ValidationException if the value is null or empty
     */
    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty");
        }
    }
    
    /**
     * Validates that an integer is positive (> 0).
     *
     * @param value the value to validate
     * @param fieldName the name of the field (for error messages)
     * @throws ValidationException if the value is not positive
     */
    public static void requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new ValidationException(fieldName + " must be positive, got: " + value);
        }
    }
    
    /**
     * Validates that an integer is non-negative (>= 0).
     *
     * @param value the value to validate
     * @param fieldName the name of the field (for error messages)
     * @throws ValidationException if the value is negative
     */
    public static void requireNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new ValidationException(fieldName + " cannot be negative, got: " + value);
        }
    }
    
    /**
     * Validates that a string is a valid date in yyyy-MM-dd format.
     *
     * @param date the date string to validate
     * @param fieldName the name of the field (for error messages)
     * @throws ValidationException if the date is invalid
     */
    public static void requireValidDate(String date, String fieldName) {
        if (date == null) {
            throw new ValidationException(fieldName + " cannot be null");
        }
        try {
            LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new ValidationException(
                fieldName + " must be in yyyy-MM-dd format, got: " + date
            );
        }
    }
    
    /**
     * Validates that a string is a valid date-time in ISO-8601 format.
     *
     * @param dateTime the date-time string to validate
     * @param fieldName the name of the field (for error messages)
     * @throws ValidationException if the date-time is invalid
     */
    public static void requireValidDateTime(String dateTime, String fieldName) {
        if (dateTime == null) {
            throw new ValidationException(fieldName + " cannot be null");
        }
        try {
            LocalDateTime.parse(dateTime);
        } catch (DateTimeParseException e) {
            throw new ValidationException(
                fieldName + " must be in ISO-8601 format, got: " + dateTime
            );
        }
    }
    
    /**
     * Validates that a language code is supported.
     * Supported languages: de, fr, it, en
     *
     * @param language the language code to validate
     * @throws ValidationException if the language is not supported
     */
    public static void requireValidLanguage(String language) {
        if (language == null) {
            throw new ValidationException("Language cannot be null");
        }
        if (!VALID_LANGUAGES.contains(language.toLowerCase())) {
            throw new ValidationException(
                "Language must be one of " + VALID_LANGUAGES + ", got: " + language
            );
        }
    }
    
    /**
     * Validates that a value is within a specified range (inclusive).
     *
     * @param value the value to validate
     * @param min the minimum allowed value (inclusive)
     * @param max the maximum allowed value (inclusive)
     * @param fieldName the name of the field (for error messages)
     * @throws ValidationException if the value is out of range
     */
    public static void requireInRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            throw new ValidationException(
                fieldName + " must be between " + min + " and " + max + ", got: " + value
            );
        }
    }
    
    /**
     * Validates that an email address is valid.
     *
     * @param email the email to validate
     * @param fieldName the name of the field (for error messages)
     * @throws ValidationException if the email is invalid
     */
    public static void requireValidEmail(String email, String fieldName) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException(fieldName + " must be a valid email address");
        }
    }
    
    /**
     * Validates that a list is not null or empty.
     *
     * @param list the list to validate
     * @param fieldName the name of the field (for error messages)
     * @throws ValidationException if the list is null or empty
     */
    public static void requireNonEmptyList(List<?> list, String fieldName) {
        if (list == null || list.isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty");
        }
    }
    
    /**
     * Validates that latitude is within valid range (-90 to 90).
     *
     * @param latitude the latitude to validate
     * @throws ValidationException if latitude is out of range
     */
    public static void requireValidLatitude(double latitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new ValidationException(
                "Latitude must be between -90 and 90, got: " + latitude
            );
        }
    }
    
    /**
     * Validates that longitude is within valid range (-180 to 180).
     *
     * @param longitude the longitude to validate
     * @throws ValidationException if longitude is out of range
     */
    public static void requireValidLongitude(double longitude) {
        if (longitude < -180.0 || longitude > 180.0) {
            throw new ValidationException(
                "Longitude must be between -180 and 180, got: " + longitude
            );
        }
    }
}
