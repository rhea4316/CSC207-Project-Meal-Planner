package com.mealplanner.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for loading application configuration from properties files.
 * Loads configuration from both application.properties (in resources) and external API keys file.
 * Responsible: Everyone (use throughout application for configuration access)
 */
public class ConfigLoader {

    private static final Properties properties = new Properties();
    private static boolean initialized = false;

    // Private constructor to prevent instantiation
    private ConfigLoader() {
        throw new AssertionError("ConfigLoader should not be instantiated");
    }

    /**
     * Initializes the configuration by loading properties from resources and external files.
     * This method is called automatically on first access, but can be called manually for testing.
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        try {
            loadResourceProperties("/config/application.properties");
            loadExternalApiKeys();
            initialized = true;
        } catch (IOException e) {
            // If loading fails, continue with empty properties (will use defaults)
            System.err.println("Warning: Could not load configuration: " + e.getMessage());
        }
    }

    /**
     * Loads properties from a resource file.
     */
    private static void loadResourceProperties(String resourcePath) throws IOException {
        try (InputStream input = ConfigLoader.class.getResourceAsStream(resourcePath)) {
            if (input != null) {
                properties.load(input);
            }
        }
    }

    /**
     * Attempts to load API keys from external config file or environment variables.
     */
    private static void loadExternalApiKeys() {
        // Try to load from config/api_keys.properties file if it exists
        try (InputStream input = ConfigLoader.class.getResourceAsStream("/config/api_keys.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            // Ignore - API keys are optional
        }
    }

    /**
     * Gets a configuration property value.
     *
     * @param key the property key
     * @return the property value, or null if not found
     */
    public static String getProperty(String key) {
        if (!initialized) {
            initialize();
        }
        return properties.getProperty(key);
    }

    /**
     * Gets a configuration property value with a default.
     *
     * @param key the property key
     * @param defaultValue the default value if property not found
     * @return the property value, or defaultValue if not found
     */
    public static String getProperty(String key, String defaultValue) {
        if (!initialized) {
            initialize();
        }
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Gets a configuration property as an integer.
     *
     * @param key the property key
     * @param defaultValue the default value if property not found or not a valid integer
     * @return the property value as integer, or defaultValue if not found/invalid
     */
    public static int getIntProperty(String key, int defaultValue) {
        if (!initialized) {
            initialize();
        }
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Gets a configuration property as a boolean.
     *
     * @param key the property key
     * @param defaultValue the default value if property not found
     * @return the property value as boolean, or defaultValue if not found
     */
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        if (!initialized) {
            initialize();
        }
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Checks if a configuration property exists.
     *
     * @param key the property key
     * @return true if the property exists, false otherwise
     */
    public static boolean hasProperty(String key) {
        if (!initialized) {
            initialize();
        }
        return properties.containsKey(key);
    }

    /**
     * Reloads all configuration. Useful for testing.
     */
    public static synchronized void reload() {
        properties.clear();
        initialized = false;
        initialize();
    }
}
