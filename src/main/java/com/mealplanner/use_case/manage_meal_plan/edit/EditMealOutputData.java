package com.mealplanner.use_case.manage_meal_plan.edit;

import com.mealplanner.entity.Schedule;

public class EditMealOutputData {
    private final Schedule schedule;
    private final String message;

    public EditMealOutputData(Schedule schedule, String message) {
        this.schedule = schedule;
        this.message = message;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public String getMessage() {
        return message;
    }
}
