package com.mealplanner.interface_adapter.presenter;

// Presenter for login - handles login success/failure and navigates to appropriate view.
// Responsible: Mona
// done: Implement OutputBoundary methods to show login result and navigate to schedule view on success

import com.mealplanner.interface_adapter.view_model.LoginViewModel;
import com.mealplanner.use_case.login.LoginOutputBoundary;
import com.mealplanner.use_case.login.LoginOutputData;

public class LoginPresenter implements LoginOutputBoundary {
    private final LoginViewModel loginViewModel;
    public LoginPresenter(LoginViewModel loginViewModel) {
        this.loginViewModel = loginViewModel;
    }

    @Override
    public void presentLoginSuccess(LoginOutputData loginOutputData) {
        ///  set logged-in user and clear errors, notify the view
        loginViewModel.setLoggedInUser(loginOutputData.getUsername());
        loginViewModel.setError(null);
        loginViewModel.firePropertyChanged();

    }
    @Override
    ///  clear loggen-in users and set error, notify the view
    public void presentLoginFailure(String errorMessage) {
        loginViewModel.setLoggedInUser(null);
        loginViewModel.setError(errorMessage);

        loginViewModel.firePropertyChanged();
    }

}
