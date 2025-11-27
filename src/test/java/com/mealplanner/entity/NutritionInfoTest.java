package com.mealplanner.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for NutritionInfo entity.
 * Tests nutrition data validation and calculations.
 *
 * Responsible: Everyone (shared entity)
 */
public class NutritionInfoTest {

    private NutritionInfo nutritionInfo;

    @BeforeEach
    public void setUp() {
        nutritionInfo = new NutritionInfo(500, 30.0, 60.0, 15.0);
    }

    @Test
    public void testNutritionInfoCreation() {
        // Test creating with valid values
        NutritionInfo info = new NutritionInfo(300, 20.0, 40.0, 10.0);
        assertEquals(300, info.getCalories());
        assertEquals(20.0, info.getProtein());
        assertEquals(40.0, info.getCarbs());
        assertEquals(10.0, info.getFat());

        // Test creating with zero values
        NutritionInfo zeroInfo = new NutritionInfo(0, 0.0, 0.0, 0.0);
        assertEquals(0, zeroInfo.getCalories());
        assertEquals(0.0, zeroInfo.getProtein());

        // Test creating with negative values throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            new NutritionInfo(-100, 20.0, 40.0, 10.0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new NutritionInfo(300, -20.0, 40.0, 10.0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new NutritionInfo(300, 20.0, -40.0, 10.0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new NutritionInfo(300, 20.0, 40.0, -10.0);
        });
    }

    @Test
    public void testGetters() {
        assertEquals(500, nutritionInfo.getCalories());
        assertEquals(30.0, nutritionInfo.getProtein());
        assertEquals(60.0, nutritionInfo.getCarbs());
        assertEquals(15.0, nutritionInfo.getFat());
    }

    @Test
    public void testScaling() {
        // Test scaling nutrition by multiplier
        NutritionInfo scaled = nutritionInfo.scale(2.0);
        assertEquals(1000, scaled.getCalories());
        assertEquals(60.0, scaled.getProtein());
        assertEquals(120.0, scaled.getCarbs());
        assertEquals(30.0, scaled.getFat());

        // Test scaling with fractional multiplier
        NutritionInfo halfScaled = nutritionInfo.scale(0.5);
        assertEquals(250, halfScaled.getCalories());
        assertEquals(15.0, halfScaled.getProtein());
        assertEquals(30.0, halfScaled.getCarbs());
        assertEquals(7.5, halfScaled.getFat());

        // Test scaling with negative value throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            nutritionInfo.scale(-1.0);
        });

        // Original should remain unchanged (immutability)
        assertEquals(500, nutritionInfo.getCalories());
    }

    @Test
    public void testAddition() {
        // Test adding two NutritionInfo objects
        NutritionInfo other = new NutritionInfo(200, 10.0, 30.0, 5.0);
        NutritionInfo sum = nutritionInfo.add(other);

        assertEquals(700, sum.getCalories());
        assertEquals(40.0, sum.getProtein());
        assertEquals(90.0, sum.getCarbs());
        assertEquals(20.0, sum.getFat());

        // Test adding with null (should error)
        assertThrows(IllegalArgumentException.class, () -> {
            nutritionInfo.add(null);
        });

        // Original should remain unchanged (immutability)
        assertEquals(500, nutritionInfo.getCalories());
    }

    @Test
    public void testEqualsAndHashCode() {
        // Test equals with same values
        NutritionInfo same = new NutritionInfo(500, 30.0, 60.0, 15.0);
        assertEquals(nutritionInfo, same);
        assertEquals(nutritionInfo.hashCode(), same.hashCode());

        // Test equals with different values
        NutritionInfo different = new NutritionInfo(600, 30.0, 60.0, 15.0);
        assertNotEquals(nutritionInfo, different);

        // Test hashCode consistency
        assertEquals(nutritionInfo.hashCode(), nutritionInfo.hashCode());
    }

    @Test
    public void testEmpty() {
        NutritionInfo empty = NutritionInfo.empty();
        assertEquals(0, empty.getCalories());
        assertEquals(0.0, empty.getProtein());
        assertEquals(0.0, empty.getCarbs());
        assertEquals(0.0, empty.getFat());
    }

    @Test
    public void testToString() {
        String result = nutritionInfo.toString();
        assertNotNull(result);
        assertTrue(result.contains("500"));
        assertTrue(result.contains("30.0"));
        assertTrue(result.contains("60.0"));
        assertTrue(result.contains("15.0"));
    }
}
