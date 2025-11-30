package com.mealplanner.util;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Utility class for executing background tasks with proper error handling,
 * loading state management, and UI thread synchronization.
 * 
 * This class helps prevent common threading issues and provides a consistent
 * pattern for background operations across the application.
 * 
 * Responsible: Everyone (shared utility)
 */
public class BackgroundTask {
    private static final Logger logger = LoggerFactory.getLogger(BackgroundTask.class);
    
    /**
     * Execute a background task with error handling and UI updates.
     * 
     * @param task Background task to execute (runs on background thread)
     * @param onSuccess Success callback (runs on JavaFX thread)
     * @param onError Error callback (runs on JavaFX thread)
     * @param loadingIndicator Runnable to show loading indicator (runs on JavaFX thread)
     * @param hideLoadingIndicator Runnable to hide loading indicator (runs on JavaFX thread)
     */
    public static void execute(
            Runnable task,
            Runnable onSuccess,
            Consumer<Exception> onError,
            Runnable loadingIndicator,
            Runnable hideLoadingIndicator
    ) {
        // Show loading indicator on JavaFX thread
        if (loadingIndicator != null) {
            Platform.runLater(loadingIndicator);
        }
        
        // Execute task on background thread
        new Thread(() -> {
            try {
                task.run();
                
                // On success, update UI on JavaFX thread
                Platform.runLater(() -> {
                    if (hideLoadingIndicator != null) {
                        hideLoadingIndicator.run();
                    }
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                });
            } catch (Exception e) {
                logger.error("Background task failed", e);
                
                // On error, update UI on JavaFX thread
                Platform.runLater(() -> {
                    if (hideLoadingIndicator != null) {
                        hideLoadingIndicator.run();
                    }
                    if (onError != null) {
                        onError.accept(e);
                    } else {
                        // Default error handling
                        logger.error("No error handler provided for background task", e);
                    }
                });
            }
        }).start();
    }
    
    /**
     * Execute a background task that returns a value.
     * 
     * @param supplier Background task that returns a value (runs on background thread)
     * @param onSuccess Success callback with result (runs on JavaFX thread)
     * @param onError Error callback (runs on JavaFX thread)
     * @param loadingIndicator Runnable to show loading indicator (runs on JavaFX thread)
     * @param hideLoadingIndicator Runnable to hide loading indicator (runs on JavaFX thread)
     * @param <T> Type of result
     */
    public static <T> void execute(
            Supplier<T> supplier,
            Consumer<T> onSuccess,
            Consumer<Exception> onError,
            Runnable loadingIndicator,
            Runnable hideLoadingIndicator
    ) {
        // Show loading indicator on JavaFX thread
        if (loadingIndicator != null) {
            Platform.runLater(loadingIndicator);
        }
        
        // Execute task on background thread
        new Thread(() -> {
            try {
                T result = supplier.get();
                
                // On success, update UI on JavaFX thread
                Platform.runLater(() -> {
                    if (hideLoadingIndicator != null) {
                        hideLoadingIndicator.run();
                    }
                    if (onSuccess != null) {
                        onSuccess.accept(result);
                    }
                });
            } catch (Exception e) {
                logger.error("Background task failed", e);
                
                // On error, update UI on JavaFX thread
                Platform.runLater(() -> {
                    if (hideLoadingIndicator != null) {
                        hideLoadingIndicator.run();
                    }
                    if (onError != null) {
                        onError.accept(e);
                    } else {
                        // Default error handling
                        logger.error("No error handler provided for background task", e);
                    }
                });
            }
        }).start();
    }
    
    /**
     * Simplified version without loading indicators.
     * 
     * @param task Background task to execute
     * @param onSuccess Success callback
     * @param onError Error callback
     */
    public static void execute(
            Runnable task,
            Runnable onSuccess,
            Consumer<Exception> onError
    ) {
        execute(task, onSuccess, onError, null, null);
    }
    
    /**
     * Execute with only error handling (no success callback).
     * 
     * @param task Background task to execute
     * @param onError Error callback
     */
    public static void execute(
            Runnable task,
            Consumer<Exception> onError
    ) {
        execute(task, null, onError, null, null);
    }
}

