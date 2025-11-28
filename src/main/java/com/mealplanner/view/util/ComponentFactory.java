package com.mealplanner.view.util;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Factory for creating reusable UI components that don't require full classes.
 */
public class ComponentFactory {

    /**
     * Creates an inline alert block (Alert.tsx).
     * @param title Title of the alert
     * @param description Description text
     * @param isDestructive If true, uses error styling
     * @return Styled VBox
     */
    public static VBox createInlineAlert(String title, String description, boolean isDestructive) {
        VBox alertBox = new VBox(5);
        alertBox.getStyleClass().add("alert");
        
        if (isDestructive) {
            alertBox.getStyleClass().add("destructive");
        }

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("alert-title");
        
        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("alert-description");
        descLabel.setWrapText(true);

        alertBox.getChildren().addAll(titleLabel, descLabel);
        return alertBox;
    }
    
    // Additional helpers for AspectRatio could go here if layout logic is complex,
    // but usually VBox/StackPane constraints are sufficient.
}

