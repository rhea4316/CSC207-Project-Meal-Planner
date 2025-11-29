package com.mealplanner.use_case.update_nutrition_goals;

import java.util.Objects;

/**
 * Input data for updating nutrition goals.
 * Encapsulates all data needed for the use case.
 * 
 * Responsible: Use Case team
 */
public class UpdateNutritionGoalsInputData {
    private final String userId;
    private final int dailyCalories;
    private final double dailyProtein;
    private final double dailyCarbs;
    private final double dailyFat;
    
    public UpdateNutritionGoalsInputData(String userId, int dailyCalories, 
                                        double dailyProtein, double dailyCarbs, double dailyFat) {
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        
        // 값 검증
        if (dailyCalories < 0 || dailyProtein < 0 || dailyCarbs < 0 || dailyFat < 0) {
            throw new IllegalArgumentException("Nutrition goals cannot be negative");
        }
        
        this.dailyCalories = dailyCalories;
        this.dailyProtein = dailyProtein;
        this.dailyCarbs = dailyCarbs;
        this.dailyFat = dailyFat;
    }
    
    // Getters
    public String getUserId() { 
        return userId; 
    }
    
    public int getDailyCalories() { 
        return dailyCalories; 
    }
    
    public double getDailyProtein() { 
        return dailyProtein; 
    }
    
    public double getDailyCarbs() { 
        return dailyCarbs; 
    }
    
    public double getDailyFat() { 
        return dailyFat; 
    }
}

