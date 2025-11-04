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
            return new NutritionInfo(0, 0.0, 0.0, 0.0);
        }

        int totalCalories = 0;
        double totalProtein = 0.0;
        double totalCarbs = 0.0;
        double totalFat = 0.0;

        for (Ingredient ingredient : ingredients) {
            totalCalories += ingredient.getCalories();
            totalProtein += ingredient.getProtein();
            totalCarbs += ingredient.getCarbs();
            totalFat += ingredient.getFat();
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
        if (multiplier <= 0) {
            throw new IllegalArgumentException("Multiplier must be positive");
        }

        return new NutritionInfo(
            (int) Math.round(original.getCalories() * multiplier),
            NumberUtil.round(original.getProtein() * multiplier, 1),
            NumberUtil.round(original.getCarbs() * multiplier, 1),
            NumberUtil.round(original.getFat() * multiplier, 1)
        );
    }

    /**
     * Calculate the percentage of daily value for each macronutrient.
     *
     * @param nutrition Nutrition info to calculate percentages for
     * @return Array of percentages [calories%, protein%, carbs%, fat%]
     */
    public static double[] calculateDailyValuePercentages(NutritionInfo nutrition) {
        return new double[] {
            (nutrition.getCalories() * 100.0) / STANDARD_DAILY_CALORIES,
            (nutrition.getProtein() * 100.0) / STANDARD_DAILY_PROTEIN,
            (nutrition.getCarbs() * 100.0) / STANDARD_DAILY_CARBS,
            (nutrition.getFat() * 100.0) / STANDARD_DAILY_FAT
        };
    }

    /**
     * Calculate macronutrient breakdown as percentages of total calories.
     *
     * @param nutrition Nutrition info
     * @return Array of percentages [protein%, carbs%, fat%]
     */
    public static double[] calculateMacroPercentages(NutritionInfo nutrition) {
        int totalCalories = nutrition.getCalories();

        if (totalCalories == 0) {
            return new double[] {0.0, 0.0, 0.0};
        }

        int proteinCalories = (int) (nutrition.getProtein() * CALORIES_PER_GRAM_PROTEIN);
        int carbCalories = (int) (nutrition.getCarbs() * CALORIES_PER_GRAM_CARBS);
        int fatCalories = (int) (nutrition.getFat() * CALORIES_PER_GRAM_FAT);

        return new double[] {
            NumberUtil.round((proteinCalories * 100.0) / totalCalories, 1),
            NumberUtil.round((carbCalories * 100.0) / totalCalories, 1),
            NumberUtil.round((fatCalories * 100.0) / totalCalories, 1)
        };
    }

    /**
     * Check if nutrition values are within healthy macronutrient ranges.
     * Standard ranges: Protein 10-35%, Carbs 45-65%, Fat 20-35%
     *
     * @param nutrition Nutrition info to validate
     * @return true if within healthy ranges
     */
    public static boolean isBalancedMacros(NutritionInfo nutrition) {
        double[] percentages = calculateMacroPercentages(nutrition);
        double proteinPercent = percentages[0];
        double carbPercent = percentages[1];
        double fatPercent = percentages[2];

        return proteinPercent >= 10 && proteinPercent <= 35
            && carbPercent >= 45 && carbPercent <= 65
            && fatPercent >= 20 && fatPercent <= 35;
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
        return (int) Math.round(
            (protein * CALORIES_PER_GRAM_PROTEIN) +
            (carbs * CALORIES_PER_GRAM_CARBS) +
            (fat * CALORIES_PER_GRAM_FAT)
        );
    }

    /**
     * Calculate remaining nutrition to meet daily goals.
     *
     * @param consumed Nutrition already consumed
     * @param goals Daily nutrition goals
     * @return NutritionInfo representing remaining amounts
     */
    public static NutritionInfo calculateRemaining(NutritionInfo consumed, NutritionInfo goals) {
        return new NutritionInfo(
            Math.max(0, goals.getCalories() - consumed.getCalories()),
            Math.max(0, NumberUtil.round(goals.getProtein() - consumed.getProtein(), 1)),
            Math.max(0, NumberUtil.round(goals.getCarbs() - consumed.getCarbs(), 1)),
            Math.max(0, NumberUtil.round(goals.getFat() - consumed.getFat(), 1))
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
        double factor = 1.0 + (tolerancePercent / 100.0);

        return consumed.getCalories() <= goals.getCalories() * factor
            && consumed.getProtein() <= goals.getProtein() * factor
            && consumed.getCarbs() <= goals.getCarbs() * factor
            && consumed.getFat() <= goals.getFat() * factor;
    }

    /**
     * Format nutrition info as a readable string.
     *
     * @param nutrition Nutrition info to format
     * @return Formatted string (e.g., "250 cal, 10g protein, 30g carbs, 8g fat")
     */
    public static String formatNutritionSummary(NutritionInfo nutrition) {
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
