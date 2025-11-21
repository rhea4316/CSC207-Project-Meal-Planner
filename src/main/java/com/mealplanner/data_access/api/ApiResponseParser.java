package com.mealplanner.data_access.api;

// Utility class for parsing API JSON responses into entity objects.
// Responsible: Everyone (API integration shared responsibility)
// TODO: Implement helper methods to convert JSON strings to Recipe, Ingredient, and NutritionInfo entities

import com.google.gson.JsonObject;

public class ApiResponseParser {
    private  JsonObject apiResponse;

    public ApiResponseParser(JsonObject apiResponse) {this.apiResponse = apiResponse;}

    public void parse(){


    }

}
