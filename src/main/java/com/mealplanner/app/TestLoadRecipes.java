package com.mealplanner.app;

import java.util.List;
import java.util.Optional;

import com.mealplanner.entity.Recipe;
import com.mealplanner.repository.impl.FileRecipeRepository;

/**
 * Quick test to verify loading recipes from files works.
 */
public class TestLoadRecipes {

    public static void main(String[] args) {
        try {
            FileRecipeRepository repo = new FileRecipeRepository();

            System.out.println("=== Testing Recipe Loading ===\n");

            // Count recipes
            int count = repo.count();
            System.out.println("Total recipes found: " + count + "\n");

            // Load all recipes
            System.out.println("Loading all recipes:");
            List<Recipe> allRecipes = repo.findAll();
            for (Recipe recipe : allRecipes) {
                System.out.println("  - " + recipe.getName() + " (ID: " + recipe.getRecipeId() + ")");
                System.out.println("    Ingredients: " + recipe.getIngredients());
                System.out.println("    Steps: " + recipe.getSteps());
                System.out.println("    Serves: " + recipe.getServingSize());
                System.out.println();
            }

            // Test findById if we have recipes
            if (!allRecipes.isEmpty()) {
                String firstId = allRecipes.get(0).getRecipeId();
                System.out.println("Testing findById with ID: " + firstId);
                Optional<Recipe> found = repo.findById(firstId);
                if (found.isPresent()) {
                    System.out.println("✓ Successfully loaded recipe: " + found.get().getName());
                } else {
                    System.out.println("✗ Failed to load recipe by ID");
                }
                System.out.println();
            }

            // Test findByName
            System.out.println("Testing search by name 'ri':");
            List<Recipe> searchResults = repo.findByName("ri");
            System.out.println("Found " + searchResults.size() + " matching recipes:");
            for (Recipe recipe : searchResults) {
                System.out.println("  - " + recipe.getName());
            }

            System.out.println("\n=== All tests completed! ===");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
