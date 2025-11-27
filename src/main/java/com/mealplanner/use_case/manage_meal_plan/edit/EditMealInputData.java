package com.mealplanner.use_case.manage_meal_plan.edit;

import com.mealplanner.entity.MealType;
import java.time.LocalDate;

// Data transfer object carrying meal edit information.
// Responsible: Grace

public class EditMealInputData {
    private final LocalDate date;
    private final MealType mealType;
    private final String recipe;

    public EditMealInputData(LocalDate date, MealType mealType, String recipe) {
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
