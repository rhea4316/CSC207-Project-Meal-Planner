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
        java.time.LocalDate date = java.time.LocalDate.now();
        com.mealplanner.entity.MealType mealType = com.mealplanner.entity.MealType.BREAKFAST;
        String recipeID = "recipe-2";
        com.mealplanner.use_case.manage_meal_plan.edit.EditMealInputData inputData = 
            new com.mealplanner.use_case.manage_meal_plan.edit.EditMealInputData(date, mealType, recipeID);
        
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        try {
            schedule.addMeal(date, mealType, "recipe-1");
        } catch (Exception e) {
            // Ignore
        }
        when(dataAccess.getUserSchedule()).thenReturn(schedule);
        
        interactor.execute(inputData);
        
        verify(dataAccess).saveSchedule(any(com.mealplanner.entity.Schedule.class));
        verify(presenter).presentEditSuccess(any(com.mealplanner.use_case.manage_meal_plan.edit.EditMealOutputData.class));
    }

    @Test
    public void testEditNonExistentMeal() {
        java.time.LocalDate date = java.time.LocalDate.now();
        com.mealplanner.entity.MealType mealType = com.mealplanner.entity.MealType.BREAKFAST;
        String recipeID = "recipe-2";
        com.mealplanner.use_case.manage_meal_plan.edit.EditMealInputData inputData = 
            new com.mealplanner.use_case.manage_meal_plan.edit.EditMealInputData(date, mealType, recipeID);
        
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        when(dataAccess.getUserSchedule()).thenReturn(schedule);
        
        interactor.execute(inputData);
        
        verify(presenter).presentEditError(contains("No meal exists"));
        verify(presenter, never()).presentEditSuccess(any());
    }

    @Test
    public void testEditWithNoChanges() {
        java.time.LocalDate date = java.time.LocalDate.now();
        com.mealplanner.entity.MealType mealType = com.mealplanner.entity.MealType.BREAKFAST;
        String recipeID = "recipe-1";
        com.mealplanner.use_case.manage_meal_plan.edit.EditMealInputData inputData = 
            new com.mealplanner.use_case.manage_meal_plan.edit.EditMealInputData(date, mealType, recipeID);
        
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        try {
            schedule.addMeal(date, mealType, recipeID);
        } catch (Exception e) {
            // Ignore
        }
        when(dataAccess.getUserSchedule()).thenReturn(schedule);
        
        interactor.execute(inputData);
        
        verify(dataAccess).saveSchedule(any(com.mealplanner.entity.Schedule.class));
        verify(presenter).presentEditSuccess(any());
    }

    @Test
    public void testEditWithInvalidRecipe() {
        java.time.LocalDate date = java.time.LocalDate.now();
        com.mealplanner.entity.MealType mealType = com.mealplanner.entity.MealType.BREAKFAST;
        String recipeID = "nonexistent-recipe";
        com.mealplanner.use_case.manage_meal_plan.edit.EditMealInputData inputData = 
            new com.mealplanner.use_case.manage_meal_plan.edit.EditMealInputData(date, mealType, recipeID);
        
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        try {
            schedule.addMeal(date, mealType, "recipe-1");
        } catch (Exception e) {
            // Ignore
        }
        when(dataAccess.getUserSchedule()).thenReturn(schedule);
        
        interactor.execute(inputData);
        
        verify(dataAccess).saveSchedule(any(com.mealplanner.entity.Schedule.class));
    }

    @Test
    public void testDataAccessFailure() {
        java.time.LocalDate date = java.time.LocalDate.now();
        com.mealplanner.entity.MealType mealType = com.mealplanner.entity.MealType.BREAKFAST;
        String recipeID = "recipe-2";
        com.mealplanner.use_case.manage_meal_plan.edit.EditMealInputData inputData = 
            new com.mealplanner.use_case.manage_meal_plan.edit.EditMealInputData(date, mealType, recipeID);
        
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
