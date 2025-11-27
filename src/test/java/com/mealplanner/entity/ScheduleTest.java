package com.mealplanner.entity;

import com.mealplanner.exception.ScheduleConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
        schedule = new Schedule("schedule123", "user123");
        today = LocalDate.now();
        tomorrow = today.plusDays(1);
    }

    @Test
    public void testScheduleCreation() {
        // Test schedule creation with valid user ID
        Schedule validSchedule = new Schedule("sched456", "user456");
        assertEquals("sched456", validSchedule.getScheduleId());
        assertEquals("user456", validSchedule.getUserId());
        assertTrue(validSchedule.isEmpty());

        // Test schedule creation with invalid user ID throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            new Schedule("", "userId");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Schedule("scheduleId", "");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Schedule(null, "userId");
        });
    }

    @Test
    public void testAddMeal() throws ScheduleConflictException {
        // Test adding meal to empty slot
        schedule.addMeal(today, MealType.BREAKFAST, "recipe123");
        assertTrue(schedule.hasMeal(today, MealType.BREAKFAST));
        assertEquals("recipe123", schedule.getMeal(today, MealType.BREAKFAST).get());

        // Test adding meal to occupied slot throws exception
        assertThrows(ScheduleConflictException.class, () -> {
            schedule.addMeal(today, MealType.BREAKFAST, "recipe456");
        });

        // Test adding meals for different meal types on same day
        schedule.addMeal(today, MealType.LUNCH, "lunch_recipe");
        schedule.addMeal(today, MealType.DINNER, "dinner_recipe");

        assertEquals(3, schedule.getMealCount());
    }

    @Test
    public void testRemoveMeal() throws ScheduleConflictException {
        schedule.addMeal(today, MealType.BREAKFAST, "recipe123");
        schedule.addMeal(today, MealType.LUNCH, "recipe456");

        // Test removing existing meal
        schedule.removeMeal(today, MealType.BREAKFAST);
        assertFalse(schedule.hasMeal(today, MealType.BREAKFAST));
        assertTrue(schedule.hasMeal(today, MealType.LUNCH));

        // Test removing from empty slot (should not throw exception)
        schedule.removeMeal(today, MealType.DINNER);
        assertFalse(schedule.hasMeal(today, MealType.DINNER));
    }

    @Test
    public void testUpdateMeal() throws ScheduleConflictException {
        schedule.addMeal(today, MealType.BREAKFAST, "recipe123");

        // Test updating existing meal
        schedule.updateMeal(today, MealType.BREAKFAST, "new_recipe");
        assertEquals("new_recipe", schedule.getMeal(today, MealType.BREAKFAST).get());

        // Test updating non-existent meal throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            schedule.updateMeal(today, MealType.LUNCH, "recipe456");
        });
    }

    @Test
    public void testGetMealsForDate() throws ScheduleConflictException {
        // Test retrieving all meals for a specific date
        schedule.addMeal(today, MealType.BREAKFAST, "breakfast");
        schedule.addMeal(today, MealType.LUNCH, "lunch");
        schedule.addMeal(today, MealType.DINNER, "dinner");

        Map<MealType, String> mealsToday = schedule.getMealsForDate(today);
        assertEquals(3, mealsToday.size());
        assertEquals("breakfast", mealsToday.get(MealType.BREAKFAST));
        assertEquals("lunch", mealsToday.get(MealType.LUNCH));
        assertEquals("dinner", mealsToday.get(MealType.DINNER));

        // Test retrieving meals for date with no meals
        Map<MealType, String> mealsForEmptyDate = schedule.getMealsForDate(tomorrow);
        assertTrue(mealsForEmptyDate.isEmpty());
    }

    @Test
    public void testGetMeal() throws ScheduleConflictException {
        schedule.addMeal(today, MealType.BREAKFAST, "recipe123");

        // Test getting existing meal
        assertTrue(schedule.getMeal(today, MealType.BREAKFAST).isPresent());
        assertEquals("recipe123", schedule.getMeal(today, MealType.BREAKFAST).get());

        // Test getting non-existent meal
        assertFalse(schedule.getMeal(today, MealType.LUNCH).isPresent());
        assertFalse(schedule.getMeal(tomorrow, MealType.BREAKFAST).isPresent());

        // Test getting with null parameters
        assertFalse(schedule.getMeal(null, MealType.BREAKFAST).isPresent());
        assertFalse(schedule.getMeal(today, null).isPresent());
    }

    @Test
    public void testGetAllMeals() throws ScheduleConflictException {
        schedule.addMeal(today, MealType.BREAKFAST, "breakfast_today");
        schedule.addMeal(tomorrow, MealType.LUNCH, "lunch_tomorrow");

        Map<LocalDate, Map<MealType, String>> allMeals = schedule.getAllMeals();

        assertEquals(2, allMeals.size());
        assertTrue(allMeals.containsKey(today));
        assertTrue(allMeals.containsKey(tomorrow));
    }

    @Test
    public void testGetMealsBetween() throws ScheduleConflictException {
        LocalDate date1 = today;
        LocalDate date2 = today.plusDays(1);
        LocalDate date3 = today.plusDays(2);
        LocalDate date4 = today.plusDays(3);

        schedule.addMeal(date1, MealType.BREAKFAST, "meal1");
        schedule.addMeal(date2, MealType.BREAKFAST, "meal2");
        schedule.addMeal(date3, MealType.BREAKFAST, "meal3");
        schedule.addMeal(date4, MealType.BREAKFAST, "meal4");

        // Test getting meals in date range (inclusive)
        Map<LocalDate, Map<MealType, String>> rangeResult = schedule.getMealsBetween(date2, date3);

        assertEquals(2, rangeResult.size());
        assertTrue(rangeResult.containsKey(date2));
        assertTrue(rangeResult.containsKey(date3));
        assertFalse(rangeResult.containsKey(date1));
        assertFalse(rangeResult.containsKey(date4));

        // Test invalid range throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            schedule.getMealsBetween(date3, date2);
        });
    }

    @Test
    public void testClearMealsBefore() throws ScheduleConflictException {
        LocalDate pastDate = today.minusDays(5);
        LocalDate recentDate = today.minusDays(1);

        schedule.addMeal(pastDate, MealType.BREAKFAST, "old_meal");
        schedule.addMeal(recentDate, MealType.BREAKFAST, "recent_meal");
        schedule.addMeal(today, MealType.BREAKFAST, "today_meal");

        schedule.clearMealsBefore(today);

        assertFalse(schedule.hasMeal(pastDate, MealType.BREAKFAST));
        assertFalse(schedule.hasMeal(recentDate, MealType.BREAKFAST));
        assertTrue(schedule.hasMeal(today, MealType.BREAKFAST));
    }

    @Test
    public void testIsEmpty() throws ScheduleConflictException {
        assertTrue(schedule.isEmpty());

        schedule.addMeal(today, MealType.BREAKFAST, "recipe");
        assertFalse(schedule.isEmpty());

        schedule.removeMeal(today, MealType.BREAKFAST);
        assertTrue(schedule.isEmpty());
    }

    @Test
    public void testIsSlotFree() throws ScheduleConflictException {
        assertTrue(schedule.isSlotFree(today, MealType.BREAKFAST));

        schedule.addMeal(today, MealType.BREAKFAST, "recipe");
        assertFalse(schedule.isSlotFree(today, MealType.BREAKFAST));
        assertTrue(schedule.isSlotFree(today, MealType.LUNCH));

        // Test with null parameters
        assertFalse(schedule.isSlotFree(null, MealType.BREAKFAST));
        assertFalse(schedule.isSlotFree(today, null));
    }

    @Test
    public void testGetMealCount() throws ScheduleConflictException {
        assertEquals(0, schedule.getMealCount());

        schedule.addMeal(today, MealType.BREAKFAST, "recipe1");
        assertEquals(1, schedule.getMealCount());

        schedule.addMeal(today, MealType.LUNCH, "recipe2");
        schedule.addMeal(tomorrow, MealType.BREAKFAST, "recipe3");
        assertEquals(3, schedule.getMealCount());
    }

    @Test
    public void testCopy() throws ScheduleConflictException {
        schedule.addMeal(today, MealType.BREAKFAST, "breakfast");
        schedule.addMeal(today, MealType.LUNCH, "lunch");

        Schedule copy = schedule.copy();

        assertEquals(schedule.getScheduleId(), copy.getScheduleId());
        assertEquals(schedule.getUserId(), copy.getUserId());
        assertEquals(schedule.getMealCount(), copy.getMealCount());
        assertTrue(copy.hasMeal(today, MealType.BREAKFAST));
        assertTrue(copy.hasMeal(today, MealType.LUNCH));

        // Modifying copy should not affect original
        copy.removeMeal(today, MealType.BREAKFAST);
        assertTrue(schedule.hasMeal(today, MealType.BREAKFAST));
    }

    @Test
    public void testEquals() {
        Schedule schedule1 = new Schedule("id1", "user1");
        Schedule schedule2 = new Schedule("id1", "user1");
        Schedule schedule3 = new Schedule("id2", "user1");

        assertEquals(schedule1, schedule2);
        assertNotEquals(schedule1, schedule3);
    }

    @Test
    public void testHashCode() {
        Schedule schedule1 = new Schedule("id1", "user1");
        Schedule schedule2 = new Schedule("id1", "user1");

        assertEquals(schedule1.hashCode(), schedule2.hashCode());
    }

    @Test
    public void testToString() throws ScheduleConflictException {
        schedule.addMeal(today, MealType.BREAKFAST, "recipe");

        String result = schedule.toString();

        assertNotNull(result);
        assertTrue(result.contains("schedule123"));
        assertTrue(result.contains("user123"));
    }
}
