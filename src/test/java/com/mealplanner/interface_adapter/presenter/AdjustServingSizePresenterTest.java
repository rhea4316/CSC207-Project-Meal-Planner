package com.mealplanner.interface_adapter.presenter;

import com.mealplanner.interface_adapter.view_model.RecipeDetailViewModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

/**
 * Test class for AdjustServingSizePresenter.
 * Tests formatting and presentation of adjusted recipes.
 *
 * Responsible: Eden (primary)
 */
public class AdjustServingSizePresenterTest {

    private AdjustServingSizePresenter presenter;

    @Mock
    private RecipeDetailViewModel viewModel;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        presenter = new AdjustServingSizePresenter(viewModel);
    }

    @Test
    public void testPresentAdjustedRecipe() {
        com.mealplanner.entity.NutritionInfo nutrition = new com.mealplanner.entity.NutritionInfo(500, 20.0, 60.0, 15.0);
        com.mealplanner.entity.Recipe adjustedRecipe = new com.mealplanner.entity.Recipe(
            "Pasta", java.util.Arrays.asList("pasta", "sauce"), "Cook pasta", 4, nutrition, null, null, "recipe-1");
        com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeOutputData outputData = 
            new com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeOutputData(adjustedRecipe);
        
        presenter.presentAdjustedRecipe(outputData);
        
        verify(viewModel).setRecipe(adjustedRecipe);
        verify(viewModel).setServingSize(4);
        verify(viewModel).setIngredients(adjustedRecipe.getIngredients());
        verify(viewModel).setNutrition(nutrition);
        verify(viewModel).setErrorMessage("");
    }

    @Test
    public void testPresentInvalidServingSize() {
        String errorMessage = "Invalid serving size";
        
        presenter.presentError(errorMessage);
        
        verify(viewModel).setErrorMessage(errorMessage);
    }

    @Test
    public void testFormatScaledIngredients() {
        com.mealplanner.entity.Recipe adjustedRecipe = new com.mealplanner.entity.Recipe(
            "Pasta", java.util.Arrays.asList("pasta", "sauce"), "Cook pasta", 4, null, null, null, "recipe-1");
        com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeOutputData outputData = 
            new com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeOutputData(adjustedRecipe);
        
        presenter.presentAdjustedRecipe(outputData);
        
        verify(viewModel).setIngredients(adjustedRecipe.getIngredients());
    }

    @Test
    public void testPresentNullOutputData() {
        presenter.presentAdjustedRecipe(null);
        
        verify(viewModel).setErrorMessage("Failed to adjust serving size");
    }
}
