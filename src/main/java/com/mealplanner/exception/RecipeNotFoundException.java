package com.mealplanner.exception;

/**
 * Exception thrown when a recipe cannot be found by ID or search criteria.
 * Responsible: Jerry (search), Regina (browse), Eden (adjust serving), Aaryan (store - check duplicates)
 */
public class RecipeNotFoundException extends MealPlannerException {

    private final String recipeId;

    public RecipeNotFoundException(String recipeId) {
        super("Recipe not found with ID: " + recipeId);
        this.recipeId = recipeId;
    }

    public RecipeNotFoundException(String message, String recipeId) {
        super(message);
        this.recipeId = recipeId;
    }

    public String getRecipeId() {
        return recipeId;
    }
}
