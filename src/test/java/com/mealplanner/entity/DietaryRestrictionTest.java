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
        DietaryRestriction[] values = DietaryRestriction.values();
        
        assertEquals(12, values.length);
        assertTrue(containsValue(values, DietaryRestriction.VEGETARIAN));
        assertTrue(containsValue(values, DietaryRestriction.VEGAN));
        assertTrue(containsValue(values, DietaryRestriction.GLUTEN_FREE));
        assertTrue(containsValue(values, DietaryRestriction.DAIRY_FREE));
        assertTrue(containsValue(values, DietaryRestriction.NUT_FREE));
        assertTrue(containsValue(values, DietaryRestriction.EGG_FREE));
        assertTrue(containsValue(values, DietaryRestriction.KOSHER));
        assertTrue(containsValue(values, DietaryRestriction.HALAL));
        assertTrue(containsValue(values, DietaryRestriction.PALEO));
        assertTrue(containsValue(values, DietaryRestriction.KETO));
        assertTrue(containsValue(values, DietaryRestriction.LOW_SODIUM));
        assertTrue(containsValue(values, DietaryRestriction.LOW_SUGAR));
    }

    @Test
    public void testFromString() {
        assertEquals(DietaryRestriction.VEGETARIAN, DietaryRestriction.fromString("VEGETARIAN"));
        assertEquals(DietaryRestriction.VEGAN, DietaryRestriction.fromString("vegan"));
        assertEquals(DietaryRestriction.GLUTEN_FREE, DietaryRestriction.fromString("GLUTEN_FREE"));
        assertEquals(DietaryRestriction.GLUTEN_FREE, DietaryRestriction.fromString("gluten free"));
        assertEquals(DietaryRestriction.LOW_SODIUM, DietaryRestriction.fromString("low sodium"));
        assertEquals(DietaryRestriction.LOW_SODIUM, DietaryRestriction.fromString("LOW_SODIUM"));
        assertEquals(DietaryRestriction.VEGETARIAN, DietaryRestriction.fromString("Vegetarian"));
        assertEquals(DietaryRestriction.GLUTEN_FREE, DietaryRestriction.fromString("Gluten-Free"));
        
        assertThrows(IllegalArgumentException.class, () -> {
            DietaryRestriction.fromString("INVALID_RESTRICTION");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            DietaryRestriction.fromString("");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            DietaryRestriction.fromString("   ");
        });
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Vegetarian", DietaryRestriction.VEGETARIAN.getDisplayName());
        assertEquals("Vegan", DietaryRestriction.VEGAN.getDisplayName());
        assertEquals("Gluten-Free", DietaryRestriction.GLUTEN_FREE.getDisplayName());
        assertEquals("Dairy-Free", DietaryRestriction.DAIRY_FREE.getDisplayName());
        assertEquals("Low Sodium", DietaryRestriction.LOW_SODIUM.getDisplayName());
        assertEquals("Low Sugar", DietaryRestriction.LOW_SUGAR.getDisplayName());
        
        assertNotEquals("GLUTEN_FREE", DietaryRestriction.GLUTEN_FREE.getDisplayName());
        assertNotEquals("VEGETARIAN", DietaryRestriction.VEGETARIAN.getDisplayName());
    }

    @Test
    public void testGetDescription() {
        assertNotNull(DietaryRestriction.VEGETARIAN.getDescription());
        assertNotNull(DietaryRestriction.VEGAN.getDescription());
        assertNotNull(DietaryRestriction.GLUTEN_FREE.getDescription());
        assertNotNull(DietaryRestriction.DAIRY_FREE.getDescription());
        
        assertFalse(DietaryRestriction.VEGETARIAN.getDescription().isEmpty());
        assertFalse(DietaryRestriction.VEGAN.getDescription().isEmpty());
        assertFalse(DietaryRestriction.GLUTEN_FREE.getDescription().isEmpty());
        
        assertEquals("No meat or fish", DietaryRestriction.VEGETARIAN.getDescription());
        assertEquals("No animal products", DietaryRestriction.VEGAN.getDescription());
        assertEquals("No gluten-containing grains", DietaryRestriction.GLUTEN_FREE.getDescription());
        assertEquals("Low-carb, high-fat diet", DietaryRestriction.KETO.getDescription());
    }
    
    private boolean containsValue(DietaryRestriction[] values, DietaryRestriction target) {
        for (DietaryRestriction value : values) {
            if (value == target) {
                return true;
            }
        }
        return false;
    }
}
