package com.mealplanner.exception;

/**
 * Exception thrown when an invalid serving size is provided (e.g., zero, negative, or unreasonably large).
 * Responsible: Eden (adjust serving size), Aaryan (store recipe with serving size)
 */
public class InvalidServingSizeException extends ValidationException {

    private final int attemptedServingSize;

    public InvalidServingSizeException(int attemptedServingSize) {
        super("Invalid serving size: " + attemptedServingSize + ". Must be a positive number.");
        this.attemptedServingSize = attemptedServingSize;
    }

    public InvalidServingSizeException(String message, int attemptedServingSize) {
        super(message);
        this.attemptedServingSize = attemptedServingSize;
    }

    public int getAttemptedServingSize() {
        return attemptedServingSize;
    }
}
