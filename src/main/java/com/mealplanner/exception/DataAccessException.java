package com.mealplanner.exception;

/**
 * Exception thrown when file I/O or database operations fail.
 * Use this for JSON parsing errors, file not found, permission denied, etc.
 * Responsible: Everyone (data access layer - DAOs)
 */
public class DataAccessException extends MealPlannerException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
