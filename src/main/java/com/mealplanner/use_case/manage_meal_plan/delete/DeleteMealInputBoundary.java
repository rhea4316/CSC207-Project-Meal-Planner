package com.mealplanner.use_case.manage_meal_plan.delete;

// Input boundary interface for deleting a meal from the schedule.
// Responsible: Grace
// TODO: Define execute method that takes DeleteMealInputData (date, meal type) as parameter

public interface DeleteMealInputBoundary {
    void execute(DeleteMealInputData inputData);
}
