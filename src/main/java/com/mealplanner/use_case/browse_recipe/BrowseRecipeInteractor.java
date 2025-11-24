package com.mealplanner.use_case.browse_recipe;

// Main business logic for browsing recipe details and viewing ingredients.
// Responsible: Regina

import com.mealplanner.config.ApiConfig;
import com.mealplanner.entity.Recipe;
import com.mealplanner.exception.ApiException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class BrowseRecipeInteractor implements BrowseRecipeInputBoundary {
    // get the recipe specifications from DataAccessInterface
    // instantiate the OutputData
    private final BrowseRecipeDataAccessInterface browseRecipeDataAccessObject;
    private final BrowseRecipeOutputBoundary browseRecipePresenter;

    public BrowseRecipeInteractor(BrowseRecipeDataAccessInterface browseRecipeDataAccessObject,
                                  BrowseRecipeOutputBoundary browseRecipePresenter) {
        this.browseRecipeDataAccessObject = Objects.requireNonNull(browseRecipeDataAccessObject, 
                "Data access object cannot be null");
        this.browseRecipePresenter = Objects.requireNonNull(browseRecipePresenter, 
                "Presenter cannot be null");
    }

    public void execute(BrowseRecipeInputData browseRecipeInputData) throws IOException {
        if (browseRecipeInputData == null) {
            browseRecipePresenter.presentError("Input data cannot be null");
            return;
        }
        
        if (browseRecipeInputData.getQuery() == null || browseRecipeInputData.getQuery().trim().isEmpty()) {
            browseRecipePresenter.presentError("Search query cannot be empty");
            return;
        }

        String apiKey = ApiConfig.getSpoonacularApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            browseRecipePresenter.presentError("API key is not configured. Please check your configuration.");
            return;
        }

        String recipeQuery = "query=" + browseRecipeInputData.getQuery();
        String numberOfRecipes = "number=" + browseRecipeInputData.getNumberOfRecipes();
        String url;
        
        if (browseRecipeInputData.getIncludedIngredients() == null || 
            browseRecipeInputData.getIncludedIngredients().trim().isEmpty()) {
            url = ApiConfig.getSpoonacularBaseUrl() + "/recipes/complexSearch?" + 
                  recipeQuery + "&" + numberOfRecipes + "&apiKey=" + apiKey;
        } else {
            String includedIngredients = "includeIngredients=" + browseRecipeInputData.getIncludedIngredients();
            url = ApiConfig.getSpoonacularBaseUrl() + "/recipes/complexSearch?" + 
                  recipeQuery + "&" + includedIngredients + "&" + numberOfRecipes + "&apiKey=" + apiKey;
        }

        try {
            String apiResponse = run(url);
            @SuppressWarnings("unchecked")
            List<Recipe> recipes = (List<Recipe>) browseRecipeDataAccessObject.searchRecipes(apiResponse);

            if (recipes == null || recipes.isEmpty()) {
                browseRecipePresenter.presentError("No recipes found, please try different wording " +
                        "in your search query" + 
                        (browseRecipeInputData.getIncludedIngredients() != null ? 
                         " or input different ingredients." : "."));
            } else {
                BrowseRecipeOutputData browseRecipeOutputData = new BrowseRecipeOutputData(recipes);
                browseRecipePresenter.presentRecipeDetails(browseRecipeOutputData);
            }
        } catch (ApiException e) {
            browseRecipePresenter.presentError("API error: " + e.getMessage());
        } catch (IOException e) {
            browseRecipePresenter.presentError("Network error: " + e.getMessage());
        }
    }

    private String run(String url) throws IOException, ApiException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new ApiException("API request failed with code: " + response.code());
            }
            if (response.body() == null) {
                throw new ApiException("API response body is null");
            }
            return response.body().string();
        }
    }
}
