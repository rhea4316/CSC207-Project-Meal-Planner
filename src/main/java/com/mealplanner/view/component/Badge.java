package com.mealplanner.view.component;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * A Badge component for status, tags, or notifications.
 * Corresponds to badge.tsx
 */
public class Badge extends StackPane {

    public enum Variant {
        DEFAULT, SECONDARY, DESTRUCTIVE, OUTLINE
    }

    public Badge(String text) {
        this(text, Variant.DEFAULT);
    }

    public Badge(String text, Variant variant) {
        Label label = new Label(text);
        label.getStyleClass().add("badge-label");
        
        getChildren().add(label);
        getStyleClass().add("badge");
        
        switch (variant) {
            case SECONDARY:
                getStyleClass().add("badge-secondary");
                break;
            case DESTRUCTIVE:
                getStyleClass().add("badge-destructive");
                break;
            case OUTLINE:
                getStyleClass().add("badge-outline");
                break;
            default:
                getStyleClass().add("badge-default");
                break;
        }
    }
}

