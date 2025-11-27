package com.mealplanner.view.style;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ModernUI {

    // 1. Design Theme Constants
    public static final Color PRIMARY_COLOR = Color.web("#4CAF50");   // Sage Green
    public static final Color BACKGROUND_COLOR = Color.web("#F5F5F5"); // Off-White
    public static final Color SURFACE_COLOR = Color.web("#FFFFFF");    // White
    public static final Color TEXT_COLOR = Color.web("#333333");       // Dark Charcoal
    public static final Color SECONDARY_COLOR = Color.web("#FF9800");  // Burnt Orange

    // Hover/Interaction Colors
    private static final Color PRIMARY_HOVER_COLOR = Color.web("#388E3C");
    private static final Color GHOST_HOVER_BG_COLOR = Color.web("#E8F5E9");
    private static final Color BORDER_COLOR = Color.rgb(220, 220, 220);
    private static final Color HEADER_TEXT_COLOR = Color.web("#2c3e50");

    // Fonts
    private static final String FONT_FAMILY = "Segoe UI";

    /**
     * Create a Card Panel (VBox) with rounded corners, border, and drop shadow.
     */
    public static VBox createCardPanel() {
        VBox card = new VBox();
        
        // Padding: 20px
        card.setPadding(new Insets(20));
        
        // Background: White, Radius: 15px
        card.setBackground(new Background(new BackgroundFill(
            SURFACE_COLOR, 
            new CornerRadii(15), 
            Insets.EMPTY
        )));

        // Border: Light Gray, Width: 1px, Radius: 15px
        card.setBorder(new Border(new BorderStroke(
            BORDER_COLOR,
            BorderStrokeStyle.SOLID,
            new CornerRadii(15),
            new BorderWidths(1)
        )));

        // Drop Shadow (Simulating paintComponent override)
        DropShadow shadow = new DropShadow();
        shadow.setBlurType(BlurType.GAUSSIAN);
        shadow.setColor(Color.rgb(0, 0, 0, 0.1)); // 10% opacity black
        shadow.setRadius(10);
        shadow.setOffsetY(2);
        card.setEffect(shadow);

        return card;
    }

    /**
     * Create a Primary Button (Sage Green).
     */
    public static Button createPrimaryButton(String text) {
        Button btn = new Button(text);
        
        // Font: White, Bold
        btn.setTextFill(Color.WHITE);
        btn.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));
        
        // Background: Sage Green, Radius: 10px
        // Padding: 10px (Top/Bottom), 20px (Left/Right)
        // Using CSS for simpler padding/radius combo or Background object
        btn.setBackground(new Background(new BackgroundFill(
            PRIMARY_COLOR, 
            new CornerRadii(10), 
            Insets.EMPTY
        )));
        btn.setPadding(new Insets(10, 20, 10, 20));
        
        // Remove default border
        btn.setBorder(Border.EMPTY);

        // Interaction: Hover Effect
        btn.setOnMouseEntered(e -> btn.setBackground(new Background(new BackgroundFill(
            PRIMARY_HOVER_COLOR, 
            new CornerRadii(10), 
            Insets.EMPTY
        ))));

        btn.setOnMouseExited(e -> btn.setBackground(new Background(new BackgroundFill(
            PRIMARY_COLOR, 
            new CornerRadii(10), 
            Insets.EMPTY
        ))));

        // Cursor handling
        btn.setOnMouseMoved(e -> btn.setCursor(javafx.scene.Cursor.HAND));

        return btn;
    }

    /**
     * Create a Ghost Button (Transparent/White with Green Border).
     */
    public static Button createGhostButton(String text) {
        Button btn = new Button(text);
        
        // Text: Sage Green
        btn.setTextFill(PRIMARY_COLOR);
        btn.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 14));

        // Background: Transparent/White (Start with Transparent)
        btn.setBackground(new Background(new BackgroundFill(
            Color.TRANSPARENT, 
            new CornerRadii(10), 
            Insets.EMPTY
        )));

        // Border: Sage Green, 2px, 10px Radius
        btn.setBorder(new Border(new BorderStroke(
            PRIMARY_COLOR,
            BorderStrokeStyle.SOLID,
            new CornerRadii(10),
            new BorderWidths(2)
        )));

        btn.setPadding(new Insets(10, 20, 10, 20));

        // Interaction: Hover Effect
        btn.setOnMouseEntered(e -> btn.setBackground(new Background(new BackgroundFill(
            GHOST_HOVER_BG_COLOR, 
            new CornerRadii(10), 
            Insets.EMPTY
        ))));

        btn.setOnMouseExited(e -> btn.setBackground(new Background(new BackgroundFill(
            Color.TRANSPARENT, 
            new CornerRadii(10), 
            Insets.EMPTY
        ))));
        
        btn.setOnMouseMoved(e -> btn.setCursor(javafx.scene.Cursor.HAND));

        return btn;
    }

    /**
     * Create a Header Label.
     */
    public static Label createHeaderLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
        label.setTextFill(HEADER_TEXT_COLOR);
        return label;
    }
}

