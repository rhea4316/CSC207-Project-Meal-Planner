package com.mealplanner.view;

import com.mealplanner.entity.Recipe;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.BrowseRecipeController;
import com.mealplanner.interface_adapter.view_model.RecipeBrowseViewModel;
import com.mealplanner.util.StringUtil;
import com.mealplanner.view.style.ModernUI;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.Cursor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;

public class BrowseRecipeView extends BorderPane implements PropertyChangeListener {
    private final RecipeBrowseViewModel viewModel;
    private final BrowseRecipeController controller;
    @SuppressWarnings("unused")
    private final ViewManagerModel viewManagerModel;

    private TextField queryField;
    private TextField ingredientsField;
    private Spinner<Integer> resultsSpinner;
    private Button searchButton;

    // Result Components
    private StackPane resultsContainer; 
    private ScrollPane listScrollPane;
    private FlowPane listPanel; 
    private VBox loadingPanel;
    private VBox emptyPanel;
    private Label errorLabel;

    public BrowseRecipeView(RecipeBrowseViewModel viewModel, BrowseRecipeController controller, ViewManagerModel viewManagerModel) {
        if (viewModel == null) throw new IllegalArgumentException("ViewModel cannot be null");
        if (controller == null) throw new IllegalArgumentException("Controller cannot be null");
        
        this.viewModel = viewModel;
        this.controller = controller;
        this.viewManagerModel = viewManagerModel;

        viewModel.addPropertyChangeListener(this);

        setPadding(new Insets(30));
        setBackground(new Background(new BackgroundFill(ModernUI.BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));

        // Title
        Label titleLabel = ModernUI.createHeaderLabel("Browse Recipes");
        
        VBox topBox = new VBox(20);
        topBox.getChildren().addAll(titleLabel, createSearchPanel());
        setTop(topBox);

        // Results
        createResultsPanel();
        setCenter(resultsContainer);
        
        // Error
        errorLabel = new Label("");
        errorLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        errorLabel.setTextFill(Color.RED);
        setBottom(errorLabel);
    }

    private VBox createSearchPanel() {
        VBox panel = ModernUI.createCardPanel();
        panel.setPadding(new Insets(20));
        panel.setSpacing(15);
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        // Query Input
        Label queryLabel = new Label("Search Query:");
        queryLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        queryField = createStyledTextField("e.g. Pasta");
        
        grid.add(queryLabel, 0, 0);
        grid.add(queryField, 1, 0);

        // Ingredients Input
        Label ingLabel = new Label("Ingredients:");
        ingLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        ingredientsField = createStyledTextField("Optional (comma-separated)");
        
        grid.add(ingLabel, 0, 1);
        grid.add(ingredientsField, 1, 1);

        // Spinner
        Label numLabel = new Label("Results:");
        numLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        resultsSpinner = new Spinner<>(1, 100, 10);
        resultsSpinner.setEditable(true);
        resultsSpinner.setPrefHeight(45);
        
        grid.add(numLabel, 0, 2);
        grid.add(resultsSpinner, 1, 2);
        
        // Column Constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        // Search Button
        searchButton = ModernUI.createPrimaryButton("Search Recipes 🔍");
        searchButton.setPrefHeight(45);
        searchButton.setMaxWidth(Double.MAX_VALUE);
        searchButton.setOnAction(e -> performSearch());
        
        panel.getChildren().addAll(grid, searchButton);
        return panel;
    }
    
    private TextField createStyledTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefHeight(45);
        tf.setFont(Font.font("Segoe UI", 14));
        tf.setStyle("-fx-background-radius: 10; -fx-border-color: #E0E0E0; -fx-border-radius: 10; -fx-padding: 0 0 0 10; -fx-background-color: white;");
        return tf;
    }

