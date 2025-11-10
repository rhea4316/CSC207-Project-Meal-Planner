package com.mealplanner.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Recipe {

    private final String name;
    private final String description;
    private final List<String> ingredients;
    private final List<String> steps;
    private final int servingSize;
    private final NutritionInfo nutritionInfo;
    private final int prepTimeMinutes;
    private final int cookTimeMinutes;
    private final List<DietaryRestriction> dietaryRestrictions;

    //Constructs a new Recipe with all required information.
    public Recipe(String name, String description, List<String> ingredients,
            List<String> steps, int servingSize, NutritionInfo nutritionInfo,
            int prepTimeMinutes, int cookTimeMinutes,
            List<DietaryRestriction> dietaryRestrictions) {

        validateInputs(name, ingredients, steps, servingSize, nutritionInfo,
                prepTimeMinutes, cookTimeMinutes);

        this.name = name.trim();
        this.description = description != null ? description.trim() : "";
        this.ingredients = new ArrayList<>(ingredients);
        this.steps = new ArrayList<>(steps);
        this.servingSize = servingSize;
        this.nutritionInfo = nutritionInfo;
        this.prepTimeMinutes = prepTimeMinutes;
        this.cookTimeMinutes = cookTimeMinutes;
        this.dietaryRestrictions = dietaryRestrictions != null
                ? new ArrayList<>(dietaryRestrictions) : new ArrayList<>();
    }

    private void validateInputs(String name, List<String> ingredients,
            List<String> steps, int servingSize,
            NutritionInfo nutritionInfo, int prepTimeMinutes,
            int cookTimeMinutes) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipe name cannot be empty");
        }
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("Ingredients list cannot beempty");
        }
        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException("Steps list cannot be empty");
        }
        if (servingSize <= 0) {
            throw new IllegalArgumentException("Serving size must be greater than zero");
        }
        if (nutritionInfo == null) {
            throw new IllegalArgumentException("Nutrition information cannot be null");
        }
        if (prepTimeMinutes < 0) {
            throw new IllegalArgumentException("Prep time cannot be less than zero minutes");
        }
        if (cookTimeMinutes < 0) {
            throw new IllegalArgumentException("Cook time cannot be less than zero minutes");
        }
    }
    //Creates a new Recipe with adjusted serving size and scaled nutrition information.

    public Recipe adjustServingSize(int newServingSize) {
        if (newServingSize <= 0) {
            throw new IllegalArgumentException("New serving size must be greater than zero");
        }

        double scaleFactor = (double) newServingSize / this.servingSize;
        NutritionInfo scaledNutrition = this.nutritionInfo.scale(scaleFactor);

        List<String> scaledIngredients = scaleIngredients(this.ingredients, scaleFactor);

        return new Recipe(
                this.name,
                this.description,
                scaledIngredients,
                this.steps,
                newServingSize,
                scaledNutrition,
                this.prepTimeMinutes,
                this.cookTimeMinutes,
                this.dietaryRestrictions
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
        // Will need to reimplemented 
        return String.format("%s (scaled by %.2f)", ingredient, scaleFactor);
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getIngredients() {
        return new ArrayList<>(ingredients);
    }

    public List<String> getSteps() {
        return new ArrayList<>(steps);
    }

    public int getServingSize() {
        return servingSize;
    }

    public NutritionInfo getNutritionInfo() {
        return nutritionInfo;
    }

    public int getPrepTimeMinutes() {
        return prepTimeMinutes;
    }

    public int getCookTimeMinutes() {
        return cookTimeMinutes;
    }

    public int getTotalTimeMinutes() {
        return prepTimeMinutes + cookTimeMinutes;
    }

    public List<DietaryRestriction> getDietaryRestrictions() {
        return new ArrayList<>(dietaryRestrictions);
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
                && prepTimeMinutes == recipe.prepTimeMinutes
                && cookTimeMinutes == recipe.cookTimeMinutes
                && Objects.equals(name, recipe.name)
                && Objects.equals(description, recipe.description)
                && Objects.equals(ingredients, recipe.ingredients)
                && Objects.equals(steps, recipe.steps)
                && Objects.equals(nutritionInfo, recipe.nutritionInfo)
                && Objects.equals(dietaryRestrictions, recipe.dietaryRestrictions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, ingredients, steps, servingSize,
                nutritionInfo, prepTimeMinutes, cookTimeMinutes,
                dietaryRestrictions);
    }

    @Override
    public String toString() {
        return String.format("%s (Serves %d)\nPrep: %d min, Cook: %d min\n%d ingredients, %d steps",
                name, servingSize, prepTimeMinutes, cookTimeMinutes, ingredients.size(), steps.size());
    }
}
