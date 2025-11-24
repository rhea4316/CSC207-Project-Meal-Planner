package com.mealplanner.use_case.search_by_ingredients;

// Main business logic for searching recipes by ingredients use case.
// Responsible: Jerry

import java.io.IOException;
import java.util.Objects;

public class SearchByIngredientsInteractor implements SearchByIngredientsInputBoundary {
    
    private final SearchByIngredientsDataAccessInterface dataAccess;
    private final SearchByIngredientsOutputBoundary presenter;
    
    public SearchByIngredientsInteractor(SearchByIngredientsDataAccessInterface dataAccess,
                                        SearchByIngredientsOutputBoundary presenter) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "Data access cannot be null");
        this.presenter = Objects.requireNonNull(presenter, "Presenter cannot be null");
    }
    
    @Override
    public void execute(SearchByIngredientsInputData inputData) {
        if (inputData == null) {
            presenter.presentError("Input data cannot be null");
            return;
        }
        
        if (inputData.isEmpty()) {
            presenter.presentError("Please provide at least one ingredient");
            return;
        }
        
        try {
            var recipes = dataAccess.searchByIngredients(inputData.getIngredients());
            
            if (recipes == null || recipes.isEmpty()) {
                presenter.presentError("No recipes found matching the provided ingredients");
            } else {
                SearchByIngredientsOutputData outputData = new SearchByIngredientsOutputData(recipes);
                presenter.presentRecipes(outputData);
            }
        } catch (IOException e) {
            presenter.presentError("Network error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            presenter.presentError("Invalid input: " + e.getMessage());
        } catch (Exception e) {
            presenter.presentError("An error occurred: " + e.getMessage());
        }
    }
}
