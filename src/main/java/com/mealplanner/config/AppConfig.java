package com.mealplanner.config;

/**
 * Configuration class for application-wide settings (data storage, UI, caching).
 * Centralizes application configuration to make it easy to access throughout the application.
 * Responsible: Everyone (use throughout application)
 */
public class AppConfig {

    // Private constructor to prevent instantiation
    private AppConfig() {
        throw new AssertionError("AppConfig should not be instantiated");
    }

    // Application Info

    /**
     * Gets the application name.
     *
     * @return the app name (default: "Meal Planner")
     */
    public static String getAppName() {
        return ConfigLoader.getProperty("app.name", "Meal Planner");
    }

    /**
     * Gets the application version.
     *
     * @return the version string (default: "1.0.0")
     */
    public static String getAppVersion() {
        return ConfigLoader.getProperty("app.version", "1.0.0");
    }

    // Data Storage Configuration

    /**
     * Gets the data storage type.
     *
     * @return storage type (default: "file")
     */
    public static String getDataStorageType() {
        return ConfigLoader.getProperty("data.storage.type", "file");
    }

    /**
     * Gets the path for user data files.
     *
     * @return user data path (default: "data/users")
     */
    public static String getUserDataPath() {
        return ConfigLoader.getProperty("data.users.path", "data/users");
    }

    /**
     * Gets the path for recipe data files.
     *
     * @return recipe data path (default: "data/recipes")
     */
    public static String getRecipeDataPath() {
        return ConfigLoader.getProperty("data.recipes.path", "data/recipes");
    }

    /**
     * Gets the path for schedule data files.
     *
     * @return schedule data path (default: "data/schedules")
     */
    public static String getScheduleDataPath() {
        return ConfigLoader.getProperty("data.schedules.path", "data/schedules");
    }

    /**
     * Gets the file extension for data files.
     *
     * @return file extension (default: ".json")
     */
    public static String getDataFileExtension() {
        return ConfigLoader.getProperty("data.file.extension", ".json");
    }

    // Cache Configuration

    /**
     * Checks if caching is enabled.
     *
     * @return true if caching is enabled (default: true)
     */
    public static boolean isCacheEnabled() {
        return ConfigLoader.getBooleanProperty("cache.enabled", true);
    }

    /**
     * Gets the cache time-to-live in minutes.
     *
     * @return TTL in minutes (default: 30)
     */
    public static int getCacheTtlMinutes() {
        return ConfigLoader.getIntProperty("cache.ttl.minutes", 30);
    }

    /**
     * Gets the maximum cache size (number of items).
     *
     * @return max cache size (default: 100)
     */
    public static int getCacheMaxSize() {
        return ConfigLoader.getIntProperty("cache.max.size", 100);
    }

    // UI Configuration

    /**
     * Gets the default window width.
     *
     * @return window width in pixels (default: 1200)
     */
    public static int getWindowWidth() {
        return ConfigLoader.getIntProperty("ui.window.width", 1200);
    }

    /**
     * Gets the default window height.
     *
     * @return window height in pixels (default: 800)
     */
    public static int getWindowHeight() {
        return ConfigLoader.getIntProperty("ui.window.height", 800);
    }

    /**
     * Gets the UI theme.
     *
     * @return theme name (default: "light")
     */
    public static String getTheme() {
        return ConfigLoader.getProperty("ui.theme", "light");
    }

    // Logging Configuration

    /**
     * Gets the logging level.
     *
     * @return log level (default: "INFO")
     */
    public static String getLoggingLevel() {
        return ConfigLoader.getProperty("logging.level", "INFO");
    }

    /**
     * Gets the log file path.
     *
     * @return log file path (default: "logs/meal-planner.log")
     */
    public static String getLogFilePath() {
        return ConfigLoader.getProperty("logging.file.path", "logs/meal-planner.log");
    }
}
