package com.mealplanner.entity;

import com.mealplanner.exception.ScheduleConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Test class for Schedule entity.
 * Tests meal scheduling, conflict detection, and schedule modifications.
 *
 * Responsible: Grace (primary), Everyone (shared entity)
 */
public class ScheduleTest {

    private Schedule schedule;
    private LocalDate today;
    private LocalDate tomorrow;

    @BeforeEach
    public void setUp() {
        schedule = new Schedule("schedule-1", "user-1");
    }

    @Test
    public void testScheduleCreation() {
        Schedule newSchedule = new Schedule("schedule-1", "user-1");
        assertEquals("schedule-1", newSchedule.getScheduleId());
        assertEquals("user-1", newSchedule.getUserId());
        assertTrue(newSchedule.isEmpty());
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Schedule("", "user-1");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Schedule("schedule-1", "");
        });
        
        assertThrows(NullPointerException.class, () -> {
            new Schedule(null, "user-1");
        });
        
        assertThrows(NullPointerException.class, () -> {
            new Schedule("schedule-1", null);
        });
    }

    @Test
    public void testAddMeal() throws ScheduleConflictException {
        LocalDate today = LocalDate.now();
        schedule.addMeal(today, MealType.BREAKFAST, "recipe-1");
        
        assertTrue(schedule.hasMeal(today, MealType.BREAKFAST));
        assertEquals("recipe-1", schedule.getMeal(today, MealType.BREAKFAST).orElse(""));
        
        assertThrows(ScheduleConflictException.class, () -> {
            schedule.addMeal(today, MealType.BREAKFAST, "recipe-2");
        });
    }

    @Test
    public void testRemoveMeal() throws ScheduleConflictException {
        LocalDate today = LocalDate.now();
        schedule.addMeal(today, MealType.BREAKFAST, "recipe-1");
        
        assertTrue(schedule.hasMeal(today, MealType.BREAKFAST));
        schedule.removeMeal(today, MealType.BREAKFAST);
        assertFalse(schedule.hasMeal(today, MealType.BREAKFAST));
        
        schedule.removeMeal(today, MealType.LUNCH);
        assertFalse(schedule.hasMeal(today, MealType.LUNCH));
    }

    @Test
    public void testUpdateMeal() throws ScheduleConflictException {
        LocalDate today = LocalDate.now();
        schedule.addMeal(today, MealType.BREAKFAST, "recipe-1");
        
        schedule.updateMeal(today, MealType.BREAKFAST, "recipe-2");
        assertEquals("recipe-2", schedule.getMeal(today, MealType.BREAKFAST).orElse(""));
        
        assertThrows(IllegalArgumentException.class, () -> {
            schedule.updateMeal(today, MealType.LUNCH, "recipe-3");
        });
    }

    @Test
    public void testGetMealsForDate() throws ScheduleConflictException {
        LocalDate today = LocalDate.now();
        schedule.addMeal(today, MealType.BREAKFAST, "recipe-1");
        schedule.addMeal(today, MealType.LUNCH, "recipe-2");
        schedule.addMeal(today, MealType.DINNER, "recipe-3");
        
        Map<MealType, String> meals = schedule.getMealsForDate(today);
        assertEquals(3, meals.size());
        assertEquals("recipe-1", meals.get(MealType.BREAKFAST));
        assertEquals("recipe-2", meals.get(MealType.LUNCH));
        assertEquals("recipe-3", meals.get(MealType.DINNER));
        
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Map<MealType, String> emptyMeals = schedule.getMealsForDate(tomorrow);
        assertTrue(emptyMeals.isEmpty());
    }

    @Test
    public void testConflictDetection() throws ScheduleConflictException {
        LocalDate today = LocalDate.now();
        schedule.addMeal(today, MealType.BREAKFAST, "recipe-1");
        
        assertThrows(ScheduleConflictException.class, () -> {
            schedule.addMeal(today, MealType.BREAKFAST, "recipe-2");
        });
        
        schedule.addMeal(today, MealType.LUNCH, "recipe-3");
        assertTrue(schedule.hasMeal(today, MealType.LUNCH));
    }

    @Test
    public void testWeeklySchedule() throws ScheduleConflictException {
        LocalDate start = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate date = start.plusDays(i);
            schedule.addMeal(date, MealType.BREAKFAST, "recipe-" + i);
        }
        
        Map<LocalDate, Map<MealType, String>> allMeals = schedule.getAllMeals();
        assertEquals(7, allMeals.size());
        
        LocalDate end = start.plusDays(6);
        Map<LocalDate, Map<MealType, String>> weeklyMeals = schedule.getMealsBetween(start, end);
        assertEquals(7, weeklyMeals.size());
        
        LocalDate partialStart = start.plusDays(2);
        LocalDate partialEnd = start.plusDays(4);
        Map<LocalDate, Map<MealType, String>> partialMeals = schedule.getMealsBetween(partialStart, partialEnd);
        assertEquals(3, partialMeals.size());
    }
}
