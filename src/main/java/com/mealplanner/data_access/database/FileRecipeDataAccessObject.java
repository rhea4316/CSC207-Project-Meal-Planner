package com.mealplanner.data_access.database;

import com.mealplanner.entity.Recipe;
import com.mealplanner.exception.DataAccessException;
import com.mealplanner.use_case.store_recipe.StoreRecipeDataAccessInterface;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

// Data access object for recipe persistence - reads/writes recipe data to JSON files.
// Responsible: Aaryan (primary for storage), Everyone (database shared responsibility)

public class FileRecipeDataAccessObject implements StoreRecipeDataAccessInterface {

    private static final String RECIPES_DIRECTORY = "data/recipes/";
    private static final String FILE_EXTENSION = ".json";

    public FileRecipeDataAccessObject() {
        ensureDirectoryExists();
    }

    @Override
    public void save(Recipe recipe) throws DataAccessException {
        if (recipe == null) {
            throw new DataAccessException("Recipe cannot be null");
        }

        String recipeId = recipe.getRecipeId();
        if (recipeId == null || recipeId.trim().isEmpty()) {
            recipeId = generateRecipeId();
        }

        String fileName = sanitizeFileName(recipeId) + FILE_EXTENSION;
        File file = new File(RECIPES_DIRECTORY + fileName);

        try {
            String json = JsonConverter.recipeToJson(recipe);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(json);
            }
        } catch (IOException e) {
            throw new DataAccessException("Failed to save recipe: " + recipeId, e);
        }
    }

    /**
     * Ensures the recipes directory exists, creating it if necessary.
     * @throws DataAccessException if directory creation fails
     */
    private void ensureDirectoryExists() {
        File directory = new File(RECIPES_DIRECTORY);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new DataAccessException("Failed to create recipes directory: " + RECIPES_DIRECTORY);
            }
        }
    }

    /**
     * Generates a unique recipe ID using UUID.
     * @return a unique recipe ID string
     */
    private String generateRecipeId() {
        return "recipe_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Sanitizes a filename to remove potentially dangerous characters.
     * @param fileName the original file name
     * @return sanitized file name
     */
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
