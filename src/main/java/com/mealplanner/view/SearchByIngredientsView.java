package com.mealplanner.view;

import com.mealplanner.entity.Recipe;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.SearchByIngredientsController;
import com.mealplanner.interface_adapter.view_model.RecipeSearchViewModel;
import com.mealplanner.util.StringUtil;
import com.mealplanner.view.util.SvgIconLoader;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

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
        setBackground(new Background(new BackgroundFill(Color.web("#f7f8f9"), CornerRadii.EMPTY, Insets.EMPTY)));

        // 1. Header Title
        VBox headerBox = new VBox(8);
        Label titleLabel = new Label("Find by Ingredients");
        titleLabel.getStyleClass().add("section-title");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: -fx-theme-foreground;");
        
        Label subTitle = new Label("Enter ingredients you have to discover recipes you can make");
        subTitle.getStyleClass().add("text-gray-500");
        subTitle.setStyle("-fx-font-size: 14px;");
        
        headerBox.getChildren().addAll(titleLabel, subTitle);
        
        // 2. Search Panel (White Card)
        VBox searchPanel = createSearchPanel();
        VBox.setMargin(searchPanel, new Insets(24, 0, 24, 0));
        
        VBox topSection = new VBox();
        topSection.getChildren().addAll(headerBox, searchPanel);
        setTop(topSection);

        // 3. Results Area
        createResultsPanel();
        setCenter(resultsContainer);
        
        // Error Label
        errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: -fx-theme-destructive; -fx-font-weight: bold;");
        errorLabel.setPadding(new Insets(10, 0, 0, 0));
        setBottom(errorLabel);
    }

    private VBox createSearchPanel() {
        VBox panel = new VBox(20);
        panel.getStyleClass().add("card-panel");
        panel.setPadding(new Insets(24));
        
        // 1. Input Field + Search Button
        HBox inputBox = new HBox(16);
        inputBox.setAlignment(Pos.CENTER_LEFT);

        // Search Input Container
        HBox searchFieldContainer = new HBox(10);
        searchFieldContainer.setAlignment(Pos.CENTER_LEFT);
        searchFieldContainer.setStyle("-fx-background-color: white; -fx-border-color: -fx-color-gray-200; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 0 12px;");
        searchFieldContainer.setPrefHeight(48);
        HBox.setHgrow(searchFieldContainer, Priority.ALWAYS);

        Node searchIcon = SvgIconLoader.loadIcon("/svg/search.svg", 20, Color.web("#9ca3af"));
        if (searchIcon != null) searchFieldContainer.getChildren().add(searchIcon);

        ingredientsField = new TextField();
        ingredientsField.setPromptText("Type an ingredient and press Enter...");
        ingredientsField.setStyle("-fx-background-color: transparent; -fx-border-width: 0; -fx-font-size: 14px;");
        ingredientsField.setPrefHeight(40);
        HBox.setHgrow(ingredientsField, Priority.ALWAYS);
        
        // Chips Container inside input box (optional, or below) - Design shows below as tags
        // So field is just text
        
        searchFieldContainer.getChildren().add(ingredientsField);

        // Handle Enter Key
        ingredientsField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                addIngredientChip(ingredientsField.getText());
                ingredientsField.clear();
            }
        });

        searchButton = new Button("Search");
        searchButton.getStyleClass().add("primary-button"); // Green button
        searchButton.setStyle("-fx-background-color: #4ade80; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 8px; -fx-padding: 10 24; -fx-font-size: 14px;");
        Node btnIcon = SvgIconLoader.loadIcon("/svg/search.svg", 16, Color.WHITE);
        if (btnIcon != null) {
            searchButton.setGraphic(btnIcon);
            searchButton.setGraphicTextGap(8);
        }
        searchButton.setPrefHeight(48);
        searchButton.setOnAction(e -> performSearch());

        inputBox.getChildren().addAll(searchFieldContainer, searchButton);

        // 2. Popular Ingredients Tags
        VBox popularSection = new VBox(8);
        
        HBox popularHeader = new HBox(6);
        popularHeader.setAlignment(Pos.CENTER_LEFT);
        Node sparkIcon = SvgIconLoader.loadIcon("/svg/star.svg", 14, Color.web("#6b7280"));
        Label popularLabel = new Label("Popular ingredients");
        popularLabel.getStyleClass().add("text-gray-500");
        popularLabel.setStyle("-fx-font-size: 13px;");
        if (sparkIcon != null) popularHeader.getChildren().add(sparkIcon);
        popularHeader.getChildren().add(popularLabel);
        
        FlowPane popularTags = new FlowPane();
        popularTags.setHgap(8);
        popularTags.setVgap(8);
        
        String[] popularItems = {"Egg", "Chicken", "Tomato", "Onion", "Garlic", "Pasta", "Rice", "Milk", "Cheese", "Potato"};
        for (String item : popularItems) {
            Button tagBtn = new Button("+ " + item);
            tagBtn.setStyle("-fx-background-color: -fx-color-gray-50; -fx-text-fill: -fx-color-gray-700; -fx-background-radius: 20px; -fx-border-color: -fx-color-gray-200; -fx-border-radius: 20px; -fx-padding: 6 12; -fx-font-size: 13px; -fx-cursor: hand;");
            tagBtn.setOnAction(e -> addIngredientChip(item));
            
            // Hover effect
            tagBtn.setOnMouseEntered(e -> tagBtn.setStyle("-fx-background-color: -fx-color-gray-100; -fx-text-fill: -fx-color-gray-900; -fx-background-radius: 20px; -fx-border-color: -fx-color-gray-300; -fx-border-radius: 20px; -fx-padding: 6 12; -fx-font-size: 13px; -fx-cursor: hand;"));
            tagBtn.setOnMouseExited(e -> tagBtn.setStyle("-fx-background-color: -fx-color-gray-50; -fx-text-fill: -fx-color-gray-700; -fx-background-radius: 20px; -fx-border-color: -fx-color-gray-200; -fx-border-radius: 20px; -fx-padding: 6 12; -fx-font-size: 13px; -fx-cursor: hand;"));
            
            popularTags.getChildren().add(tagBtn);
        }
        
        popularSection.getChildren().addAll(popularHeader, popularTags);

        // 3. Active Chips Display Area (The user's selected ingredients)
        chipsContainer = new FlowPane();
        chipsContainer.setHgap(8);
        chipsContainer.setVgap(8);
        // Only show if there are chips
        chipsContainer.managedProperty().bind(chipsContainer.visibleProperty());
        chipsContainer.visibleProperty().bind(javafx.beans.binding.Bindings.isNotEmpty(chipsContainer.getChildren()));

        // 4. Quick Filters
        VBox filtersSection = new VBox(8);
        Label filtersLabel = new Label("Quick Filters");
        filtersLabel.getStyleClass().add("text-gray-500");
        filtersLabel.setStyle("-fx-font-size: 13px;");
        
        quickFiltersContainer = new FlowPane();
        quickFiltersContainer.setHgap(10);
        quickFiltersContainer.setVgap(10);
        
        // Filters with Icons
        addFilterButton("Breakfast", "/svg/mug-hot.svg");
        addFilterButton("Lunch", "/svg/brightness.svg");
        addFilterButton("Dinner", "/svg/moon.svg");
        addFilterButton("Vegetarian", "/svg/leaf.svg");
        addFilterButton("Quick (< 30min)", "/svg/time-fast.svg"); // Assumed icon
        
        filtersSection.getChildren().addAll(filtersLabel, quickFiltersContainer);

        panel.getChildren().addAll(inputBox, chipsContainer, popularSection, filtersSection);
        return panel;
    }

    private void addFilterButton(String text, String iconPath) {
        ToggleButton btn = new ToggleButton(text);
        btn.setStyle("-fx-background-color: white; -fx-text-fill: -fx-color-gray-600; -fx-border-color: -fx-color-gray-200; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8 12; -fx-font-size: 13px; -fx-cursor: hand;");
        
        Node icon = SvgIconLoader.loadIcon(iconPath, 16, Color.web("#6b7280"));
        if (icon != null) {
            btn.setGraphic(icon);
            btn.setGraphicTextGap(8);
        }

        btn.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // Active State
                btn.setStyle("-fx-background-color: #f0fdf4; -fx-text-fill: #166534; -fx-border-color: #22c55e; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8 12; -fx-font-size: 13px; -fx-cursor: hand;");
                if (btn.getGraphic() != null) {
                    // Tint icon green (simplified by reloading or assuming SvgIconLoader returns a new node each time? 
                    // SvgIconLoader loads new node, so we'd need to set a new graphic. 
                    // For simplicity, let's just change the button style first. 
                    // Ideally we reload icon with green color.)
                     Node activeIcon = SvgIconLoader.loadIcon(iconPath, 16, Color.web("#166534"));
                     btn.setGraphic(activeIcon);
                }
                activeFilters.add(text);
            } else {
                // Inactive State
                btn.setStyle("-fx-background-color: white; -fx-text-fill: -fx-color-gray-600; -fx-border-color: -fx-color-gray-200; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8 12; -fx-font-size: 13px; -fx-cursor: hand;");
                if (btn.getGraphic() != null) {
                     Node inactiveIcon = SvgIconLoader.loadIcon(iconPath, 16, Color.web("#6b7280"));
                     btn.setGraphic(inactiveIcon);
                }
                activeFilters.remove(text);
            }
            // Trigger search logic if needed
        });
        
        quickFiltersContainer.getChildren().add(btn);
    }

    private void addIngredientChip(String text) {
        String ingredient = StringUtil.safeTrim(text);
        if (StringUtil.isNullOrEmpty(ingredient)) return;
        
        // Case-insensitive check
        boolean exists = ingredientList.stream().anyMatch(i -> i.equalsIgnoreCase(ingredient));
        if (exists) return;

        ingredientList.add(ingredient);

        // Create Chip UI
        HBox chip = new HBox(6);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setStyle("-fx-background-color: #ecfccb; -fx-background-radius: 20px; -fx-padding: 6 12; -fx-border-color: #bef264; -fx-border-radius: 20px;");
        
        Label label = new Label(ingredient);
        label.setStyle("-fx-text-fill: #3f6212; -fx-font-weight: 600; -fx-font-size: 13px;");

        Label closeBtn = new Label("✕");
        closeBtn.setStyle("-fx-text-fill: #3f6212; -fx-font-size: 10px; -fx-cursor: hand;");
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
        resultsContainer.getStyleClass().add("card-panel");
        resultsContainer.setStyle(resultsContainer.getStyle() + "-fx-background-color: #f0fdf4; -fx-border-color: #dcfce7; -fx-border-width: 1px; -fx-border-radius: 16px; -fx-padding: 40;"); // Light green background for empty state area

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

        // 3. Empty View (Initial State)
        emptyPanel = new VBox(16);
        emptyPanel.setAlignment(Pos.CENTER);
        
        // Icon Circle
        StackPane iconCircle = new StackPane();
        Circle bg = new Circle(32);
        bg.setFill(Color.web("#4ade80")); // Bright green
        bg.setEffect(new javafx.scene.effect.DropShadow(10, Color.rgb(74, 222, 128, 0.4)));
        
        Node chefIcon = SvgIconLoader.loadIcon("/svg/restaurant.svg", 32, Color.WHITE); // Assuming chef/restaurant icon
        if (chefIcon != null) iconCircle.getChildren().addAll(bg, chefIcon);
        else iconCircle.getChildren().add(bg); // Fallback

        Label startTitle = new Label("Start by adding your ingredients!");
        startTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: -fx-color-gray-800;");
        
        Label startSub = new Label("We'll help you find recipes you can make");
        startSub.setStyle("-fx-font-size: 14px; -fx-text-fill: -fx-color-gray-500;");
        
        Label hintLabel = new Label("✨ Click popular ingredients above to add them quickly");
        hintLabel.setStyle("-fx-background-color: #ecfccb; -fx-text-fill: #3f6212; -fx-padding: 8 16; -fx-background-radius: 20px; -fx-font-size: 13px; -fx-font-weight: 500;");
        VBox.setMargin(hintLabel, new Insets(20, 0, 0, 0));

        emptyPanel.getChildren().addAll(iconCircle, startTitle, startSub, hintLabel);

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
                if (listScrollPane != null) listScrollPane.setVisible(true);
                // Reset container style to default card panel or transparent if list is shown?
                // Design keeps the container but maybe white bg for results? 
                // For now, keep it consistent.
                break;
        }
    }

    private void performSearch() {
        // Add current text if exists
        String currentText = ingredientsField.getText();
        if (StringUtil.hasContent(currentText)) {
            addIngredientChip(currentText);
            ingredientsField.clear();
        }

        if (ingredientList.isEmpty()) {
            // Show visual feedback? 
            // errorLabel.setText("Please add at least one ingredient");
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
            // Optional: Change empty message to "No results"
            Label title = (Label) emptyPanel.getChildren().get(1);
            title.setText("No recipes found matching your ingredients");
            Label sub = (Label) emptyPanel.getChildren().get(2);
            sub.setText("Try removing some filters or adding different ingredients");
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
        // Same card style as Dashboard or similar
        card.setPrefWidth(220);
        card.setMinWidth(220);
        card.setSpacing(10);
        card.setPadding(new Insets(12));
        card.setCursor(Cursor.HAND);

        // 1. Image Placeholder
        Region thumbnail = new Region();
        thumbnail.setPrefHeight(120);
        thumbnail.setStyle("-fx-background-color: #e5e7eb; -fx-background-radius: 8px;");
        card.getChildren().add(thumbnail);

        // 2. Title
        Label title = new Label(recipe.getName());
        title.getStyleClass().add("text-gray-900");
        title.setStyle("-fx-font-weight: 600; -fx-font-size: 14px;");
        title.setWrapText(true);
        card.getChildren().add(title);

        // 3. Match Badge (Logic)
        // Simplified for UI demo: Check overlap count
        long matchCount = 0;
        List<String> recipeIngredients = recipe.getIngredients();
        if (recipeIngredients != null) {
            matchCount = recipeIngredients.stream()
                .filter(rIng -> ingredientList.stream().anyMatch(uIng -> rIng.toLowerCase().contains(uIng.toLowerCase())))
                .count();
        }
        
        Label badge = new Label(matchCount > 0 ? matchCount + " ingredients match" : "Suggested");
        badge.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-padding: 2 6; -fx-background-radius: 4px; -fx-font-size: 11px; -fx-font-weight: 600;");
        card.getChildren().add(badge);
        
        // 4. Meta Info
        HBox metaBox = new HBox(10);
        metaBox.setAlignment(Pos.CENTER_LEFT);
        
        Label calLabel = new Label((recipe.getNutritionInfo() != null ? recipe.getNutritionInfo().getCalories() : "N/A") + " kcal");
        calLabel.getStyleClass().add("text-gray-500");
        calLabel.setStyle("-fx-font-size: 11px;");
        
        metaBox.getChildren().add(calLabel);
        card.getChildren().add(metaBox);

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
