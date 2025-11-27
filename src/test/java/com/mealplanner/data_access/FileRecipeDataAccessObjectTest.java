package com.mealplanner.data_access;

// Tests for recipe file persistence.
// Responsible: Aaryan (primary), Everyone (testing)

import com.mealplanner.data_access.database.FileRecipeDataAccessObject;
import com.mealplanner.data_access.database.FileScheduleDataAccessObject;
import com.mealplanner.entity.MealType;
import com.mealplanner.entity.Schedule;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FileRecipeDataAccessObjectTest {
    @Test
    void saveSchedule_savesWithoutError() {
        Schedule schedule = createTestSchedule();
        FileScheduleDataAccessObject dao = new FileScheduleDataAccessObject();
        // pass schedule into your input data / interactor here
        dao.saveSchedule(schedule);
    }

    @Test
    void saveAndLoadSchedule_roundTrip() {
        FileScheduleDataAccessObject dao = new FileScheduleDataAccessObject();

        // ---- build a test schedule ----
        String scheduleId = "test-schedule-1";
        String userId = "test-user";

        Map<LocalDate, Map<MealType, String>> mealsByDate = new HashMap<>();

        EnumMap<MealType, String> day1Meals = new EnumMap<>(MealType.class);
        day1Meals.put(MealType.BREAKFAST, "recipe-bf-1");
        day1Meals.put(MealType.LUNCH, "recipe-l-1");
        day1Meals.put(MealType.DINNER, "recipe-d-1");

        LocalDate date = LocalDate.of(2024, 11, 27);
        mealsByDate.put(date, day1Meals);

        Schedule original = new Schedule(scheduleId, userId, mealsByDate);

        // ---- save ----
        dao.saveSchedule(original);

        // ---- load ----
        Schedule loaded = dao.loadSchedule(scheduleId);
        assertNotNull(loaded, "Loaded schedule should not be null");

        // ---- basic field checks ----
        assertEquals(scheduleId, loaded.getScheduleId());
        assertEquals(userId, loaded.getUserId());

        // ---- check one meal survived the round-trip ----
        Map<LocalDate, Map<MealType, String>> loadedMeals = loaded.getAllMeals();
        assertTrue(loadedMeals.containsKey(date));
        assertEquals("recipe-bf-1",
                loadedMeals.get(date).get(MealType.BREAKFAST));
    }

    public static Schedule createTestSchedule() {
        String scheduleId = "test-schedule-1";
        String userId = "test-user";

        // Map<LocalDate, Map<MealType, String>>
        Map<LocalDate, Map<MealType, String>> initialMeals = new HashMap<>();

        // Day 1
        EnumMap<MealType, String> day1Meals = new EnumMap<>(MealType.class);
        day1Meals.put(MealType.BREAKFAST, "recipe-bf-1");
        day1Meals.put(MealType.LUNCH,     "recipe-l-1");
        day1Meals.put(MealType.DINNER,    "recipe-d-1");
        initialMeals.put(LocalDate.of(2024, 11, 27), day1Meals);

        // Day 2
        EnumMap<MealType, String> day2Meals = new EnumMap<>(MealType.class);
        day2Meals.put(MealType.BREAKFAST, "recipe-bf-2");
        day2Meals.put(MealType.DINNER,    "recipe-d-2");
        initialMeals.put(LocalDate.of(2024, 11, 28), day2Meals);

        // Use your 3-arg constructor
        return new Schedule(scheduleId, userId, initialMeals);
    }
}

