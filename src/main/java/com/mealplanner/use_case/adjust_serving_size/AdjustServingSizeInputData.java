package com.mealplanner.use_case.adjust_serving_size;

// Data transfer object carrying recipe ID and new serving size from user.
// Responsible: Eden

public class AdjustServingSizeInputData {
    private final String recipeId;
    private final int newServingSize;

    public AdjustServingSizeInputData(String recipeId, int newServingSize) {
        this.recipeId = recipeId;
        this.newServingSize = newServingSize;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public int getNewServingSize() {
        return newServingSize;
    }
}
