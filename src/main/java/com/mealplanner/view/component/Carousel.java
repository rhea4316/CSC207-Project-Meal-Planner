package com.mealplanner.view.component;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.util.List;

/**
 * A simple Horizontal Carousel component.
 * Corresponds to carousel.tsx
 */
public class Carousel extends StackPane {
    
    private final HBox contentBox;
    private final ScrollPane scrollPane;

    public Carousel(List<Node> items) {
        contentBox = new HBox(10);
        contentBox.setAlignment(Pos.CENTER_LEFT);
        if (items != null) {
            contentBox.getChildren().addAll(items);
        }

        scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Hide scrollbar
        scrollPane.setPannable(true);
        scrollPane.getStyleClass().add("carousel-scroll");

        getChildren().add(scrollPane);
        getStyleClass().add("carousel");
        
        // Optional: Add Arrow Buttons overlay
        // For simplicity, we rely on scroll/pan
    }
    
    public void addItem(Node item) {
        contentBox.getChildren().add(item);
    }
}

