package com.mealplanner.use_case.browse_recipe;

// Data transfer object carrying the recipe ID to browse.
// Responsible: Regina

/**
 * The input data for the BrowseRecipe use case.
 */
public class BrowseRecipeInputData {
    private final String query;
    // natural-word query for searching recipes, e.g. "pasta"
    private final String includedIngredients;
    // must include the listed ingredients, separated by a comma
    private final int numberOfRecipes;
    // the number of search results that the user wants

    public BrowseRecipeInputData(String query, int numberOfRecipes) {
        this.query = query;
        this.numberOfRecipes = numberOfRecipes;
        this.includedIngredients = null;
    }

    public BrowseRecipeInputData(String query, int numberOfRecipes, String includedIngredients) {
        this.query = query;
        this.numberOfRecipes = numberOfRecipes;
        this.includedIngredients = includedIngredients;
    }

    // Getters:
    public String getQuery() {
        return query;
    }

    public String getIncludedIngredients() {
        return includedIngredients;
    }

    public String getNumberOfRecipes() {
        return String.valueOf(numberOfRecipes);
    }

    public int getNumberOfRecipesInt() {
        return numberOfRecipes;
    }

}

