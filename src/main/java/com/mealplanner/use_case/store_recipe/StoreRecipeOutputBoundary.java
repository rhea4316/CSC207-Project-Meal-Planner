package com.mealplanner.use_case.store_recipe;

// Output boundary interface for presenting recipe save success or errors.
// Responsible: Aaryan
// TODO: Define methods for presentSuccess (with calculated nutrition) and presentError (validation failures)
/**
 * Output boundary for the store-recipe use case. Implementations present success or error results.
 */
public interface StoreRecipeOutputBoundary {

	/**
	 * Present a successful save with output data (saved recipe, nutrition info, etc).
	 */
	void presentSuccess(StoreRecipeOutputData outputData);

	/**
	 * Present an error message to the caller/UI.
	 */
	void presentError(String errorMessage);

}
