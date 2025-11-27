package com.mealplanner.interface_adapter.controller;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mealplanner.use_case.store_recipe.StoreRecipeInputBoundary;
import com.mealplanner.use_case.store_recipe.StoreRecipeInputData;

/**
 * Test class for StoreRecipeController.
 */
public class StoreRecipeControllerTest {

    private StoreRecipeController controller;
    private TestInteractor interactor;

    @BeforeEach
    public void setUp() {
        interactor = new TestInteractor();
        controller = new StoreRecipeController(interactor);
    }

    @Test
    public void testExecuteWithValidData() {
        // Arrange
        String name = "Test Recipe";
        List<String> ingredients = Arrays.asList("Ingredient 1", "Ingredient 2");
        List<String> steps = Arrays.asList("Step 1", "Step 2");
        int servingSize = 4;

        // Act
        controller.execute(name, ingredients, steps, servingSize);

        // Assert
        assertTrue(interactor.executeCalled);
        assertNotNull(interactor.lastInputData);
        assertEquals(name, interactor.lastInputData.getName());
        assertEquals(ingredients, interactor.lastInputData.getIngredients());
        assertEquals(steps, interactor.lastInputData.getSteps());
        assertEquals(servingSize, interactor.lastInputData.getServingSize());
    }

    @Test
    public void testExecuteFromFormWithCommaSeparatedIngredients() {
        // Arrange
        String name = "Test Recipe";
        String ingredientsRaw = "Ingredient 1, Ingredient 2, Ingredient 3";
        String stepsRaw = "Step 1\nStep 2";
        String servingSizeStr = "4";

        // Act
        controller.executeFromForm(name, ingredientsRaw, stepsRaw, servingSizeStr);

        // Assert
        assertTrue(interactor.executeCalled);
        assertEquals(3, interactor.lastInputData.getIngredients().size());
        assertEquals("Ingredient 1", interactor.lastInputData.getIngredients().get(0));
        assertEquals("Ingredient 2", interactor.lastInputData.getIngredients().get(1));
        assertEquals("Ingredient 3", interactor.lastInputData.getIngredients().get(2));
    }

    @Test
    public void testExecuteFromFormWithNewlineSeparatedSteps() {
        // Arrange
        String name = "Test Recipe";
        String ingredientsRaw = "Ingredient 1";
        String stepsRaw = "Step 1\nStep 2\nStep 3";
        String servingSizeStr = "2";

        // Act
        controller.executeFromForm(name, ingredientsRaw, stepsRaw, servingSizeStr);

        // Assert
        assertTrue(interactor.executeCalled);
        assertEquals(3, interactor.lastInputData.getSteps().size());
        assertEquals("Step 1", interactor.lastInputData.getSteps().get(0));
        assertEquals("Step 2", interactor.lastInputData.getSteps().get(1));
        assertEquals("Step 3", interactor.lastInputData.getSteps().get(2));
    }

    @Test
    public void testExecuteFromFormWithMixedSeparators() {
        // Arrange
        String name = "Test Recipe";
        String ingredientsRaw = "Ingredient 1,Ingredient 2\nIngredient 3";
        String stepsRaw = "Step 1";
        String servingSizeStr = "1";

        // Act
        controller.executeFromForm(name, ingredientsRaw, stepsRaw, servingSizeStr);

        // Assert
        assertTrue(interactor.executeCalled);
        assertEquals(3, interactor.lastInputData.getIngredients().size());
    }

    @Test
    public void testExecuteFromFormWithInvalidServingSize() {
        // Arrange
        String name = "Test Recipe";
        String ingredientsRaw = "Ingredient 1";
        String stepsRaw = "Step 1";
        String servingSizeStr = "invalid";

        // Act
        controller.executeFromForm(name, ingredientsRaw, stepsRaw, servingSizeStr);

        // Assert
        assertTrue(interactor.executeCalled);
        assertEquals(1, interactor.lastInputData.getServingSize()); // Default to 1
    }

    @Test
    public void testExecuteFromFormWithNullServingSize() {
        // Arrange
        String name = "Test Recipe";
        String ingredientsRaw = "Ingredient 1";
        String stepsRaw = "Step 1";

        // Act
        controller.executeFromForm(name, ingredientsRaw, stepsRaw, null);

        // Assert
        assertTrue(interactor.executeCalled);
        assertEquals(1, interactor.lastInputData.getServingSize()); // Default to 1
    }

