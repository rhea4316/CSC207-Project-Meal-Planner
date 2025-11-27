package com.mealplanner.use_case.manage_meal_plan.delete;

import com.mealplanner.entity.Schedule;
import com.mealplanner.entity.MealType;
import com.mealplanner.use_case.manage_meal_plan.edit.EditMealOutputData;

import java.time.LocalDate;

// Main business logic for deleting a meal from the schedule.
// Responsible: Grace

public class DeleteMealInteractor implements DeleteMealInputBoundary {

    private final DeleteMealDataAccessInterface dataAccess;
    private final DeleteMealOutputBoundary presenter;

    public DeleteMealInteractor(DeleteMealDataAccessInterface dataAccess,
                             DeleteMealOutputBoundary presenter) {
        this.dataAccess = dataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(DeleteMealInputData inputData) {

        //Validate meal exists
        LocalDate date = inputData.getDate();
        MealType mealType = inputData.getMealType();
        Schedule schedule = dataAccess.getUserSchedule();

        if (!schedule.hasMeal(date, mealType)) {
            presenter.presentDeleteError("No meal exists for " + mealType + " on " + date + ".");
            return;
        }

        //Update with new recipe
        schedule.removeMeal(date, mealType);

        //Save schedule
        dataAccess.saveSchedule(schedule);

        //Pass result to presenter
        DeleteMealOutputData outputData = new
                DeleteMealOutputData(schedule, "Meal has been deleted successfully.");
        presenter.presentDeleteSuccess(outputData);

    }
}
