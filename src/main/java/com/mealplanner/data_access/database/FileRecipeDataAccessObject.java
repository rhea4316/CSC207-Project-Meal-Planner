package com.mealplanner.data_access.database;

import com.mealplanner.entity.Recipe;
import com.mealplanner.entity.User;
import com.mealplanner.exception.DataAccessException;
import com.mealplanner.repository.RecipeRepository;
import com.mealplanner.repository.UserRepository;
import com.mealplanner.use_case.get_recommendations.GetRecommendationsDataAccessInterface;
import com.mealplanner.use_case.store_recipe.StoreRecipeDataAccessInterface;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Data access object for recipe persistence - reads/writes recipe data to JSON files.
// Responsible: Aaryan (primary for storage), Everyone (database shared responsibility)

public class FileRecipeDataAccessObject implements StoreRecipeDataAccessInterface, GetRecommendationsDataAccessInterface {

    private static final String RECIPES_DIRECTORY = "data/recipes/";
    private static final String FILE_EXTENSION = ".json";
    
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;

    public FileRecipeDataAccessObject() {
        this(null, null);
    }
    
    public FileRecipeDataAccessObject(UserRepository userRepository) {
        this(userRepository, null);
    }
    
    public FileRecipeDataAccessObject(UserRepository userRepository, RecipeRepository recipeRepository) {
        this.userRepository = userRepository;
        this.recipeRepository = recipeRepository;
        ensureDirectoryExists();
    }

    @Override
    public void save(Recipe recipe) {
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
     * Retrieves a recipe by its ID.
     * @param recipeId the unique identifier of the recipe
     * @return Recipe object if found, null otherwise
     */
    public Recipe getRecipeById(String recipeId) {
        if (recipeId == null || recipeId.trim().isEmpty()) {
            return null;
        }

        String fileName = sanitizeFileName(recipeId) + FILE_EXTENSION;
        Path filePath = Path.of(RECIPES_DIRECTORY + fileName);

        if (!Files.exists(filePath)) {
            return null;
        }

        try {
            String json = Files.readString(filePath);
            return JsonConverter.jsonToRecipe(json);
        } catch (IOException e) {
            throw new DataAccessException("Failed to read recipe: " + recipeId, e);
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
    
    @Override
    public List<Recipe> getSavedRecipesByUser(String userId) {
        List<Recipe> recipes = new ArrayList<>();
        
        if (userId == null || userId.trim().isEmpty()) {
            return recipes;
        }
        
        if (userRepository == null) {
            // UserRepository가 없으면 빈 리스트 반환
            return recipes;
        }
        
        try {
            // User 조회
            java.util.Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return recipes;
            }
            
            User user = userOpt.get();
            List<String> savedRecipeIds = user.getSavedRecipeIds();
            
            // 각 recipeId로 레시피 조회
            for (String recipeId : savedRecipeIds) {
                Recipe recipe = getRecipeById(recipeId);
                if (recipe != null) {
                    recipes.add(recipe);
                }
            }
        } catch (Exception e) {
            // 에러 발생 시 빈 리스트 반환
            return recipes;
        }
        
        return recipes;
    }
    
    @Override
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        
        if (recipeRepository == null) {
            // RecipeRepository가 없으면 빈 리스트 반환
            return recipes;
        }
        
        try {
            recipes = recipeRepository.findAll();
        } catch (Exception e) {
            // 에러 발생 시 빈 리스트 반환
            return recipes;
        }
        
        return recipes;
    }
}
