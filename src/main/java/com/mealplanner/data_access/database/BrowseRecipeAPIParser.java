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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrowseRecipeAPIParser implements BrowseRecipeDataAccessInterface {
    
    private static final Logger logger = LoggerFactory.getLogger(BrowseRecipeAPIParser.class);
    //would probably need a getrecipe/list of recipes method:
    // i.e. input search filters or recipe name and then the method would call the api and return the parsed information
    private final SpoonacularApiClient apiClient;

    public BrowseRecipeAPIParser(SpoonacularApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "SpoonacularApiClient cannot be null");
    }

    @Override
    public List<Recipe> searchRecipes(BrowseRecipeInputData inputData) throws IOException, RecipeNotFoundException {
        if (inputData == null) {
            throw new IllegalArgumentException("Input data cannot be null");
        }

        String query = inputData.getQuery();
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }

        int numberOfRecipes = inputData.getNumberOfRecipesInt();
        String includedIngredients = inputData.getIncludedIngredients();

        // Call API using SpoonacularApiClient
        // OPTIMIZATION: complexSearch now includes addRecipeInformation=true, so we get full details in one call
        String apiResponse = apiClient.complexSearch(query, numberOfRecipes, includedIngredients);

        // Parse the response
        final List<Recipe> recipes = new ArrayList<>();
        final JSONObject jsonBody = new JSONObject(apiResponse);
        final JSONArray jsonArray = jsonBody.getJSONArray("results");

        if (jsonArray.isEmpty()) {
            throw new RecipeNotFoundException("Recipes not found with given query and ingredients", null);
        }

        // OPTIMIZATION: Parse recipes directly from complexSearch response instead of making N additional API calls
        logger.debug("Parsing {} recipes from API response", jsonArray.length());
        int successCount = 0;
        int failCount = 0;
        
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject currentRecipe = jsonArray.getJSONObject(i);
            try {
                // Parse recipe directly from the search result which now includes full information
                Recipe recipe = com.mealplanner.data_access.api.ApiResponseParser.parseRecipe(currentRecipe);
                if (recipe != null) {
                    recipes.add(recipe);
                    successCount++;
                    logger.debug("Successfully parsed recipe: {}", recipe.getName());
                } else {
                    failCount++;
                    logger.warn("parseRecipe returned null for recipe at index {}", i);
                }
            } catch (Exception e) {
                // Skip recipes that fail to parse - continue with others
                failCount++;
                logger.warn("Failed to parse recipe from API response at index {}: {}", i, e.getMessage(), e);
            }
        }

        logger.debug("Recipe parsing complete: {} successful, {} failed, {} total", successCount, failCount, recipes.size());

        if (recipes.isEmpty()) {
            throw new RecipeNotFoundException("Found recipes but failed to receive details for all of them", null);
        }
        return recipes;
    }
}
