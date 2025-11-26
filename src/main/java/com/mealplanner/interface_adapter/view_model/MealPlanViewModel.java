package com.mealplanner.interface_adapter.view_model;

import com.mealplanner.entity.MealType;
import com.mealplanner.entity.Schedule;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class MealPlanViewModel {
    private Schedule schedule;
    private LocalDate selectedDate;
    private MealType selectedMealType;
    private String errorMessage;
    private String successMessage;
    private Map<LocalDate, Map<MealType, String>> weeklyMeals;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public MealPlanViewModel() {
        this.schedule = null;
        this.selectedDate = LocalDate.now();
        this.selectedMealType = MealType.BREAKFAST;
        this.errorMessage = "";
        this.successMessage = "";
        this.weeklyMeals = new HashMap<>();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void setSchedule(Schedule schedule) {
        Schedule oldSchedule = this.schedule;
        this.schedule = schedule;

        if (schedule != null) {
            this.weeklyMeals = new HashMap<>(schedule.getAllMeals());
        } else {
            this.weeklyMeals = new HashMap<>();
        }

        this.propertyChangeSupport.firePropertyChange("schedule", oldSchedule, this.schedule);
        this.propertyChangeSupport.firePropertyChange("weeklyMeals", null, this.weeklyMeals);
    }

    public void setSelectedDate(LocalDate date) {
        LocalDate oldDate = this.selectedDate;
        this.selectedDate = date;
        this.propertyChangeSupport.firePropertyChange("selectedDate", oldDate, this.selectedDate);
    }

    public void setSelectedMealType(MealType mealType) {
        MealType oldMealType = this.selectedMealType;
        this.selectedMealType = mealType;
        this.propertyChangeSupport.firePropertyChange("selectedMealType", oldMealType, this.selectedMealType);
    }

    public void setErrorMessage(String message) {
        String oldMessage = this.errorMessage;
        this.errorMessage = message != null ? message : "";
        this.successMessage = "";
        this.propertyChangeSupport.firePropertyChange("errorMessage", oldMessage, this.errorMessage);
    }

    public void setSuccessMessage(String message) {
        String oldMessage = this.successMessage;
        this.successMessage = message != null ? message : "";
        this.errorMessage = "";
        this.propertyChangeSupport.firePropertyChange("successMessage", oldMessage, this.successMessage);
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public MealType getSelectedMealType() {
        return selectedMealType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public Map<LocalDate, Map<MealType, String>> getWeeklyMeals() {
        return new HashMap<>(weeklyMeals);
    }
}
