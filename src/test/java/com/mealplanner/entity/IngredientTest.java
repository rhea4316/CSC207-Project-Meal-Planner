package com.mealplanner.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Unit tests for Ingredient entity.
// Responsible: Everyone (testing is shared responsibility)

public class IngredientTest {

    @Test
    public void testIngredientCreationWithValidData() {
        Ingredient ingredient = new Ingredient("Chicken Breast", 200.0, "g", 330, 62.0, 0.0, 7.0);

        assertEquals("Chicken Breast", ingredient.getName());
        assertEquals(200.0, ingredient.getQuantity());
        assertEquals("g", ingredient.getUnit());
        assertEquals(330, ingredient.getCalories());
        assertEquals(62.0, ingredient.getProtein());
        assertEquals(0.0, ingredient.getCarbs());
        assertEquals(7.0, ingredient.getFat());
    }

    @Test
    public void testIngredientCreationWithEmptyNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Ingredient("", 100.0, "g", 100, 10.0, 10.0, 5.0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Ingredient(null, 100.0, "g", 100, 10.0, 10.0, 5.0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Ingredient("   ", 100.0, "g", 100, 10.0, 10.0, 5.0);
        });
    }

    @Test
    public void testIngredientCreationWithNegativeQuantityThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Ingredient("Rice", -50.0, "g", 100, 2.0, 20.0, 0.5);
        });
    }

    @Test
    public void testIngredientCreationWithNegativeNutritionValuesThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Ingredient("Apple", 100.0, "g", -50, 0.3, 14.0, 0.2);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Ingredient("Apple", 100.0, "g", 50, -0.3, 14.0, 0.2);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Ingredient("Apple", 100.0, "g", 50, 0.3, -14.0, 0.2);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Ingredient("Apple", 100.0, "g", 50, 0.3, 14.0, -0.2);
        });
    }

    @Test
    public void testIngredientCreationWithNullUnit() {
        Ingredient ingredient = new Ingredient("Salt", 5.0, null, 0, 0.0, 0.0, 0.0);
        assertEquals("", ingredient.getUnit());
    }

    @Test
    public void testIngredientCreationWithZeroValues() {
        Ingredient ingredient = new Ingredient("Water", 0.0, "ml", 0, 0.0, 0.0, 0.0);

        assertEquals("Water", ingredient.getName());
        assertEquals(0.0, ingredient.getQuantity());
        assertEquals(0, ingredient.getCalories());
    }

    @Test
    public void testIngredientEquality() {
        Ingredient ingredient1 = new Ingredient("Sugar", 50.0, "g", 200, 0.0, 50.0, 0.0);
        Ingredient ingredient2 = new Ingredient("Sugar", 50.0, "g", 200, 0.0, 50.0, 0.0);
        Ingredient ingredient3 = new Ingredient("Salt", 50.0, "g", 0, 0.0, 0.0, 0.0);

        assertEquals(ingredient1, ingredient2);
        assertNotEquals(ingredient1, ingredient3);
    }

    @Test
    public void testIngredientHashCode() {
        Ingredient ingredient1 = new Ingredient("Sugar", 50.0, "g", 200, 0.0, 50.0, 0.0);
        Ingredient ingredient2 = new Ingredient("Sugar", 50.0, "g", 200, 0.0, 50.0, 0.0);

        assertEquals(ingredient1.hashCode(), ingredient2.hashCode());
    }

    @Test
    public void testIngredientToString() {
        Ingredient ingredient = new Ingredient("Flour", 500.0, "g", 1820, 50.0, 380.0, 5.0);
        String result = ingredient.toString();

        assertNotNull(result);
        assertTrue(result.contains("Flour"));
        assertTrue(result.contains("500.00"));
        assertTrue(result.contains("g"));
        assertTrue(result.contains("1820"));
    }

    @Test
    public void testIngredientNameIsTrimmed() {
        Ingredient ingredient = new Ingredient("  Olive Oil  ", 15.0, "ml", 120, 0.0, 0.0, 14.0);
        assertEquals("Olive Oil", ingredient.getName());
    }
}
