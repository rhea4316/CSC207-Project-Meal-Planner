package com.mealplanner.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for MealType enum.
 * Tests enum values and string parsing.
 *
 * Responsible: Everyone (P0 improvement)
 */
public class MealTypeTest {

    @Test
    public void testEnumValues() {
        MealType[] mealTypes = MealType.values();
        assertEquals(3, mealTypes.length, "Should have exactly 3 meal types");

        // Verify BREAKFAST, LUNCH, DINNER
        assertNotNull(MealType.BREAKFAST);
        assertNotNull(MealType.LUNCH);
        assertNotNull(MealType.DINNER);
    }

    @Test
    public void testFromString() {
        // Test parsing valid strings (case-insensitive)
        assertEquals(MealType.BREAKFAST, MealType.fromString("breakfast"));
        assertEquals(MealType.LUNCH, MealType.fromString("lunch"));
        assertEquals(MealType.DINNER, MealType.fromString("dinner"));

        // Test parsing "breakfast", "BREAKFAST", "Breakfast"
        assertEquals(MealType.BREAKFAST, MealType.fromString("breakfast"));
        assertEquals(MealType.BREAKFAST, MealType.fromString("BREAKFAST"));
        assertEquals(MealType.BREAKFAST, MealType.fromString("Breakfast"));
        assertEquals(MealType.BREAKFAST, MealType.fromString("BrEaKfAsT"));

        // Test other meal types
        assertEquals(MealType.LUNCH, MealType.fromString("LUNCH"));
        assertEquals(MealType.LUNCH, MealType.fromString("Lunch"));
        assertEquals(MealType.DINNER, MealType.fromString("DINNER"));
        assertEquals(MealType.DINNER, MealType.fromString("Dinner"));

        // Test parsing invalid string throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            MealType.fromString("snack");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            MealType.fromString("brunch");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            MealType.fromString("invalid");
        });
    }

    @Test
    public void testGetDisplayName() {
        // Test display names are properly formatted
        assertEquals("Breakfast", MealType.BREAKFAST.getDisplayName());
        assertEquals("Lunch", MealType.LUNCH.getDisplayName());
        assertEquals("Dinner", MealType.DINNER.getDisplayName());

        // Verify "Breakfast" not "BREAKFAST"
        assertNotEquals("BREAKFAST", MealType.BREAKFAST.getDisplayName());
        assertNotEquals("breakfast", MealType.BREAKFAST.getDisplayName());
        assertNotEquals("LUNCH", MealType.LUNCH.getDisplayName());
        assertNotEquals("DINNER", MealType.DINNER.getDisplayName());
    }

    @Test
    public void testToString() {
        // Test toString returns expected format
        assertEquals("Breakfast", MealType.BREAKFAST.toString());
        assertEquals("Lunch", MealType.LUNCH.toString());
        assertEquals("Dinner", MealType.DINNER.toString());
    }
}
