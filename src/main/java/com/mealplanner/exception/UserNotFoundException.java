package com.mealplanner.exception;

/**
 * Exception thrown when a user cannot be found by username or ID.
 * Responsible: Mona (login, view schedule)
 */
public class UserNotFoundException extends MealPlannerException {

    private final String username;

    public UserNotFoundException(String username) {
        super("User not found: " + (username != null ? username : "null"));
        this.username = username;
    }

    public UserNotFoundException(String message, String username) {
        super(message != null ? message : "User not found");
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
