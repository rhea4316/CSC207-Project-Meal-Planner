package com.mealplanner.use_case.signup;

// Main business logic for user registration.
// Responsible: Everyone

import com.mealplanner.entity.User;
import com.mealplanner.util.PasswordUtil;
import java.util.Objects;
import java.util.UUID;

public class SignupInteractor implements SignupInputBoundary {
    private final SignupDataAccessInterface userDataAccess;
    private final SignupOutputBoundary presenter;

    public SignupInteractor(SignupDataAccessInterface userDataAccess, SignupOutputBoundary presenter) {
        this.userDataAccess = Objects.requireNonNull(userDataAccess, "User data access cannot be null");
        this.presenter = Objects.requireNonNull(presenter, "Presenter cannot be null");
    }

    @Override
    public void execute(SignupInputData inputData) {
        String username = inputData.getUsername();
        String password = inputData.getPassword();

        // Validate input
        if (username == null || username.trim().isEmpty()) {
            presenter.presentSignupFailure("Username cannot be empty");
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            presenter.presentSignupFailure("Password cannot be empty");
            return;
        }

        username = username.trim();
        password = password.trim();

        // Check if username already exists
        if (userDataAccess.existsByUsername(username)) {
            presenter.presentSignupFailure("Username already exists");
            return;
        }

        // Validate password strength (optional - minimum length)
        if (password.length() < 6) {
            presenter.presentSignupFailure("Password must be at least 6 characters long");
            return;
        }

        try {
            // Generate unique user ID
            String userId = UUID.randomUUID().toString();

            // Hash password
            String hashedPassword = PasswordUtil.hashPassword(password);

            // Create new user
            User newUser = new User(userId, username, hashedPassword);

            // Save user
            userDataAccess.save(newUser);

            // Present success
            SignupOutputData outputData = new SignupOutputData(userId, username);
            presenter.presentSignupSuccess(outputData);
        } catch (Exception e) {
            presenter.presentSignupFailure("Failed to create account: " + e.getMessage());
        }
    }
}

