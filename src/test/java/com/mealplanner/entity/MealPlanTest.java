package com.mealplanner.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for MealPlan entity.
 * Tests daily meal plan creation and nutrition calculation.
 *
 * Responsible: Everyone (shared entity)
 */
public class MealPlanTest {

    private MealPlan mealPlan;
    private Recipe breakfast;
    private Recipe lunch;
    private Recipe dinner;

    @BeforeEach
    public void setUp() {
        breakfast = new Recipe("Oatmeal", Arrays.asList("oats", "milk"), "Cook oats", 1,
                new NutritionInfo(300, 10.0, 50.0, 5.0), 10, null, "breakfast_1");

        lunch = new Recipe("Chicken Salad", Arrays.asList("chicken", "lettuce"), "Mix ingredients", 1,
                new NutritionInfo(400, 30.0, 20.0, 15.0), 15, null, "lunch_1");

        dinner = new Recipe("Salmon", Arrays.asList("salmon", "rice"), "Grill salmon", 1,
                new NutritionInfo(500, 40.0, 30.0, 20.0), 25, null, "dinner_1");

        mealPlan = new MealPlan(breakfast, lunch, dinner);
    }

    @Test
    public void testMealPlanCreation() {
        // Test meal plan creation with valid recipes
        assertNotNull(mealPlan);
        assertEquals(breakfast, mealPlan.getBreakfast());
        assertEquals(lunch, mealPlan.getLunch());
        assertEquals(dinner, mealPlan.getDinner());

        // Test meal plan creation with null recipes throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            new MealPlan(null, lunch, dinner);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new MealPlan(breakfast, null, dinner);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new MealPlan(breakfast, lunch, null);
        });
    }

    @Test
    public void testAddMeal() {
        // Test adding breakfast
        Recipe newBreakfast = new Recipe("Pancakes", Arrays.asList("flour", "eggs"), "Cook", 1);
        mealPlan.setBreakfast(newBreakfast);
        assertEquals(newBreakfast, mealPlan.getBreakfast());

        // Test adding lunch
        Recipe newLunch = new Recipe("Burger", Arrays.asList("bun", "patty"), "Grill", 1);
        mealPlan.setLunch(newLunch);
        assertEquals(newLunch, mealPlan.getLunch());

        // Test adding dinner
        Recipe newDinner = new Recipe("Steak", Arrays.asList("beef"), "Grill", 1);
        mealPlan.setDinner(newDinner);
        assertEquals(newDinner, mealPlan.getDinner());

        // Test adding null meal throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            mealPlan.setBreakfast(null);
        });
    }

    @Test
    public void testTotalNutritionCalculation() {
        // Test calculating total daily calories
        NutritionInfo totalNutrition = mealPlan.getTotalDailyNutrition();

        // Total: 300 + 400 + 500 = 1200 calories
        assertEquals(1200, totalNutrition.getCalories());

        // Total protein: 10 + 30 + 40 = 80g
        assertEquals(80.0, totalNutrition.getProtein());

        // Total carbs: 50 + 20 + 30 = 100g
        assertEquals(100.0, totalNutrition.getCarbs());

        // Total fat: 5 + 15 + 20 = 40g
        assertEquals(40.0, totalNutrition.getFat());
    }

    @Test
    public void testTotalNutritionWithNullNutrition() {
        // Test with recipes without nutrition info
        Recipe noNutritionBreakfast = new Recipe("Toast", Arrays.asList("bread"), "Toast", 1);
        Recipe noNutritionLunch = new Recipe("Soup", Arrays.asList("water", "vegetables"), "Boil", 1);
        Recipe noNutritionDinner = new Recipe("Pasta", Arrays.asList("pasta"), "Cook", 1);

        MealPlan emptyNutrition = new MealPlan(noNutritionBreakfast, noNutritionLunch, noNutritionDinner);
        NutritionInfo totalNutrition = emptyNutrition.getTotalDailyNutrition();

        // Should handle null nutrition info gracefully
        assertEquals(0, totalNutrition.getCalories());
        assertEquals(0.0, totalNutrition.getProtein());
    }

    @Test
    public void testGetMealByType() {
        // Test getting specific meal type
        Map<MealType, Recipe> meals = mealPlan.getMeals();
        assertEquals(breakfast, meals.get(MealType.BREAKFAST));
        assertEquals(lunch, meals.get(MealType.LUNCH));
        assertEquals(dinner, meals.get(MealType.DINNER));
    }

    @Test
    public void testGetServingSize() {
        // Serving size should be sum of all meal serving sizes
        assertEquals(3, mealPlan.getServingSize());
    }

    @Test
    public void testServingSizeUpdatesWhenMealsChange() {
        Recipe twoServingBreakfast = new Recipe("Big Breakfast", Arrays.asList("eggs", "bacon"),
                "Cook", 2);

        mealPlan.setBreakfast(twoServingBreakfast);

        // New serving size: 2 (breakfast) + 1 (lunch) + 1 (dinner) = 4
        assertEquals(4, mealPlan.getServingSize());
    }

    @Test
    public void testEquals() {
        MealPlan mealPlan1 = new MealPlan(breakfast, lunch, dinner);
        MealPlan mealPlan2 = new MealPlan(breakfast, lunch, dinner);

        Recipe differentDinner = new Recipe("Pizza", Arrays.asList("dough", "cheese"), "Bake", 1);
        MealPlan mealPlan3 = new MealPlan(breakfast, lunch, differentDinner);

        assertEquals(mealPlan1, mealPlan2);
        assertNotEquals(mealPlan1, mealPlan3);
    }

    @Test
    public void testHashCode() {
        MealPlan mealPlan1 = new MealPlan(breakfast, lunch, dinner);
        MealPlan mealPlan2 = new MealPlan(breakfast, lunch, dinner);

        assertEquals(mealPlan1.hashCode(), mealPlan2.hashCode());
    }

    @Test
    public void testToString() {
        String result = mealPlan.toString();

        assertNotNull(result);
        assertTrue(result.contains("Oatmeal"));
        assertTrue(result.contains("Chicken Salad"));
        assertTrue(result.contains("Salmon"));
    }
}
