package com.mealplanner.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Objects;

/**
 * Utility class for parsing ingredient strings and extracting quantity, unit, and name.
 * Handles various formats like "2 cups flour", "1/2 cup milk", "3 eggs", etc.
 *
 * Responsible: Everyone (shared utility)
 */
public class IngredientParser {

    // Common unit names (single word or two words)
    // This helps distinguish units from ingredient names
    private static final String UNIT_PATTERN_STR = 
        "(?:cups?|tablespoons?|teaspoons?|ounces?|pounds?|lbs?|grams?|kilograms?|kg|ml|milliliters?|liters?|L|" +
        "fluid\\s+ounces?|fl\\s+oz|pieces?|items?|whole|pinch|dash|to\\s+taste|handful|sprinkle)";
    
    // Pattern to match: optional number (integer or decimal or fraction), optional unit, ingredient name
    // Examples: "2 cups flour", "1/2 cup milk", "3 eggs", "1.5 cups sugar", "pinch of salt"
    // Simplified pattern to avoid PatternSyntaxException - split into patterns for clarity
    
    // Pattern 1: With quantity and unit - matches "2 cups flour", "1/2 cup milk", "1 1/2 cups water"
    // Group 1: quantity, Group 2: unit, Group 3: name
    private static final Pattern INGREDIENT_WITH_QUANTITY_AND_UNIT_PATTERN = Pattern.compile(
        "^\\s*(\\d+(?:\\.\\d+)?|\\d+/\\d+|\\d+\\s+\\d+/\\d+)\\s+(" + UNIT_PATTERN_STR + ")\\s+(?:of\\s+)?(.+)\\s*$",
        Pattern.CASE_INSENSITIVE
    );
    
    // Pattern 2: With quantity but no unit - matches "3 eggs", "2 tomatoes"
    // Group 1: quantity, Group 2: name
    private static final Pattern INGREDIENT_WITH_QUANTITY_NO_UNIT_PATTERN = Pattern.compile(
        "^\\s*(\\d+(?:\\.\\d+)?|\\d+/\\d+|\\d+\\s+\\d+/\\d+)\\s+(.+)\\s*$",
        Pattern.CASE_INSENSITIVE
    );
    
    // Pattern 3: Without quantity but with unit - matches "pinch of salt", "cup flour"
    // Group 1: unit, Group 2: name
    private static final Pattern INGREDIENT_WITHOUT_QUANTITY_WITH_UNIT_PATTERN = Pattern.compile(
        "^\\s*(" + UNIT_PATTERN_STR + ")\\s+(?:of\\s+)?(.+)\\s*$",
        Pattern.CASE_INSENSITIVE
    );
    
