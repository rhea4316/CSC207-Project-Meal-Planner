package com.mealplanner.interface_adapter.controller;

// Controller for browsing recipe details - receives recipe selection and calls interactor.
// Responsible: Regina

import com.mealplanner.use_case.browse_recipe.BrowseRecipeInputBoundary;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeInputData;

import java.io.IOException;
import java.util.Objects;

public class BrowseRecipeController {
    private final BrowseRecipeInputBoundary browseRecipeInteractor;

    public BrowseRecipeController(BrowseRecipeInputBoundary browseRecipeInteractor) {
        this.browseRecipeInteractor = Objects.requireNonNull(browseRecipeInteractor, 
                "Interactor cannot be null");
    }

    public void execute(String query, int numberOfRecipes, String ingredients) throws IOException {
        if (query == null || query.trim().isEmpty()) {
            return; // Let interactor handle validation
        }
        BrowseRecipeInputData browseRecipeInputData = new BrowseRecipeInputData(query, numberOfRecipes, ingredients);
        browseRecipeInteractor.execute(browseRecipeInputData);
    }

    public void execute(String query, int numberOfRecipes) throws IOException {
        if (query == null || query.trim().isEmpty()) {
            return; // Let interactor handle validation
        }
        BrowseRecipeInputData browseRecipeInputData = new BrowseRecipeInputData(query, numberOfRecipes);
        browseRecipeInteractor.execute(browseRecipeInputData);
    }
}
