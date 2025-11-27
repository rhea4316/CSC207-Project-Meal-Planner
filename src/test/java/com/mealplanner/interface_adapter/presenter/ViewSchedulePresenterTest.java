package com.mealplanner.interface_adapter.presenter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

/**
 * Test class for ViewSchedulePresenter.
 * Tests formatting and presentation of meal schedules.
 *
 * Responsible: Mona (primary)
 */
public class ViewSchedulePresenterTest {

    private ViewSchedulePresenter presenter;

    @Mock
    private com.mealplanner.interface_adapter.view_model.ScheduleViewModel viewModel;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        presenter = new ViewSchedulePresenter(viewModel);
    }

    @Test
    public void testPresentSchedule() {
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        try {
            schedule.addMeal(java.time.LocalDate.now(), com.mealplanner.entity.MealType.BREAKFAST, "recipe-1");
        } catch (Exception e) {
            // Ignore
        }
        com.mealplanner.use_case.view_schedule.ViewScheduleOutputData outputData = 
            new com.mealplanner.use_case.view_schedule.ViewScheduleOutputData("testuser", schedule);
        
        presenter.presentSchedule(outputData);
        
        verify(viewModel).setUsername("testuser");
        verify(viewModel).setSchedule(schedule);
        verify(viewModel).setError(null);
        verify(viewModel).firePropertyChanged();
    }

    @Test
    public void testPresentEmptySchedule() {
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        com.mealplanner.use_case.view_schedule.ViewScheduleOutputData outputData = 
            new com.mealplanner.use_case.view_schedule.ViewScheduleOutputData("testuser", schedule);
        
        presenter.presentSchedule(outputData);
        
        verify(viewModel).setSchedule(schedule);
    }

    @Test
    public void testFormatMealDetails() {
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        try {
            schedule.addMeal(java.time.LocalDate.now(), com.mealplanner.entity.MealType.BREAKFAST, "recipe-1");
        } catch (Exception e) {
            // Ignore
        }
        com.mealplanner.use_case.view_schedule.ViewScheduleOutputData outputData = 
            new com.mealplanner.use_case.view_schedule.ViewScheduleOutputData("testuser", schedule);
        
        presenter.presentSchedule(outputData);
        
        verify(viewModel).setSchedule(schedule);
    }

    @Test
    public void testPresentError() {
        String errorMessage = "Schedule not found";
        
        presenter.presentError(errorMessage);
        
        verify(viewModel).setSchedule(null);
        verify(viewModel).setError(errorMessage);
        verify(viewModel).firePropertyChanged();
    }
}
