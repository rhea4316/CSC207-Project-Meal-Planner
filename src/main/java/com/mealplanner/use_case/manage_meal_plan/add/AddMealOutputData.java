package com.mealplanner.use_case.manage_meal_plan.add;

import com.mealplanner.entity.Schedule;

public class AddMealOutputData {
    private final Schedule schedule;
    private final String message;

    public AddMealOutputData(Schedule schedule, String message) {
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
