package com.mealplanner.use_case.login;

// Data transfer object carrying login credentials (username).
// Responsible: Mona
// Done: Implement with username field (add password if needed for future authentication)

public class LoginInputData {
    private final String username;

    public LoginInputData(String username) {
        this.username = username;
    };

    public String getUsername() {
        return username;
    }
}
