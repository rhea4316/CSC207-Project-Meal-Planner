package com.mealplanner.interface_adapter.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeInputBoundary;

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
        String recipeId = "recipe-1";
        int newServingSize = 4;
        
        controller.execute(recipeId, newServingSize);
        
        verify(interactor).execute(argThat(inputData -> 
            inputData.getRecipeId().equals(recipeId) &&
            inputData.getNewServingSize() == newServingSize
        ));
    }

    @Test
    public void testAdjustServingSizeWithInvalidSize() {
        String recipeId = "recipe-1";
        int invalidSize = -1;
        
        controller.execute(recipeId, invalidSize);
        
        verify(interactor).execute(argThat(inputData -> 
            inputData.getRecipeId().equals(recipeId) &&
            inputData.getNewServingSize() == invalidSize
        ));
    }

    @Test
    public void testAdjustServingSizeWithZero() {
        String recipeId = "recipe-1";
        int zeroSize = 0;
        
        controller.execute(recipeId, zeroSize);
        
        verify(interactor).execute(argThat(inputData -> 
            inputData.getRecipeId().equals(recipeId) &&
            inputData.getNewServingSize() == zeroSize
        ));
    }

    @Test
    public void testAdjustServingSizeWithEmptyRecipeId() {
        controller.execute("", 4);
        verify(interactor, never()).execute(any());
    }

    @Test
    public void testAdjustServingSizeWithNullRecipeId() {
        controller.execute(null, 4);
        verify(interactor, never()).execute(any());
    }
}
