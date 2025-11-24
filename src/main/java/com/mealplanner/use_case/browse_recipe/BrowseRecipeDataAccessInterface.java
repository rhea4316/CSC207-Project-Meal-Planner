package com.mealplanner.use_case.browse_recipe;

// Data access interface for retrieving recipe details by ID.
// Responsible: Regina (interface), Everyone (implementation)

import com.mealplanner.entity.Recipe;

import java.io.IOException;
import java.util.List;

public interface BrowseRecipeDataAccessInterface {
    // get recipe ID/specifications from InputData
    // use those to find corresponding recipes from API

    /**
     * Search for recipes based on input criteria.
     * 
     * @param inputData Search criteria (query, number of recipes, optional ingredients)
     * @return List of recipes matching the search criteria
     * @throws IOException if API call fails
     */
    List<Recipe> searchRecipes(BrowseRecipeInputData inputData) throws IOException;

}
