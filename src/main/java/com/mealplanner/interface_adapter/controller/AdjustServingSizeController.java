package com.mealplanner.interface_adapter.controller;

import com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeInputBoundary;
import com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeInputData;
import com.mealplanner.util.StringUtil;
import java.util.Objects;

// Controller for adjusting recipe serving size - receives recipe and new serving count.
// Responsible: Eden

public class AdjustServingSizeController {
    private final AdjustServingSizeInputBoundary interactor;

    public AdjustServingSizeController(AdjustServingSizeInputBoundary interactor) {
        this.interactor = Objects.requireNonNull(interactor, "Interactor cannot be null");
    }

    public void execute(String recipeId, int newServingSize) {
        if (StringUtil.isNullOrEmpty(recipeId)) {
            return; // Let interactor handle validation
        }
        AdjustServingSizeInputData inputData = new AdjustServingSizeInputData(recipeId, newServingSize);
        interactor.execute(inputData);
    }
}
