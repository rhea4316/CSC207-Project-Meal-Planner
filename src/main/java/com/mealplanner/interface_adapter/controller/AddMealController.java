package com.mealplanner.interface_adapter.controller;

import com.mealplanner.entity.MealType;
import java.time.LocalDate;

import com.mealplanner.use_case.manage_meal_plan.add.AddMealInputData;
import com.mealplanner.use_case.manage_meal_plan.add.AddMealInputBoundary;

// Controller for adding meals to schedule - receives meal slot data and calls interactor.
// Responsible: Grace
// TODO: Implement execute method that converts date, meal type, and recipe selection to InputData and calls add meal interactor

public class AddMealController {
    private final AddMealInputBoundary interactor;

    public AddMealController(AddMealInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(String dateRaw, String mealTypeRaw, String recipeID) {
        // Convert to inputData
        LocalDate date = LocalDate.parse(dateRaw);
        MealType mealType = MealType.valueOf(mealTypeRaw.toUpperCase());

        AddMealInputData inputData = new AddMealInputData(date, mealType, recipeID);

        // Call add meal interactor
        interactor.execute(inputData);
    }
}
