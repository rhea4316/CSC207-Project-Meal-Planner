package com.mealplanner.interface_adapter.controller;

// Controller for user login - receives username and password and calls login interactor.
// Responsible: Mona

import com.mealplanner.use_case.login.LoginInputBoundary;
import com.mealplanner.use_case.login.LoginInputData;

public class LoginController {

    private final LoginInputBoundary loginInteractor;
    public LoginController(LoginInputBoundary loginInteractor) {
        this.loginInteractor = loginInteractor;
    }
    public void execute(String username, String password){
        LoginInputData inputData = new LoginInputData(username, password);
        loginInteractor.execute(inputData);
    }

}
