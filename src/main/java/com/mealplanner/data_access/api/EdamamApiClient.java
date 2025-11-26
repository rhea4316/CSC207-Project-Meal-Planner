package com.mealplanner.data_access.api;

// API client for Edamam API - alternative source for nutrition analysis.
// Responsible: Everyone (API integration shared responsibility)

import com.mealplanner.config.ApiConfig;
import com.mealplanner.entity.NutritionInfo;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class EdamamApiClient {
    
    private final OkHttpClient client;
    
    public EdamamApiClient() {
        this.client = new OkHttpClient();
    }
    
    public EdamamApiClient(OkHttpClient client) {
        this.client = client;
    }
    
    /**
     * Analyze nutrition information for a food item or recipe.
     * 
     * @param foodItem Name or description of the food item
     * @return NutritionInfo entity
     * @throws IOException if API call fails
     */
    public NutritionInfo analyzeNutrition(String foodItem) throws IOException {
        if (foodItem == null || foodItem.trim().isEmpty()) {
            throw new IllegalArgumentException("Food item cannot be null or empty");
        }
        
        String baseUrl = ApiConfig.getEdamamBaseUrl();
        String appId = ApiConfig.getEdamamAppId();
        String appKey = ApiConfig.getEdamamAppKey();
        
        if (appId == null || appId.trim().isEmpty() || appKey == null || appKey.trim().isEmpty()) {
            throw new IOException("Edamam API is not configured (missing app ID or key)");
        }
        
        // URL encode the food item
        String encodedFoodItem = URLEncoder.encode(foodItem.trim(), StandardCharsets.UTF_8);
        
        // Use Edamam Food Database API
        String url = baseUrl + "/api/food-database/v2/parser?ingr=" + encodedFoodItem 
                    + "&app_id=" + appId + "&app_key=" + appKey;
        
        String apiResponse = makeRequest(url);
        JSONObject json = new JSONObject(apiResponse);
        
        return parseNutritionFromEdamamJson(json);
    }
    
    /**
     * Get meal planning suggestions.
     * 
     * @param calories Target daily calories
     * @return Meal plan data as JSON string
     * @throws IOException if API call fails
     */
    public String getMealPlan(int calories) throws IOException {
        if (calories <= 0) {
            throw new IllegalArgumentException("Calories must be greater than zero");
        }
        
        String baseUrl = ApiConfig.getEdamamBaseUrl();
        String appId = ApiConfig.getEdamamAppId();
        String appKey = ApiConfig.getEdamamAppKey();
        
        if (appId == null || appId.trim().isEmpty() || appKey == null || appKey.trim().isEmpty()) {
            throw new IOException("Edamam API is not configured (missing app ID or key)");
        }
        
        // Use Edamam Meal Planner API
        String url = baseUrl + "/api/meal-planner/v1/plans?calories=" + calories 
                    + "&app_id=" + appId + "&app_key=" + appKey;
        
        return makeRequest(url);
    }
    
    /**
     * Helper method to make API requests.
     * 
     * @param url The full API URL
     * @return Response body as string
     * @throws IOException if request fails
     */
    private String makeRequest(String url) throws IOException {
        if (!ApiConfig.isEdamamConfigured()) {
            throw new IOException("Edamam API is not configured (missing app ID or key)");
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
     * Parse NutritionInfo from Edamam API JSON response.
     */
    private NutritionInfo parseNutritionFromEdamamJson(JSONObject json) {
        int calories = 0;
        double protein = 0.0;
        double carbs = 0.0;
        double fat = 0.0;
        
        // Edamam API structure: nutrients array with label and quantity
        if (json.has("hints") && json.getJSONArray("hints").length() > 0) {
            JSONObject firstHint = json.getJSONArray("hints").getJSONObject(0);
            if (firstHint.has("food") && firstHint.getJSONObject("food").has("nutrients")) {
                JSONObject nutrients = firstHint.getJSONObject("food").getJSONObject("nutrients");
                
                calories = (int) Math.round(nutrients.optDouble("ENERC_KCAL", 0.0));
                protein = nutrients.optDouble("PROCNT", 0.0);
                carbs = nutrients.optDouble("CHOCDF", 0.0);
                fat = nutrients.optDouble("FAT", 0.0);
            }
        }
        
        return new NutritionInfo(calories, protein, carbs, fat);
    }
}
