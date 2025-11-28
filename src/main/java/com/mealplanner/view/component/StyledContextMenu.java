package com.mealplanner.view.component;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

/**
 * A styled ContextMenu.
 * Corresponds to context-menu.tsx
 */
public class StyledContextMenu extends ContextMenu {

    public StyledContextMenu() {
        getStyleClass().add("styled-context-menu");
    }

    public void addOption(String text, Runnable action) {
        MenuItem item = new MenuItem(text);
        item.setOnAction(e -> action.run());
        getItems().add(item);
    }
}

