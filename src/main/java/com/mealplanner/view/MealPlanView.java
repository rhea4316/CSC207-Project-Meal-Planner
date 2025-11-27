package com.mealplanner.view;

import com.mealplanner.entity.MealType;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.AddMealController;
import com.mealplanner.interface_adapter.controller.DeleteMealController;
import com.mealplanner.interface_adapter.controller.EditMealController;
import com.mealplanner.interface_adapter.view_model.MealPlanViewModel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class MealPlanView extends BorderPane implements PropertyChangeListener {
    private final MealPlanViewModel mealPlanViewModel;
    private final ViewManagerModel viewManagerModel;
    private final AddMealController addMealController;
    private final EditMealController editMealController;
    private final DeleteMealController deleteMealController;

    private DatePicker datePicker;
    private ComboBox<MealType> mealTypeComboBox;
    private TextField recipeIdField;
    private Label errorLabel;
    private Label successLabel;
    private VBox mealsListBox;

    public MealPlanView(MealPlanViewModel mealPlanViewModel, ViewManagerModel viewManagerModel,
                       AddMealController addMealController, EditMealController editMealController,
                       DeleteMealController deleteMealController) {
        if (mealPlanViewModel == null) throw new IllegalArgumentException("MealPlanViewModel cannot be null");
        if (viewManagerModel == null) throw new IllegalArgumentException("ViewManagerModel cannot be null");
        
        this.mealPlanViewModel = mealPlanViewModel;
        this.viewManagerModel = viewManagerModel;
        this.addMealController = addMealController;
        this.editMealController = editMealController;
        this.deleteMealController = deleteMealController;

        this.mealPlanViewModel.addPropertyChangeListener(this);

        setPadding(new Insets(20));
        setStyle("-fx-background-color: #F5F5F5;");

        createHeader();
        createForm();
        createMealsList();
        createFooter();
        
        updateView();
    }

    private void createHeader() {
        Label titleLabel = new Label("Manage Meal Plan");
        titleLabel.getStyleClass().add("title-label");
        setTop(titleLabel);
    }

    private void createForm() {
        VBox formBox = new VBox(15);
        formBox.getStyleClass().add("card-panel");
        formBox.setPadding(new Insets(20));
        formBox.setMaxWidth(500);

        Label formTitle = new Label("Add/Edit/Delete Meal");
        formTitle.getStyleClass().add("section-title");
        formBox.getChildren().add(formTitle);

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(15);
        formGrid.setAlignment(Pos.CENTER_LEFT);

        // Date Picker
        formGrid.add(new Label("Date:"), 0, 0);
        datePicker = new DatePicker(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);
        formGrid.add(datePicker, 1, 0);

        // Meal Type ComboBox
        formGrid.add(new Label("Meal Type:"), 0, 1);
        mealTypeComboBox = new ComboBox<>();
        mealTypeComboBox.getItems().addAll(MealType.values());
        mealTypeComboBox.setValue(MealType.BREAKFAST);
        mealTypeComboBox.setMaxWidth(Double.MAX_VALUE);
        formGrid.add(mealTypeComboBox, 1, 1);

        // Recipe ID Field
        formGrid.add(new Label("Recipe ID:"), 0, 2);
        recipeIdField = new TextField();
        recipeIdField.getStyleClass().add("text-field");
        recipeIdField.setPromptText("Enter recipe ID");
        recipeIdField.setMaxWidth(Double.MAX_VALUE);
        formGrid.add(recipeIdField, 1, 2);

        // Column constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        formGrid.getColumnConstraints().addAll(col1, col2);

        formBox.getChildren().add(formGrid);

        // Action Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button addButton = new Button("Add Meal");
        addButton.getStyleClass().add("modern-button");
        addButton.setOnAction(e -> performAddMeal());

        Button editButton = new Button("Edit Meal");
        editButton.getStyleClass().add("action-button");
        editButton.setOnAction(e -> performEditMeal());

        Button deleteButton = new Button("Delete Meal");
        deleteButton.getStyleClass().add("secondary-button");
        deleteButton.setOnAction(e -> performDeleteMeal());

        buttonBox.getChildren().addAll(addButton, editButton, deleteButton);
        formBox.getChildren().add(buttonBox);

        // Status Labels
        errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");
        successLabel = new Label("");
        successLabel.setStyle("-fx-text-fill: green;");
        
        VBox statusBox = new VBox(5);
        statusBox.getChildren().addAll(errorLabel, successLabel);
        formBox.getChildren().add(statusBox);

        setLeft(formBox);
    }

    private void createMealsList() {
        VBox listBox = new VBox(10);
        listBox.getStyleClass().add("card-panel");
        listBox.setPadding(new Insets(20));
        listBox.setMinWidth(400);

        Label listTitle = new Label("Current Meals");
        listTitle.getStyleClass().add("section-title");
        listBox.getChildren().add(listTitle);

        mealsListBox = new VBox(10);
        mealsListBox.setSpacing(10);
        listBox.getChildren().add(mealsListBox);

        ScrollPane scrollPane = new ScrollPane(mealsListBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        listBox.getChildren().set(1, scrollPane);

        setCenter(listBox);
    }

    private void createFooter() {
        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.CENTER_RIGHT);
        footerBox.setPadding(new Insets(20, 0, 0, 0));

        Button backButton = new Button("Back to Dashboard");
        backButton.getStyleClass().add("secondary-button");
        backButton.setOnAction(e -> {
            if (viewManagerModel != null) {
                viewManagerModel.setActiveView(ViewManager.DASHBOARD_VIEW);
            }
        });

        footerBox.getChildren().add(backButton);
        setBottom(footerBox);
    }

    private void performAddMeal() {
        LocalDate date = datePicker.getValue();
        MealType mealType = mealTypeComboBox.getValue();
        String recipeId = recipeIdField.getText().trim();

        if (date == null) {
            errorLabel.setText("Please select a date");
            return;
        }
        if (mealType == null) {
            errorLabel.setText("Please select a meal type");
            return;
        }
        if (recipeId.isEmpty()) {
            errorLabel.setText("Please enter a recipe ID");
            return;
        }

        errorLabel.setText("");
        addMealController.execute(date.toString(), mealType.name(), recipeId);
    }

    private void performEditMeal() {
        LocalDate date = datePicker.getValue();
        MealType mealType = mealTypeComboBox.getValue();
        String recipeId = recipeIdField.getText().trim();

        if (date == null) {
            errorLabel.setText("Please select a date");
            return;
        }
        if (mealType == null) {
            errorLabel.setText("Please select a meal type");
            return;
        }
        if (recipeId.isEmpty()) {
            errorLabel.setText("Please enter a recipe ID");
            return;
        }

        errorLabel.setText("");
        editMealController.execute(date.toString(), mealType.name(), recipeId);
    }

    private void performDeleteMeal() {
        LocalDate date = datePicker.getValue();
        MealType mealType = mealTypeComboBox.getValue();

        if (date == null) {
            errorLabel.setText("Please select a date");
            return;
        }
        if (mealType == null) {
            errorLabel.setText("Please select a meal type");
            return;
        }

        errorLabel.setText("");
        deleteMealController.execute(date.toString(), mealType.name());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(this::updateView);
    }

    private void updateView() {
        // Update error/success messages
        String error = mealPlanViewModel.getErrorMessage();
        String success = mealPlanViewModel.getSuccessMessage();
        
        if (error != null && !error.isEmpty()) {
            errorLabel.setText(error);
            successLabel.setText("");
        } else if (success != null && !success.isEmpty()) {
            successLabel.setText(success);
            errorLabel.setText("");
        } else {
            errorLabel.setText("");
            successLabel.setText("");
        }

        // Update meals list
        mealsListBox.getChildren().clear();
        
        Map<LocalDate, Map<MealType, String>> weeklyMeals = mealPlanViewModel.getWeeklyMeals();
        if (weeklyMeals != null && !weeklyMeals.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
            
            for (Map.Entry<LocalDate, Map<MealType, String>> dateEntry : weeklyMeals.entrySet()) {
                LocalDate date = dateEntry.getKey();
                Map<MealType, String> meals = dateEntry.getValue();
                
                if (meals != null && !meals.isEmpty()) {
                    VBox dateBox = new VBox(5);
                    dateBox.getStyleClass().add("card-panel");
                    dateBox.setPadding(new Insets(10));
                    
                    Label dateLabel = new Label(date.format(formatter));
                    dateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    dateBox.getChildren().add(dateLabel);
                    
                    for (Map.Entry<MealType, String> mealEntry : meals.entrySet()) {
                        HBox mealRow = new HBox(10);
                        mealRow.setAlignment(Pos.CENTER_LEFT);
                        
                        Label mealTypeLabel = new Label(mealEntry.getKey().toString() + ":");
                        mealTypeLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 80;");
                        
                        Label recipeLabel = new Label(mealEntry.getValue());
                        recipeLabel.setWrapText(true);
                        
                        mealRow.getChildren().addAll(mealTypeLabel, recipeLabel);
                        dateBox.getChildren().add(mealRow);
                    }
                    
                    mealsListBox.getChildren().add(dateBox);
                }
            }
        } else {
            Label emptyLabel = new Label("No meals planned yet");
            emptyLabel.setStyle("-fx-text-fill: gray;");
            mealsListBox.getChildren().add(emptyLabel);
        }
    }
}

