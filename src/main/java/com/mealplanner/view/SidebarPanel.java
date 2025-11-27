package com.mealplanner.view;

import com.mealplanner.interface_adapter.ViewManagerModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SidebarPanel extends VBox {
    private final ViewManagerModel viewManagerModel;

    public SidebarPanel(ViewManagerModel viewManagerModel) {
        this.viewManagerModel = viewManagerModel;
        
        getStyleClass().add("sidebar");
        setSpacing(5);
        setPadding(new Insets(0));
        setMinWidth(250);

        // Logo
        Label logoLabel = new Label("Meal Planner");
        logoLabel.getStyleClass().add("sidebar-logo");
        getChildren().add(logoLabel);

        // Menu Items
        addMenuItem("Dashboard", ViewManager.DASHBOARD_VIEW);
        addMenuItem("Weekly Plan", ViewManager.SCHEDULE_VIEW);
        addMenuItem("Search Recipes", ViewManager.SEARCH_BY_INGREDIENTS_VIEW);
        addMenuItem("My Cookbook", ViewManager.STORE_RECIPE_VIEW);
        addMenuItem("Browse Recipes", ViewManager.BROWSE_RECIPE_VIEW);
        
        // User Profile (Spacer then Label)
        VBox spacer = new VBox();
        spacer.setMinHeight(200); // Rough spacer or use VBox.vgrow
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        getChildren().add(spacer);
        
        Label userLabel = new Label("Logged in as User");
        userLabel.getStyleClass().add("sidebar-user-label");
        getChildren().add(userLabel);
    }

    private void addMenuItem(String text, String viewName) {
        Button btn = new Button(text);
        btn.getStyleClass().add("sidebar-item");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        
        btn.setOnAction(e -> viewManagerModel.setActiveView(viewName));
        
        getChildren().add(btn);
    }
}
