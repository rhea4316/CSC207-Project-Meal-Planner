package com.mealplanner.view.component;

import com.mealplanner.entity.MealType;
import com.mealplanner.interface_adapter.controller.AddMealController;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;

public class AddToMealPlanDialog {

    private final Dialog dialog;
    private final AddMealController controller;
    private final String recipeId;
    private final Sonner sonner;

    private DatePicker datePicker;
    private ComboBox<MealType> mealTypeComboBox;

    public AddToMealPlanDialog(Stage owner, AddMealController controller, String recipeId, String recipeName) {
        this.controller = controller;
        this.recipeId = recipeId;

        this.dialog = new Dialog(owner, "Add to Meal Plan", "Schedule '" + recipeName + "' for a specific date and meal time.");
        this.sonner = new Sonner(owner);
        
        createForm();
        
        dialog.addFooterButton("Cancel", () -> {}, false); // Action handled by Dialog close
        dialog.addFooterButton("Add to Plan", this::onAdd, true);
    }

    private void createForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(10, 0, 0, 0));

        // Date Selection
        VBox dateBox = new VBox(5);
        Label dateLabel = new Label("Select Date");
        dateLabel.getStyleClass().add("text-gray-700");
        dateLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 14px;");
        
        datePicker = new DatePicker(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.getStyleClass().add("date-picker");
        // Format date picker if needed, but default is usually fine for selection
        
        dateBox.getChildren().addAll(dateLabel, datePicker);

        // Meal Type Selection
        VBox typeBox = new VBox(5);
        Label typeLabel = new Label("Select Meal Type");
        typeLabel.getStyleClass().add("text-gray-700");
        typeLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 14px;");
        
        mealTypeComboBox = new ComboBox<>();
        mealTypeComboBox.getItems().addAll(MealType.values());
        mealTypeComboBox.setValue(MealType.BREAKFAST);
        mealTypeComboBox.setMaxWidth(Double.MAX_VALUE);
        mealTypeComboBox.getStyleClass().add("combo-box");
        
        // Custom converter to show nice names if enum is ALL_CAPS
        mealTypeComboBox.setConverter(new StringConverter<MealType>() {
            @Override
            public String toString(MealType object) {
                if (object == null) return "";
                String name = object.name().toLowerCase();
                return name.substring(0, 1).toUpperCase() + name.substring(1);
            }

            @Override
            public MealType fromString(String string) {
                return null; // Not needed for read-only combo
            }
        });

        typeBox.getChildren().addAll(typeLabel, mealTypeComboBox);

        form.getChildren().addAll(dateBox, typeBox);
        dialog.setContent(form);
    }

    private void onAdd() {
        LocalDate date = datePicker.getValue();
        MealType type = mealTypeComboBox.getValue();
        
        if (date != null && type != null) {
            controller.execute(date.toString(), type.name(), recipeId);
            if (sonner != null) {
                String friendlyMeal = type.name().substring(0, 1).toUpperCase() + type.name().substring(1).toLowerCase();
                sonner.show("Added to Plan", friendlyMeal + " on " + date + " scheduled.", Sonner.Type.SUCCESS);
            }
            dialog.close();
        }
    }

    public void show() {
        dialog.show();
    }
}

