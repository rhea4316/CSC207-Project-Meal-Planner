package com.mealplanner.use_case.manage_meal_plan.edit;

import com.mealplanner.entity.Schedule;
import com.mealplanner.entity.MealType;
import java.time.LocalDate;
import java.util.Objects;

// Main business logic for editing an existing meal in the schedule.
// Responsible: Grace

public class EditMealInteractor implements EditMealInputBoundary {

    private final EditMealDataAccessInterface dataAccess;
    private final EditMealOutputBoundary presenter;

    public EditMealInteractor(EditMealDataAccessInterface dataAccess,
                             EditMealOutputBoundary presenter) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "Data access cannot be null");
        this.presenter = Objects.requireNonNull(presenter, "Presenter cannot be null");
    }

    @Override
    public void execute(EditMealInputData inputData) {
        if (inputData == null) {
            presenter.presentEditError("Input data cannot be null.");
            return;
        }

        // Get input values
        LocalDate date = inputData.getDate();
        MealType mealType = inputData.getMealType();
        String recipeID = inputData.getRecipe();

        // Validate inputs
        if (date == null) {
            presenter.presentEditError("Date cannot be null.");
            return;
        }
        if (mealType == null) {
            presenter.presentEditError("Meal type cannot be null.");
            return;
        }
        if (recipeID == null || recipeID.trim().isEmpty()) {
            presenter.presentEditError("Recipe ID cannot be null or empty.");
            return;
        }

        Schedule schedule = dataAccess.getUserSchedule();

        // Validate meal exists
        if (!schedule.hasMeal(date, mealType)) {
            presenter.presentEditError("No meal exists for " + mealType + " on " + date + ".");
            return;
        }

        // Update with new recipe
        try {
            schedule.updateMeal(date, mealType, recipeID);
        } catch (IllegalArgumentException e) {
            presenter.presentEditError(e.getMessage());
            return;
        }

        // Save schedule
        dataAccess.saveSchedule(schedule);

        // Pass result to presenter
        EditMealOutputData outputData = new EditMealOutputData(schedule, "Meal has been edited successfully.");
        presenter.presentEditSuccess(outputData);
    }
}