    private void createResultsPanel() {
        resultsContainer = new StackPane();
        resultsContainer.setPadding(new Insets(30, 0, 0, 0));

        // 1. List View
        listPanel = new FlowPane();
        listPanel.setHgap(20);
        listPanel.setVgap(20);
        listPanel.setPadding(new Insets(10));
        listPanel.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));

        listScrollPane = new ScrollPane(listPanel);
        listScrollPane.setFitToWidth(true);
        listScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        listScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // 2. Loading
        loadingPanel = new VBox();
        loadingPanel.setAlignment(Pos.CENTER);
        Label loadingLabel = new Label("Searching...");
        loadingLabel.setFont(Font.font("Segoe UI", 18));
        loadingLabel.setTextFill(Color.GRAY);
        loadingPanel.getChildren().add(loadingLabel);

        // 3. Empty
        emptyPanel = new VBox(15);
        emptyPanel.setAlignment(Pos.CENTER);
        
        Label iconLabel = new Label("🍽️");
        iconLabel.setFont(Font.font("Segoe UI Emoji", 64));
        
        Label emptyLabel = new Label("Ready to browse");
        emptyLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        emptyLabel.setTextFill(Color.LIGHTGRAY);
        
        emptyPanel.getChildren().addAll(iconLabel, emptyLabel);

        resultsContainer.getChildren().addAll(emptyPanel, loadingPanel, listScrollPane);
        showView("EMPTY");
    }
    
    private void showView(String viewName) {
        loadingPanel.setVisible(false);
        emptyPanel.setVisible(false);
        if (listScrollPane != null) listScrollPane.setVisible(false);
        
        switch(viewName) {
            case "LOADING": loadingPanel.setVisible(true); break;
            case "EMPTY": emptyPanel.setVisible(true); break;
            case "LIST": if (listScrollPane != null) listScrollPane.setVisible(true); break;
        }
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
        showView("LOADING");

        new Thread(() -> {
            try {
                if (StringUtil.isNullOrEmpty(ingredients)) {
                    controller.execute(query, numberOfRecipes);
                } else {
                    controller.execute(query, numberOfRecipes, ingredients);
                }
            } catch (IOException ex) {
                Platform.runLater(() -> {
                    errorLabel.setText("Network error: " + ex.getMessage());
                    showView("EMPTY");
                });
            }
        }).start();
    }

    private void displayRecipes(List<Recipe> recipes) {
        Platform.runLater(() -> {
            listPanel.getChildren().clear();
            
            if (recipes == null || recipes.isEmpty()) {
                ((Label) emptyPanel.getChildren().get(1)).setText("No recipes found");
                showView("EMPTY");
            } else {
                for (Recipe recipe : recipes) {
                    listPanel.getChildren().add(createRecipeCard(recipe));
                }
                showView("LIST");
            }
        });
    }
    
    private VBox createRecipeCard(Recipe recipe) {
        VBox card = ModernUI.createCardPanel();
        card.setPrefWidth(250);
        card.setMinWidth(250);
        card.setSpacing(10);
        card.setCursor(Cursor.HAND);

        // Thumbnail
        StackPane thumbnail = new StackPane();
        thumbnail.setPrefHeight(140);
        thumbnail.setStyle("-fx-background-color: #E0E0E0; -fx-background-radius: 10;");
        Label imgIcon = new Label("🍲");
        imgIcon.setFont(Font.font("Segoe UI Emoji", 48));
        thumbnail.getChildren().add(imgIcon);
        card.getChildren().add(thumbnail);

        // Title
        Label title = new Label(recipe.getName());
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        title.setTextFill(ModernUI.TEXT_COLOR);
        title.setWrapText(true);
        card.getChildren().add(title);

        // Details
        Label infoLabel = new Label("Servings: " + recipe.getServingSize());
        infoLabel.setFont(Font.font("Segoe UI", 12));
        infoLabel.setTextFill(Color.GRAY);
        card.getChildren().add(infoLabel);

        // Button
        Button viewBtn = ModernUI.createGhostButton("View Details");
        viewBtn.setMaxWidth(Double.MAX_VALUE);
        card.getChildren().add(viewBtn);

        // Hover
        card.setOnMouseEntered(e -> {
            card.setBorder(new Border(new BorderStroke(
                ModernUI.PRIMARY_COLOR, BorderStrokeStyle.SOLID, new CornerRadii(15), new BorderWidths(2)
            )));
        });
        card.setOnMouseExited(e -> {
             card.setBorder(new Border(new BorderStroke(
                Color.rgb(220, 220, 220), BorderStrokeStyle.SOLID, new CornerRadii(15), new BorderWidths(1)
            )));
        });

        return card;
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
                        showView("EMPTY");
                    } else {
                        errorLabel.setText("");
                    }
                    break;
            }
        });
    }
}
