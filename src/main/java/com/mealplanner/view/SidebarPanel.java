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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class SidebarPanel extends VBox implements PropertyChangeListener {
    private final ViewManagerModel viewManagerModel;
    private Button currentActiveButton;
    private Label userLabel;

    // Colors matching style.css
    private static final Color ACTIVE_TEXT_COLOR = Color.web("#fbfbfb"); 
    private static final Color INACTIVE_TEXT_COLOR = Color.web("#717182");

    // Helper class to store button data
    private static class MenuItemData {
        String viewName;
        String activeIconPath;
        String inactiveIconPath;

        MenuItemData(String viewName, String activeIconPath, String inactiveIconPath) {
            this.viewName = viewName;
            this.activeIconPath = activeIconPath;
            this.inactiveIconPath = inactiveIconPath;
        }
    }

    public SidebarPanel(ViewManagerModel viewManagerModel) {
        this.viewManagerModel = viewManagerModel;
        this.viewManagerModel.addPropertyChangeListener(this);
        
        getStyleClass().add("sidebar");
        setMinWidth(240);

        // Logo
        Label logo = new Label("PlanEat");
        logo.getStyleClass().add("sidebar-logo");
        getChildren().add(logo);

        // Menu Items with Icons
        addMenuItem("Dashboard", ViewManager.DASHBOARD_VIEW, "/svg/dashboard-fill.svg", "/svg/dashboard.svg");
        addMenuItem("Weekly Plan", ViewManager.SCHEDULE_VIEW, "/svg/weekly-plan-fill.svg", "/svg/weekly-plan.svg");
        addMenuItem("Find by Ingredients", ViewManager.SEARCH_BY_INGREDIENTS_VIEW, "/svg/search.svg", "/svg/search.svg");
        addMenuItem("My Cookbook", ViewManager.STORE_RECIPE_VIEW, "/svg/cookbook-fill.svg", "/svg/cookbook.svg");
        addMenuItem("Recipe Catalog", ViewManager.BROWSE_RECIPE_VIEW, "/svg/shopping-basket-fill.svg", "/svg/shopping-basket.svg");
        
        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);
        
        // Separator (New)
        com.mealplanner.view.component.Separator separator = new com.mealplanner.view.component.Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));
        getChildren().add(separator);
        
        // Profile Section
        addMenuItem("Profile Settings", ViewManager.PROFILE_SETTINGS_VIEW, "/svg/users.svg", "/svg/users.svg");

        userLabel = new Label("Logged in as User");
        userLabel.getStyleClass().add("sidebar-user-label");
        getChildren().add(userLabel);
        
        // Set initial active view
        updateActiveButton(viewManagerModel.getActiveView());
    }

    private void addMenuItem(String text, String viewName, String activeIconPath, String inactiveIconPath) {
        Button btn = new Button();
        btn.getStyleClass().add("sidebar-item");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        
        // Create HBox for icon and text
        HBox contentBox = new HBox(12);
        contentBox.setAlignment(Pos.CENTER_LEFT);
        
        // Load initial icon (inactive)
        Node icon = SvgIconLoader.loadIcon(inactiveIconPath, 20, INACTIVE_TEXT_COLOR);
        if (icon != null) {
            contentBox.getChildren().add(icon);
        }
        
        // Add text label
        Label textLabel = new Label(text);
        textLabel.setTextFill(INACTIVE_TEXT_COLOR); 
        contentBox.getChildren().add(textLabel);
        
        btn.setGraphic(contentBox);
        
        // Store reference data
        btn.setUserData(new MenuItemData(viewName, activeIconPath, inactiveIconPath));
        
        btn.setOnAction(e -> viewManagerModel.setActiveView(viewName));
        
        getChildren().add(btn);
    }
    
    private void updateButtonState(Button btn, boolean isActive) {
        if (btn.getGraphic() instanceof HBox) {
            HBox contentBox = (HBox) btn.getGraphic();
            MenuItemData data = (MenuItemData) btn.getUserData();
            
            Color targetColor = isActive ? ACTIVE_TEXT_COLOR : INACTIVE_TEXT_COLOR;
            String targetIconPath = isActive ? data.activeIconPath : data.inactiveIconPath;
            
            // Update Icon
            if (!contentBox.getChildren().isEmpty()) {
                Node newIcon = SvgIconLoader.loadIcon(targetIconPath, 20, targetColor);
                if (newIcon != null) {
                    contentBox.getChildren().set(0, newIcon);
                }
            }
            
            // Update Text Color
            if (contentBox.getChildren().size() > 1) {
                 Node textNode = contentBox.getChildren().get(1);
                 if (textNode instanceof Label) {
                     ((Label) textNode).setTextFill(targetColor);
                 }
            }
        }
    }
    
    private void updateActiveButton(String activeView) {
        // Deactivate current button
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("selected");
            updateButtonState(currentActiveButton, false);
        }
        
        if (activeView == null) {
            return;
        }
        
        // Find and activate the button for the current view
        for (javafx.scene.Node node : getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                if (btn.getUserData() instanceof MenuItemData) {
                    MenuItemData data = (MenuItemData) btn.getUserData();
                    if (activeView.equals(data.viewName)) {
                        btn.getStyleClass().add("selected");
                        updateButtonState(btn, true);
                        currentActiveButton = btn;
                        break;
                    }
                }
            }
        }
    }

    public void setUsername(String username) {
        if (userLabel != null) {
            userLabel.setText("Logged in as " + username);
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
