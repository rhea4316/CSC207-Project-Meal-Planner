package com.mealplanner.use_case.store_recipe;

// Main business logic for storing/creating recipes with nutrition calculation.
// Responsible: Aaryan
import java.util.Objects;
import java.util.UUID;
import java.util.List;

import com.mealplanner.entity.Recipe;
import com.mealplanner.exception.DataAccessException;
import com.mealplanner.repository.RecipeRepository;
import com.mealplanner.util.StringUtil;
import com.mealplanner.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreRecipeInteractor implements StoreRecipeInputBoundary {

	private static final Logger logger = LoggerFactory.getLogger(StoreRecipeInteractor.class);
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

		if (StringUtil.isNullOrEmpty(inputData.getName())) {
			presenter.presentError("Recipe name cannot be empty");
			return;
		}

		if (!ValidationUtil.validateRecipeName(inputData.getName())) {
			presenter.presentError("Recipe name is invalid");
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

		if (!ValidationUtil.validateServingSize(inputData.getServingSize())) {
			presenter.presentError("Serving size must be between 1 and 100");
			return;
		}

		// Check for duplicate recipe name (case-insensitive)
		// Only check if this is a new recipe (no recipeId provided)
		String recipeId = inputData.getRecipeId();
		if (StringUtil.isNullOrEmpty(recipeId)) {
			try {
				List<Recipe> existingRecipes = recipeRepository.findByName(inputData.getName());
				if (existingRecipes != null && !existingRecipes.isEmpty()) {
					// Check for exact match (case-insensitive)
					boolean exactMatch = existingRecipes.stream()
							.anyMatch(r -> r.getName().equalsIgnoreCase(inputData.getName()));
					
					if (exactMatch) {
						logger.info("Duplicate recipe name detected: '{}'", inputData.getName());
						presenter.presentError("A recipe with the name '" + inputData.getName() + 
								"' already exists. Please choose a different name or edit the existing recipe.");
						return;
					}
				}
			} catch (DataAccessException e) {
				// If we can't check for duplicates, log but continue (don't block recipe creation)
				// This is a non-critical check
				logger.warn("Failed to check for duplicate recipe name '{}': {}", inputData.getName(), e.getMessage(), e);
			}
			
			recipeId = "recipe-" + UUID.randomUUID().toString();
		}

		// Convert steps list to string (join with newlines)
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
