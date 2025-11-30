package com.mealplanner.use_case.browse_recipe;

// Data access interface for retrieving recipe details by ID.
// Responsible: Regina (interface), Everyone (implementation)
import java.io.IOException;
import java.util.List;

import com.mealplanner.entity.Recipe;
import com.mealplanner.exception.RecipeNotFoundException;

public interface BrowseRecipeDataAccessInterface {
    // get recipe ID/specifications from InputData
    // use those to find corresponding recipes from API

    /**
     * Search for recipes based on input criteria.
     * 
     * @param inputData Search criteria (query, number of recipes, optional ingredients)
     * @return List of recipes matching the search criteria
     * @throws IOException if API call fails
     * @throws RecipeNotFoundException if recipe not found
     */
    List<Recipe> searchRecipes(BrowseRecipeInputData inputData) throws IOException, RecipeNotFoundException;

}
