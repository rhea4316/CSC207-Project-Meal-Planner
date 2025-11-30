package com.mealplanner.interface_adapter.controller;

import com.mealplanner.use_case.update_nutrition_goals.UpdateNutritionGoalsInputBoundary;
import com.mealplanner.use_case.update_nutrition_goals.UpdateNutritionGoalsInputData;

/**
 * Controller for updating nutrition goals.
 * Handles user input and invokes the use case interactor.
 * 
 * Responsible: Interface Adapter team
 */
public class UpdateNutritionGoalsController {
    
    private final UpdateNutritionGoalsInputBoundary interactor;
    
    public UpdateNutritionGoalsController(UpdateNutritionGoalsInputBoundary interactor) {
        this.interactor = interactor;
    }
    
    /**
     * Executes the update nutrition goals use case.
     * 
     * @param userId user ID
     * @param dailyCalories target daily calories
     * @param dailyProtein target daily protein (grams)
     * @param dailyCarbs target daily carbs (grams)
     * @param dailyFat target daily fat (grams)
     */
    public void execute(String userId, int dailyCalories, 
                       double dailyProtein, double dailyCarbs, double dailyFat) {
        UpdateNutritionGoalsInputData inputData = new UpdateNutritionGoalsInputData(
            userId, dailyCalories, dailyProtein, dailyCarbs, dailyFat
        );
        interactor.execute(inputData);
    }
}

