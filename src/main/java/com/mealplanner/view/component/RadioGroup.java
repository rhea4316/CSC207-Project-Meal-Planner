package com.mealplanner.view.component;

import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * A styled Radio Group component.
 * Corresponds to radio-group.tsx
 */
public class RadioGroup extends VBox {

    private final ToggleGroup group;

    public RadioGroup(List<String> options) {
        group = new ToggleGroup();
        setSpacing(10);
        getStyleClass().add("radio-group");

        for (String option : options) {
            RadioButton rb = new RadioButton(option);
            rb.setToggleGroup(group);
            rb.getStyleClass().add("radio-button");
            getChildren().add(rb);
        }
    }

    public String getSelectedValue() {
        RadioButton selected = (RadioButton) group.getSelectedToggle();
        return selected != null ? selected.getText() : null;
    }
}

