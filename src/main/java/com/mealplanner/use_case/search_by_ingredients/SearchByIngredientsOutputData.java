package com.mealplanner.use_case.search_by_ingredients;

// Data transfer object carrying output data with search results.
// Responsible: Jerry

import com.mealplanner.entity.Recipe;

import java.util.ArrayList;
import java.util.List;

public class SearchByIngredientsOutputData {
    private final List<Recipe> recipes;
    
    public SearchByIngredientsOutputData(List<Recipe> recipes) {
        this.recipes = recipes != null ? new ArrayList<>(recipes) : new ArrayList<>();
    }
    
    public List<Recipe> getRecipes() {
        return new ArrayList<>(recipes);
    }
    
    public boolean isEmpty() {
        return recipes.isEmpty();
    }
    
    public int getCount() {
        return recipes.size();
    }
}
