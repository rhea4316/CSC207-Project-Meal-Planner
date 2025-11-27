package com.mealplanner.use_case.manage_meal_plan.delete;

// Input boundary interface for deleting a meal from the schedule.
// Responsible: Grace

public interface DeleteMealInputBoundary {
    void execute(DeleteMealInputData inputData);
}
