package com.mealplanner.view.component;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

/**
 * A styled Navigation Menu (Toolbar-like).
 * Corresponds to navigation-menu.tsx
 */
public class NavigationMenu extends HBox {

    public NavigationMenu() {
        setSpacing(5);
        getStyleClass().add("navigation-menu");
    }

    public void addLink(String text, Runnable action) {
        Button linkBtn = new Button(text);
        linkBtn.getStyleClass().add("navigation-menu-link");
        linkBtn.setOnAction(e -> action.run());
        getChildren().add(linkBtn);
    }
}

