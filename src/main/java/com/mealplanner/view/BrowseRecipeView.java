package com.mealplanner.view;

import com.mealplanner.entity.Recipe;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.BrowseRecipeController;
import com.mealplanner.interface_adapter.view_model.RecipeBrowseViewModel;
import com.mealplanner.util.StringUtil;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;

public class BrowseRecipeView extends BorderPane implements PropertyChangeListener {
    private final RecipeBrowseViewModel viewModel;
    private final BrowseRecipeController controller;
    private final ViewManagerModel viewManagerModel;

    private TextField queryField;
    private TextField ingredientsField;
    private Spinner<Integer> resultsSpinner;
    private TextArea resultsArea;
    private Label errorLabel;

    public BrowseRecipeView(RecipeBrowseViewModel viewModel, BrowseRecipeController controller, ViewManagerModel viewManagerModel) {
        if (viewModel == null) throw new IllegalArgumentException("ViewModel cannot be null");
        if (controller == null) throw new IllegalArgumentException("Controller cannot be null");
        
        this.viewModel = viewModel;
        this.controller = controller;
        // viewManagerModel stored for navigation if needed in future
        this.viewManagerModel = viewManagerModel;

        viewModel.addPropertyChangeListener(this);

        setPadding(new Insets(20));
        setStyle("-fx-background-color: white;");

        // Title
        Label titleLabel = new Label("Browse Recipes");
        titleLabel.getStyleClass().add("title-label");
        
        VBox topBox = new VBox(10);
        topBox.getChildren().addAll(titleLabel, createSearchPanel());
        setTop(topBox);

        // Results
        createResultsPanel();
        
        // Error
        errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");
        setBottom(errorLabel);
    }

    private GridPane createSearchPanel() {
        GridPane panel = new GridPane();
        panel.setHgap(10);
        panel.setVgap(10);
        
        // Query
        panel.add(new Label("Search Query:"), 0, 0);
        queryField = new TextField();
        panel.add(queryField, 1, 0);

        // Ingredients
        panel.add(new Label("Ingredients (comma-separated):"), 0, 1);
        ingredientsField = new TextField();
        ingredientsField.setPromptText("Optional");
        panel.add(ingredientsField, 1, 1);

        // Number of Results
        panel.add(new Label("Number of Results:"), 0, 2);
        resultsSpinner = new Spinner<>(1, 100, 1);
        resultsSpinner.setEditable(true);
        panel.add(resultsSpinner, 1, 2);

        // Button
        Button searchButton = new Button("Search");
        searchButton.getStyleClass().add("modern-button");
        searchButton.setOnAction(e -> performSearch());
        
        HBox btnBox = new HBox(searchButton);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        panel.add(btnBox, 1, 3);

        return panel;
    }

    private void createResultsPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(20, 0, 0, 0));
        
        Label resultsLabel = new Label("Search Results:");
        resultsLabel.getStyleClass().add("section-title");
        
        resultsArea = new TextArea();
        resultsArea.setEditable(false);
        resultsArea.setWrapText(true);
        
        panel.getChildren().addAll(resultsLabel, resultsArea);
        setCenter(panel);
    }

    private void performSearch() {
        String query = StringUtil.safeTrim(queryField.getText());
        String ingredients = StringUtil.safeTrim(ingredientsField.getText());
        int numberOfRecipes = resultsSpinner.getValue();

        if (StringUtil.isNullOrEmpty(query)) {
            errorLabel.setText("Please enter a search query");
            return;
        }
        
        errorLabel.setText("");

        // Ideally execute in background thread like SearchByIngredientsController
        // For now, just wrap logic
        new Thread(() -> {
            try {
                if (StringUtil.isNullOrEmpty(ingredients)) {
                    controller.execute(query, numberOfRecipes);
                } else {
                    controller.execute(query, numberOfRecipes, ingredients);
                }
            } catch (IOException ex) {
                Platform.runLater(() -> viewModel.setErrorMessage("Network error: " + ex.getMessage()));
            }
        }).start();
    }

    private void displayRecipes(List<Recipe> recipes) {
        if (recipes == null) {
            resultsArea.setText("No recipes available.");
            return;
        }
        
        StringBuilder results = new StringBuilder();
        if (recipes.isEmpty()) {
            results.append("No recipes found.");
        } else {
            for (Recipe recipe : recipes) {
                results.append("Recipe: ").append(recipe.getName()).append("\n");
                results.append("Serving Size: ").append(recipe.getServingSize()).append("\n");
                if (recipe.getIngredients() != null) {
                    results.append("Ingredients: ").append(recipe.getIngredients()).append("\n");
                }
                results.append("\n--------------------------------------------------\n");
            }
        }
        resultsArea.setText(results.toString());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(() -> {
            String prop = evt.getPropertyName();
            switch (prop) {
                case "recipes":
                    displayRecipes(viewModel.getRecipes());
                    break;
                case "errorMessage":
                    String msg = viewModel.getErrorMessage();
                    if (StringUtil.hasContent(msg)) {
                        errorLabel.setText(msg);
                        resultsArea.setText("");
                    } else {
                        errorLabel.setText("");
                    }
                    break;
            }
        });
    }
}
