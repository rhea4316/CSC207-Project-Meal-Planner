package com.mealplanner.use_case.view_schedule;

// Data transfer object carrying user's schedule with all meals.
// Responsible: Mona
// done: Implement with Schedule object containing weekly meal plan data

import com.mealplanner.entity.Schedule;

public class ViewScheduleOutputData {

    private final String username;
    private final Schedule schedule;

    public ViewScheduleOutputData(String username, Schedule schedule) {
        this.username = username;
        this.schedule = schedule;
    }

    public String getUsername() {
        return username;
    }
    public Schedule getSchedule() {
        return schedule;
    }


}