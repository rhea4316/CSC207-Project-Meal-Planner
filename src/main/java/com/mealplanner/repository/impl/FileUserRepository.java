package com.mealplanner.repository.impl;

import com.mealplanner.entity.User;
import com.mealplanner.exception.DataAccessException;
import com.mealplanner.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * File-based implementation of UserRepository.
 * Stores users in JSON files on the file system.
 *
 * Responsible: Database team (Mona primary, Aaryan, Grace)
 */

public class FileUserRepository implements UserRepository {

    private static final Logger logger = LoggerFactory.getLogger(FileUserRepository.class);

    private static final String File_Extension = ".user";

    private Path dataDir;

    private final String dataDirectory;


    /**
     * Create a new FileUserRepository.
     *
     * @param dataDirectory Directory where user files are stored
     */
    public FileUserRepository(String dataDirectory) {
        this.dataDirectory = dataDirectory;

        // done: Initialize directory, create if doesn't exist

        this.dataDir = Paths.get(dataDirectory);
        try{
            Files.createDirectories(this.dataDir);
            logger.info("User directory created at: {}", this.dataDir);
        } catch (IOException e){
            logger.error("Error creating user directory.", this,dataDir, e);
        }

        logger.info("FileUserRepository initialized with directory: {}", dataDirectory);
    }
    private Path getUserFilePath(String userId){
        return dataDir.resolve(userId + File_Extension);
    }

    private String serializeUser(User user){
        StringBuilder sb = new StringBuilder();
        sb.append("userId=").append(user.getUsername()).append("\n");
        sb.append("username=").append(user.getUsername()).append("\n");
        return sb.toString();
    }

    private User deserializeUser(String content){
        String[] lines = content.split("\\R");
        String userid = null;
        String username = null;

        for (String line : lines) {
            int idx = line.indexOf('=');
            if (idx <= 0){
                continue;
            }
            String key = line.substring(0, idx).trim();
            String value = line.substring(idx + 1).trim();

            if (key.equals("userId")){
                userid = value;
            } else if (key.equals("username")){
                username = value;
            }
        }
        if (userid == null || username == null){
            throw new IllegalArgumentException("Invalid user or username format.");
        }

        return new User(userid, username);
    }

    @Override
    public void save(User user) throws DataAccessException {
        // done: Implement save - serialize user to JSON file
        logger.debug("Saving user: {}", user.getUserId());
        Path filepath = getUserFilePath(user.getUserId());

        try {
            String content = serializeUser(user);
            Files.writeString(filepath, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            } catch (IOException e) {
            logger.error("Error saving user to file");
            throw new DataAccessException("Error saving user to file", e);
        }
        }

    @Override
    public Optional<User> findById(String userId) throws DataAccessException {
        logger.debug("Finding user by ID: {}", userId);
        Path filepath = getUserFilePath(userId);

        if (!Files.exists(filepath)) {
            return Optional.empty();
        }
        try {
            String content = Files.readString(filepath, StandardCharsets.UTF_8);
            User user = deserializeUser(content);
            return Optional.of(user);
        } catch (IOException e) {
            logger.error("Error reading user from file.", e);
        }catch  (Exception e){
            logger.error("Invalid user file.", e);
            throw new DataAccessException("Invalid file .", e);
        }
        return Optional.empty();

    }

    @Override
    public Optional<User> findByUsername(String username) throws DataAccessException {
        // done: Implement findByUsername - search through all users
        logger.debug("Finding user by username: {}", username);

        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }

        try{
            for (User user :findAll()){
                if (user.getUsername().equals(username)){
                    return Optional.of(user);
                }
            }
            return Optional.empty();
        } catch (Exception e){
            logger.error("Failed to find user by username.", e);
            throw new DataAccessException("Failed to find user by username.", e);
        }

    }

    @Override
    public List<User> findAll() throws DataAccessException {
        // done: Implement findAll - read all user files in directory
        logger.debug("Finding all users");

        List<User> users = new ArrayList<>();

        try(DirectoryStream<Path> stream = Files.newDirectoryStream(dataDir, "*" + File_Extension))
        {

            for (Path path : stream) {
                if (!Files.isRegularFile(path)) {
                    continue;
                }

                try {
                    String content = Files.readString(path, StandardCharsets.UTF_8);
                    User user = deserializeUser(content);
                    users.add(user);
                } catch (IOException e) {
                    logger.warn("Failed to read file {}, skipping.", path, e);
                }
            }

        }catch (IOException e){
            logger.error("Failed to list users in directory{}.", dataDir, e);
        }
        return users;

    }

    @Override
    public boolean delete(String userId) throws DataAccessException {
        // done: Implement delete - remove user file
        logger.debug("Deleting user: {}", userId);
        Path filepath = getUserFilePath(userId);

        try{
            return Files.deleteIfExists(filepath);
        } catch (IOException e){
        logger.error("Failed to delete user file", e);
        throw new DataAccessException("Failed to delete user file", e);
        }
    }

    @Override
    public boolean exists(String userId) throws DataAccessException {
        // done: Implement exists - check if user file exists
        logger.debug("Checking if user exists: {}", userId);
        Path filepath = getUserFilePath(userId);

        return Files.exists(filepath) && Files.isRegularFile(filepath);
    }

    @Override
    public boolean usernameExists(String username) throws DataAccessException {
        // done: Implement usernameExists - search through all users
        logger.debug("Checking if username exists: {}", username);
        try {
            for (User user :findAll()){
                if (user.getUsername().equals(username)){
                    return true;
                }
            }
            return false;
        } catch (Exception e){
            logger.error("Failed to check if username exists", e);
            throw new DataAccessException("Failed to check if username exists", e);
        }
    }

    @Override
    public int count() throws DataAccessException {
        // done: Implement count - count user files in directory
        logger.debug("Counting users");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dataDir,
                "*" + File_Extension)){
            int count = 0;
            for (Path ignored: stream) {
                count++;
            }
            return count;
        } catch (IOException e){
            logger.error("Failed to count users", e);
            throw new DataAccessException("Failed to count users", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        // done Implement clear - delete all user files
        logger.warn("Clearing all users");
    try(DirectoryStream<Path> stream = Files.newDirectoryStream(dataDir, "*" + File_Extension)){
        for (Path path : stream) {
            try{
                Files.deleteIfExists(path);
            } catch(IOException e){
                logger.warn("Failed to clear all users", e);

            }
            }
        } catch (IOException e){
        logger.error("Failed to clear all users", e);
        throw new DataAccessException("Failed to clear all users", e);
    }
    }
}
