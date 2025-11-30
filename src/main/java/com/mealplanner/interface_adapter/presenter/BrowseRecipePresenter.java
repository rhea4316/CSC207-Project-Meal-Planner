package com.mealplanner.interface_adapter.presenter;

// Presenter for recipe details - converts OutputData to ViewModel and updates view.
// Responsible: Regina
import java.util.Objects;

import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.view_model.RecipeBrowseViewModel;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeOutputBoundary;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeOutputData;

/**
 * The Presenter for the BrowseRecipe use case.
 */
public class BrowseRecipePresenter implements BrowseRecipeOutputBoundary {
    private final RecipeBrowseViewModel browseRecipeViewModel;
    private final ViewManagerModel viewManager;

    public BrowseRecipePresenter(RecipeBrowseViewModel browseRecipeViewModel, ViewManagerModel viewManager) {
        this.browseRecipeViewModel = Objects.requireNonNull(browseRecipeViewModel, 
                "ViewModel cannot be null");
        this.viewManager = Objects.requireNonNull(viewManager, "ViewManager cannot be null");
    }

    @Override
    public void presentRecipeDetails(BrowseRecipeOutputData browseRecipeOutputData) {
        if (browseRecipeOutputData == null || browseRecipeOutputData.getRecipes() == null) {
            browseRecipeViewModel.setErrorMessage("No recipe data available");
            return;
        }
        browseRecipeViewModel.setRecipes(browseRecipeOutputData.getRecipes());
        if (viewManager != null) {
            viewManager.setActiveView("BrowseRecipeView");
        }
    }

    @Override
    public void presentError(String errorMessage) {
        browseRecipeViewModel.setErrorMessage(errorMessage != null ? errorMessage : "An error occurred");
    }
}
