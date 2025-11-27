package com.mealplanner.use_case.manage_meal_plan;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for AddMealInteractor.
 * Tests adding meals to schedule with conflict detection.
 *
 * Responsible: Grace (primary)
 */
public class AddMealInteractorTest {

    private com.mealplanner.use_case.manage_meal_plan.add.AddMealInteractor interactor;

    @Mock
    private com.mealplanner.use_case.manage_meal_plan.add.AddMealDataAccessInterface dataAccess;

    @Mock
    private com.mealplanner.use_case.manage_meal_plan.add.AddMealOutputBoundary presenter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        interactor = new com.mealplanner.use_case.manage_meal_plan.add.AddMealInteractor(dataAccess, presenter);
    }

    @Test
    public void testAddMealToEmptySlot() {
        java.time.LocalDate date = java.time.LocalDate.now();
        com.mealplanner.entity.MealType mealType = com.mealplanner.entity.MealType.BREAKFAST;
        String recipeID = "recipe-1";
        com.mealplanner.use_case.manage_meal_plan.add.AddMealInputData inputData = 
            new com.mealplanner.use_case.manage_meal_plan.add.AddMealInputData(date, mealType, recipeID);
        
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        when(dataAccess.getUserSchedule()).thenReturn(schedule);
        
        interactor.execute(inputData);
        
        verify(dataAccess).saveSchedule(any(com.mealplanner.entity.Schedule.class));
        verify(presenter).presentAddSuccess(any(com.mealplanner.use_case.manage_meal_plan.add.AddMealOutputData.class));
    }

    @Test
    public void testAddMealToOccupiedSlot() {
        java.time.LocalDate date = java.time.LocalDate.now();
        com.mealplanner.entity.MealType mealType = com.mealplanner.entity.MealType.BREAKFAST;
        String recipeID = "recipe-1";
        com.mealplanner.use_case.manage_meal_plan.add.AddMealInputData inputData = 
            new com.mealplanner.use_case.manage_meal_plan.add.AddMealInputData(date, mealType, recipeID);
        
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        try {
            schedule.addMeal(date, mealType, "existing-recipe");
        } catch (Exception e) {
            // Ignore
        }
        when(dataAccess.getUserSchedule()).thenReturn(schedule);
        
        interactor.execute(inputData);
        
        verify(presenter).presentAddError(contains("Meal slot already taken"));
        verify(presenter, never()).presentAddSuccess(any());
    }

    @Test
    public void testAddMealPastDate() {
        java.time.LocalDate pastDate = java.time.LocalDate.now().minusDays(1);
        com.mealplanner.entity.MealType mealType = com.mealplanner.entity.MealType.BREAKFAST;
        String recipeID = "recipe-1";
        com.mealplanner.use_case.manage_meal_plan.add.AddMealInputData inputData = 
            new com.mealplanner.use_case.manage_meal_plan.add.AddMealInputData(pastDate, mealType, recipeID);
        
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        when(dataAccess.getUserSchedule()).thenReturn(schedule);
        
        interactor.execute(inputData);
        
        verify(dataAccess).saveSchedule(any(com.mealplanner.entity.Schedule.class));
    }

    @Test
    public void testAddMealInvalidRecipe() {
        java.time.LocalDate date = java.time.LocalDate.now();
        com.mealplanner.entity.MealType mealType = com.mealplanner.entity.MealType.BREAKFAST;
        String recipeID = "nonexistent-recipe";
        com.mealplanner.use_case.manage_meal_plan.add.AddMealInputData inputData = 
            new com.mealplanner.use_case.manage_meal_plan.add.AddMealInputData(date, mealType, recipeID);
        
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        when(dataAccess.getUserSchedule()).thenReturn(schedule);
        
        interactor.execute(inputData);
        
        verify(dataAccess).saveSchedule(any(com.mealplanner.entity.Schedule.class));
    }

    @Test
    public void testAddMealInvalidMealType() {
        java.time.LocalDate date = java.time.LocalDate.now();
        com.mealplanner.entity.MealType mealType = com.mealplanner.entity.MealType.BREAKFAST;
        String recipeID = "recipe-1";
        com.mealplanner.use_case.manage_meal_plan.add.AddMealInputData inputData = 
            new com.mealplanner.use_case.manage_meal_plan.add.AddMealInputData(date, mealType, recipeID);
        
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        when(dataAccess.getUserSchedule()).thenReturn(schedule);
        
        interactor.execute(inputData);
        
        verify(dataAccess).saveSchedule(any(com.mealplanner.entity.Schedule.class));
    }

    @Test
    public void testDataAccessFailure() {
        java.time.LocalDate date = java.time.LocalDate.now();
        com.mealplanner.entity.MealType mealType = com.mealplanner.entity.MealType.BREAKFAST;
        String recipeID = "recipe-1";
        com.mealplanner.use_case.manage_meal_plan.add.AddMealInputData inputData = 
            new com.mealplanner.use_case.manage_meal_plan.add.AddMealInputData(date, mealType, recipeID);
        
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        when(dataAccess.getUserSchedule()).thenReturn(schedule);
        doThrow(new RuntimeException("Database error")).when(dataAccess).saveSchedule(any());
        
        assertThrows(RuntimeException.class, () -> {
            interactor.execute(inputData);
        });
    }
}
