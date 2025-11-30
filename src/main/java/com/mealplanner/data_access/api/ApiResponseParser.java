package com.mealplanner.data_access.api;

import com.mealplanner.entity.DietaryRestriction;
import com.mealplanner.entity.Ingredient;
import com.mealplanner.entity.NutritionInfo;
import com.mealplanner.entity.Recipe;
import com.mealplanner.exception.ApiException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ApiResponseParser {

    public static Recipe parseRecipe(JSONObject json) throws ApiException {
        try {
            String name = json.getString("title");
            String recipeId = String.valueOf(json.getInt("id"));
            
            List<String> ingredients = parseIngredientsFromJson(json.optJSONArray("extendedIngredients"));
            String steps = parseStepsFromJson(json);
            
            int servingSize = json.optInt("servings", 1);
            if (servingSize <= 0) {
                servingSize = 1;
            }
            
            NutritionInfo nutritionInfo = null;
            if (json.has("nutrition")) {
                JSONObject nutritionJson = json.getJSONObject("nutrition");
                nutritionInfo = parseNutritionInfo(nutritionJson);
            }
            
            Integer cookTimeMinutes = null;
            if (json.has("readyInMinutes")) {
                int readyTime = json.getInt("readyInMinutes");
                if (readyTime > 0) {
                    cookTimeMinutes = readyTime;
                }
            }
            
            List<DietaryRestriction> dietaryRestrictions = parseDietaryRestrictions(json.optJSONArray("diets"));
            
            // Parse image URL
            String imageUrl = null;
            if (json.has("image")) {
                imageUrl = json.optString("image", null);
            }
            
            return new Recipe(name, ingredients, steps, servingSize, 
                            nutritionInfo, cookTimeMinutes, dietaryRestrictions, imageUrl, recipeId);
        } catch (Exception e) {
            throw new ApiException("Failed to parse recipe from JSON: " + e.getMessage(), e);
        }
    }

    public static NutritionInfo parseNutritionInfo(JSONObject json) throws ApiException {
        try {
            int calories = 0;
            double protein = 0.0;
            double carbs = 0.0;
            double fat = 0.0;
            
            if (json.has("calories")) {
                calories = json.optInt("calories", 0);
            }
            
            if (json.has("protein")) {
                protein = json.optDouble("protein", 0.0);
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
            }
            
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
        } catch (Exception e) {
            throw new ApiException("Failed to parse nutrition info from JSON: " + e.getMessage(), e);
        }
    }

    public static Ingredient parseIngredient(JSONObject json) throws ApiException {
        try {
            String name = json.optString("nameClean", json.optString("name", ""));
            if (name == null || name.trim().isEmpty()) {
                name = "Unknown Ingredient";
            }
            
            double quantity = json.optDouble("amount", 0.0);
            String unit = json.optString("unit", "");
            
            JSONObject nutrition = json.optJSONObject("nutrition");
            int calories = 0;
            double protein = 0.0;
            double carbs = 0.0;
            double fat = 0.0;
            
            if (nutrition != null) {
                calories = nutrition.optInt("calories", 0);
                protein = nutrition.optDouble("protein", 0.0);
                carbs = nutrition.optDouble("carbs", 0.0);
                fat = nutrition.optDouble("fat", 0.0);
            }
            
            return new Ingredient(name, quantity, unit, calories, protein, carbs, fat);
        } catch (IllegalArgumentException e) {
            throw new ApiException("Invalid ingredient data: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ApiException("Failed to parse ingredient from JSON: " + e.getMessage(), e);
        }
    }

    private static List<String> parseIngredientsFromJson(JSONArray ingredientsArray) {
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

    private static String parseStepsFromJson(JSONObject json) {
        // First, try to get analyzedInstructions (structured format from Spoonacular API)
        if (json.has("analyzedInstructions")) {
            JSONArray analyzedInstructions = json.optJSONArray("analyzedInstructions");
            if (analyzedInstructions != null && analyzedInstructions.length() > 0) {
                JSONObject instructionSet = analyzedInstructions.getJSONObject(0);
                if (instructionSet.has("steps")) {
                    JSONArray steps = instructionSet.getJSONArray("steps");
                    List<String> stepList = new ArrayList<>();
                    for (int i = 0; i < steps.length(); i++) {
                        JSONObject step = steps.getJSONObject(i);
                        String stepText = step.optString("step", "");
                        if (!stepText.isEmpty()) {
                            stepList.add(stepText);
                        }
                    }
                    if (!stepList.isEmpty()) {
                        return String.join("\n", stepList);
                    }
                }
            }
        }
        
        // Fallback to plain instructions field
        if (json.has("instructions")) {
            String instructions = json.optString("instructions", "");
            if (!instructions.isEmpty()) {
                // Remove HTML tags if present (Spoonacular sometimes returns HTML)
                String cleaned = instructions.replaceAll("<[^>]+>", "").trim();
                
                // Split by period followed by space (most common pattern)
                // This will split "Step 1. Step 2. Step 3." into separate steps
                String[] sentences = cleaned.split("\\.\\s+");
                
                List<String> stepList = new ArrayList<>();
                for (String sentence : sentences) {
                    String trimmed = sentence.trim();
                    if (!trimmed.isEmpty()) {
                        // Add period back if it was removed by split (except for last sentence)
                        if (!trimmed.endsWith(".") && !trimmed.endsWith("!") && !trimmed.endsWith("?")) {
                            trimmed += ".";
                        }
                        stepList.add(trimmed);
                    }
                }
                
                // If we successfully split into multiple steps, join with newlines
                if (stepList.size() > 1) {
                    return String.join("\n", stepList);
                }
                
                // If splitting didn't create multiple steps, check if it's already newline-separated
                if (cleaned.contains("\n")) {
                    return cleaned;
                }
                
                // If all else fails, return as-is (single long instruction)
                return cleaned;
            }
        }
        
        if (json.has("sourceUrl")) {
            return json.optString("sourceUrl", "");
        }
        
        return "See source URL for instructions";
    }

    private static List<DietaryRestriction> parseDietaryRestrictions(JSONArray dietsArray) {
        List<DietaryRestriction> restrictions = new ArrayList<>();
        if (dietsArray != null) {
            for (int i = 0; i < dietsArray.length(); i++) {
                String diet = dietsArray.getString(i);
                try {
                    DietaryRestriction restriction = DietaryRestriction.fromString(diet);
                    restrictions.add(restriction);
                } catch (IllegalArgumentException e) {
                }
            }
        }
        return restrictions;
    }

    private ApiResponseParser() {
    }
}
