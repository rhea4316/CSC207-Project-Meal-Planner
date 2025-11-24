package com.mealplanner.use_case.browse_recipe;

// Output boundary interface for presenting recipe details to the user.
// Responsible: Regina

public interface BrowseRecipeOutputBoundary {
    void presentRecipeDetails(BrowseRecipeOutputData browseRecipeOutputData);

    void presentError(String errorMessage);

}
