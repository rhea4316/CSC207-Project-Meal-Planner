package com.mealplanner.interface_adapter.presenter;

import com.mealplanner.entity.NutritionInfo;
import com.mealplanner.entity.Recipe;
import com.mealplanner.interface_adapter.view_model.RecipeDetailViewModel;
import com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeOutputData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for AdjustServingSizePresenter.
 * Tests formatting and presentation of adjusted recipes.
 *
 * Responsible: Eden (primary)
 */
public class AdjustServingSizePresenterTest {

    private AdjustServingSizePresenter presenter;
    private RecipeDetailViewModel viewModel;
    private Recipe originalRecipe;

    @BeforeEach
    public void setUp() {
        viewModel = new RecipeDetailViewModel();
        presenter = new AdjustServingSizePresenter(viewModel);

        List<String> ingredients = Arrays.asList("200g Chicken", "100g Rice");
        NutritionInfo nutritionInfo = new NutritionInfo(500, 40.0, 50.0, 15.0);
        originalRecipe = new Recipe("Chicken and Rice", ingredients, "Cook it", 2,
                nutritionInfo, 30, null, "recipe123");
    }

    @Test
    public void testPresentAdjustedRecipe() {
        // Create adjusted recipe (doubled serving size)
        Recipe adjustedRecipe = originalRecipe.adjustServingSize(4);
        AdjustServingSizeOutputData outputData = new AdjustServingSizeOutputData(adjustedRecipe);

        // Present the adjusted recipe
        presenter.presentAdjustedRecipe(outputData);

        // Verify ingredients are scaled correctly
        assertEquals(adjustedRecipe, viewModel.getRecipe());
        assertEquals(4, viewModel.getServingSize());
        assertNotNull(viewModel.getIngredients());
        assertEquals(2, viewModel.getIngredients().size());

        // Verify nutrition is scaled correctly
        NutritionInfo scaledNutrition = viewModel.getNutrition();
        assertNotNull(scaledNutrition);
        assertEquals(1000, scaledNutrition.getCalories()); // 500 * 2
        assertEquals(80.0, scaledNutrition.getProtein()); // 40 * 2
        assertEquals(100.0, scaledNutrition.getCarbs()); // 50 * 2
        assertEquals(30.0, scaledNutrition.getFat()); // 15 * 2

        // Error message should be cleared
        assertEquals("", viewModel.getErrorMessage());
    }

    @Test
    public void testPresentAdjustedRecipeWithNullOutputData() {
        // Present null output data
        presenter.presentAdjustedRecipe(null);

        // Verify error message is set
        assertEquals("Failed to adjust serving size", viewModel.getErrorMessage());
    }

    @Test
    public void testPresentAdjustedRecipeWithNullRecipe() {
        // Create output data with null recipe
        AdjustServingSizeOutputData outputData = new AdjustServingSizeOutputData(null);

        presenter.presentAdjustedRecipe(outputData);

        // Verify error message is set
        assertEquals("Failed to adjust serving size", viewModel.getErrorMessage());
    }

    @Test
    public void testPresentError() {
        String errorMessage = "Invalid serving size: must be greater than zero";

        // Present error
        presenter.presentError(errorMessage);

        // Verify error message
        assertEquals(errorMessage, viewModel.getErrorMessage());
    }

    @Test
    public void testPresentErrorWithNullMessage() {
        // Present null error message
        presenter.presentError(null);

        // Verify default error message is set
        assertEquals("An error occurred", viewModel.getErrorMessage());
    }

    @Test
    public void testPresenterRequiresNonNullViewModel() {
        // Test that presenter constructor throws exception with null view model
        assertThrows(NullPointerException.class, () -> {
            new AdjustServingSizePresenter(null);
        });
    }

    @Test
    public void testPresentAdjustedRecipeWithFractionalScaling() {
        // Create adjusted recipe (halved serving size)
        Recipe halfRecipe = originalRecipe.adjustServingSize(1);
        AdjustServingSizeOutputData outputData = new AdjustServingSizeOutputData(halfRecipe);

        presenter.presentAdjustedRecipe(outputData);

        // Verify nutrition is scaled correctly with fractional multiplier
        NutritionInfo scaledNutrition = viewModel.getNutrition();
        assertNotNull(scaledNutrition);
        assertEquals(250, scaledNutrition.getCalories()); // 500 * 0.5
        assertEquals(20.0, scaledNutrition.getProtein()); // 40 * 0.5
        assertEquals(25.0, scaledNutrition.getCarbs()); // 50 * 0.5
        assertEquals(7.5, scaledNutrition.getFat(), 0.01); // 15 * 0.5
    }

    @Test
    public void testPresentAdjustedRecipeWithNoNutrition() {
        // Create recipe without nutrition info
        Recipe noNutritionRecipe = new Recipe("Simple Salad",
                Arrays.asList("Lettuce", "Tomato"), "Mix", 1);

        Recipe adjusted = noNutritionRecipe.adjustServingSize(2);
        AdjustServingSizeOutputData outputData = new AdjustServingSizeOutputData(adjusted);

        presenter.presentAdjustedRecipe(outputData);

        // Should handle null nutrition gracefully
        assertEquals(adjusted, viewModel.getRecipe());
        assertNull(viewModel.getNutrition());
        assertEquals("", viewModel.getErrorMessage());
    }
}
