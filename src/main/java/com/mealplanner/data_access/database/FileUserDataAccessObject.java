package com.mealplanner.data_access.database;

import com.mealplanner.entity.User;
import com.mealplanner.exception.UserNotFoundException;
import com.mealplanner.use_case.login.LoginDataAccessInterface;
import com.mealplanner.use_case.signup.SignupDataAccessInterface;
import com.mealplanner.use_case.update_nutrition_goals.UpdateNutritionGoalsDataAccessInterface;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

// Data access object for user persistence - reads/writes user data to JSON files.
// Responsible: Mona (primary), Everyone (database shared responsibility)

public class FileUserDataAccessObject implements LoginDataAccessInterface, SignupDataAccessInterface, UpdateNutritionGoalsDataAccessInterface {

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

    // UpdateNutritionGoalsDataAccessInterface implementation

    @Override
    public User getUserById(String userId) throws UserNotFoundException {
        if (userId == null || userId.trim().isEmpty()) {
            throw new UserNotFoundException("User ID cannot be null or empty");
        }

        // 모든 사용자 파일 검색 (username 기반 파일명이므로 모든 파일을 순회해야 함)
        File usersDir = new File(USERS_DIRECTORY);
        if (!usersDir.exists() || !usersDir.isDirectory()) {
            throw new UserNotFoundException("Users directory not found");
        }

        File[] userFiles = usersDir.listFiles((dir, name) -> name.endsWith(FILE_EXTENSION));

        if (userFiles == null || userFiles.length == 0) {
            throw new UserNotFoundException("No user files found");
        }

        // 각 파일을 읽어서 userId로 검색
        for (File file : userFiles) {
            try {
                String json = new String(Files.readAllBytes(Paths.get(file.getPath())));
                User user = JsonConverter.jsonToUser(json);

                if (user != null && userId.equals(user.getUserId())) {
                    // createdAt이 null이면 현재 시간으로 설정 (하위 호환성)
                    if (user.getCreatedAt() == null) {
                        // User는 불변 필드이므로 새 User 객체 생성 필요
                        // 하지만 이미 파싱된 user 객체는 createdAt이 없을 수 있음
                        // JsonConverter에서 처리하거나 여기서 처리
                        // 일단 반환하고, 필요시 마이그레이션 로직 추가
                    }
                    return user;
                }
            } catch (IOException e) {
                // 파일 읽기 실패 시 다음 파일로
                continue;
            }
        }

        throw new UserNotFoundException("User with ID: " + userId + " not found");
    }

    @Override
    public void updateUser(User user) throws UserNotFoundException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        // 기존 사용자 확인
        try {
            User existingUser = getUserById(user.getUserId());
            
            // 기존 파일명 (username 기반) 유지하여 업데이트
            String fileName = sanitizeFileName(existingUser.getUsername()) + FILE_EXTENSION;
            File file = new File(USERS_DIRECTORY + fileName);
            
            // User 객체를 JSON으로 변환하여 저장
            String json = JsonConverter.userToJson(user);
            Files.write(Paths.get(file.getPath()), json.getBytes());
            
        } catch (UserNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("Failed to update user: " + e.getMessage(), e);
        }
    }
}
