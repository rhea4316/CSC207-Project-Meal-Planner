package com.mealplanner.data_access.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mealplanner.data_access.api.SpoonacularApiClient;
import com.mealplanner.entity.Recipe;
import com.mealplanner.exception.RecipeNotFoundException;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeDataAccessInterface;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeInputData;

public class BrowseRecipeAPIParser implements BrowseRecipeDataAccessInterface {

    private final SpoonacularApiClient apiClient;

    public BrowseRecipeAPIParser(SpoonacularApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "SpoonacularApiClient cannot be null");
    }

    @Override
    public List<Recipe> searchRecipes(BrowseRecipeInputData inputData) throws IOException, RecipeNotFoundException {
        if (inputData == null) {
            throw new IllegalArgumentException("Input data cannot be null");
        }
        
        final String query = inputData.getQuery();
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        
        final int numberOfRecipes = inputData.getNumberOfRecipesInt();
        final String includedIngredients = inputData.getIncludedIngredients();
        
        // Call API using SpoonacularApiClient
        final String apiResponse = apiClient.complexSearch(query, numberOfRecipes, includedIngredients);
        
        // Parse the response
        final List<Recipe> recipes = new ArrayList<>();
        final JSONObject jsonBody = new JSONObject(apiResponse);
        final JSONArray jsonArray = jsonBody.getJSONArray("results");

        if (jsonArray.isEmpty()) {
            throw new RecipeNotFoundException("Recipes not found with given query and ingredients", null);
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONObject currentRecipe = jsonArray.getJSONObject(i);
            final int currentRecipeId = currentRecipe.getInt("id");

            // Fetch full recipe information using SpoonacularApiClient
            final Recipe recipe = apiClient.getRecipeById(currentRecipeId);
            recipes.add(recipe);
        }

        if (recipes.isEmpty()) {
            throw new RecipeNotFoundException("Found recipes but failed to receive details for all of them", null);
        }
        return recipes;
    }
}
