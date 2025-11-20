package com.mealplanner.use_case.browse_recipe;

// Main business logic for browsing recipe details and viewing ingredients.
// Responsible: Regina

import com.google.gson.JsonObject;
import com.mealplanner.data_access.database.BrowseRecipeAPIParser;
import com.mealplanner.entity.Recipe;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;

public class BrowseRecipeInteractor implements BrowseRecipeInputBoundary {
    // get the recipe specifications from DataAccessInterface
    // instantiate the OutputData
    private final BrowseRecipeDataAccessInterface browseRecipeDataAccessObject;
    private final BrowseRecipeOutputBoundary browseRecipePresenter;

    public BrowseRecipeInteractor(BrowseRecipeDataAccessInterface browseRecipeDataAccessObject,
                                  BrowseRecipeOutputBoundary browseRecipePresenter) {
        this.browseRecipeDataAccessObject = browseRecipeDataAccessObject;
        this.browseRecipePresenter= browseRecipePresenter;
    }

    public void execute(BrowseRecipeInputData browseRecipeInputData) throws IOException {

        String recipeQuery = "query=" + browseRecipeInputData.getQuery();
        String includedIngredients = "includeIngredients=" + browseRecipeInputData.getIncludedIngredients();
        String numberOfRecipes = "number=" + browseRecipeInputData.getNumberOfRecipes();

        if (browseRecipeInputData.getIncludedIngredients() == null) {
            String url = "https://api.spoonacular.com/recipes/complexSearch?" + recipeQuery + "&" + numberOfRecipes
                    + "&apiKey=1568ebc937304b8991ea3a1a003e1e40";
            String apiResponse = run(url);
            List<Recipe> recipes = browseRecipeDataAccessObject.searchRecipes(apiResponse);

            if (recipes.size() == 0) {
                browseRecipePresenter.presentError("No recipes found, please try different wording " +
                        "in your search query.");
            } else {
                BrowseRecipeOutputData browseRecipeOutputData = new BrowseRecipeOutputData(recipes);
                browseRecipePresenter.presentRecipeDetails(browseRecipeOutputData);
            }
        }

        else {
            String url = "https://api.spoonacular.com/recipes/complexSearch?" + recipeQuery + "&" +
                    includedIngredients + "&" + numberOfRecipes + "&apiKey=1568ebc937304b8991ea3a1a003e1e40";
            String apiResponse = run(url);
            List<Recipe> recipes = browseRecipeDataAccessObject.searchRecipes(apiResponse);

            if (recipes.size() == 0) {
                browseRecipePresenter.presentError("No recipes found, please try different wording " +
                        "in your search query or input different ingredients.");
            } else {
                BrowseRecipeOutputData browseRecipeOutputData = new BrowseRecipeOutputData(recipes);
                browseRecipePresenter.presentRecipeDetails(browseRecipeOutputData);
            }
        }

    }

    String run(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}
