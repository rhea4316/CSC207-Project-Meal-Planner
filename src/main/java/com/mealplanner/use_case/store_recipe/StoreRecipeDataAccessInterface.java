package com.mealplanner.use_case.store_recipe;

// Data access interface for saving recipes to persistent storage.
// Responsible: Aaryan (interface), Everyone (implementation via FileRecipeDataAccessObject)
// TODO: Define method to save Recipe entity to database/file storage
import com.mealplanner.entity.Recipe;
import com.mealplanner.exception.DataAccessException;

/**
 * Minimal data access interface used by the store-recipe interactor.
 * Implementations should persist a Recipe and may throw DataAccessException on failures.
 */
public interface StoreRecipeDataAccessInterface {

	/**
	 * Save the provided recipe to persistent storage.
	 * @param recipe recipe to save
	 * @throws DataAccessException on persistence errors
	 */
	void save(Recipe recipe) throws DataAccessException;

}
