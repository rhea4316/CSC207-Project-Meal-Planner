package com.mealplanner.interface_adapter.presenter;

// Presenter for viewing schedule - converts schedule OutputData to ViewModel for display.
// Responsible: Mona

import com.mealplanner.interface_adapter.view_model.ScheduleViewModel;
import com.mealplanner.use_case.view_schedule.ViewScheduleOutputBoundary;
import com.mealplanner.use_case.view_schedule.ViewScheduleOutputData;
import java.util.Objects;

public class ViewSchedulePresenter implements ViewScheduleOutputBoundary {
    private final ScheduleViewModel scheduleViewModel;

    public ViewSchedulePresenter(ScheduleViewModel scheduleViewModel) {
        this.scheduleViewModel = Objects.requireNonNull(scheduleViewModel, "ScheduleViewModel cannot be null");
    }

    @Override
    public void presentSchedule(ViewScheduleOutputData outputData) {
        if (outputData == null) {
            scheduleViewModel.setError("Schedule data is missing");
            scheduleViewModel.firePropertyChanged();
            return;
        }

        if (outputData.getUsername() != null) {
            scheduleViewModel.setUsername(outputData.getUsername());
        }
        if (outputData.getSchedule() != null) {
            scheduleViewModel.setSchedule(outputData.getSchedule());
        }
        scheduleViewModel.setError(null);
        scheduleViewModel.firePropertyChanged();
    }

    @Override
    public void presentError(String errorMessage) {
        scheduleViewModel.setSchedule(null);
        scheduleViewModel.setError(errorMessage != null ? errorMessage : "An error occurred");
        scheduleViewModel.firePropertyChanged();
    }
}
