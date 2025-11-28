package com.mealplanner.view;

import com.mealplanner.entity.Unit;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.StoreRecipeController;
import com.mealplanner.interface_adapter.view_model.RecipeStoreViewModel;
import com.mealplanner.util.NumberUtil;
import com.mealplanner.util.StringUtil;
import com.mealplanner.view.component.*;
// Remove ambiguous Button import, use full class names or standard Button

import javafx.application.Platform;
import javafx.geometry.Insets;
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

    private Input nameField;
    private Input ingredientQtyField;
    private Select<Unit> unitCombo;
    private Input ingredientNameField;
    private ListView<String> ingredientList;
    private com.mealplanner.view.component.Textarea stepsArea;
    private Input servingSizeField;
    
    // Notifications
    private Sonner sonner;

    public StoreRecipeView(StoreRecipeController controller, RecipeStoreViewModel viewModel, ViewManagerModel viewManagerModel) {
        if (controller == null) throw new IllegalArgumentException("Controller cannot be null");
        
        this.controller = controller;
        this.viewModel = viewModel;
        this.viewManagerModel = viewManagerModel;

        if (viewModel != null) {
            viewModel.addPropertyChangeListener(this);
        }

        // Root Style
        getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        // Title
        Label titleLabel = new Label("Create New Recipe");
        titleLabel.getStyleClass().add("section-title");
        titleLabel.setStyle("-fx-font-size: 32px;"); 
        setTop(titleLabel);

        // Form
        createForm();
        
        // Setup Sonner for notifications
        sonner = new Sonner();
    }

    private void createForm() {
        VBox container = new VBox();
        container.getStyleClass().add("card-panel");
        container.setPadding(new Insets(30));
        container.setMaxWidth(800);
        
        Form form = new Form();
        form.setPadding(new Insets(0));

        // 1. Name
        nameField = new Input();
        nameField.setPromptText("Recipe Name (e.g., Grilled Chicken Salad)");
        form.addField("Recipe Name", nameField);

        // 2. Ingredients
        VBox ingSection = new VBox(10);
        Label ingLabel = new Label("Ingredients");
        ingLabel.getStyleClass().add("form-label");
        
        HBox ingBox = new HBox(10);
        
        ingredientQtyField = new Input();
        ingredientQtyField.setPromptText("Qty");
        ingredientQtyField.setPrefWidth(80);
        
        unitCombo = new Select<>();
        unitCombo.getItems().addAll(Unit.values());
        unitCombo.setPromptText("Unit");
        unitCombo.setPrefHeight(40);
        unitCombo.setPrefWidth(100);
        
        ingredientNameField = new Input();
        ingredientNameField.setPromptText("Ingredient Name");
        HBox.setHgrow(ingredientNameField, Priority.ALWAYS);
        
        javafx.scene.control.Button addIngBtn = new javafx.scene.control.Button("Add");
        addIngBtn.getStyleClass().add("secondary-button");
        addIngBtn.setPrefHeight(40);
        addIngBtn.setOnAction(e -> addIngredient());
        
        ingBox.getChildren().addAll(ingredientQtyField, unitCombo, ingredientNameField, addIngBtn);
        
        ingredientList = new ListView<>();
        ingredientList.setPrefHeight(150);
        ingredientList.getStyleClass().add("text-field"); 
        
        ingSection.getChildren().addAll(ingLabel, ingBox, ingredientList);
        form.getChildren().add(ingSection);

        // 3. Instructions
        stepsArea = new com.mealplanner.view.component.Textarea();
        stepsArea.setPromptText("Step 1: ...");
        stepsArea.setPrefRowCount(5);
        form.addField("Instructions", stepsArea);

        // 4. Serving Size
        servingSizeField = new Input("1");
        servingSizeField.setMaxWidth(100);
        form.addField("Serving Size", servingSizeField);

        // 5. Actions
        javafx.scene.control.Button saveBtn = new javafx.scene.control.Button("Save Recipe");
        saveBtn.getStyleClass().add("primary-button");
        saveBtn.setPrefHeight(45);
        saveBtn.setPrefWidth(200);
        saveBtn.setOnAction(e -> saveRecipe());
        
        HBox actionBox = new HBox(saveBtn);
        actionBox.setPadding(new Insets(20, 0, 0, 0));
        
        container.getChildren().addAll(form, actionBox);
        setCenter(container);
    }

    private void addIngredient() {
        String name = StringUtil.safeTrim(ingredientNameField.getText());
        if (StringUtil.isNullOrEmpty(name)) {
            sonner.show("Error", "Please enter an ingredient name", Sonner.Type.ERROR);
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
        if (StringUtil.isNullOrEmpty(name)) {
            sonner.show("Error", "Please enter a recipe name", Sonner.Type.ERROR);
            return;
        }
        
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
                sonner.show("Success", (String) evt.getNewValue(), Sonner.Type.SUCCESS);
            } else if (RecipeStoreViewModel.PROP_ERROR_MESSAGE.equals(evt.getPropertyName())) {
                sonner.show("Error", (String) evt.getNewValue(), Sonner.Type.ERROR);
            }
        });
    }
}
