package com.mealplanner.util;

import com.mealplanner.entity.Unit;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for converting ingredient quantities between different units.
 * Handles weight, volume, and count conversions.
 *
 * Responsible: Everyone (shared utility, especially for serving size adjustments)
 */
public class IngredientConverter {

    // Weight conversions to grams
    private static final Map<Unit, Double> WEIGHT_TO_GRAMS = new HashMap<>();
    static {
        WEIGHT_TO_GRAMS.put(Unit.GRAMS, 1.0);
        WEIGHT_TO_GRAMS.put(Unit.KILOGRAMS, 1000.0);
        WEIGHT_TO_GRAMS.put(Unit.OUNCES, 28.3495);
        WEIGHT_TO_GRAMS.put(Unit.POUNDS, 453.592);
    }

    // Volume conversions to milliliters
    private static final Map<Unit, Double> VOLUME_TO_ML = new HashMap<>();
    static {
        VOLUME_TO_ML.put(Unit.MILLILITERS, 1.0);
        VOLUME_TO_ML.put(Unit.LITERS, 1000.0);
        VOLUME_TO_ML.put(Unit.CUPS, 236.588);
        VOLUME_TO_ML.put(Unit.TABLESPOONS, 14.7868);
        VOLUME_TO_ML.put(Unit.TEASPOONS, 4.92892);
        VOLUME_TO_ML.put(Unit.FLUID_OUNCES, 29.5735);
    }

    /**
     * Convert a quantity from one unit to another.
     *
     * @param quantity Original quantity
     * @param fromUnit Unit to convert from
     * @param toUnit Unit to convert to
     * @return Converted quantity
     * @throws IllegalArgumentException if units are not compatible
     */
    public static double convert(double quantity, Unit fromUnit, Unit toUnit) {
        if (fromUnit == toUnit) {
            return quantity;
        }

        if (!fromUnit.isConvertibleTo(toUnit)) {
            throw new IllegalArgumentException(
                String.format("Cannot convert from %s to %s - incompatible unit types",
                    fromUnit, toUnit)
            );
        }

        // Weight conversions
        if (WEIGHT_TO_GRAMS.containsKey(fromUnit) && WEIGHT_TO_GRAMS.containsKey(toUnit)) {
            double grams = quantity * WEIGHT_TO_GRAMS.get(fromUnit);
            return grams / WEIGHT_TO_GRAMS.get(toUnit);
        }

        // Volume conversions
        if (VOLUME_TO_ML.containsKey(fromUnit) && VOLUME_TO_ML.containsKey(toUnit)) {
            double ml = quantity * VOLUME_TO_ML.get(fromUnit);
            return ml / VOLUME_TO_ML.get(toUnit);
        }

        // Count conversions (piece to piece)
        if (fromUnit.getUnitType() == Unit.UnitType.COUNT && toUnit.getUnitType() == Unit.UnitType.COUNT) {
            return quantity;
        }

        throw new IllegalArgumentException("Conversion not supported for these units");
    }

    /**
     * Scale an ingredient quantity by a multiplier.
     * Useful for adjusting serving sizes.
     *
     * @param originalQuantity Original quantity
     * @param unit Unit of measurement
     * @param multiplier Scale factor (e.g., 2.0 for double)
     * @return Scaled quantity
     */
    public static double scaleQuantity(double originalQuantity, Unit unit, double multiplier) {
        if (multiplier <= 0) {
            throw new IllegalArgumentException("Multiplier must be positive");
        }

        double scaled = originalQuantity * multiplier;

        // Round based on unit type and magnitude
        if (unit.getUnitType() == Unit.UnitType.COUNT) {
            // Round to nearest whole number for countable items
            return Math.round(scaled);
        } else if (scaled < 1) {
            // Keep 2 decimal places for small quantities
            return NumberUtil.round(scaled, 2);
        } else if (scaled < 10) {
            // Keep 1 decimal place for medium quantities
            return NumberUtil.round(scaled, 1);
        } else {
            // Round to whole number for large quantities
            return Math.round(scaled);
        }
    }

