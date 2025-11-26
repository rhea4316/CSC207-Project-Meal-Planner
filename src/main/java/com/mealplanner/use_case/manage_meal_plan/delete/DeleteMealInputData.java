package com.mealplanner.use_case.manage_meal_plan.delete;

import com.mealplanner.entity.MealType;
import java.time.LocalDate;

// Data transfer object carrying meal deletion information (date, meal type).
// Responsible: Grace
// TODO: Implement with date and meal type of the meal to delete

public class DeleteMealInputData {
    private final LocalDate date;
    private final MealType mealType;

    public DeleteMealInputData(LocalDate date, MealType mealType) {
        this.date = date;
        this.mealType = mealType;
    }

    public LocalDate getDate() {
        return date;
    }

    public MealType getMealType() {
        return mealType;
    }

}
