package com.mealplanner.app;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.mealplanner.entity.Recipe;
import com.mealplanner.exception.DataAccessException;
import com.mealplanner.repository.RecipeRepository;
import com.mealplanner.repository.impl.FileRecipeRepository;

/**
 * Demo application to test FileRecipeRepository functionality.
 */
public class FileRecipeRepositoryDemo {

    public static void main(String[] args) {
        try {
            // Use test directory
            RecipeRepository repo = new FileRecipeRepository("data/recipes-test");

            System.out.println("=== FileRecipeRepository Demo ===\n");

            // Clear any existing data
            System.out.println("Clearing existing recipes...");
            repo.clear();
            System.out.println("Initial count: " + repo.count() + "\n");

            // Create and save a recipe
            System.out.println("Creating and saving a recipe...");
            Recipe recipe1 = new Recipe(
                    "Pasta Carbonara",
                    Arrays.asList("400g spaghetti", "200g pancetta", "4 eggs", "100g parmesan", "Black pepper"),
                    String.valueOf(Arrays.asList("Boil pasta", "Cook pancetta", "Mix eggs and cheese", "Combine all", "Serve hot")),
                    4,
                    null,
                    30,
                    null,
                    "recipe-001"
            );
            repo.save(recipe1);
            System.out.println("Saved: " + recipe1.getName() + "\n");

            // Save another recipe
            System.out.println("Saving another recipe...");
            Recipe recipe2 = new Recipe(
                    "Caesar Salad",
                    Arrays.asList("Romaine lettuce", "Caesar dressing", "Parmesan", "Croutons"),
                    String.valueOf(Arrays.asList("Wash lettuce", "Add dressing", "Top with parmesan and croutons")),
                    2,
                    null,
                    10,
                    null,
                    "recipe-002"
            );
            repo.save(recipe2);
            System.out.println("Saved: " + recipe2.getName() + "\n");

            // Count recipes
            System.out.println("Total recipes: " + repo.count() + "\n");

            // Find by ID
            System.out.println("Finding recipe by ID 'recipe-001'...");
            Optional<Recipe> found = repo.findById("recipe-001");
            if (found.isPresent()) {
                Recipe r = found.get();
                System.out.println("Found: " + r.getName());
                System.out.println("Ingredients: " + r.getIngredients().size());
                System.out.println("Steps: " + r.getSteps().size() + "\n");
            }

            // Find all recipes
            System.out.println("Finding all recipes...");
            List<Recipe> allRecipes = repo.findAll();
            System.out.println("Found " + allRecipes.size() + " recipes:");
            for (Recipe r : allRecipes) {
                System.out.println("  - " + r.getName() + " (ID: " + r.getRecipeId() + ")");
            }
            System.out.println();

            // Find by name
            System.out.println("Searching for recipes containing 'salad'...");
            List<Recipe> salads = repo.findByName("salad");
            System.out.println("Found " + salads.size() + " matching recipes:");
            for (Recipe r : salads) {
                System.out.println("  - " + r.getName());
            }
            System.out.println();

            // Check existence
            System.out.println("Checking if recipe-001 exists: " + repo.exists("recipe-001"));
            System.out.println("Checking if recipe-999 exists: " + repo.exists("recipe-999") + "\n");

            // Delete a recipe
            System.out.println("Deleting recipe-002...");
            boolean deleted = repo.delete("recipe-002");
            System.out.println("Deletion successful: " + deleted);
            System.out.println("New count: " + repo.count() + "\n");

            // Verify deletion
            System.out.println("Verifying deletion...");
            Optional<Recipe> deletedRecipe = repo.findById("recipe-002");
            System.out.println("Recipe-002 found: " + deletedRecipe.isPresent() + "\n");

            System.out.println("=== Demo completed successfully! ===");

        } catch (DataAccessException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
