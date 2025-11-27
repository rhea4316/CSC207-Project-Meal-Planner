package com.mealplanner.entity;

import com.mealplanner.util.IngredientParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Recipe {
    // Required fields
    private final String name;
    private final List<String> ingredients;
    private final String steps;
    private final int servingSize;
    
    // Optional fields
    private final NutritionInfo nutritionInfo;
    private final Integer cookTimeMinutes;
    private final List<DietaryRestriction> dietaryRestrictions;

    // Add a unique identifier for the recipe
    private final String recipeId;

    /**
     * Constructs a new Recipe with required and optional information.
     */
    public Recipe(String name, List<String> ingredients, String steps,
                 int servingSize, NutritionInfo nutritionInfo, Integer cookTimeMinutes,
                 List<DietaryRestriction> dietaryRestrictions, String recipeId) {
        validateInputs(name, ingredients, steps, servingSize);

        this.name = name.trim();
        this.ingredients = new ArrayList<>(ingredients);
        this.steps = steps;
        this.servingSize = servingSize;
        this.nutritionInfo = nutritionInfo; // Can be null
        this.cookTimeMinutes = cookTimeMinutes; // Can be null
        this.dietaryRestrictions = dietaryRestrictions != null ? 
            new ArrayList<>(dietaryRestrictions) : new ArrayList<>();
        this.recipeId = recipeId != null ? recipeId.trim() : null;
    }

    /**
     * Constructs a new Recipe with only required fields.
     */
    public Recipe(String name, List<String> ingredients, String steps, int servingSize) {
        this(name, ingredients, steps, servingSize, null, null, null, null);
    }

    private void validateInputs(String name, List<String> ingredients,
            String steps, int servingSize) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipe name cannot be empty");
        }
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("Ingredients list cannot be empty");
        }
        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException("Steps list cannot be empty");
        }
        if (servingSize <= 0) {
            throw new IllegalArgumentException("Serving size must be greater than zero");
        }
    }
    //Creates a new Recipe with adjusted serving size and scaled nutrition information.

    public Recipe adjustServingSize(int newServingSize) {
        if (newServingSize <= 0) {
            throw new IllegalArgumentException("New serving size must be greater than zero");
        }

        double scaleFactor = (double) newServingSize / this.servingSize;
        NutritionInfo scaledNutrition = this.nutritionInfo != null ? 
            this.nutritionInfo.scale(scaleFactor) : null;

        List<String> scaledIngredients = scaleIngredients(this.ingredients, scaleFactor);

        return new Recipe(
                this.name,
                scaledIngredients,
                this.steps,
                newServingSize,
                scaledNutrition,
                this.cookTimeMinutes,
                this.dietaryRestrictions,
                this.recipeId // Preserve the recipeId
        );
    }

    private List<String> scaleIngredients(List<String> originalIngredients, double scaleFactor) {
        List<String> scaled = new ArrayList<>();
        for (String ingredient : originalIngredients) {
            scaled.add(scaleIngredientQuantity(ingredient, scaleFactor));
        }
        return scaled;
    }

    private String scaleIngredientQuantity(String ingredient, double scaleFactor) {
        // Use IngredientParser to parse and scale the ingredient
        return IngredientParser.scaleIngredient(ingredient, scaleFactor);
    }

    // Getters
    public String getName() {
        return name;
    }

    public List<String> getIngredients() {
        return new ArrayList<>(ingredients);
    }

    public String getSteps() {
        return steps;
    }

    public int getServingSize() {
        return servingSize;
    }

    public NutritionInfo getNutritionInfo() {
        return nutritionInfo;
    }

    public Integer getCookTimeMinutes() {
        return cookTimeMinutes;
    }

    public Integer getTotalTimeMinutes() {
        return cookTimeMinutes;
    }

    public List<DietaryRestriction> getDietaryRestrictions() {
        return new ArrayList<>(dietaryRestrictions);
    }

    public String getRecipeId() {
        return recipeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Recipe recipe = (Recipe) o;
        return servingSize == recipe.servingSize
                && Objects.equals(name, recipe.name)
                && Objects.equals(ingredients, recipe.ingredients)
                && Objects.equals(steps, recipe.steps)
                && Objects.equals(nutritionInfo, recipe.nutritionInfo)
                && Objects.equals(cookTimeMinutes, recipe.cookTimeMinutes)
                && Objects.equals(dietaryRestrictions, recipe.dietaryRestrictions)
                && Objects.equals(recipeId, recipe.recipeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, ingredients, steps, servingSize,
                nutritionInfo, cookTimeMinutes, dietaryRestrictions, recipeId);
    }

    @Override
    public String toString() {
        int stepsCount = steps != null ? steps.split("\n").length : 0; // Use "\n" or "\\n" depending on if steps is already escaped
        return String.format("%s (Serves %d)%s\n%d ingredients, %d step(s)",
                name, servingSize,
                cookTimeMinutes != null ? String.format("\nCook: %d min", cookTimeMinutes) : "",
                ingredients.size(), stepsCount);
    }
}
