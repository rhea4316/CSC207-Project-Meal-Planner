package com.mealplanner.use_case.browse_recipe;

// Input boundary interface for browsing recipe details and viewing ingredients.
// Responsible: Regina
// TODO: Define execute method that takes BrowseRecipeInputData (containing recipe ID) as parameter

import java.io.IOException;

public interface BrowseRecipeInputBoundary {

    void execute(BrowseRecipeInputData browseRecipeInputData) throws IOException;

}
