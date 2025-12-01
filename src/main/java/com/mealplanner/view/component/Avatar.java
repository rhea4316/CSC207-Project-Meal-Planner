package com.mealplanner.view.component;

import com.mealplanner.util.ImageCacheManager;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

/**
 * A circular Avatar component that displays an image or fallback text.
 * Corresponds to avatar.tsx
 */
public class Avatar extends StackPane {
    
    private static final ImageCacheManager imageCache = ImageCacheManager.getInstance();
    
    private final Circle shape;
    private final Label fallbackLabel;

    public Avatar(double radius) {
        this.shape = new Circle(radius);
        this.shape.getStyleClass().add("avatar-shape");
        
        this.fallbackLabel = new Label();
        this.fallbackLabel.getStyleClass().add("avatar-fallback");
        
        // Default state: fallback visible, generic background
        this.shape.setFill(Color.web("#ececf0")); // -fx-theme-muted default
        
        getChildren().addAll(shape, fallbackLabel);
        getStyleClass().add("avatar");
    }

    public Avatar(double radius, String imageUrl, String fallbackText) {
        this(radius);
        setImage(imageUrl);
        setFallback(fallbackText);
    }

    public void setImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                Image image = imageCache.getImage(imageUrl);
                if (!image.isError()) {
                    shape.setFill(new ImagePattern(image));
                    fallbackLabel.setVisible(false);
                } else {
                    showFallback();
                }
            } catch (Exception e) {
                showFallback();
            }
        } else {
            showFallback();
        }
    }

    public void setFallback(String text) {
        fallbackLabel.setText(text);
    }

    private void showFallback() {
        shape.setFill(Color.web("#ececf0")); // Restore muted color
        fallbackLabel.setVisible(true);
    }
    
    /**
     * Sets the background color of the avatar circle.
     * @param color The color to set (e.g., "#68CA2A")
     */
    public void setBackgroundColor(String color) {
        if (color != null && !color.isEmpty()) {
            shape.setFill(Color.web(color));
        }
    }
}
