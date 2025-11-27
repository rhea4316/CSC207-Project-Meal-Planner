package com.mealplanner.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Unit tests for Ingredient entity.
// Responsible: Everyone (testing is shared responsibility)

public class IngredientTest {

    @Test
    public void testIngredientCreation() {
        Ingredient ingredient = new Ingredient("Tomato", 100.0, "g", 18, 0.9, 3.9, 0.2);
        
        assertEquals("Tomato", ingredient.getName());
        assertEquals(100.0, ingredient.getQuantity());
        assertEquals("g", ingredient.getUnit());
        assertEquals(18, ingredient.getCalories());
        assertEquals(0.9, ingredient.getProtein());
        assertEquals(3.9, ingredient.getCarbs());
        assertEquals(0.2, ingredient.getFat());
    }

    @Test
    public void testIngredientCreationWithNullUnit() {
        Ingredient ingredient = new Ingredient("Salt", 1.0, null, 0, 0.0, 0.0, 0.0);
        assertEquals("", ingredient.getUnit());
    }

    @Test
    public void testIngredientCreationWithEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Ingredient("", 100.0, "g", 18, 0.9, 3.9, 0.2);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Ingredient("   ", 100.0, "g", 18, 0.9, 3.9, 0.2);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Ingredient(null, 100.0, "g", 18, 0.9, 3.9, 0.2);
        });
    }

    @Test
    public void testIngredientCreationWithNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Ingredient("Tomato", -1.0, "g", 18, 0.9, 3.9, 0.2);
        });
    }

    @Test
    public void testIngredientCreationWithNegativeNutrition() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Ingredient("Tomato", 100.0, "g", -1, 0.9, 3.9, 0.2);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Ingredient("Tomato", 100.0, "g", 18, -1.0, 3.9, 0.2);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Ingredient("Tomato", 100.0, "g", 18, 0.9, -1.0, 0.2);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Ingredient("Tomato", 100.0, "g", 18, 0.9, 3.9, -1.0);
        });
    }

    @Test
    public void testGetters() {
        Ingredient ingredient = new Ingredient("Chicken", 200.0, "g", 231, 43.5, 0.0, 5.0);
        
        assertEquals("Chicken", ingredient.getName());
        assertEquals(200.0, ingredient.getQuantity());
        assertEquals("g", ingredient.getUnit());
        assertEquals(231, ingredient.getCalories());
        assertEquals(43.5, ingredient.getProtein());
        assertEquals(0.0, ingredient.getCarbs());
        assertEquals(5.0, ingredient.getFat());
    }

    @Test
    public void testEqualsAndHashCode() {
        Ingredient ingredient1 = new Ingredient("Tomato", 100.0, "g", 18, 0.9, 3.9, 0.2);
        Ingredient ingredient2 = new Ingredient("Tomato", 100.0, "g", 18, 0.9, 3.9, 0.2);
        Ingredient ingredient3 = new Ingredient("Chicken", 200.0, "g", 231, 43.5, 0.0, 5.0);
        
        assertEquals(ingredient1, ingredient2);
        assertNotEquals(ingredient1, ingredient3);
        assertEquals(ingredient1.hashCode(), ingredient2.hashCode());
    }

    @Test
    public void testToString() {
        Ingredient ingredient = new Ingredient("Tomato", 100.0, "g", 18, 0.9, 3.9, 0.2);
        String result = ingredient.toString();
        
        assertTrue(result.contains("Tomato"));
        assertTrue(result.contains("100.00"));
        assertTrue(result.contains("g"));
        assertTrue(result.contains("18"));
    }
}
