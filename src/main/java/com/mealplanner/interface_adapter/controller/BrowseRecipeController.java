package com.mealplanner.interface_adapter.controller;

// Controller for browsing recipe details - receives recipe selection and calls interactor.
// Responsible: Regina
import java.io.IOException;
import java.util.Objects;

import com.mealplanner.use_case.browse_recipe.BrowseRecipeInputBoundary;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeInputData;
import com.mealplanner.util.StringUtil;

/**
 * The controller class for the BrowseRecipe use case.
 */
public class BrowseRecipeController {
    private final BrowseRecipeInputBoundary browseRecipeInteractor;

    public BrowseRecipeController(BrowseRecipeInputBoundary browseRecipeInteractor) {
        this.browseRecipeInteractor = Objects.requireNonNull(browseRecipeInteractor, 
                "Interactor cannot be null");
    }

    /**
     * Executes the BrowseRecipe use case.
     * @param query natural-word search query
     * @param numberOfRecipes number of wanted search results
     * @param ingredients optional included ingredients
     * @throws IOException if API call fails
     */
    public void execute(String query, int numberOfRecipes, String ingredients) throws IOException {
        if (StringUtil.isNullOrEmpty(query)) {
            return;
        }
        final BrowseRecipeInputData browseRecipeInputData = new BrowseRecipeInputData(query, numberOfRecipes,
                ingredients);
        browseRecipeInteractor.execute(browseRecipeInputData);
    }

    /**
     * Executes the BrowseRecipe use case.
     * @param query natural-word search query
     * @param numberOfRecipes number of wanted search results
     * @throws IOException if API call fails
     */
    public void execute(String query, int numberOfRecipes) throws IOException {
        if (StringUtil.isNullOrEmpty(query)) {
            return;
        }
        final BrowseRecipeInputData browseRecipeInputData = new BrowseRecipeInputData(query, numberOfRecipes);
        browseRecipeInteractor.execute(browseRecipeInputData);
    }
}
