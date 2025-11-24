package com.mealplanner.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// Core entity representing a single day's meal plan (breakfast, lunch, dinner).
// Responsible: Grace (primary for meal plan management), Mona (view schedule)

public class MealPlan {
    private final Map<MealType, Recipe> meals;
    private int servingSize;

    public MealPlan(Recipe breakfast, Recipe lunch, Recipe dinner) {
        if (breakfast == null || lunch == null || dinner == null) {
            throw new IllegalArgumentException("All meals (breakfast, lunch, dinner) must be non-null");
        }
        this.meals = new HashMap<>();
        meals.put(MealType.BREAKFAST, breakfast);
        meals.put(MealType.LUNCH, lunch);
        meals.put(MealType.DINNER, dinner);

        servingSize = breakfast.getServingSize() + lunch.getServingSize() + dinner.getServingSize();
    }

    // Getters
    public int getServingSize() {
        return servingSize;
    }

    public Recipe getBreakfast() {return meals.get(MealType.BREAKFAST);}

    public Recipe getLunch() {return meals.get(MealType.LUNCH);}

    public Recipe getDinner() {return meals.get(MealType.DINNER);}

    public Map<MealType, Recipe> getMeals() {return meals;}


    // Setters
    public void setBreakfast(Recipe breakfast) {
        if (breakfast == null) {
            throw new IllegalArgumentException("Breakfast cannot be null");
        }
        meals.put(MealType.BREAKFAST, breakfast);
        servingSize = breakfast.getServingSize() +
                meals.get(MealType.LUNCH).getServingSize() +
                meals.get(MealType.DINNER).getServingSize();
    }

    public void setLunch(Recipe lunch) {
        if (lunch == null) {
            throw new IllegalArgumentException("Lunch cannot be null");
        }
        meals.put(MealType.LUNCH, lunch);
        servingSize = meals.get(MealType.BREAKFAST).getServingSize() +
                lunch.getServingSize() +
                meals.get(MealType.DINNER).getServingSize();
    }

    public void setDinner(Recipe dinner) {
        if (dinner == null) {
            throw new IllegalArgumentException("Dinner cannot be null");
        }
        meals.put(MealType.DINNER, dinner);
        servingSize = meals.get(MealType.BREAKFAST).getServingSize() +
                meals.get(MealType.LUNCH).getServingSize() +
                dinner.getServingSize();
    }


    // Business Methods
    public NutritionInfo getTotalDailyNutrition() {
        Recipe breakfast = meals.get(MealType.BREAKFAST);
        Recipe lunch = meals.get(MealType.LUNCH);
        Recipe dinner = meals.get(MealType.DINNER);
        
        NutritionInfo breakfastNutrition = breakfast.getNutritionInfo();
        NutritionInfo lunchNutrition = lunch.getNutritionInfo();
        NutritionInfo dinnerNutrition = dinner.getNutritionInfo();
        
        // Handle null nutrition info by using empty NutritionInfo
        if (breakfastNutrition == null) breakfastNutrition = NutritionInfo.empty();
        if (lunchNutrition == null) lunchNutrition = NutritionInfo.empty();
        if (dinnerNutrition == null) dinnerNutrition = NutritionInfo.empty();
        
        int calories = breakfastNutrition.getCalories() +
                lunchNutrition.getCalories() +
                dinnerNutrition.getCalories();

        double proteins = breakfastNutrition.getProtein() +
                lunchNutrition.getProtein() +
                dinnerNutrition.getProtein();

        double carbs = breakfastNutrition.getCarbs() +
                lunchNutrition.getCarbs() +
                dinnerNutrition.getCarbs();

        double fats = breakfastNutrition.getFat() +
                lunchNutrition.getFat() +
                dinnerNutrition.getFat();

        return new NutritionInfo(calories, proteins, carbs, fats);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MealPlan other = (MealPlan) o;
        return Objects.equals(meals.get(MealType.BREAKFAST), other.meals.get(MealType.BREAKFAST))
                && Objects.equals(meals.get(MealType.LUNCH), other.meals.get(MealType.LUNCH))
                && Objects.equals(meals.get(MealType.DINNER), other.meals.get(MealType.DINNER));
    }

    @Override
    public int hashCode() {
        return Objects.hash(meals, servingSize);
    }

    @Override
    public String toString() {
        return "Meal Plan {" +
                "breakfast=" + meals.get(MealType.BREAKFAST).getName() +
                ", lunch=" + meals.get(MealType.LUNCH).getName() +
                ", dinner=" + meals.get(MealType.DINNER).getName() +
                '}';
    }

}
