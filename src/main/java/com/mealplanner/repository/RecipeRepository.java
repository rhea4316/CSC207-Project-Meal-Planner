package com.mealplanner.repository;

import com.mealplanner.entity.Recipe;
import com.mealplanner.exception.DataAccessException;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Recipe data access.
 * Provides abstraction over the underlying storage mechanism.
 *
 * This interface allows the application to change storage implementations
 * (file, database, API) without affecting business logic.
 *
 * Responsible: Everyone (database team implements, all use cases consume)
 */
public interface RecipeRepository {

    /**
     * Save a recipe to the repository.
     * If a recipe with the same ID exists, it will be updated.
     *
     * @param recipe Recipe to save
     * @throws DataAccessException if save operation fails
     */
    void save(Recipe recipe) throws DataAccessException;

    /**
     * Find a recipe by its unique ID.
     *
     * @param recipeId Recipe ID to search for
     * @return Optional containing the recipe if found, empty otherwise
     * @throws DataAccessException if read operation fails
     */
    Optional<Recipe> findById(String recipeId) throws DataAccessException;

    /**
     * Find all recipes in the repository.
     *
     * @return List of all recipes (may be empty)
     * @throws DataAccessException if read operation fails
     */
    List<Recipe> findAll() throws DataAccessException;

    /**
     * Find recipes by name (case-insensitive, partial match).
     *
     * @param name Name or partial name to search for
     * @return List of matching recipes (may be empty)
     * @throws DataAccessException if read operation fails
     */
    List<Recipe> findByName(String name) throws DataAccessException;

    /**
     * Delete a recipe by its ID.
     *
     * @param recipeId Recipe ID to delete
     * @return true if recipe was deleted, false if not found
     * @throws DataAccessException if delete operation fails
     */
    boolean delete(String recipeId) throws DataAccessException;

    /**
     * Check if a recipe with the given ID exists.
     *
     * @param recipeId Recipe ID to check
     * @return true if recipe exists
     * @throws DataAccessException if read operation fails
     */
    boolean exists(String recipeId) throws DataAccessException;

    /**
     * Get the total count of recipes in the repository.
     *
     * @return Number of recipes
     * @throws DataAccessException if read operation fails
     */
    int count() throws DataAccessException;

    /**
     * Clear all recipes from the repository.
     * Use with caution!
     *
     * @throws DataAccessException if delete operation fails
     */
    void clear() throws DataAccessException;
}
