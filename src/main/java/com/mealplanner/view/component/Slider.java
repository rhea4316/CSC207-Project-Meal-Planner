package com.mealplanner.view.component;

/**
 * A styled Slider component.
 * Corresponds to slider.tsx
 */
public class Slider extends javafx.scene.control.Slider {

    public Slider(double min, double max, double value) {
        super(min, max, value);
        initialize();
    }
    
    public Slider() {
        super();
        initialize();
    }

    private void initialize() {
        getStyleClass().add("styled-slider");
    }
}

