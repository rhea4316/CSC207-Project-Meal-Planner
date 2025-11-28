package com.mealplanner.view.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * A styled Dialog component.
 * Corresponds to dialog.tsx
 */
public class Dialog {

    private final Stage stage;
    private final VBox contentBox;
    private final Label titleLabel;
    private final Label descriptionLabel;

    public Dialog(Stage owner, String title, String description) {
        stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);

        // Main Container
        StackPane root = new StackPane();
        root.getStyleClass().add("dialog-overlay");
        root.setAlignment(Pos.CENTER);

        // Content Box
        contentBox = new VBox(10);
        contentBox.getStyleClass().add("dialog-content");
        contentBox.setMaxWidth(500);
        contentBox.setMaxHeight(400);
        contentBox.setPadding(new Insets(20));

        // Header
        titleLabel = new Label(title);
        titleLabel.getStyleClass().add("dialog-title");
        
        descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("dialog-description");
        descriptionLabel.setWrapText(true);

        VBox header = new VBox(5, titleLabel, descriptionLabel);
        header.getStyleClass().add("dialog-header");
        
        contentBox.getChildren().add(header);
        
        root.getChildren().add(contentBox);

        // Close on background click (optional, but common in web dialogs)
        // root.setOnMouseClicked(e -> {
        //     if (e.getTarget() == root) stage.close();
        // });

        Scene scene = new Scene(root);
        scene.setFill(null); // Transparent scene
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        
        stage.setScene(scene);
    }

    public void setContent(Node content) {
        contentBox.getChildren().add(content);
    }

    public void addFooterButton(String text, Runnable action, boolean isPrimary) {
        Button btn = new Button(text);
        btn.getStyleClass().add(isPrimary ? "primary-button" : "secondary-button");
        btn.setOnAction(e -> {
            action.run();
            stage.close();
        });

        // Check if footer exists
        Node lastNode = contentBox.getChildren().isEmpty() ? null : contentBox.getChildren().get(contentBox.getChildren().size() - 1);
        HBox footer;
        if (lastNode instanceof HBox && 
            lastNode.getStyleClass().contains("dialog-footer")) {
            footer = (HBox) lastNode;
        } else {
            footer = new HBox(10);
            footer.getStyleClass().add("dialog-footer");
            footer.setAlignment(Pos.CENTER_RIGHT);
            contentBox.getChildren().add(footer);
        }
        footer.getChildren().add(btn);
    }

    public void show() {
        stage.showAndWait();
    }

    public void close() {
        stage.close();
    }
}

