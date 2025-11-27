package com.mealplanner.data_access.database;

import com.mealplanner.data_access.api.SpoonacularApiClient;
import com.mealplanner.entity.Recipe;
import com.mealplanner.exception.RecipeNotFoundException;
import com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeDataAccessInterface;

import java.io.IOException;

/**
 * Data access object for adjusting serving sizes.
 * Uses SpoonacularApiClient to retrieve recipes.
 * Responsible: Everyone (implementation)
 */
public class AdjustServingSizeDataAccessObject implements AdjustServingSizeDataAccessInterface {
    
    private final SpoonacularApiClient apiClient;
    
    public AdjustServingSizeDataAccessObject(SpoonacularApiClient apiClient) {
        if (apiClient == null) {
            throw new IllegalArgumentException("SpoonacularApiClient cannot be null");
        }
        this.apiClient = apiClient;
    }
    
    @Override
    public Recipe getRecipeById(String recipeId) throws RecipeNotFoundException {
        if (recipeId == null || recipeId.trim().isEmpty()) {
            throw new RecipeNotFoundException("Recipe ID cannot be empty");
        }
        
        try {
            Recipe recipe = apiClient.getRecipeById(recipeId);
            if (recipe == null) {
                throw new RecipeNotFoundException(recipeId);
            }
            return recipe;
        } catch (IOException e) {
            throw new RecipeNotFoundException("Failed to retrieve recipe: " + e.getMessage(), recipeId);
        } catch (NumberFormatException e) {
            throw new RecipeNotFoundException("Invalid recipe ID format: " + recipeId, recipeId);
        }
    }
}

