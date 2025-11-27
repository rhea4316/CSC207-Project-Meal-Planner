package com.mealplanner.use_case.signup;

import com.mealplanner.entity.User;

// Data access interface for user registration.
// Responsible: Everyone

public interface SignupDataAccessInterface {
    /**
     * Check if a username is already taken.
     * @param username Username to check
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Save a new user to the repository.
     * @param user User to save
     */
    void save(User user);
}

