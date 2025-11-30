package com.mealplanner.interface_adapter;

// Manages which view is currently active and handles view navigation.
// Responsible: Everyone (GUI)

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ViewManagerModel {
    private String activeView;
    private String previousView;
    private String currentUserId;
    private String currentUsername;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public String getActiveView() {
        return activeView;
    }

    public void setActiveView(String activeView) {
        String oldView = this.activeView;
        String oldPrevious = this.previousView;
        this.previousView = oldView;
        this.activeView = activeView;
        support.firePropertyChange("previousView", oldPrevious, this.previousView);
        support.firePropertyChange("view", oldView, activeView);
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        String oldUserId = this.currentUserId;
        this.currentUserId = currentUserId;
        support.firePropertyChange("currentUserId", oldUserId, currentUserId);
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public void setCurrentUsername(String currentUsername) {
        String oldUsername = this.currentUsername;
        this.currentUsername = currentUsername;
        support.firePropertyChange("currentUsername", oldUsername, currentUsername);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public String getPreviousView() {
        return previousView;
    }
}
