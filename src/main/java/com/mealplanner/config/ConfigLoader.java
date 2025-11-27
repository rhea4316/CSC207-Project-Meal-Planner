package com.mealplanner.config;

import java.io.File;
import java.io.FileInputStream;
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
        // First, try to load from project root config/api_keys.properties file
        try {
            File configFile = new File("config/api_keys.properties");
            if (configFile.exists() && configFile.isFile()) {
                try (FileInputStream input = new FileInputStream(configFile)) {
                    properties.load(input);
                }
            }
        } catch (IOException e) {
            // Ignore - try other sources
        }
        
        // Also try to load from resources/config/api_keys.properties
        try (InputStream input = ConfigLoader.class.getResourceAsStream("/config/api_keys.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            // Ignore - API keys are optional
        }
        
        // Override with environment variables if they exist
        String spoonacularKey = System.getenv("SPOONACULAR_API_KEY");
        if (spoonacularKey != null && !spoonacularKey.trim().isEmpty()) {
            properties.setProperty("spoonacular.api.key", spoonacularKey);
        }
        
        String edamamAppId = System.getenv("EDAMAM_APP_ID");
        if (edamamAppId != null && !edamamAppId.trim().isEmpty()) {
            properties.setProperty("edamam.app.id", edamamAppId);
        }
        
        String edamamAppKey = System.getenv("EDAMAM_APP_KEY");
        if (edamamAppKey != null && !edamamAppKey.trim().isEmpty()) {
            properties.setProperty("edamam.app.key", edamamAppKey);
        }
        
        // Resolve environment variable placeholders in properties
        resolveEnvironmentVariables();
    }
    
    /**
     * Resolves environment variable placeholders in property values.
     * Replaces ${VAR_NAME} with actual environment variable values.
     */
    private static void resolveEnvironmentVariables() {
        Properties resolved = new Properties();
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            if (value != null && value.startsWith("${") && value.endsWith("}")) {
                String envVarName = value.substring(2, value.length() - 1);
                String envValue = System.getenv(envVarName);
                if (envValue != null && !envValue.trim().isEmpty()) {
                    resolved.setProperty(key, envValue);
                } else {
                    // Keep original value if env var not found
                    resolved.setProperty(key, value);
                }
            } else {
                resolved.setProperty(key, value);
            }
        }
        properties.clear();
        properties.putAll(resolved);
    }

    /**
     * Gets a configuration property value.
     *
     * @param key the property key
     * @return the property value, or null if not found
     */
    public static String getProperty(String key) {
        if (key == null) {
            return null;
        }
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
        if (key == null) {
            return defaultValue;
        }
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
        if (key == null) {
            return defaultValue;
        }
        if (!initialized) {
            initialize();
        }
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
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
        if (key == null) {
            return defaultValue;
        }
        if (!initialized) {
            initialize();
        }
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }

    /**
     * Checks if a configuration property exists.
     *
     * @param key the property key
     * @return true if the property exists, false otherwise
     */
    public static boolean hasProperty(String key) {
        if (key == null) {
            return false;
        }
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
