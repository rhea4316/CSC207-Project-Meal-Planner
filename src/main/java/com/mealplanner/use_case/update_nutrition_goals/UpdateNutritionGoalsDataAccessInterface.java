package com.mealplanner.use_case.update_nutrition_goals;

import com.mealplanner.entity.User;
import com.mealplanner.exception.UserNotFoundException;

/**
 * Data access interface for updating user nutrition goals.
 * Responsible: Database team
 */
public interface UpdateNutritionGoalsDataAccessInterface {
    /**
     * Retrieves a user by their unique ID.
     * 
     * @param userId the user's unique identifier
     * @return User entity
     * @throws UserNotFoundException if user not found
     */
    User getUserById(String userId) throws UserNotFoundException;
    
    /**
     * Updates an existing user in the data store.
     * 
     * @param user the user entity with updated information
     * @throws UserNotFoundException if user not found
     */
    void updateUser(User user) throws UserNotFoundException;
}

