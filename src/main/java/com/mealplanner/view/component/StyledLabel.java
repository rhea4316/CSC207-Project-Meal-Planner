package com.mealplanner.view.component;

/**
 * A styled Label component.
 * Corresponds to label.tsx
 * Note: JavaFX already has a Label class, so we extend it or use a static factory.
 * Extending avoids name collision if package is imported, but simple usage is better.
 * We'll call it StyledLabel to be safe, or just ensure usage via full package or style class.
 */
public class StyledLabel extends javafx.scene.control.Label {

    public StyledLabel(String text) {
        super(text);
        getStyleClass().add("styled-label");
    }
}

