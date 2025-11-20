package com.mealplanner.interface_adapter.presenter;

// Presenter for recipe details - converts OutputData to ViewModel and updates view.
// Responsible: Regina
// TODO: Implement OutputBoundary methods to format recipe details and ingredient list for RecipeBrowseViewModel

import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.view_model.RecipeBrowseViewModel;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeOutputBoundary;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeOutputData;

public class BrowseRecipePresenter implements BrowseRecipeOutputBoundary {
    private final RecipeBrowseViewModel browseRecipeViewModel;
    private final ViewManagerModel viewManager;

    public BrowseRecipePresenter(RecipeBrowseViewModel browseRecipeViewModel, ViewManagerModel viewManager) {
        this.browseRecipeViewModel = browseRecipeViewModel;
        this.viewManager = viewManager;
    }

    @Override
    public void presentRecipeDetails(BrowseRecipeOutputData browseRecipeOutputData) {
        browseRecipeViewModel.setRecipes(browseRecipeOutputData.getRecipes());
//        viewManager.setActiveView
    }

    @Override
    public void presentError(String errorMessage) {
        browseRecipeViewModel.setErrorMessage(errorMessage);
    }
}
