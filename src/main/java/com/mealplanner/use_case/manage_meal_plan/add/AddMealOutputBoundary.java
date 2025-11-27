package com.mealplanner.use_case.manage_meal_plan.add;

public interface AddMealOutputBoundary {
    void presentAddSuccess(AddMealOutputData outputData);
    void presentAddError(String errorMessage);
}
