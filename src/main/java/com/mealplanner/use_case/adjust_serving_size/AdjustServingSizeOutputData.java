package com.mealplanner.use_case.adjust_serving_size;

import com.mealplanner.entity.Recipe;

// Data transfer object carrying adjusted recipe with scaled ingredients and nutrition.
// Responsible: Eden

public class AdjustServingSizeOutputData {
    private final Recipe adjustedRecipe;

    public AdjustServingSizeOutputData(Recipe adjustedRecipe) {
        this.adjustedRecipe = adjustedRecipe;
    }

    public Recipe getAdjustedRecipe() {
        return adjustedRecipe;
    }
}
