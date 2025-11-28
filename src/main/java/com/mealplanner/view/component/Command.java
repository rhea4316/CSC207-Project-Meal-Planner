package com.mealplanner.view.component;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * A Command Palette component (Search + List).
 * Corresponds to command.tsx
 */
public class Command extends VBox {

    private final TextField searchInput;
    private final ListView<String> commandList;

    public Command() {
        getStyleClass().add("command-dialog");
        setPadding(new Insets(10));
        setSpacing(10);

        // Search Input
        searchInput = new TextField();
        searchInput.setPromptText("Type a command or search...");
        searchInput.getStyleClass().add("command-input");
        
        // List
        commandList = new ListView<>();
        commandList.getStyleClass().add("command-list");
        VBox.setVgrow(commandList, Priority.ALWAYS);

        getChildren().addAll(searchInput, commandList);
    }

    public void setItems(ObservableList<String> items) {
        commandList.setItems(items);
    }
}

