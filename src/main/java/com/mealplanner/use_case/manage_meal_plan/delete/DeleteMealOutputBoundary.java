package com.mealplanner.use_case.manage_meal_plan.delete;

public interface DeleteMealOutputBoundary {
    void presentDeleteSuccess(DeleteMealOutputData outputData);
    void presentDeleteError(String errorMessage);
}
