package com.mealplanner.view.component;

import javafx.scene.control.Tooltip;
import javafx.util.Duration;

/**
 * A styled Tooltip component.
 * Corresponds to tooltip.tsx
 */
public class StyledTooltip extends Tooltip {

    public StyledTooltip(String text) {
        super(text);
        setShowDelay(Duration.millis(300));
        getStyleClass().add("styled-tooltip");
    }
}

