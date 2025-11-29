package com.mealplanner.view;

import com.mealplanner.entity.Recipe;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.BrowseRecipeController;
import com.mealplanner.interface_adapter.view_model.RecipeBrowseViewModel;
import com.mealplanner.interface_adapter.view_model.RecipeDetailViewModel;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class BrowseRecipeView extends BorderPane implements PropertyChangeListener {
    private final RecipeBrowseViewModel viewModel;
    private final BrowseRecipeController controller;
    private final ViewManagerModel viewManagerModel;
    private final RecipeDetailViewModel recipeDetailViewModel;

    private TextField searchField;
    @SuppressWarnings("unused")
    private Button searchButton;
    
    // Filters
    private FlowPane categoryFilters;
    private ToggleGroup categoryGroup;
    private String selectedCategory = "All";

    // OPTIMIZATION: Store all recipes for client-side filtering
    private List<Recipe> allRecipes = new ArrayList<>();

    // Result Components
    private ScrollPane listScrollPane;
    private FlowPane listPanel; 
    private VBox loadingPanel;
    private VBox emptyPanel;
    private Label errorLabel;
    private Label countLabel; // "Showing 9 of 16 recipes"

    public BrowseRecipeView(RecipeBrowseViewModel viewModel, BrowseRecipeController controller, ViewManagerModel viewManagerModel, RecipeDetailViewModel recipeDetailViewModel) {
        if (viewModel == null) throw new IllegalArgumentException("ViewModel cannot be null");
        if (controller == null) throw new IllegalArgumentException("Controller cannot be null");
        if (viewManagerModel == null) throw new IllegalArgumentException("ViewManagerModel cannot be null");
        if (recipeDetailViewModel == null) throw new IllegalArgumentException("RecipeDetailViewModel cannot be null");
        
        this.viewModel = viewModel;
        this.controller = controller;
        this.viewManagerModel = viewManagerModel;
        this.recipeDetailViewModel = recipeDetailViewModel;
        
        viewModel.addPropertyChangeListener(this);

        // Root Styles
        getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));
        setBackground(new Background(new BackgroundFill(Color.web("#f7f8f9"), CornerRadii.EMPTY, Insets.EMPTY)));

        // Title & Subtitle
        VBox headerBox = new VBox(8);
        Label titleLabel = new Label("Recipe Catalog");
        titleLabel.getStyleClass().add("section-title");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: -fx-theme-foreground;");
        
        Label subTitle = new Label("Browse and discover new recipes to try");
        subTitle.getStyleClass().add("text-gray-500");
        subTitle.setStyle("-fx-font-size: 14px;");
        
        headerBox.getChildren().addAll(titleLabel, subTitle);

        // Saved Button (Top Right) - Mockup
        Button savedBtn = new Button("3 Saved");
        savedBtn.setStyle("-fx-background-color: #84cc16; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 8px; -fx-padding: 8 16; -fx-cursor: hand;");
        Node bookmarkIcon = SvgIconLoader.loadIcon("/svg/book-fill.svg", 14, Color.WHITE);
        if (bookmarkIcon != null) {
            savedBtn.setGraphic(bookmarkIcon);
            savedBtn.setGraphicTextGap(8);
        }
        
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        topBar.getChildren().addAll(headerBox, spacer, savedBtn);
        
        VBox topSection = new VBox(24);
        topSection.getChildren().addAll(topBar, createSearchPanel());
        setTop(topSection);

        // Results Count Label
        countLabel = new Label("");
        countLabel.setStyle("-fx-text-fill: -fx-theme-muted-foreground; -fx-font-size: 14px; -fx-font-weight: 500;");
        // Add icon to count label? Mockup has a little zigzag icon
        
        VBox resultsHeader = new VBox(countLabel);
        resultsHeader.setPadding(new Insets(20, 0, 10, 0));
        
        // Results List
        createResultsPanel();
        
        VBox centerBox = new VBox(resultsHeader, listScrollPane);
        VBox.setVgrow(listScrollPane, Priority.ALWAYS); // Ensure scroll pane takes available space
        setCenter(centerBox);
        
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
        
        // 1. Search Input
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle("-fx-background-color: white; -fx-border-color: -fx-color-gray-200; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 0 12px;");
        searchBox.setPrefHeight(48);

        Node searchIcon = SvgIconLoader.loadIcon("/svg/search.svg", 20, Color.web("#9ca3af"));
        if (searchIcon != null) searchBox.getChildren().add(searchIcon);

        searchField = new TextField();
        searchField.setPromptText("Search recipes by name (e.g., Pasta, Kimchi Stew)...");
        searchField.setStyle("-fx-background-color: transparent; -fx-border-width: 0; -fx-font-size: 14px;");
        searchField.setPrefHeight(40);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                performSearch();
            }
        });
        
        searchBox.getChildren().add(searchField);

        // 2. Category Filters
        VBox filterBox = new VBox(10);
        
        HBox filterHeader = new HBox(6);
        filterHeader.setAlignment(Pos.CENTER_LEFT);
        Node filterIcon = SvgIconLoader.loadIcon("/svg/settings-sliders.svg", 14, Color.web("#6b7280")); // Funnel/filter icon fallback
        Label filterLabel = new Label("Filter by category");
        filterLabel.getStyleClass().add("text-gray-500");
        filterLabel.setStyle("-fx-font-size: 13px;");
        if (filterIcon != null) filterHeader.getChildren().add(filterIcon);
        filterHeader.getChildren().add(filterLabel);
        
        categoryFilters = new FlowPane();
        categoryFilters.setHgap(10);
        categoryFilters.setVgap(10);
        
        categoryGroup = new ToggleGroup();
        
        // Categories
        addCategoryFilter("All", true, null);
        addCategoryFilter("Breakfast", false, "/svg/mug-hot.svg");
        addCategoryFilter("Lunch", false, "/svg/brightness.svg");
        addCategoryFilter("Dinner", false, "/svg/moon.svg");
        addCategoryFilter("Snacks", false, "/svg/apple.svg");
        addCategoryFilter("Desserts", false, "/svg/heart.svg");
        addCategoryFilter("Vegetarian", false, "/svg/leaf.svg");
        addCategoryFilter("Vegan", false, "/svg/leaf.svg");

        filterBox.getChildren().addAll(filterHeader, categoryFilters);

        panel.getChildren().addAll(searchBox, filterBox);
        return panel;
    }

    private void addCategoryFilter(String name, boolean isSelected, String iconPath) {
        ToggleButton btn = new ToggleButton(name);
        btn.setToggleGroup(categoryGroup);
        btn.setSelected(isSelected);
        
        // Default Style
        String defaultStyle = "-fx-background-color: white; -fx-text-fill: -fx-color-gray-600; -fx-border-color: -fx-color-gray-200; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand;";
        // Selected Style (Green)
        String selectedStyle = "-fx-background-color: #84cc16; -fx-text-fill: white; -fx-border-color: #84cc16; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8 16; -fx-font-size: 13px; -fx-font-weight: 600; -fx-cursor: hand;";
        
        btn.setStyle(isSelected ? selectedStyle : defaultStyle);
        
        if (iconPath != null) {
            Node icon = SvgIconLoader.loadIcon(iconPath, 16, isSelected ? Color.WHITE : Color.web("#6b7280"));
            if (icon != null) {
                btn.setGraphic(icon);
                btn.setGraphicTextGap(8);
            }
        } else if (name.equals("All") && isSelected) {
             // "All" might have a specific icon or just text. Mockup shows icon.
             Node icon = SvgIconLoader.loadIcon("/svg/apps.svg", 16, Color.WHITE); // Grid icon
             if (icon != null) btn.setGraphic(icon);
             btn.setGraphicTextGap(8);
        } else if (name.equals("All")) {
             Node icon = SvgIconLoader.loadIcon("/svg/apps.svg", 16, Color.web("#6b7280"));
             if (icon != null) btn.setGraphic(icon);
             btn.setGraphicTextGap(8);
        }

        btn.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                btn.setStyle(selectedStyle);
                selectedCategory = name;
                // Update icon color to white
                if (btn.getGraphic() != null) {
                     // Reloading icon with white color is tricky without storing path.
                     // For now, assume simple toggle logic or just keep it simple.
                     // (In a real app, we'd have a custom button class to handle state/icon/color)
                     // Let's try to reload if we know the path or if it's "All"
                     if (iconPath != null) {
                        btn.setGraphic(SvgIconLoader.loadIcon(iconPath, 16, Color.WHITE));
                     } else if (name.equals("All")) {
                        btn.setGraphic(SvgIconLoader.loadIcon("/svg/apps.svg", 16, Color.WHITE));
                     }
                }
                // OPTIMIZATION: Apply client-side filtering instead of re-fetching from API
                applyClientSideFilter();
            } else {
                btn.setStyle(defaultStyle);
                // Update icon color to gray
                 if (btn.getGraphic() != null) {
                     if (iconPath != null) {
                        btn.setGraphic(SvgIconLoader.loadIcon(iconPath, 16, Color.web("#6b7280")));
                     } else if (name.equals("All")) {
                        btn.setGraphic(SvgIconLoader.loadIcon("/svg/apps.svg", 16, Color.web("#6b7280")));
                     }
                }
            }
        });

        categoryFilters.getChildren().add(btn);
    }

    private void createResultsPanel() {
        // 1. List View
        listPanel = new FlowPane();
        listPanel.setHgap(24);
        listPanel.setVgap(24);
        listPanel.setPadding(new Insets(10));
        listPanel.setBackground(Background.EMPTY);
        listPanel.setAlignment(Pos.TOP_LEFT);

        listScrollPane = new ScrollPane(listPanel);
        listScrollPane.setFitToWidth(true);
        listScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        listScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        listScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        listScrollPane.setMinHeight(400);
        
        // Increase scroll speed
        listScrollPane.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() != 0) {
                double delta = event.getDeltaY() * 3.0;
                double height = listScrollPane.getContent().getBoundsInLocal().getHeight();
                double vHeight = listScrollPane.getViewportBounds().getHeight();
                
                double scrollableHeight = height - vHeight;
                if (scrollableHeight > 0) {
                    double vValueShift = -delta / scrollableHeight;
                    double nextVvalue = listScrollPane.getVvalue() + vValueShift;
                    
                    if (nextVvalue >= 0 && nextVvalue <= 1.0 || (listScrollPane.getVvalue() > 0 && listScrollPane.getVvalue() < 1.0)) {
                        listScrollPane.setVvalue(Math.min(Math.max(nextVvalue, 0), 1));
                        event.consume();
                    }
                }
            }
        });

        // 2. Loading
        loadingPanel = new VBox(15);
        loadingPanel.setAlignment(Pos.CENTER);
        ProgressIndicator spinner = new ProgressIndicator();
        Label loadingLabel = new Label("Searching recipes...");
        loadingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: -fx-theme-muted-foreground;");
        loadingPanel.getChildren().addAll(spinner, loadingLabel);

        // 3. Empty
        emptyPanel = new VBox(15);
        emptyPanel.setAlignment(Pos.CENTER);
        
        Label iconLabel = new Label("🍳");
        iconLabel.setStyle("-fx-font-size: 48px;");
        
        Label emptyLabel = new Label("Start searching to find delicious recipes!");
        emptyLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -fx-theme-muted-foreground;");
        
        emptyPanel.getChildren().addAll(iconLabel, emptyLabel);
        
        // Initially show empty or welcome state?
        // Since we might want to show some default recipes, let's trigger an initial search or show empty.
        // Let's show empty state initially.
        listPanel.getChildren().add(emptyPanel);
    }

    private void performSearch() {
        String query = StringUtil.safeTrim(searchField.getText());
        
        // Mock Logic for "All" vs "Category" since API might not support category filtering directly in this demo
        // In a real implementation, pass category to controller
        
        // If query is empty and category is "All", maybe show popular/random?
        // For now, ensure we search something.
        
        if (StringUtil.isNullOrEmpty(query) && selectedCategory.equals("All")) {
            // Maybe just search "chicken" or generic term to fill catalog?
            // Or wait for user input. 
            // Let's prompt user.
            if (listPanel.getChildren().isEmpty()) {
                 listPanel.getChildren().add(emptyPanel);
            }
            return;
        }
        
        String effectiveQuery = StringUtil.isNullOrEmpty(query) ? selectedCategory : query;
        if (selectedCategory.equals("All") && StringUtil.isNullOrEmpty(query)) effectiveQuery = "pasta"; // Fallback demo

        errorLabel.setText("");
        
        // Show Loading
        listPanel.getChildren().clear();
        listPanel.getChildren().add(loadingPanel);

        // Run search in background
        final String finalQuery = effectiveQuery;
        new Thread(() -> {
            try {
                // Default to 10 results for now
                controller.execute(finalQuery, 12); 
            } catch (IOException ex) {
                Platform.runLater(() -> {
                    errorLabel.setText("Network error: " + ex.getMessage());
                    listPanel.getChildren().clear();
                    listPanel.getChildren().add(emptyPanel);
                });
            }
        }).start();
    }

    private void displayRecipes(List<Recipe> recipes) {
        Platform.runLater(() -> {
            // OPTIMIZATION: Store all recipes for client-side filtering
            allRecipes = recipes != null ? new ArrayList<>(recipes) : new ArrayList<>();

            // Apply current filter
            applyClientSideFilter();
        });
    }

    /**
     * OPTIMIZATION: Apply client-side category filtering without re-fetching from API.
     */
    private void applyClientSideFilter() {
        listPanel.getChildren().clear();

        if (allRecipes == null || allRecipes.isEmpty()) {
            emptyPanel.getChildren().set(1, new Label("No recipes found. Try a different term."));
            listPanel.getChildren().add(emptyPanel);
            countLabel.setText("Showing 0 recipes");
            return;
        }

        // Filter recipes based on selected category
        List<Recipe> filteredRecipes = allRecipes.stream()
            .filter(recipe -> matchesCategory(recipe, selectedCategory))
            .collect(java.util.stream.Collectors.toList());

        if (filteredRecipes.isEmpty()) {
            emptyPanel.getChildren().set(1, new Label("No recipes found in this category."));
            listPanel.getChildren().add(emptyPanel);
            countLabel.setText("Showing 0 recipes");
        } else {
            countLabel.setText("Showing " + filteredRecipes.size() + " of " + allRecipes.size() + " recipes");

            // Mockup shows specific "Showing 9 of 16 recipes" style
            Node trendIcon = SvgIconLoader.loadIcon("/svg/chart.svg", 16, Color.web("#84cc16")); // Green zigzag
            if (trendIcon != null) {
                countLabel.setGraphic(trendIcon);
                countLabel.setGraphicTextGap(8);
            }

            for (Recipe recipe : filteredRecipes) {
                listPanel.getChildren().add(createRecipeCard(recipe));
            }
        }
    }

    /**
     * Check if a recipe matches the given category filter.
     */
    private boolean matchesCategory(Recipe recipe, String category) {
        if (category == null || category.equals("All")) {
            return true;
        }

        String recipeName = recipe.getName().toLowerCase();

        // Simple category matching based on recipe name
        // In a real application, this would use recipe tags or categories from the API
        switch (category) {
            case "Breakfast":
                return recipeName.contains("breakfast") || recipeName.contains("pancake")
                    || recipeName.contains("toast") || recipeName.contains("oatmeal")
                    || recipeName.contains("egg");
            case "Lunch":
                return recipeName.contains("lunch") || recipeName.contains("sandwich")
                    || recipeName.contains("salad") || recipeName.contains("soup");
            case "Dinner":
                return recipeName.contains("dinner") || recipeName.contains("steak")
                    || recipeName.contains("chicken") || recipeName.contains("fish")
                    || recipeName.contains("pasta");
            case "Snacks":
                return recipeName.contains("snack") || recipeName.contains("chip")
                    || recipeName.contains("dip");
            case "Desserts":
                return recipeName.contains("dessert") || recipeName.contains("cake")
                    || recipeName.contains("cookie") || recipeName.contains("ice cream")
                    || recipeName.contains("chocolate");
            case "Vegetarian":
                return !recipeName.contains("meat") && !recipeName.contains("chicken")
                    && !recipeName.contains("beef") && !recipeName.contains("pork")
                    && !recipeName.contains("fish");
            case "Vegan":
                return !recipeName.contains("meat") && !recipeName.contains("chicken")
                    && !recipeName.contains("beef") && !recipeName.contains("egg")
                    && !recipeName.contains("cheese") && !recipeName.contains("milk");
            default:
                return true;
        }
    }
    
    private VBox createRecipeCard(Recipe recipe) {
        VBox card = new VBox();
        card.getStyleClass().add("meal-card");
        card.setPrefWidth(300); // Wider card as per mockup
        card.setMinWidth(300);
        card.setSpacing(0);
        card.setPadding(new Insets(0)); // Padding handled inside content box
        card.setCursor(Cursor.HAND);

        // 1. Image Area (with Tags overlay)
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(180);
        imageContainer.setStyle("-fx-background-color: #e5e7eb; -fx-background-radius: 12px 12px 0 0;");
        imageContainer.setClip(new javafx.scene.shape.Rectangle(0, 0, 300, 180));
        ((javafx.scene.shape.Rectangle) imageContainer.getClip()).setArcWidth(12);
        ((javafx.scene.shape.Rectangle) imageContainer.getClip()).setArcHeight(12);
        
        // Load recipe image if available
        String imageUrl = recipe.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                ImageView imageView = new ImageView();
                imageView.setFitWidth(300);
                imageView.setFitHeight(180);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                imageView.setCache(true);
                
                Image image = new Image(imageUrl, true); // true = load in background
                imageView.setImage(image);
                imageContainer.getChildren().add(0, imageView); // Add image as first child (behind overlays)
            } catch (Exception e) {
                // If image loading fails, fall back to placeholder background
            }
        }
        
        // Overlay Tag (e.g., "Easy", "Medium") - Random for demo
        Label difficultyTag = new Label(new Random().nextBoolean() ? "Easy" : "Medium");
        difficultyTag.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-text-fill: #1f2937; -fx-font-size: 11px; -fx-font-weight: 600; -fx-padding: 4 8; -fx-background-radius: 4px;");
        StackPane.setAlignment(difficultyTag, Pos.TOP_LEFT);
        StackPane.setMargin(difficultyTag, new Insets(12));
        
        // Save Button Overlay
        Button saveBtn = new Button();
        saveBtn.setStyle("-fx-background-color: white; -fx-background-radius: 50%; -fx-min-width: 32px; -fx-min-height: 32px; -fx-max-width: 32px; -fx-max-height: 32px; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        Node bookmarkIcon = SvgIconLoader.loadIcon("/svg/book.svg", 14, Color.web("#374151"));
        saveBtn.setGraphic(bookmarkIcon);
        StackPane.setAlignment(saveBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(saveBtn, new Insets(12));

        // Add overlays (they will be on top of the image)
        imageContainer.getChildren().addAll(difficultyTag, saveBtn);
        card.getChildren().add(imageContainer);

        // 2. Content Area
        VBox content = new VBox(12);
        content.setPadding(new Insets(16));
        
        // Title
        Label title = new Label(recipe.getName());
        title.getStyleClass().add("text-gray-900");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        title.setWrapText(true);
        
        // Meta Row (Time, Cals, Servings)
        HBox metaRow = new HBox(12);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        
        // Time
        Label timeLabel = createMetaLabel("30 min", "/svg/clock.svg", "#3b82f6", "#eff6ff"); // Blue theme
        // Cals
        String cals = (recipe.getNutritionInfo() != null) ? recipe.getNutritionInfo().getCalories() + "" : "350";
        Label calLabel = createMetaLabel(cals, "/svg/fire-flame.svg", "#f97316", "#fff7ed"); // Orange theme
        // Servings
        Label servLabel = createMetaLabel(String.valueOf(recipe.getServingSize()), "/svg/users.svg", "#a855f7", "#faf5ff"); // Purple theme
        
        metaRow.getChildren().addAll(timeLabel, calLabel, servLabel);
        
        // Tags Row
        FlowPane tagsRow = new FlowPane();
        tagsRow.setHgap(8);
        tagsRow.setVgap(8);
        
        tagsRow.getChildren().add(createTag("Vegetarian", false));
        tagsRow.getChildren().add(createTag("Italian", false));
        
        // Add to Plan Button
        Button addBtn = new Button("Add to Plan");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setStyle("-fx-background-color: #4ade80; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 8px; -fx-cursor: hand; -fx-padding: 10;");
        Node plusIcon = SvgIconLoader.loadIcon("/svg/plus.svg", 16, Color.WHITE);
        if (plusIcon != null) {
            addBtn.setGraphic(plusIcon);
            addBtn.setGraphicTextGap(8);
        }

        content.getChildren().addAll(title, metaRow, tagsRow, addBtn);
        card.getChildren().add(content);

        // Hover Effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-border-color: -fx-theme-primary; -fx-border-width: 1px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);");
        });
        card.setOnMouseExited(e -> {
            card.setStyle(""); // Reset to default CSS
        });

        card.setOnMouseClicked(e -> openRecipeDetail(recipe));
        addBtn.setOnAction(e -> {
            openRecipeDetail(recipe);
            e.consume();
        });

        return card;
    }
    
    private Label createMetaLabel(String text, String iconPath, String colorHex, String bgHex) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + colorHex + "; -fx-background-color: " + bgHex + "; -fx-padding: 4 8; -fx-background-radius: 6px; -fx-font-size: 12px; -fx-font-weight: 600;");
        Node icon = SvgIconLoader.loadIcon(iconPath, 12, Color.web(colorHex));
        if (icon != null) {
            label.setGraphic(icon);
            label.setGraphicTextGap(4);
        }
        return label;
    }
    
    private Label createTag(String text, boolean isOutline) {
        Label label = new Label(text);
        if (isOutline) {
            label.setStyle("-fx-text-fill: #4b5563; -fx-border-color: #e5e7eb; -fx-border-radius: 12px; -fx-padding: 2 8; -fx-font-size: 11px;");
        } else {
            label.setStyle("-fx-text-fill: #166534; -fx-background-color: #dcfce7; -fx-background-radius: 12px; -fx-padding: 2 8; -fx-font-size: 11px; -fx-font-weight: 500;");
        }
        Node icon = SvgIconLoader.loadIcon("/svg/leaf.svg", 10, Color.web("#166534"));
        if (icon != null && !isOutline) {
            label.setGraphic(icon);
            label.setGraphicTextGap(4);
        }
        return label;
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
                        listPanel.getChildren().clear();
                        listPanel.getChildren().add(emptyPanel);
                    } else {
                        errorLabel.setText("");
                    }
                    break;
            }
        });
    }

    private void openRecipeDetail(Recipe recipe) {
        if (recipe == null || recipeDetailViewModel == null || viewManagerModel == null) {
            return;
        }
        recipeDetailViewModel.setRecipe(recipe);
        viewManagerModel.setActiveView(ViewManager.RECIPE_DETAIL_VIEW);
    }
}
