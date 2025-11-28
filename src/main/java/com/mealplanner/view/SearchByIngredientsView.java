package com.mealplanner.view;

import com.mealplanner.entity.Recipe;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.SearchByIngredientsController;
import com.mealplanner.interface_adapter.view_model.RecipeSearchViewModel;
import com.mealplanner.util.StringUtil;
import com.mealplanner.view.style.ModernUI;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

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
    private FlowPane listPanel; // Use FlowPane for grid-like card layout
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

        setPadding(new Insets(30));
        setBackground(new Background(new BackgroundFill(ModernUI.BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));

        // Header + Search Area
        VBox topBox = new VBox(20);
        Label titleLabel = ModernUI.createHeaderLabel("Find by Ingredients"); // Updated Title
        
        VBox searchPanel = createSearchPanel();
        topBox.getChildren().addAll(titleLabel, searchPanel);
        setTop(topBox);

        // Results Area
        createResultsPanel();
        setCenter(resultsContainer);
        
        // Error Label
        errorLabel = new Label("");
        errorLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        errorLabel.setTextFill(Color.RED);
        errorLabel.setPadding(new Insets(10, 0, 0, 0));
        setBottom(errorLabel);
    }

    private VBox createSearchPanel() {
        VBox panel = ModernUI.createCardPanel(); // White bg, rounded
        panel.setPadding(new Insets(20));
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
        ingredientsField.setFont(Font.font("Segoe UI", 16));
        ingredientsField.setStyle("-fx-background-radius: 10; -fx-border-color: #E0E0E0; -fx-border-radius: 10; -fx-padding: 0 15 0 15; -fx-background-color: white;");
        HBox.setHgrow(ingredientsField, Priority.ALWAYS);

        // Handle Enter Key for Chip Creation
        ingredientsField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                addIngredientChip(ingredientsField.getText());
                ingredientsField.clear();
            }
        });

        searchButton = ModernUI.createPrimaryButton("Find Recipes 🔍");
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
        btn.setFont(Font.font("Segoe UI", 13));
        btn.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #4B5563; -fx-background-radius: 20; -fx-border-color: #E5E7EB; -fx-border-radius: 20; -fx-padding: 5 12 5 12;");
        btn.setCursor(Cursor.HAND);
        
        btn.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                btn.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-background-radius: 20; -fx-border-color: #5CDB95; -fx-border-radius: 20; -fx-padding: 5 12 5 12;");
                activeFilters.add(text);
            } else {
                btn.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #4B5563; -fx-background-radius: 20; -fx-border-color: #E5E7EB; -fx-border-radius: 20; -fx-padding: 5 12 5 12;");
                activeFilters.remove(text);
            }
            // Optional: Trigger search immediately on filter change if ingredients exist
            if (!ingredientList.isEmpty()) {
                performSearch();
            }
        });
        
        return btn;
    }

    private void addIngredientChip(String text) {
        String ingredient = StringUtil.safeTrim(text);
        if (StringUtil.isNullOrEmpty(ingredient)) return;
        if (ingredientList.contains(ingredient)) return; // Prevent duplicates

        ingredientList.add(ingredient);

        // Create Chip UI
        HBox chip = new HBox(5);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setStyle("-fx-background-color: #D1FAE5; -fx-background-radius: 20; -fx-padding: 5 12 5 12; -fx-border-color: #5CDB95; -fx-border-radius: 20; -fx-border-width: 1;");
        
        Label label = new Label(ingredient);
        label.setTextFill(ModernUI.PRIMARY_DARK);
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));

        // Delete Button (x)
        Label closeBtn = new Label("✕");
        closeBtn.setTextFill(ModernUI.PRIMARY_DARK);
        closeBtn.setCursor(Cursor.HAND);
        closeBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
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

        // 1. List View (FlowPane inside ScrollPane)
        listPanel = new FlowPane();
        listPanel.setHgap(20);
        listPanel.setVgap(20);
        listPanel.setPadding(new Insets(10));
        listPanel.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));

        listScrollPane = new ScrollPane(listPanel);
        listScrollPane.setFitToWidth(true);
        listScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        listScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // 2. Loading View
        loadingPanel = new VBox();
        loadingPanel.setAlignment(Pos.CENTER);
        Label loadingLabel = new Label("Searching recipes...");
        loadingLabel.setFont(Font.font("Segoe UI", 18));
        loadingLabel.setTextFill(Color.GRAY);
        loadingPanel.getChildren().add(loadingLabel);

        // 3. Empty View
        emptyPanel = new VBox(15);
        emptyPanel.setAlignment(Pos.CENTER);
        
        Label iconLabel = new Label("🍽️");
        iconLabel.setFont(Font.font("Segoe UI Emoji", 64));
        
        Label emptyLabel = new Label("Add ingredients to start searching");
        emptyLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        emptyLabel.setTextFill(Color.LIGHTGRAY);
        
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
            case "LIST":
                if (listScrollPane != null) {
                    listScrollPane.setVisible(true);
                }
                break;
        }
    }

    private void performSearch() {
        // If user typed something but didn't press enter, add it as a chip first
        String currentText = ingredientsField.getText();
        if (StringUtil.hasContent(currentText)) {
            addIngredientChip(currentText);
            ingredientsField.clear();
        }

        if (ingredientList.isEmpty()) {
            errorLabel.setText("Please add at least one ingredient");
            return;
        }
        
        // Convert list to comma-separated string for controller
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
            // Update empty text for no results
            ((Label) emptyPanel.getChildren().get(1)).setText("No recipes found for these ingredients");
        } else {
            for (Recipe recipe : recipes) {
                listPanel.getChildren().add(createRecipeCard(recipe));
            }
            showView("LIST");
        }
    }

    private VBox createRecipeCard(Recipe recipe) {
        VBox card = ModernUI.createCardPanel();
        card.setPrefWidth(250); // Fixed width for card grid
        card.setMinWidth(250);
        card.setSpacing(10);
        card.setCursor(Cursor.HAND);

        // Thumbnail Placeholder
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

        // Summary Info
        String ingredientCount = recipe.getIngredients() != null ? String.valueOf(recipe.getIngredients().size()) : "0";
        String calInfo = (recipe.getNutritionInfo() != null) ? recipe.getNutritionInfo().getCalories() + " kcal" : "N/A";
        
        // Matching Score Calculation
        int matchCount = 0;
        List<String> recipeIngredients = recipe.getIngredients();
        if (recipeIngredients != null) {
            for (String rIng : recipeIngredients) {
                for (String uIng : ingredientList) {
                    // Simple contains check (case-insensitive)
                    if (rIng.toLowerCase().contains(uIng.toLowerCase())) {
                        matchCount++;
                        break; // Found a match for this recipe ingredient
                    }
                }
            }
        }
        
        int totalIngredients = recipeIngredients != null ? recipeIngredients.size() : 0;
        int missingCount = totalIngredients - matchCount;
        
        // Create Badge
        Label badge = new Label();
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        badge.setPadding(new Insets(4, 8, 4, 8));
        badge.setStyle("-fx-background-radius: 12;");

        if (missingCount == 0) {
            badge.setText("Perfect Match! ⭐");
            badge.setTextFill(Color.web("#065F46")); // Dark Green
            badge.setStyle(badge.getStyle() + "-fx-background-color: #D1FAE5;"); // Light Mint
        } else if (missingCount <= 2) {
            badge.setText("Missing " + missingCount + " items");
            badge.setTextFill(Color.web("#92400E")); // Dark Yellow
            badge.setStyle(badge.getStyle() + "-fx-background-color: #FEF3C7;"); // Light Yellow
        } else {
            badge.setText("Missing " + missingCount + " items");
            badge.setTextFill(Color.web("#991B1B")); // Dark Red
            badge.setStyle(badge.getStyle() + "-fx-background-color: #FEE2E2;"); // Light Red
        }

        HBox badgeContainer = new HBox(badge);
        badgeContainer.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(badgeContainer);

        Label infoLabel = new Label("Ingredients: " + ingredientCount + "\n" + calInfo);
        infoLabel.setFont(Font.font("Segoe UI", 12));
        infoLabel.setTextFill(Color.GRAY);
        card.getChildren().add(infoLabel);

        // View Button
        Button viewBtn = ModernUI.createGhostButton("View Details");
        viewBtn.setMaxWidth(Double.MAX_VALUE);
        viewBtn.setOnAction(e -> System.out.println("View Recipe: " + recipe.getName()));
        card.getChildren().add(viewBtn);

        // Hover Effect
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
