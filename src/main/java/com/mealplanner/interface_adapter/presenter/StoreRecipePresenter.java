package com.mealplanner.interface_adapter.presenter;

// Presenter for recipe storage confirmation - converts OutputData to ViewModel and shows success/error.
// Responsible: Aaryan
// TODO: Implement OutputBoundary methods to show save confirmation with nutrition info in RecipeStoreViewModel

import com.mealplanner.interface_adapter.view_model.RecipeStoreViewModel;
import com.mealplanner.use_case.store_recipe.StoreRecipeOutputBoundary;
import com.mealplanner.use_case.store_recipe.StoreRecipeOutputData;

/**
 * Simple presenter for the store-recipe use case.
 * Currently maps the output DTO to a brief success message and logs errors.
 */
public class StoreRecipePresenter implements StoreRecipeOutputBoundary {

	private final RecipeStoreViewModel viewModel;

	public StoreRecipePresenter(RecipeStoreViewModel viewModel) {
		this.viewModel = viewModel;
	}

	@Override
	public void presentSuccess(StoreRecipeOutputData outputData) {
		if (viewModel == null) {
			// fallback to console logging
			if (outputData == null || outputData.getSavedRecipe() == null) {
				System.out.println("Recipe saved (no details provided).");
				return;
			}
			var recipe = outputData.getSavedRecipe();
			String msg = String.format("Saved recipe '%s' (serves %d).",
					recipe.getName(), recipe.getServingSize());
			System.out.println(msg);
			return;
		}

		if (outputData == null || outputData.getSavedRecipe() == null) {
			viewModel.setSuccessMessage("Recipe saved.");
			return;
		}

		var recipe = outputData.getSavedRecipe();
		String msg = String.format("Saved recipe '%s' (serves %d) — %d ingredients, %d steps.",
				recipe.getName(), recipe.getServingSize(), recipe.getIngredients().size(), recipe.getSteps().size());
		viewModel.setSuccessMessage(msg);
	}

	@Override
	public void presentError(String errorMessage) {
		if (viewModel != null) {
			viewModel.setErrorMessage(errorMessage != null ? errorMessage : "Failed to save recipe");
		} else {
			System.err.println("Failed to save recipe: " + (errorMessage != null ? errorMessage : "Unknown error"));
		}
	}

}
