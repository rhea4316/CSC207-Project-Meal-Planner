package com.mealplanner.view.component;

import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.List;

/**
 * Breadcrumb navigation component.
 * Corresponds to breadcrumb.tsx
 */
public class Breadcrumb extends HBox {

    public static class Item {
        String text;
        Runnable action;

        public Item(String text, Runnable action) {
            this.text = text;
            this.action = action;
        }
    }

    public Breadcrumb(List<Item> items) {
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(5);
        getStyleClass().add("breadcrumb");

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            
            if (i < items.size() - 1) {
                // Link
                Hyperlink link = new Hyperlink(item.text);
                link.getStyleClass().add("breadcrumb-link");
                if (item.action != null) {
                    link.setOnAction(e -> item.action.run());
                }
                getChildren().add(link);
                
                // Separator
                Label separator = new Label(">"); // or chevron icon
                separator.getStyleClass().add("breadcrumb-separator");
                getChildren().add(separator);
            } else {
                // Current Page (Text)
                Label current = new Label(item.text);
                current.getStyleClass().add("breadcrumb-page");
                getChildren().add(current);
            }
        }
    }
}

