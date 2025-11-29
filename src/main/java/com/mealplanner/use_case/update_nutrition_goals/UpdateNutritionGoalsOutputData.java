package com.mealplanner.use_case.update_nutrition_goals;

import com.mealplanner.entity.NutritionGoals;

/**
 * Output data for updating nutrition goals use case.
 * Contains the updated nutrition goals.
 * 
 * Responsible: Use Case team
 */
public class UpdateNutritionGoalsOutputData {
    private final NutritionGoals nutritionGoals;
    
    public UpdateNutritionGoalsOutputData(NutritionGoals nutritionGoals) {
        this.nutritionGoals = nutritionGoals;
    }
    
    public NutritionGoals getNutritionGoals() {
        return nutritionGoals;
    }
}

