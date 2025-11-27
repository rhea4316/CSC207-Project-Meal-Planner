package com.mealplanner.use_case.browse_recipe;

// Input boundary interface for browsing recipe details and viewing ingredients.
// Responsible: Regina

import java.io.IOException;

public interface BrowseRecipeInputBoundary {

    void execute(BrowseRecipeInputData browseRecipeInputData) throws IOException;

}
