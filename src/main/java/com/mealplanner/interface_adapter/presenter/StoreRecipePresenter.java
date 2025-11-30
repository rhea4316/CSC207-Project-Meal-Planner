package com.mealplanner.interface_adapter.presenter;

// Presenter for recipe storage confirmation - converts OutputData to ViewModel and shows success/error.
// Responsible: Aaryan

import com.mealplanner.interface_adapter.view_model.RecipeStoreViewModel;
import com.mealplanner.use_case.store_recipe.StoreRecipeOutputBoundary;
import com.mealplanner.use_case.store_recipe.StoreRecipeOutputData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple presenter for the store-recipe use case.
 * Currently maps the output DTO to a brief success message and logs errors.
 */
public class StoreRecipePresenter implements StoreRecipeOutputBoundary {

	private static final Logger logger = LoggerFactory.getLogger(StoreRecipePresenter.class);
	private final RecipeStoreViewModel viewModel;

	public StoreRecipePresenter(RecipeStoreViewModel viewModel) {
		this.viewModel = viewModel; // Can be null for console-only mode
	}

	@Override
	public void presentSuccess(StoreRecipeOutputData outputData) {
		if (viewModel == null) {
			// fallback to console logging
			if (outputData == null || outputData.getSavedRecipe() == null) {
				logger.info("Recipe saved (no details provided).");
				return;
			}
			var recipe = outputData.getSavedRecipe();
			String msg = String.format("Saved recipe '%s' (serves %d).",
					recipe.getName(), recipe.getServingSize());
			logger.info(msg);
			return;
		}

		if (outputData == null || outputData.getSavedRecipe() == null) {
			viewModel.setSuccessMessage("Recipe saved.");
			return;
		}

		var recipe = outputData.getSavedRecipe();
		String stepsString = recipe.getSteps();
		int stepCount = stepsString != null ? stepsString.split("\n").length : 0;
		
		String msg = String.format("Saved recipe '%s' (serves %d) — %d ingredients, %d step(s).",
				recipe.getName(), recipe.getServingSize(), recipe.getIngredients().size(), stepCount);
		viewModel.setSuccessMessage(msg);
	}

	@Override
	public void presentError(String errorMessage) {
		if (viewModel != null) {
			// Make error messages more user-friendly
			String friendlyMessage = errorMessage != null ? errorMessage : "Failed to save recipe";
			
			// Improve common error messages
			if (friendlyMessage.contains("already exists")) {
				// Keep duplicate name message as-is (already user-friendly)
			} else if (friendlyMessage.contains("validation") || friendlyMessage.contains("invalid")) {
				friendlyMessage = "Please check your input. " + friendlyMessage;
			} else if (friendlyMessage.contains("DataAccessException") || friendlyMessage.contains("database")) {
				friendlyMessage = "Unable to save recipe. Please try again or check your connection.";
			} else if (friendlyMessage.contains("IOException") || friendlyMessage.contains("network")) {
				friendlyMessage = "Network error occurred. Please check your connection and try again.";
			}
			
			viewModel.setErrorMessage(friendlyMessage);
		} else {
			logger.error("Failed to save recipe: {}", errorMessage != null ? errorMessage : "Unknown error");
		}
	}

}
