package com.mealplanner.use_case.manage_meal_plan.add;

import com.mealplanner.entity.MealType;
import java.time.LocalDate;

// Data transfer object carrying meal slot information (date, meal type, recipe ID).
// Responsible: Grace

public class AddMealInputData {
    private final LocalDate date;
    private final MealType mealType;
    private final String recipe;

    public AddMealInputData(LocalDate date, MealType mealType, String recipe) {
        this.date = date;
        this.mealType = mealType;
        this.recipe = recipe;
    }

    public LocalDate getDate() {
        return date;
    }

    public MealType getMealType() {
        return mealType;
    }

    public String getRecipe() {
        return recipe;
    }

}
