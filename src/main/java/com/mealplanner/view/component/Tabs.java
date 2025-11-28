package com.mealplanner.view.component;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * A styled Tabs component.
 * Corresponds to tabs.tsx
 */
public class Tabs extends TabPane {

    public Tabs() {
        super();
        initialize();
    }

    private void initialize() {
        getStyleClass().add("styled-tabs");
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
    }

    public void addTab(String title, Node content) {
        Tab tab = new Tab(title, content);
        getTabs().add(tab);
    }
}

