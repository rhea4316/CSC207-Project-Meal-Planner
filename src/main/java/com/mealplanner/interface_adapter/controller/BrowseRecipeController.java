package com.mealplanner.interface_adapter.controller;

// Controller for browsing recipe details - receives recipe selection and calls interactor.
// Responsible: Regina
// TODO: Implement execute method that converts recipe ID from UI to InputData and calls interactor

import com.mealplanner.use_case.browse_recipe.BrowseRecipeInputBoundary;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeInputData;

import java.io.IOException;

public class BrowseRecipeController {
    private final BrowseRecipeInputBoundary browseRecipeInteractor;

    public BrowseRecipeController(BrowseRecipeInputBoundary browseRecipeInteractor) {
        this.browseRecipeInteractor = browseRecipeInteractor;
    }

    public void execute(String query, int numberOfRecipes, String ingredients) throws IOException {
        BrowseRecipeInputData browseRecipeInputData = new BrowseRecipeInputData(query, numberOfRecipes, ingredients);
        browseRecipeInteractor.execute(browseRecipeInputData);
    }

    public void execute(String query, int numberOfRecipes) throws IOException {
        BrowseRecipeInputData browseRecipeInputData = new BrowseRecipeInputData(query, numberOfRecipes);
        browseRecipeInteractor.execute(browseRecipeInputData);
    }

}
