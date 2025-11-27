package com.mealplanner.use_case.signup;

// Data transfer object carrying newly registered user information.
// Responsible: Everyone

public class SignupOutputData {
    private final String userId;
    private final String username;

    public SignupOutputData(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}

