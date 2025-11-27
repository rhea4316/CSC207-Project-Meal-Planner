package com.mealplanner.util;

import com.mealplanner.entity.Ingredient;
import com.mealplanner.entity.NutritionInfo;

import java.util.List;

/**
 * Utility class for nutrition calculations and macronutrient analysis.
 * Provides helpers for calculating totals, percentages, and daily values.
 *
 * Responsible: Everyone (shared utility)
 */
public class NutritionCalculator {

    // Standard daily calorie recommendations
    public static final int STANDARD_DAILY_CALORIES = 2000;
    public static final double STANDARD_DAILY_PROTEIN = 50.0;  // grams
    public static final double STANDARD_DAILY_CARBS = 275.0;   // grams
    public static final double STANDARD_DAILY_FAT = 70.0;      // grams

    // Calorie conversions (calories per gram)
    public static final int CALORIES_PER_GRAM_PROTEIN = 4;
    public static final int CALORIES_PER_GRAM_CARBS = 4;
    public static final int CALORIES_PER_GRAM_FAT = 9;

    /**
     * Calculate total nutrition from a list of ingredients.
     *
     * @param ingredients List of ingredients to sum
     * @return NutritionInfo with totals
     */
    public static NutritionInfo calculateTotalNutrition(List<Ingredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return NutritionInfo.empty();
        }
        
        int totalCalories = 0;
        double totalProtein = 0.0;
        double totalCarbs = 0.0;
        double totalFat = 0.0;
        
        for (Ingredient ingredient : ingredients) {
            if (ingredient != null) {
                totalCalories += ingredient.getCalories();
                totalProtein += ingredient.getProtein();
                totalCarbs += ingredient.getCarbs();
                totalFat += ingredient.getFat();
            }
        }
        
