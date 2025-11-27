package com.mealplanner.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for NutritionGoals entity.
 * Tests nutrition goal validation and comparison logic.
 *
 * Responsible: Everyone (shared entity created in P0)
 */
public class NutritionGoalsTest {

    private NutritionGoals goals;
    private NutritionInfo consumed;

    @BeforeEach
    public void setUp() {
        goals = new NutritionGoals(2000, 150.0, 200.0, 70.0);
        consumed = new NutritionInfo(1500, 100.0, 150.0, 50.0);
    }

    @Test
    public void testGoalsCreation() {
        // Test creating goals with valid values
        NutritionGoals validGoals = new NutritionGoals(2500, 180.0, 250.0, 80.0);
        assertEquals(2500, validGoals.getDailyCalories());
        assertEquals(180.0, validGoals.getDailyProtein());
        assertEquals(250.0, validGoals.getDailyCarbs());
        assertEquals(80.0, validGoals.getDailyFat());

        // Test creating goals with negative values throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            new NutritionGoals(-2000, 150.0, 200.0, 70.0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new NutritionGoals(2000, -150.0, 200.0, 70.0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new NutritionGoals(2000, 150.0, -200.0, 70.0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new NutritionGoals(2000, 150.0, 200.0, -70.0);
        });
    }

    @Test
    public void testIsWithinGoals() {
        // Test when nutrition is within goals
        assertTrue(goals.isWithinGoals(consumed));

        // Test when nutrition is under goals
        NutritionInfo underGoals = new NutritionInfo(1000, 50.0, 100.0, 30.0);
        assertTrue(goals.isWithinGoals(underGoals));

        // Test when nutrition exceeds goals (calories)
        NutritionInfo exceededCalories = new NutritionInfo(2100, 100.0, 150.0, 50.0);
        assertFalse(goals.isWithinGoals(exceededCalories));

        // Test when nutrition exceeds goals (protein)
        NutritionInfo exceededProtein = new NutritionInfo(1500, 160.0, 150.0, 50.0);
        assertFalse(goals.isWithinGoals(exceededProtein));

        // Test with null throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            goals.isWithinGoals(null);
        });
    }

    @Test
    public void testCalculateRemaining() {
        // Test calculating remaining nutrition
        NutritionInfo remaining = goals.calculateRemaining(consumed);
        assertEquals(500, remaining.getCalories());
        assertEquals(50.0, remaining.getProtein());
        assertEquals(50.0, remaining.getCarbs());
        assertEquals(20.0, remaining.getFat());

        // Test with zero consumed
        NutritionInfo zeroConsumed = new NutritionInfo(0, 0.0, 0.0, 0.0);
        NutritionInfo allRemaining = goals.calculateRemaining(zeroConsumed);
        assertEquals(2000, allRemaining.getCalories());
        assertEquals(150.0, allRemaining.getProtein());

        // Test when goals are exceeded throws exception
        NutritionInfo exceeded = new NutritionInfo(2500, 100.0, 150.0, 50.0);
        assertThrows(UnsupportedOperationException.class, () -> {
            goals.calculateRemaining(exceeded);
        });

        // Test with null throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            goals.calculateRemaining(null);
        });
    }

    @Test
    public void testCalculatePercentages() {
        // Test percentage calculation
        double[] percentages = goals.calculatePercentages(consumed);
        assertEquals(4, percentages.length);
        assertEquals(75.0, percentages[0], 0.01); // calories: 1500/2000 * 100
        assertEquals(66.67, percentages[1], 0.01); // protein: 100/150 * 100
        assertEquals(75.0, percentages[2], 0.01); // carbs: 150/200 * 100
        assertEquals(71.43, percentages[3], 0.01); // fat: 50/70 * 100

        // Test with zero goals throws exception
        NutritionGoals zeroGoals = new NutritionGoals(0, 0.0, 0.0, 0.0);
        assertThrows(UnsupportedOperationException.class, () -> {
            zeroGoals.calculatePercentages(consumed);
        });

        // Test with null throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            goals.calculatePercentages(null);
        });
    }

    @Test
    public void testCreateDefault() {
        // Test default 2000 calorie goals
        NutritionGoals defaultGoals = NutritionGoals.createDefault();
        assertEquals(2000, defaultGoals.getDailyCalories());

        // Verify default macros are reasonable
        assertTrue(defaultGoals.getDailyProtein() > 0);
        assertTrue(defaultGoals.getDailyCarbs() > 0);
        assertTrue(defaultGoals.getDailyFat() > 0);
    }

    @Test
    public void testEqualsAndHashCode() {
        // Test equals with same values
        NutritionGoals same = new NutritionGoals(2000, 150.0, 200.0, 70.0);
        assertEquals(goals, same);
        assertEquals(goals.hashCode(), same.hashCode());

        // Test equals with different values
        NutritionGoals different = new NutritionGoals(2500, 150.0, 200.0, 70.0);
        assertNotEquals(goals, different);

        // Test hashCode consistency
        assertEquals(goals.hashCode(), goals.hashCode());
    }

    @Test
    public void testToString() {
        String result = goals.toString();
        assertNotNull(result);
        assertTrue(result.contains("2000"));
        assertTrue(result.contains("150.0"));
        assertTrue(result.contains("200.0"));
        assertTrue(result.contains("70.0"));
    }
}
