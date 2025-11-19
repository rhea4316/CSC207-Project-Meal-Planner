package com.mealplanner.entity;

// Core entity representing a user with saved recipes, meal schedule, and nutrition goals.
// Responsible: Mona (primary for login/user management), Everyone (used across use cases)
// TODO: Implement user class with methods for managing saved recipes and generating grocery lists

public class User {
    private final String userId;

    public User(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
