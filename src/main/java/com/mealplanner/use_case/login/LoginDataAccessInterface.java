package com.mealplanner.use_case.login;
import com.mealplanner.entity.User;
import com.mealplanner.exception.UserNotFoundException;

// Data access interface for retrieving user data during login.
// Responsible: Mona (interface), Everyone (implementation via FileUserDataAccessObject)
// Done: Define methods to check if user exists and retrieve User entity by username

import com.mealplanner.exception.UserNotFoundException;

public interface LoginDataAccessInterface {

    ///  return true if a user with the given username exists

    boolean existsByUsername(String username);

    ///  return a user by username. throws UserNotFoundException if user does not exist

    User getUserByUsername(String username) throws UserNotFoundException;

}
