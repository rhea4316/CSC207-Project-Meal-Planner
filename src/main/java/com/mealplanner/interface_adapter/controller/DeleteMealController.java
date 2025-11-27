package com.mealplanner.interface_adapter.controller;

import com.mealplanner.entity.MealType;
import java.time.LocalDate;

import com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealInputData;
import com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealInputBoundary;

// Controller for deleting meals from schedule - receives meal slot identifier and calls interactor.
// Responsible: Grace

public class DeleteMealController {
    private final DeleteMealInputBoundary interactor;

    public DeleteMealController(DeleteMealInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(String dateRaw, String mealTypeRaw) {
        // Convert to inputData
        LocalDate date = LocalDate.parse(dateRaw);
        MealType mealType = MealType.valueOf(mealTypeRaw.toUpperCase());

        DeleteMealInputData inputData = new DeleteMealInputData(date, mealType);

        // Call delete meal interactor
        interactor.execute(inputData);
    }
}
