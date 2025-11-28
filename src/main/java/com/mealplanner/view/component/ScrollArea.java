package com.mealplanner.view.component;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;

/**
 * A styled ScrollArea component.
 * Corresponds to scroll-area.tsx
 */
public class ScrollArea extends ScrollPane {

    public ScrollArea(Node content) {
        super(content);
        setFitToWidth(true);
        // fitToHeight defaults to false for vertical scrolling
        getStyleClass().add("scroll-area");
        
        // Customize scrollbars via CSS
    }
}

