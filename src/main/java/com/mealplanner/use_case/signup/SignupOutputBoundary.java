package com.mealplanner.use_case.signup;

// Output boundary interface for presenting signup success or failure.
// Responsible: Everyone

public interface SignupOutputBoundary {
    void presentSignupSuccess(SignupOutputData outputData);
    void presentSignupFailure(String errorMessage);
}

