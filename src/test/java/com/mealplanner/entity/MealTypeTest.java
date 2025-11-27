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
        MealType[] values = MealType.values();
        assertEquals(3, values.length);
        assertTrue(containsValue(values, MealType.BREAKFAST));
        assertTrue(containsValue(values, MealType.LUNCH));
        assertTrue(containsValue(values, MealType.DINNER));
    }

    @Test
    public void testFromString() {
        assertEquals(MealType.BREAKFAST, MealType.fromString("breakfast"));
        assertEquals(MealType.BREAKFAST, MealType.fromString("BREAKFAST"));
        assertEquals(MealType.BREAKFAST, MealType.fromString("Breakfast"));
        assertEquals(MealType.LUNCH, MealType.fromString("lunch"));
        assertEquals(MealType.LUNCH, MealType.fromString("LUNCH"));
        assertEquals(MealType.LUNCH, MealType.fromString("Lunch"));
        assertEquals(MealType.DINNER, MealType.fromString("dinner"));
        assertEquals(MealType.DINNER, MealType.fromString("DINNER"));
        assertEquals(MealType.DINNER, MealType.fromString("Dinner"));
        
        assertThrows(IllegalArgumentException.class, () -> {
            MealType.fromString("INVALID");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            MealType.fromString("");
        });
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Breakfast", MealType.BREAKFAST.getDisplayName());
        assertEquals("Lunch", MealType.LUNCH.getDisplayName());
        assertEquals("Dinner", MealType.DINNER.getDisplayName());
        
        assertNotEquals("BREAKFAST", MealType.BREAKFAST.getDisplayName());
        assertNotEquals("LUNCH", MealType.LUNCH.getDisplayName());
        assertNotEquals("DINNER", MealType.DINNER.getDisplayName());
    }

    @Test
    public void testToString() {
        assertEquals("Breakfast", MealType.BREAKFAST.toString());
        assertEquals("Lunch", MealType.LUNCH.toString());
        assertEquals("Dinner", MealType.DINNER.toString());
    }
    
    private boolean containsValue(MealType[] values, MealType target) {
        for (MealType value : values) {
            if (value == target) {
                return true;
            }
        }
        return false;
    }
}
