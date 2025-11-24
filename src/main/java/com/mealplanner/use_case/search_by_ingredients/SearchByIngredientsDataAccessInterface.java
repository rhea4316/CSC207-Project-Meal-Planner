package com.mealplanner.use_case.search_by_ingredients;

// Data access interface for retrieving recipes matching given ingredients.
// Responsible: Jerry (interface), Everyone (implementation via SpoonacularApiClient)

import com.mealplanner.entity.Recipe;

import java.io.IOException;
import java.util.List;

public interface SearchByIngredientsDataAccessInterface {
    List<Recipe> searchByIngredients(List<String> ingredients) throws IOException;
}
