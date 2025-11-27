package com.mealplanner.exception;

/**
 * Base exception class for all Meal Planner application exceptions.
 * All custom exceptions should extend this class to provide consistent error handling.
 * Responsible: Everyone (use throughout the application)
 */
public class MealPlannerException extends RuntimeException {

    public MealPlannerException(String message) {
        super(message != null ? message : "An error occurred");
    }

    public MealPlannerException(String message, Throwable cause) {
        super(message != null ? message : "An error occurred", cause);
    }
}
