package com.mealplanner.view.component;

import javafx.scene.Node;
import javafx.scene.control.TitledPane;

/**
 * A styled Collapsible component using JavaFX TitledPane.
 * Corresponds to collapsible.tsx
 */
public class Collapsible extends TitledPane {

    public Collapsible(String title, Node content) {
        super(title, content);
        setExpanded(false); // Default collapsed
        getStyleClass().add("collapsible");
    }
}

