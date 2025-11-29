package com.mealplanner.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Arrays;

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
    private NutritionInfo breakfastNutrition;
    private NutritionInfo lunchNutrition;
    private NutritionInfo dinnerNutrition;

    @BeforeEach
    public void setUp() {
        breakfastNutrition = new NutritionInfo(300, 10.0, 50.0, 5.0);
        lunchNutrition = new NutritionInfo(500, 20.0, 60.0, 15.0);
        dinnerNutrition = new NutritionInfo(600, 25.0, 70.0, 20.0);

        breakfast = new Recipe(
            "Scrambled Eggs",
            Arrays.asList("2 eggs", "butter", "salt"),
            "Cook eggs in butter",
            2,
            breakfastNutrition,
            10,
            null,
            "recipe-1"
        );

        lunch = new Recipe(
            "Chicken Salad",
            Arrays.asList("chicken", "lettuce", "tomato"),
            "Mix ingredients",
            1,
            lunchNutrition,
            15,
            null,
            "recipe-2"
        );

        dinner = new Recipe(
            "Grilled Salmon",
            Arrays.asList("salmon", "lemon", "herbs"),
            "Grill salmon",
            2,
            dinnerNutrition,
            20,
            null,
            "recipe-3"
        );

        mealPlan = new MealPlan(breakfast, lunch, dinner);
    }

    @Test
    public void testMealPlanCreation() {
        Recipe testBreakfast = new Recipe(
            "Test Breakfast",
            Arrays.asList("ingredient1"),
            "step1",
            1,
            null,
            null,
            null,
            null
        );
        Recipe testLunch = new Recipe(
            "Test Lunch",
            Arrays.asList("ingredient2"),
            "step2",
            1,
            null,
            null,
            null,
            null
        );
        Recipe testDinner = new Recipe(
            "Test Dinner",
            Arrays.asList("ingredient3"),
            "step3",
            1,
            null,
            null,
            null,
            null
        );

        MealPlan newMealPlan = new MealPlan(testBreakfast, testLunch, testDinner);
        assertNotNull(newMealPlan);
        assertEquals(testBreakfast, newMealPlan.getBreakfast());
        assertEquals(testLunch, newMealPlan.getLunch());
        assertEquals(testDinner, newMealPlan.getDinner());
        assertEquals(3, newMealPlan.getServingSize());

        assertThrows(IllegalArgumentException.class, () -> {
            new MealPlan(null, testLunch, testDinner);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new MealPlan(testBreakfast, null, testDinner);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new MealPlan(testBreakfast, testLunch, null);
        });
    }

    @Test
    public void testAddMeal() {
        Recipe newBreakfast = new Recipe(
            "New Breakfast",
            Arrays.asList("new ingredient"),
            "new step",
            1,
            null,
            null,
            null,
            null
        );
        Recipe newLunch = new Recipe(
            "New Lunch",
            Arrays.asList("new ingredient2"),
            "new step2",
            2,
            null,
            null,
            null,
            null
        );
        Recipe newDinner = new Recipe(
            "New Dinner",
            Arrays.asList("new ingredient3"),
            "new step3",
            3,
            null,
            null,
            null,
            null
        );

        mealPlan.setBreakfast(newBreakfast);
        assertEquals(newBreakfast, mealPlan.getBreakfast());
        // After setting breakfast: newBreakfast(1) + lunch(1) + dinner(2) = 4
        assertEquals(4, mealPlan.getServingSize());

        mealPlan.setLunch(newLunch);
        assertEquals(newLunch, mealPlan.getLunch());
        // After setting lunch: newBreakfast(1) + newLunch(2) + dinner(2) = 5
        assertEquals(5, mealPlan.getServingSize());

        mealPlan.setDinner(newDinner);
        assertEquals(newDinner, mealPlan.getDinner());
        // After setting dinner: newBreakfast(1) + newLunch(2) + newDinner(3) = 6
        assertEquals(6, mealPlan.getServingSize());

        assertThrows(IllegalArgumentException.class, () -> {
            mealPlan.setBreakfast(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            mealPlan.setLunch(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            mealPlan.setDinner(null);
        });
    }

    @Test
    public void testTotalNutritionCalculation() {
        NutritionInfo totalNutrition = mealPlan.getTotalDailyNutrition();

        assertEquals(1400, totalNutrition.getCalories());
        assertEquals(55.0, totalNutrition.getProtein(), 0.01);
        assertEquals(180.0, totalNutrition.getCarbs(), 0.01);
        assertEquals(40.0, totalNutrition.getFat(), 0.01);

        Recipe noNutritionBreakfast = new Recipe(
            "No Nutrition Breakfast",
            Arrays.asList("ingredient"),
            "step",
            1,
            null,
            null,
            null,
            null
        );
        Recipe noNutritionLunch = new Recipe(
            "No Nutrition Lunch",
            Arrays.asList("ingredient2"),
            "step2",
            1,
            null,
            null,
            null,
            null
        );
        Recipe noNutritionDinner = new Recipe(
            "No Nutrition Dinner",
            Arrays.asList("ingredient3"),
            "step3",
            1,
            null,
            null,
            null,
            null
        );

        MealPlan mealPlanNoNutrition = new MealPlan(noNutritionBreakfast, noNutritionLunch, noNutritionDinner);
        NutritionInfo totalNutritionEmpty = mealPlanNoNutrition.getTotalDailyNutrition();

        assertEquals(0, totalNutritionEmpty.getCalories());
        assertEquals(0.0, totalNutritionEmpty.getProtein(), 0.01);
        assertEquals(0.0, totalNutritionEmpty.getCarbs(), 0.01);
        assertEquals(0.0, totalNutritionEmpty.getFat(), 0.01);
    }

    @Test
    public void testGetMealByType() {
        assertEquals(breakfast, mealPlan.getBreakfast());
        assertEquals(lunch, mealPlan.getLunch());
        assertEquals(dinner, mealPlan.getDinner());

        assertEquals(breakfast, mealPlan.getMeals().get(MealType.BREAKFAST));
        assertEquals(lunch, mealPlan.getMeals().get(MealType.LUNCH));
        assertEquals(dinner, mealPlan.getMeals().get(MealType.DINNER));
    }

    @Test
    public void testIsComplete() {
        assertNotNull(mealPlan.getBreakfast());
        assertNotNull(mealPlan.getLunch());
        assertNotNull(mealPlan.getDinner());

        assertEquals(3, mealPlan.getMeals().size());
        assertTrue(mealPlan.getMeals().containsKey(MealType.BREAKFAST));
        assertTrue(mealPlan.getMeals().containsKey(MealType.LUNCH));
        assertTrue(mealPlan.getMeals().containsKey(MealType.DINNER));
    }
}