    /**
     * Normalize a unit to the most appropriate size for display.
     * For example, 1000g -> 1kg, 1000ml -> 1L
     *
     * @param quantity Original quantity
     * @param unit Original unit
     * @return Array [normalizedQuantity, normalizedUnit]
     */
    public static Object[] normalizeUnit(double quantity, Unit unit) {
        // Normalize weight
        if (unit == Unit.GRAMS && quantity >= 1000) {
            return new Object[]{quantity / 1000.0, Unit.KILOGRAMS};
        }
        if (unit == Unit.KILOGRAMS && quantity < 1) {
            return new Object[]{quantity * 1000.0, Unit.GRAMS};
        }

        // Normalize volume
        if (unit == Unit.MILLILITERS && quantity >= 1000) {
            return new Object[]{quantity / 1000.0, Unit.LITERS};
        }
        if (unit == Unit.LITERS && quantity < 1) {
            return new Object[]{quantity * 1000.0, Unit.MILLILITERS};
        }

        // Normalize tablespoons to cups (US cooking convention)
        if (unit == Unit.TABLESPOONS && quantity >= 16) {
            return new Object[]{quantity / 16.0, Unit.CUPS};
        }

        // Normalize teaspoons to tablespoons
        if (unit == Unit.TEASPOONS && quantity >= 3) {
            return new Object[]{quantity / 3.0, Unit.TABLESPOONS};
        }

        return new Object[]{quantity, unit};
    }

    /**
     * Format an ingredient quantity with appropriate precision and unit.
     *
     * @param quantity Quantity value
     * @param unit Unit of measurement
     * @return Formatted string (e.g., "250g", "1.5 cups", "3 eggs")
     */
    public static String formatQuantity(double quantity, Unit unit) {
        Object[] normalized = normalizeUnit(quantity, unit);
        double normalizedQuantity = (double) normalized[0];
        Unit normalizedUnit = (Unit) normalized[1];

        String quantityStr;
        if (NumberUtil.isWholeNumber(normalizedQuantity)) {
            quantityStr = String.valueOf((int) normalizedQuantity);
        } else if (normalizedQuantity < 1) {
            quantityStr = NumberUtil.formatAsFraction(normalizedQuantity);
        } else {
            quantityStr = NumberUtil.formatOneDecimal(normalizedQuantity);
        }

        return quantityStr + " " + normalizedUnit.getAbbreviation();
    }

    /**
     * Check if a quantity is reasonable for the given unit.
     * Helps validate user input.
     *
     * @param quantity Quantity to validate
     * @param unit Unit of measurement
     * @return true if quantity seems reasonable
     */
    public static boolean isReasonableQuantity(double quantity, Unit unit) {
        if (quantity <= 0 || quantity > 100000) {
            return false;  // Negative or absurdly large
        }

        // Unit-specific checks
        switch (unit.getUnitType()) {
            case WEIGHT:
                // 0.01g to 10kg seems reasonable for a single ingredient
                double grams = convert(quantity, unit, Unit.GRAMS);
                return grams >= 0.01 && grams <= 10000;

            case VOLUME:
                // 0.1ml to 5L seems reasonable
                double ml = convert(quantity, unit, Unit.MILLILITERS);
                return ml >= 0.1 && ml <= 5000;

            case COUNT:
                // 0.1 to 100 pieces seems reasonable
                return quantity >= 0.1 && quantity <= 100;

            case SPECIAL:
                // PINCH, DASH, TO_TASTE are always reasonable if positive
                return quantity > 0 && quantity <= 10;

            default:
                return true;
        }
    }

    /**
     * Suggest the best unit for a given quantity and current unit.
     * Helps improve readability.
     *
     * @param quantity Current quantity
     * @param currentUnit Current unit
     * @return Suggested unit for better readability
     */
    public static Unit suggestBestUnit(double quantity, Unit currentUnit) {
        Object[] normalized = normalizeUnit(quantity, currentUnit);
        return (Unit) normalized[1];
    }

    /**
     * Parse a quantity string with unit (e.g., "250g", "1.5 cups").
     *
     * @param quantityStr String to parse
     * @return Array [quantity, unit] or null if invalid
     */
    public static Object[] parseQuantityString(String quantityStr) {
        if (quantityStr == null || quantityStr.trim().isEmpty()) {
            return null;
        }

        quantityStr = quantityStr.trim();

        // Try to find where the number ends and unit begins
        int splitIndex = 0;
        for (int i = 0; i < quantityStr.length(); i++) {
            char c = quantityStr.charAt(i);
            if (!Character.isDigit(c) && c != '.' && c != '/' && c != ' ') {
                splitIndex = i;
                break;
            }
        }

        if (splitIndex == 0) {
            return null;  // No valid number found
        }

        String numPart = quantityStr.substring(0, splitIndex).trim();
        String unitPart = quantityStr.substring(splitIndex).trim();

        // Parse quantity (handle fractions)
        double quantity;
        if (numPart.contains("/")) {
            quantity = NumberUtil.fractionToDecimal(numPart);
        } else {
            quantity = NumberUtil.parseDouble(numPart, -1);
        }

        if (quantity < 0) {
            return null;
        }

        // Parse unit
        try {
            Unit unit = Unit.fromString(unitPart);
            return new Object[]{quantity, unit};
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private IngredientConverter() {
        // Utility class - prevent instantiation
    }
}
