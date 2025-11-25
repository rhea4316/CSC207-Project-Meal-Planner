package com.mealplanner.interface_adapter.controller;

// Controller for viewing user's meal schedule - receives user identifier and calls interactor.
// Responsible: Mona
// done: Implement execute method that converts username/user ID to InputData and calls view schedule interactor

import com.mealplanner.use_case.view_schedule.ViewScheduleInputBoundary;
import com.mealplanner.use_case.view_schedule.ViewScheduleInputData;

public class ViewScheduleController {

    private final ViewScheduleInputBoundary interactor;
    public ViewScheduleController(ViewScheduleInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(String username) {
        ViewScheduleInputData inputData = new ViewScheduleInputData(username);
        interactor.execute(inputData);
    }


}
