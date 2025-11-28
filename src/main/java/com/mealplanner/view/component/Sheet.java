package com.mealplanner.view.component;

import javafx.scene.Node;

/**
 * A styled Sheet component (Sidebar/Drawer).
 * Corresponds to sheet.tsx
 * Effectively an alias for Drawer in our JavaFX implementation context,
 * but could be specialized if Sheet has distinct behavior from generic Drawer.
 */
public class Sheet extends Drawer {

    public Sheet(Node content) {
        super(content);
        // Sheet-specific styling if needed
        getStyleClass().add("sheet");
    }
}

