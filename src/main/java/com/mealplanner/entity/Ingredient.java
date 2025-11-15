package com.mealplanner.entity;

// Core entity representing a single ingredient with nutritional information.
// Responsible: Everyone (shared entity used across all use cases)
// TODO: Implement immutable ingredient class with name, quantity, unit, and nutrition values (calories, protein, carbs, fat)
public final class Ingredient {

    private final String name;
    private final double quantity;
    private final String unit;
    private final int calories;
    private final double protein;
    private final double carbs;
    private final double fat;
    public Ingredient(String name, double quantity, String unit, int calories, double protein, double carbs, double fat) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }

    public String getName() {
        return name;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public int getCalories() {
        return calories;
    }

    public double getProtein() {
        return protein;
    }

    public double getCarbs() {
        return carbs;
    }

    public double getFat() {
        return fat;
    }
}
