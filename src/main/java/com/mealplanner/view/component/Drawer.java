package com.mealplanner.view.component;

import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * A Slide-in Drawer component.
 * Corresponds to drawer.tsx
 */
public class Drawer extends StackPane {

    private final VBox drawerContent;
    private final StackPane overlay;
    private boolean isOpen = false;

    public Drawer(Node content) {
        setAlignment(Pos.CENTER_LEFT); // Or RIGHT based on direction
        setPickOnBounds(false); // Allow clicking through when closed

        // Overlay (Dimmed background)
        overlay = new StackPane();
        overlay.getStyleClass().add("drawer-overlay");
        overlay.setVisible(false);
        overlay.setOnMouseClicked(e -> close());

        // Drawer Content Panel
        drawerContent = new VBox();
        drawerContent.getStyleClass().add("drawer-content");
        drawerContent.getChildren().add(content);
        drawerContent.setMaxWidth(300);
        drawerContent.setTranslateX(-300); // Start hidden off-screen

        getChildren().addAll(overlay, drawerContent);
    }

    public void open() {
        if (isOpen) return;
        isOpen = true;
        overlay.setVisible(true);
        setPickOnBounds(true); // Block clicks to underlying UI
        
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), drawerContent);
        transition.setToX(0);
        transition.play();
    }

    public void close() {
        if (!isOpen) return;
        isOpen = false;
        
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), drawerContent);
        transition.setToX(-300);
        transition.setOnFinished(e -> {
            overlay.setVisible(false);
            setPickOnBounds(false);
        });
        transition.play();
    }
}

