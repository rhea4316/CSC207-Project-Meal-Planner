package com.mealplanner.use_case.manage_meal_plan;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        java.time.LocalDate date = java.time.LocalDate.now();
        com.mealplanner.entity.MealType mealType = com.mealplanner.entity.MealType.BREAKFAST;
        com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealInputData inputData = 
            new com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealInputData(date, mealType);
        
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        try {
            schedule.addMeal(date, mealType, "recipe-1");
        } catch (Exception e) {
            // Ignore
        }
        when(dataAccess.getUserSchedule()).thenReturn(schedule);
        
        interactor.execute(inputData);
        
        verify(dataAccess).saveSchedule(any(com.mealplanner.entity.Schedule.class));
        verify(presenter).presentDeleteSuccess(any(com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealOutputData.class));
    }

    @Test
    public void testDeleteNonExistentMeal() {
        java.time.LocalDate date = java.time.LocalDate.now();
        com.mealplanner.entity.MealType mealType = com.mealplanner.entity.MealType.BREAKFAST;
        com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealInputData inputData = 
            new com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealInputData(date, mealType);
        
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        when(dataAccess.getUserSchedule()).thenReturn(schedule);
        
        interactor.execute(inputData);
        
        verify(presenter).presentDeleteError(contains("No meal exists"));
        verify(presenter, never()).presentDeleteSuccess(any());
    }

    @Test
    public void testDeleteInvalidDate() {
        java.time.LocalDate date = java.time.LocalDate.now();
        com.mealplanner.entity.MealType mealType = com.mealplanner.entity.MealType.BREAKFAST;
        com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealInputData inputData = 
            new com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealInputData(date, mealType);
        
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        when(dataAccess.getUserSchedule()).thenReturn(schedule);
        
        interactor.execute(inputData);
        
        verify(presenter).presentDeleteError(anyString());
    }

    @Test
    public void testDataAccessFailure() {
        java.time.LocalDate date = java.time.LocalDate.now();
        com.mealplanner.entity.MealType mealType = com.mealplanner.entity.MealType.BREAKFAST;
        com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealInputData inputData = 
            new com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealInputData(date, mealType);
        
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        try {
            schedule.addMeal(date, mealType, "recipe-1");
        } catch (Exception e) {
            // Ignore
        }
        when(dataAccess.getUserSchedule()).thenReturn(schedule);
        doThrow(new RuntimeException("Database error")).when(dataAccess).saveSchedule(any());
        
        assertThrows(RuntimeException.class, () -> {
            interactor.execute(inputData);
        });
    }
}
