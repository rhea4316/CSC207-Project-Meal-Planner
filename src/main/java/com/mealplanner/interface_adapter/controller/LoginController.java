package com.mealplanner.interface_adapter.controller;

// Controller for user login - receives username and calls login interactor.
// Responsible: Mona
// done: Implement execute method that converts username (and password if needed) to InputData and calls login interactor

import com.mealplanner.use_case.login.LoginInputBoundary;
import com.mealplanner.use_case.login.LoginInputData;

public class LoginController {

    private final LoginInputBoundary loginInteractor;
    public LoginController(LoginInputBoundary loginInteractor) {
        this.loginInteractor = loginInteractor;
    }
    public void execute(String username){
        LoginInputData inputData = new LoginInputData(username);
        loginInteractor.execute(inputData);
    }

}
