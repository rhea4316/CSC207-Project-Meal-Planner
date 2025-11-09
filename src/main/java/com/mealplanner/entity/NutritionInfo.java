package com.mealplanner.entity;

/**
 * Value object representing nutritional information (calories, protein, carbs, fat).
 * This entity encapsulates nutrition data to keep other entities clean.
 * Immutable to ensure thread safety and prevent accidental modifications.
 * Responsible: Everyone (shared entity used for nutrition calculations)
 */
public class NutritionInfo {

    private final int calories;
    private final double protein;    // in grams
    private final double carbs;      // in grams
    private final double fat;        // in grams

    /**
     * Creates a new NutritionInfo with specified values.
     *
     * @param calories calorie count
     * @param protein protein in grams
     * @param carbs carbohydrates in grams
     * @param fat fat in grams
     * @throws IllegalArgumentException if any value is negative
     */
    public NutritionInfo(int calories, double protein, double carbs, double fat) {
        if (calories < 0 || protein < 0 || carbs < 0 || fat < 0) {
            throw new IllegalArgumentException("Nutrition values cannot be negative");
        }

        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }

    /**
     * Creates an empty NutritionInfo (all zeros).
     */
    public static NutritionInfo empty() {
        return new NutritionInfo(0, 0.0, 0.0, 0.0);
    }

    // Getters

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

    // Business methods

    /**
     * Scales this nutrition info by a multiplier (for serving size adjustments).
     *
     * @param multiplier the scale factor (e.g., 2.0 for double servings)
     * @return new NutritionInfo with scaled values
     * @throws IllegalArgumentException if multiplier is negative
     */
    public NutritionInfo scale(double multiplier) {
        int roundedCalories = (int) Math.round(this.calories * multiplier);

        return new NutritionInfo(roundedCalories, this.protein * multiplier, this.carbs * multiplier, this.fat * multiplier);
    }

    /**
     * Adds another NutritionInfo to this one (for combining nutrition from multiple sources).
     *
     * @param other the other NutritionInfo to add
     * @return new NutritionInfo with combined values
     * @throws IllegalArgumentException if other is null
     */
    public NutritionInfo add(NutritionInfo other) {
        int newCalories = this.calories + other.calories;
        double newProtein = this.protein + other.protein;
        double newCarbs = this.carbs + other.carbs;
        double newFat = this.fat + other.fat;

        return new NutritionInfo(newCalories, newProtein, newCarbs, newFat);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NutritionInfo that = (NutritionInfo) o;

        if (calories != that.calories) return false;
        if (Double.compare(that.protein, protein) != 0) return false;
        if (Double.compare(that.carbs, carbs) != 0) return false;
        return Double.compare(that.fat, fat) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = calories;
        temp = Double.doubleToLongBits(protein);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(carbs);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(fat);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "NutritionInfo{" +
                "calories=" + calories +
                ", protein=" + protein + "g" +
                ", carbs=" + carbs + "g" +
                ", fat=" + fat + "g" +
                '}';
    }
}
