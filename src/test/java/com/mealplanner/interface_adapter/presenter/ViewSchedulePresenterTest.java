package com.mealplanner.interface_adapter.presenter;

import com.mealplanner.entity.Schedule;
import com.mealplanner.interface_adapter.view_model.ScheduleViewModel;
import com.mealplanner.use_case.view_schedule.ViewScheduleOutputData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ViewSchedulePresenter.
 * Tests formatting and presentation of meal schedules.
 *
 * Responsible: Mona (primary)
 */
public class ViewSchedulePresenterTest {

    private ViewSchedulePresenter presenter;
    private ScheduleViewModel viewModel;

    @BeforeEach
    public void setUp() {
        viewModel = new ScheduleViewModel();
        presenter = new ViewSchedulePresenter(viewModel);
    }

    @Test
    public void testPresentSchedule() {
        // Arrange
        String username = "testuser";
        Schedule schedule = new Schedule("schedule1", "user123");
        ViewScheduleOutputData outputData = new ViewScheduleOutputData(username, schedule);

        // Act
        presenter.presentSchedule(outputData);

        // Assert
        assertEquals(username, viewModel.getUsername());
        assertEquals(schedule, viewModel.getSchedule());
        assertNull(viewModel.getError());
    }

    @Test
    public void testPresentEmptySchedule() {
        // Arrange
        String username = "testuser";
        Schedule emptySchedule = new Schedule("schedule1", "user123");
        ViewScheduleOutputData outputData = new ViewScheduleOutputData(username, emptySchedule);

        // Act
        presenter.presentSchedule(outputData);

        // Assert
        assertEquals(username, viewModel.getUsername());
        assertEquals(emptySchedule, viewModel.getSchedule());
        assertNull(viewModel.getError());
    }

    @Test
    public void testFormatMealDetails() {
        // Arrange
        String username = "testuser";
        Schedule schedule = new Schedule("schedule1", "user123");
        ViewScheduleOutputData outputData = new ViewScheduleOutputData(username, schedule);

        // Act
        presenter.presentSchedule(outputData);

        // Assert
        Schedule retrievedSchedule = viewModel.getSchedule();
        assertNotNull(retrievedSchedule);
        assertEquals("schedule1", retrievedSchedule.getScheduleId());
        assertEquals("user123", retrievedSchedule.getUserId());
    }

    @Test
    public void testPresentError() {
        // Arrange
        String errorMessage = "User not found";

        // Act
        presenter.presentError(errorMessage);

        // Assert
        assertEquals(errorMessage, viewModel.getError());
        assertNull(viewModel.getSchedule());
    }

    @Test
    public void testPresentErrorNull() {
        // Act
        presenter.presentError(null);

        // Assert
        assertEquals("An error occurred", viewModel.getError());
        assertNull(viewModel.getSchedule());
    }

    @Test
    public void testPresentScheduleNull() {
        // Act
        presenter.presentSchedule(null);

        // Assert
        assertEquals("Schedule data is missing", viewModel.getError());
    }

    @Test
    public void testPresentScheduleWithNullUsername() {
        // Arrange
        Schedule schedule = new Schedule("schedule1", "user123");
        ViewScheduleOutputData outputData = new ViewScheduleOutputData(null, schedule);

        // Act
        presenter.presentSchedule(outputData);

        // Assert
        assertNull(viewModel.getUsername());
        assertEquals(schedule, viewModel.getSchedule());
        assertNull(viewModel.getError());
    }

    @Test
    public void testPresentScheduleWithNullSchedule() {
        // Arrange
        ViewScheduleOutputData outputData = new ViewScheduleOutputData("testuser", null);

        // Act
        presenter.presentSchedule(outputData);

        // Assert
        assertEquals("testuser", viewModel.getUsername());
        assertNull(viewModel.getSchedule());
        assertNull(viewModel.getError());
    }
}
