package com.mealplanner.use_case.view_schedule;

// Output boundary interface for presenting user's meal schedule.
// Responsible: Mona
// done: Define methods for presentSchedule (success with schedule data) and presentError (schedule not found, etc.)



public interface ViewScheduleOutputBoundary {

    void presentSchedule(ViewScheduleOutputData outputData);

    void presentError(String errorMessage);


}
