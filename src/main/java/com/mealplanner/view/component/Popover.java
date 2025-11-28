package com.mealplanner.view.component;

import javafx.scene.Node;
import javafx.stage.Popup;
import javafx.stage.Window;

/**
 * A styled Popover component.
 * Corresponds to popover.tsx
 */
public class Popover {

    private final Popup popup;

    public Popover(Node content) {
        popup = new Popup();
        popup.getContent().add(content);
        popup.setAutoHide(true); // Close when clicking outside
        
        // Add style class to content node if it's a Region, or wrap it
        if (content instanceof javafx.scene.layout.Region) {
            ((javafx.scene.layout.Region) content).getStyleClass().add("popover-content");
        }
    }

    public void show(Node ownerNode) {
        Window window = ownerNode.getScene().getWindow();
        // Calculate position relative to ownerNode
        javafx.geometry.Point2D point = ownerNode.localToScreen(0, 0);
        popup.show(window, point.getX(), point.getY() + ownerNode.getBoundsInLocal().getHeight());
    }
    
    public void hide() {
        popup.hide();
    }
}

