package com.mealplanner.view.component;

import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

import java.util.List;

/**
 * A styled Toggle Group component.
 * Corresponds to toggle-group.tsx
 */
public class StyledToggleGroup extends HBox {

    private final ToggleGroup group;

    public StyledToggleGroup(List<String> options) {
        group = new ToggleGroup();
        setSpacing(5);
        getStyleClass().add("styled-toggle-group");

        for (String option : options) {
            ToggleButton tb = new ToggleButton(option);
            tb.setToggleGroup(group);
            tb.getStyleClass().add("group-toggle-button");
            getChildren().add(tb);
        }
    }

    public String getSelectedValue() {
        ToggleButton selected = (ToggleButton) group.getSelectedToggle();
        return selected != null ? selected.getText() : null;
    }
}

