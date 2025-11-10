package com.mealplanner.entity;

import com.mealplanner.exception.ScheduleConflictException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Schedule {

    private final String scheduleId;
    private final String userId;
    private final Map<LocalDate, EnumMap<MealType, String>> mealsByDate;

    public Schedule(String scheduleId, String userId) {
        this(scheduleId, userId, Collections.emptyMap());
    }

    public Schedule(String scheduleId,
                    String userId,
                    Map<LocalDate, Map<MealType, String>> initialMeals) {
        this.scheduleId = validateId(scheduleId, "scheduleId");
        this.userId = validateId(userId, "userId");
        this.mealsByDate = new HashMap<>();
        if (initialMeals != null) {
            initialMeals.forEach((date, meals) -> {
                requireNonNull(date, "date");
                if (meals == null) {
                    throw new IllegalArgumentException("Meal map cannot be null");
                }
                meals.forEach((mealType, recipeId) -> {
                    requireNonNull(mealType, "mealType");
                    String safeRecipeId = validateId(recipeId, "recipeId");
                    putMeal(date, mealType, safeRecipeId);
                });
            });
        }
    }

    private static String validateId(String value, String field) {
        Objects.requireNonNull(value, field + " cannot be null");
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
        return value.trim();
    }

    private static <T> T requireNonNull(T value, String name) {
        return Objects.requireNonNull(value, name + " cannot be null");
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public String getUserId() {
        return userId;
    }

    /** Add a meal if the slot is empty. */
    public void addMeal(LocalDate date, MealType mealType, String recipeId)
            throws ScheduleConflictException {
        LocalDate safeDate = requireNonNull(date, "date");
        MealType safeMealType = requireNonNull(mealType, "mealType");
        String safeRecipeId = validateId(recipeId, "recipeId");

        EnumMap<MealType, String> mealsForDate = mealsByDate.computeIfAbsent(safeDate, ignored -> new EnumMap<>(MealType.class));
        if (mealsForDate.containsKey(safeMealType)) {
            throw new ScheduleConflictException(safeDate.toString(), safeMealType.getDisplayName());
        }
        mealsForDate.put(safeMealType, safeRecipeId);
    }

    /** Replace an existing meal slot with a new recipe. */
    public void updateMeal(LocalDate date, MealType mealType, String recipeId) {
        LocalDate safeDate = requireNonNull(date, "date");
        MealType safeMealType = requireNonNull(mealType, "mealType");
        String safeRecipeId = validateId(recipeId, "recipeId");
        EnumMap<MealType, String> mealsForDate = mealsByDate.get(safeDate);
        if (mealsForDate == null || !mealsForDate.containsKey(safeMealType)) {
            throw new IllegalArgumentException("No existing meal for " + safeMealType + " on " + safeDate);
        }
        mealsForDate.put(safeMealType, safeRecipeId);
    }

    /** Delete a meal slot if it exists. */
    public void removeMeal(LocalDate date, MealType mealType) {
        LocalDate safeDate = requireNonNull(date, "date");
        MealType safeMealType = requireNonNull(mealType, "mealType");
        EnumMap<MealType, String> mealsForDate = mealsByDate.get(safeDate);
        if (mealsForDate == null || mealsForDate.remove(safeMealType) == null) {
            return;
        }
        if (mealsForDate.isEmpty()) {
            mealsByDate.remove(safeDate);
        }
    }

    /** Check whether a meal exists for the given day and slot. */
    public boolean hasMeal(LocalDate date, MealType mealType) {
        EnumMap<MealType, String> mealsForDate = mealsByDate.get(date);
        return mealsForDate != null && mealsForDate.containsKey(mealType);
    }

    /** Look up a recipe id for a specific slot. */
    public Optional<String> getMeal(LocalDate date, MealType mealType) {
        if (date == null || mealType == null) {
            return Optional.empty();
        }
        EnumMap<MealType, String> mealsForDate = mealsByDate.get(date);
        if (mealsForDate == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(mealsForDate.get(mealType));
    }

    /** View all meals scheduled for one date. */
    public Map<MealType, String> getMealsForDate(LocalDate date) {
        EnumMap<MealType, String> mealsForDate = mealsByDate.get(date);
        if (mealsForDate == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new EnumMap<>(mealsForDate));
    }

    /** View every stored meal grouped by date. */
    public Map<LocalDate, Map<MealType, String>> getAllMeals() {
        Map<LocalDate, Map<MealType, String>> copy = new HashMap<>();
        mealsByDate.forEach((date, meals) ->
                copy.put(date, Collections.unmodifiableMap(new EnumMap<>(meals))));
        return Collections.unmodifiableMap(copy);
    }

    /** View meals in the provided date range (inclusive). */
    public Map<LocalDate, Map<MealType, String>> getMealsBetween(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "start cannot be null");
        Objects.requireNonNull(end, "end cannot be null");
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("start date cannot be after end date");
        }

        Map<LocalDate, Map<MealType, String>> result = new HashMap<>();
        mealsByDate.forEach((date, meals) -> {
            if (!date.isBefore(start) && !date.isAfter(end)) {
                result.put(date, Collections.unmodifiableMap(new EnumMap<>(meals)));
            }
        });
        return Collections.unmodifiableMap(result);
    }

    /** Drop every meal scheduled before the cutoff date. */
    public void clearMealsBefore(LocalDate cutoff) {
        if (cutoff == null) {
            return;
        }
        mealsByDate.entrySet().removeIf(entry -> entry.getKey().isBefore(cutoff));
    }

    /** Count how many meal slots are filled. */
    public int getMealCount() {
        return mealsByDate.values().stream()
                .mapToInt(Map::size)
                .sum();
    }

    /** True when the schedule has no meals at all. */
    public boolean isEmpty() {
        return mealsByDate.isEmpty();
    }

    /** Quick helper to see if the slot is free. */
    public boolean isSlotFree(LocalDate date, MealType mealType) {
        if (date == null || mealType == null) {
            return false;
        }
        EnumMap<MealType, String> mealsForDate = mealsByDate.get(date);
        return mealsForDate == null || !mealsForDate.containsKey(mealType);
    }

    private void putMeal(LocalDate date, MealType mealType, String recipeId) {
        EnumMap<MealType, String> mealsForDate = mealsByDate.computeIfAbsent(date, ignored -> new EnumMap<>(MealType.class));
        mealsForDate.put(mealType, recipeId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Schedule)) return false;
        Schedule schedule = (Schedule) o;
        return scheduleId.equals(schedule.scheduleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheduleId);
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "scheduleId='" + scheduleId + '\'' +
                ", userId='" + userId + '\'' +
                ", mealCount=" + getMealCount() +
                '}';
    }

    /** Create a deep copy of the current schedule. */
    public Schedule copy() {
        Map<LocalDate, Map<MealType, String>> snapshot = new HashMap<>();
        mealsByDate.forEach((date, meals) -> snapshot.put(date, new EnumMap<>(meals)));
        return new Schedule(scheduleId, userId, snapshot);
    }
}
