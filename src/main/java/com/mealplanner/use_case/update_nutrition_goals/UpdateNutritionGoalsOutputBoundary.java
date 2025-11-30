package com.mealplanner.use_case.update_nutrition_goals;

/**
 * Output boundary interface for updating nutrition goals use case.
 * Defines the contract for the presenter.
 * 
 * Responsible: Use Case team
 */
public interface UpdateNutritionGoalsOutputBoundary {
    /**
     * Presents success result after updating nutrition goals.
     * 
     * @param outputData contains the updated nutrition goals
     */
    void presentSuccess(UpdateNutritionGoalsOutputData outputData);
    
    /**
     * Presents error message if update failed.
     * 
     * @param errorMessage error description
     */
    void presentError(String errorMessage);
}

