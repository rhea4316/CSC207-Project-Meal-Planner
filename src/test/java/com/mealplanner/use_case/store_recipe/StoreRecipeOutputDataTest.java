package com.mealplanner.use_case.store_recipe;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;

import com.mealplanner.entity.DietaryRestriction;
import com.mealplanner.entity.NutritionInfo;
import com.mealplanner.entity.Recipe;

/**
 * Test class for StoreRecipeOutputData.
 */
public class StoreRecipeOutputDataTest {

    @Test
    public void testConstructorWithValidRecipe() {
        // Arrange
        List<String> ingredients = Arrays.asList("Ingredient 1", "Ingredient 2");
        String steps = "Step 1\nStep 2";
        NutritionInfo nutritionInfo = new NutritionInfo(200, 10, 20, 15);
        List<DietaryRestriction> restrictions = Collections.emptyList();
        
        Recipe recipe = new Recipe(
                "Test Recipe",
                ingredients,
                steps,
                4,
                nutritionInfo,
                30,
                restrictions,
                "recipe-123"
        );

        // Act
        StoreRecipeOutputData outputData = new StoreRecipeOutputData(recipe);

        // Assert
        assertNotNull(outputData.getSavedRecipe());
        assertEquals(recipe, outputData.getSavedRecipe());
        assertEquals("Test Recipe", outputData.getSavedRecipe().getName());
        assertEquals("recipe-123", outputData.getSavedRecipe().getRecipeId());
    }

    @Test
    public void testGetSavedRecipe() {
        // Arrange
        Recipe recipe = new Recipe(
                "Test Recipe",
                Arrays.asList("Ingredient 1"),
                "Steps",
                4,
                new NutritionInfo(200, 10, 20, 15),
                30,
                Collections.emptyList(),
                "recipe-456"
        );
        StoreRecipeOutputData outputData = new StoreRecipeOutputData(recipe);

        // Act
        Recipe retrievedRecipe = outputData.getSavedRecipe();

        // Assert
        assertSame(recipe, retrievedRecipe);
    }

    @Test
    public void testWithNullRecipe() {
        // Arrange & Act
        StoreRecipeOutputData outputData = new StoreRecipeOutputData(null);

        // Assert
        assertNull(outputData.getSavedRecipe());
    }

    @Test
    public void testRecipePropertiesAccessible() {
        // Arrange
        NutritionInfo nutritionInfo = new NutritionInfo(500, 25, 60, 20);
        Recipe recipe = new Recipe(
                "Complex Recipe",
                Arrays.asList("Ingredient 1", "Ingredient 2", "Ingredient 3"),
                "Step 1\nStep 2\nStep 3",
                6,
                nutritionInfo,
                45,
                Collections.emptyList(),
                "recipe-789"
        );
        
        StoreRecipeOutputData outputData = new StoreRecipeOutputData(recipe);

        // Act & Assert
        assertEquals("Complex Recipe", outputData.getSavedRecipe().getName());
        assertEquals(6, outputData.getSavedRecipe().getServingSize());
        assertEquals(45, outputData.getSavedRecipe().getCookTimeMinutes());
        assertEquals("recipe-789", outputData.getSavedRecipe().getRecipeId());
        assertEquals(nutritionInfo, outputData.getSavedRecipe().getNutritionInfo());
    }
}
