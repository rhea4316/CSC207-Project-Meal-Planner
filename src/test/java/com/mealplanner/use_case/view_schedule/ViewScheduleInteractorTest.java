package com.mealplanner.use_case.view_schedule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

/**
 * Test class for ViewScheduleInteractor.
 * Tests viewing user's meal schedule.
 *
 * Responsible: Mona (primary)
 */
public class ViewScheduleInteractorTest {

    private ViewScheduleInteractor interactor;

    @Mock
    private com.mealplanner.data_access.database.FileScheduleDataAccessObject dataAccess;

    @Mock
    private ViewScheduleOutputBoundary presenter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        interactor = new ViewScheduleInteractor(dataAccess, presenter);
    }

    @Test
    public void testViewScheduleSuccess() throws com.mealplanner.exception.UserNotFoundException, com.mealplanner.exception.ScheduleConflictException {
        String username = "testuser";
        ViewScheduleInputData inputData = new ViewScheduleInputData(username);
        
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        schedule.addMeal(java.time.LocalDate.now(), com.mealplanner.entity.MealType.BREAKFAST, "recipe-1");
        
        com.mealplanner.entity.User user = new com.mealplanner.entity.User("user-1", username, "password");
        user.setMealSchedule(schedule);
        
        when(dataAccess.getUserByUsername(username)).thenReturn(user);
        
        interactor.execute(inputData);
        
        verify(presenter).presentSchedule(argThat(outputData -> 
            outputData.getUsername().equals(username) &&
            outputData.getSchedule() != null
        ));
    }

    @Test
    public void testViewEmptySchedule() throws com.mealplanner.exception.UserNotFoundException {
        String username = "testuser";
        ViewScheduleInputData inputData = new ViewScheduleInputData(username);
        
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        com.mealplanner.entity.User user = new com.mealplanner.entity.User("user-1", username, "password");
        user.setMealSchedule(schedule);
        
        when(dataAccess.getUserByUsername(username)).thenReturn(user);
        
        interactor.execute(inputData);
        
        verify(presenter).presentError("No Schedule found for user");
    }

    @Test
    public void testViewScheduleInvalidUser() throws com.mealplanner.exception.UserNotFoundException {
        String username = "nonexistent";
        ViewScheduleInputData inputData = new ViewScheduleInputData(username);
        
        when(dataAccess.getUserByUsername(username)).thenThrow(new com.mealplanner.exception.UserNotFoundException("User not found"));
        
        interactor.execute(inputData);
        
        verify(presenter).presentError("Username not found");
    }

    @Test
    public void testViewSpecificDate() throws com.mealplanner.exception.UserNotFoundException, com.mealplanner.exception.ScheduleConflictException {
        String username = "testuser";
        ViewScheduleInputData inputData = new ViewScheduleInputData(username);
        
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        schedule.addMeal(java.time.LocalDate.now(), com.mealplanner.entity.MealType.BREAKFAST, "recipe-1");
        
        com.mealplanner.entity.User user = new com.mealplanner.entity.User("user-1", username, "password");
        user.setMealSchedule(schedule);
        
        when(dataAccess.getUserByUsername(username)).thenReturn(user);
        
        interactor.execute(inputData);
        
        verify(presenter).presentSchedule(any());
    }

    @Test
    public void testViewWeeklySchedule() throws com.mealplanner.exception.UserNotFoundException, com.mealplanner.exception.ScheduleConflictException {
        String username = "testuser";
        ViewScheduleInputData inputData = new ViewScheduleInputData(username);
        
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        java.time.LocalDate start = java.time.LocalDate.now();
        for (int i = 0; i < 7; i++) {
            schedule.addMeal(start.plusDays(i), com.mealplanner.entity.MealType.BREAKFAST, "recipe-" + i);
        }
        
        com.mealplanner.entity.User user = new com.mealplanner.entity.User("user-1", username, "password");
        user.setMealSchedule(schedule);
        
        when(dataAccess.getUserByUsername(username)).thenReturn(user);
        
        interactor.execute(inputData);
        
        verify(presenter).presentSchedule(any());
    }

    @Test
    public void testRecipeDetailsInSchedule() throws com.mealplanner.exception.UserNotFoundException, com.mealplanner.exception.ScheduleConflictException {
        String username = "testuser";
        ViewScheduleInputData inputData = new ViewScheduleInputData(username);
        
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        schedule.addMeal(java.time.LocalDate.now(), com.mealplanner.entity.MealType.BREAKFAST, "recipe-1");
        
        com.mealplanner.entity.User user = new com.mealplanner.entity.User("user-1", username, "password");
        user.setMealSchedule(schedule);
        
        when(dataAccess.getUserByUsername(username)).thenReturn(user);
        
        interactor.execute(inputData);
        
        verify(presenter).presentSchedule(any());
    }

    @Test
    public void testDataAccessFailure() throws com.mealplanner.exception.UserNotFoundException {
        String username = "testuser";
        ViewScheduleInputData inputData = new ViewScheduleInputData(username);
        
        when(dataAccess.getUserByUsername(username)).thenThrow(new RuntimeException("Database error"));
        
        interactor.execute(inputData);
        
        verify(presenter).presentError("Unexpected error: Database error");
    }

    @Test
    public void testViewScheduleWithEmptyUsername() {
        ViewScheduleInputData inputData = new ViewScheduleInputData("");
        
        interactor.execute(inputData);
        
        verify(presenter).presentError("Username cannot be empty");
    }

    @Test
    public void testSaveSchedule() {
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        ViewScheduleInputData inputData = new ViewScheduleInputData("user-1");
        inputData.loadSchedule(schedule);
        
        interactor.saveSchedule(inputData);
        
        verify(dataAccess).saveSchedule(schedule);
    }

    @Test
    public void testLoadSchedule() {
        String username = "testuser";
        ViewScheduleInputData inputData = new ViewScheduleInputData(username);
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        // Add a meal to make schedule non-empty
        try {
            schedule.addMeal(java.time.LocalDate.now(), com.mealplanner.entity.MealType.BREAKFAST, "recipe-1");
        } catch (Exception e) {
            // Ignore
        }
        
        when(dataAccess.loadScheduleByUsername(username)).thenReturn(schedule);
        
        interactor.loadSchedule(inputData);
        
        verify(presenter).presentSchedule(any());
    }
}
