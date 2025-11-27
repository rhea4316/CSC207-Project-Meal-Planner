package com.mealplanner.interface_adapter.controller;

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
    private com.mealplanner.use_case.view_schedule.ViewScheduleInputBoundary interactor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ViewScheduleController(interactor);
    }

    @Test
    public void testViewScheduleWithValidUsername() {
        String username = "testuser";
        
        controller.execute(username);
        
        verify(interactor).execute(argThat(inputData -> 
            inputData.getUsername().equals(username)
        ));
    }

    @Test
    public void testViewScheduleWithEmptyUsername() {
        controller.execute("");
        
        verify(interactor).execute(argThat(inputData -> 
            inputData.getUsername().equals("")
        ));
    }

    @Test
    public void testViewScheduleForDateRange() {
        String username = "testuser";
        controller.execute(username);
        
        verify(interactor).execute(any());
    }

    @Test
    public void testLoadSchedule() {
        String scheduleId = "schedule-1";
        
        controller.loadSchedule(scheduleId);
        
        verify(interactor).loadSchedule(argThat(inputData -> 
            inputData.getUsername().equals(scheduleId)
        ));
    }

    @Test
    public void testSaveSchedule() {
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        
        controller.saveSchedule(schedule);
        
        verify(interactor).saveSchedule(argThat(inputData -> 
            inputData.getSchedule() == schedule
        ));
    }
}
