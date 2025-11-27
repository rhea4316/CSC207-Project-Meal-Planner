package com.mealplanner.use_case.search_by_ingredients;

// Data transfer object carrying input data for searching recipes by ingredients.
// Responsible: Jerry

import java.util.ArrayList;
import java.util.List;

public class SearchByIngredientsInputData {
    private final List<String> ingredients;
    
    public SearchByIngredientsInputData(List<String> ingredients) {
        if (ingredients == null) {
            throw new IllegalArgumentException("Ingredients list cannot be null");
        }
        this.ingredients = new ArrayList<>(ingredients);
    }
    
    public List<String> getIngredients() {
        return new ArrayList<>(ingredients);
    }
    
    public boolean isEmpty() {
        return ingredients.isEmpty();
    }
}
