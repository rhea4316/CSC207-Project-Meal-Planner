package com.mealplanner.view;

import com.mealplanner.entity.NutritionInfo;
import com.mealplanner.entity.Recipe;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.AdjustServingSizeController;
import com.mealplanner.interface_adapter.view_model.RecipeDetailViewModel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class RecipeDetailView extends BorderPane implements PropertyChangeListener {
    private final RecipeDetailViewModel viewModel;
    private final AdjustServingSizeController controller;
    private final ViewManagerModel viewManagerModel;

    // UI Components
    private Label recipeNameLabel;
    private Label servingSizeValueLabel;
    private VBox ingredientsPanel;
    private TextArea instructionsArea;
    private TextArea nutritionArea;
    private Label errorLabel;

    public RecipeDetailView(RecipeDetailViewModel viewModel, AdjustServingSizeController controller, ViewManagerModel viewManagerModel) {
        if (viewModel == null) throw new IllegalArgumentException("ViewModel cannot be null");
        if (controller == null) throw new IllegalArgumentException("Controller cannot be null");
        
        this.viewModel = viewModel;
        this.controller = controller;
        this.viewManagerModel = viewManagerModel;

        viewModel.addPropertyChangeListener(this);

        setPadding(new Insets(20));
        getStyleClass().add("bg-white");
        
        createHeader();
        createMainContent();
    }

    private void createHeader() {
        BorderPane headerPanel = new BorderPane();
        headerPanel.setPadding(new Insets(0, 0, 20, 0));

        Button backButton = new Button("← Back");
        backButton.getStyleClass().add("secondary-button");
        backButton.setOnAction(e -> {
            if (viewManagerModel != null) {
                viewManagerModel.setActiveView(ViewManager.BROWSE_RECIPE_VIEW);
            }
        });
        headerPanel.setLeft(backButton);

        recipeNameLabel = new Label("Recipe Name");
        recipeNameLabel.getStyleClass().add("title-label");
        recipeNameLabel.setAlignment(Pos.CENTER);
        BorderPane.setAlignment(recipeNameLabel, Pos.CENTER);
        headerPanel.setCenter(recipeNameLabel);

        setTop(headerPanel);
    }

    private void createMainContent() {
        GridPane mainPanel = new GridPane();
        mainPanel.setHgap(40);
        
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(40);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(60);
        mainPanel.getColumnConstraints().addAll(col1, col2);

        // --- LEFT PANEL ---
        VBox leftPanel = new VBox(20);
        
        // Image Placeholder
        Region imagePanel = new Region();
        imagePanel.getStyleClass().add("image-panel");
        imagePanel.setPrefHeight(250);
        leftPanel.getChildren().add(imagePanel);

        // Nutrition
        VBox infoPanel = new VBox(10);
        Label nutritionTitle = new Label("Nutrition Facts");
        nutritionTitle.getStyleClass().add("section-title");
        
        nutritionArea = new TextArea();
        nutritionArea.setEditable(false);
        nutritionArea.setPrefRowCount(6);
        nutritionArea.getStyleClass().add("nutrition-area");
        
        infoPanel.getChildren().addAll(nutritionTitle, nutritionArea);
        leftPanel.getChildren().add(infoPanel);
        
        mainPanel.add(leftPanel, 0, 0);


        // --- RIGHT PANEL ---
        VBox rightPanel = new VBox(20);

        // 1. Serving Size
        HBox servingPanel = new HBox(15);
        servingPanel.setAlignment(Pos.CENTER_LEFT);
        
        Label servingLabel = new Label("Serving Size: ");
        servingLabel.getStyleClass().add("section-title");
        
        Button minusBtn = new Button("-");
        minusBtn.getStyleClass().add("secondary-button");
        minusBtn.setOnAction(e -> adjustServingSize(-1));
        
        servingSizeValueLabel = new Label("1");
        servingSizeValueLabel.getStyleClass().add("serving-size-label");
        
        Button plusBtn = new Button("+");
        plusBtn.getStyleClass().add("secondary-button");
        plusBtn.setOnAction(e -> adjustServingSize(1));

        servingPanel.getChildren().addAll(servingLabel, minusBtn, servingSizeValueLabel, plusBtn);
        rightPanel.getChildren().add(servingPanel);

        // 2. Ingredients
        VBox ingredientsContainer = new VBox(10);
        Label ingTitle = new Label("Ingredients");
        ingTitle.getStyleClass().add("section-title");
        
        ingredientsPanel = new VBox(5);
        ScrollPane ingScroll = new ScrollPane(ingredientsPanel);
        ingScroll.setFitToWidth(true);
        ingScroll.setPrefHeight(200);
        
        ingredientsContainer.getChildren().addAll(ingTitle, ingScroll);
        rightPanel.getChildren().add(ingredientsContainer);

        // 3. Instructions
        VBox instructionsContainer = new VBox(10);
        Label instTitle = new Label("Instructions");
        instTitle.getStyleClass().add("section-title");
        
        instructionsArea = new TextArea();
        instructionsArea.setWrapText(true);
        instructionsArea.setEditable(false);
        
        instructionsContainer.getChildren().addAll(instTitle, instructionsArea);
        rightPanel.getChildren().add(instructionsContainer);
        
        // Expand instructions to fill
        VBox.setVgrow(instructionsContainer, Priority.ALWAYS);
        VBox.setVgrow(instructionsArea, Priority.ALWAYS);

        mainPanel.add(rightPanel, 1, 0);
        
        setCenter(mainPanel);
        
        // Error Label
        errorLabel = new Label(" ");
        errorLabel.getStyleClass().add("error-label");
        setBottom(errorLabel);
    }

    private void adjustServingSize(int delta) {
        try {
            int current = Integer.parseInt(servingSizeValueLabel.getText());
            int next = current + delta;
            if (next < 1) next = 1;
            
            Recipe recipe = viewModel.getRecipe();
            if (recipe != null) {
                controller.execute(recipe.getRecipeId(), next);
            }
        } catch (NumberFormatException ex) {
            // Ignore
        }
    }

    private void updateIngredientsList(List<String> ingredients) {
        ingredientsPanel.getChildren().clear();
        if (ingredients != null) {
            for (String ing : ingredients) {
                CheckBox checkBox = new CheckBox(ing);
                checkBox.setWrapText(true);
                ingredientsPanel.getChildren().add(checkBox);
            }
        }
    }

    private void updateNutritionDisplay(NutritionInfo info) {
        if (info == null) {
            nutritionArea.setText("No nutrition info.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Calories:  %d\n", info.getCalories()));
        sb.append(String.format("Protein:   %.1f g\n", info.getProtein()));
        sb.append(String.format("Carbs:     %.1f g\n", info.getCarbs()));
        sb.append(String.format("Fat:       %.1f g\n", info.getFat()));
        nutritionArea.setText(sb.toString());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(() -> {
            String prop = evt.getPropertyName();
            switch (prop) {
                case RecipeDetailViewModel.PROP_RECIPE:
                    Recipe r = viewModel.getRecipe();
                    if (r != null) {
                        recipeNameLabel.setText(r.getName());
                        instructionsArea.setText(r.getSteps());
                    } else {
                        recipeNameLabel.setText("No Recipe Selected");
                    }
                    break;
                case RecipeDetailViewModel.PROP_SERVING_SIZE:
                    servingSizeValueLabel.setText(String.valueOf(viewModel.getServingSize()));
                    break;
                case RecipeDetailViewModel.PROP_INGREDIENTS:
                    updateIngredientsList(viewModel.getIngredients());
                    break;
                case RecipeDetailViewModel.PROP_NUTRITION:
                    updateNutritionDisplay(viewModel.getNutrition());
                    break;
                case RecipeDetailViewModel.PROP_ERROR_MESSAGE:
                    String err = viewModel.getErrorMessage();
                    errorLabel.setText(err == null ? " " : err);
                    break;
            }
        });
    }
}
