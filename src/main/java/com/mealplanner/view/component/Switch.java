package com.mealplanner.view.component;

import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * A custom Switch component (Toggle).
 * Corresponds to switch.tsx
 */
public class Switch extends StackPane {

    private final BooleanProperty switchedOn = new SimpleBooleanProperty(false);
    private final Rectangle background;
    private final Circle thumb;

    public Switch() {
        getStyleClass().add("switch");
        
        background = new Rectangle(40, 20);
        background.setArcWidth(20);
        background.setArcHeight(20);
        background.getStyleClass().add("switch-background");
        
        thumb = new Circle(9);
        thumb.setTranslateX(-10);
        thumb.getStyleClass().add("switch-thumb");
        
        getChildren().addAll(background, thumb);
        setAlignment(Pos.CENTER);
        
        setOnMouseClicked(e -> switchedOn.set(!switchedOn.get()));
        
        switchedOn.addListener((obs, oldVal, newVal) -> animateSwitch(newVal));
    }
    
    private void animateSwitch(boolean on) {
        TranslateTransition translate = new TranslateTransition(Duration.millis(200), thumb);
        translate.setToX(on ? 10 : -10);
        translate.play();
        
        background.getStyleClass().removeAll("on", "off");
        background.getStyleClass().add(on ? "on" : "off");
    }
    
    public BooleanProperty switchedOnProperty() {
        return switchedOn;
    }
}

