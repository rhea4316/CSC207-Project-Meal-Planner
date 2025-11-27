package com.mealplanner.view;

import com.mealplanner.interface_adapter.ViewManagerModel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

// Manages view switching and navigation between different screens using CardLayout.
// Responsible: Everyone (GUI implementation)

public class ViewManager extends JPanel implements PropertyChangeListener {
    public static final String LOGIN_VIEW = "LoginView";
    public static final String SIGNUP_VIEW = "SignupView";
    public static final String BROWSE_RECIPE_VIEW = "BrowseRecipeView";
    public static final String RECIPE_DETAIL_VIEW = "RecipeDetailView";
    public static final String STORE_RECIPE_VIEW = "StoreRecipeView";
    public static final String SEARCH_BY_INGREDIENTS_VIEW = "SearchByIngredientsView";
    public static final String SCHEDULE_VIEW = "ScheduleView";
    public static final String MEAL_PLAN_VIEW = "MealPlanView";

    private final ViewManagerModel viewManagerModel;
    private final CardLayout cardLayout;
    private final Map<String, JPanel> views;

    public ViewManager(ViewManagerModel viewManagerModel) {
        if (viewManagerModel == null) {
            throw new IllegalArgumentException("ViewManagerModel cannot be null");
        }
        
        this.viewManagerModel = viewManagerModel;
        this.cardLayout = new CardLayout();
        this.views = new HashMap<>();

        setLayout(cardLayout);
        viewManagerModel.addPropertyChangeListener(this);
    }

    /**
     * Register a view with the ViewManager.
     * Example: viewManager.addView(ViewManager.RECIPE_DETAIL_VIEW, recipeDetailView);
     */
    public void addView(String viewName, JPanel view) {
        if (viewName == null || viewName.trim().isEmpty()) {
            throw new IllegalArgumentException("View name cannot be null or empty");
        }
        if (view == null) {
            throw new IllegalArgumentException("View cannot be null");
        }
        views.put(viewName, view);
        add(view, viewName);
    }

    public void switchToView(String viewName) {
        if (views.containsKey(viewName)) {
            cardLayout.show(this, viewName);
            viewManagerModel.setActiveView(viewName);
        }
    }

    public String getCurrentView() {
        return viewManagerModel.getActiveView();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("view".equals(evt.getPropertyName())) {
            String newView = (String) evt.getNewValue();
            if (newView != null && views.containsKey(newView)) {
                cardLayout.show(this, newView);
            }
        }
    }
}
