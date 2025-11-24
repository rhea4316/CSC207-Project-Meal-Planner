package com.mealplanner.interface_adapter.presenter;

import com.mealplanner.interface_adapter.view_model.RecipeDetailViewModel;
import com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeOutputBoundary;
import com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeOutputData;

// Presenter for adjusted recipe - converts OutputData with scaled recipe to ViewModel.
// Responsible: Eden

public class AdjustServingSizePresenter implements AdjustServingSizeOutputBoundary {
    private final RecipeDetailViewModel viewModel;

    public AdjustServingSizePresenter(RecipeDetailViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentAdjustedRecipe(AdjustServingSizeOutputData outputData) {
        if (outputData == null || outputData.getAdjustedRecipe() == null) {
            viewModel.setErrorMessage("Failed to adjust serving size");
            return;
        }

        var adjustedRecipe = outputData.getAdjustedRecipe();
        viewModel.setRecipe(adjustedRecipe);
        viewModel.setServingSize(adjustedRecipe.getServingSize());
        viewModel.setIngredients(adjustedRecipe.getIngredients());
        viewModel.setNutrition(adjustedRecipe.getNutritionInfo());
        viewModel.setErrorMessage("");
    }

    @Override
    public void presentError(String errorMessage) {
        viewModel.setErrorMessage(errorMessage != null ? errorMessage : "An error occurred");
    }
}
