package com.mealplanner.data_access.database;

import com.mealplanner.entity.User;
import com.mealplanner.exception.UserNotFoundException;
import com.mealplanner.use_case.login.LoginDataAccessInterface;
import com.mealplanner.use_case.signup.SignupDataAccessInterface;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

// Data access object for user persistence - reads/writes user data to JSON files.
// Responsible: Mona (primary), Everyone (database shared responsibility)

public class FileUserDataAccessObject implements LoginDataAccessInterface, SignupDataAccessInterface {

    private static final String USERS_DIRECTORY = "data/users/";
    private static final String FILE_EXTENSION = ".json";

    public FileUserDataAccessObject() {
        ensureDirectoryExists();
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        String fileName = sanitizeFileName(username) + FILE_EXTENSION;
        File file = new File(USERS_DIRECTORY + fileName);
        return file.exists() && file.isFile();
    }

    @Override
    public User getUserByUsername(String username) throws UserNotFoundException {
        if (username == null || username.trim().isEmpty()) {
            throw new UserNotFoundException(username);
        }

        String fileName = sanitizeFileName(username) + FILE_EXTENSION;
        File file = new File(USERS_DIRECTORY + fileName);

        if (!file.exists()) {
            throw new UserNotFoundException(username);
        }

        try {
            String json = new String(Files.readAllBytes(Paths.get(file.getPath())));
            User user = JsonConverter.jsonToUser(json);

            if (user == null) {
                throw new UserNotFoundException("Failed to parse user data for: " + username, username);
            }

            return user;
        } catch (IOException e) {
            throw new UserNotFoundException("Failed to read user file for: " + username, username);
        }
    }

    /**
     * Ensures the users directory exists, creating it if necessary.
     */
    private void ensureDirectoryExists() {
        File directory = new File(USERS_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * Sanitizes a username to create a safe filename.
     * @param username the original username
     * @return sanitized filename
     */
    private String sanitizeFileName(String username) {
        return username.trim().replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    // SignupDataAccessInterface implementation

    @Override
    public void save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        try {
            String fileName = sanitizeFileName(user.getUsername()) + FILE_EXTENSION;
            File file = new File(USERS_DIRECTORY + fileName);

            // Convert user to JSON
            String json = JsonConverter.userToJson(user);

            // Write to file
            Files.write(Paths.get(file.getPath()), json.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save user: " + e.getMessage(), e);
        }
    }
}
