package com.mealplanner.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DietaryRestriction enum.
 * Tests dietary restriction values and display.
 *
 * Responsible: Everyone (P0 improvement)
 */
public class DietaryRestrictionTest {

    @Test
    public void testEnumValues() {
        DietaryRestriction[] restrictions = DietaryRestriction.values();
        assertTrue(restrictions.length >= 12, "Should have at least 12 dietary restrictions");

        // Verify expected dietary restrictions exist
        assertNotNull(DietaryRestriction.VEGETARIAN);
        assertNotNull(DietaryRestriction.VEGAN);
        assertNotNull(DietaryRestriction.GLUTEN_FREE);
        assertNotNull(DietaryRestriction.DAIRY_FREE);
        assertNotNull(DietaryRestriction.NUT_FREE);
        assertNotNull(DietaryRestriction.EGG_FREE);
        assertNotNull(DietaryRestriction.KOSHER);
        assertNotNull(DietaryRestriction.HALAL);
        assertNotNull(DietaryRestriction.PALEO);
        assertNotNull(DietaryRestriction.KETO);
        assertNotNull(DietaryRestriction.LOW_SODIUM);
        assertNotNull(DietaryRestriction.LOW_SUGAR);
    }

    @Test
    public void testFromString() {
        // Test parsing valid strings
        assertEquals(DietaryRestriction.VEGETARIAN, DietaryRestriction.fromString("vegetarian"));
        assertEquals(DietaryRestriction.VEGAN, DietaryRestriction.fromString("vegan"));
        assertEquals(DietaryRestriction.GLUTEN_FREE, DietaryRestriction.fromString("gluten_free"));

        // Test case-insensitive parsing
        assertEquals(DietaryRestriction.VEGETARIAN, DietaryRestriction.fromString("VEGETARIAN"));
        assertEquals(DietaryRestriction.VEGAN, DietaryRestriction.fromString("VeGaN"));

        // Test parsing with underscores and spaces
        assertEquals(DietaryRestriction.GLUTEN_FREE, DietaryRestriction.fromString("gluten free"));
        assertEquals(DietaryRestriction.LOW_SODIUM, DietaryRestriction.fromString("low_sodium"));
        assertEquals(DietaryRestriction.LOW_SODIUM, DietaryRestriction.fromString("low sodium"));

        // Test invalid string throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            DietaryRestriction.fromString("invalid_restriction");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            DietaryRestriction.fromString("not_a_real_diet");
        });
    }

    @Test
    public void testGetDisplayName() {
        // Test display names are human-readable
        assertEquals("Vegetarian", DietaryRestriction.VEGETARIAN.getDisplayName());
        assertEquals("Vegan", DietaryRestriction.VEGAN.getDisplayName());

        // Verify "Gluten-Free" not "GLUTEN_FREE"
        assertEquals("Gluten-Free", DietaryRestriction.GLUTEN_FREE.getDisplayName());
        assertNotEquals("GLUTEN_FREE", DietaryRestriction.GLUTEN_FREE.getDisplayName());

        assertEquals("Dairy-Free", DietaryRestriction.DAIRY_FREE.getDisplayName());
        assertEquals("Low Sodium", DietaryRestriction.LOW_SODIUM.getDisplayName());
    }

    @Test
    public void testGetDescription() {
        // Test descriptions are present
        assertNotNull(DietaryRestriction.VEGETARIAN.getDescription());
        assertNotNull(DietaryRestriction.VEGAN.getDescription());
        assertNotNull(DietaryRestriction.GLUTEN_FREE.getDescription());

        // Verify descriptions are helpful for users
        assertFalse(DietaryRestriction.VEGETARIAN.getDescription().isEmpty());
        assertTrue(DietaryRestriction.VEGETARIAN.getDescription().length() > 5);

        // Test specific descriptions
        assertTrue(DietaryRestriction.VEGAN.getDescription().toLowerCase().contains("animal"));
        assertTrue(DietaryRestriction.GLUTEN_FREE.getDescription().toLowerCase().contains("gluten"));
    }
}
