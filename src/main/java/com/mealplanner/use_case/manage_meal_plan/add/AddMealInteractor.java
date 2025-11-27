package com.mealplanner.use_case.manage_meal_plan.add;

import com.mealplanner.entity.Schedule;
import com.mealplanner.entity.MealType;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import com.mealplanner.exception.MealPlannerException;

// Main business logic for adding a meal to the schedule.
// Responsible: Grace
// TODO: Implement execute method: validate date/meal type, check slot availability, add meal to schedule, save, pass result to presenter

public class AddMealInteractor implements AddMealInputBoundary {

    private final AddMealDataAccessInterface dataAccess;
    private final AddMealOutputBoundary presenter;

    public AddMealInteractor(AddMealDataAccessInterface dataAccess,
                             AddMealOutputBoundary presenter) {
        this.dataAccess = dataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(AddMealInputData inputData) {

        //Validate date
        LocalDate date;
        try {
            date = inputData.getDate();

        } catch (DateTimeParseException e) {
            presenter.presentAddError("Invalid date format. Must be YYYY-MM-DD.");
            return;
        }

        //Validate meal type
        MealType mealType;
        try {
            mealType = inputData.getMealType();

        } catch (IllegalArgumentException e) {
            presenter.presentAddError("Invalid meal type. Must be BREAKFAST, LUNCH, or DINNER.");
            return;
        }

        Schedule schedule = dataAccess.getUserSchedule();
        String recipeID = inputData.getRecipe();

        //Check if the slot is available
        if (schedule.hasMeal(date, mealType)) {
            presenter.presentAddError("Meal slot already taken by " + mealType + " on " + date);
            return;
        }

        //Add meal to schedule
        try {
            schedule.addMeal(date, mealType, recipeID);

        } catch (MealPlannerException e) {
            presenter.presentAddError(e.getMessage());
            return;
        }

        //Save schedule
        dataAccess.saveSchedule(schedule);

        //Pass result to presenter
        AddMealOutputData outputData = new AddMealOutputData(schedule, "Meal has been added successfully.");
        presenter.presentAddSuccess(outputData);
    }
}
