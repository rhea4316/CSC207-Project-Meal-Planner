package com.mealplanner.use_case.store_recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Test class for StoreRecipeInputData.
 */
public class StoreRecipeInputDataTest {

    @Test
    public void testConstructorWithValidData() {
        // Arrange
        String name = "Test Recipe";
        List<String> ingredients = Arrays.asList("Ingredient 1", "Ingredient 2");
        List<String> steps = Arrays.asList("Step 1", "Step 2");
        int servingSize = 4;

        // Act
        StoreRecipeInputData inputData = new StoreRecipeInputData(name, ingredients, steps, servingSize);

        // Assert
        assertEquals(name, inputData.getName());
        assertEquals(ingredients, inputData.getIngredients());
        assertEquals(steps, inputData.getSteps());
        assertEquals(servingSize, inputData.getServingSize());
    }

    @Test
    public void testConstructorWithNullIngredients() {
        // Arrange & Act
        StoreRecipeInputData inputData = new StoreRecipeInputData(
                "Test Recipe",
                null,
                Arrays.asList("Step 1"),
                4
        );

        // Assert
        assertNotNull(inputData.getIngredients());
        assertTrue(inputData.getIngredients().isEmpty());
    }

    @Test
    public void testConstructorWithNullSteps() {
        // Arrange & Act
        StoreRecipeInputData inputData = new StoreRecipeInputData(
                "Test Recipe",
                Arrays.asList("Ingredient 1"),
                null,
                4
        );

        // Assert
        assertNotNull(inputData.getSteps());
        assertTrue(inputData.getSteps().isEmpty());
    }

    @Test
    public void testGetIngredientsReturnsDefensiveCopy() {
        // Arrange
        List<String> ingredients = new ArrayList<>(Arrays.asList("Ingredient 1"));
        StoreRecipeInputData inputData = new StoreRecipeInputData(
                "Test Recipe",
                ingredients,
                Arrays.asList("Step 1"),
                4
        );

        // Act
        List<String> returnedIngredients = inputData.getIngredients();
        returnedIngredients.add("Ingredient 2");

        // Assert
        assertEquals(1, inputData.getIngredients().size());
        assertEquals("Ingredient 1", inputData.getIngredients().get(0));
    }

    @Test
    public void testGetStepsReturnsDefensiveCopy() {
        // Arrange
        List<String> steps = new ArrayList<>(Arrays.asList("Step 1"));
        StoreRecipeInputData inputData = new StoreRecipeInputData(
                "Test Recipe",
                Arrays.asList("Ingredient 1"),
                steps,
                4
        );

        // Act
        List<String> returnedSteps = inputData.getSteps();
        returnedSteps.add("Step 2");

        // Assert
        assertEquals(1, inputData.getSteps().size());
        assertEquals("Step 1", inputData.getSteps().get(0));
    }

    @Test
    public void testConstructorCreatesDefensiveCopy() {
        // Arrange
        List<String> ingredients = new ArrayList<>(Arrays.asList("Ingredient 1"));
        List<String> steps = new ArrayList<>(Arrays.asList("Step 1"));
        
        StoreRecipeInputData inputData = new StoreRecipeInputData(
                "Test Recipe",
                ingredients,
                steps,
                4
        );

        // Act - modify original lists
        ingredients.add("Ingredient 2");
        steps.add("Step 2");

        // Assert - internal state should not change
        assertEquals(1, inputData.getIngredients().size());
        assertEquals(1, inputData.getSteps().size());
    }

    @Test
    public void testWithEmptyLists() {
        // Arrange & Act
        StoreRecipeInputData inputData = new StoreRecipeInputData(
                "Empty Recipe",
                new ArrayList<>(),
                new ArrayList<>(),
                1
        );

        // Assert
        assertTrue(inputData.getIngredients().isEmpty());
        assertTrue(inputData.getSteps().isEmpty());
    }

    @Test
    public void testWithZeroServingSize() {
        // Arrange & Act
        StoreRecipeInputData inputData = new StoreRecipeInputData(
                "Test Recipe",
                Arrays.asList("Ingredient 1"),
                Arrays.asList("Step 1"),
                0
        );

        // Assert
        assertEquals(0, inputData.getServingSize());
    }

    @Test
    public void testWithNegativeServingSize() {
        // Arrange & Act
        StoreRecipeInputData inputData = new StoreRecipeInputData(
                "Test Recipe",
                Arrays.asList("Ingredient 1"),
                Arrays.asList("Step 1"),
                -1
        );

        // Assert
        assertEquals(-1, inputData.getServingSize());
    }

    @Test
    public void testWithNullName() {
        // Arrange & Act
        StoreRecipeInputData inputData = new StoreRecipeInputData(
                null,
                Arrays.asList("Ingredient 1"),
                Arrays.asList("Step 1"),
                4
        );

        // Assert
        assertNull(inputData.getName());
    }
}
