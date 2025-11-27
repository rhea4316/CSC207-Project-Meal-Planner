package com.mealplanner.interface_adapter.presenter;

import com.mealplanner.entity.Schedule;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.view_model.MealPlanViewModel;
import com.mealplanner.use_case.manage_meal_plan.add.AddMealOutputData;
import com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealOutputData;
import com.mealplanner.use_case.manage_meal_plan.edit.EditMealOutputData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for MealPlanPresenter.
 * Tests formatting and presentation of meal plan operations.
 *
 * Responsible: Grace (primary)
 */
public class MealPlanPresenterTest {

    private MealPlanPresenter presenter;
    private MealPlanViewModel viewModel;
    private ViewManagerModel viewManager;

    @BeforeEach
    public void setUp() {
        viewModel = new MealPlanViewModel();
        viewManager = new ViewManagerModel();
        presenter = new MealPlanPresenter(viewModel, viewManager);
    }

    @Test
    public void testPresentAddSuccess() {
        // Arrange
        Schedule schedule = new Schedule("schedule1", "user123");
        AddMealOutputData outputData = new AddMealOutputData(schedule, "Meal added successfully");

        // Act
        presenter.presentAddSuccess(outputData);

        // Assert
        assertEquals(schedule, viewModel.getSchedule());
        assertEquals("Meal added successfully", viewModel.getSuccessMessage());
        assertEquals("MealPlanView", viewManager.getActiveView());
    }

    @Test
    public void testPresentEditSuccess() {
        // Arrange
        Schedule schedule = new Schedule("schedule1", "user123");
        EditMealOutputData outputData = new EditMealOutputData(schedule, "Meal edited successfully");

        // Act
        presenter.presentEditSuccess(outputData);

        // Assert
        assertEquals(schedule, viewModel.getSchedule());
        assertEquals("Meal edited successfully", viewModel.getSuccessMessage());
        assertEquals("MealPlanView", viewManager.getActiveView());
    }

    @Test
    public void testPresentDeleteSuccess() {
        // Arrange
        Schedule schedule = new Schedule("schedule1", "user123");
        DeleteMealOutputData outputData = new DeleteMealOutputData(schedule, "Meal deleted successfully");

        // Act
        presenter.presentDeleteSuccess(outputData);

        // Assert
        assertEquals(schedule, viewModel.getSchedule());
        assertEquals("Meal deleted successfully", viewModel.getSuccessMessage());
        assertEquals("MealPlanView", viewManager.getActiveView());
    }

    @Test
    public void testPresentConflictError() {
        // Arrange
        String errorMessage = "Scheduling conflict: Meal already exists";

        // Act
        presenter.presentAddError(errorMessage);

        // Assert
        assertEquals(errorMessage, viewModel.getErrorMessage());
    }

    @Test
    public void testFormatWeeklySchedule() {
        // Arrange
        Schedule schedule = new Schedule("schedule1", "user123");
        AddMealOutputData outputData = new AddMealOutputData(schedule, "Meal added");

        // Act
        presenter.presentAddSuccess(outputData);

        // Assert
        assertNotNull(viewModel.getSchedule());
        assertNotNull(viewModel.getWeeklyMeals());
        // Schedule should be set and weekly meals should be populated
        assertEquals(schedule, viewModel.getSchedule());
    }

    @Test
    public void testPresentAddError() {
        // Arrange
        String errorMessage = "Failed to add meal";

        // Act
        presenter.presentAddError(errorMessage);

        // Assert
        assertEquals(errorMessage, viewModel.getErrorMessage());
    }

    @Test
    public void testPresentEditError() {
        // Arrange
        String errorMessage = "Failed to edit meal";

        // Act
        presenter.presentEditError(errorMessage);

        // Assert
        assertEquals(errorMessage, viewModel.getErrorMessage());
    }

    @Test
    public void testPresentDeleteError() {
        // Arrange
        String errorMessage = "Failed to delete meal";

        // Act
        presenter.presentDeleteError(errorMessage);

        // Assert
        assertEquals(errorMessage, viewModel.getErrorMessage());
    }

    @Test
    public void testPresentAddSuccessNull() {
        // Act
        presenter.presentAddSuccess(null);

        // Assert
        assertEquals("Failed to add meal", viewModel.getErrorMessage());
    }

    @Test
    public void testPresentEditSuccessNull() {
        // Act
        presenter.presentEditSuccess(null);

        // Assert
        assertEquals("Failed to edit meal", viewModel.getErrorMessage());
    }

    @Test
    public void testPresentDeleteSuccessNull() {
        // Act
        presenter.presentDeleteSuccess(null);

        // Assert
        assertEquals("Failed to delete meal", viewModel.getErrorMessage());
    }

    @Test
    public void testPresentAddErrorNull() {
        // Act
        presenter.presentAddError(null);

        // Assert
        assertEquals("Failed to add meal", viewModel.getErrorMessage());
    }
}
