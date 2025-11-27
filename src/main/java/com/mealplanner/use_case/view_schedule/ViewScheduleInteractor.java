package com.mealplanner.use_case.view_schedule;

import com.mealplanner.entity.Schedule;
import com.mealplanner.entity.User;
import com.mealplanner.exception.UserNotFoundException;
import java.util.Objects;

// Main business logic for retrieving and displaying user's meal schedule.
// Responsible: Mona

public class ViewScheduleInteractor implements ViewScheduleInputBoundary {

    private final ViewScheduleDataAccessInterface dataAccess;
    private final ViewScheduleOutputBoundary presenter;

    public ViewScheduleInteractor(ViewScheduleDataAccessInterface dataAccess, ViewScheduleOutputBoundary presenter) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "Data access cannot be null");
        this.presenter = Objects.requireNonNull(presenter, "Presenter cannot be null");
    }

    @Override
    public void execute(ViewScheduleInputData inputData) {
        if (inputData == null) {
            presenter.presentError("Input data cannot be null");
            return;
        }

        String username = inputData.getUsername();

        if (username == null || username.trim().isEmpty()) {
            presenter.presentError("Username cannot be empty");
            return;
        }

        username = username.trim();

        try {
            User user = dataAccess.getUserByUsername(username);

            Schedule schedule = user.getMealSchedule();

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
            presenter.presentError("Unexpected error: " + e.getMessage());
        }
    }
    @Override
    public void saveSchedule(ViewScheduleInputData inputData) {
        if (inputData == null) {
            presenter.presentError("Input data cannot be null");
            return;
        }

        Schedule schedule = inputData.getSchedule();
        if (schedule == null) {
            presenter.presentError("Schedule cannot be empty");
            return;
        }

        try {
            dataAccess.saveSchedule(schedule);
        } catch (Exception e) {
            presenter.presentError("Failed to save schedule: " + e.getMessage());
        }
    }

    @Override
    public void loadSchedule(ViewScheduleInputData inputData) {
        if (inputData == null) {
            presenter.presentError("Input data cannot be null");
            return;
        }

        String username = inputData.getUsername();
        
        if (username == null || username.trim().isEmpty()) {
            presenter.presentError("Username cannot be empty");
            return;
        }
        
        username = username.trim();
        
        try {
            Schedule schedule = dataAccess.loadScheduleByUsername(username);
            
            if (schedule == null) {
                presenter.presentError("No Schedule found for user");
                return;
            }
            
            ViewScheduleOutputData outputData = new ViewScheduleOutputData(username, schedule);
            presenter.presentSchedule(outputData);
        } catch (Exception e) {
            presenter.presentError("Failed to load schedule: " + e.getMessage());
        }
    }
}