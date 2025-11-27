package com.mealplanner.view;

import com.mealplanner.entity.Unit;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.StoreRecipeController;
import com.mealplanner.interface_adapter.view_model.RecipeStoreViewModel;
import com.mealplanner.util.NumberUtil;
import com.mealplanner.util.StringUtil;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class StoreRecipeView extends BorderPane implements PropertyChangeListener {
    
    private final StoreRecipeController controller;
    @SuppressWarnings("unused")
    private final RecipeStoreViewModel viewModel;
    @SuppressWarnings("unused")
    private final ViewManagerModel viewManagerModel;

    private TextField nameField;
    private TextField ingredientQtyField;
    private ComboBox<Unit> unitCombo;
    private TextField ingredientNameField;
    private ListView<String> ingredientList;
    private TextArea stepsArea;
    private TextField servingSizeField;
    private Label statusLabel;

    public StoreRecipeView(StoreRecipeController controller, RecipeStoreViewModel viewModel, ViewManagerModel viewManagerModel) {
        if (controller == null) throw new IllegalArgumentException("Controller cannot be null");
        
        this.controller = controller;
        // viewModel and viewManagerModel are stored for potential future use (e.g., navigation, error display)
        this.viewModel = viewModel;
        this.viewManagerModel = viewManagerModel;

        if (viewModel != null) {
            viewModel.addPropertyChangeListener(this);
        }

        setPadding(new Insets(20));
        setStyle("-fx-background-color: white;");

        // Title
        Label titleLabel = new Label("Create New Recipe");
        titleLabel.getStyleClass().add("title-label");
        setTop(titleLabel);

        // Form
        createForm();
    }

    private void createForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        int row = 0;

        // Name
        grid.add(new Label("Recipe Name:"), 0, row);
        nameField = new TextField();
        grid.add(nameField, 1, row++);

        // Ingredient Input
        grid.add(new Label("Add Ingredient:"), 0, row);
        HBox ingBox = new HBox(5);
        ingredientQtyField = new TextField();
        ingredientQtyField.setPromptText("Qty");
        ingredientQtyField.setPrefWidth(50);
        
        unitCombo = new ComboBox<>();
        unitCombo.getItems().addAll(Unit.values());
        unitCombo.setPromptText("Unit");
        
        ingredientNameField = new TextField();
        ingredientNameField.setPromptText("Name");
        
        Button addIngBtn = new Button("Add");
        addIngBtn.getStyleClass().add("secondary-button");
        addIngBtn.setOnAction(e -> addIngredient());
        
        ingBox.getChildren().addAll(ingredientQtyField, unitCombo, ingredientNameField, addIngBtn);
        grid.add(ingBox, 1, row++);

        // Ingredient List
        grid.add(new Label("Ingredients:"), 0, row);
        ingredientList = new ListView<>();
        ingredientList.setPrefHeight(100);
        grid.add(ingredientList, 1, row++);

        // Steps
        grid.add(new Label("Instructions:"), 0, row);
        stepsArea = new TextArea();
        stepsArea.setPrefRowCount(5);
        stepsArea.setWrapText(true);
        grid.add(stepsArea, 1, row++);

        // Serving Size
        grid.add(new Label("Serving Size:"), 0, row);
        servingSizeField = new TextField("1");
        grid.add(servingSizeField, 1, row++);

        // Save Button
        Button saveBtn = new Button("Save Recipe");
        saveBtn.getStyleClass().add("modern-button");
        saveBtn.setOnAction(e -> saveRecipe());
        
        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(Pos.CENTER_LEFT);
        statusLabel = new Label("");
        bottomBox.getChildren().addAll(saveBtn, statusLabel);
        
        grid.add(bottomBox, 1, row);

        setCenter(grid);
    }

    private void addIngredient() {
        String name = StringUtil.safeTrim(ingredientNameField.getText());
        if (StringUtil.isNullOrEmpty(name)) {
            // Show validation error
            return;
        }
        
        String qty = StringUtil.safeTrim(ingredientQtyField.getText());
        Unit unit = unitCombo.getValue();
        
        String entry = String.format("%s %s %s", 
            StringUtil.isNullOrEmpty(qty) ? "" : qty, 
            unit != null ? unit.getAbbreviation() : "", 
            name).trim();
            
        ingredientList.getItems().add(entry);
        
        ingredientQtyField.clear();
        ingredientNameField.clear();
        unitCombo.getSelectionModel().clearSelection();
    }

    private void saveRecipe() {
        String name = StringUtil.safeTrim(nameField.getText());
        List<String> ingredients = new ArrayList<>(ingredientList.getItems());
        String stepsRaw = stepsArea.getText();
        List<String> steps = new ArrayList<>();
        
        if (stepsRaw != null) {
            for (String s : stepsRaw.split("\\n")) {
                if (!s.isBlank()) steps.add(s.trim());
            }
        }

        int servingSize = NumberUtil.parseInt(servingSizeField.getText(), 1);
        if (servingSize <= 0) servingSize = 1;

        controller.execute(name, ingredients, steps, servingSize);
        clearForm();
    }

    private void clearForm() {
        nameField.clear();
        ingredientList.getItems().clear();
        stepsArea.clear();
        servingSizeField.setText("1");
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(() -> {
            if (RecipeStoreViewModel.PROP_SUCCESS_MESSAGE.equals(evt.getPropertyName())) {
                statusLabel.setText((String) evt.getNewValue());
                statusLabel.setStyle("-fx-text-fill: green;");
            } else if (RecipeStoreViewModel.PROP_ERROR_MESSAGE.equals(evt.getPropertyName())) {
                statusLabel.setText((String) evt.getNewValue());
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        });
    }
}
