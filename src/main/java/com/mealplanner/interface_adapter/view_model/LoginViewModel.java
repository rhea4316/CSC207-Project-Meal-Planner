package com.mealplanner.interface_adapter.view_model;

// ViewModel for login screen - holds data for LoginView.
// Responsible: Mona, Everyone (GUI)
// done: Implement fields for login state, error messages, and property change support for login success/failure

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class LoginViewModel {
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private String loggedInUser;
    private String error;

    public String getLoggedInUser(){
        return loggedInUser;
    }

    public void setLoggedInUser(String loggedInUser){
        this.loggedInUser = loggedInUser;
    }
    public String getError(){
        return error;
    }
    public void setError(String error){
        this.error = error;
    }
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void firePropertyChanged() {
        propertyChangeSupport.firePropertyChange("login", null, null);
    }
}
