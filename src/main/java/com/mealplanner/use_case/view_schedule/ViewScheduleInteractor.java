package com.mealplanner.use_case.view_schedule;

import com.mealplanner.data_access.database.FileScheduleDataAccessObject;
import com.mealplanner.entity.Schedule;
import com.mealplanner.entity.User;
import com.mealplanner.exception.UserNotFoundException;

// Main business logic for retrieving and displaying user's meal schedule.
// Responsible: Mona

public class ViewScheduleInteractor implements ViewScheduleInputBoundary {

    private final FileScheduleDataAccessObject dataAccess;
    private final ViewScheduleOutputBoundary presenter;

    public ViewScheduleInteractor(FileScheduleDataAccessObject dataAccess, ViewScheduleOutputBoundary presenter) {
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
            presenter.presentError("Unexpected error");

        }
    }
    @Override
    public void saveSchedule(ViewScheduleInputData inputData) {
        Schedule schedule = inputData.getSchedule();
        if (schedule == null) {
            presenter.presentError("Schedule cannot be empty");
            return;
        }
        dataAccess.saveSchedule(schedule);
    }

    @Override
    public void loadSchedule(ViewScheduleInputData inputData) {
        Schedule schedule = dataAccess.loadSchedule(inputData.getUsername());
        ViewScheduleOutputData outputData = new ViewScheduleOutputData(null,schedule);
        presenter.presentSchedule(outputData);
    }
}