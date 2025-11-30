package com.mealplanner.interface_adapter.presenter;

import com.mealplanner.interface_adapter.view_model.ProfileSettingsViewModel;
import com.mealplanner.use_case.update_nutrition_goals.UpdateNutritionGoalsOutputBoundary;
import com.mealplanner.use_case.update_nutrition_goals.UpdateNutritionGoalsOutputData;
import java.util.Objects;

/**
 * Presenter for update nutrition goals use case.
 * Updates the ViewModel based on use case results.
 * 
 * Responsible: Interface Adapter team
 */
public class UpdateNutritionGoalsPresenter implements UpdateNutritionGoalsOutputBoundary {
    
    private final ProfileSettingsViewModel viewModel;
    
    public UpdateNutritionGoalsPresenter(ProfileSettingsViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "ViewModel cannot be null");
    }
    
    @Override
    public void presentSuccess(UpdateNutritionGoalsOutputData outputData) {
        if (outputData == null || outputData.getNutritionGoals() == null) {
            viewModel.setError("Received invalid update result");
            return;
        }
        
        // ViewModel 업데이트
        viewModel.setNutritionGoals(outputData.getNutritionGoals());
        viewModel.setError(null);
        
        // 성공 이벤트 발생
        viewModel.fireNutritionGoalsUpdated();
    }
    
    @Override
    public void presentError(String errorMessage) {
        viewModel.setError(errorMessage != null ? errorMessage : "An unknown error occurred");
    }
}

