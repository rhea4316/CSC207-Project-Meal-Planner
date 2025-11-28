package com.mealplanner.view.component;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

/**
 * A styled Menubar component.
 * Corresponds to menubar.tsx
 */
public class StyledMenuBar extends MenuBar {

    public StyledMenuBar() {
        getStyleClass().add("styled-menubar");
    }

    public void addMenu(String title, MenuItem... items) {
        Menu menu = new Menu(title);
        menu.getItems().addAll(items);
        getMenus().add(menu);
    }
}

