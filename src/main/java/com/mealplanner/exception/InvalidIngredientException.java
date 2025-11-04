package com.mealplanner.exception;

/**
 * Exception thrown when ingredient data is invalid (e.g., empty name, negative quantity, invalid unit).
 * Responsible: Jerry (search validation), Aaryan (recipe creation), Everyone (entity validation)
 */
public class InvalidIngredientException extends ValidationException {

    private final String ingredientName;

    public InvalidIngredientException(String ingredientName, String reason) {
        super("Invalid ingredient '" + ingredientName + "': " + reason);
        this.ingredientName = ingredientName;
    }

    public InvalidIngredientException(String message) {
        super(message);
        this.ingredientName = null;
    }

    public String getIngredientName() {
        return ingredientName;
    }
}
