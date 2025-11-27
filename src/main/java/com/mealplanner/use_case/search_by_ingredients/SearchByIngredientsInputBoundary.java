package com.mealplanner.use_case.search_by_ingredients;

// Input boundary interface defining the contract for searching recipes by ingredients.
// Responsible: Jerry

public interface SearchByIngredientsInputBoundary {
    void execute(SearchByIngredientsInputData inputData);
}
