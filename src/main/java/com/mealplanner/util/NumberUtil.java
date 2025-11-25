package com.mealplanner.util;

import java.text.DecimalFormat;

/**
 * Utility class for number operations including rounding, formatting, and range checking.
 *
 * Responsible: Everyone (shared utility)
 */
public class NumberUtil {

    private static final DecimalFormat ONE_DECIMAL = new DecimalFormat("0.0");
    private static final DecimalFormat TWO_DECIMALS = new DecimalFormat("0.00");

    /**
     * Round a double to a specified number of decimal places.
     *
     * @param value Value to round
     * @param decimalPlaces Number of decimal places
     * @return Rounded value
     */
    public static double round(double value, int decimalPlaces) {
        if (decimalPlaces < 0) {
            throw new IllegalArgumentException("Decimal places cannot be negative");
        }
        double multiplier = Math.pow(10, decimalPlaces);
        return Math.round(value * multiplier) / multiplier;
    }

    /**
     * Check if a number is within a valid range (inclusive).
     *
     * @param value Value to check
     * @param min Minimum allowed value
     * @param max Maximum allowed value
     * @return true if value is within range
     */
    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }

    /**
     * Clamp a value to a specified range.
     *
     * @param value Value to clamp
     * @param min Minimum value
     * @param max Maximum value
     * @return Clamped value
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    /**
     * Format a number to one decimal place.
     *
     * @param value Value to format
     * @return Formatted string (e.g., "3.5")
     */
    public static String formatOneDecimal(double value) {
        return ONE_DECIMAL.format(value);
    }

    /**
     * Format a number to two decimal places.
     *
     * @param value Value to format
     * @return Formatted string (e.g., "3.14")
     */
    public static String formatTwoDecimals(double value) {
        return TWO_DECIMALS.format(value);
    }

    /**
     * Check if a double is effectively an integer (no fractional part).
     *
     * @param value Value to check
     * @return true if value has no fractional part
     */
    public static boolean isWholeNumber(double value) {
        return Math.abs(value - Math.round(value)) < 0.0001;
    }

    /**
     * Format a fraction for display (e.g., 0.5 -> "1/2", 0.333 -> "1/3").
     * Returns decimal string if no simple fraction match.
     *
     * @param value Decimal value
     * @return Fraction string or decimal
     */
    public static String formatAsFraction(double value) {
        if (Math.abs(value - 0.5) < 0.001) {
            return "1/2";
        } else if (Math.abs(value - 0.25) < 0.001) {
            return "1/4";
        } else if (Math.abs(value - 0.75) < 0.001) {
            return "3/4";
        } else if (Math.abs(value - 0.333) < 0.001 || Math.abs(value - 0.3333) < 0.001) {
            return "1/3";
        } else if (Math.abs(value - 0.666) < 0.001 || Math.abs(value - 0.6667) < 0.001) {
            return "2/3";
        } else {
            return String.valueOf(value);
        }
    }

    /**
     * Calculate percentage (value/total * 100).
     *
     * @param value Part value
     * @param total Total value
     * @return Percentage (0-100+)
     */
    public static double calculatePercentage(double value, double total) {
        if (total == 0) {
            return 0;
        }
        return (value / total) * 100;
    }

    /**
     * Parse a string to double with a default value on error.
     *
     * @param str String to parse
     * @param defaultValue Value to return if parsing fails
     * @return Parsed double or default
     */
    public static double parseDouble(String str, double defaultValue) {
        if (str == null || str.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(str.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parse a string to int with a default value on error.
     *
     * @param str String to parse
     * @param defaultValue Value to return if parsing fails
     * @return Parsed int or default
     */
    public static int parseInt(String str, int defaultValue) {
        if (str == null || str.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Check if two doubles are approximately equal within tolerance.
     *
     * @param a First value
     * @param b Second value
     * @param tolerance Maximum allowed difference
     * @return true if values are within tolerance
     */
    public static boolean approximatelyEqual(double a, double b, double tolerance) {
        return Math.abs(a - b) <= tolerance;
    }

    /**
     * Convert a fraction string to decimal (e.g., "1/2" -> 0.5).
     *
     * @param fraction Fraction string (e.g., "1/2", "3/4")
     * @return Decimal value, or 0 if invalid
     */
    public static double fractionToDecimal(String fraction) {
        if (fraction == null || fraction.trim().isEmpty()) {
            return 0;
        }
        
        String trimmed = fraction.trim();
        if (!trimmed.contains("/")) {
            return 0;
        }
        
        String[] parts = trimmed.split("/");
        if (parts.length != 2) {
            return 0;
        }
        
        try {
            double numerator = Double.parseDouble(parts[0].trim());
            double denominator = Double.parseDouble(parts[1].trim());
            
            if (denominator == 0) {
                return 0;
            }
            
            return numerator / denominator;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private NumberUtil() {
        // Utility class - prevent instantiation
    }
}
