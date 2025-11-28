package com.mealplanner.view;

import com.mealplanner.entity.Recipe;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.BrowseRecipeController;
import com.mealplanner.interface_adapter.view_model.RecipeBrowseViewModel;
import com.mealplanner.util.StringUtil;
import com.mealplanner.view.component.*;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Cursor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;

public class BrowseRecipeView extends BorderPane implements PropertyChangeListener {
    private final RecipeBrowseViewModel viewModel;
    private final BrowseRecipeController controller;
    // viewManagerModel removed as unused

    private Input queryField;
    private Input ingredientsField;
    private Select<Integer> resultsSelect;
    private Button searchButton;

    // Result Components
    private StackPane resultsContainer; 
    private ScrollArea listScrollArea;
    private FlowPane listPanel; 
    private VBox loadingPanel;
    private VBox emptyPanel;
    private Label errorLabel;
    private com.mealplanner.view.component.Pagination pagination;

    public BrowseRecipeView(RecipeBrowseViewModel viewModel, BrowseRecipeController controller, ViewManagerModel viewManagerModel) {
        if (viewModel == null) throw new IllegalArgumentException("ViewModel cannot be null");
        if (controller == null) throw new IllegalArgumentException("Controller cannot be null");
        
        this.viewModel = viewModel;
        this.controller = controller;
        
        viewModel.addPropertyChangeListener(this);

        // Root Styles
        getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        // Title
        Label titleLabel = new Label("Browse Recipes");
        titleLabel.getStyleClass().add("section-title");
        titleLabel.setStyle("-fx-font-size: 32px;");
        
        VBox topBox = new VBox(20);
        topBox.getChildren().addAll(titleLabel, createSearchPanel());
        setTop(topBox);

        // Results
        createResultsPanel();
        setCenter(resultsContainer);
        
        // Bottom (Pagination + Error)
        VBox bottomBox = new VBox(10);
        bottomBox.setAlignment(Pos.CENTER);
        
        pagination = new com.mealplanner.view.component.Pagination(5, 1, page -> {
            // Handle page change
        });
        pagination.setVisible(false); 
        
        errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: -fx-theme-destructive; -fx-font-weight: bold;");
        
        bottomBox.getChildren().addAll(pagination, errorLabel);
        setBottom(bottomBox);
    }

    private VBox createSearchPanel() {
        VBox panel = new VBox();
        panel.getStyleClass().add("card-panel");
        panel.setSpacing(15);
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        // Query Input
        Label queryLabel = new Label("Search Query:");
        queryLabel.setStyle("-fx-font-weight: 600;");
        queryField = new Input();
        queryField.setPromptText("e.g. Pasta");
        
        grid.add(queryLabel, 0, 0);
        grid.add(queryField, 1, 0);

        // Ingredients Input
        Label ingLabel = new Label("Ingredients:");
        ingLabel.setStyle("-fx-font-weight: 600;");
        ingredientsField = new Input();
        ingredientsField.setPromptText("Optional (comma-separated)");
        
        grid.add(ingLabel, 0, 1);
        grid.add(ingredientsField, 1, 1);

        // Results Count
        Label numLabel = new Label("Results:");
        numLabel.setStyle("-fx-font-weight: 600;");
        resultsSelect = new Select<>();
        resultsSelect.getItems().addAll(10, 20, 50, 100);
        resultsSelect.setValue(10);
        resultsSelect.setPrefWidth(100);
        
        grid.add(numLabel, 0, 2);
        grid.add(resultsSelect, 1, 2);
        
        // Column Constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        // Search Button
        searchButton = new Button("Search Recipes 🔍");
        searchButton.getStyleClass().add("primary-button");
        searchButton.setPrefHeight(45);
        searchButton.setMaxWidth(Double.MAX_VALUE);
        searchButton.setOnAction(e -> performSearch());
        
        panel.getChildren().addAll(grid, searchButton);
        return panel;
    }

