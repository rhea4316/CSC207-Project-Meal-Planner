package com.mealplanner.app;

import com.mealplanner.entity.User;

/**
 * Thread-safe singleton class for managing the current logged-in user session.
 * This provides a centralized way to access the current user's information
 * across different views and components.
 *
 * Uses the Bill Pugh Singleton pattern (holder pattern) for thread safety
 * without synchronization overhead.
 *
 * Responsible: Everyone (shared session management)
 */
public class SessionManager {

    private volatile User currentUser;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private SessionManager() {
        this.currentUser = null;
    }

    /**
     * Holder class for lazy initialization with thread safety.
     */
    private static class SingletonHolder {
        private static final SessionManager INSTANCE = new SessionManager();
    }

    /**
     * Gets the singleton instance of SessionManager.
     * Thread-safe without synchronization overhead.
     *
     * @return the SessionManager instance
     */
    public static SessionManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Sets the current logged-in user.
     *
     * @param user the user to set as current (can be null to clear)
     */
    public synchronized void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Gets the current logged-in user.
     *
     * @return the current user, or null if no user is logged in
     */
    public synchronized User getCurrentUser() {
        return currentUser;
    }

    /**
     * Clears the current user session (for logout).
     */
    public synchronized void clearSession() {
        this.currentUser = null;
    }

    /**
     * Checks if a user is currently logged in.
     *
     * @return true if a user is logged in, false otherwise
     */
    public synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Gets the current user's ID if logged in.
     *
     * @return the user ID, or null if not logged in
     */
    public String getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    /**
     * Gets the current user's username if logged in.
     *
     * @return the username, or null if not logged in
     */
    public String getCurrentUsername() {
        User user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }
}
