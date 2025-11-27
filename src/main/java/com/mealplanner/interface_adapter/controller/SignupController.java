package com.mealplanner.interface_adapter.controller;

// Controller for user registration - receives username and password and calls signup interactor.
// Responsible: Everyone

import com.mealplanner.use_case.signup.SignupInputBoundary;
import com.mealplanner.use_case.signup.SignupInputData;

public class SignupController {
    private final SignupInputBoundary signupInteractor;

    public SignupController(SignupInputBoundary signupInteractor) {
        this.signupInteractor = signupInteractor;
    }

    public void execute(String username, String password) {
        SignupInputData inputData = new SignupInputData(username, password);
        signupInteractor.execute(inputData);
    }
}

