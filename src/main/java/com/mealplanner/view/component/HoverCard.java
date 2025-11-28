package com.mealplanner.view.component;

import javafx.scene.control.Tooltip;
import javafx.util.Duration;

/**
 * A styled HoverCard (Tooltip wrapper).
 * Corresponds to hover-card.tsx
 */
public class HoverCard extends Tooltip {

    public HoverCard(String text) {
        super(text);
        setShowDelay(Duration.millis(200));
        getStyleClass().add("hover-card");
    }
}

