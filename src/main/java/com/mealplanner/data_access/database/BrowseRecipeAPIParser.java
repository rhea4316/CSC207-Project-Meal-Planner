package com.mealplanner.data_access.database;

import com.mealplanner.entity.Recipe;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeDataAccessInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BrowseRecipeAPIParser implements BrowseRecipeDataAccessInterface {
    //would probably need a getrecipe/list of recipes method:
    // i.e. input search filters or recipe name and then the method would call the api and return the parsed information
//    private String query;
////    private String includedIngredients;
//    private int numberOfRecipes;
//
//    public BrowseRecipeAPIParser(String query, int numberOfRecipes) {
//        this.query = query;
////        this.includedIngredients = includedIngredients;
//        this.numberOfRecipes = numberOfRecipes;
//    }
    private final OkHttpClient client;

    public BrowseRecipeAPIParser(OkHttpClient okHttpClient) {
        this.client = okHttpClient;
    }


    @Override
    public List searchRecipes(String apiResponse) throws IOException {
        List<Recipe> recipes = new ArrayList();
        JSONObject jsonBody = new JSONObject(apiResponse);
        JSONArray jsonArray = jsonBody.getJSONArray("recipes");

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject currentRecipe = jsonArray.getJSONObject(i);
            int currentRecipeId = currentRecipe.getInt("id");

            ArrayList<String> ingredients = getIngredients(currentRecipeId);
            String steps = getSteps(currentRecipeId);
            int servingSize = getServingSize(currentRecipeId);

            Recipe recipe = new Recipe(currentRecipe.getString("title"), ingredients, steps, servingSize);
            recipes.add(recipe);

        }
        return recipes;
    }

    private ArrayList<String> getIngredients(int recipeId) throws IOException {
        String url = "https://api.spoonacular.com/recipes/" + recipeId + "/information?includeNutrition=false";
        String apiResponse = run(url);
        ArrayList<String> ingredients = new ArrayList<>();
        JSONObject jsonBody = new JSONObject(apiResponse);
        JSONArray extendedIngredients = jsonBody.getJSONArray("extendedIngredients");

        for (int i = 0; i < extendedIngredients.length(); i++) {
            JSONObject currentIngredient = extendedIngredients.getJSONObject(i);
            ingredients.add(currentIngredient.getString("nameClean"));
        }

        return ingredients;
    }

    private int getServingSize(int recipeId) throws IOException {
        String url = "https://api.spoonacular.com/recipes/" + recipeId + "/information?includeNutrition=false";
        String apiResponse = run(url);
        JSONObject jsonBody = new JSONObject(apiResponse);
        return jsonBody.getInt("servings");
    }

    private String getSteps(int recipeId) throws IOException {
        String url = "https://api.spoonacular.com/recipes/" + recipeId + "/information?includeNutrition=false";
        String apiResponse = run(url);
        JSONObject jsonBody = new JSONObject(apiResponse);
        return jsonBody.getString("sourceUrl");
    }

    private String run(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}
