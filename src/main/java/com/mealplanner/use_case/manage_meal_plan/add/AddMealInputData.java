package com.mealplanner.use_case.manage_meal_plan.add;

import com.mealplanner.entity.Recipe;
import com.mealplanner.entity.MealType;
import java.time.LocalDate;

// Data transfer object carrying meal slot information (date, meal type, recipe ID).
// Responsible: Grace
// TODO: Implement with date, meal type (breakfast/lunch/dinner), and recipe to add

public class AddMealInputData {
    private final LocalDate date;
    private final MealType mealType;
    private final Recipe recipe;

    public AddMealInputData(LocalDate date, MealType mealType, Recipe recipe) {
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

    public Recipe getRecipe() {
        return recipe;
    }

}
