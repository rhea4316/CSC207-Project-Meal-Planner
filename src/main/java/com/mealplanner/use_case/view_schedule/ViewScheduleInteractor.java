package com.mealplanner.use_case.view_schedule;

import com.mealplanner.entity.Schedule;
import com.mealplanner.entity.User;
import com.mealplanner.exception.UserNotFoundException;

// Main business logic for retrieving and displaying user's meal schedule.
// Responsible: Mona
// done: Implement execute method: retrieve user's schedule from data access, format for display, pass to presenter

public class ViewScheduleInteractor implements ViewScheduleInputBoundary {

    private final ViewScheduleDataAccessInterface dataAccess;
    private final ViewScheduleOutputBoundary presenter;

    public ViewScheduleInteractor(ViewScheduleDataAccessInterface dataAccess, ViewScheduleOutputBoundary presenter) {
        this.dataAccess = dataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(ViewScheduleInputData inputData) {
        String username = inputData.getUsername();

        if (username == null || username.trim().isEmpty()) {
            presenter.presentError("Username cannot be empty");
            return;
        }

        username = username.trim();

        try {
            User user = dataAccess.getUserByUsername(username);

            Schedule schedule = dataAccess.getScheduleByUserID(user.getUserId());

            if (schedule == null || schedule.isEmpty()) {
                presenter.presentError("No Schedule found for user");
                return;
            }

            ViewScheduleOutputData outputData =
                    new ViewScheduleOutputData(user.getUsername(), schedule);
            presenter.presentSchedule(outputData);

        } catch (UserNotFoundException e) {
            presenter.presentError("Username not found");
        } catch (Exception e) {
            presenter.presentError("Unexpected error");

        }
    }


}