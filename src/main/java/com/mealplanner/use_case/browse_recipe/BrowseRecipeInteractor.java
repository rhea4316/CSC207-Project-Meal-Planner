package com.mealplanner.use_case.browse_recipe;

// Main business logic for browsing recipe details and viewing ingredients.
// Responsible: Regina
// TODO: Implement execute method: retrieve recipe details, extract ingredient list, pass to presenter

public class BrowseRecipeInteractor implements BrowseRecipeInputBoundary {
    // get the recipe specifications from DataAccessInterface
    // instantiate the OutputData
    private final BrowseRecipeDataAccessInterface browseRecipeDataAccessObject;
    private final BrowseRecipeOutputData browseRecipeOutputData;

    public BrowseRecipeInteractor(BrowseRecipeDataAccessInterface browseRecipeDataAccessObject,
                                  BrowseRecipeOutputData browseRecipeOutputData) {
        this.browseRecipeDataAccessObject = browseRecipeDataAccessObject;
        this.browseRecipeOutputData = browseRecipeOutputData;
    }

    public void execute(BrowseRecipeInputData browseRecipeInputData) {

    }
}
