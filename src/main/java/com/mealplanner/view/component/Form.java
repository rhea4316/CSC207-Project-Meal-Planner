package com.mealplanner.view.component;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Form layout helper.
 * Corresponds to form.tsx
 */
public class Form extends VBox {

    public Form() {
        setSpacing(15);
        setPadding(new Insets(20));
        getStyleClass().add("form-container");
    }

    public void addField(String labelText, Node inputField) {
        VBox fieldContainer = new VBox(5);
        
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");
        
        fieldContainer.getChildren().addAll(label, inputField);
        getChildren().add(fieldContainer);
    }
    
    public void addField(String labelText, Node inputField, String errorMessage) {
        VBox fieldContainer = new VBox(5);
        
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");
        
        Label errorLabel = new Label(errorMessage);
        errorLabel.getStyleClass().add("form-message"); // Error style
        errorLabel.setVisible(errorMessage != null && !errorMessage.isEmpty());
        
        fieldContainer.getChildren().addAll(label, inputField, errorLabel);
        getChildren().add(fieldContainer);
    }
}

