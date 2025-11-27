package com.mealplanner.use_case.adjust_serving_size;

// Output boundary interface for presenting adjusted recipe with scaled ingredients.
// Responsible: Eden

public interface AdjustServingSizeOutputBoundary {
    void presentAdjustedRecipe(AdjustServingSizeOutputData outputData);
    void presentError(String errorMessage);
}
