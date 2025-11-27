package com.mealplanner.interface_adapter.controller;

import com.mealplanner.entity.MealType;
import java.time.LocalDate;

import com.mealplanner.use_case.manage_meal_plan.edit.EditMealInputData;
import com.mealplanner.use_case.manage_meal_plan.edit.EditMealInputBoundary;

// Controller for editing meals in schedule - receives meal modification data and calls interactor.
// Responsible: Grace

public class EditMealController {
    private final EditMealInputBoundary interactor;

    public EditMealController(EditMealInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(String dateRaw, String mealTypeRaw, String recipeID) {
        // Convert to inputData
        LocalDate date = LocalDate.parse(dateRaw);
        MealType mealType = MealType.valueOf(mealTypeRaw.toUpperCase());

        EditMealInputData inputData = new EditMealInputData(date, mealType, recipeID);

        // Call edit meal interactor
        interactor.execute(inputData);
    }
}
