package com.mealplanner.view.component;

import javafx.scene.control.ProgressBar;

/**
 * A styled Progress component.
 * Corresponds to progress.tsx
 */
public class Progress extends ProgressBar {

    public Progress(double progress) {
        super(progress);
        getStyleClass().add("styled-progress");
    }
    
    public Progress() {
        this(0);
    }
}

