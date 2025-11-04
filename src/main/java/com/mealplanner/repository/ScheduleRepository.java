package com.mealplanner.repository;

import com.mealplanner.entity.Schedule;
import com.mealplanner.exception.DataAccessException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Schedule data access.
 * Provides abstraction over the underlying storage mechanism.
 *
 * This interface allows the application to change storage implementations
 * (file, database, API) without affecting business logic.
 *
 * Responsible: Grace (primary), Everyone (database team implements)
 */
public interface ScheduleRepository {

    /**
     * Save a schedule to the repository.
     * If a schedule with the same ID exists, it will be updated.
     *
     * @param schedule Schedule to save
     * @throws DataAccessException if save operation fails
     */
    void save(Schedule schedule) throws DataAccessException;

    /**
     * Find a schedule by its unique ID.
     *
     * @param scheduleId Schedule ID to search for
     * @return Optional containing the schedule if found, empty otherwise
     * @throws DataAccessException if read operation fails
     */
    Optional<Schedule> findById(String scheduleId) throws DataAccessException;

    /**
     * Find a schedule by user ID.
     *
     * @param userId User ID to search for
     * @return Optional containing the schedule if found, empty otherwise
     * @throws DataAccessException if read operation fails
     */
    Optional<Schedule> findByUserId(String userId) throws DataAccessException;

    /**
     * Find all schedules in the repository.
     *
     * @return List of all schedules (may be empty)
     * @throws DataAccessException if read operation fails
     */
    List<Schedule> findAll() throws DataAccessException;

    /**
     * Find schedules containing a specific date.
     *
     * @param date Date to search for
     * @return List of schedules containing this date (may be empty)
     * @throws DataAccessException if read operation fails
     */
    List<Schedule> findByDate(LocalDate date) throws DataAccessException;

    /**
     * Delete a schedule by its ID.
     *
     * @param scheduleId Schedule ID to delete
     * @return true if schedule was deleted, false if not found
     * @throws DataAccessException if delete operation fails
     */
    boolean delete(String scheduleId) throws DataAccessException;

    /**
     * Delete a schedule by user ID.
     *
     * @param userId User ID whose schedule to delete
     * @return true if schedule was deleted, false if not found
     * @throws DataAccessException if delete operation fails
     */
    boolean deleteByUserId(String userId) throws DataAccessException;

    /**
     * Check if a schedule with the given ID exists.
     *
     * @param scheduleId Schedule ID to check
     * @return true if schedule exists
     * @throws DataAccessException if read operation fails
     */
    boolean exists(String scheduleId) throws DataAccessException;

    /**
     * Get the total count of schedules in the repository.
     *
     * @return Number of schedules
     * @throws DataAccessException if read operation fails
     */
    int count() throws DataAccessException;

    /**
     * Clear all schedules from the repository.
     * Use with caution!
     *
     * @throws DataAccessException if delete operation fails
     */
    void clear() throws DataAccessException;
}
