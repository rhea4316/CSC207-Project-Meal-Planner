package com.mealplanner.use_case.adjust_serving_size;

import com.mealplanner.entity.Recipe;
import com.mealplanner.exception.RecipeNotFoundException;
import java.util.Objects;

// Main business logic for adjusting recipe serving sizes with ingredient scaling.
// Responsible: Eden

public class AdjustServingSizeInteractor implements AdjustServingSizeInputBoundary {
    private final AdjustServingSizeDataAccessInterface dataAccess;
    private final AdjustServingSizeOutputBoundary presenter;

    public AdjustServingSizeInteractor(AdjustServingSizeDataAccessInterface dataAccess,
                                      AdjustServingSizeOutputBoundary presenter) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "Data access cannot be null");
        this.presenter = Objects.requireNonNull(presenter, "Presenter cannot be null");
    }

    @Override
    public void execute(AdjustServingSizeInputData inputData) {
        if (inputData == null) {
            presenter.presentError("Input data cannot be null");
            return;
        }
        
        String recipeId = inputData.getRecipeId();
        int newServingSize = inputData.getNewServingSize();
        
        // Validate recipe ID
        if (recipeId == null || recipeId.trim().isEmpty()) {
            presenter.presentError("Recipe ID cannot be empty");
            return;
        }
        
        // Validate serving size
        if (newServingSize <= 0) {
            presenter.presentError("Serving size must be greater than zero");
            return;
        }

        try {
            // Retrieve recipe
            Recipe recipe = dataAccess.getRecipeById(recipeId);
            
            // Adjust serving size using Recipe's built-in method
            Recipe adjustedRecipe = recipe.adjustServingSize(newServingSize);
            
            // Create output data and present success
            AdjustServingSizeOutputData outputData = new AdjustServingSizeOutputData(adjustedRecipe);
            presenter.presentAdjustedRecipe(outputData);
            
        } catch (RecipeNotFoundException e) {
            presenter.presentError("Recipe not found: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            presenter.presentError("Invalid serving size: " + e.getMessage());
        } catch (Exception e) {
            presenter.presentError("An error occurred while adjusting serving size: " + e.getMessage());
        }
    }
}
