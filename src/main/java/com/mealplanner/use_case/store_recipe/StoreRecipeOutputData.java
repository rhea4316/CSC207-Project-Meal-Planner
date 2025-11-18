package com.mealplanner.use_case.store_recipe;

// Data transfer object carrying success confirmation and saved recipe details.
// Responsible: Aaryan
// TODO: Implement with saved Recipe object including calculated nutrition information
import com.mealplanner.entity.Recipe;

/**
 * Minimal output DTO for the store-recipe use case.
 */
public class StoreRecipeOutputData {

	private final Recipe savedRecipe;

	public StoreRecipeOutputData(Recipe savedRecipe) {
		this.savedRecipe = savedRecipe;
	}

	public Recipe getSavedRecipe() {
		return savedRecipe;
	}

}
