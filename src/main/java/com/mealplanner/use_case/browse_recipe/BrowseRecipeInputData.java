package com.mealplanner.use_case.browse_recipe;

// Data transfer object carrying the recipe ID to browse.
// Responsible: Regina
// TODO: Implement with recipe ID or recipe selection criteria from user

import com.mealplanner.entity.Recipe;

public class BrowseRecipeInputData {
    private final String recipeName;
    private final String recipeID;
    //or is recipeID a recipe? idk

    public BrowseRecipeInputData(String name) {
        this.recipeName = name;
        this.recipeID = null;
    }

    public BrowseRecipeInputData(Recipe recipe) {
        this.recipeName = null;
        this.recipeID = recipe.getName();
    }

    //Getters:
    String getRecipeID() {return recipeID;}
    String getRecipeName() {return recipeName;}

}

