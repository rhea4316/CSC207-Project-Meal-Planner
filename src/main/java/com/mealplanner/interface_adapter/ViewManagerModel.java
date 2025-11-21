package com.mealplanner.interface_adapter;

// Manages which view is currently active and handles view navigation.
// Responsible: Everyone (GUI)
// TODO: Implement view state management with property change support to notify ViewManager of view switches

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ViewManagerModel {
    private String activeView;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public String getActiveView() {
        return activeView;
    }

    public void setActiveView(String activeView) {
        String oldView = this.activeView;
        this.activeView = activeView;
        support.firePropertyChange("view", oldView, activeView);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
}
