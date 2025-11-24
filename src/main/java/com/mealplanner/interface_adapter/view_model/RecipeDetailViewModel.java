package com.mealplanner.interface_adapter.view_model;

import com.mealplanner.entity.NutritionInfo;
import com.mealplanner.entity.Recipe;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

// ViewModel for detailed recipe view with adjustable servings - holds data for RecipeDetailView.
// Responsible: Eden, Everyone (GUI)

public class RecipeDetailViewModel {
    public static final String PROP_RECIPE = "recipe";
    public static final String PROP_SERVING_SIZE = "servingSize";
    public static final String PROP_INGREDIENTS = "ingredients";
    public static final String PROP_NUTRITION = "nutrition";
    public static final String PROP_ERROR_MESSAGE = "errorMessage";

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private Recipe recipe;
    private int servingSize;
    private List<String> ingredients;
    private NutritionInfo nutrition;
    private String errorMessage;

    public RecipeDetailViewModel() {
        this.servingSize = 1;
        this.ingredients = new ArrayList<>();
        this.errorMessage = "";
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        Recipe oldRecipe = this.recipe;
        this.recipe = recipe;
        if (recipe != null) {
            this.servingSize = recipe.getServingSize();
            this.ingredients = new ArrayList<>(recipe.getIngredients());
            this.nutrition = recipe.getNutritionInfo();
        } else {
            this.servingSize = 1;
            this.ingredients = new ArrayList<>();
            this.nutrition = null;
        }
        propertyChangeSupport.firePropertyChange(PROP_RECIPE, oldRecipe, recipe);
        propertyChangeSupport.firePropertyChange(PROP_SERVING_SIZE, null, servingSize);
        propertyChangeSupport.firePropertyChange(PROP_INGREDIENTS, null, new ArrayList<>(ingredients));
        propertyChangeSupport.firePropertyChange(PROP_NUTRITION, null, nutrition);
    }

    public int getServingSize() {
        return servingSize;
    }

    public void setServingSize(int servingSize) {
        int oldServingSize = this.servingSize;
        this.servingSize = servingSize;
        propertyChangeSupport.firePropertyChange(PROP_SERVING_SIZE, oldServingSize, servingSize);
    }

    public List<String> getIngredients() {
        return new ArrayList<>(ingredients);
    }

    public void setIngredients(List<String> ingredients) {
        List<String> oldIngredients = new ArrayList<>(this.ingredients);
        this.ingredients = ingredients != null ? new ArrayList<>(ingredients) : new ArrayList<>();
        propertyChangeSupport.firePropertyChange(PROP_INGREDIENTS, oldIngredients, new ArrayList<>(this.ingredients));
    }

    public NutritionInfo getNutrition() {
        return nutrition;
    }

    public void setNutrition(NutritionInfo nutrition) {
        NutritionInfo oldNutrition = this.nutrition;
        this.nutrition = nutrition;
        propertyChangeSupport.firePropertyChange(PROP_NUTRITION, oldNutrition, nutrition);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        String oldErrorMessage = this.errorMessage;
        this.errorMessage = errorMessage != null ? errorMessage : "";
        propertyChangeSupport.firePropertyChange(PROP_ERROR_MESSAGE, oldErrorMessage, this.errorMessage);
    }
}
