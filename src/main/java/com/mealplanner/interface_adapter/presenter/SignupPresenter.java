package com.mealplanner.interface_adapter.presenter;

// Presenter for signup - handles signup success/failure and navigates to appropriate view.
// Responsible: Everyone

import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.view_model.SignupViewModel;
import com.mealplanner.use_case.signup.SignupOutputBoundary;
import com.mealplanner.use_case.signup.SignupOutputData;
import java.util.Objects;

public class SignupPresenter implements SignupOutputBoundary {
    private final SignupViewModel signupViewModel;
    private final ViewManagerModel viewManagerModel;

    public SignupPresenter(SignupViewModel signupViewModel, ViewManagerModel viewManagerModel) {
        this.signupViewModel = Objects.requireNonNull(signupViewModel, "SignupViewModel cannot be null");
        this.viewManagerModel = viewManagerModel;
    }

    @Override
    public void presentSignupSuccess(SignupOutputData outputData) {
        if (outputData == null) {
            signupViewModel.setError("Signup data is missing");
            signupViewModel.firePropertyChanged();
            return;
        }

        signupViewModel.setRegisteredUser(outputData.getUsername());
        signupViewModel.setError(null);
        signupViewModel.firePropertyChanged();

        // Store current user information in ViewManagerModel
        if (viewManagerModel != null) {
            viewManagerModel.setCurrentUserId(outputData.getUserId());
            viewManagerModel.setCurrentUsername(outputData.getUsername());
            // Navigate to ScheduleView after successful signup
            viewManagerModel.setActiveView("ScheduleView");
        }
    }

    @Override
    public void presentSignupFailure(String errorMessage) {
        signupViewModel.setRegisteredUser(null);
        signupViewModel.setError(errorMessage != null ? errorMessage : "Signup failed");
        signupViewModel.firePropertyChanged();
    }
}

