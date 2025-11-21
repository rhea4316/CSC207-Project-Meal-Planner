package com.mealplanner.entity;

import org.jetbrains.annotations.NotNull;

/**
 * Value object representing a user's daily nutritional goals.
 * This entity encapsulates nutrition targets to keep the User entity clean (Single Responsibility Principle).
 * Immutable to ensure thread safety and prevent accidental modifications.
 * Responsible: Everyone (shared entity used for nutrition tracking)
 */
public class NutritionGoals {

    private final int dailyCalories;
    private final double dailyProtein;    // in grams
    private final double dailyCarbs;      // in grams
    private final double dailyFat;        // in grams

    /**
     * Creates a new NutritionGoals with specified daily targets.
     *
     * @param dailyCalories target daily calorie intake
     * @param dailyProtein target daily protein in grams
     * @param dailyCarbs target daily carbohydrates in grams
     * @param dailyFat target daily fat in grams
     * @throws IllegalArgumentException if any value is negative
     */
    public NutritionGoals(int dailyCalories, double dailyProtein, double dailyCarbs, double dailyFat) {
        if (dailyCalories < 0 || dailyProtein < 0 || dailyCarbs < 0 || dailyFat < 0) {
            throw new IllegalArgumentException("Nutrition goals cannot be negative");
        }

        this.dailyCalories = dailyCalories;
        this.dailyProtein = dailyProtein;
        this.dailyCarbs = dailyCarbs;
        this.dailyFat = dailyFat;
    }

    /**
     * Creates default nutrition goals (2000 calories, balanced macros).
     */
    public static NutritionGoals createDefault() {
        return new NutritionGoals(2000, 150, 200, 70);
    }

    // Getters

    public int getDailyCalories() {
        return dailyCalories;
    }

    public double getDailyProtein() {
        return dailyProtein;
    }

    public double getDailyCarbs() {
        return dailyCarbs;
    }

    public double getDailyFat() {
        return dailyFat;
    }

    // Business methods

    /**
     * Checks if the provided nutrition info is within the daily goals.
     *
     * @param actual the actual nutrition consumed
     * @return true if all nutrients are at or below goals
     */
    public boolean isWithinGoals(@NotNull NutritionInfo actual) {
        return this.dailyCalories >= actual.getCalories() && this.dailyProtein >= actual.getProtein() &&
                this.dailyCarbs >= actual.getCarbs() && this.dailyFat >= actual.getFat();
    }

    /**
     * Calculates remaining nutrition to reach goals.
     *
     * @param consumed the nutrition already consumed
     * @return NutritionInfo representing remaining amounts (or negative if over)
     */
    public NutritionInfo calculateRemaining(NutritionInfo consumed) {
        if (isWithinGoals(consumed)) {
            return new NutritionInfo(this.dailyCalories-consumed.getCalories(), this.dailyProtein-consumed.getProtein(),
                    this.dailyCalories-consumed.getCalories(), this.dailyFat-consumed.getFat());
        }
        throw new UnsupportedOperationException("Consumed more than goal.");
    }

    /**
     * Calculates percentage of goal achieved for each nutrient.
     *
     * @param consumed the nutrition consumed
     * @return array of percentages [calories%, protein%, carbs%, fat%]
     */
    public double[] calculatePercentages(@NotNull NutritionInfo consumed) {
        if (this.dailyCalories == 0 || this.dailyProtein == 0 || this.dailyCarbs == 0 || this.dailyFat == 0) {
            throw new UnsupportedOperationException("Nutrition goals cannot be zero");
        }
        double calories_per = (double) consumed.getCalories() / this.dailyCalories * 100;
        double protein_per = (double) consumed.getProtein() / this.dailyProtein * 100;
        double carb_per = (double) consumed.getCarbs() / this.dailyCarbs * 100;
        double fat_per = (double) consumed.getFat() / this.dailyFat  * 100;
        double [] percentages = {calories_per, protein_per, carb_per, fat_per};
        return percentages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NutritionGoals that = (NutritionGoals) o;

        if (dailyCalories != that.dailyCalories) return false;
        if (Double.compare(that.dailyProtein, dailyProtein) != 0) return false;
        if (Double.compare(that.dailyCarbs, dailyCarbs) != 0) return false;
        return Double.compare(that.dailyFat, dailyFat) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = dailyCalories;
        temp = Double.doubleToLongBits(dailyProtein);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(dailyCarbs);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(dailyFat);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "NutritionGoals{" +
                "calories=" + dailyCalories +
                ", protein=" + dailyProtein + "g" +
                ", carbs=" + dailyCarbs + "g" +
                ", fat=" + dailyFat + "g" +
                '}';
    }
}