    @Test
    public void testExecuteFromFormWithZeroServingSize() {
        // Arrange
        String name = "Test Recipe";
        String ingredientsRaw = "Ingredient 1";
        String stepsRaw = "Step 1";
        String servingSizeStr = "0";

        // Act
        controller.executeFromForm(name, ingredientsRaw, stepsRaw, servingSizeStr);

        // Assert
        assertTrue(interactor.executeCalled);
        assertEquals(1, interactor.lastInputData.getServingSize()); // Defaults to 1
    }

    @Test
    public void testExecuteFromFormWithNegativeServingSize() {
        // Arrange
        String name = "Test Recipe";
        String ingredientsRaw = "Ingredient 1";
        String stepsRaw = "Step 1";
        String servingSizeStr = "-5";

        // Act
        controller.executeFromForm(name, ingredientsRaw, stepsRaw, servingSizeStr);

        // Assert
        assertTrue(interactor.executeCalled);
        assertEquals(1, interactor.lastInputData.getServingSize()); // Defaults to 1
    }

    @Test
    public void testExecuteFromFormWithNullIngredients() {
        // Arrange
        String name = "Test Recipe";
        String stepsRaw = "Step 1";
        String servingSizeStr = "2";

        // Act
        controller.executeFromForm(name, null, stepsRaw, servingSizeStr);

        // Assert
        assertTrue(interactor.executeCalled);
        assertTrue(interactor.lastInputData.getIngredients().isEmpty());
    }

    @Test
    public void testExecuteFromFormWithNullSteps() {
        // Arrange
        String name = "Test Recipe";
        String ingredientsRaw = "Ingredient 1";
        String servingSizeStr = "2";

        // Act
        controller.executeFromForm(name, ingredientsRaw, null, servingSizeStr);

        // Assert
        assertTrue(interactor.executeCalled);
        assertTrue(interactor.lastInputData.getSteps().isEmpty());
    }

    @Test
    public void testExecuteFromFormTrimsWhitespace() {
        // Arrange
        String name = "Test Recipe";
        String ingredientsRaw = "  Ingredient 1  ,  Ingredient 2  ";
        String stepsRaw = "  Step 1  \n  Step 2  ";
        String servingSizeStr = "  4  ";

        // Act
        controller.executeFromForm(name, ingredientsRaw, stepsRaw, servingSizeStr);

        // Assert
        assertTrue(interactor.executeCalled);
        assertEquals("Ingredient 1", interactor.lastInputData.getIngredients().get(0));
        assertEquals("Ingredient 2", interactor.lastInputData.getIngredients().get(1));
        assertEquals("Step 1", interactor.lastInputData.getSteps().get(0));
        assertEquals("Step 2", interactor.lastInputData.getSteps().get(1));
        assertEquals(4, interactor.lastInputData.getServingSize());
    }

    @Test
    public void testExecuteFromFormIgnoresEmptyStrings() {
        // Arrange
        String name = "Test Recipe";
        String ingredientsRaw = "Ingredient 1,,Ingredient 2,  ,Ingredient 3";
        String stepsRaw = "Step 1\n\nStep 2";
        String servingSizeStr = "2";

        // Act
        controller.executeFromForm(name, ingredientsRaw, stepsRaw, servingSizeStr);

        // Assert
        assertTrue(interactor.executeCalled);
        assertEquals(3, interactor.lastInputData.getIngredients().size());
        assertEquals(2, interactor.lastInputData.getSteps().size());
    }

    @Test
    public void testConstructorWithNullInteractor() {
        // Act & Assert - expect NullPointerException when passing null
        NullPointerException thrown = assertThrows(
                NullPointerException.class,
                () -> new StoreRecipeController(null)
        );
        assertNotNull(thrown);
    }

    // Test double
    private static class TestInteractor implements StoreRecipeInputBoundary {
        boolean executeCalled = false;
        StoreRecipeInputData lastInputData;

        @Override
        public void execute(StoreRecipeInputData inputData) {
            this.executeCalled = true;
            this.lastInputData = inputData;
        }
    }
}
