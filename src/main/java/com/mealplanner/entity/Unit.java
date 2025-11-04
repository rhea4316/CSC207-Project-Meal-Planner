package com.mealplanner.entity;

/**
 * Enum representing measurement units for ingredients.
 * Provides type safety and standardization for ingredient quantities.
 * Responsible: Everyone (especially Aaryan for recipe storage, Eden for serving size adjustments)
 */
public enum Unit {

    // Weight units
    GRAMS("g", "gram", UnitType.WEIGHT),
    KILOGRAMS("kg", "kilogram", UnitType.WEIGHT),
    OUNCES("oz", "ounce", UnitType.WEIGHT),
    POUNDS("lb", "pound", UnitType.WEIGHT),

    // Volume units
    MILLILITERS("ml", "milliliter", UnitType.VOLUME),
    LITERS("L", "liter", UnitType.VOLUME),
    CUPS("cup", "cup", UnitType.VOLUME),
    TABLESPOONS("tbsp", "tablespoon", UnitType.VOLUME),
    TEASPOONS("tsp", "teaspoon", UnitType.VOLUME),
    FLUID_OUNCES("fl oz", "fluid ounce", UnitType.VOLUME),

    // Count units
    PIECES("piece", "piece", UnitType.COUNT),
    ITEMS("item", "item", UnitType.COUNT),
    WHOLE("whole", "whole", UnitType.COUNT),

    // Special units
    PINCH("pinch", "pinch", UnitType.SPECIAL),
    DASH("dash", "dash", UnitType.SPECIAL),
    TO_TASTE("to taste", "to taste", UnitType.SPECIAL);

    private final String abbreviation;
    private final String fullName;
    private final UnitType type;

    Unit(String abbreviation, String fullName, UnitType type) {
        this.abbreviation = abbreviation;
        this.fullName = fullName;
        this.type = type;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public String getFullName() {
        return fullName;
    }

    public UnitType getType() {
        return type;
    }

    /**
     * Converts a string to a Unit enum value (case-insensitive).
     *
     * @param value the string to convert
     * @return the corresponding Unit
     * @throws IllegalArgumentException if the value doesn't match any unit
     */
    public static Unit fromString(String value) {
        String normalized = value.toLowerCase().trim();

        for (Unit unit : Unit.values()) {
            if (unit.abbreviation.toLowerCase().equals(normalized) ||
                unit.fullName.toLowerCase().equals(normalized) ||
                unit.name().toLowerCase().equals(normalized)) {
                return unit;
            }
        }
        throw new IllegalArgumentException("Invalid unit: " + value);
    }

    /**
     * Checks if this unit can be converted to another unit (same type).
     *
     * @param other the unit to check conversion compatibility with
     * @return true if units are of the same type and can be converted
     */
    public boolean isConvertibleTo(Unit other) {
        return this.type == other.type && this.type != UnitType.SPECIAL;
    }

    @Override
    public String toString() {
        return abbreviation;
    }

    /**
     * Enum representing the type category of a unit.
     */
    public enum UnitType {
        WEIGHT,
        VOLUME,
        COUNT,
        SPECIAL
    }
}
