package com.mealplanner.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// Core entity representing a single day's meal plan (breakfast, lunch, dinner).
// Responsible: Grace (primary for meal plan management), Mona (view schedule)
// TODO: Implement meal plan class with method to calculate total daily nutrition from all meals

public class MealPlan {
//dictionary for breakfast, lunch, dinner
//int: serving size
    private final Map<MealType, Recipe> meals;
    private int serving_size;

    public MealPlan(Recipe breakfast, Recipe lunch, Recipe dinner) {
        this.meals = new HashMap<>();
        meals.put(MealType.BREAKFAST, breakfast);
        meals.put(MealType.LUNCH, lunch);
        meals.put(MealType.DINNER, dinner);

        serving_size = breakfast.getServingSize() + lunch.getServingSize() + dinner.getServingSize();
    }


    // Getters
    public int getServing_size() {return serving_size;}

    public Recipe getBreakfast() {return meals.get(MealType.BREAKFAST);}

    public Recipe getLunch() {return meals.get(MealType.LUNCH);}

    public Recipe getDinner() {return meals.get(MealType.DINNER);}

    public Map<MealType, Recipe> getMeals() {return meals;}


    // Setters
    public void setBreakfast(Recipe breakfast) {
        meals.put(MealType.BREAKFAST, breakfast);
        serving_size = breakfast.getServingSize() +
                meals.get(MealType.LUNCH).getServingSize() +
                meals.get(MealType.DINNER).getServingSize();
    }

    public void setLunch(Recipe lunch) {
        meals.put(MealType.LUNCH, lunch);
        serving_size = meals.get(MealType.BREAKFAST).getServingSize() +
                lunch.getServingSize() +
                meals.get(MealType.DINNER).getServingSize();
    }

    public void setDinner(Recipe dinner) {
        meals.put(MealType.DINNER, dinner);
        serving_size = meals.get(MealType.BREAKFAST).getServingSize() +
                meals.get(MealType.LUNCH).getServingSize() +
                dinner.getServingSize();
    }


    // Business Methods
    public NutritionInfo getTotalDailyNutrition() {
        int calories = meals.get(MealType.BREAKFAST).getNutritionInfo().getCalories() +
                meals.get(MealType.LUNCH).getNutritionInfo().getCalories() +
                meals.get(MealType.DINNER).getNutritionInfo().getCalories();

        double proteins = meals.get(MealType.BREAKFAST).getNutritionInfo().getProtein() +
                meals.get(MealType.LUNCH).getNutritionInfo().getProtein() +
                meals.get(MealType.DINNER).getNutritionInfo().getProtein();

        double carbs = meals.get(MealType.BREAKFAST).getNutritionInfo().getCarbs() +
                meals.get(MealType.LUNCH).getNutritionInfo().getCarbs() +
                meals.get(MealType.DINNER).getNutritionInfo().getCarbs();

        double fats = meals.get(MealType.BREAKFAST).getNutritionInfo().getFat() +
                meals.get(MealType.LUNCH).getNutritionInfo().getFat() +
                meals.get(MealType.DINNER).getNutritionInfo().getFat();

        return new NutritionInfo(calories, proteins, carbs, fats);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MealPlan other = (MealPlan) o;
        return meals.get(MealType.BREAKFAST) == other.meals.get(MealType.BREAKFAST)
                && meals.get(MealType.LUNCH) == other.meals.get(MealType.LUNCH)
                && meals.get(MealType.DINNER) == other.meals.get(MealType.DINNER);
    }

    @Override
    public int hashCode() {
        return Objects.hash(meals, serving_size);
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
