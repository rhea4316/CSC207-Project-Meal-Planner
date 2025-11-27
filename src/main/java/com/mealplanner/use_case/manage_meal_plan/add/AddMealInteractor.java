package com.mealplanner.use_case.manage_meal_plan.add;

import com.mealplanner.entity.Schedule;
import com.mealplanner.entity.MealType;
import java.time.LocalDate;
import com.mealplanner.exception.MealPlannerException;
import java.util.Objects;

// Main business logic for adding a meal to the schedule.
// Responsible: Grace

public class AddMealInteractor implements AddMealInputBoundary {

    private final AddMealDataAccessInterface dataAccess;
    private final AddMealOutputBoundary presenter;

    public AddMealInteractor(AddMealDataAccessInterface dataAccess,
                             AddMealOutputBoundary presenter) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "Data access cannot be null");
        this.presenter = Objects.requireNonNull(presenter, "Presenter cannot be null");
    }

    @Override
    public void execute(AddMealInputData inputData) {
        if (inputData == null) {
            presenter.presentAddError("Input data cannot be null.");
            return;
        }

        // Get input values
        LocalDate date = inputData.getDate();
        MealType mealType = inputData.getMealType();
        String recipeID = inputData.getRecipe();

        // Validate inputs
        if (date == null) {
            presenter.presentAddError("Date cannot be null.");
            return;
        }
        if (mealType == null) {
            presenter.presentAddError("Meal type cannot be null.");
            return;
        }
        if (recipeID == null || recipeID.trim().isEmpty()) {
            presenter.presentAddError("Recipe ID cannot be null or empty.");
            return;
        }

        Schedule schedule = dataAccess.getUserSchedule();

        // Check if the slot is available
        if (schedule.hasMeal(date, mealType)) {
            presenter.presentAddError("Meal slot already taken by " + mealType + " on " + date);
            return;
        }

        // Add meal to schedule
        try {
            schedule.addMeal(date, mealType, recipeID);
        } catch (MealPlannerException e) {
            presenter.presentAddError(e.getMessage());
            return;
        }

        // Save schedule
        dataAccess.saveSchedule(schedule);

        // Pass result to presenter
        AddMealOutputData outputData = new AddMealOutputData(schedule, "Meal has been added successfully.");
        presenter.presentAddSuccess(outputData);
    }
}
