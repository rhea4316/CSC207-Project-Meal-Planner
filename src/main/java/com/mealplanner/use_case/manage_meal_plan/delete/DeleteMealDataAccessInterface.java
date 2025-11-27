package com.mealplanner.use_case.manage_meal_plan.delete;

import com.mealplanner.entity.Schedule;

// Data access interface for retrieving and updating schedule after deletion.
// Responsible: Grace (interface), Everyone (implementation)

public interface DeleteMealDataAccessInterface {
    Schedule getUserSchedule();
    void saveSchedule(Schedule schedule);
}
