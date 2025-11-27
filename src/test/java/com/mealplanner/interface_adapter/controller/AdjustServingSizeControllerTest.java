package com.mealplanner.interface_adapter.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeInputBoundary;
import com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeInputData;

/**
 * Test class for AdjustServingSizeController.
 * Tests controller input validation and interactor invocation.
 *
 * Responsible: Eden (primary)
 */
public class AdjustServingSizeControllerTest {

    private AdjustServingSizeController controller;

    @Mock
    private AdjustServingSizeInputBoundary interactor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new AdjustServingSizeController(interactor);
    }

    @Test
    public void testAdjustServingSizeWithValidInput() {
        // Arrange
        String recipeId = "recipe123";
        int newServingSize = 4;

        // Act
        controller.execute(recipeId, newServingSize);

        // Assert
        verify(interactor).execute(argThat(inputData -> 
            inputData.getRecipeId().equals(recipeId) &&
            inputData.getNewServingSize() == newServingSize
        ));
    }

    @Test
    public void testAdjustServingSizeWithInvalidSize() {
        // Arrange
        String recipeId = "recipe123";
        int invalidServingSize = -1;

        // Act
        controller.execute(recipeId, invalidServingSize);

        // Assert
        verify(interactor).execute(argThat(inputData -> 
            inputData.getRecipeId().equals(recipeId) &&
            inputData.getNewServingSize() == invalidServingSize
        ));
    }

    @Test
    public void testAdjustServingSizeWithZero() {
        // Arrange
        String recipeId = "recipe123";
        int zeroServingSize = 0;

        // Act
        controller.execute(recipeId, zeroServingSize);

        // Assert
        verify(interactor).execute(argThat(inputData -> 
            inputData.getRecipeId().equals(recipeId) &&
            inputData.getNewServingSize() == zeroServingSize
        ));
    }

    @Test
    public void testAdjustServingSizeWithEmptyRecipeId() {
        // Arrange
        String emptyRecipeId = "";
        int newServingSize = 4;

        // Act
        controller.execute(emptyRecipeId, newServingSize);

        // Assert
        verify(interactor, never()).execute(any(AdjustServingSizeInputData.class));
    }

    @Test
    public void testAdjustServingSizeWithNullRecipeId() {
        // Arrange
        String nullRecipeId = null;
        int newServingSize = 4;

        // Act
        controller.execute(nullRecipeId, newServingSize);

        // Assert
        verify(interactor, never()).execute(any(AdjustServingSizeInputData.class));
    }

    @Test
    public void testAdjustServingSizeWithLargeServingSize() {
        // Arrange
        String recipeId = "recipe123";
        int largeServingSize = 100;

        // Act
        controller.execute(recipeId, largeServingSize);

        // Assert
        verify(interactor).execute(argThat(inputData -> 
            inputData.getRecipeId().equals(recipeId) &&
            inputData.getNewServingSize() == largeServingSize
        ));
    }
}
