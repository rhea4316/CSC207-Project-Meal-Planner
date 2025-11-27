package com.mealplanner.data_access.api;

// API client for Spoonacular API - handles recipe search and nutrition data retrieval.
// Responsible: Everyone (API integration shared responsibility)

import com.mealplanner.config.ApiConfig;
import com.mealplanner.entity.NutritionInfo;
import com.mealplanner.entity.Recipe;
import com.mealplanner.exception.ApiException;
import com.mealplanner.util.StringUtil;
import com.mealplanner.util.NumberUtil;
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
        
        if (StringUtil.isNullOrEmpty(apiKey)) {
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
        
        if (StringUtil.isNullOrEmpty(apiKey)) {
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
        if (StringUtil.isNullOrEmpty(recipeId)) {
            throw new IOException("Recipe ID cannot be null or empty");
        }
        int id = NumberUtil.parseInt(recipeId, -1);
        if (id < 0) {
            throw new IOException("Invalid recipe ID format: " + recipeId);
        }
        return getRecipeById(id);
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
        
        if (StringUtil.isNullOrEmpty(apiKey)) {
            throw new IOException("Spoonacular API key is not configured");
        }
        
        // URL encode parameters
        String encodedQuery = URLEncoder.encode(StringUtil.safeTrim(query), StandardCharsets.UTF_8);
        
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("/recipes/complexSearch?");
        urlBuilder.append("query=").append(encodedQuery);
        urlBuilder.append("&number=").append(numberOfRecipes);
        
        String trimmedIngredients = StringUtil.safeTrim(includedIngredients);
        if (!StringUtil.isNullOrEmpty(trimmedIngredients)) {
            String encodedIngredients = URLEncoder.encode(trimmedIngredients, StandardCharsets.UTF_8);
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
        
        if (StringUtil.isNullOrEmpty(apiKey)) {
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
            throw new IOException("Spoonacular API key is not configured. " +
                    "Please set it in config/api_keys.properties or as environment variable SPOONACULAR_API_KEY");
        }
        
        Request request = new Request.Builder().url(url).build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                int code = response.code();
                String errorMessage = "API request failed with code: " + code;
                
                if (code == 401) {
                    errorMessage += " (Unauthorized). " +
                            "This usually means your API key is invalid, expired, or not set correctly. " +
                            "Please check your Spoonacular API key in config/api_keys.properties or environment variable SPOONACULAR_API_KEY";
                } else if (code == 402) {
                    errorMessage += " (Payment Required). " +
                            "Your API quota may have been exceeded. Please check your Spoonacular account.";
                } else if (code == 429) {
                    errorMessage += " (Too Many Requests). " +
                            "You have exceeded the rate limit. Please wait before making more requests.";
                }
                
                throw new IOException(errorMessage);
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
     * @throws IOException if parsing fails
     */
    private Recipe parseRecipeFromJson(JSONObject json) throws IOException {
        try {
            return ApiResponseParser.parseRecipe(json);
        } catch (ApiException e) {
            throw new IOException("Failed to parse recipe from JSON: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parse NutritionInfo from JSON object.
     * 
     * @param json JSONObject from API response
     * @return NutritionInfo entity
     * @throws IOException if parsing fails
     */
    private NutritionInfo parseNutritionFromJson(JSONObject json) throws IOException {
        try {
            return ApiResponseParser.parseNutritionInfo(json);
        } catch (ApiException e) {
            throw new IOException("Failed to parse nutrition info from JSON: " + e.getMessage(), e);
        }
    }
}
