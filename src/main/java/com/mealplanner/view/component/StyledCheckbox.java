package com.mealplanner.view.component;

import javafx.scene.control.CheckBox;

/**
 * A styled Checkbox component.
 * Corresponds to checkbox.tsx
 */
public class StyledCheckbox extends CheckBox {

    public StyledCheckbox(String text) {
        super(text);
        initialize();
    }

    public StyledCheckbox() {
        super();
        initialize();
    }

    private void initialize() {
        getStyleClass().add("styled-checkbox");
    }
}

