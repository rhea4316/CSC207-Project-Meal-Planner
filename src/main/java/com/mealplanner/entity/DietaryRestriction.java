package com.mealplanner.entity;

/**
 * Enum representing common dietary restrictions and preferences.
 * Can be used for filtering recipes and validating meal plans.
 * Responsible: Everyone (optional feature for future enhancements)
 */
public enum DietaryRestriction {

    VEGETARIAN("Vegetarian", "No meat or fish"),
    VEGAN("Vegan", "No animal products"),
    GLUTEN_FREE("Gluten-Free", "No gluten-containing grains"),
    DAIRY_FREE("Dairy-Free", "No dairy products"),
    NUT_FREE("Nut-Free", "No nuts or nut products"),
    EGG_FREE("Egg-Free", "No eggs"),
    KOSHER("Kosher", "Follows Jewish dietary laws"),
    HALAL("Halal", "Follows Islamic dietary laws"),
    PALEO("Paleo", "Follows Paleolithic diet principles"),
    KETO("Keto", "Low-carb, high-fat diet"),
    LOW_SODIUM("Low Sodium", "Reduced sodium content"),
    LOW_SUGAR("Low Sugar", "Reduced sugar content");

    private final String displayName;
    private final String description;

    DietaryRestriction(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Converts a string to a DietaryRestriction enum value (case-insensitive).
     *
     * @param value the string to convert
     * @return the corresponding DietaryRestriction
     * @throws IllegalArgumentException if the value doesn't match any restriction
     */
    public static DietaryRestriction fromString(String value) {
        String normalized = value.toLowerCase().trim().replace(" ", "_");

        for (DietaryRestriction restriction : DietaryRestriction.values()) {
            if (restriction.name().toLowerCase().equals(normalized) ||
                restriction.displayName.toLowerCase().replace(" ", "_").equals(normalized)) {
                return restriction;
            }
        }
        throw new IllegalArgumentException("Invalid dietary restriction: " + value);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
