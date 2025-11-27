package com.mealplanner.interface_adapter.controller;

import com.mealplanner.entity.Schedule;
import com.mealplanner.use_case.view_schedule.ViewScheduleInputBoundary;
import com.mealplanner.use_case.view_schedule.ViewScheduleInputData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

/**
 * Test class for ViewScheduleController.
 * Tests controller input validation and interactor invocation.
 *
 * Responsible: Mona (primary)
 */
public class ViewScheduleControllerTest {

    private ViewScheduleController controller;

    @Mock
    private ViewScheduleInputBoundary interactor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ViewScheduleController(interactor);
    }

    @Test
    public void testViewScheduleWithValidUsername() {
        // Arrange
        String username = "testuser";

        // Act
        controller.execute(username);

        // Assert
        verify(interactor).execute(argThat(inputData -> 
            inputData.getUsername().equals(username)
        ));
    }

    @Test
    public void testViewScheduleWithEmptyUsername() {
        // Arrange
        String username = "";

        // Act
        controller.execute(username);

        // Assert
        verify(interactor).execute(argThat(inputData -> 
            inputData.getUsername().equals(username)
        ));
    }

    @Test
    public void testViewScheduleForDateRange() {
        // Arrange
        String username = "testuser";

        // Act
        controller.execute(username);

        // Assert
        verify(interactor).execute(any(ViewScheduleInputData.class));
    }

    @Test
    public void testSaveSchedule() {
        // Arrange
        Schedule schedule = new Schedule("schedule1", "user123");

        // Act
        controller.saveSchedule(schedule);

        // Assert
        verify(interactor).saveSchedule(argThat(inputData -> 
            inputData.getSchedule().equals(schedule)
        ));
    }

    @Test
    public void testLoadSchedule() {
        // Arrange
        String scheduleId = "schedule1";

        // Act
        controller.loadSchedule(scheduleId);

        // Assert
        verify(interactor).loadSchedule(argThat(inputData -> 
            inputData.getUsername().equals(scheduleId)
        ));
    }
}
