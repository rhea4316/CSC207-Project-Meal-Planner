package com.mealplanner.interface_adapter.presenter;

import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.view_model.MealPlanViewModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

/**
 * Test class for MealPlanPresenter.
 * Tests formatting and presentation of meal plan operations.
 *
 * Responsible: Grace (primary)
 */
public class MealPlanPresenterTest {

    private MealPlanPresenter presenter;

    @Mock
    private MealPlanViewModel viewModel;

    @Mock
    private ViewManagerModel viewManager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        presenter = new MealPlanPresenter(viewModel, viewManager);
    }

    @Test
    public void testPresentAddSuccess() {
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        com.mealplanner.use_case.manage_meal_plan.add.AddMealOutputData outputData = 
            new com.mealplanner.use_case.manage_meal_plan.add.AddMealOutputData(schedule, "Meal added successfully");
        
        presenter.presentAddSuccess(outputData);
        
        verify(viewModel).setSchedule(schedule);
        verify(viewModel).setSuccessMessage("Meal added successfully");
        verify(viewManager).setActiveView(com.mealplanner.view.ViewManager.SCHEDULE_VIEW);
    }

    @Test
    public void testPresentEditSuccess() {
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        com.mealplanner.use_case.manage_meal_plan.edit.EditMealOutputData outputData = 
            new com.mealplanner.use_case.manage_meal_plan.edit.EditMealOutputData(schedule, "Meal edited successfully");
        
        presenter.presentEditSuccess(outputData);
        
        verify(viewModel).setSchedule(schedule);
        verify(viewModel).setSuccessMessage("Meal edited successfully");
        verify(viewManager).setActiveView(com.mealplanner.view.ViewManager.SCHEDULE_VIEW);
    }

    @Test
    public void testPresentDeleteSuccess() {
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealOutputData outputData = 
            new com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealOutputData(schedule, "Meal deleted successfully");
        
        presenter.presentDeleteSuccess(outputData);
        
        verify(viewModel).setSchedule(schedule);
        verify(viewModel).setSuccessMessage("Meal deleted successfully");
        verify(viewManager).setActiveView(com.mealplanner.view.ViewManager.SCHEDULE_VIEW);
    }

    @Test
    public void testPresentConflictError() {
        String errorMessage = "Meal slot already taken";
        
        presenter.presentAddError(errorMessage);
        
        verify(viewModel).setErrorMessage(errorMessage);
    }

    @Test
    public void testFormatWeeklySchedule() {
        com.mealplanner.entity.Schedule schedule = new com.mealplanner.entity.Schedule("schedule-1", "user-1");
        com.mealplanner.use_case.manage_meal_plan.add.AddMealOutputData outputData = 
            new com.mealplanner.use_case.manage_meal_plan.add.AddMealOutputData(schedule, "Success");
        
        presenter.presentAddSuccess(outputData);
        
        verify(viewModel).setSchedule(schedule);
    }

    @Test
    public void testPresentNullOutputData() {
        presenter.presentAddSuccess(null);
        verify(viewModel).setErrorMessage("Failed to add meal");
        
        presenter.presentEditSuccess(null);
        verify(viewModel).setErrorMessage("Failed to edit meal");
        
        presenter.presentDeleteSuccess(null);
        verify(viewModel).setErrorMessage("Failed to delete meal");
    }
}
