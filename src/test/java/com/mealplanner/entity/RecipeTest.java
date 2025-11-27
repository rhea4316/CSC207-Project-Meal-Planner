package com.mealplanner.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

// Unit tests for Recipe entity business logic.
// Responsible: Everyone (testing is shared responsibility)

public class RecipeTest {

    private Recipe recipe;
    private NutritionInfo nutritionInfo;

    @BeforeEach
    public void setUp() {
        nutritionInfo = new NutritionInfo(500, 20.0, 60.0, 15.0);
        recipe = new Recipe(
            "Test Recipe",
            Arrays.asList("ingredient1", "ingredient2"),
            "Step 1\nStep 2",
            4,
            nutritionInfo,
            30,
            null,
            "recipe-1"
        );
    }

    @Test
    public void testRecipeCreation() {
        assertEquals("Test Recipe", recipe.getName());
        assertEquals(2, recipe.getIngredients().size());
        assertEquals("Step 1\nStep 2", recipe.getSteps());
        assertEquals(4, recipe.getServingSize());
        assertEquals(nutritionInfo, recipe.getNutritionInfo());
        assertEquals(30, recipe.getCookTimeMinutes().intValue());
        assertEquals("recipe-1", recipe.getRecipeId());
    }

    @Test
    public void testRecipeCreationWithMinimalFields() {
        Recipe minimalRecipe = new Recipe(
            "Minimal Recipe",
            Arrays.asList("ingredient"),
            "Step 1",
            2
        );
        
        assertEquals("Minimal Recipe", minimalRecipe.getName());
        assertEquals(1, minimalRecipe.getIngredients().size());
        assertEquals("Step 1", minimalRecipe.getSteps());
        assertEquals(2, minimalRecipe.getServingSize());
        assertNull(minimalRecipe.getNutritionInfo());
        assertNull(minimalRecipe.getCookTimeMinutes());
        assertNull(minimalRecipe.getRecipeId());
    }

    @Test
    public void testRecipeCreationWithInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Recipe("", Arrays.asList("ingredient"), "steps", 2);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Recipe("   ", Arrays.asList("ingredient"), "steps", 2);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Recipe(null, Arrays.asList("ingredient"), "steps", 2);
        });
    }

    @Test
    public void testRecipeCreationWithEmptyIngredients() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Recipe("Recipe", null, "steps", 2);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Recipe("Recipe", Arrays.asList(), "steps", 2);
        });
    }

    @Test
    public void testRecipeCreationWithEmptySteps() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Recipe("Recipe", Arrays.asList("ingredient"), "", 2);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Recipe("Recipe", Arrays.asList("ingredient"), null, 2);
        });
    }

    @Test
    public void testRecipeCreationWithInvalidServingSize() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Recipe("Recipe", Arrays.asList("ingredient"), "steps", 0);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Recipe("Recipe", Arrays.asList("ingredient"), "steps", -1);
        });
    }

    @Test
    public void testAdjustServingSize() {
        Recipe adjusted = recipe.adjustServingSize(8);
        
        assertEquals(8, adjusted.getServingSize());
        assertEquals("Test Recipe", adjusted.getName());
        assertEquals(recipe.getSteps(), adjusted.getSteps());
        
        NutritionInfo adjustedNutrition = adjusted.getNutritionInfo();
        assertNotNull(adjustedNutrition);
        assertEquals(1000, adjustedNutrition.getCalories());
        assertEquals(40.0, adjustedNutrition.getProtein(), 0.01);
        assertEquals(120.0, adjustedNutrition.getCarbs(), 0.01);
        assertEquals(30.0, adjustedNutrition.getFat(), 0.01);
    }

    @Test
    public void testAdjustServingSizeWithFractional() {
        Recipe adjusted = recipe.adjustServingSize(2);
        
        assertEquals(2, adjusted.getServingSize());
        NutritionInfo adjustedNutrition = adjusted.getNutritionInfo();
        assertEquals(250, adjustedNutrition.getCalories());
        assertEquals(10.0, adjustedNutrition.getProtein(), 0.01);
        assertEquals(30.0, adjustedNutrition.getCarbs(), 0.01);
        assertEquals(7.5, adjustedNutrition.getFat(), 0.01);
    }

    @Test
    public void testAdjustServingSizeWithInvalidSize() {
        assertThrows(IllegalArgumentException.class, () -> {
            recipe.adjustServingSize(0);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            recipe.adjustServingSize(-1);
        });
    }

    @Test
    public void testAdjustServingSizeWithoutNutrition() {
        Recipe recipeNoNutrition = new Recipe(
            "No Nutrition Recipe",
            Arrays.asList("ingredient"),
            "steps",
            2
        );
        
        Recipe adjusted = recipeNoNutrition.adjustServingSize(4);
        assertEquals(4, adjusted.getServingSize());
        assertNull(adjusted.getNutritionInfo());
    }

    @Test
    public void testGetters() {
        List<String> ingredients = recipe.getIngredients();
        assertEquals(2, ingredients.size());
        assertTrue(ingredients.contains("ingredient1"));
        assertTrue(ingredients.contains("ingredient2"));
        
        List<DietaryRestriction> restrictions = recipe.getDietaryRestrictions();
        assertNotNull(restrictions);
        assertTrue(restrictions.isEmpty());
    }

    @Test
    public void testEqualsAndHashCode() {
        Recipe recipe1 = new Recipe(
            "Test Recipe",
            Arrays.asList("ingredient1", "ingredient2"),
            "Step 1\nStep 2",
            4,
            nutritionInfo,
            30,
            null,
            "recipe-1"
        );
        
        Recipe recipe2 = new Recipe(
            "Test Recipe",
            Arrays.asList("ingredient1", "ingredient2"),
            "Step 1\nStep 2",
            4,
            nutritionInfo,
            30,
            null,
            "recipe-1"
        );
        
        Recipe recipe3 = new Recipe(
            "Different Recipe",
            Arrays.asList("ingredient"),
            "steps",
            2,
            null,
            null,
            null,
            "recipe-2"
        );
        
        assertEquals(recipe1, recipe2);
        assertNotEquals(recipe1, recipe3);
        assertEquals(recipe1.hashCode(), recipe2.hashCode());
    }

    @Test
    public void testToString() {
        String result = recipe.toString();
        assertTrue(result.contains("Test Recipe"));
        assertTrue(result.contains("Serves 4"));
        assertTrue(result.contains("2 ingredients"));
        assertTrue(result.contains("Cook: 30 min"));
    }
}
