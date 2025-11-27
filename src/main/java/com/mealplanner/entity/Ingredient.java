package com.mealplanner.entity;

import java.util.Objects;

// Core entity representing a single ingredient with nutritional information.
// Responsible: Everyone (shared entity used across all use cases)
public final class Ingredient {

    private final String name;
    private final double quantity;
    private final String unit;
    private final int calories;
    private final double protein;
    private final double carbs;
    private final double fat;
    
    public Ingredient(String name, double quantity, String unit, int calories, double protein, double carbs, double fat) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingredient name cannot be empty");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        if (calories < 0 || protein < 0 || carbs < 0 || fat < 0) {
            throw new IllegalArgumentException("Nutrition values cannot be negative");
        }
        
        this.name = name.trim();
        this.quantity = quantity;
        this.unit = unit != null ? unit : "";
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return Double.compare(that.quantity, quantity) == 0 &&
                calories == that.calories &&
                Double.compare(that.protein, protein) == 0 &&
                Double.compare(that.carbs, carbs) == 0 &&
                Double.compare(that.fat, fat) == 0 &&
                Objects.equals(name, that.name) &&
                Objects.equals(unit, that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, quantity, unit, calories, protein, carbs, fat);
    }

    @Override
    public String toString() {
        return String.format("Ingredient{name='%s', quantity=%.2f %s, calories=%d, protein=%.1fg, carbs=%.1fg, fat=%.1fg}",
                name, quantity, unit, calories, protein, carbs, fat);
    }
}
