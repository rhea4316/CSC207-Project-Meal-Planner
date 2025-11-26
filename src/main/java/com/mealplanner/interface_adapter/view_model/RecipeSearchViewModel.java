package com.mealplanner.interface_adapter.view_model;

// ViewModel for recipe search results - holds data for SearchByIngredientsView.
// Responsible: Jerry, Everyone (GUI)

import com.mealplanner.entity.Recipe;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

public class RecipeSearchViewModel {
    public static final String PROP_RECIPES = "recipes";
    public static final String PROP_ERROR_MESSAGE = "errorMessage";
    public static final String PROP_LOADING = "isLoading";

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private List<Recipe> recipes;
    private String errorMessage;
    private boolean isLoading;

    public RecipeSearchViewModel() {
        this.recipes = new ArrayList<>();
        this.errorMessage = "";
        this.isLoading = false;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public List<Recipe> getRecipes() {
        return new ArrayList<>(recipes);
    }

    public void setRecipes(List<Recipe> recipes) {
        List<Recipe> oldRecipes = new ArrayList<>(this.recipes);
        if (recipes != null) {
            this.recipes = new ArrayList<>(recipes);
        } else {
            this.recipes = new ArrayList<>();
        }
        propertyChangeSupport.firePropertyChange(PROP_RECIPES, oldRecipes, new ArrayList<>(this.recipes));
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        String oldErrorMessage = this.errorMessage;
        this.errorMessage = errorMessage != null ? errorMessage : "";
        propertyChangeSupport.firePropertyChange(PROP_ERROR_MESSAGE, oldErrorMessage, this.errorMessage);
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean isLoading) {
        boolean oldLoading = this.isLoading;
        this.isLoading = isLoading;
        propertyChangeSupport.firePropertyChange(PROP_LOADING, oldLoading, this.isLoading);
    }
}