    private void createResultsPanel() {
        resultsContainer = new StackPane();
        resultsContainer.setPadding(new Insets(30, 0, 0, 0));

        // 1. List View
        listPanel = new FlowPane();
        listPanel.setHgap(20);
        listPanel.setVgap(20);
        listPanel.setPadding(new Insets(10));
        listPanel.setBackground(Background.EMPTY);
        listPanel.setAlignment(Pos.TOP_LEFT);

        listScrollArea = new ScrollArea(listPanel);
        listScrollArea.setFitToWidth(true);
        listScrollArea.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // 2. Loading
        loadingPanel = new VBox();
        loadingPanel.setAlignment(Pos.CENTER);
        
        Skeleton loadingSkeleton = new Skeleton(300, 200);
        Label loadingLabel = new Label("Searching...");
        loadingLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: -fx-theme-muted-foreground;");
        loadingPanel.getChildren().addAll(loadingSkeleton, loadingLabel);

        // 3. Empty
        emptyPanel = new VBox(15);
        emptyPanel.setAlignment(Pos.CENTER);
        
        Label iconLabel = new Label("🍽️");
        iconLabel.setStyle("-fx-font-size: 64px;");
        
        Label emptyLabel = new Label("Ready to browse");
        emptyLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: -fx-theme-muted-foreground;");
        
        emptyPanel.getChildren().addAll(iconLabel, emptyLabel);

        resultsContainer.getChildren().addAll(emptyPanel, loadingPanel, listScrollArea);
        showView("EMPTY");
    }
    
    private void showView(String viewName) {
        loadingPanel.setVisible(false);
        emptyPanel.setVisible(false);
        if (listScrollArea != null) listScrollArea.setVisible(false);
        if (pagination != null) pagination.setVisible(false);
        
        switch(viewName) {
            case "LOADING": loadingPanel.setVisible(true); break;
            case "EMPTY": emptyPanel.setVisible(true); break;
            case "LIST": 
                if (listScrollArea != null) listScrollArea.setVisible(true); 
                if (pagination != null) pagination.setVisible(true);
                break;
        }
    }

    private void performSearch() {
        String query = StringUtil.safeTrim(queryField.getText());
        String ingredients = StringUtil.safeTrim(ingredientsField.getText());
        Integer numberOfRecipes = resultsSelect.getValue();

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
        VBox card = new VBox();
        card.getStyleClass().add("meal-card");
        card.setPrefWidth(250);
        card.setMinWidth(250);
        card.setSpacing(10);
        card.setCursor(Cursor.HAND);

        // Thumbnail
        Region thumbnail = new Region();
        thumbnail.setPrefHeight(140);
        thumbnail.setStyle("-fx-background-color: -fx-theme-muted; -fx-background-radius: 8px;");
        card.getChildren().add(thumbnail);

        // Badge (e.g., Random tag for demo)
        HBox tags = new HBox(5);
        Badge tag = new Badge("Recipe", Badge.Variant.DEFAULT);
        tags.getChildren().add(tag);
        card.getChildren().add(tags);

        // Title
        Label title = new Label(recipe.getName());
        title.getStyleClass().add("meal-card-title");
        title.setWrapText(true);
        card.getChildren().add(title);

        // Details
        Label infoLabel = new Label("Servings: " + recipe.getServingSize());
        infoLabel.getStyleClass().add("meal-card-subtitle");
        card.getChildren().add(infoLabel);

        // Context Menu
        StyledContextMenu contextMenu = new StyledContextMenu();
        MenuItem saveItem = new MenuItem("Save to Cookbook");
        MenuItem viewItem = new MenuItem("View Details");
        contextMenu.getItems().addAll(saveItem, viewItem);
        
        card.setOnContextMenuRequested(e -> 
            contextMenu.show(card, e.getScreenX(), e.getScreenY())
        );

        // Button
        Button viewBtn = new Button("View Details");
        viewBtn.getStyleClass().add("ghost-button");
        viewBtn.setMaxWidth(Double.MAX_VALUE);
        card.getChildren().add(viewBtn);

        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-border-color: -fx-theme-primary; -fx-border-width: 2px;");
        });
        card.setOnMouseExited(e -> {
            card.setStyle(""); 
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
