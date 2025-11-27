package com.mealplanner.use_case.store_recipe;

// Main business logic for storing/creating recipes with nutrition calculation.
// Responsible: Aaryan 
// TODO: Implement execute method: validate fields, create Recipe entity, calculate nutrition, save to database, pass result to presenter
import java.util.Objects;
import java.util.UUID;

import com.mealplanner.entity.Recipe;
import com.mealplanner.exception.DataAccessException;
import com.mealplanner.repository.RecipeRepository;

public class StoreRecipeInteractor implements StoreRecipeInputBoundary {

	private final StoreRecipeOutputBoundary presenter;
	private final RecipeRepository recipeRepository;

	public StoreRecipeInteractor(StoreRecipeOutputBoundary presenter, RecipeRepository recipeRepository) {
		this.presenter = Objects.requireNonNull(presenter);
		this.recipeRepository = Objects.requireNonNull(recipeRepository);
	}

	@Override
	public void execute(StoreRecipeInputData inputData) {
		// Basic validation
		if (inputData == null) {
			presenter.presentError("Input data cannot be null");
			return;
		}

		if (inputData.getName() == null || inputData.getName().trim().isEmpty()) {
			presenter.presentError("Recipe name cannot be empty");
			return;
		}

		if (inputData.getIngredients() == null || inputData.getIngredients().isEmpty()) {
			presenter.presentError("Ingredients list cannot be empty");
			return;
		}

		if (inputData.getSteps() == null || inputData.getSteps().isEmpty()) {
			presenter.presentError("Steps list cannot be empty");
			return;
		}

		if (inputData.getServingSize() <= 0) {
			presenter.presentError("Serving size must be greater than zero");
			return;
		}

		// Generate a unique recipe ID
		String recipeId = "recipe-" + UUID.randomUUID().toString();

		// Convert steps list to string
		String stepsString = String.join("\n", inputData.getSteps());

		// Create Recipe entity (nutrition calculation and optional fields omitted here)
		Recipe recipe = new Recipe(
				inputData.getName(),
				inputData.getIngredients(),
				stepsString,
				inputData.getServingSize(),
				null, // nutritionInfo
				null, // cookTimeMinutes
				null, // dietaryRestrictions
				recipeId
		);

		try {
			// Persist recipe using repository
			recipeRepository.save(recipe);

			// Wrap and present success
			presenter.presentSuccess(new StoreRecipeOutputData(recipe));
		} catch (DataAccessException dae) {
			presenter.presentError("Failed to save recipe: " + dae.getMessage());
		} catch (RuntimeException re) {
			// Catch other runtime issues and report
			presenter.presentError("An error occurred while saving recipe: " + re.getMessage());
		}
	}

}
