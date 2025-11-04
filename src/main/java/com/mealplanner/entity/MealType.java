package com.mealplanner.entity;

/**
 * Enum representing the type of meal (breakfast, lunch, or dinner).
 * Use this instead of magic strings to prevent typos and provide type safety.
 * Responsible: Everyone (especially Grace for meal plan management, Mona for schedule viewing)
 */
public enum MealType {

    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner");

    private final String displayName;

    MealType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Converts a string to a MealType enum value (case-insensitive).
     *
     * @param value the string to convert
     * @return the corresponding MealType
     * @throws IllegalArgumentException if the value doesn't match any meal type
     */
    public static MealType fromString(String value) {
        for (MealType type : MealType.values()) {
            if (type.name().equalsIgnoreCase(value) ||
                type.displayName.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid meal type: " + value);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
