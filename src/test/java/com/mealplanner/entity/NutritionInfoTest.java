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
        nutritionInfo = new NutritionInfo(500, 20.0, 60.0, 15.0);
    }

    @Test
    public void testNutritionInfoCreation() {
        NutritionInfo info = new NutritionInfo(500, 20.0, 60.0, 15.0);
        assertEquals(500, info.getCalories());
        assertEquals(20.0, info.getProtein());
        assertEquals(60.0, info.getCarbs());
        assertEquals(15.0, info.getFat());
        
        NutritionInfo zeroInfo = new NutritionInfo(0, 0.0, 0.0, 0.0);
        assertEquals(0, zeroInfo.getCalories());
        assertEquals(0.0, zeroInfo.getProtein());
        assertEquals(0.0, zeroInfo.getCarbs());
        assertEquals(0.0, zeroInfo.getFat());
        
        assertThrows(IllegalArgumentException.class, () -> {
            new NutritionInfo(-1, 20.0, 60.0, 15.0);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new NutritionInfo(500, -1.0, 60.0, 15.0);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new NutritionInfo(500, 20.0, -1.0, 15.0);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new NutritionInfo(500, 20.0, 60.0, -1.0);
        });
    }

    @Test
    public void testGetters() {
        assertEquals(500, nutritionInfo.getCalories());
        assertEquals(20.0, nutritionInfo.getProtein());
        assertEquals(60.0, nutritionInfo.getCarbs());
        assertEquals(15.0, nutritionInfo.getFat());
        
        NutritionInfo original = new NutritionInfo(500, 20.0, 60.0, 15.0);
        original.scale(2.0);
        assertEquals(500, original.getCalories());
        assertEquals(20.0, original.getProtein());
    }

    @Test
    public void testScaling() {
        NutritionInfo scaled = nutritionInfo.scale(2.0);
        assertEquals(1000, scaled.getCalories());
        assertEquals(40.0, scaled.getProtein(), 0.01);
        assertEquals(120.0, scaled.getCarbs(), 0.01);
        assertEquals(30.0, scaled.getFat(), 0.01);
        
        NutritionInfo fractionalScaled = nutritionInfo.scale(0.5);
        assertEquals(250, fractionalScaled.getCalories());
        assertEquals(10.0, fractionalScaled.getProtein(), 0.01);
        assertEquals(30.0, fractionalScaled.getCarbs(), 0.01);
        assertEquals(7.5, fractionalScaled.getFat(), 0.01);
        
        NutritionInfo zeroScaled = nutritionInfo.scale(0.0);
        assertEquals(0, zeroScaled.getCalories());
        assertEquals(0.0, zeroScaled.getProtein());
        assertEquals(0.0, zeroScaled.getCarbs());
        assertEquals(0.0, zeroScaled.getFat());
        
        assertThrows(IllegalArgumentException.class, () -> {
            nutritionInfo.scale(-1.0);
        });
    }

    @Test
    public void testAddition() {
        NutritionInfo other = new NutritionInfo(300, 15.0, 40.0, 10.0);
        NutritionInfo sum = nutritionInfo.add(other);
        
        assertEquals(800, sum.getCalories());
        assertEquals(35.0, sum.getProtein(), 0.01);
        assertEquals(100.0, sum.getCarbs(), 0.01);
        assertEquals(25.0, sum.getFat(), 0.01);
        
        assertThrows(IllegalArgumentException.class, () -> {
            nutritionInfo.add(null);
        });
    }

    @Test
    public void testEqualsAndHashCode() {
        NutritionInfo info1 = new NutritionInfo(500, 20.0, 60.0, 15.0);
        NutritionInfo info2 = new NutritionInfo(500, 20.0, 60.0, 15.0);
        NutritionInfo info3 = new NutritionInfo(300, 15.0, 40.0, 10.0);
        
        assertEquals(info1, info2);
        assertNotEquals(info1, info3);
        assertEquals(info1.hashCode(), info2.hashCode());
    }

    @Test
    public void testEmpty() {
        NutritionInfo empty = NutritionInfo.empty();
        assertEquals(0, empty.getCalories());
        assertEquals(0.0, empty.getProtein());
        assertEquals(0.0, empty.getCarbs());
        assertEquals(0.0, empty.getFat());
    }
}
