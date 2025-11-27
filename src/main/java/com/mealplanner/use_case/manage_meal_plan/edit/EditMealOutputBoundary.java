package com.mealplanner.use_case.manage_meal_plan.edit;

public interface EditMealOutputBoundary {
    void presentEditSuccess(EditMealOutputData outputData);
    void presentEditError(String errorMessage);
}
