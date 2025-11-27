package com.mealplanner.interface_adapter.view_model;

// ViewModel for viewing user's meal schedule - holds data for ScheduleView.
// Responsible: Mona, Everyone (GUI)
// done: Implement fields for user's weekly meal plan, daily nutrition totals, and property change notifications

import com.mealplanner.entity.Schedule;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ScheduleViewModel {

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private String username;
    private Schedule schedule;
    private String error;

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public Schedule getSchedule() {
        return schedule;
    }
    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
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
        propertyChangeSupport.firePropertyChange("schedule", null, null);
    }

}
