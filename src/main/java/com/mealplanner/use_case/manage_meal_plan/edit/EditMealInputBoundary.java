package com.mealplanner.use_case.manage_meal_plan.edit;

// Input boundary interface for editing an existing meal in the schedule.
// Responsible: Grace
// TODO: Define execute method that takes EditMealInputData (date, meal type, new recipe) as parameter

public interface EditMealInputBoundary {
    void execute(EditMealInputData inputData);
}
