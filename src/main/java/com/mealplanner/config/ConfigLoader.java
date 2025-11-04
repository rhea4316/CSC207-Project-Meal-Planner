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
            // Load application.properties from resources
            loadResourceProperties("config/application.properties");

            // Try to load API keys from root config directory (fallback to environment variables)
            loadExternalApiKeys();

            initialized = true;
        } catch (IOException e) {
            System.err.println("Warning: Could not load all configuration files: " + e.getMessage());
            System.err.println("Some features may not work correctly. Please check your configuration.");
        }
    }

    /**
     * Loads properties from a resource file.
     */
    private static void loadResourceProperties(String resourcePath) throws IOException {
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IOException("Unable to find " + resourcePath);
            }
            properties.load(input);
        }
    }

    /**
     * Attempts to load API keys from external config file or environment variables.
     */
    private static void loadExternalApiKeys() {
        // Try to load from config/api_keys.properties file
        try (InputStream input = ConfigLoader.class.getClassLoader()
                .getResourceAsStream("../config/api_keys.properties")) {
            if (input != null) {
                Properties apiKeys = new Properties();
                apiKeys.load(input);
                properties.putAll(apiKeys);
                return;
            }
        } catch (IOException e) {
            // Ignore, will try environment variables
        }

        // Fallback to environment variables
        String spoonacularKey = System.getenv("SPOONACULAR_API_KEY");
        if (spoonacularKey != null && !spoonacularKey.isEmpty()) {
            properties.setProperty("spoonacular.api.key", spoonacularKey);
        }

        String edamamId = System.getenv("EDAMAM_APP_ID");
        if (edamamId != null && !edamamId.isEmpty()) {
            properties.setProperty("edamam.app.id", edamamId);
        }

        String edamamKey = System.getenv("EDAMAM_APP_KEY");
        if (edamamKey != null && !edamamKey.isEmpty()) {
            properties.setProperty("edamam.app.key", edamamKey);
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
        String value = getProperty(key);
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
        String value = getProperty(key);
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
