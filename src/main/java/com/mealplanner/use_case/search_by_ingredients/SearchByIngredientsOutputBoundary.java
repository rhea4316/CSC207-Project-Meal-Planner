package com.mealplanner.use_case.search_by_ingredients;

// Output boundary interface for presenting search results to the user.
// Responsible: Jerry

public interface SearchByIngredientsOutputBoundary {
    void presentRecipes(SearchByIngredientsOutputData outputData);
    void presentError(String errorMessage);
}
