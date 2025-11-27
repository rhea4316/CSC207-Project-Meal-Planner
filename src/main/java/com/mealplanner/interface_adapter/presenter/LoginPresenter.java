package com.mealplanner.interface_adapter.presenter;

// Presenter for login - handles login success/failure and navigates to appropriate view.
// Responsible: Mona

import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.view_model.LoginViewModel;
import com.mealplanner.use_case.login.LoginOutputBoundary;
import com.mealplanner.use_case.login.LoginOutputData;
import java.util.Objects;

public class LoginPresenter implements LoginOutputBoundary {
    private final LoginViewModel loginViewModel;
    private final ViewManagerModel viewManagerModel;
    
    public LoginPresenter(LoginViewModel loginViewModel, ViewManagerModel viewManagerModel) {
        this.loginViewModel = Objects.requireNonNull(loginViewModel, "LoginViewModel cannot be null");
        this.viewManagerModel = viewManagerModel;
    }

    @Override
    public void presentLoginSuccess(LoginOutputData loginOutputData) {
        // Set logged-in user and clear errors, notify the view
        if (loginOutputData == null) {
            loginViewModel.setError("Login data is missing");
            loginViewModel.firePropertyChanged();
            return;
        }
        
        loginViewModel.setLoggedInUser(loginOutputData.getUsername());
        loginViewModel.setError(null);
        loginViewModel.firePropertyChanged();
        
        // Store current user information in ViewManagerModel
        if (viewManagerModel != null) {
            viewManagerModel.setCurrentUserId(loginOutputData.getUserUId());
            viewManagerModel.setCurrentUsername(loginOutputData.getUsername());
            // Navigate to ScheduleView after successful login
            viewManagerModel.setActiveView("ScheduleView");
        }
    }
    
    @Override
    // Clear logged-in user and set error, notify the view
    public void presentLoginFailure(String errorMessage) {
        loginViewModel.setLoggedInUser(null);
        loginViewModel.setError(errorMessage != null ? errorMessage : "Login failed");
        loginViewModel.firePropertyChanged();
    }
}
