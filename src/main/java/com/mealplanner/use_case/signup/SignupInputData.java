package com.mealplanner.use_case.signup;

// Data transfer object carrying signup credentials (username and password).
// Responsible: Everyone

public class SignupInputData {
    private final String username;
    private final String password;

    public SignupInputData(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

