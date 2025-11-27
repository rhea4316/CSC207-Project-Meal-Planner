package com.mealplanner.use_case.manage_meal_plan.edit;

import com.mealplanner.entity.Schedule;

// Data access interface for retrieving and updating schedule data.
// Responsible: Grace (interface), Everyone (implementation)

public interface EditMealDataAccessInterface {
    Schedule getUserSchedule();
    void saveSchedule(Schedule schedule);
}
