package com.mealplanner.repository.impl;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mealplanner.config.AppConfig;
import com.mealplanner.entity.Recipe;
import com.mealplanner.exception.DataAccessException;
import com.mealplanner.repository.RecipeRepository;

/**
 * File-based implementation of RecipeRepository.
 * Stores recipes in JSON files on the file system.
 *
 * Responsible: Database team (Aaryan, Grace, Mona primary)
 */
public class FileRecipeRepository implements RecipeRepository {

    private static final Logger logger = LoggerFactory.getLogger(FileRecipeRepository.class);

    private final String dataDirectory;
    private final Gson gson;

    /**
     * Create a new FileRecipeRepository.
     *
     * @param dataDirectory Directory where recipe files are stored
     */
    public FileRecipeRepository(String dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        initializeDirectory();
        logger.info("FileRecipeRepository initialized with directory: {}", dataDirectory);
    }

    /**
     * Create a new FileRecipeRepository using the default path from AppConfig.
     */
    public FileRecipeRepository() {
        this(AppConfig.getRecipeDataPath());
    }

    private void initializeDirectory() {
        try {
            Path dirPath = Paths.get(dataDirectory);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                logger.info("Created recipe data directory: {}", dataDirectory);
            }
        } catch (IOException e) {
            logger.error("Failed to create data directory: {}", dataDirectory, e);
            throw new RuntimeException("Failed to initialize recipe repository", e);
        }
    }

    private String getFilePath(String recipeId) {
        return dataDirectory + File.separator + recipeId + AppConfig.getDataFileExtension();
    }

    @Override
    public void save(Recipe recipe) throws DataAccessException {
        if (recipe == null) {
            throw new DataAccessException("Cannot save null recipe");
        }
        if (recipe.getRecipeId() == null || recipe.getRecipeId().isEmpty()) {
            throw new DataAccessException("Recipe must have a valid ID");
        }

        logger.debug("Saving recipe: {}", recipe.getRecipeId());
        String filePath = getFilePath(recipe.getRecipeId());

        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(recipe, writer);
            logger.info("Successfully saved recipe: {}", recipe.getRecipeId());
        } catch (IOException e) {
            logger.error("Failed to save recipe: {}", recipe.getRecipeId(), e);
            throw new DataAccessException("Failed to save recipe: " + recipe.getRecipeId(), e);
        }
    }

    @Override
    public Optional<Recipe> findById(String recipeId) throws DataAccessException {
        if (recipeId == null || recipeId.isEmpty()) {
            return Optional.empty();
        }

        logger.debug("Finding recipe by ID: {}", recipeId);
        String filePath = getFilePath(recipeId);
        File file = new File(filePath);

        if (!file.exists()) {
            logger.debug("Recipe not found: {}", recipeId);
            return Optional.empty();
        }

        try (FileReader reader = new FileReader(file)) {
            Recipe recipe = gson.fromJson(reader, Recipe.class);
            logger.debug("Successfully loaded recipe: {}", recipeId);
            return Optional.ofNullable(recipe);
        } catch (IOException e) {
            logger.error("Failed to read recipe: {}", recipeId, e);
            throw new DataAccessException("Failed to read recipe: " + recipeId, e);
        }
    }

    @Override
    public List<Recipe> findAll() throws DataAccessException {
        logger.debug("Finding all recipes");
        File dir = new File(dataDirectory);

        if (!dir.exists() || !dir.isDirectory()) {
            logger.warn("Recipe directory does not exist: {}", dataDirectory);
            return new ArrayList<>();
        }

        try (Stream<Path> paths = Files.walk(Paths.get(dataDirectory), 1)) {
            List<Recipe> recipes = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(AppConfig.getDataFileExtension()))
                    .map(this::loadRecipeFromPath)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            logger.info("Loaded {} recipes", recipes.size());
            return recipes;
        } catch (IOException e) {
            logger.error("Failed to read recipes from directory", e);
            throw new DataAccessException("Failed to read recipes from directory", e);
        }
    }

    private Optional<Recipe> loadRecipeFromPath(Path path) {
        try (FileReader reader = new FileReader(path.toFile())) {
            Recipe recipe = gson.fromJson(reader, Recipe.class);
            return Optional.ofNullable(recipe);
        } catch (IOException e) {
            logger.error("Failed to load recipe from file: {}", path, e);
            return Optional.empty();
        }
    }

    @Override
    public List<Recipe> findByName(String name) throws DataAccessException {
        if (name == null || name.isEmpty()) {
            return new ArrayList<>();
        }

        logger.debug("Finding recipes by name: {}", name);
        String searchTerm = name.toLowerCase().trim();

        return findAll().stream()
                .filter(recipe -> recipe.getName().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
    }

    @Override
    public boolean delete(String recipeId) throws DataAccessException {
        if (recipeId == null || recipeId.isEmpty()) {
            return false;
        }

        logger.debug("Deleting recipe: {}", recipeId);
        String filePath = getFilePath(recipeId);
        File file = new File(filePath);

        if (!file.exists()) {
            logger.debug("Recipe file not found for deletion: {}", recipeId);
            return false;
        }

        try {
            boolean deleted = file.delete();
            if (deleted) {
                logger.info("Successfully deleted recipe: {}", recipeId);
            } else {
                logger.warn("Failed to delete recipe file: {}", recipeId);
            }
            return deleted;
        } catch (SecurityException e) {
            logger.error("Security exception while deleting recipe: {}", recipeId, e);
            throw new DataAccessException("Failed to delete recipe: " + recipeId, e);
        }
    }

    @Override
    public boolean exists(String recipeId) throws DataAccessException {
        if (recipeId == null || recipeId.isEmpty()) {
            return false;
        }

        logger.debug("Checking if recipe exists: {}", recipeId);
        String filePath = getFilePath(recipeId);
        return new File(filePath).exists();
    }

    @Override
    public int count() throws DataAccessException {
        logger.debug("Counting recipes");
        File dir = new File(dataDirectory);

        if (!dir.exists() || !dir.isDirectory()) {
            return 0;
        }

        try (Stream<Path> paths = Files.walk(Paths.get(dataDirectory), 1)) {
            long count = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(AppConfig.getDataFileExtension()))
                    .count();

            logger.debug("Recipe count: {}", count);
            return (int) count;
        } catch (IOException e) {
            logger.error("Failed to count recipes", e);
            throw new DataAccessException("Failed to count recipes", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        logger.warn("Clearing all recipes");
        File dir = new File(dataDirectory);

        if (!dir.exists() || !dir.isDirectory()) {
            logger.warn("Recipe directory does not exist, nothing to clear");
            return;
        }

        try (Stream<Path> paths = Files.walk(Paths.get(dataDirectory), 1)) {
            List<Path> filesToDelete = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(AppConfig.getDataFileExtension()))
                    .collect(Collectors.toList());

            int deletedCount = 0;
            for (Path path : filesToDelete) {
                try {
                    Files.delete(path);
                    deletedCount++;
                } catch (IOException e) {
                    logger.error("Failed to delete file: {}", path, e);
                }
            }

            logger.info("Cleared {} recipes", deletedCount);
        } catch (IOException e) {
            logger.error("Failed to clear recipes", e);
            throw new DataAccessException("Failed to clear recipes", e);
        }
    }
}
