package com.mealplanner.view.component;

import javafx.scene.control.TextField;

/**
 * A styled Input component (TextField).
 * Corresponds to input.tsx
 */
public class Input extends TextField {

    public Input() {
        super();
        initialize();
    }

    public Input(String text) {
        super(text);
        initialize();
    }

    private void initialize() {
        getStyleClass().add("input-field");
        // Remove default style class if needed, or append
        // getStyleClass().removeAll("text-field"); 
    }
}

