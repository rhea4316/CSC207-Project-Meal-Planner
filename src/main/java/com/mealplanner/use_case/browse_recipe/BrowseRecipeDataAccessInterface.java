package com.mealplanner.use_case.browse_recipe;

// Data access interface for retrieving recipe details by ID.
// Responsible: Regina (interface), Everyone (implementation)
// TODO: Define method to get recipe by ID from API or database

import com.mealplanner.entity.Recipe;

public interface BrowseRecipeDataAccessInterface {
    // get recipe ID/specifications from InputData
    // use those to find corresponding recipes from API
    void getRecipeID(Recipe recipe);

    void getRecipeName(String name);
}
