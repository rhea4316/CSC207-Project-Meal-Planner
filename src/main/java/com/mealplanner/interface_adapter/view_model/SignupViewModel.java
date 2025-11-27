package com.mealplanner.interface_adapter.view_model;

// ViewModel for signup screen - holds data for SignupView.
// Responsible: Everyone

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class SignupViewModel {
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private String registeredUser;
    private String error;

    public String getRegisteredUser() {
        return registeredUser;
    }

    public void setRegisteredUser(String registeredUser) {
        this.registeredUser = registeredUser;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void firePropertyChanged() {
        propertyChangeSupport.firePropertyChange("signup", null, null);
    }
}

