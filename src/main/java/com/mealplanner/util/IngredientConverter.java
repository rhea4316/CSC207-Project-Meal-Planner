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
        // TODO: Implement unit conversion
        throw new UnsupportedOperationException("Not yet implemented");
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
        // TODO: Implement quantity scaling
        throw new UnsupportedOperationException("Not yet implemented");
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
        // TODO: Implement unit normalization
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Format an ingredient quantity with appropriate precision and unit.
     *
     * @param quantity Quantity value
     * @param unit Unit of measurement
     * @return Formatted string (e.g., "250g", "1.5 cups", "3 eggs")
     */
    public static String formatQuantity(double quantity, Unit unit) {
        // TODO: Implement quantity formatting
        throw new UnsupportedOperationException("Not yet implemented");
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
        // TODO: Implement quantity validation
        throw new UnsupportedOperationException("Not yet implemented");
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
        // TODO: Implement unit suggestion
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Parse a quantity string with unit (e.g., "250g", "1.5 cups").
     *
     * @param quantityStr String to parse
     * @return Array [quantity, unit] or null if invalid
     */
    public static Object[] parseQuantityString(String quantityStr) {
        // TODO: Implement quantity string parsing
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private IngredientConverter() {
        // Utility class - prevent instantiation
    }
}
