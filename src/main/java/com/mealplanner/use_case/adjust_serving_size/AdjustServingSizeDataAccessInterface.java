package com.mealplanner.use_case.adjust_serving_size;

import com.mealplanner.entity.Recipe;
import com.mealplanner.exception.RecipeNotFoundException;

// Data access interface for retrieving recipe to adjust.
// Responsible: Eden (interface), Everyone (implementation)

public interface AdjustServingSizeDataAccessInterface {
    Recipe getRecipeById(String recipeId) throws RecipeNotFoundException;
}
