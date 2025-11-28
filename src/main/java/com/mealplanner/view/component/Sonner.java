package com.mealplanner.view.component;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * A Toast notification manager (Sonner).
 * Corresponds to sonner.tsx
 */
public class Sonner {

    public enum Type {
        INFO, SUCCESS, ERROR, WARNING
    }

    // Helper for static access if needed, or instance based usage
    private Window owner;
    
    public Sonner() {
    }
    
    public Sonner(Window owner) {
        this.owner = owner;
    }
    
    public void setOwner(Window owner) {
        this.owner = owner;
    }

    public void show(String title, String message, Type type) {
        // If owner is null, try to find active window or just return
        if (owner == null) {
            Window active = Window.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);
            if (active == null) return;
            owner = active;
        }
        
        Popup popup = new Popup();
        
        StackPane container = new StackPane();
        container.getStyleClass().add("toast-container");
        // Apply type specific class
        switch (type) {
            case ERROR: container.getStyleClass().add("toast-error"); break;
            case SUCCESS: container.getStyleClass().add("toast-success"); break;
            default: container.getStyleClass().add("toast-info"); break;
        }
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("toast-title");
        
        Label msgLabel = new Label(message);
        msgLabel.getStyleClass().add("toast-message");
        
        javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(5, titleLabel, msgLabel);
        box.setAlignment(Pos.CENTER_LEFT);
        
        container.getChildren().add(box);
        
        popup.getContent().add(container);
        popup.setAutoHide(true);
        
        // Show at bottom right of owner
        popup.show(owner, 
            owner.getX() + owner.getWidth() - 350, 
            owner.getY() + owner.getHeight() - 100);
            
        // Fade in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), container);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        // Auto hide after 3 seconds
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), container);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> popup.hide());
            fadeOut.play();
        });
        delay.play();
    }

    // Legacy support if needed, or just update existing usages
    public static void show(Window owner, String message, boolean isError) {
        new Sonner(owner).show(isError ? "Error" : "Info", message, isError ? Type.ERROR : Type.INFO);
    }
}
