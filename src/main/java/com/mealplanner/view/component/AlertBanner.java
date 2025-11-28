package com.mealplanner.view.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Node;

/**
 * Inline Alert Banner component.
 * Corresponds to alert.tsx
 */
public class AlertBanner extends HBox {
    public enum Variant {
        DEFAULT,
        DESTRUCTIVE
    }

    private final Label descLabel;

    public AlertBanner(String title, String description) {
        this(title, description, Variant.DEFAULT, null);
    }
    
    // Legacy constructor support if called with Type.DESTRUCTIVE which maps to Variant.DESTRUCTIVE
    // Assuming the calling code uses Type.DESTRUCTIVE enum from AlertBanner itself if defined there previously,
    // but here we see Variant. Let's add Type enum for compatibility or update calling code.
    // The error said "Type cannot be resolved", so let's add Type as an alias or update usages.
    // Updating the class to include Type is safer if other files use it.
    public enum Type {
        DEFAULT,
        DESTRUCTIVE
    }

    public AlertBanner(String title, String description, Type type) {
        this(title, description, type == Type.DESTRUCTIVE ? Variant.DESTRUCTIVE : Variant.DEFAULT, null);
    }

    public AlertBanner(String title, String description, Variant variant, Node icon) {
        getStyleClass().add("alert");
        if (variant == Variant.DESTRUCTIVE) {
            getStyleClass().add("alert-destructive");
        }

        setSpacing(12);
        setAlignment(Pos.TOP_LEFT);
        setPadding(new Insets(12, 16, 12, 16));

        // Icon
        if (icon != null) {
            getChildren().add(icon);
        }

        // Content
        VBox content = new VBox(4);
        
        if (title != null && !title.isEmpty()) {
            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add("alert-title");
            content.getChildren().add(titleLabel);
        }

        descLabel = new Label(description != null ? description : "");
        descLabel.getStyleClass().add("alert-description");
        descLabel.setWrapText(true);
        content.getChildren().add(descLabel);

        getChildren().add(content);
    }

    public void setDescription(String description) {
        descLabel.setText(description);
    }
}