    // Pattern 4: Without quantity and unit - matches "salt", "flour"
    // Group 1: name
    private static final Pattern INGREDIENT_NAME_ONLY_PATTERN = Pattern.compile(
        "^\\s*(.+)\\s*$",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * Parsed ingredient information.
     */
    public static class ParsedIngredient {
        private final double quantity;
        private final String unit;
        private final String name;

        public ParsedIngredient(double quantity, String unit, String name) {
            this.quantity = quantity;
            this.unit = unit != null ? unit.trim() : "";
            this.name = name != null ? name.trim() : "";
        }

        public double getQuantity() {
            return quantity;
        }

        public String getUnit() {
            return unit;
        }

        public String getName() {
            return name;
        }

        /**
         * Formats the parsed ingredient back to a string.
         *
         * @return Formatted ingredient string
         */
        public String format() {
            if (quantity <= 0) {
                return name;
            }

            StringBuilder sb = new StringBuilder();
            
            // Format quantity (handle fractions and decimals)
            String quantityStr = formatQuantity(quantity);
            sb.append(quantityStr);

            // Add unit if present
            if (!unit.isEmpty()) {
                sb.append(" ").append(unit);
            }

            // Add ingredient name
            if (!name.isEmpty()) {
                sb.append(" ").append(name);
            }

            return sb.toString();
        }

        /**
         * Formats a quantity value, preferring fractions for common values.
         */
        private String formatQuantity(double qty) {
            // Check for common fractions
            if (Math.abs(qty - 0.25) < 0.001) return "1/4";
            if (Math.abs(qty - 0.33) < 0.01) return "1/3";
            if (Math.abs(qty - 0.5) < 0.001) return "1/2";
            if (Math.abs(qty - 0.67) < 0.01) return "2/3";
            if (Math.abs(qty - 0.75) < 0.001) return "3/4";
            if (Math.abs(qty - 1.5) < 0.001) return "1 1/2";
            if (Math.abs(qty - 2.5) < 0.001) return "2 1/2";
            if (Math.abs(qty - 3.5) < 0.001) return "3 1/2";

            // For whole numbers, return as integer
            if (qty == (int) qty) {
                return String.valueOf((int) qty);
            }

            // For decimals, format to 1 decimal place
            return String.format("%.1f", qty);
        }
    }

    /**
     * Parse an ingredient string to extract quantity, unit, and name.
     *
     * @param ingredientString Ingredient string (e.g., "2 cups flour", "1/2 cup milk")
     * @return ParsedIngredient object with parsed components
     * @throws IllegalArgumentException if ingredient string is null or empty
     */
    public static ParsedIngredient parse(String ingredientString) {
        Objects.requireNonNull(ingredientString, "Ingredient string cannot be null");
        
        String trimmed = ingredientString.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Ingredient string cannot be empty");
        }

        // Try patterns in order of specificity
        String quantityStr = null;
        String unitStr = null;
        String nameStr = null;
        Matcher matcher;
        
        // Pattern 1: With quantity and unit (most specific)
        matcher = INGREDIENT_WITH_QUANTITY_AND_UNIT_PATTERN.matcher(trimmed);
        if (matcher.matches()) {
            quantityStr = matcher.group(1);
            unitStr = matcher.group(2);
            nameStr = matcher.group(3);
        } else {
            // Pattern 2: With quantity but no unit
            matcher = INGREDIENT_WITH_QUANTITY_NO_UNIT_PATTERN.matcher(trimmed);
            if (matcher.matches()) {
                quantityStr = matcher.group(1);
                nameStr = matcher.group(2);
            } else {
                // Pattern 3: Without quantity but with unit
                matcher = INGREDIENT_WITHOUT_QUANTITY_WITH_UNIT_PATTERN.matcher(trimmed);
                if (matcher.matches()) {
                    unitStr = matcher.group(1);
                    nameStr = matcher.group(2);
                } else {
                    // Pattern 4: Name only
                    matcher = INGREDIENT_NAME_ONLY_PATTERN.matcher(trimmed);
                    if (matcher.matches()) {
                        nameStr = matcher.group(1);
                    } else {
                        // Fallback: treat entire string as ingredient name
                        return new ParsedIngredient(0, "", trimmed);
                    }
                }
            }
        }

        // Parse quantity
        double quantity = parseQuantity(quantityStr);

        // Clean up unit (remove "of" if present)
        String unit = unitStr != null ? unitStr.trim() : "";
        if (unit.equalsIgnoreCase("of")) {
            unit = "";
        }

        // Clean up name - if nameStr is null or empty, use trimmed string
        String name = (nameStr != null && !nameStr.trim().isEmpty()) ? nameStr.trim() : trimmed;

        return new ParsedIngredient(quantity, unit, name);
    }

    /**
     * Parse a quantity string to a double value.
     * Handles integers, decimals, and fractions.
     *
     * @param quantityStr Quantity string (e.g., "2", "1.5", "1/2", "1 1/2")
     * @return Parsed quantity as double
     */
    private static double parseQuantity(String quantityStr) {
        if (quantityStr == null || quantityStr.trim().isEmpty()) {
            return 0;
        }

        String trimmed = quantityStr.trim();

        // Handle mixed numbers (e.g., "1 1/2")
        if (trimmed.contains(" ")) {
            String[] parts = trimmed.split("\\s+", 2);
            if (parts.length == 2) {
                try {
                    double whole = Double.parseDouble(parts[0]);
                    double fraction = parseFraction(parts[1]);
                    return whole + fraction;
                } catch (NumberFormatException e) {
                    // Fall through to try parsing as single value
                }
            }
        }

        // Handle fractions (e.g., "1/2", "3/4")
        if (trimmed.contains("/")) {
            return parseFraction(trimmed);
        }

        // Handle decimal or integer
        try {
            return Double.parseDouble(trimmed);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Parse a fraction string to a double value.
     *
     * @param fractionStr Fraction string (e.g., "1/2", "3/4")
     * @return Parsed fraction as double
     */
    private static double parseFraction(String fractionStr) {
        if (fractionStr == null || !fractionStr.contains("/")) {
            return 0;
        }

        String[] parts = fractionStr.split("/");
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

    /**
     * Scale an ingredient string by a factor.
     *
     * @param ingredientString Original ingredient string
     * @param scaleFactor Scaling factor (e.g., 2.0 to double, 0.5 to halve)
     * @return Scaled ingredient string
     */
    public static String scaleIngredient(String ingredientString, double scaleFactor) {
        if (scaleFactor <= 0) {
            throw new IllegalArgumentException("Scale factor must be positive");
        }

        ParsedIngredient parsed = parse(ingredientString);
        double scaledQuantity = parsed.getQuantity() * scaleFactor;
        
        return new ParsedIngredient(scaledQuantity, parsed.getUnit(), parsed.getName()).format();
    }

    private IngredientParser() {
        // Utility class - prevent instantiation
    }
}

