package com.mealplanner.interface_adapter.controller;

import com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeInputBoundary;
import com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeInputData;

// Controller for adjusting recipe serving size - receives recipe and new serving count.
// Responsible: Eden

public class AdjustServingSizeController {
    private final AdjustServingSizeInputBoundary interactor;

    public AdjustServingSizeController(AdjustServingSizeInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(String recipeId, int newServingSize) {
        AdjustServingSizeInputData inputData = new AdjustServingSizeInputData(recipeId, newServingSize);
        interactor.execute(inputData);
    }
}
