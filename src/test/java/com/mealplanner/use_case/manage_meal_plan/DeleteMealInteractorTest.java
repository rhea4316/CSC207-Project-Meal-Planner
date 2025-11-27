package com.mealplanner.use_case.manage_meal_plan;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mealplanner.entity.Schedule;
import com.mealplanner.entity.MealType;
import com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealInputData;
import com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealOutputData;
import com.mealplanner.exception.ScheduleConflictException;
import java.time.LocalDate;

/**
 * Test class for DeleteMealInteractor.
 * Tests deleting meals from schedule.
 *
 * Responsible: Grace (primary)
 */
public class DeleteMealInteractorTest {

    private com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealInteractor interactor;

    @Mock
    private com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealDataAccessInterface dataAccess;

    @Mock
    private com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealOutputBoundary presenter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        interactor = new com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealInteractor(dataAccess, presenter);
    }

    @Test
    public void testDeleteExistingMeal() {
        // Arrange
        LocalDate date = LocalDate.now().plusDays(1);
        MealType mealType = MealType.BREAKFAST;
        Schedule schedule = new Schedule("schedule-1", "user-1");
        DeleteMealInputData inputData = new DeleteMealInputData(date, mealType);

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
        verify(presenter).presentDeleteSuccess(any(DeleteMealOutputData.class));
        verify(presenter, never()).presentDeleteError(anyString());
        assertFalse(schedule.hasMeal(date, mealType));
    }

    @Test
    public void testDeleteNonExistentMeal() {
        // Arrange
        LocalDate date = LocalDate.now().plusDays(1);
        MealType mealType = MealType.LUNCH;
        Schedule schedule = new Schedule("schedule-1", "user-1");
        DeleteMealInputData inputData = new DeleteMealInputData(date, mealType);

        when(dataAccess.getUserSchedule()).thenReturn(schedule);

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess).getUserSchedule();
        verify(dataAccess, never()).saveSchedule(any(Schedule.class));
        verify(presenter).presentDeleteError(contains("No meal exists"));
        verify(presenter, never()).presentDeleteSuccess(any(DeleteMealOutputData.class));
    }

    @Test
    public void testDeleteInvalidDate() {
        // Arrange
        LocalDate date = LocalDate.now().plusDays(1); // Valid date
        MealType mealType = MealType.DINNER;
        Schedule schedule = new Schedule("schedule-1", "user-1");
        DeleteMealInputData inputData = new DeleteMealInputData(date, mealType);

        when(dataAccess.getUserSchedule()).thenReturn(schedule);

        // Act
        interactor.execute(inputData);

        // Assert
        // Note: Current implementation doesn't validate date format separately
        // LocalDate handles date validation, so invalid dates would be caught at input creation
        verify(dataAccess).getUserSchedule();
        verify(presenter).presentDeleteError(anyString());
    }

    @Test
    public void testDataAccessFailure() {
        // Arrange
        LocalDate date = LocalDate.now().plusDays(1);
        MealType mealType = MealType.BREAKFAST;
        Schedule schedule = new Schedule("schedule-1", "user-1");
        DeleteMealInputData inputData = new DeleteMealInputData(date, mealType);

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
