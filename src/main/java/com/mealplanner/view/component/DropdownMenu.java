package com.mealplanner.view.component;

import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;

/**
 * A styled Dropdown Menu.
 * Corresponds to dropdown-menu.tsx
 */
public class DropdownMenu extends MenuButton {

    public DropdownMenu(String text) {
        super(text);
        getStyleClass().add("dropdown-menu");
    }

    public void addOption(String text, Runnable action) {
        MenuItem item = new MenuItem(text);
        item.setOnAction(e -> action.run());
        getItems().add(item);
    }
}

