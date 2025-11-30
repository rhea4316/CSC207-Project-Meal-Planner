package com.mealplanner.use_case.browse_recipe;

// Output boundary interface for presenting recipe details to the user.
// Responsible: Regina

/**
 * Output boundary for BrowseRecipe use case.
 */
public interface BrowseRecipeOutputBoundary {

    /**
     * Prepares success view for BrowseRecipe use case.
     * @param browseRecipeOutputData list of resulted recipes
     */
    void presentRecipeDetails(BrowseRecipeOutputData browseRecipeOutputData);

    /**
     * Prepares failure view for BrowseRecipe use case.
     * @param errorMessage failure message
     */
    void presentError(String errorMessage);

}
