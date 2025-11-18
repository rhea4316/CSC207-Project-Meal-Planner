package com.mealplanner.use_case.browse_recipe;

// Data transfer object carrying recipe details including ingredient list.
// Responsible: Regina
// TODO: Implement with Recipe object, ingredient list with quantities, and preparation details

import com.mealplanner.entity.Recipe;
import java.util.List;

public class BrowseRecipeOutputData {
    // should output a list of relevant recipes based on the user's specifications
    private final List<Recipe> recipes;

    public BrowseRecipeOutputData(List<Recipe> recipes) {
        this.recipes = recipes;
    }

    //Getter:
    public List<Recipe> getRecipes() {return recipes;}
}
