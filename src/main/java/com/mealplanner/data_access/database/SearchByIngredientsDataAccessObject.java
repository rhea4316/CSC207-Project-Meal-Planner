package com.mealplanner.data_access.database;

import com.mealplanner.data_access.api.SpoonacularApiClient;
import com.mealplanner.entity.Recipe;
import com.mealplanner.use_case.search_by_ingredients.SearchByIngredientsDataAccessInterface;

import java.io.IOException;
import java.util.List;

/**
 * Data access object for searching recipes by ingredients.
 * Uses SpoonacularApiClient to search for recipes.
 * Responsible: Everyone (implementation)
 */
public class SearchByIngredientsDataAccessObject implements SearchByIngredientsDataAccessInterface {
    
    private final SpoonacularApiClient apiClient;
    
    public SearchByIngredientsDataAccessObject(SpoonacularApiClient apiClient) {
        if (apiClient == null) {
            throw new IllegalArgumentException("SpoonacularApiClient cannot be null");
        }
        this.apiClient = apiClient;
    }
    
    @Override
    public List<Recipe> searchByIngredients(List<String> ingredients) throws IOException {
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("Ingredients list cannot be null or empty");
        }
        
        return apiClient.searchByIngredients(ingredients);
    }
}

