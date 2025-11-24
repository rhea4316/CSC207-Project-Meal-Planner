package com.mealplanner.data_access.api;

// API client for Spoonacular API - handles recipe search and nutrition data retrieval.
// Responsible: Everyone (API integration shared responsibility)

import com.mealplanner.config.ApiConfig;
import com.mealplanner.entity.DietaryRestriction;
import com.mealplanner.entity.NutritionInfo;
import com.mealplanner.entity.Recipe;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SpoonacularApiClient {
    
    private final OkHttpClient client;
    
    public SpoonacularApiClient() {
        this.client = new OkHttpClient();
    }
    
    public SpoonacularApiClient(OkHttpClient client) {
        this.client = client;
    }
    
    /**
     * Search for recipes by ingredients.
     * 
     * @param ingredients List of ingredient names
     * @return List of recipes matching the ingredients
     * @throws IOException if API call fails
     */
    public List<Recipe> searchByIngredients(List<String> ingredients) throws IOException {
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("Ingredients list cannot be null or empty");
        }
        
        String baseUrl = ApiConfig.getSpoonacularBaseUrl();
        String apiKey = ApiConfig.getSpoonacularApiKey();
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IOException("Spoonacular API key is not configured");
        }
        
        // Join ingredients with comma and URL encode
        String ingredientsParam = String.join(",", ingredients);
        String encodedIngredients = URLEncoder.encode(ingredientsParam, StandardCharsets.UTF_8);
        int maxResults = ApiConfig.getSpoonacularMaxResults();
        
        String url = baseUrl + "/recipes/findByIngredients?ingredients=" + encodedIngredients 
                    + "&number=" + maxResults + "&apiKey=" + apiKey;
        
        String apiResponse = makeRequest(url);
        JSONArray jsonArray = new JSONArray(apiResponse);
        
        List<Recipe> recipes = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject recipeJson = jsonArray.getJSONObject(i);
            int recipeId = recipeJson.getInt("id");
            
            // Get full recipe details
            try {
                Recipe fullRecipe = getRecipeById(recipeId);
                recipes.add(fullRecipe);
            } catch (IOException e) {
                // Skip recipes that fail to load, continue with others
            }
        }
        
        return recipes;
    }
    
    /**
     * Get a recipe by its ID.
     * 
     * @param recipeId The Spoonacular recipe ID
     * @return Recipe entity
     * @throws IOException if API call fails or recipe not found
     */
    public Recipe getRecipeById(int recipeId) throws IOException {
        String baseUrl = ApiConfig.getSpoonacularBaseUrl();
        String apiKey = ApiConfig.getSpoonacularApiKey();
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IOException("Spoonacular API key is not configured");
        }
        
        String url = baseUrl + "/recipes/" + recipeId + "/information?includeNutrition=true&apiKey=" + apiKey;
        String apiResponse = makeRequest(url);
        
        JSONObject json = new JSONObject(apiResponse);
        return parseRecipeFromJson(json);
    }
    
    /**
     * Get a recipe by its ID (String version for compatibility).
     * 
     * @param recipeId The Spoonacular recipe ID as String
     * @return Recipe entity
     * @throws IOException if API call fails or recipe not found
     */
    public Recipe getRecipeById(String recipeId) throws IOException {
        try {
            int id = Integer.parseInt(recipeId);
            return getRecipeById(id);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid recipe ID format: " + recipeId, e);
        }
    }
    
    /**
     * Search for recipes using complex search with query and optional ingredients.
     * 
     * @param query Search query string
     * @param numberOfRecipes Number of recipes to return
     * @param includedIngredients Optional comma-separated list of ingredients to include
     * @return JSON response string from API
     * @throws IOException if API call fails
     */
    public String complexSearch(String query, int numberOfRecipes, String includedIngredients) throws IOException {
        String baseUrl = ApiConfig.getSpoonacularBaseUrl();
        String apiKey = ApiConfig.getSpoonacularApiKey();
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IOException("Spoonacular API key is not configured");
        }
        
        // URL encode parameters
        String encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
        
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("/recipes/complexSearch?");
        urlBuilder.append("query=").append(encodedQuery);
        urlBuilder.append("&number=").append(numberOfRecipes);
        
        if (includedIngredients != null && !includedIngredients.trim().isEmpty()) {
            String encodedIngredients = URLEncoder.encode(includedIngredients.trim(), StandardCharsets.UTF_8);
            urlBuilder.append("&includeIngredients=").append(encodedIngredients);
        }
        
        urlBuilder.append("&apiKey=").append(apiKey);
        
        return makeRequest(urlBuilder.toString());
    }
    
    /**
     * Get nutrition information for a recipe.
     * 
     * @param recipeId The Spoonacular recipe ID
     * @return NutritionInfo entity
     * @throws IOException if API call fails
     */
    public NutritionInfo getNutritionInfo(int recipeId) throws IOException {
        String baseUrl = ApiConfig.getSpoonacularBaseUrl();
        String apiKey = ApiConfig.getSpoonacularApiKey();
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IOException("Spoonacular API key is not configured");
        }
        
        String url = baseUrl + "/recipes/" + recipeId + "/nutritionWidget.json?apiKey=" + apiKey;
        String apiResponse = makeRequest(url);
        
        JSONObject json = new JSONObject(apiResponse);
        return parseNutritionFromJson(json);
    }
    
    /**
     * Helper method to make API requests.
     * 
     * @param url The full API URL
     * @return Response body as string
     * @throws IOException if request fails
     */
    private String makeRequest(String url) throws IOException {
        if (!ApiConfig.isSpoonacularConfigured()) {
            throw new IOException("Spoonacular API key is not configured");
        }
        
        Request request = new Request.Builder().url(url).build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API request failed with code: " + response.code());
            }
            if (response.body() == null) {
                throw new IOException("API response body is null");
            }
            return response.body().string();
        }
    }
    
    /**
     * Parse a Recipe from Spoonacular API JSON response.
     * 
     * @param json JSONObject from API response
     * @return Recipe entity
     */
    private Recipe parseRecipeFromJson(JSONObject json) {
        String name = json.getString("title");
        String recipeId = String.valueOf(json.getInt("id"));
        
        // Parse ingredients
        List<String> ingredients = parseIngredientsFromJson(json.optJSONArray("extendedIngredients"));
        
        // Parse steps/instructions
        String steps = parseStepsFromJson(json);
        
        // Parse serving size
        int servingSize = json.optInt("servings", 1);
        if (servingSize <= 0) {
            servingSize = 1; // Default to 1 if invalid
        }
        
        // Parse nutrition info
        NutritionInfo nutritionInfo = null;
        if (json.has("nutrition")) {
            JSONObject nutritionJson = json.getJSONObject("nutrition");
            nutritionInfo = parseNutritionFromJson(nutritionJson);
        }
        
        // Parse cook time
        Integer cookTimeMinutes = null;
        if (json.has("readyInMinutes")) {
            int readyTime = json.getInt("readyInMinutes");
            if (readyTime > 0) {
                cookTimeMinutes = readyTime;
            }
        }
        
        // Parse dietary restrictions
        List<DietaryRestriction> dietaryRestrictions = parseDietaryRestrictions(json.optJSONArray("diets"));
        
        return new Recipe(name, ingredients, steps, servingSize, 
                         nutritionInfo, cookTimeMinutes, dietaryRestrictions, recipeId);
    }
    
    /**
     * Parse ingredients from JSON array.
     */
    private List<String> parseIngredientsFromJson(JSONArray ingredientsArray) {
        List<String> ingredients = new ArrayList<>();
        if (ingredientsArray != null) {
            for (int i = 0; i < ingredientsArray.length(); i++) {
                JSONObject ingredient = ingredientsArray.getJSONObject(i);
                String name = ingredient.optString("nameClean", ingredient.optString("name", ""));
                if (!name.isEmpty()) {
                    ingredients.add(name);
                }
            }
        }
        return ingredients;
    }
    
    /**
     * Parse cooking steps/instructions from JSON.
     */
    private String parseStepsFromJson(JSONObject json) {
        // Try to get instructions first
        if (json.has("instructions")) {
            String instructions = json.optString("instructions", "");
            if (!instructions.isEmpty()) {
                return instructions;
            }
        }
        
        // Fall back to sourceUrl if instructions not available
        if (json.has("sourceUrl")) {
            return json.optString("sourceUrl", "");
        }
        
        // Last resort: return empty string (will be validated by Recipe constructor)
        return "See source URL for instructions";
    }
    
    /**
     * Parse dietary restrictions from JSON array.
     */
    private List<DietaryRestriction> parseDietaryRestrictions(JSONArray dietsArray) {
        List<DietaryRestriction> restrictions = new ArrayList<>();
        if (dietsArray != null) {
            for (int i = 0; i < dietsArray.length(); i++) {
                String diet = dietsArray.getString(i);
                try {
                    DietaryRestriction restriction = DietaryRestriction.fromString(diet);
                    restrictions.add(restriction);
                } catch (IllegalArgumentException e) {
                    // Skip invalid dietary restrictions
                }
            }
        }
        return restrictions;
    }
    
    /**
     * Parse NutritionInfo from JSON object.
     */
    private NutritionInfo parseNutritionFromJson(JSONObject json) {
        int calories = 0;
        double protein = 0.0;
        double carbs = 0.0;
        double fat = 0.0;
        
        // Try different possible JSON structures
        if (json.has("calories")) {
            calories = json.optInt("calories", 0);
        }
        
        if (json.has("protein")) {
            protein = json.optDouble("protein", 0.0);
        } else if (json.has("protein")) {
            JSONObject proteinObj = json.optJSONObject("protein");
            if (proteinObj != null) {
                protein = proteinObj.optDouble("amount", 0.0);
            }
        }
        
        if (json.has("carbs")) {
            carbs = json.optDouble("carbs", 0.0);
        } else if (json.has("carbohydrates")) {
            JSONObject carbsObj = json.optJSONObject("carbohydrates");
            if (carbsObj != null) {
                carbs = carbsObj.optDouble("amount", 0.0);
            }
        }
        
        if (json.has("fat")) {
            fat = json.optDouble("fat", 0.0);
        } else if (json.has("fat")) {
            JSONObject fatObj = json.optJSONObject("fat");
            if (fatObj != null) {
                fat = fatObj.optDouble("amount", 0.0);
            }
        }
        
        // Try nutrients array structure (common in Spoonacular)
        if (json.has("nutrients")) {
            JSONArray nutrients = json.getJSONArray("nutrients");
            for (int i = 0; i < nutrients.length(); i++) {
                JSONObject nutrient = nutrients.getJSONObject(i);
                String name = nutrient.optString("name", "").toLowerCase();
                double amount = nutrient.optDouble("amount", 0.0);
                
                if (name.contains("calorie")) {
                    calories = (int) Math.round(amount);
                } else if (name.contains("protein")) {
                    protein = amount;
                } else if (name.contains("carbohydrate") || name.contains("carb")) {
                    carbs = amount;
                } else if (name.contains("fat")) {
                    fat = amount;
                }
            }
        }
        
        return new NutritionInfo(calories, protein, carbs, fat);
    }
}
