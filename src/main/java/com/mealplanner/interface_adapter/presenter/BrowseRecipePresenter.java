package com.mealplanner.interface_adapter.presenter;

// Presenter for recipe details - converts OutputData to ViewModel and updates view.
// Responsible: Regina
import java.util.Objects;

import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.view_model.RecipeBrowseViewModel;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeOutputBoundary;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeOutputData;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Presenter for the BrowseRecipe use case.
 */
public class BrowseRecipePresenter implements BrowseRecipeOutputBoundary {
    private static final Logger logger = LoggerFactory.getLogger(BrowseRecipePresenter.class);
    private final RecipeBrowseViewModel browseRecipeViewModel;
    private final ViewManagerModel viewManager;

    public BrowseRecipePresenter(RecipeBrowseViewModel browseRecipeViewModel, ViewManagerModel viewManager) {
        this.browseRecipeViewModel = Objects.requireNonNull(browseRecipeViewModel, 
                "ViewModel cannot be null");
        this.viewManager = Objects.requireNonNull(viewManager, "ViewManager cannot be null");
    }

    @Override
    public void presentRecipeDetails(BrowseRecipeOutputData browseRecipeOutputData) {
        logger.debug("presentRecipeDetails called with {} recipes", 
            browseRecipeOutputData != null && browseRecipeOutputData.getRecipes() != null 
                ? browseRecipeOutputData.getRecipes().size() : 0);
        
        // Runnable to update ViewModel
        Runnable updateViewModel = () -> {
            if (browseRecipeOutputData == null || browseRecipeOutputData.getRecipes() == null) {
                logger.warn("presentRecipeDetails: OutputData or recipes is null");
                browseRecipeViewModel.setErrorMessage("No recipe data available");
                browseRecipeViewModel.setRecipes(java.util.Collections.emptyList());
                // Don't change view on error - stay on current view
                return;
            }
            
            int recipeCount = browseRecipeOutputData.getRecipes().size();
            logger.debug("Setting {} recipes to ViewModel", recipeCount);
            browseRecipeViewModel.setRecipes(browseRecipeOutputData.getRecipes());
            logger.debug("Recipes set to ViewModel, PropertyChange event should be fired");
            
            // Only switch view if we're not already on BrowseRecipeView
            if (viewManager != null && !"BrowseRecipeView".equals(viewManager.getActiveView())) {
                viewManager.setActiveView("BrowseRecipeView");
            }
        };
        
        // Ensure ViewModel updates happen on JavaFX Application Thread (if available)
        if (Platform.isFxApplicationThread()) {
            updateViewModel.run();
        } else {
            try {
                Platform.runLater(updateViewModel);
            } catch (IllegalStateException e) {
                // JavaFX Toolkit not initialized (e.g., in tests) - run directly
                logger.debug("JavaFX Toolkit not initialized, running directly");
                updateViewModel.run();
            }
        }
    }

    @Override
    public void presentError(String errorMessage) {
        logger.debug("presentError called with message: {}", errorMessage);
        
        // Runnable to update ViewModel
        Runnable updateViewModel = () -> {
            browseRecipeViewModel.setErrorMessage(errorMessage != null ? errorMessage : "An error occurred");
            browseRecipeViewModel.setRecipes(java.util.Collections.emptyList());
            // Don't change view on error - stay on current view to show error message
        };
        
        // Ensure ViewModel updates happen on JavaFX Application Thread (if available)
        if (Platform.isFxApplicationThread()) {
            updateViewModel.run();
        } else {
            try {
                Platform.runLater(updateViewModel);
            } catch (IllegalStateException e) {
                // JavaFX Toolkit not initialized (e.g., in tests) - run directly
                logger.debug("JavaFX Toolkit not initialized, running directly");
                updateViewModel.run();
            }
        }
    }
}
