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
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.Cursor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class SearchByIngredientsView extends BorderPane implements PropertyChangeListener {
    private final RecipeSearchViewModel viewModel;
    private final SearchByIngredientsController controller;
    @SuppressWarnings("unused")
    private final ViewManagerModel viewManagerModel;

    private TextField ingredientsField;
    private Button searchButton;

    // Result Components
    private StackPane resultsContainer; 
    private ScrollPane listScrollPane;
    private FlowPane listPanel; // Use FlowPane for grid-like card layout
    private VBox loadingPanel;
    private VBox emptyPanel;
    private Label errorLabel;

    public SearchByIngredientsView(SearchByIngredientsController controller, RecipeSearchViewModel viewModel, ViewManagerModel viewManagerModel) {
        if (viewModel == null) throw new IllegalArgumentException("ViewModel cannot be null");
        if (controller == null) throw new IllegalArgumentException("Controller cannot be null");

        this.viewModel = viewModel;
        this.controller = controller;
        this.viewManagerModel = viewManagerModel;

        viewModel.addPropertyChangeListener(this);

        setPadding(new Insets(30));
        setBackground(new Background(new BackgroundFill(ModernUI.BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));

        // Header + Search Area
        VBox topBox = new VBox(20);
        Label titleLabel = ModernUI.createHeaderLabel("Search by Ingredients");
        
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
        
        HBox inputBox = new HBox(15);
        inputBox.setAlignment(Pos.CENTER_LEFT);

        // Input Field
        ingredientsField = new TextField();
        ingredientsField.setPromptText("Enter ingredients (comma separated)");
        ingredientsField.setPrefHeight(45);
        ingredientsField.setFont(Font.font("Segoe UI", 16));
        ingredientsField.setStyle("-fx-background-radius: 10; -fx-border-color: #E0E0E0; -fx-border-radius: 10; -fx-padding: 0 0 0 10; -fx-background-color: white;");
        HBox.setHgrow(ingredientsField, Priority.ALWAYS);

        // Search Button
        searchButton = ModernUI.createPrimaryButton("Search 🔍");
        searchButton.setPrefHeight(45);
        searchButton.setOnAction(e -> performSearch());

        inputBox.getChildren().addAll(ingredientsField, searchButton);
        panel.getChildren().add(inputBox);
        
        return panel;
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
        
        Label emptyLabel = new Label("No ingredients entered");
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
        String ingredientsRaw = StringUtil.safeTrim(ingredientsField.getText());

        if (StringUtil.isNullOrEmpty(ingredientsRaw)) {
            errorLabel.setText("Please enter at least one ingredient");
            return;
        }
        
        errorLabel.setText("");
        viewModel.setLoading(true);
        viewModel.setErrorMessage("");
        
        controller.execute(ingredientsRaw);
    }

    private void displayRecipes(List<Recipe> recipes) {
        listPanel.getChildren().clear();

        if (recipes == null || recipes.isEmpty()) {
            showView("EMPTY");
            // Update empty text for no results
            ((Label) emptyPanel.getChildren().get(1)).setText("No recipes found");
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
