package com.mealplanner.view;

import com.mealplanner.entity.Recipe;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.SearchByIngredientsController;
import com.mealplanner.interface_adapter.view_model.RecipeSearchViewModel;
import com.mealplanner.util.StringUtil;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class SearchByIngredientsView extends BorderPane implements PropertyChangeListener {
    private final RecipeSearchViewModel viewModel;
    private final SearchByIngredientsController controller;
    @SuppressWarnings("unused")
    private final ViewManagerModel viewManagerModel;

    private TextField ingredientsField;
    private FlowPane chipsContainer;
    private Button searchButton;
    private List<String> ingredientList;

    // Result Components
    private StackPane resultsContainer; 
    private ScrollPane listScrollPane;
    private FlowPane listPanel; 
    private VBox loadingPanel;
    private VBox emptyPanel;
    private Label errorLabel;

    // Filters
    private FlowPane quickFiltersContainer;
    private List<String> activeFilters = new ArrayList<>();

    public SearchByIngredientsView(SearchByIngredientsController controller, RecipeSearchViewModel viewModel, ViewManagerModel viewManagerModel) {
        if (viewModel == null) throw new IllegalArgumentException("ViewModel cannot be null");
        if (controller == null) throw new IllegalArgumentException("Controller cannot be null");

        this.viewModel = viewModel;
        this.controller = controller;
        this.viewManagerModel = viewManagerModel;
        this.ingredientList = new ArrayList<>();

        viewModel.addPropertyChangeListener(this);

        // Root Style
        getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        // Header + Search Area
        VBox topBox = new VBox(20);
        Label titleLabel = new Label("Find by Ingredients");
        titleLabel.getStyleClass().add("section-title");
        titleLabel.setStyle("-fx-font-size: 32px;");
        
        VBox searchPanel = createSearchPanel();
        topBox.getChildren().addAll(titleLabel, searchPanel);
        setTop(topBox);

        // Results Area
        createResultsPanel();
        setCenter(resultsContainer);
        
        // Error Label
        errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: -fx-theme-destructive; -fx-font-weight: bold;");
        errorLabel.setPadding(new Insets(10, 0, 0, 0));
        setBottom(errorLabel);
    }

    private VBox createSearchPanel() {
        VBox panel = new VBox();
        panel.getStyleClass().add("card-panel");
        panel.setSpacing(15);
        
        // 1. Chips Container (Where tags appear)
        chipsContainer = new FlowPane();
        chipsContainer.setHgap(8);
        chipsContainer.setVgap(8);
        chipsContainer.setPadding(new Insets(0, 0, 5, 0));

        // 2. Input Field + Search Button
        HBox inputBox = new HBox(15);
        inputBox.setAlignment(Pos.CENTER_LEFT);

        ingredientsField = new TextField();
        ingredientsField.setPromptText("Type an ingredient and press Enter (e.g., 'Egg')");
        ingredientsField.setPrefHeight(45);
        ingredientsField.getStyleClass().add("text-field");
        HBox.setHgrow(ingredientsField, Priority.ALWAYS);

        // Handle Enter Key for Chip Creation
        ingredientsField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                addIngredientChip(ingredientsField.getText());
                ingredientsField.clear();
            }
        });

        searchButton = new Button("Find Recipes 🔍");
        searchButton.getStyleClass().add("primary-button");
        searchButton.setPrefHeight(45);
        searchButton.setOnAction(e -> performSearch());

        inputBox.getChildren().addAll(ingredientsField, searchButton);
        
        // 3. Quick Filters
        quickFiltersContainer = new FlowPane();
        quickFiltersContainer.setHgap(10);
        quickFiltersContainer.setVgap(10);
        
        String[] filters = {"Breakfast", "Lunch", "Dinner", "Vegetarian", "Gluten Free", "Quick (< 30min)"};
        for (String filter : filters) {
            ToggleButton filterBtn = createFilterButton(filter);
            quickFiltersContainer.getChildren().add(filterBtn);
        }

        panel.getChildren().addAll(chipsContainer, inputBox, quickFiltersContainer);
        return panel;
    }

    private ToggleButton createFilterButton(String text) {
        ToggleButton btn = new ToggleButton(text);
        btn.getStyleClass().add("filter-toggle");
        
        btn.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                btn.setStyle("-fx-background-color: -fx-theme-secondary; -fx-text-fill: -fx-theme-primary; -fx-border-color: -fx-theme-primary; -fx-font-weight: bold;");
                activeFilters.add(text);
            } else {
                btn.setStyle(""); // Revert to CSS class style
                activeFilters.remove(text);
            }
            // Optional: Trigger search immediately
            if (!ingredientList.isEmpty()) {
                performSearch();
            }
        });
        
        return btn;
    }

    private void addIngredientChip(String text) {
        String ingredient = StringUtil.safeTrim(text);
        if (StringUtil.isNullOrEmpty(ingredient)) return;
        if (ingredientList.contains(ingredient)) return;

        ingredientList.add(ingredient);

        // Create Chip UI using CSS
        HBox chip = new HBox(5);
        chip.getStyleClass().add("chip");
        
        Label label = new Label(ingredient);
        label.getStyleClass().add("label");

        Label closeBtn = new Label("✕");
        closeBtn.getStyleClass().add("close-button");
        closeBtn.setOnMouseClicked(e -> removeIngredientChip(chip, ingredient));

        chip.getChildren().addAll(label, closeBtn);
        chipsContainer.getChildren().add(chip);
    }

    private void removeIngredientChip(Node chip, String ingredient) {
        chipsContainer.getChildren().remove(chip);
        ingredientList.remove(ingredient);
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

        listScrollPane = new ScrollPane(listPanel);
        listScrollPane.setFitToWidth(true);
        listScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        listScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // 2. Loading View
        loadingPanel = new VBox();
        loadingPanel.setAlignment(Pos.CENTER);
        Label loadingLabel = new Label("Searching recipes...");
        loadingLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: -fx-theme-muted-foreground;");
        loadingPanel.getChildren().add(loadingLabel);

        // 3. Empty View
        emptyPanel = new VBox(15);
        emptyPanel.setAlignment(Pos.CENTER);
        
        Label iconLabel = new Label("🍽️");
        iconLabel.setStyle("-fx-font-size: 64px;");
        
        Label emptyLabel = new Label("Add ingredients to start searching");
        emptyLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: -fx-theme-muted-foreground;");
        
        emptyPanel.getChildren().addAll(iconLabel, emptyLabel);

        resultsContainer.getChildren().addAll(emptyPanel, loadingPanel, listScrollPane);
        
        showView("EMPTY");
    }
    
    private void showView(String viewName) {
        loadingPanel.setVisible(false);
        emptyPanel.setVisible(false);
        if (listScrollPane != null) {
            listScrollPane.setVisible(false);
        }
        
        switch(viewName) {
            case "LOADING": loadingPanel.setVisible(true); break;
            case "EMPTY": emptyPanel.setVisible(true); break;
            case "LIST": if (listScrollPane != null) listScrollPane.setVisible(true); break;
        }
    }

    private void performSearch() {
        String currentText = ingredientsField.getText();
        if (StringUtil.hasContent(currentText)) {
            addIngredientChip(currentText);
            ingredientsField.clear();
        }

        if (ingredientList.isEmpty()) {
            errorLabel.setText("Please add at least one ingredient");
            return;
        }
        
        String query = String.join(",", ingredientList);
        
        errorLabel.setText("");
        viewModel.setLoading(true);
        viewModel.setErrorMessage("");
        
        controller.execute(query);
    }

    private void displayRecipes(List<Recipe> recipes) {
        listPanel.getChildren().clear();

        if (recipes == null || recipes.isEmpty()) {
            showView("EMPTY");
            ((Label) emptyPanel.getChildren().get(1)).setText("No recipes found for these ingredients");
        } else {
            for (Recipe recipe : recipes) {
                listPanel.getChildren().add(createRecipeCard(recipe));
            }
            showView("LIST");
        }
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

        // Title
        Label title = new Label(recipe.getName());
        title.getStyleClass().add("meal-card-title");
        title.setWrapText(true);
        card.getChildren().add(title);

        // Matching Logic Display
        int matchCount = 0;
        List<String> recipeIngredients = recipe.getIngredients();
        if (recipeIngredients != null) {
            for (String rIng : recipeIngredients) {
                for (String uIng : ingredientList) {
                    if (rIng.toLowerCase().contains(uIng.toLowerCase())) {
                        matchCount++;
                        break;
                    }
                }
            }
        }
        
        int totalIngredients = recipeIngredients != null ? recipeIngredients.size() : 0;
        int missingCount = totalIngredients - matchCount;
        
        Label badge = new Label();
        badge.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 4px 8px; -fx-background-radius: 12;");
        
        if (missingCount == 0) {
            badge.setText("Perfect Match! ⭐");
            badge.setTextFill(Color.web("#065F46"));
            badge.setStyle(badge.getStyle() + "-fx-background-color: #D1FAE5;");
        } else {
            badge.setText("Missing " + missingCount + " items");
            badge.setTextFill(Color.web("#92400E"));
            badge.setStyle(badge.getStyle() + "-fx-background-color: #FEF3C7;");
        }
        
        HBox badgeContainer = new HBox(badge);
        badgeContainer.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(badgeContainer);

        // Info
        Label infoLabel = new Label("Ingredients: " + totalIngredients + "\n" + 
            ((recipe.getNutritionInfo() != null) ? recipe.getNutritionInfo().getCalories() + " kcal" : "N/A"));
        infoLabel.getStyleClass().add("meal-card-subtitle");
        card.getChildren().add(infoLabel);

        // Button
        Button viewBtn = new Button("View Details");
        viewBtn.getStyleClass().add("ghost-button");
        viewBtn.setMaxWidth(Double.MAX_VALUE);
        card.getChildren().add(viewBtn);

        // Hover
        card.setOnMouseEntered(e -> card.setStyle("-fx-border-color: -fx-theme-primary; -fx-border-width: 2px;"));
        card.setOnMouseExited(e -> card.setStyle(""));

        return card;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(() -> {
            String propertyName = evt.getPropertyName();
            
            if (RecipeSearchViewModel.PROP_LOADING.equals(propertyName)) {
                if (viewModel.isLoading()) {
                    showView("LOADING");
                    searchButton.setDisable(true);
                } else {
                    searchButton.setDisable(false);
                }
            } else if (RecipeSearchViewModel.PROP_RECIPES.equals(propertyName)) {
                displayRecipes(viewModel.getRecipes());
            } else if (RecipeSearchViewModel.PROP_ERROR_MESSAGE.equals(propertyName)) {
                String errorMsg = viewModel.getErrorMessage();
                if (StringUtil.hasContent(errorMsg)) {
                    errorLabel.setText(errorMsg);
                } else {
                    errorLabel.setText("");
                }
            }
        });
    }
}
