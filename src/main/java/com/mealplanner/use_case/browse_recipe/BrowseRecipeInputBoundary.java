package com.mealplanner.use_case.browse_recipe;

// Input boundary interface for browsing recipe details and viewing ingredients.
// Responsible: Regina

import java.io.IOException;

/**
 * Input Boundary for BrowseRecipe use case.
 */
public interface BrowseRecipeInputBoundary {

    /**
     * Executes the BrowseRecipe use case.
     * @param browseRecipeInputData the input data carrying the search criteria
     * @throws IOException if the API fails
     */
    void execute(BrowseRecipeInputData browseRecipeInputData) throws IOException;

}
