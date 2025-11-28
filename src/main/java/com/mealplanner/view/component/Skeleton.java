package com.mealplanner.view.component;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.scene.layout.Region;
import javafx.util.Duration;

/**
 * A Skeleton loading placeholder component.
 * Corresponds to skeleton.tsx
 */
public class Skeleton extends Region {

    public Skeleton(double width, double height) {
        setPrefSize(width, height);
        getStyleClass().add("skeleton");
        
        // Pulse animation
        FadeTransition fade = new FadeTransition(Duration.millis(1000), this);
        fade.setFromValue(0.5);
        fade.setToValue(1.0);
        fade.setAutoReverse(true);
        fade.setCycleCount(Animation.INDEFINITE);
        fade.play();
    }
}

