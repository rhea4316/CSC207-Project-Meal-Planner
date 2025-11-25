package com.mealplanner.use_case.view_schedule;

// Data transfer object carrying user identifier for schedule retrieval.
// Responsible: Mona
// done: Implement with username or user ID to identify whose schedule to view

public class ViewScheduleInputData {

    private final String username;
    public ViewScheduleInputData(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

}
