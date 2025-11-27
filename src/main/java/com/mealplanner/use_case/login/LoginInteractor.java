package com.mealplanner.use_case.login;

// Main business logic for user login and authentication.
// Responsible: Mona

import com.mealplanner.entity.User;
import com.mealplanner.exception.UserNotFoundException;
import java.util.Objects;

public class LoginInteractor implements LoginInputBoundary {

    private final LoginDataAccessInterface userDataAccess;
    private final LoginOutputBoundary presenter;

    public LoginInteractor(LoginDataAccessInterface userDataAccess, LoginOutputBoundary presenter) {
        this.userDataAccess = Objects.requireNonNull(userDataAccess, "User data access cannot be null");
        this.presenter = Objects.requireNonNull(presenter, "Presenter cannot be null");
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