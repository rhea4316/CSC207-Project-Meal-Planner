package com.mealplanner.use_case.manage_meal_plan.add;

import com.mealplanner.entity.Schedule;

// Data access interface for retrieving and saving schedule data.
// Responsible: Grace (interface), Everyone (implementation via FileScheduleDataAccessObject)

public interface AddMealDataAccessInterface {
    Schedule getUserSchedule();
    void saveSchedule(Schedule schedule);
}
