package com.mealplanner.use_case.view_schedule;

// Data access interface for retrieving user's schedule data.
// Responsible: Mona (interface), Everyone (implementation via FileScheduleDataAccessObject)
// done: Define method to retrieve Schedule entity by user ID

import com.mealplanner.entity.User;
import com.mealplanner.exception.UserNotFoundException;

public interface ViewScheduleDataAccessInterface {

    boolean existsByUsername(String username);

    // Retrieve the User entity by username. Throws UserNotFoundException if user dne
    User getUserByUsername(String username) throws UserNotFoundException;


}
