package com.mealplanner.use_case.view_schedule;

import com.mealplanner.entity.Schedule;
import com.mealplanner.entity.User;
import com.mealplanner.exception.UserNotFoundException;
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
    private ViewScheduleDataAccessInterface dataAccess;

    @Mock
    private ViewScheduleOutputBoundary presenter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        interactor = new ViewScheduleInteractor(dataAccess, presenter);
    }

    @Test
    public void testViewScheduleSuccess() {
        // Arrange
        String username = "testuser";
        Schedule schedule = new Schedule("schedule1", "user123");
        User user = new User("user123", username, "password");
        user.setMealSchedule(schedule);
        
        when(dataAccess.getUserByUsername(username)).thenReturn(user);

        ViewScheduleInputData inputData = new ViewScheduleInputData(username);

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess).getUserByUsername(username);
        verify(presenter).presentSchedule(argThat(outputData -> 
            outputData.getUsername().equals(username) && 
            outputData.getSchedule().equals(schedule)
        ));
        verify(presenter, never()).presentError(anyString());
    }

    @Test
    public void testViewEmptySchedule() {
        // Arrange
        String username = "testuser";
        Schedule emptySchedule = new Schedule("schedule1", "user123");
        User user = new User("user123", username, "password");
        user.setMealSchedule(emptySchedule);
        
        when(dataAccess.getUserByUsername(username)).thenReturn(user);

        ViewScheduleInputData inputData = new ViewScheduleInputData(username);

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess).getUserByUsername(username);
        verify(presenter).presentError("No Schedule found for user");
        verify(presenter, never()).presentSchedule(any());
    }

    @Test
    public void testViewScheduleInvalidUser() {
        // Arrange
        String username = "nonexistent";
        
        when(dataAccess.getUserByUsername(username)).thenThrow(new UserNotFoundException(username));

        ViewScheduleInputData inputData = new ViewScheduleInputData(username);

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess).getUserByUsername(username);
        verify(presenter).presentError("Username not found");
        verify(presenter, never()).presentSchedule(any());
    }

    @Test
    public void testViewScheduleNullInput() {
        // Act
        interactor.execute(null);

        // Assert
        verify(dataAccess, never()).getUserByUsername(anyString());
        verify(presenter).presentError("Input data cannot be null");
        verify(presenter, never()).presentSchedule(any());
    }

    @Test
    public void testViewScheduleEmptyUsername() {
        // Arrange
        ViewScheduleInputData inputData = new ViewScheduleInputData("");

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess, never()).getUserByUsername(anyString());
        verify(presenter).presentError("Username cannot be empty");
        verify(presenter, never()).presentSchedule(any());
    }

    @Test
    public void testViewScheduleNullUsername() {
        // Arrange
        ViewScheduleInputData inputData = new ViewScheduleInputData(null);

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess, never()).getUserByUsername(anyString());
        verify(presenter).presentError("Username cannot be empty");
        verify(presenter, never()).presentSchedule(any());
    }

    @Test
    public void testViewScheduleWhitespaceUsername() {
        // Arrange
        ViewScheduleInputData inputData = new ViewScheduleInputData("   ");

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess, never()).getUserByUsername(anyString());
        verify(presenter).presentError("Username cannot be empty");
        verify(presenter, never()).presentSchedule(any());
    }

    @Test
    public void testViewScheduleNullSchedule() {
        // Arrange
        String username = "testuser";
        User user = new User("user123", username, "password");
        user.setMealSchedule(null);
        
        when(dataAccess.getUserByUsername(username)).thenReturn(user);

        ViewScheduleInputData inputData = new ViewScheduleInputData(username);

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess).getUserByUsername(username);
        verify(presenter).presentError("No Schedule found for user");
        verify(presenter, never()).presentSchedule(any());
    }

    @Test
    public void testDataAccessFailure() {
        // Arrange
        String username = "testuser";
        
        when(dataAccess.getUserByUsername(username)).thenThrow(new RuntimeException("Database error"));

        ViewScheduleInputData inputData = new ViewScheduleInputData(username);

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess).getUserByUsername(username);
        verify(presenter).presentError("Unexpected error: Database error");
        verify(presenter, never()).presentSchedule(any());
    }

    @Test
    public void testSaveScheduleSuccess() {
        // Arrange
        Schedule schedule = new Schedule("schedule1", "user123");
        ViewScheduleInputData inputData = new ViewScheduleInputData("testuser");
        inputData.loadSchedule(schedule);

        // Act
        interactor.saveSchedule(inputData);

        // Assert
        verify(dataAccess).saveSchedule(schedule);
        verify(presenter, never()).presentError(anyString());
    }

    @Test
    public void testSaveScheduleNullInput() {
        // Act
        interactor.saveSchedule(null);

        // Assert
        verify(dataAccess, never()).saveSchedule(any());
        verify(presenter).presentError("Input data cannot be null");
    }

    @Test
    public void testSaveScheduleNullSchedule() {
        // Arrange
        ViewScheduleInputData inputData = new ViewScheduleInputData("testuser");

        // Act
        interactor.saveSchedule(inputData);

        // Assert
        verify(dataAccess, never()).saveSchedule(any());
        verify(presenter).presentError("Schedule cannot be empty");
    }

    @Test
    public void testLoadScheduleSuccess() {
        // Arrange
        String username = "testuser";
        Schedule schedule = new Schedule("schedule1", "user123");
        
        when(dataAccess.loadScheduleByUsername(username)).thenReturn(schedule);

        ViewScheduleInputData inputData = new ViewScheduleInputData(username);

        // Act
        interactor.loadSchedule(inputData);

        // Assert
        verify(dataAccess).loadScheduleByUsername(username);
        verify(presenter).presentSchedule(argThat(outputData -> 
            outputData.getUsername().equals(username) && 
            outputData.getSchedule().equals(schedule)
        ));
        verify(presenter, never()).presentError(anyString());
    }

    @Test
    public void testLoadScheduleNullSchedule() {
        // Arrange
        String username = "testuser";
        
        when(dataAccess.loadScheduleByUsername(username)).thenReturn(null);

        ViewScheduleInputData inputData = new ViewScheduleInputData(username);

        // Act
        interactor.loadSchedule(inputData);

        // Assert
        verify(dataAccess).loadScheduleByUsername(username);
        verify(presenter).presentError("No Schedule found for user");
        verify(presenter, never()).presentSchedule(any());
    }

    @Test
    public void testLoadScheduleEmptyUsername() {
        // Arrange
        ViewScheduleInputData inputData = new ViewScheduleInputData("");

        // Act
        interactor.loadSchedule(inputData);

        // Assert
        verify(dataAccess, never()).loadScheduleByUsername(anyString());
        verify(presenter).presentError("Username cannot be empty");
        verify(presenter, never()).presentSchedule(any());
    }
}
