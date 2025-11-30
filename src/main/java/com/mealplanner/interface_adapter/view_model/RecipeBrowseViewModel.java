package com.mealplanner.interface_adapter.view_model;

// ViewModel for browsing recipe details - holds data for BrowseRecipeView.
// Responsible: Regina, Everyone (GUI)

import com.mealplanner.entity.Recipe;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

public class RecipeBrowseViewModel {
    private List<Recipe> recipes;
    private List<Recipe> recommendations;
    private String errorMessage;
    private boolean displayRecipes;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public RecipeBrowseViewModel() {
        this.recipes = new ArrayList<>();
        this.recommendations = new ArrayList<>();
        this.errorMessage = "";
        this.displayRecipes = false;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void setRecipes(List<Recipe> recipes) {
        List<Recipe> oldRecipes = this.recipes;
        boolean oldDisplayRecipes = this.displayRecipes;

        if (recipes != null) {
            this.recipes = new ArrayList<> (recipes);
        } else {
            this.recipes = new ArrayList<>();
        }

        this.displayRecipes = true;
        this.propertyChangeSupport.firePropertyChange("recipes", oldRecipes, this.recipes);
        this.propertyChangeSupport.firePropertyChange("displayRecipes", oldDisplayRecipes, this.displayRecipes);
    }

    public void setErrorMessage(String errorMessage) {
        String oldErrorMessage = this.errorMessage;
        boolean oldDisplayRecipes = this.displayRecipes;

        this.errorMessage = errorMessage != null ? errorMessage : "";

        this.displayRecipes = false;
        this.propertyChangeSupport.firePropertyChange("errorMessage", oldErrorMessage, this.errorMessage);
        this.propertyChangeSupport.firePropertyChange("displayRecipes", oldDisplayRecipes, this.displayRecipes);
    }

    public void setRecommendations(List<Recipe> recommendations) {
        List<Recipe> oldRecommendations = this.recommendations;
        this.recommendations = recommendations != null 
            ? new ArrayList<>(recommendations) 
            : new ArrayList<>();
        this.propertyChangeSupport.firePropertyChange("recommendations", 
            oldRecommendations, this.recommendations);
    }

    //Getters:
    public List<Recipe> getRecipes() {return this.recipes;}

    public List<Recipe> getRecommendations() {
        return new ArrayList<>(recommendations);
    }

    public String getErrorMessage() {return this.errorMessage;}

    public boolean isDisplayRecipes() {return this.displayRecipes;}

}
