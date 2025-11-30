package com.mealplanner.interface_adapter.view_model;

import com.mealplanner.entity.NutritionGoals;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * ViewModel for ProfileSettingsView.
 * Holds the state for profile settings including nutrition goals.
 * 
 * Responsible: Interface Adapter team
 */
public class ProfileSettingsViewModel {
    
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    
    private NutritionGoals nutritionGoals;
    private String error;
    
    public NutritionGoals getNutritionGoals() {
        return nutritionGoals;
    }
    
    public void setNutritionGoals(NutritionGoals goals) {
        NutritionGoals old = this.nutritionGoals;
        this.nutritionGoals = goals;
        firePropertyChanged("nutritionGoals", old, goals);
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        String old = this.error;
        this.error = error;
        firePropertyChanged("error", old, error);
    }
    
    public void fireNutritionGoalsUpdated() {
        firePropertyChanged("nutritionGoalsUpdated", null, nutritionGoals);
    }
    
    private void firePropertyChanged(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
}

