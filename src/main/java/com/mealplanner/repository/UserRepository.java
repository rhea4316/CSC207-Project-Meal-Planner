package com.mealplanner.repository;

import com.mealplanner.entity.User;
import com.mealplanner.exception.DataAccessException;
import com.mealplanner.exception.UserNotFoundException;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User data access.
 * Provides abstraction over the underlying storage mechanism.
 *
 * This interface allows the application to change storage implementations
 * (file, database, API) without affecting business logic.
 *
 * Responsible: Mona (primary), Everyone (database team implements)
 */
public interface UserRepository {

    /**
     * Save a user to the repository.
     * If a user with the same ID exists, it will be updated.
     *
     * @param user User to save
     * @throws DataAccessException if save operation fails
     */
    void save(User user) throws DataAccessException;

    /**
     * Find a user by their unique ID.
     *
     * @param userId User ID to search for
     * @return Optional containing the user if found, empty otherwise
     * @throws DataAccessException if read operation fails
     */
    Optional<User> findById(String userId) throws DataAccessException;

    /**
     * Find a user by their username.
     *
     * @param username Username to search for
     * @return Optional containing the user if found, empty otherwise
     * @throws DataAccessException if read operation fails
     */
    Optional<User> findByUsername(String username) throws DataAccessException;

    /**
     * Find all users in the repository.
     *
     * @return List of all users (may be empty)
     * @throws DataAccessException if read operation fails
     */
    List<User> findAll() throws DataAccessException;

    /**
     * Delete a user by their ID.
     *
     * @param userId User ID to delete
     * @return true if user was deleted, false if not found
     * @throws DataAccessException if delete operation fails
     */
    boolean delete(String userId) throws DataAccessException;

    /**
     * Check if a user with the given ID exists.
     *
     * @param userId User ID to check
     * @return true if user exists
     * @throws DataAccessException if read operation fails
     */
    boolean exists(String userId) throws DataAccessException;

    /**
     * Check if a username is already taken.
     *
     * @param username Username to check
     * @return true if username exists
     * @throws DataAccessException if read operation fails
     */
    boolean usernameExists(String username) throws DataAccessException;

    /**
     * Get the total count of users in the repository.
     *
     * @return Number of users
     * @throws DataAccessException if read operation fails
     */
    int count() throws DataAccessException;

    /**
     * Clear all users from the repository.
     * Use with caution!
     *
     * @throws DataAccessException if delete operation fails
     */
    void clear() throws DataAccessException;
}
