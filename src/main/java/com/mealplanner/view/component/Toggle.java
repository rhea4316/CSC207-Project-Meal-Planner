package com.mealplanner.view.component;

import javafx.scene.control.ToggleButton;

/**
 * A styled Toggle component.
 * Corresponds to toggle.tsx
 */
public class Toggle extends ToggleButton {

    public Toggle(String text) {
        super(text);
        initialize();
    }

    private void initialize() {
        getStyleClass().add("styled-toggle");
    }
}

