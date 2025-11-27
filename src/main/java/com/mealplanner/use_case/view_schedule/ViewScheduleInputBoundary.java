package com.mealplanner.use_case.view_schedule;

// Input boundary interface for viewing user's meal schedule.
// Responsible: Mona
// done: Define execute method that takes ViewScheduleInputData (username/user ID) as parameter

public interface ViewScheduleInputBoundary {
    void execute(ViewScheduleInputData inputData);
    void saveSchedule(ViewScheduleInputData inputData);
    void loadSchedule(ViewScheduleInputData inputData);
}
