package com.mealplanner.interface_adapter.presenter;

// Presenter for search results - converts OutputData to ViewModel and updates view.
// Responsible: Jerry

import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.view_model.RecipeSearchViewModel;
import com.mealplanner.use_case.search_by_ingredients.SearchByIngredientsOutputBoundary;
import com.mealplanner.use_case.search_by_ingredients.SearchByIngredientsOutputData;
import java.util.Objects;

public class SearchByIngredientsPresenter implements SearchByIngredientsOutputBoundary {
    private final RecipeSearchViewModel viewModel;
    private final ViewManagerModel viewManagerModel;

    public SearchByIngredientsPresenter(RecipeSearchViewModel viewModel, ViewManagerModel viewManagerModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "ViewModel cannot be null");
        this.viewManagerModel = Objects.requireNonNull(viewManagerModel, "ViewManagerModel cannot be null");
    }

    @Override
    public void presentRecipes(SearchByIngredientsOutputData outputData) {
        if (viewModel == null) {
            return;
        }

        if (outputData == null || outputData.getRecipes() == null || outputData.isEmpty()) {
            viewModel.setErrorMessage("No recipes found matching the provided ingredients");
            viewModel.setLoading(false);
            return;
        }

        viewModel.setRecipes(outputData.getRecipes());
        viewModel.setErrorMessage("");
        viewModel.setLoading(false);
        
        // Switch to SearchByIngredientsView to show results
        if (viewManagerModel != null) {
            viewManagerModel.setActiveView("SearchByIngredientsView");
        }
    }

    @Override
    public void presentError(String errorMessage) {
        if (viewModel != null) {
            viewModel.setErrorMessage(errorMessage != null ? errorMessage : "An error occurred");
            viewModel.setLoading(false);
        }
    }
}
