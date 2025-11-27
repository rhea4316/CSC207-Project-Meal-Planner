package com.mealplanner.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Unit tests for Recipe entity business logic.
// Responsible: Everyone (testing is shared responsibility)

public class RecipeTest {

    private Recipe recipe;
    private List<String> ingredients;
    private NutritionInfo nutritionInfo;

    @BeforeEach
    public void setUp() {
        ingredients = Arrays.asList("200g Chicken Breast", "100g Brown Rice", "50g Broccoli");
        nutritionInfo = new NutritionInfo(450, 45.0, 40.0, 10.0);
        recipe = new Recipe("Chicken and Rice", ingredients, "1. Cook rice\n2. Grill chicken\n3. Steam broccoli",
                2, nutritionInfo, 30, null, "recipe_123");
    }

    @Test
    public void testRecipeCreationWithAllFields() {
        List<DietaryRestriction> restrictions = Arrays.asList(DietaryRestriction.GLUTEN_FREE);
        Recipe fullRecipe = new Recipe("Pasta", Arrays.asList("pasta", "sauce"), "Cook pasta",
                4, nutritionInfo, 20, restrictions, "recipe_456");

        assertEquals("Pasta", fullRecipe.getName());
        assertEquals(4, fullRecipe.getServingSize());
        assertEquals(20, fullRecipe.getCookTimeMinutes());
        assertEquals("recipe_456", fullRecipe.getRecipeId());
        assertEquals(1, fullRecipe.getDietaryRestrictions().size());
    }

    @Test
    public void testRecipeCreationWithRequiredFieldsOnly() {
        Recipe simpleRecipe = new Recipe("Simple Salad", Arrays.asList("lettuce", "tomato"),
                "Mix ingredients", 1);

        assertEquals("Simple Salad", simpleRecipe.getName());
        assertEquals(1, simpleRecipe.getServingSize());
        assertNull(simpleRecipe.getNutritionInfo());
        assertNull(simpleRecipe.getCookTimeMinutes());
        assertNull(simpleRecipe.getRecipeId());
    }

    @Test
    public void testRecipeCreationWithInvalidNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Recipe("", ingredients, "Cook", 2);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Recipe(null, ingredients, "Cook", 2);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Recipe("   ", ingredients, "Cook", 2);
        });
    }

    @Test
    public void testRecipeCreationWithEmptyIngredientsThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Recipe("Test", Arrays.asList(), "Cook", 2);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Recipe("Test", null, "Cook", 2);
        });
    }

    @Test
    public void testRecipeCreationWithInvalidServingSizeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Recipe("Test", ingredients, "Cook", 0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Recipe("Test", ingredients, "Cook", -1);
        });
    }

    @Test
    public void testAdjustServingSize() {
        Recipe adjusted = recipe.adjustServingSize(4);

        assertEquals(4, adjusted.getServingSize());
        assertEquals("Chicken and Rice", adjusted.getName());
        assertEquals(recipe.getRecipeId(), adjusted.getRecipeId());

        // Nutrition should be scaled by factor of 2 (from 2 to 4 servings)
        NutritionInfo adjustedNutrition = adjusted.getNutritionInfo();
        assertEquals(900, adjustedNutrition.getCalories());
        assertEquals(90.0, adjustedNutrition.getProtein());
        assertEquals(80.0, adjustedNutrition.getCarbs());
        assertEquals(20.0, adjustedNutrition.getFat());

        // Original recipe should remain unchanged
        assertEquals(2, recipe.getServingSize());
        assertEquals(450, recipe.getNutritionInfo().getCalories());
    }

    @Test
    public void testAdjustServingSizeWithInvalidSizeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            recipe.adjustServingSize(0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            recipe.adjustServingSize(-1);
        });
    }

    @Test
    public void testAdjustServingSizeWithNullNutrition() {
        Recipe noNutrition = new Recipe("Simple", ingredients, "Cook", 2);
        Recipe adjusted = noNutrition.adjustServingSize(4);

        assertEquals(4, adjusted.getServingSize());
        assertNull(adjusted.getNutritionInfo());
    }

    @Test
    public void testGetters() {
        assertEquals("Chicken and Rice", recipe.getName());
        assertEquals(3, recipe.getIngredients().size());
        assertEquals("1. Cook rice\n2. Grill chicken\n3. Steam broccoli", recipe.getSteps());
        assertEquals(2, recipe.getServingSize());
        assertEquals(nutritionInfo, recipe.getNutritionInfo());
        assertEquals(30, recipe.getCookTimeMinutes());
        assertEquals(30, recipe.getTotalTimeMinutes());
        assertEquals("recipe_123", recipe.getRecipeId());
    }

    @Test
    public void testIngredientsListIsDefensiveCopy() {
        List<String> originalIngredients = recipe.getIngredients();
        originalIngredients.add("New Ingredient");

        // Original recipe should not be affected
        assertEquals(3, recipe.getIngredients().size());
    }

    @Test
    public void testDietaryRestrictionsListIsDefensiveCopy() {
        List<DietaryRestriction> restrictions = Arrays.asList(DietaryRestriction.VEGAN);
        Recipe veganRecipe = new Recipe("Tofu", ingredients, "Cook", 2, nutritionInfo, 20, restrictions, null);

        List<DietaryRestriction> retrieved = veganRecipe.getDietaryRestrictions();
        retrieved.add(DietaryRestriction.GLUTEN_FREE);

        // Original recipe should not be affected
        assertEquals(1, veganRecipe.getDietaryRestrictions().size());
    }

    @Test
    public void testEquals() {
        Recipe recipe1 = new Recipe("Pasta", ingredients, "Cook pasta", 2, nutritionInfo, 20, null, "id1");
        Recipe recipe2 = new Recipe("Pasta", ingredients, "Cook pasta", 2, nutritionInfo, 20, null, "id1");
        Recipe recipe3 = new Recipe("Pizza", ingredients, "Cook pizza", 2, nutritionInfo, 20, null, "id2");

        assertEquals(recipe1, recipe2);
        assertNotEquals(recipe1, recipe3);
    }

    @Test
    public void testHashCode() {
        Recipe recipe1 = new Recipe("Pasta", ingredients, "Cook pasta", 2, nutritionInfo, 20, null, "id1");
        Recipe recipe2 = new Recipe("Pasta", ingredients, "Cook pasta", 2, nutritionInfo, 20, null, "id1");

        assertEquals(recipe1.hashCode(), recipe2.hashCode());
    }

    @Test
    public void testToString() {
        String result = recipe.toString();

        assertNotNull(result);
        assertTrue(result.contains("Chicken and Rice"));
        assertTrue(result.contains("2"));
    }

    @Test
    public void testRecipeNameIsTrimmed() {
        Recipe trimmedRecipe = new Recipe("  Pasta Carbonara  ", ingredients, "Cook", 2);
        assertEquals("Pasta Carbonara", trimmedRecipe.getName());
    }
}
