package com.mealplanner.use_case.store_recipe;

// Input boundary interface for storing/creating a new recipe.
// Responsible: Aaryan
// TODO: Define execute method that takes StoreRecipeInputData (recipe details) as parameter
/**
 * Input boundary for the store-recipe use case.
 */
public interface StoreRecipeInputBoundary {

	/**
	 * Execute the use case to store/create a recipe.
	 * @param inputData incoming DTO containing recipe details
	 */
	void execute(StoreRecipeInputData inputData);

}
