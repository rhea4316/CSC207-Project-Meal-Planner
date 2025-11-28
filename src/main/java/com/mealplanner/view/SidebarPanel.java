package com.mealplanner.view;

import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.view.component.Avatar;
import com.mealplanner.view.util.SvgIconLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class SidebarPanel extends VBox implements PropertyChangeListener {
    private final ViewManagerModel viewManagerModel;
    private Button currentActiveButton;
    
    // Colors matching style.css updated Green Theme
    // Text Colors
    private static final Color TEXT_DEFAULT = Color.web("#374151"); // Gray 700
    private static final Color TEXT_ACTIVE = Color.web("#4d7c0f"); // Lime 700 (Modified from #1a8b00)
    
    // Icon Colors
    private static final Color ICON_DEFAULT = Color.web("#6b7280"); // Gray 500 (Modified from #6B7280 - same hex, just ensuring consistency)
    private static final Color ICON_ACTIVE = Color.WHITE; // Active Icon Color (White)

    // Helper class to store button data
    private static class MenuItemData {
        String viewName;
        @SuppressWarnings("unused")
        String activeIconPath; // Reserved for future use (currently using inactive path with color change)
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
        setMinWidth(250);
        setSpacing(8); // Increased vertical spacing between items

        // Logo
        HBox logoBox = new HBox(10);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setPadding(new Insets(0, 0, 30, 10));
        
        // Refined Logo Area
        HBox brandBox = new HBox(12);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        // Increased left padding to 20 to match right visual weight/border
        brandBox.setPadding(new Insets(10, 10, 30, 20));
        
        // Icon placeholder (Green square with rounded corners)
        Node brandIcon = SvgIconLoader.loadIcon("/svg/leaf.svg", 24, Color.web("#4CAF50")); // Use leaf or similar
        if (brandIcon == null) {
            // Fallback shape
            Region r = new Region();
            r.setPrefSize(32, 32);
            r.setStyle("-fx-background-color: #4CAF50; -fx-background-radius: 8px;");
            brandIcon = r;
        }
        
        VBox brandText = new VBox(-2);
        Label brandName = new Label("PlanEat");
        brandName.getStyleClass().add("text-gray-900");
        brandName.setStyle("-fx-font-family: 'Poppins'; -fx-font-weight: bold; -fx-font-size: 20px;");
        Label brandSub = new Label("Meal Planner");
        brandSub.getStyleClass().add("text-gray-500");
        brandSub.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 11px;");
        brandText.getChildren().addAll(brandName, brandSub);
        
        brandBox.getChildren().addAll(brandIcon, brandText);
        getChildren().add(brandBox);

        // PLAN Category
        addCategoryLabel("PLAN");
        addMenuItem("Dashboard", ViewManager.DASHBOARD_VIEW, "/svg/home-fill.svg", "/svg/home.svg");
        addMenuItem("Weekly Plan", ViewManager.SCHEDULE_VIEW, "/svg/calendar-fill.svg", "/svg/calendar.svg");

        // RECIPES Category
        addCategoryLabel("RECIPES");
        addMenuItem("Find by Ingredients", ViewManager.SEARCH_BY_INGREDIENTS_VIEW, "/svg/apple-fill.svg", "/svg/apple.svg");
        addMenuItem("Recipe Catalog", ViewManager.BROWSE_RECIPE_VIEW, "/svg/books-fill.svg", "/svg/books.svg");
        addMenuItem("My CookBook", ViewManager.STORE_RECIPE_VIEW, "/svg/open-book-fill.svg", "/svg/open-book.svg");
        
        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);
        
        // Profile Section (Bottom)
        createProfileSection();
        
        // Set initial active view
        updateActiveButton(viewManagerModel.getActiveView());
    }

    private void addCategoryLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("sidebar-category");
        // Add left padding to align with items, and top/bottom margin
        label.setPadding(new Insets(20, 10, 8, 20)); 
        getChildren().add(label);
    }

    private void addMenuItem(String text, String viewName, String activeIconPath, String inactiveIconPath) {
        Button btn = new Button();
        btn.getStyleClass().add("sidebar-item");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        
        // Increase height slightly (padding handles effective height)
        btn.setPadding(new Insets(12, 15, 12, 15)); 
        
        // Reduce vertical margin slightly as requested
        VBox.setMargin(btn, new Insets(1, 0, 1, 0)); 
        
        // Create HBox for icon and text
        HBox contentBox = new HBox(16); // Increased spacing between icon and text
        contentBox.setAlignment(Pos.CENTER_LEFT);
        
        // Icon Container (StackPane for background + icon)
        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(32, 32);
        iconContainer.setMaxSize(32, 32); // Fixed size for consistent alignment
        iconContainer.setAlignment(Pos.CENTER);
        
        // Background shape (hidden by default)
        Rectangle iconBg = new Rectangle(32, 32);
        iconBg.setArcWidth(14);
        iconBg.setArcHeight(14);
        iconBg.setFill(Color.TRANSPARENT); // Default transparent
        
        // Load initial icon (inactive)
        Node icon = SvgIconLoader.loadIcon(inactiveIconPath, 18, ICON_DEFAULT);
        
        iconContainer.getChildren().addAll(iconBg);
        if (icon != null) {
            iconContainer.getChildren().add(icon);
        }
        
        contentBox.getChildren().add(iconContainer);
        
        // Add text label
        Label textLabel = new Label(text);
        contentBox.getChildren().add(textLabel);
        
        btn.setGraphic(contentBox);
        
        // Store reference data
        btn.setUserData(new MenuItemData(viewName, activeIconPath, inactiveIconPath));
        
        btn.setOnAction(e -> viewManagerModel.setActiveView(viewName));
        
        getChildren().add(btn);
    }
    
    private void createProfileSection() {
        HBox profileBox = new HBox(12);
        profileBox.getStyleClass().add("sidebar-profile");
        profileBox.setAlignment(Pos.CENTER_LEFT);
        // Increased left padding to 20
        profileBox.setPadding(new Insets(12, 12, 12, 20));
        
        // Avatar
        Avatar avatar = new Avatar(20, null, "EC"); // 40px size
        // Override avatar style to match image (Green circle)
        avatar.setStyle("-fx-background-color: #4CAF50;"); 
        
        VBox userInfo = new VBox(0);
        Label nameLabel = new Label("Eden Chang");
        nameLabel.getStyleClass().add("text-gray-900");
        nameLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-weight: 600; -fx-font-size: 14px;");
        
        Label statusLabel = new Label("Premium");
        statusLabel.getStyleClass().add("text-gray-500");
        statusLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 11px;");
        
        userInfo.getChildren().addAll(nameLabel, statusLabel);
        
        // Dropdown icon
        Label arrow = new Label("⌄");
        arrow.getStyleClass().add("text-gray-400");
        arrow.setStyle("-fx-font-size: 16px; -fx-padding: 0 0 5 0;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        profileBox.getChildren().addAll(avatar, userInfo, spacer, arrow);
        
        // Action to profile settings
        profileBox.setOnMouseClicked(e -> viewManagerModel.setActiveView(ViewManager.PROFILE_SETTINGS_VIEW));
        
        getChildren().add(profileBox);
    }
    
    private void updateButtonState(Button btn, boolean isActive) {
        if (btn.getGraphic() instanceof HBox) {
            HBox contentBox = (HBox) btn.getGraphic();
            MenuItemData data = (MenuItemData) btn.getUserData();
            
            // Colors
            Color targetIconColor = isActive ? ICON_ACTIVE : ICON_DEFAULT;
            Color targetTextColor = isActive ? TEXT_ACTIVE : TEXT_DEFAULT;
            
            // Icon path: keep inactive path (line style) even when active, but change color
            String targetIconPath = data.inactiveIconPath; 
            
            // Icon Container
            if (!contentBox.getChildren().isEmpty() && contentBox.getChildren().get(0) instanceof StackPane) {
                StackPane iconContainer = (StackPane) contentBox.getChildren().get(0);
                
                // Update Background
                if (!iconContainer.getChildren().isEmpty() && iconContainer.getChildren().get(0) instanceof Rectangle) {
                    Rectangle bg = (Rectangle) iconContainer.getChildren().get(0);
                    if (isActive) {
                        Stop[] stops = new Stop[] { new Stop(0, Color.web("#77ce00")), new Stop(1, Color.web("#00c94f")) };
                        LinearGradient lg = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
                        bg.setFill(lg);
                    } else {
                        bg.setFill(Color.TRANSPARENT);
                    }
                }
                
                // Update Icon
                if (iconContainer.getChildren().size() > 1) {
                    Node newIcon = SvgIconLoader.loadIcon(targetIconPath, 18, targetIconColor);
                    if (newIcon != null) {
                        iconContainer.getChildren().set(1, newIcon);
                    }
                }
            }
            
            // Update Text Color
            if (contentBox.getChildren().size() > 1) {
                 Node textNode = contentBox.getChildren().get(1);
                 if (textNode instanceof Label) {
                     ((Label) textNode).setTextFill(targetTextColor);
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
        for (Node node : getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                if (btn.getUserData() instanceof MenuItemData) {
                    MenuItemData data = (MenuItemData) btn.getUserData();
                    if (activeView.equals(data.viewName)) {
                        btn.getStyleClass().add("selected");
                        updateButtonState(btn, true);
                        currentActiveButton = btn;
                    }
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
