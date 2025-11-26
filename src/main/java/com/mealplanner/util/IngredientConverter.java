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
        if (fromUnit == null || toUnit == null) {
            throw new IllegalArgumentException("Units cannot be null");
        }
        
        if (fromUnit == toUnit) {
            return quantity;
        }
        
        if (!fromUnit.isConvertibleTo(toUnit)) {
            throw new IllegalArgumentException("Cannot convert between incompatible units");
        }
        
        Unit.UnitType type = fromUnit.getType();
        
        if (type == Unit.UnitType.WEIGHT) {
            Double fromGrams = WEIGHT_TO_GRAMS.get(fromUnit);
            Double toGrams = WEIGHT_TO_GRAMS.get(toUnit);
            if (fromGrams == null || toGrams == null) {
                throw new IllegalArgumentException("Unit not supported for weight conversion");
            }
            double grams = quantity * fromGrams;
            return grams / toGrams;
        } else if (type == Unit.UnitType.VOLUME) {
            Double fromMl = VOLUME_TO_ML.get(fromUnit);
            Double toMl = VOLUME_TO_ML.get(toUnit);
            if (fromMl == null || toMl == null) {
                throw new IllegalArgumentException("Unit not supported for volume conversion");
            }
            double ml = quantity * fromMl;
            return ml / toMl;
        } else {
            return quantity;
        }
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
        if (multiplier < 0) {
            throw new IllegalArgumentException("Multiplier cannot be negative");
        }
        return originalQuantity * multiplier;
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
        if (unit == null) {
            throw new IllegalArgumentException("Unit cannot be null");
        }
        
        Unit.UnitType type = unit.getType();
        
        if (type == Unit.UnitType.WEIGHT) {
            Double gramsPerUnit = WEIGHT_TO_GRAMS.get(unit);
            if (gramsPerUnit == null) {
                return new Object[]{quantity, unit};
            }
            double grams = quantity * gramsPerUnit;
            if (grams >= 1000) {
                return new Object[]{grams / 1000.0, Unit.KILOGRAMS};
            } else {
                return new Object[]{grams, Unit.GRAMS};
            }
        } else if (type == Unit.UnitType.VOLUME) {
            Double mlPerUnit = VOLUME_TO_ML.get(unit);
            if (mlPerUnit == null) {
                return new Object[]{quantity, unit};
            }
            double ml = quantity * mlPerUnit;
            if (ml >= 1000) {
                return new Object[]{ml / 1000.0, Unit.LITERS};
            } else {
                return new Object[]{ml, Unit.MILLILITERS};
            }
        } else {
            return new Object[]{quantity, unit};
        }
    }

    /**
     * Format an ingredient quantity with appropriate precision and unit.
     *
     * @param quantity Quantity value
     * @param unit Unit of measurement
     * @return Formatted string (e.g., "250g", "1.5 cups", "3 eggs")
     */
    public static String formatQuantity(double quantity, Unit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("Unit cannot be null");
        }
        
        if (quantity == Math.floor(quantity)) {
            return String.format("%d %s", (int) quantity, unit.getAbbreviation());
        } else {
            return String.format("%.2f %s", quantity, unit.getAbbreviation());
        }
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
        if (quantity <= 0) {
            return false;
        }
        
        if (unit == null) {
            return false;
        }
        
        Unit.UnitType type = unit.getType();
        double maxValue = 10000.0;
        
        if (type == Unit.UnitType.WEIGHT) {
            double grams = quantity * WEIGHT_TO_GRAMS.getOrDefault(unit, 1.0);
            return grams <= maxValue;
        } else if (type == Unit.UnitType.VOLUME) {
            double ml = quantity * VOLUME_TO_ML.getOrDefault(unit, 1.0);
            return ml <= maxValue;
        } else {
            return quantity <= maxValue;
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
        if (currentUnit == null) {
            throw new IllegalArgumentException("Unit cannot be null");
        }
        
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
        
        String trimmed = quantityStr.trim();
        
        int lastDigitIndex = -1;
        for (int i = 0; i < trimmed.length(); i++) {
            if (Character.isDigit(trimmed.charAt(i)) || trimmed.charAt(i) == '.' || trimmed.charAt(i) == '-') {
                lastDigitIndex = i;
            }
        }
        
        if (lastDigitIndex < 0) {
            return null;
        }
        
        String numberStr = trimmed.substring(0, lastDigitIndex + 1).trim();
        String unitStr = trimmed.substring(lastDigitIndex + 1).trim();
        
        if (unitStr.isEmpty()) {
            return null;
        }
        
        try {
            double quantity = Double.parseDouble(numberStr);
            Unit unit = Unit.fromString(unitStr);
            return new Object[]{quantity, unit};
        } catch (Exception e) {
            return null;
        }
    }

    private IngredientConverter() {
        // Utility class - prevent instantiation
    }
}
