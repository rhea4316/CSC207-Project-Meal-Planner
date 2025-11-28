package com.mealplanner.view.component;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;

/**
 * A styled Resizable Panel component (SplitPane wrapper).
 * Corresponds to resizable.tsx
 */
public class Resizable extends SplitPane {

    public Resizable(Orientation orientation, Node... items) {
        setOrientation(orientation);
        getItems().addAll(items);
        getStyleClass().add("resizable-panel-group");
        setDividerPositions(0.5); // Default split
    }
}

