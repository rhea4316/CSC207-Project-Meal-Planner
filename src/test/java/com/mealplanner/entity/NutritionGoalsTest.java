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
        NutritionGoals newGoals = new NutritionGoals(2000, 150.0, 200.0, 70.0);
        assertEquals(2000, newGoals.getDailyCalories());
        assertEquals(150.0, newGoals.getDailyProtein());
        assertEquals(200.0, newGoals.getDailyCarbs());
        assertEquals(70.0, newGoals.getDailyFat());
        
        assertThrows(IllegalArgumentException.class, () -> {
            new NutritionGoals(-1, 150.0, 200.0, 70.0);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new NutritionGoals(2000, -1.0, 200.0, 70.0);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new NutritionGoals(2000, 150.0, -1.0, 70.0);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new NutritionGoals(2000, 150.0, 200.0, -1.0);
        });
    }

    @Test
    public void testIsWithinGoals() {
        NutritionInfo within = new NutritionInfo(1500, 100.0, 150.0, 50.0);
        assertTrue(goals.isWithinGoals(within));
        
        NutritionInfo exceeds = new NutritionInfo(2500, 200.0, 250.0, 80.0);
        assertFalse(goals.isWithinGoals(exceeds));
        
        NutritionInfo under = new NutritionInfo(1000, 50.0, 100.0, 30.0);
        assertTrue(goals.isWithinGoals(under));
        
        assertThrows(IllegalArgumentException.class, () -> {
            goals.isWithinGoals(null);
        });
    }

    @Test
    public void testCalculateRemaining() {
        NutritionInfo remaining = goals.calculateRemaining(consumed);
        
        assertEquals(500, remaining.getCalories());
        assertEquals(50.0, remaining.getProtein(), 0.01);
        assertEquals(50.0, remaining.getCarbs(), 0.01);
        assertEquals(20.0, remaining.getFat(), 0.01);
        
        NutritionInfo zeroConsumed = new NutritionInfo(0, 0.0, 0.0, 0.0);
        NutritionInfo allRemaining = goals.calculateRemaining(zeroConsumed);
        assertEquals(2000, allRemaining.getCalories());
        assertEquals(150.0, allRemaining.getProtein());
        
        NutritionInfo exceeded = new NutritionInfo(2500, 200.0, 250.0, 80.0);
        assertThrows(UnsupportedOperationException.class, () -> {
            goals.calculateRemaining(exceeded);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            goals.calculateRemaining(null);
        });
    }

    @Test
    public void testCalculatePercentages() {
        double[] percentages = goals.calculatePercentages(consumed);
        
        assertEquals(4, percentages.length);
        assertEquals(75.0, percentages[0], 0.01);
        assertEquals(66.67, percentages[1], 0.01);
        assertEquals(75.0, percentages[2], 0.01);
        assertEquals(71.43, percentages[3], 0.01);
        
        assertThrows(IllegalArgumentException.class, () -> {
            goals.calculatePercentages(null);
        });
        
        NutritionGoals zeroGoals = new NutritionGoals(0, 0.0, 0.0, 0.0);
        assertThrows(UnsupportedOperationException.class, () -> {
            zeroGoals.calculatePercentages(consumed);
        });
    }

    @Test
    public void testCreateDefault() {
        NutritionGoals defaultGoals = NutritionGoals.createDefault();
        
        assertEquals(2000, defaultGoals.getDailyCalories());
        assertEquals(150.0, defaultGoals.getDailyProtein());
        assertEquals(200.0, defaultGoals.getDailyCarbs());
        assertEquals(70.0, defaultGoals.getDailyFat());
    }

    @Test
    public void testEqualsAndHashCode() {
        NutritionGoals goals1 = new NutritionGoals(2000, 150.0, 200.0, 70.0);
        NutritionGoals goals2 = new NutritionGoals(2000, 150.0, 200.0, 70.0);
        NutritionGoals goals3 = new NutritionGoals(1800, 120.0, 180.0, 60.0);
        
        assertEquals(goals1, goals2);
        assertNotEquals(goals1, goals3);
        assertEquals(goals1.hashCode(), goals2.hashCode());
    }
}
