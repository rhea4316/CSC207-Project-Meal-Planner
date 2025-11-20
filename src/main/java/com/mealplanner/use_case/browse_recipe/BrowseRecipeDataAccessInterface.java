package com.mealplanner.use_case.browse_recipe;

// Data access interface for retrieving recipe details by ID.
// Responsible: Regina (interface), Everyone (implementation)
// TODO: Define method to get recipe by ID from API or database

import com.google.gson.JsonObject;
import com.mealplanner.entity.Recipe;

import java.io.IOException;
import java.util.List;

public interface BrowseRecipeDataAccessInterface {
    // get recipe ID/specifications from InputData
    // use those to find corresponding recipes from API

    List searchRecipes(String apiResponse) throws IOException;

}
