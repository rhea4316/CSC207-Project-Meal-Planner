package com.mealplanner.use_case.manage_meal_plan.delete;

import com.mealplanner.entity.MealType;
import java.time.LocalDate;

// Data transfer object carrying meal deletion information (date, meal type).
// Responsible: Grace

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
