package com.mealplanner.use_case.manage_meal_plan.delete;

import com.mealplanner.entity.Schedule;
import com.mealplanner.entity.MealType;
import java.time.LocalDate;
import java.util.Objects;

// Main business logic for deleting a meal from the schedule.
// Responsible: Grace

public class DeleteMealInteractor implements DeleteMealInputBoundary {

    private final DeleteMealDataAccessInterface dataAccess;
    private final DeleteMealOutputBoundary presenter;

    public DeleteMealInteractor(DeleteMealDataAccessInterface dataAccess,
                             DeleteMealOutputBoundary presenter) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "Data access cannot be null");
        this.presenter = Objects.requireNonNull(presenter, "Presenter cannot be null");
    }

    @Override
    public void execute(DeleteMealInputData inputData) {
        if (inputData == null) {
            presenter.presentDeleteError("Input data cannot be null.");
            return;
        }

        // Get input values
        LocalDate date = inputData.getDate();
        MealType mealType = inputData.getMealType();

        // Validate inputs
        if (date == null) {
            presenter.presentDeleteError("Date cannot be null.");
            return;
        }
        if (mealType == null) {
            presenter.presentDeleteError("Meal type cannot be null.");
            return;
        }

        Schedule schedule = dataAccess.getUserSchedule();

        // Validate meal exists
        if (!schedule.hasMeal(date, mealType)) {
            presenter.presentDeleteError("No meal exists for " + mealType + " on " + date + ".");
            return;
        }

        // Remove meal from schedule
        schedule.removeMeal(date, mealType);

        // Save schedule
        dataAccess.saveSchedule(schedule);

        // Pass result to presenter
        DeleteMealOutputData outputData = new DeleteMealOutputData(schedule, "Meal has been deleted successfully.");
        presenter.presentDeleteSuccess(outputData);
    }
}
