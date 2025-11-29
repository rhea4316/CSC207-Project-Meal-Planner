package com.mealplanner.use_case.login;

import com.mealplanner.entity.User;

// Data transfer object carrying authenticated user information.
// Responsible: Mona

public class LoginOutputData {

    private final String userUId;
    private final String username;
    private final User user;

    public LoginOutputData(String userUId, String username) {
        this.userUId = userUId;
        this.username = username;
        this.user = null;
    }

    public LoginOutputData(String userUId, String username, User user) {
        this.userUId = userUId;
        this.username = username;
        this.user = user;
    }

    public String getUserUId() {
        return userUId;
    }

    public String getUsername() {
        return username;
    }

    public User getUser() {
        return user;
    }
}
