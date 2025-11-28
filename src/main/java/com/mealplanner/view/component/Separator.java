package com.mealplanner.view.component;

import javafx.geometry.Orientation;

/**
 * A styled Separator component.
 * Corresponds to separator.tsx
 */
public class Separator extends javafx.scene.control.Separator {

    public Separator() {
        this(Orientation.HORIZONTAL);
    }

    public Separator(Orientation orientation) {
        super(orientation);
        getStyleClass().add("styled-separator");
    }
}

