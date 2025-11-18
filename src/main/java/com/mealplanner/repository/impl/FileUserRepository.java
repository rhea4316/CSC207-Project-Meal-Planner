package com.mealplanner.repository.impl;

import com.mealplanner.entity.User;
import com.mealplanner.exception.DataAccessException;
import com.mealplanner.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * File-based implementation of UserRepository.
 * Stores users in JSON files on the file system.
 *
 * Responsible: Database team (Mona primary, Aaryan, Grace)
 * TODO: Implement file I/O operations for user storage
 */
public class FileUserRepository implements UserRepository {

    private static final Logger logger = LoggerFactory.getLogger(FileUserRepository.class);

    private final String dataDirectory;

    /**
     * Create a new FileUserRepository.
     *
     * @param dataDirectory Directory where user files are stored
     */
    public FileUserRepository(String dataDirectory) {
        this.dataDirectory = dataDirectory;
        // TODO: Initialize directory, create if doesn't exist
        logger.info("FileUserRepository initialized with directory: {}", dataDirectory);
    }

    @Override
    public void save(User user) throws DataAccessException {
        // TODO: Implement save - serialize user to JSON file
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Optional<User> findById(String userId) throws DataAccessException {
        // TODO: Implement findById - read user JSON file
        logger.debug("Finding user by ID: {}", userId);
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Optional<User> findByUsername(String username) throws DataAccessException {
        // TODO: Implement findByUsername - search through all users
        logger.debug("Finding user by username: {}", username);
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public List<User> findAll() throws DataAccessException {
        // TODO: Implement findAll - read all user files in directory
        logger.debug("Finding all users");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean delete(String userId) throws DataAccessException {
        // TODO: Implement delete - remove user file
        logger.debug("Deleting user: {}", userId);
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean exists(String userId) throws DataAccessException {
        // TODO: Implement exists - check if user file exists
        logger.debug("Checking if user exists: {}", userId);
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean usernameExists(String username) throws DataAccessException {
        // TODO: Implement usernameExists - search through all users
        logger.debug("Checking if username exists: {}", username);
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int count() throws DataAccessException {
        // TODO: Implement count - count user files in directory
        logger.debug("Counting users");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void clear() throws DataAccessException {
        // TODO: Implement clear - delete all user files
        logger.warn("Clearing all users");
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