        return new NutritionInfo(totalCalories, totalProtein, totalCarbs, totalFat);
    }

    /**
     * Scale nutrition values by a multiplier (for serving size adjustments).
     *
     * @param original Original nutrition info
     * @param multiplier Scale factor (e.g., 2.0 for double servings)
     * @return Scaled NutritionInfo
     */
    public static NutritionInfo scaleNutrition(NutritionInfo original, double multiplier) {
        if (original == null) {
            throw new IllegalArgumentException("Nutrition info cannot be null");
        }
        return original.scale(multiplier);
    }

    /**
     * Calculate the percentage of daily value for each macronutrient.
     *
     * @param nutrition Nutrition info to calculate percentages for
     * @return Array of percentages [calories%, protein%, carbs%, fat%]
     */
    public static double[] calculateDailyValuePercentages(NutritionInfo nutrition) {
        if (nutrition == null) {
            throw new IllegalArgumentException("Nutrition info cannot be null");
        }
        
        double caloriesPercent = (nutrition.getCalories() / (double) STANDARD_DAILY_CALORIES) * 100;
        double proteinPercent = (nutrition.getProtein() / STANDARD_DAILY_PROTEIN) * 100;
        double carbsPercent = (nutrition.getCarbs() / STANDARD_DAILY_CARBS) * 100;
        double fatPercent = (nutrition.getFat() / STANDARD_DAILY_FAT) * 100;
        
        return new double[]{caloriesPercent, proteinPercent, carbsPercent, fatPercent};
    }

    /**
     * Calculate macronutrient breakdown as percentages of total calories.
     *
     * @param nutrition Nutrition info
     * @return Array of percentages [protein%, carbs%, fat%]
     */
    public static double[] calculateMacroPercentages(NutritionInfo nutrition) {
        if (nutrition == null) {
            throw new IllegalArgumentException("Nutrition info cannot be null");
        }
        
        int totalCalories = nutrition.getCalories();
        if (totalCalories == 0) {
            return new double[]{0.0, 0.0, 0.0};
        }
        
        double proteinCalories = nutrition.getProtein() * CALORIES_PER_GRAM_PROTEIN;
        double carbsCalories = nutrition.getCarbs() * CALORIES_PER_GRAM_CARBS;
        double fatCalories = nutrition.getFat() * CALORIES_PER_GRAM_FAT;
        
        double proteinPercent = (proteinCalories / totalCalories) * 100;
        double carbsPercent = (carbsCalories / totalCalories) * 100;
        double fatPercent = (fatCalories / totalCalories) * 100;
        
        return new double[]{proteinPercent, carbsPercent, fatPercent};
    }

    /**
     * Check if nutrition values are within healthy macronutrient ranges.
     * Standard ranges: Protein 10-35%, Carbs 45-65%, Fat 20-35%
     *
     * @param nutrition Nutrition info to validate
     * @return true if within healthy ranges
     */
    public static boolean isBalancedMacros(NutritionInfo nutrition) {
        if (nutrition == null) {
            throw new IllegalArgumentException("Nutrition info cannot be null");
        }
        
        double[] macroPercentages = calculateMacroPercentages(nutrition);
        double proteinPercent = macroPercentages[0];
        double carbsPercent = macroPercentages[1];
        double fatPercent = macroPercentages[2];
        
        boolean proteinOk = proteinPercent >= 10 && proteinPercent <= 35;
        boolean carbsOk = carbsPercent >= 45 && carbsPercent <= 65;
        boolean fatOk = fatPercent >= 20 && fatPercent <= 35;
        
        return proteinOk && carbsOk && fatOk;
    }

    /**
     * Calculate calories from macronutrients.
     * Useful for validating nutrition data accuracy.
     *
     * @param protein Grams of protein
     * @param carbs Grams of carbohydrates
     * @param fat Grams of fat
     * @return Estimated total calories
     */
    public static int calculateCaloriesFromMacros(double protein, double carbs, double fat) {
        double proteinCalories = protein * CALORIES_PER_GRAM_PROTEIN;
        double carbsCalories = carbs * CALORIES_PER_GRAM_CARBS;
        double fatCalories = fat * CALORIES_PER_GRAM_FAT;
        
        return (int) Math.round(proteinCalories + carbsCalories + fatCalories);
    }

    /**
     * Calculate remaining nutrition to meet daily goals.
     *
     * @param consumed Nutrition already consumed
     * @param goals Daily nutrition goals
     * @return NutritionInfo representing remaining amounts
     */
    public static NutritionInfo calculateRemaining(NutritionInfo consumed, NutritionInfo goals) {
        if (consumed == null || goals == null) {
            throw new IllegalArgumentException("Nutrition info cannot be null");
        }
        
        int remainingCalories = goals.getCalories() - consumed.getCalories();
        double remainingProtein = goals.getProtein() - consumed.getProtein();
        double remainingCarbs = goals.getCarbs() - consumed.getCarbs();
        double remainingFat = goals.getFat() - consumed.getFat();
        
        return new NutritionInfo(
            Math.max(0, remainingCalories),
            Math.max(0, remainingProtein),
            Math.max(0, remainingCarbs),
            Math.max(0, remainingFat)
        );
    }

    /**
     * Check if consumed nutrition is within goals (with tolerance).
     *
     * @param consumed Nutrition consumed
     * @param goals Daily nutrition goals
     * @param tolerancePercent Allowed percentage over goals (e.g., 10 for 10%)
     * @return true if within goals plus tolerance
     */
    public static boolean isWithinGoals(NutritionInfo consumed, NutritionInfo goals, double tolerancePercent) {
        if (consumed == null || goals == null) {
            throw new IllegalArgumentException("Nutrition info cannot be null");
        }
        
        double toleranceMultiplier = 1.0 + (tolerancePercent / 100.0);
        
        int maxCalories = (int) Math.round(goals.getCalories() * toleranceMultiplier);
        double maxProtein = goals.getProtein() * toleranceMultiplier;
        double maxCarbs = goals.getCarbs() * toleranceMultiplier;
        double maxFat = goals.getFat() * toleranceMultiplier;
        
        boolean caloriesOk = consumed.getCalories() <= maxCalories;
        boolean proteinOk = consumed.getProtein() <= maxProtein;
        boolean carbsOk = consumed.getCarbs() <= maxCarbs;
        boolean fatOk = consumed.getFat() <= maxFat;
        
        return caloriesOk && proteinOk && carbsOk && fatOk;
    }

    /**
     * Format nutrition info as a readable string.
     *
     * @param nutrition Nutrition info to format
     * @return Formatted string (e.g., "250 cal, 10g protein, 30g carbs, 8g fat")
     */
    public static String formatNutritionSummary(NutritionInfo nutrition) {
        if (nutrition == null) {
            throw new IllegalArgumentException("Nutrition info cannot be null");
        }
        
        return String.format("%d cal, %.1fg protein, %.1fg carbs, %.1fg fat",
            nutrition.getCalories(),
            nutrition.getProtein(),
            nutrition.getCarbs(),
            nutrition.getFat()
        );
    }

    private NutritionCalculator() {
        // Utility class - prevent instantiation
    }
}
