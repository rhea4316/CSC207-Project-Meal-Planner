package com.mealplanner.interface_adapter.presenter;

import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.view_model.MealPlanViewModel;
import com.mealplanner.interface_adapter.view_model.ScheduleViewModel;
import com.mealplanner.use_case.manage_meal_plan.add.AddMealOutputBoundary;
import com.mealplanner.use_case.manage_meal_plan.add.AddMealOutputData;
import com.mealplanner.use_case.manage_meal_plan.edit.EditMealOutputBoundary;
import com.mealplanner.use_case.manage_meal_plan.edit.EditMealOutputData;
import com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealOutputBoundary;
import com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealOutputData;

import java.util.Objects;

public class MealPlanPresenter implements AddMealOutputBoundary, EditMealOutputBoundary, DeleteMealOutputBoundary {
    private final MealPlanViewModel mealPlanViewModel;
    private final ScheduleViewModel scheduleViewModel;
    private final ViewManagerModel viewManager;

    public MealPlanPresenter(MealPlanViewModel mealPlanViewModel, ScheduleViewModel scheduleViewModel, ViewManagerModel viewManager) {
        this.mealPlanViewModel = Objects.requireNonNull(mealPlanViewModel, "MealPlanViewModel cannot be null");
        this.scheduleViewModel = Objects.requireNonNull(scheduleViewModel, "ScheduleViewModel cannot be null");
        this.viewManager = viewManager;
    }

    @Override
    public void presentAddSuccess(AddMealOutputData outputData) {
        if (outputData == null) {
            mealPlanViewModel.setErrorMessage("Failed to add meal");
            scheduleViewModel.setError("Failed to add meal");
            return;
        }

        mealPlanViewModel.setSchedule(outputData.getSchedule());
        mealPlanViewModel.setSuccessMessage(outputData.getMessage());
        
        // Update ScheduleViewModel so ScheduleView and DashboardView can reflect the changes
        // setSchedule() already fires property change, so no need to call firePropertyChanged() separately
        scheduleViewModel.setSchedule(outputData.getSchedule());
        scheduleViewModel.setError(null);

        if (viewManager != null) {
            viewManager.setActiveView(com.mealplanner.view.ViewManager.SCHEDULE_VIEW);
        }
    }

    @Override
    public void presentAddError(String errorMessage) {
        mealPlanViewModel.setErrorMessage(errorMessage != null ? errorMessage : "Failed to add meal");
    }

    @Override
    public void presentEditSuccess(EditMealOutputData outputData) {
        if (outputData == null) {
            mealPlanViewModel.setErrorMessage("Failed to edit meal");
            scheduleViewModel.setError("Failed to edit meal");
            return;
        }

        mealPlanViewModel.setSchedule(outputData.getSchedule());
        mealPlanViewModel.setSuccessMessage(outputData.getMessage());
        
        // Update ScheduleViewModel so ScheduleView and DashboardView can reflect the changes
        // setSchedule() already fires property change, so no need to call firePropertyChanged() separately
        scheduleViewModel.setSchedule(outputData.getSchedule());
        scheduleViewModel.setError(null);

        if (viewManager != null) {
            viewManager.setActiveView(com.mealplanner.view.ViewManager.SCHEDULE_VIEW);
        }
    }

    @Override
    public void presentEditError(String errorMessage) {
        mealPlanViewModel.setErrorMessage(errorMessage != null ? errorMessage : "Failed to edit meal");
    }

    @Override
    public void presentDeleteSuccess(DeleteMealOutputData outputData) {
        if (outputData == null) {
            mealPlanViewModel.setErrorMessage("Failed to delete meal");
            scheduleViewModel.setError("Failed to delete meal");
            return;
        }

        mealPlanViewModel.setSchedule(outputData.getSchedule());
        mealPlanViewModel.setSuccessMessage(outputData.getMessage());
        
        // Update ScheduleViewModel so ScheduleView and DashboardView can reflect the changes
        // setSchedule() already fires property change, so no need to call firePropertyChanged() separately
        scheduleViewModel.setSchedule(outputData.getSchedule());
        scheduleViewModel.setError(null);

        if (viewManager != null) {
            viewManager.setActiveView(com.mealplanner.view.ViewManager.SCHEDULE_VIEW);
        }
    }

    @Override
    public void presentDeleteError(String errorMessage) {
        mealPlanViewModel.setErrorMessage(errorMessage != null ? errorMessage : "Failed to delete meal");
    }
}
