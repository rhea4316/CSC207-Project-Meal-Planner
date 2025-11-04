package com.mealplanner.config;

/**
 * Configuration class for API settings (Spoonacular and Edamam).
 * Centralizes API configuration to make it easy to access throughout the application.
 * Responsible: Everyone (especially those working with API integration)
 */
public class ApiConfig {

    // Private constructor to prevent instantiation
    private ApiConfig() {
        throw new AssertionError("ApiConfig should not be instantiated");
    }

    // Spoonacular API Configuration

    /**
     * Gets the Spoonacular API key.
     *
     * @return the API key, or null if not configured
     */
    public static String getSpoonacularApiKey() {
        return ConfigLoader.getProperty("spoonacular.api.key");
    }

    /**
     * Gets the Spoonacular API base URL.
     *
     * @return the base URL (default: https://api.spoonacular.com)
     */
    public static String getSpoonacularBaseUrl() {
        return ConfigLoader.getProperty("spoonacular.base.url", "https://api.spoonacular.com");
    }

    /**
     * Gets the maximum number of results to return from Spoonacular search.
     *
     * @return max results (default: 10)
     */
    public static int getSpoonacularMaxResults() {
        return ConfigLoader.getIntProperty("spoonacular.max.results", 10);
    }

    /**
     * Gets the timeout in seconds for Spoonacular API calls.
     *
     * @return timeout in seconds (default: 30)
     */
    public static int getSpoonacularTimeoutSeconds() {
        return ConfigLoader.getIntProperty("spoonacular.timeout.seconds", 30);
    }

    /**
     * Checks if Spoonacular API is configured (has API key).
     *
     * @return true if API key is set, false otherwise
     */
    public static boolean isSpoonacularConfigured() {
        // TODO: Implement Spoonacular configuration check
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // Edamam API Configuration

    /**
     * Gets the Edamam Application ID.
     *
     * @return the app ID, or null if not configured
     */
    public static String getEdamamAppId() {
        return ConfigLoader.getProperty("edamam.app.id");
    }

    /**
     * Gets the Edamam Application Key.
     *
     * @return the app key, or null if not configured
     */
    public static String getEdamamAppKey() {
        return ConfigLoader.getProperty("edamam.app.key");
    }

    /**
     * Gets the Edamam API base URL.
     *
     * @return the base URL (default: https://api.edamam.com)
     */
    public static String getEdamamBaseUrl() {
        return ConfigLoader.getProperty("edamam.base.url", "https://api.edamam.com");
    }

    /**
     * Gets the timeout in seconds for Edamam API calls.
     *
     * @return timeout in seconds (default: 30)
     */
    public static int getEdamamTimeoutSeconds() {
        return ConfigLoader.getIntProperty("edamam.timeout.seconds", 30);
    }

    /**
     * Checks if Edamam API is configured (has both app ID and key).
     *
     * @return true if both app ID and key are set, false otherwise
     */
    public static boolean isEdamamConfigured() {
        // TODO: Implement Edamam configuration check
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
