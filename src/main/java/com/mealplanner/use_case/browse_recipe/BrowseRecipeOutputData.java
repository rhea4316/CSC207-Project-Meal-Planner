package com.mealplanner.use_case.browse_recipe;

// Data transfer object carrying recipe details including ingredient list.
// Responsible: Regina
import java.util.List;

import com.mealplanner.entity.Recipe;

public class BrowseRecipeOutputData {
    // should output a list of relevant recipes based on the user's specifications
    private final List<Recipe> recipes;

    public BrowseRecipeOutputData(List<Recipe> recipes) {
        this.recipes = recipes;
    }

    // Getter:
    public List<Recipe> getRecipes() {
        return recipes;
    }
}
