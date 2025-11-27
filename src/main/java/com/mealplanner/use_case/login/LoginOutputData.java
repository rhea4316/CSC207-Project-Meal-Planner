package com.mealplanner.use_case.login;

// Data transfer object carrying authenticated user information.
// Responsible: Mona

public class LoginOutputData {

    private final String userUId;
    private final String username;

    public LoginOutputData(String userUId, String username) {
        this.userUId = userUId;
        this.username = username;
    }

    public String getUserUId() {
        return userUId;
    }

    public String getUsername() {
        return username;
    }
}
