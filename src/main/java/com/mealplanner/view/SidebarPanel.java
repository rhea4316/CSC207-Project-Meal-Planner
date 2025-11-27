package com.mealplanner.view;

import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.view.util.SvgIconLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class SidebarPanel extends VBox implements PropertyChangeListener {
    private final ViewManagerModel viewManagerModel;
    private Button currentActiveButton;

    public SidebarPanel(ViewManagerModel viewManagerModel) {
        this.viewManagerModel = viewManagerModel;
        this.viewManagerModel.addPropertyChangeListener(this);
        
        getStyleClass().add("sidebar");
        setSpacing(10);
        setPadding(new Insets(20));
        setMinWidth(240);

        // Logo/Menu Icon
        Label menuIcon = new Label("☰");
        menuIcon.setFont(javafx.scene.text.Font.font(24));
        menuIcon.setPadding(new Insets(0, 0, 30, 0));
        getChildren().add(menuIcon);

        // Menu Items with Icons
        addMenuItem("Dashboard", ViewManager.DASHBOARD_VIEW, "/svg/dashboard-fill.svg");
        addMenuItem("Weekly Plan", ViewManager.SCHEDULE_VIEW, "/svg/weekly-plan.svg");
        addMenuItem("Search Recipes", ViewManager.SEARCH_BY_INGREDIENTS_VIEW, "/svg/search.svg");
        addMenuItem("My Cookbook", ViewManager.STORE_RECIPE_VIEW, "/svg/cookbook.svg");
        addMenuItem("Browse Recipes", ViewManager.BROWSE_RECIPE_VIEW, "/svg/shopping-basket.svg");
        
        // User Profile (Spacer then Label)
        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);
        
        Label userLabel = new Label("Logged in as User");
        userLabel.getStyleClass().add("sidebar-user-label");
        getChildren().add(userLabel);
        
        // Set initial active view
        updateActiveButton(viewManagerModel.getActiveView());
    }

    private void addMenuItem(String text, String viewName, String iconPath) {
        Button btn = new Button();
        btn.getStyleClass().add("sidebar-item");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        
        // Create HBox for icon and text
        HBox contentBox = new HBox(12);
        contentBox.setAlignment(Pos.CENTER_LEFT);
        
        // Load icon
        Node icon = SvgIconLoader.loadIcon(iconPath, 20, Color.web("#6B7280"));
        if (icon != null) {
            contentBox.getChildren().add(icon);
        }
        
        // Add text label
        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-text-fill: #6B7280;");
        contentBox.getChildren().add(textLabel);
        
        btn.setGraphic(contentBox);
        
        // Update icon color on hover/active
        btn.setOnMouseEntered(e -> {
            if (btn != currentActiveButton) {
                updateIconColor(btn, Color.web("#1F2937"));
            }
        });
        
        btn.setOnMouseExited(e -> {
            if (btn != currentActiveButton) {
                updateIconColor(btn, Color.web("#6B7280"));
            }
        });
        
        btn.setOnAction(e -> viewManagerModel.setActiveView(viewName));
        
        // Store reference to set as active if needed
        btn.setUserData(viewName);
        
        getChildren().add(btn);
    }
    
    private void updateIconColor(Button btn, Color color) {
        if (btn.getGraphic() instanceof HBox) {
            HBox contentBox = (HBox) btn.getGraphic();
            for (Node node : contentBox.getChildren()) {
                if (node instanceof javafx.scene.shape.SVGPath) {
                    ((javafx.scene.shape.SVGPath) node).setFill(color);
                }
            }
            // Update text color
            for (Node node : contentBox.getChildren()) {
                if (node instanceof Label) {
                    ((Label) node).setTextFill(color);
                }
            }
        }
    }
    
    private void updateActiveButton(String activeView) {
        // Remove active style from current button
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("selected");
            updateIconColor(currentActiveButton, Color.web("#6B7280"));
        }
        
        // Find and activate the button for the current view
        for (javafx.scene.Node node : getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                if (activeView.equals(btn.getUserData())) {
                    btn.getStyleClass().add("selected");
                    updateIconColor(btn, Color.WHITE);
                    currentActiveButton = btn;
                    break;
                }
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("view".equals(evt.getPropertyName())) {
            String newView = (String) evt.getNewValue();
            javafx.application.Platform.runLater(() -> updateActiveButton(newView));
        }
    }
}
