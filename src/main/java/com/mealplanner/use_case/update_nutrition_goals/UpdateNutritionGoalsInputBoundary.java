package com.mealplanner.use_case.update_nutrition_goals;

/**
 * Input boundary interface for updating nutrition goals use case.
 * Defines the contract for the interactor.
 * 
 * Responsible: Use Case team
 */
public interface UpdateNutritionGoalsInputBoundary {
    /**
     * Executes the use case to update user's nutrition goals.
     * 
     * @param inputData contains userId and new nutrition goal values
     */
    void execute(UpdateNutritionGoalsInputData inputData);
}

