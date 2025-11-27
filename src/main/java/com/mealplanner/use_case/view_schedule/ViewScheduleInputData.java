package com.mealplanner.use_case.view_schedule;

// Data transfer object carrying user identifier for schedule retrieval.
// Responsible: Mona
// done: Implement with username or user ID to identify whose schedule to view

import com.mealplanner.entity.Schedule;

public class ViewScheduleInputData {

    private final String username;
    private Schedule schedule;
    public ViewScheduleInputData(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
    public void loadSchedule (Schedule s) {this.schedule = s;}
    public Schedule getSchedule() {return schedule;}

}
