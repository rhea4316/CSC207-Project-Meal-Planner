package com.mealplanner.use_case.login;

// Output boundary interface for presenting login success or failure.
// Responsible: Mona
// Done: Define methods for presentLoginSuccess (user data) and presentLoginError (user not found, etc.)

public interface LoginOutputBoundary {

    void presentLoginSuccess(LoginOutputData outputData);

    void presentLoginFailure(String errorMessage);

}
