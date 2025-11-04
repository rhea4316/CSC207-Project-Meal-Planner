package com.mealplanner.util;

import java.util.regex.Pattern;

/**
 * Utility class for string operations including validation, formatting, and sanitization.
 *
 * Responsible: Everyone (shared utility)
 */
public class StringUtil {

    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern VALID_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,20}$");

    // Special characters that should be removed for safety
    private static final Pattern UNSAFE_CHARS = Pattern.compile("[<>\"'&;]");

    /**
     * Check if a string is null or empty (after trimming).
     *
     * @param str String to check
     * @return true if null or blank
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if a string is not null and not empty.
     *
     * @param str String to check
     * @return true if has content
     */
    public static boolean hasContent(String str) {
        return !isNullOrEmpty(str);
    }

    /**
     * Safely trim a string, returning empty string if null.
     *
     * @param str String to trim
     * @return Trimmed string, or empty string if null
     */
    public static String safeTrim(String str) {
        return str == null ? "" : str.trim();
    }

    /**
     * Capitalize the first letter of a string.
     *
     * @param str String to capitalize
     * @return Capitalized string
     */
    public static String capitalize(String str) {
        if (isNullOrEmpty(str)) {
            return str;
        }

        String trimmed = str.trim();
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
    }

    /**
     * Capitalize each word in a string (title case).
     *
     * @param str String to convert
     * @return Title case string (e.g., "chicken pasta" -> "Chicken Pasta")
     */
    public static String toTitleCase(String str) {
        if (isNullOrEmpty(str)) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        String[] words = str.trim().split("\\s+");

        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                result.append(" ");
            }
            result.append(capitalize(words[i]));
        }

        return result.toString();
    }

    /**
     * Sanitize string by removing potentially unsafe characters.
     * Useful for preventing XSS or other injection attacks.
     *
     * @param str String to sanitize
     * @return Sanitized string
     */
    public static String sanitize(String str) {
        if (str == null) {
            return null;
        }

        return UNSAFE_CHARS.matcher(str).replaceAll("");
    }

    /**
     * Validate that a string contains only alphanumeric characters and spaces.
     *
     * @param str String to validate
     * @return true if valid
     */
    public static boolean isAlphanumeric(String str) {
        if (isNullOrEmpty(str)) {
            return false;
        }

        return ALPHANUMERIC_PATTERN.matcher(str).matches();
    }

    /**
     * Validate username format.
     * Rules: 3-20 characters, letters, numbers, underscore, and hyphen only.
     *
     * @param username Username to validate
     * @return true if valid
     */
    public static boolean isValidUsername(String username) {
        if (isNullOrEmpty(username)) {
            return false;
        }

        return VALID_USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Validate email format (basic validation).
     *
     * @param email Email to validate
     * @return true if valid format
     */
    public static boolean isValidEmail(String email) {
        if (isNullOrEmpty(email)) {
            return false;
        }

        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Truncate string to maximum length, adding ellipsis if needed.
     *
     * @param str String to truncate
     * @param maxLength Maximum length (including ellipsis)
     * @return Truncated string
     */
    public static String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }

        if (maxLength <= 3) {
            return str.substring(0, maxLength);
        }

        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * Check if string length is within valid range.
     *
     * @param str String to check
     * @param minLength Minimum length
     * @param maxLength Maximum length
     * @return true if length is valid
     */
    public static boolean isValidLength(String str, int minLength, int maxLength) {
        if (str == null) {
            return false;
        }

        int length = str.trim().length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * Remove extra whitespace (multiple spaces become single space).
     *
     * @param str String to clean
     * @return String with normalized whitespace
     */
    public static String normalizeWhitespace(String str) {
        if (str == null) {
            return null;
        }

        return str.trim().replaceAll("\\s+", " ");
    }

    /**
     * Join array of strings with a delimiter.
     *
     * @param strings Array of strings
     * @param delimiter Delimiter to use
     * @return Joined string
     */
    public static String join(String[] strings, String delimiter) {
        if (strings == null || strings.length == 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            if (i > 0) {
                result.append(delimiter);
            }
            result.append(strings[i]);
        }

        return result.toString();
    }

    /**
     * Pluralize a word based on count.
     * Simple implementation - adds 's' for count != 1.
     *
     * @param word Singular word
     * @param count Count to check
     * @return Pluralized word if needed
     */
    public static String pluralize(String word, int count) {
        if (count == 1) {
            return word;
        }

        // Handle common special cases
        if (word.endsWith("y")) {
            return word.substring(0, word.length() - 1) + "ies";
        }
        if (word.endsWith("s") || word.endsWith("x") || word.endsWith("ch") || word.endsWith("sh")) {
            return word + "es";
        }

        return word + "s";
    }

    /**
     * Format a list of items as a readable string.
     * Examples: "A", "A and B", "A, B, and C"
     *
     * @param items Array of item strings
     * @return Formatted list string
     */
    public static String formatList(String[] items) {
        if (items == null || items.length == 0) {
            return "";
        }

        if (items.length == 1) {
            return items[0];
        }

        if (items.length == 2) {
            return items[0] + " and " + items[1];
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                result.append(", ");
            }
            if (i == items.length - 1) {
                result.append("and ");
            }
            result.append(items[i]);
        }

        return result.toString();
    }

    /**
     * Extract numeric value from a string (first number found).
     *
     * @param str String containing number
     * @return Numeric value or 0 if not found
     */
    public static double extractNumber(String str) {
        if (isNullOrEmpty(str)) {
            return 0.0;
        }

        // Find first sequence of digits and decimal point
        StringBuilder numStr = new StringBuilder();
        boolean foundDigit = false;

        for (char c : str.toCharArray()) {
            if (Character.isDigit(c) || (c == '.' && !numStr.toString().contains("."))) {
                numStr.append(c);
                foundDigit = true;
            } else if (foundDigit) {
                break;  // Stop at first non-numeric after finding digits
            }
        }

        return NumberUtil.parseDouble(numStr.toString(), 0.0);
    }

    /**
     * Repeat a string n times.
     *
     * @param str String to repeat
     * @param times Number of times to repeat
     * @return Repeated string
     */
    public static String repeat(String str, int times) {
        if (str == null || times <= 0) {
            return "";
        }

        StringBuilder result = new StringBuilder(str.length() * times);
        for (int i = 0; i < times; i++) {
            result.append(str);
        }

        return result.toString();
    }

    /**
     * Mask a string by replacing middle characters with asterisks.
     * Useful for displaying sensitive info like passwords.
     *
     * @param str String to mask
     * @param visibleChars Number of characters to show at start and end
     * @return Masked string
     */
    public static String mask(String str, int visibleChars) {
        if (str == null || str.length() <= visibleChars * 2) {
            return str;
        }

        String start = str.substring(0, visibleChars);
        String end = str.substring(str.length() - visibleChars);
        String middle = repeat("*", str.length() - (visibleChars * 2));

        return start + middle + end;
    }

    private StringUtil() {
        // Utility class - prevent instantiation
    }
}
