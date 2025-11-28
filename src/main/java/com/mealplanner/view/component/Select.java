package com.mealplanner.view.component;

import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

/**
 * A styled Select component (ComboBox).
 * Corresponds to select.tsx
 */
public class Select<T> extends ComboBox<T> {

    public Select() {
        super();
        initialize();
    }

    public Select(ObservableList<T> items) {
        super(items);
        initialize();
    }

    private void initialize() {
        getStyleClass().add("styled-select");
    }
}

