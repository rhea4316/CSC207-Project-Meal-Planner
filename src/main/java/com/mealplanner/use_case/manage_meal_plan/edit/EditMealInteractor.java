package com.mealplanner.use_case.manage_meal_plan.edit;

import com.mealplanner.entity.Schedule;
import com.mealplanner.entity.MealType;
import java.time.LocalDate;

// Main business logic for editing an existing meal in the schedule.
// Responsible: Grace
// TODO: Implement execute method: validate meal exists, update with new recipe, save schedule, pass result to presenter

public class EditMealInteractor implements EditMealInputBoundary {

    private final EditMealDataAccessInterface dataAccess;
    private final EditMealOutputBoundary presenter;

    public EditMealInteractor(EditMealDataAccessInterface dataAccess,
                             EditMealOutputBoundary presenter) {
        this.dataAccess = dataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(EditMealInputData inputData) {

        //Validate meal exists
        LocalDate date = inputData.getDate();
        MealType mealType = inputData.getMealType();
        Schedule schedule = dataAccess.getUserSchedule();
        String recipeID = inputData.getRecipe();

        if (!schedule.hasMeal(date, mealType)) {
            presenter.presentEditError("No meal exists for " + mealType + " on " + date + ".");
            return;
        }

        //Update with new recipe
        schedule.updateMeal(date, mealType, recipeID);

        //Save schedule
        dataAccess.saveSchedule(schedule);

        //Pass result to presenter
        EditMealOutputData outputData = new EditMealOutputData(schedule, "Meal has been edited successfully.");
        presenter.presentEditSuccess(outputData);
    }
}
