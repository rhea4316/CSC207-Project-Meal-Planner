package com.mealplanner.use_case.login;

// Main business logic for user login and authentication.
// Responsible: Mona
// TODO: Implement execute method: validate username exists, retrieve user data, pass to presenter for success/error

import com.mealplanner.entity.User;
import com.mealplanner.exception.UserNotFoundException;

public class LoginInteractor implements LoginInputBoundary {

    private final LoginDataAccessInterface userDataAccess;
    private final LoginOutputBoundary presenter;

    public LoginInteractor(LoginDataAccessInterface userDataAccess, LoginOutputBoundary presenter) {
        this.userDataAccess = userDataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(LoginInputData inputData) {
        String username = inputData.getUsername();

        if (username == null || username.trim().isEmpty()){
            presenter.presentLoginFailure("Username cannot be empty");
            return;
        }

        username = username.trim();

        try {
            User user = userDataAccess.getUserByUsername(username);
            LoginOutputData outputData = new LoginOutputData(user.getUserId(), user.getUsername());
            presenter.presentLoginSuccess(outputData);
        } catch (UserNotFoundException e) {
            presenter.presentLoginFailure("User not found");
        } catch (Exception e) {
            presenter.presentLoginFailure("Unexpected error during login");
        }
    }
}