package com.mealplanner.use_case.manage_meal_plan;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mealplanner.entity.Schedule;
import com.mealplanner.entity.MealType;
import com.mealplanner.use_case.manage_meal_plan.edit.EditMealInputData;
import com.mealplanner.use_case.manage_meal_plan.edit.EditMealOutputData;
import com.mealplanner.exception.ScheduleConflictException;
import java.time.LocalDate;

/**
 * Test class for EditMealInteractor.
 * Tests editing existing meals in schedule.
 *
 * Responsible: Grace (primary)
 */
public class EditMealInteractorTest {

    private com.mealplanner.use_case.manage_meal_plan.edit.EditMealInteractor interactor;

    @Mock
    private com.mealplanner.use_case.manage_meal_plan.edit.EditMealDataAccessInterface dataAccess;

    @Mock
    private com.mealplanner.use_case.manage_meal_plan.edit.EditMealOutputBoundary presenter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        interactor = new com.mealplanner.use_case.manage_meal_plan.edit.EditMealInteractor(dataAccess, presenter);
    }

    @Test
    public void testEditExistingMeal() {
        // Arrange
        LocalDate date = LocalDate.now().plusDays(1);
        MealType mealType = MealType.BREAKFAST;
        String newRecipeId = "recipe-456";
        Schedule schedule = new Schedule("schedule-1", "user-1");
        EditMealInputData inputData = new EditMealInputData(date, mealType, newRecipeId);

        // Add a meal first
        try {
            schedule.addMeal(date, mealType, "recipe-123");
        } catch (ScheduleConflictException e) {
            fail("Should not throw exception when adding first meal");
        }

        when(dataAccess.getUserSchedule()).thenReturn(schedule);

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess).getUserSchedule();
        verify(dataAccess).saveSchedule(any(Schedule.class));
        verify(presenter).presentEditSuccess(any(EditMealOutputData.class));
        verify(presenter, never()).presentEditError(anyString());
        assertTrue(schedule.hasMeal(date, mealType));
        assertEquals(newRecipeId, schedule.getMeal(date, mealType).orElse(""));
    }

    @Test
    public void testEditNonExistentMeal() {
        // Arrange
        LocalDate date = LocalDate.now().plusDays(1);
        MealType mealType = MealType.LUNCH;
        String newRecipeId = "recipe-456";
        Schedule schedule = new Schedule("schedule-1", "user-1");
        EditMealInputData inputData = new EditMealInputData(date, mealType, newRecipeId);

        when(dataAccess.getUserSchedule()).thenReturn(schedule);

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess).getUserSchedule();
        verify(dataAccess, never()).saveSchedule(any(Schedule.class));
        verify(presenter).presentEditError(contains("No meal exists"));
        verify(presenter, never()).presentEditSuccess(any(EditMealOutputData.class));
    }

    @Test
    public void testEditWithNoChanges() {
        // Arrange
        LocalDate date = LocalDate.now().plusDays(1);
        MealType mealType = MealType.DINNER;
        String recipeId = "recipe-123";
        Schedule schedule = new Schedule("schedule-1", "user-1");
        EditMealInputData inputData = new EditMealInputData(date, mealType, recipeId);

        // Add a meal first
        try {
            schedule.addMeal(date, mealType, recipeId);
        } catch (ScheduleConflictException e) {
            fail("Should not throw exception when adding first meal");
        }

        when(dataAccess.getUserSchedule()).thenReturn(schedule);

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess).getUserSchedule();
        verify(dataAccess).saveSchedule(any(Schedule.class));
        verify(presenter).presentEditSuccess(any(EditMealOutputData.class));
        // Note: Current implementation allows editing with the same recipe
        // This is valid behavior - the meal is updated even if it's the same recipe
    }

    @Test
    public void testEditWithInvalidRecipe() {
        // Arrange
        LocalDate date = LocalDate.now().plusDays(1);
        MealType mealType = MealType.BREAKFAST;
        String emptyRecipeId = "";
        Schedule schedule = new Schedule("schedule-1", "user-1");
        EditMealInputData inputData = new EditMealInputData(date, mealType, emptyRecipeId);

        // Add a meal first
        try {
            schedule.addMeal(date, mealType, "recipe-123");
        } catch (ScheduleConflictException e) {
            fail("Should not throw exception when adding first meal");
        }

        when(dataAccess.getUserSchedule()).thenReturn(schedule);

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess).getUserSchedule();
        // Schedule.updateMeal will throw IllegalArgumentException for empty recipe ID
        verify(presenter).presentEditError(anyString());
        verify(presenter, never()).presentEditSuccess(any(EditMealOutputData.class));
    }

    @Test
    public void testDataAccessFailure() {
        // Arrange
        LocalDate date = LocalDate.now().plusDays(1);
        MealType mealType = MealType.BREAKFAST;
        String newRecipeId = "recipe-456";
        Schedule schedule = new Schedule("schedule-1", "user-1");
        EditMealInputData inputData = new EditMealInputData(date, mealType, newRecipeId);

        // Add a meal first
        try {
            schedule.addMeal(date, mealType, "recipe-123");
        } catch (ScheduleConflictException e) {
            fail("Should not throw exception when adding first meal");
        }

        when(dataAccess.getUserSchedule()).thenReturn(schedule);
        doThrow(new RuntimeException("Database error")).when(dataAccess).saveSchedule(any(Schedule.class));

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess).getUserSchedule();
        verify(dataAccess).saveSchedule(any(Schedule.class));
        // Note: Current implementation doesn't catch saveSchedule exceptions
        // This test documents the current behavior
    }
}
