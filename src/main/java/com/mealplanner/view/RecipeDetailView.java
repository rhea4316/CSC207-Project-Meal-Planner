package com.mealplanner.view;

import com.mealplanner.entity.NutritionInfo;
import com.mealplanner.entity.Recipe;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.AdjustServingSizeController;
import com.mealplanner.interface_adapter.view_model.RecipeDetailViewModel;
import com.mealplanner.view.component.*;
// Removed component.Button/TextArea imports to resolve ambiguity, use standard controls with custom components mixed or fully qualify if needed.
// But here we intended to use the standard ones or wrappers. Let's stick to standard for Button/TextArea if wrappers not strictly needed, or use simple class names if unique.
// Wait, Textarea wrapper exists. Button wrapper does not exist (only standard Button styled).

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class RecipeDetailView extends BorderPane implements PropertyChangeListener {
    private final RecipeDetailViewModel viewModel;
    private final AdjustServingSizeController controller;
    private final ViewManagerModel viewManagerModel;

    // UI Components
    private Label recipeNameLabel;
    private Label servingSizeValueLabel;
    private VBox ingredientsPanel;
    private com.mealplanner.view.component.Textarea instructionsArea;
    private com.mealplanner.view.component.Textarea nutritionArea;
    private Label errorLabel;
    private com.mealplanner.view.component.Slider servingSlider;

    public RecipeDetailView(RecipeDetailViewModel viewModel, AdjustServingSizeController controller, ViewManagerModel viewManagerModel) {
        if (viewModel == null) throw new IllegalArgumentException("ViewModel cannot be null");
        if (controller == null) throw new IllegalArgumentException("Controller cannot be null");
        
        this.viewModel = viewModel;
        this.controller = controller;
        this.viewManagerModel = viewManagerModel;

        viewModel.addPropertyChangeListener(this);

        // Root Style
        getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));
        
        createHeader();
        createMainContent();
    }

    private void createHeader() {
        VBox headerContainer = new VBox(10);
        
        // Breadcrumb
        List<Breadcrumb.Item> items = new ArrayList<>();
        items.add(new Breadcrumb.Item("Recipes", () -> viewManagerModel.setActiveView(ViewManager.BROWSE_RECIPE_VIEW)));
        items.add(new Breadcrumb.Item("Detail", null));
        
        Breadcrumb breadcrumb = new Breadcrumb(items);
        
        HBox headerPanel = new HBox(20);
        headerPanel.setAlignment(Pos.CENTER_LEFT);

        recipeNameLabel = new Label("Recipe Name");
        recipeNameLabel.getStyleClass().add("section-title");
        recipeNameLabel.setStyle("-fx-font-size: 24px;"); 

        headerPanel.getChildren().addAll(recipeNameLabel);

        headerContainer.getChildren().addAll(breadcrumb, headerPanel, new com.mealplanner.view.component.Separator());
        setTop(headerContainer);
    }

    private void createMainContent() {
        GridPane mainPanel = new GridPane();
        mainPanel.setHgap(30);
        mainPanel.setVgap(20);
        mainPanel.setPadding(new Insets(20, 0, 0, 0));
        
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(40);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(60);
        mainPanel.getColumnConstraints().addAll(col1, col2);

        // --- LEFT PANEL ---
        VBox leftPanel = new VBox(20);
        leftPanel.getStyleClass().add("card-panel"); 
        
        // Image Placeholder
        Skeleton imagePanel = new Skeleton(300, 250);
        leftPanel.getChildren().add(imagePanel);

        // Nutrition
        VBox infoPanel = new VBox(10);
        Label nutritionTitle = new Label("Nutrition Facts");
        nutritionTitle.getStyleClass().add("section-title");
        
        nutritionArea = new com.mealplanner.view.component.Textarea();
        nutritionArea.setEditable(false);
        nutritionArea.setPrefRowCount(6);
        nutritionArea.getStyleClass().add("nutrition-area");
        
        infoPanel.getChildren().addAll(nutritionTitle, nutritionArea);
        leftPanel.getChildren().add(infoPanel);
        
        mainPanel.add(leftPanel, 0, 0);

        // --- RIGHT PANEL ---
        VBox rightPanel = new VBox(20);
        rightPanel.getStyleClass().add("card-panel");

        // 1. Serving Size (Slider)
        VBox servingPanel = new VBox(10);
        
        Label servingLabel = new Label("Adjust Servings:");
        servingLabel.getStyleClass().add("section-title");
        
        HBox sliderContainer = new HBox(15);
        sliderContainer.setAlignment(Pos.CENTER_LEFT);
        
        servingSlider = new com.mealplanner.view.component.Slider(1, 10, 1);
        servingSlider.setPrefWidth(300);
        servingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int newSize = newVal.intValue();
            servingSizeValueLabel.setText(String.valueOf(newSize));
            // Debounce logic typically needed, but calling direct for demo
            adjustServingSize(newSize);
        });
        
        servingSizeValueLabel = new Label("1");
        servingSizeValueLabel.getStyleClass().add("serving-size-label");
        
        sliderContainer.getChildren().addAll(servingSlider, servingSizeValueLabel);
        servingPanel.getChildren().addAll(servingLabel, sliderContainer);
        rightPanel.getChildren().add(servingPanel);

        // 2. Tabs for Ingredients & Instructions
        Tabs tabs = new Tabs();
        
        // Ingredients Tab
        VBox ingredientsContainer = new VBox(10);
        ingredientsContainer.setPadding(new Insets(15, 0, 0, 0));
        ingredientsPanel = new VBox(8); 
        ScrollArea ingScroll = new ScrollArea(ingredientsPanel);
        ingScroll.setFitToWidth(true);
        ingScroll.setPrefHeight(300);
        ingredientsContainer.getChildren().add(ingScroll);
        
        tabs.addTab("Ingredients", ingredientsContainer);

        // Instructions Tab
        VBox instructionsContainer = new VBox(10);
        instructionsContainer.setPadding(new Insets(15, 0, 0, 0));
        instructionsArea = new com.mealplanner.view.component.Textarea();
        instructionsArea.setWrapText(true);
        instructionsArea.setEditable(false);
        instructionsArea.setPrefHeight(300);
        instructionsContainer.getChildren().add(instructionsArea);
        
        tabs.addTab("Instructions", instructionsContainer);

        rightPanel.getChildren().add(tabs);
        
        VBox.setVgrow(tabs, Priority.ALWAYS);

        mainPanel.add(rightPanel, 1, 0);
        
        // Allow right panel to grow vertically
        RowConstraints row1 = new RowConstraints();
        row1.setVgrow(Priority.ALWAYS);
        mainPanel.getRowConstraints().add(row1);
        
        setCenter(mainPanel);
        
        // Error Label
        errorLabel = new Label(" ");
        errorLabel.getStyleClass().add("error-label");
        setBottom(errorLabel);
    }

    private void adjustServingSize(int newSize) {
        Recipe recipe = viewModel.getRecipe();
        if (recipe != null) {
            controller.execute(recipe.getRecipeId(), newSize);
        }
    }

    private void updateIngredientsList(List<String> ingredients) {
        ingredientsPanel.getChildren().clear();
        if (ingredients != null) {
            for (String ing : ingredients) {
                StyledCheckbox checkBox = new StyledCheckbox(ing);
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
                    int size = viewModel.getServingSize();
                    servingSizeValueLabel.setText(String.valueOf(size));
                    servingSlider.setValue(size);
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
