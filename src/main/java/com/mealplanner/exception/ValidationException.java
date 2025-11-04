package com.mealplanner.exception;

/**
 * Exception thrown when input validation fails (e.g., empty strings, invalid formats, out of range values).
 * Use this for general validation errors that don't fit more specific exception types.
 * Responsible: Everyone (use in their interactors for input validation)
 */
public class ValidationException extends MealPlannerException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
