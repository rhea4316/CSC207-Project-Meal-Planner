package com.mealplanner.view.component;

import javafx.scene.control.TextArea;

/**
 * A styled Textarea component.
 * Corresponds to textarea.tsx
 */
public class Textarea extends TextArea {

    public Textarea() {
        super();
        initialize();
    }

    public Textarea(String text) {
        super(text);
        initialize();
    }

    private void initialize() {
        getStyleClass().add("styled-textarea");
        setWrapText(true);
    }
}

