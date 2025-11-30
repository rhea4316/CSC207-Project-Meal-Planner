package com.mealplanner.view;

import com.mealplanner.interface_adapter.ViewManagerModel;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

// Manages view switching and navigation between different screens using JavaFX StackPane.
// Responsible: Everyone (GUI implementation)

public class ViewManager extends StackPane implements PropertyChangeListener {
    public static final String LOGIN_VIEW = "LoginView";
    public static final String SIGNUP_VIEW = "SignupView";
    public static final String DASHBOARD_VIEW = "DashboardView";
    public static final String BROWSE_RECIPE_VIEW = "BrowseRecipeView";
    public static final String RECIPE_DETAIL_VIEW = "RecipeDetailView";
    public static final String STORE_RECIPE_VIEW = "StoreRecipeView";
    public static final String SEARCH_BY_INGREDIENTS_VIEW = "SearchByIngredientsView";
    public static final String SCHEDULE_VIEW = "ScheduleView";
    // public static final String MEAL_PLAN_VIEW = "MealPlanView"; // Removed
    public static final String PROFILE_SETTINGS_VIEW = "ProfileSettingsView";

    private final ViewManagerModel viewManagerModel;
    private final Map<String, Node> views;

    public ViewManager(ViewManagerModel viewManagerModel) {
        if (viewManagerModel == null) {
            throw new IllegalArgumentException("ViewManagerModel cannot be null");
        }
        
        this.viewManagerModel = viewManagerModel;
        this.views = new HashMap<>();

        viewManagerModel.addPropertyChangeListener(this);
    }

    /**
     * Register a view with the ViewManager.
     * Example: viewManager.addView(ViewManager.RECIPE_DETAIL_VIEW, recipeDetailView);
     */
    public void addView(String viewName, Node view) {
        if (viewName == null || viewName.trim().isEmpty()) {
            throw new IllegalArgumentException("View name cannot be null or empty");
        }
        if (view == null) {
            throw new IllegalArgumentException("View cannot be null");
        }
        views.put(viewName, view);
    }

    public void switchToView(String viewName) {
        if (viewName == null || viewName.trim().isEmpty()) {
            System.err.println("ViewManager: Cannot switch to null or empty view name");
            return;
        }
        
        if (!views.containsKey(viewName)) {
            System.err.println("ViewManager: View '" + viewName + "' is not registered. Available views: " + views.keySet());
            return;
        }
        
        Node view = views.get(viewName);
        if (view == null) {
            System.err.println("ViewManager: View '" + viewName + "' is null");
            return;
        }
        
        try {
            // Dispose current view if it implements disposable interface
            Node currentView = getChildren().isEmpty() ? null : getChildren().get(0);
            if (currentView != null && currentView != view) {
                disposeView(currentView);
            }
            
            // Replace current view
            getChildren().setAll(view);
        } catch (Exception e) {
            System.err.println("ViewManager: Error switching to view '" + viewName + "': " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Dispose a view by calling its dispose method if it exists.
     * Uses reflection to check for dispose method to avoid coupling.
     */
    private void disposeView(Node view) {
        try {
            java.lang.reflect.Method disposeMethod = view.getClass().getMethod("dispose");
            if (disposeMethod != null) {
                disposeMethod.invoke(view);
            }
        } catch (NoSuchMethodException e) {
            // View doesn't have dispose method - that's okay
        } catch (Exception e) {
            System.err.println("ViewManager: Error disposing view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getCurrentView() {
        return viewManagerModel.getActiveView();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("view".equals(evt.getPropertyName())) {
            String newView = (String) evt.getNewValue();
            Platform.runLater(() -> switchToView(newView));
        }
    }
}
