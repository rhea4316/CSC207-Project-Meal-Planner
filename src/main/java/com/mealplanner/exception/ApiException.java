package com.mealplanner.exception;

/**
 * Exception thrown when external API calls fail (network errors, invalid API keys, rate limits, etc.).
 * Responsible: Everyone (API integration - SpoonacularApiClient, EdamamApiClient)
 */
public class ApiException extends MealPlannerException {

    private final int statusCode;

    public ApiException(String message) {
        super(message);
        this.statusCode = -1;
    }

    public ApiException(String message, int statusCode) {
        super(message + " (HTTP " + statusCode + ")");
        this.statusCode = statusCode;
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
