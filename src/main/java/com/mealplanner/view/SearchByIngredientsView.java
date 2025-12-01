package com.mealplanner.view;

import com.mealplanner.entity.Recipe;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.SearchByIngredientsController;
import com.mealplanner.interface_adapter.view_model.RecipeSearchViewModel;
import com.mealplanner.interface_adapter.view_model.RecipeDetailViewModel;
import com.mealplanner.repository.RecipeRepository;
import com.mealplanner.exception.DataAccessException;
import com.mealplanner.util.StringUtil;
import com.mealplanner.util.ImageCacheManager;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Stop;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchByIngredientsView extends BorderPane implements PropertyChangeListener {
    private final RecipeSearchViewModel viewModel;
    private final SearchByIngredientsController controller;
    private final ViewManagerModel viewManagerModel;
    private final RecipeDetailViewModel recipeDetailViewModel;
    private final RecipeRepository recipeRepository;
    private final ImageCacheManager imageCache = ImageCacheManager.getInstance();

    private TextField ingredientsField;
    private FlowPane chipsContainer;
    private Button searchButton;
    private ObservableList<String> ingredientList;

    // Result Components
    private StackPane resultsContainer; 
    private ScrollPane listScrollPane;
    private FlowPane listPanel; 
    private VBox loadingPanel;
    private VBox emptyPanel;
    private VBox errorPanel;
    private Label errorLabel;
    private HBox resultsCountLabel; // 검색 결과 개수 표시

    // Filters
    private FlowPane quickFiltersContainer;
    private List<String> activeFilters = new ArrayList<>();
    
    // Popular Ingredients buttons map for removal
    private java.util.Map<String, Button> popularIngredientButtons = new java.util.HashMap<>();
    private FlowPane popularTags;
    private String[] popularItems = {"Egg", "Chicken", "Tomato", "Onion", "Garlic", "Pasta", "Rice", "Milk", "Cheese", "Potato"};

    // OPTIMIZATION: Store all recipes for client-side filtering
    private List<Recipe> allRecipes = new ArrayList<>();

    public SearchByIngredientsView(SearchByIngredientsController controller, RecipeSearchViewModel viewModel, ViewManagerModel viewManagerModel, RecipeDetailViewModel recipeDetailViewModel, RecipeRepository recipeRepository) {
        if (viewModel == null) throw new IllegalArgumentException("ViewModel cannot be null");
        if (controller == null) throw new IllegalArgumentException("Controller cannot be null");
        if (viewManagerModel == null) throw new IllegalArgumentException("ViewManagerModel cannot be null");
        if (recipeDetailViewModel == null) throw new IllegalArgumentException("RecipeDetailViewModel cannot be null");
        if (recipeRepository == null) throw new IllegalArgumentException("RecipeRepository cannot be null");

        this.viewModel = viewModel;
        this.controller = controller;
        this.viewManagerModel = viewManagerModel;
        this.recipeDetailViewModel = recipeDetailViewModel;
        this.recipeRepository = recipeRepository;
        this.ingredientList = FXCollections.observableArrayList();

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
        
        // Load local database recipes on initialization
        loadLocalRecipes();
    }
    
    /**
     * Loads recipes from local database and merges with API results.
     */
    private void loadLocalRecipes() {
        if (recipeRepository == null) {
            return;
        }
        
        new Thread(() -> {
            try {
                List<Recipe> localRecipes = recipeRepository.findAll();
                if (localRecipes != null && !localRecipes.isEmpty()) {
                    Platform.runLater(() -> {
                        // Merge with existing recipes (avoid duplicates by recipe ID)
                        List<Recipe> mergedRecipes = new ArrayList<>(allRecipes);
                        for (Recipe localRecipe : localRecipes) {
                            // Check if recipe already exists (by ID or name)
                            boolean exists = mergedRecipes.stream()
                                .anyMatch(r -> r.getRecipeId() != null && r.getRecipeId().equals(localRecipe.getRecipeId()) ||
                                              (r.getName() != null && r.getName().equals(localRecipe.getName())));
                            if (!exists) {
                                mergedRecipes.add(localRecipe);
                            }
                        }
                        allRecipes = mergedRecipes;
                        displayRecipes(allRecipes);
                    });
                }
            } catch (DataAccessException e) {
                // Silently fail - local recipes are optional
            } catch (Exception e) {
                // Silently fail - local recipes are optional
            }
        }).start();
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
        searchFieldContainer.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 0 12px;");
        searchFieldContainer.setPrefHeight(48);
        HBox.setHgrow(searchFieldContainer, Priority.ALWAYS);

        Node searchIcon = SvgIconLoader.loadIcon("/svg/search.svg", 20, Color.web("#9ca3af"));
        if (searchIcon != null) searchFieldContainer.getChildren().add(searchIcon);

        ingredientsField = new TextField();
        ingredientsField.setPromptText("Type an ingredient and press Enter...");
        ingredientsField.setStyle("-fx-background-color: transparent; -fx-border-width: 0; -fx-font-size: 14px;");
        ingredientsField.setPrefHeight(40);
        HBox.setHgrow(ingredientsField, Priority.ALWAYS);
        
        searchFieldContainer.getChildren().add(ingredientsField);
        
        // Focus effect - 연두색 링
        ingredientsField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                searchFieldContainer.setStyle("-fx-background-color: white; -fx-border-color: #84cc16; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 0 12px;");
            } else {
                searchFieldContainer.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 0 12px;");
            }
        });

        // Handle Enter Key
        ingredientsField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                addIngredientChip(ingredientsField.getText());
                ingredientsField.clear();
            }
        });

        searchButton = new Button("Search");
        updateSearchButtonStyle();
        searchButton.setPrefHeight(48);
        searchButton.setOnAction(e -> performSearch());

        inputBox.getChildren().addAll(searchFieldContainer, searchButton);

        // 2. Popular Ingredients Tags (재료가 없을 때만 표시)
        VBox popularSection = new VBox(8);
        
        HBox popularHeader = new HBox(6);
        popularHeader.setAlignment(Pos.CENTER_LEFT);
        Node sparkIcon = SvgIconLoader.loadIcon("/svg/sparkles.svg", 14, Color.web("#6b7280"));
        Label popularLabel = new Label("Popular ingredients");
        popularLabel.getStyleClass().add("text-gray-500");
        popularLabel.setStyle("-fx-font-size: 13px;");
        if (sparkIcon != null) popularHeader.getChildren().add(sparkIcon);
        popularHeader.getChildren().add(popularLabel);
        
        popularTags = new FlowPane();
        popularTags.setHgap(8);
        popularTags.setVgap(8);
        
        for (String item : popularItems) {
            Button tagBtn = createPopularIngredientButton(item);
            popularTags.getChildren().add(tagBtn);
            popularIngredientButtons.put(item, tagBtn);
        }
        
        popularSection.getChildren().addAll(popularHeader, popularTags);
        // Popular ingredients는 항상 표시

        // 3. Your Ingredients 섹션 (재료가 있을 때만 표시)
        VBox yourIngredientsSection = new VBox(8);
        
        HBox yourIngredientsHeader = new HBox();
        yourIngredientsHeader.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(yourIngredientsHeader, Priority.ALWAYS);
        
        Label yourIngredientsLabel = new Label();
        yourIngredientsLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;");
        yourIngredientsLabel.textProperty().bind(javafx.beans.binding.Bindings.createStringBinding(
            () -> "Your ingredients (" + ingredientList.size() + ")",
            javafx.beans.binding.Bindings.size(ingredientList)
        ));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button clearAllBtn = new Button("Clear all");
        clearAllBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 4 8;");
        clearAllBtn.setOnMouseEntered(e -> clearAllBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #dc2626; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 4 8;"));
        clearAllBtn.setOnMouseExited(e -> clearAllBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 4 8;"));
            clearAllBtn.setOnAction(e -> {
            if (chipsContainer != null) {
                chipsContainer.getChildren().clear();
            }
            if (ingredientList != null) {
                ingredientList.clear();
            }
            updateSearchButtonStyle();
            // Popular ingredients 버튼들을 다시 추가
            if (popularTags != null && popularIngredientButtons != null) {
                for (String item : popularItems) {
                    if (!popularIngredientButtons.containsKey(item)) {
                        Button tagBtn = createPopularIngredientButton(item);
                        popularTags.getChildren().add(tagBtn);
                        popularIngredientButtons.put(item, tagBtn);
                    }
                }
            }
        });
        
        yourIngredientsHeader.getChildren().addAll(yourIngredientsLabel, spacer, clearAllBtn);
        
        chipsContainer = new FlowPane();
        chipsContainer.setHgap(8);
        chipsContainer.setVgap(8);
        
        yourIngredientsSection.getChildren().addAll(yourIngredientsHeader, chipsContainer);
        // 재료가 있을 때만 표시
        yourIngredientsSection.visibleProperty().bind(javafx.beans.binding.Bindings.isNotEmpty(ingredientList));
        yourIngredientsSection.managedProperty().bind(yourIngredientsSection.visibleProperty());

        // 4. Quick Filters
        VBox filtersSection = new VBox(8);
        
        HBox filtersHeader = new HBox(6);
        filtersHeader.setAlignment(Pos.CENTER_LEFT);
        Node filterIcon = SvgIconLoader.loadIcon("/svg/filter.svg", 14, Color.web("#6b7280"));
        Label filtersLabel = new Label("Filter by Category");
        filtersLabel.getStyleClass().add("text-gray-500");
        filtersLabel.setStyle("-fx-font-size: 13px;");
        if (filterIcon != null) filtersHeader.getChildren().add(filterIcon);
        filtersHeader.getChildren().add(filtersLabel);
        
        quickFiltersContainer = new FlowPane();
        quickFiltersContainer.setHgap(10);
        quickFiltersContainer.setVgap(10);
        
        // Filters with Icons
        addFilterButton("Breakfast", "/svg/mug-hot.svg");
        addFilterButton("Lunch", "/svg/brightness.svg");
        addFilterButton("Dinner", "/svg/moon.svg");
        addFilterButton("Vegetarian", "/svg/leaf.svg");
        addFilterButton("Quick (< 30min)", "/svg/bolt.svg");
        
        filtersSection.getChildren().addAll(filtersHeader, quickFiltersContainer);

        panel.getChildren().addAll(inputBox, yourIngredientsSection, popularSection, filtersSection);
        return panel;
    }

    /**
     * Popular Ingredient 버튼을 생성하는 헬퍼 메서드
     */
    private Button createPopularIngredientButton(String item) {
        Button tagBtn = new Button("+ " + item);
        tagBtn.setStyle("-fx-background-color: #f9fafb; -fx-text-fill: #374151; -fx-background-radius: 20px; -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 20px; -fx-padding: 6 12; -fx-font-size: 13px; -fx-cursor: hand;");
        tagBtn.setOnAction(e -> addIngredientChip(item));
        
        // Hover effect - 연두색
        tagBtn.setOnMouseEntered(e -> tagBtn.setStyle("-fx-background-color: #f7fee7; -fx-text-fill: #4d7c0f; -fx-background-radius: 20px; -fx-border-color: #d9f99d; -fx-border-width: 1px; -fx-border-radius: 20px; -fx-padding: 6 12; -fx-font-size: 13px; -fx-cursor: hand;"));
        tagBtn.setOnMouseExited(e -> tagBtn.setStyle("-fx-background-color: #f9fafb; -fx-text-fill: #374151; -fx-background-radius: 20px; -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 20px; -fx-padding: 6 12; -fx-font-size: 13px; -fx-cursor: hand;"));
        
        return tagBtn;
    }
    
    private void addFilterButton(String text, String iconPath) {
        ToggleButton btn = new ToggleButton(text);
        btn.setStyle("-fx-background-color: white; -fx-text-fill: -fx-color-gray-600; -fx-border-color: -fx-color-gray-200; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8 12; -fx-font-size: 13px; -fx-cursor: hand;");
        
        Node icon = SvgIconLoader.loadIcon(iconPath, 16, Color.web("#6b7280"));
        if (icon != null) {
            btn.setGraphic(icon);
            btn.setGraphicTextGap(8);
        }

        // Hover effect (only when not selected)
        btn.setOnMouseEntered(e -> {
            if (!btn.isSelected()) {
                btn.setStyle("-fx-background-color: #f2f4f5; -fx-text-fill: -fx-color-gray-600; -fx-border-color: #d7d9de; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8 12; -fx-font-size: 13px; -fx-cursor: hand;");
                if (btn.getGraphic() != null) {
                    Node hoverIcon = SvgIconLoader.loadIcon(iconPath, 16, Color.web("#6b7280"));
                    btn.setGraphic(hoverIcon);
                }
            }
        });
        btn.setOnMouseExited(e -> {
            if (!btn.isSelected()) {
                btn.setStyle("-fx-background-color: white; -fx-text-fill: -fx-color-gray-600; -fx-border-color: -fx-color-gray-200; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8 12; -fx-font-size: 13px; -fx-cursor: hand;");
                if (btn.getGraphic() != null) {
                    Node normalIcon = SvgIconLoader.loadIcon(iconPath, 16, Color.web("#6b7280"));
                    btn.setGraphic(normalIcon);
                }
            }
        });

        btn.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // Active State
                btn.setStyle("-fx-background-color: #68CA2A; -fx-text-fill: #ffffff; -fx-border-color: #68CA2A; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8 12; -fx-font-size: 13px; -fx-cursor: hand;");
                if (btn.getGraphic() != null) {
                     Node activeIcon = SvgIconLoader.loadIcon(iconPath, 16, Color.web("#ffffff"));
                     btn.setGraphic(activeIcon);
                }
                if (!activeFilters.contains(text)) {
                    activeFilters.add(text);
                }
                // OPTIMIZATION: Apply client-side filtering instead of re-fetching from API
                if (!allRecipes.isEmpty()) {
                    displayRecipes(allRecipes);
                }
            } else {
                // Inactive State
                btn.setStyle("-fx-background-color: white; -fx-text-fill: -fx-color-gray-600; -fx-border-color: -fx-color-gray-200; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8 12; -fx-font-size: 13px; -fx-cursor: hand;");
                if (btn.getGraphic() != null) {
                     Node inactiveIcon = SvgIconLoader.loadIcon(iconPath, 16, Color.web("#6b7280"));
                     btn.setGraphic(inactiveIcon);
                }
                activeFilters.remove(text);
                // OPTIMIZATION: Apply client-side filtering instead of re-fetching from API
                if (!allRecipes.isEmpty()) {
                    displayRecipes(allRecipes);
                }
            }
            // Trigger search logic if needed
        });
        
        quickFiltersContainer.getChildren().add(btn);
    }

    private void addIngredientChip(String text) {
        if (text == null || ingredientList == null || chipsContainer == null) {
            return;
        }
        
        String ingredient = StringUtil.safeTrim(text);
        if (StringUtil.isNullOrEmpty(ingredient)) {
            return;
        }
        
        // Case-insensitive check
        boolean exists = ingredientList.stream().anyMatch(i -> i != null && i.equalsIgnoreCase(ingredient));
        if (exists) {
            return;
        }

        ingredientList.add(ingredient);
        
        // Remove the button from Popular Ingredients list (case-insensitive)
        if (popularIngredientButtons != null && popularTags != null) {
            String matchingKey = null;
            for (String key : popularIngredientButtons.keySet()) {
                if (key != null && key.equalsIgnoreCase(ingredient)) {
                    matchingKey = key;
                    break;
                }
            }
            if (matchingKey != null) {
                Button buttonToRemove = popularIngredientButtons.remove(matchingKey);
                if (buttonToRemove != null) {
                    popularTags.getChildren().remove(buttonToRemove);
                }
            }
        }
        
        // Update search button state
        updateSearchButtonStyle();

        // Create Chip UI - Popular ingredients와 동일한 크기 및 스타일
        HBox chip = new HBox(6);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 20px; -fx-padding: 6 12; -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 20px;");
        chip.setOnMouseEntered(e -> chip.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 20px; -fx-padding: 6 12; -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 20px;"));
        chip.setOnMouseExited(e -> chip.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 20px; -fx-padding: 6 12; -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 20px;"));
        
        Label label = new Label(ingredient);
        label.setStyle("-fx-text-fill: #374151; -fx-font-size: 13px;");

        // X 버튼 - 회색 기본, 호버 시 빨간색
        StackPane closeBtnContainer = new StackPane();
        closeBtnContainer.setStyle("-fx-background-color: transparent; -fx-background-radius: 50%; -fx-padding: 4px; -fx-cursor: hand;");
        Label closeBtn = new Label("✕");
        closeBtn.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px;");
        closeBtnContainer.getChildren().add(closeBtn);
        closeBtnContainer.setOnMouseEntered(e -> closeBtn.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 14px;"));
        closeBtnContainer.setOnMouseExited(e -> closeBtn.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px;"));
        closeBtnContainer.setOnMouseClicked(e -> removeIngredientChip(chip, ingredient));

        chip.getChildren().addAll(label, closeBtnContainer);
        if (chipsContainer != null) {
            chipsContainer.getChildren().add(chip);
        }
    }

    private void removeIngredientChip(Node chip, String ingredient) {
        if (chip == null || ingredient == null) {
            return;
        }
        
        if (chipsContainer != null) {
            chipsContainer.getChildren().remove(chip);
        }
        if (ingredientList != null) {
            // Case-insensitive removal
            String toRemove = null;
            for (String item : ingredientList) {
                if (item != null && item.equalsIgnoreCase(ingredient)) {
                    toRemove = item;
                    break;
                }
            }
            if (toRemove != null) {
                ingredientList.remove(toRemove);
            }
        }
        
        // Popular ingredients 버튼 복귀 (해당 재료가 popular items에 있는 경우)
        if (popularItems != null && popularIngredientButtons != null && popularTags != null) {
            for (String popularItem : popularItems) {
                if (popularItem != null && popularItem.equalsIgnoreCase(ingredient)) {
                    // 이미 버튼이 존재하는지 확인
                    if (!popularIngredientButtons.containsKey(popularItem)) {
                        Button tagBtn = createPopularIngredientButton(popularItem);
                        popularTags.getChildren().add(tagBtn);
                        popularIngredientButtons.put(popularItem, tagBtn);
                    }
                    break;
                }
            }
        }
        
        // Update search button state
        updateSearchButtonStyle();
    }

    private void createResultsPanel() {
        resultsContainer = new StackPane();
        // 배경, 테두리, 패딩 제거하여 공간 최대 활용
        resultsContainer.setStyle("-fx-background-color: transparent;");

        // 1. 검색 결과 개수 표시
        resultsCountLabel = new HBox(8);
        resultsCountLabel.setAlignment(Pos.CENTER_LEFT);
        resultsCountLabel.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 12px; -fx-padding: 12 16;");
        resultsCountLabel.setVisible(false);
        
        // CheckCircle2 아이콘이 없으므로 star-fill 사용 (스펙에서는 CheckCircle2 요구)
        Node checkIcon = SvgIconLoader.loadIcon("/svg/star-fill.svg", 20, Color.web("#84cc16"));
        if (checkIcon == null) {
            checkIcon = new Label("✅");
            ((Label) checkIcon).setStyle("-fx-font-size: 20px;");
        }
        
        Label countText = new Label();
        countText.setStyle("-fx-text-fill: #111827; -fx-font-size: 14px;");
        
        Label countBadge = new Label();
        countBadge.setStyle("-fx-background-color: #84cc16; -fx-text-fill: white; -fx-background-radius: 20px; -fx-padding: 2 10; -fx-font-size: 12px; -fx-font-weight: 600;");
        
        Label recipesText = new Label("recipes");
        recipesText.setStyle("-fx-text-fill: #111827; -fx-font-size: 14px;");
        
        resultsCountLabel.getChildren().addAll(checkIcon, countText, countBadge, recipesText);
        
        // 2. List View - 패딩 제거하여 공간 최대 활용
        listPanel = new FlowPane();
        listPanel.setHgap(24);
        listPanel.setVgap(24);
        listPanel.setPadding(new Insets(0));
        listPanel.setBackground(Background.EMPTY);
        
        VBox listContainer = new VBox(12);
        listContainer.getChildren().addAll(resultsCountLabel, listPanel);

        listScrollPane = new ScrollPane(listContainer);
        listScrollPane.setFitToWidth(true);
        listScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        listScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        // Increase scroll speed
        listScrollPane.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() == 0 || listScrollPane == null) {
                return;
            }
            
            try {
                double delta = event.getDeltaY() * 3.0;
                double height = listScrollPane.getContent().getBoundsInLocal().getHeight();
                double vHeight = listScrollPane.getViewportBounds().getHeight();
                
                double scrollableHeight = height - vHeight;
                if (scrollableHeight > 0) {
                    double vValueShift = -delta / scrollableHeight;
                    double currentValue = listScrollPane.getVvalue();
                    double nextVvalue = currentValue + vValueShift;
                    
                    // 범위 체크 개선
                    if (nextVvalue >= 0.0 && nextVvalue <= 1.0) {
                        listScrollPane.setVvalue(nextVvalue);
                        event.consume();
                    } else if (currentValue > 0.0 && currentValue < 1.0) {
                        // 경계 근처에서도 스크롤 허용
                        listScrollPane.setVvalue(Math.max(0.0, Math.min(1.0, nextVvalue)));
                        event.consume();
                    }
                }
            } catch (Exception e) {
                // 스크롤 처리 중 오류 발생 시 기본 동작 유지
            }
        });

        // 2. Loading View
        loadingPanel = new VBox();
        loadingPanel.setAlignment(Pos.CENTER);
        Label loadingLabel = new Label("Searching recipes...");
        loadingLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: -fx-theme-muted-foreground;");
        loadingPanel.getChildren().add(loadingLabel);

        // 3. Empty View (Initial State) - 개선된 UI
        emptyPanel = new VBox(16);
        emptyPanel.setAlignment(Pos.CENTER);
        emptyPanel.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 0px; -fx-padding: 64px;");
        
        // Icon Circle - 그라데이션 배경
        StackPane iconCircle = new StackPane();
        Circle bg = new Circle(48); // 96px × 96px (48px 반지름)
        Stop[] stops = new Stop[] { new Stop(0, Color.web("#a3e635")), new Stop(1, Color.web("#22c55e")) };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        bg.setFill(gradient);
        bg.setEffect(new javafx.scene.effect.DropShadow(20, Color.rgb(163, 230, 53, 0.2)));
        
        Node chefIcon = SvgIconLoader.loadIcon("/svg/restaurant.svg", 48, Color.WHITE);
        if (chefIcon != null) iconCircle.getChildren().addAll(bg, chefIcon);
        else iconCircle.getChildren().add(bg);

        Label startTitle = new Label("Start by adding your ingredients!");
        startTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: 600; -fx-text-fill: #111827;");
        VBox.setMargin(startTitle, new Insets(0, 0, 8, 0));
        
        Label startSub = new Label("We'll help you find recipes you can make");
        startSub.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280;");
        VBox.setMargin(startSub, new Insets(0, 0, 24, 0));
        
        // 힌트 배지 - Sparkles 아이콘 포함
        HBox hintBadge = new HBox(6);
        hintBadge.setAlignment(Pos.CENTER);
        hintBadge.setStyle("-fx-background-color: #ecfccb; -fx-background-radius: 20px; -fx-padding: 8 16;");
        Node sparklesIcon = SvgIconLoader.loadIcon("/svg/sparkles.svg", 14, Color.web("#4d7c0f"));
        Label hintText = new Label("Click popular ingredients above to add them quickly");
        hintText.setStyle("-fx-text-fill: #4d7c0f; -fx-font-size: 12px; -fx-font-weight: 500;");
        if (sparklesIcon != null) hintBadge.getChildren().add(sparklesIcon);
        hintBadge.getChildren().add(hintText);

        emptyPanel.getChildren().addAll(iconCircle, startTitle, startSub, hintBadge);

        // 4. Error View
        errorPanel = new VBox(16);
        errorPanel.setAlignment(Pos.CENTER);
        
        StackPane errorIconCircle = new StackPane();
        Circle errorBg = new Circle(32);
        errorBg.setFill(Color.web("#fee2e2")); // red-100
        errorBg.setEffect(new javafx.scene.effect.DropShadow(10, Color.rgb(239, 68, 68, 0.4)));
        
        Node errorIcon = SvgIconLoader.loadIcon("/svg/cross-small.svg", 24, Color.web("#dc2626")); // red-600
        if (errorIcon != null) errorIconCircle.getChildren().addAll(errorBg, errorIcon);
        else errorIconCircle.getChildren().add(errorBg);

        Label errorTitle = new Label("검색 중 오류가 발생했습니다");
        errorTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #1f2937;");
        
        Label errorSub = new Label("인터넷 연결을 확인하고 다시 시도해주세요");
        errorSub.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280;");
        
        Button retryButton = new Button("다시 시도");
        retryButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 8px; -fx-padding: 10 20; -fx-cursor: hand;");
        retryButton.setOnAction(e -> performSearch());
        
        errorPanel.getChildren().addAll(errorIconCircle, errorTitle, errorSub, retryButton);

        resultsContainer.getChildren().addAll(emptyPanel, loadingPanel, errorPanel, listScrollPane);
        
        showView("EMPTY");
    }
    
    private void showView(String viewName) {
        if (viewName == null) {
            return;
        }
        
        if (loadingPanel != null) loadingPanel.setVisible(false);
        if (emptyPanel != null) emptyPanel.setVisible(false);
        if (errorPanel != null) errorPanel.setVisible(false);
        if (listScrollPane != null) {
            listScrollPane.setVisible(false);
        }
        
        switch(viewName) {
            case "LOADING": 
                if (loadingPanel != null) loadingPanel.setVisible(true); 
                break;
            case "EMPTY": 
                if (emptyPanel != null) emptyPanel.setVisible(true); 
                break;
            case "ERROR": 
                if (errorPanel != null) errorPanel.setVisible(true); 
                break;
            case "LIST": 
                if (listScrollPane != null) listScrollPane.setVisible(true);
                break;
            default:
                // 알 수 없는 뷰 이름은 무시
                break;
        }
    }

    private void performSearch() {
        if (ingredientsField == null || ingredientList == null || controller == null || viewModel == null) {
            return;
        }
        
        // Add current text if exists
        String currentText = ingredientsField.getText();
        if (StringUtil.hasContent(currentText)) {
            addIngredientChip(currentText);
            ingredientsField.clear();
        }

        if (ingredientList.isEmpty()) {
            if (errorLabel != null) {
                errorLabel.setText("Please add at least one ingredient before searching.");
            }
            showView("EMPTY");
            return;
        }
        
        // Clear previous errors
        if (errorLabel != null) {
            errorLabel.setText("");
        }
        viewModel.setLoading(true);
        viewModel.setErrorMessage("");
        
        // Convert ObservableList to regular List for controller
        List<String> ingredientsList = new ArrayList<>(ingredientList);
        controller.execute(ingredientsList);
    }

    private void displayRecipes(List<Recipe> recipes) {
        if (listPanel == null) {
            return;
        }
        
        // OPTIMIZATION: Store all recipes for client-side filtering
        List<Recipe> apiRecipes = recipes != null ? new ArrayList<>(recipes) : new ArrayList<>();
        
        // Merge with local database recipes
        if (recipeRepository != null) {
            try {
                List<Recipe> localRecipes = recipeRepository.findAll();
                if (localRecipes != null && !localRecipes.isEmpty()) {
                    // Merge recipes, avoiding duplicates
                    for (Recipe localRecipe : localRecipes) {
                        if (localRecipe == null) {
                            continue;
                        }
                        boolean exists = apiRecipes.stream()
                            .filter(r -> r != null)
                            .anyMatch(r -> {
                                String localId = localRecipe.getRecipeId();
                                String localName = localRecipe.getName();
                                return (localId != null && localId.equals(r.getRecipeId())) ||
                                       (localName != null && localName.equals(r.getName()));
                            });
                        if (!exists) {
                            apiRecipes.add(localRecipe);
                        }
                    }
                }
            } catch (DataAccessException e) {
                // Silently fail - local recipes are optional
            } catch (Exception e) {
                // Silently fail - local recipes are optional
            }
        }
        
        allRecipes = apiRecipes;

        listPanel.getChildren().clear();

        List<Recipe> filtered = applyQuickFilters(allRecipes);

        if (filtered == null || filtered.isEmpty()) {
            showView("EMPTY");
            // 결과 없음 상태 UI 업데이트 - 안전한 타입 체크
            if (emptyPanel != null && !emptyPanel.getChildren().isEmpty()) {
                Node firstChild = emptyPanel.getChildren().get(0);
                if (firstChild instanceof StackPane) {
                    StackPane iconCircle = (StackPane) firstChild;
                    iconCircle.getChildren().clear();
                    
                    if (allRecipes.isEmpty()) {
                        // 검색 결과가 전혀 없을 때 - 초기 상태 유지
                        Circle bg = new Circle(48);
                        Stop[] stops = new Stop[] { new Stop(0, Color.web("#a3e635")), new Stop(1, Color.web("#22c55e")) };
                        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
                        bg.setFill(gradient);
                        bg.setEffect(new javafx.scene.effect.DropShadow(20, Color.rgb(163, 230, 53, 0.2)));
                        Node chefIcon = SvgIconLoader.loadIcon("/svg/restaurant.svg", 48, Color.WHITE);
                        if (chefIcon != null) iconCircle.getChildren().addAll(bg, chefIcon);
                        else iconCircle.getChildren().add(bg);
                        
                        if (emptyPanel.getChildren().size() > 1 && emptyPanel.getChildren().get(1) instanceof Label) {
                            ((Label) emptyPanel.getChildren().get(1)).setText("Start by adding your ingredients!");
                        }
                        if (emptyPanel.getChildren().size() > 2 && emptyPanel.getChildren().get(2) instanceof Label) {
                            ((Label) emptyPanel.getChildren().get(2)).setText("We'll help you find recipes you can make");
                        }
                        if (emptyPanel.getChildren().size() > 3 && emptyPanel.getChildren().get(3) instanceof HBox) {
                            ((HBox) emptyPanel.getChildren().get(3)).setVisible(true);
                        }
                    } else {
                        // 필터로 인해 결과가 없을 때 - 검색 아이콘으로 변경
                        Circle bg = new Circle(32);
                        bg.setFill(Color.web("#f3f4f6"));
                        Node searchIcon = SvgIconLoader.loadIcon("/svg/search.svg", 32, Color.web("#9ca3af"));
                        if (searchIcon != null) iconCircle.getChildren().addAll(bg, searchIcon);
                        else iconCircle.getChildren().add(bg);
                        
                        if (emptyPanel.getChildren().size() > 1 && emptyPanel.getChildren().get(1) instanceof Label) {
                            ((Label) emptyPanel.getChildren().get(1)).setText("No recipes found with these ingredients");
                        }
                        if (emptyPanel.getChildren().size() > 2 && emptyPanel.getChildren().get(2) instanceof Label) {
                            ((Label) emptyPanel.getChildren().get(2)).setText("Try adding more ingredients or adjusting filters");
                        }
                        if (emptyPanel.getChildren().size() > 3 && emptyPanel.getChildren().get(3) instanceof HBox) {
                            ((HBox) emptyPanel.getChildren().get(3)).setVisible(false);
                        }
                    }
                }
            }
            
            // 결과 개수 라벨 숨기기
            if (resultsCountLabel != null) {
                resultsCountLabel.setVisible(false);
            }
        } else {
            // 검색 결과 개수 표시
            updateResultsCountLabel(filtered.size());
            
            for (Recipe recipe : filtered) {
                listPanel.getChildren().add(createRecipeCard(recipe));
            }
            showView("LIST");
        }
    }

    private VBox createRecipeCard(Recipe recipe) {
        if (recipe == null) {
            return new VBox(); // 빈 카드 반환
        }
        
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: white; -fx-border-color: #f3f4f6; -fx-border-width: 2px; -fx-border-radius: 16px; -fx-background-radius: 16px;");
        card.setPrefWidth(220);
        card.setMinWidth(220);
        card.setMaxWidth(220);
        card.setPrefHeight(Region.USE_COMPUTED_SIZE);
        card.setMinHeight(Region.USE_COMPUTED_SIZE);
        card.setCursor(Cursor.HAND);
        
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-border-color: #d9f99d; -fx-border-width: 2px; -fx-border-radius: 16px; -fx-background-radius: 16px;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-border-color: #f3f4f6; -fx-border-width: 2px; -fx-border-radius: 16px; -fx-background-radius: 16px;"));

        // 1. Image with Match Badge - 이미지를 카드 절반에 fill-in
        StackPane thumbnail = new StackPane();
        thumbnail.setPrefHeight(208);
        thumbnail.setMinHeight(208);
        thumbnail.setMaxHeight(208);
        thumbnail.setStyle("-fx-background-color: linear-gradient(to bottom, #f3f4f6, #f9fafb); -fx-background-radius: 16px 16px 0 0;");
        
        // 클리핑 마스크로 카드 상단에 맞춤
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(0, 0, 220, 208);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        thumbnail.setClip(clip);
        
        // Load recipe image if available - fill-in으로 설정
        String imageUrl = recipe.getImageUrl();
        if (imageUrl != null && !imageUrl.trim().isEmpty() && imageCache != null) {
            try {
                ImageView imageView = new ImageView();
                imageView.setFitWidth(220);
                imageView.setFitHeight(208);
                imageView.setPreserveRatio(false); // fill-in을 위해 비율 유지 안 함
                imageView.setSmooth(true);
                imageView.setCache(true);
                
                Image image = imageCache.getImage(imageUrl);
                if (image != null) {
                    imageView.setImage(image);
                    thumbnail.getChildren().add(0, imageView); // 배경으로 추가
                }
            } catch (Exception e) {
                // If image loading fails, fall back to placeholder background
                // 이미지 로딩 실패는 조용히 처리 (플레이스홀더 배경 사용)
            }
        }
        
        // 매칭 배지 계산
        List<String> recipeIngredients = recipe.getIngredients() != null ? recipe.getIngredients() : new ArrayList<>();
        List<String> matchedIngredients = new ArrayList<>();
        List<String> missingIngredients = new ArrayList<>();
        
        if (ingredientList != null && !ingredientList.isEmpty()) {
            for (String rIng : recipeIngredients) {
                if (rIng == null || rIng.trim().isEmpty()) {
                    continue;
                }
                String rIngLower = rIng.toLowerCase();
                boolean found = ingredientList.stream()
                    .filter(uIng -> uIng != null && !uIng.trim().isEmpty())
                    .anyMatch(uIng -> {
                        String uIngLower = uIng.toLowerCase();
                        return rIngLower.contains(uIngLower) || uIngLower.contains(rIngLower);
                    });
                if (found) {
                    matchedIngredients.add(rIng);
                } else {
                    missingIngredients.add(rIng);
                }
            }
        } else {
            // 재료가 없으면 모든 재료가 missing
            missingIngredients.addAll(recipeIngredients);
        }
        
        int missing = missingIngredients.size();
        
        // 매칭 배지 생성
        HBox matchBadge = new HBox(4);
        matchBadge.setAlignment(Pos.CENTER);
        matchBadge.setStyle("-fx-background-radius: 8px; -fx-padding: 4 8;");
        StackPane.setAlignment(matchBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(matchBadge, new Insets(8, 8, 0, 0));
        
        Node badgeIcon;
        String badgeText;
        if (missing == 0) {
            // Perfect Match
            matchBadge.setStyle("-fx-background-color: linear-gradient(to right, #84cc16, #22c55e); -fx-background-radius: 8px; -fx-padding: 4 8;");
            badgeIcon = SvgIconLoader.loadIcon("/svg/star-fill.svg", 10, Color.WHITE);
            badgeText = "Perfect";
        } else if (missing == 1) {
            // Almost Perfect - AlertCircle 아이콘이 없으므로 circle-ellipsis 사용 (스펙에서는 AlertCircle 요구)
            matchBadge.setStyle("-fx-background-color: linear-gradient(to right, #fbbf24, #fb923c); -fx-background-radius: 8px; -fx-padding: 4 8;");
            badgeIcon = SvgIconLoader.loadIcon("/svg/circle-ellipsis.svg", 10, Color.WHITE);
            badgeText = "Almost";
        } else {
            // Need X more - AlertCircle 아이콘이 없으므로 circle-ellipsis 사용 (스펙에서는 AlertCircle 요구)
            matchBadge.setStyle("-fx-background-color: linear-gradient(to right, #fb7185, #ef4444); -fx-background-radius: 8px; -fx-padding: 4 8;");
            badgeIcon = SvgIconLoader.loadIcon("/svg/circle-ellipsis.svg", 10, Color.WHITE);
            badgeText = "Need " + missing + " more";
        }
        
        if (badgeIcon != null) matchBadge.getChildren().add(badgeIcon);
        Label badgeLabel = new Label(badgeText);
        badgeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: 500;");
        matchBadge.getChildren().add(badgeLabel);
        
        thumbnail.getChildren().add(matchBadge);
        card.getChildren().add(thumbnail);

        // 2. Content Area with padding - 최소 높이 설정하여 버튼 위치 고정
        VBox contentArea = new VBox(12);
        contentArea.setPadding(new Insets(12));
        contentArea.setStyle("-fx-background-color: white;");
        contentArea.setMinHeight(300); // 최소 높이 설정으로 버튼이 항상 하단에 위치
        
        // Title
        Label title = new Label(recipe.getName());
        title.setStyle("-fx-font-weight: 600; -fx-font-size: 18px; -fx-text-fill: #111827;");
        title.setWrapText(true);
        title.setMaxHeight(54); // 3줄 제한
        contentArea.getChildren().add(title);

        // 3. Meta Info Badges
        HBox metaBox = new HBox(8);
        metaBox.setAlignment(Pos.CENTER_LEFT);
        
        // 칼로리 배지
        HBox calBadge = new HBox(6);
        calBadge.setAlignment(Pos.CENTER);
        calBadge.setStyle("-fx-background-color: #fff7ed; -fx-background-radius: 8px; -fx-padding: 6 10;");
        Node flameIcon = SvgIconLoader.loadIcon("/svg/fire-flame.svg", 14, Color.web("#ea580c"));
        if (flameIcon == null) {
            flameIcon = new Label("🔥");
            ((Label) flameIcon).setStyle("-fx-font-size: 14px;");
        }
        Label calValue = new Label(String.valueOf(recipe.getNutritionInfo() != null ? recipe.getNutritionInfo().getCalories() : "N/A"));
        calValue.setStyle("-fx-text-fill: #ea580c; -fx-font-size: 12px;");
        calBadge.getChildren().addAll(flameIcon, calValue);
        
        // 조리 시간 배지
        HBox timeBadge = new HBox(6);
        timeBadge.setAlignment(Pos.CENTER);
        timeBadge.setStyle("-fx-background-color: #eff6ff; -fx-background-radius: 8px; -fx-padding: 6 10;");
        Node clockIcon = SvgIconLoader.loadIcon("/svg/clock.svg", 14, Color.web("#2563eb"));
        if (clockIcon == null) {
            clockIcon = new Label("⏰");
            ((Label) clockIcon).setStyle("-fx-font-size: 14px;");
        }
        Integer cookTime = recipe.getCookTimeMinutes();
        String timeText = cookTime != null ? cookTime + " min" : "N/A";
        Label timeValue = new Label(timeText);
        timeValue.setStyle("-fx-text-fill: #2563eb; -fx-font-size: 12px;");
        timeBadge.getChildren().addAll(clockIcon, timeValue);
        
        // 인분 배지
        HBox servingBadge = new HBox(6);
        servingBadge.setAlignment(Pos.CENTER);
        servingBadge.setStyle("-fx-background-color: #faf5ff; -fx-background-radius: 8px; -fx-padding: 6 10;");
        Node usersIcon = SvgIconLoader.loadIcon("/svg/users.svg", 14, Color.web("#9333ea"));
        if (usersIcon == null) {
            usersIcon = new Label("👥");
            ((Label) usersIcon).setStyle("-fx-font-size: 14px;");
        }
        Label servingValue = new Label(String.valueOf(recipe.getServingSize()));
        servingValue.setStyle("-fx-text-fill: #9333ea; -fx-font-size: 12px;");
        servingBadge.getChildren().addAll(usersIcon, servingValue);
        
        metaBox.getChildren().addAll(calBadge, timeBadge, servingBadge);
        contentArea.getChildren().add(metaBox);
        
        // 4. 재료 매칭 정보
        VBox ingredientsInfo = new VBox(8);
        
        // You have 섹션
        if (!matchedIngredients.isEmpty()) {
            HBox youHaveBox = new HBox(6);
            youHaveBox.setAlignment(Pos.CENTER_LEFT);
            // CheckCircle2 아이콘이 없으므로 star-fill 사용 (스펙에서는 CheckCircle2 요구)
            Node checkIcon = SvgIconLoader.loadIcon("/svg/star-fill.svg", 16, Color.web("#84cc16"));
            if (checkIcon == null) {
                checkIcon = new Label("✅");
                ((Label) checkIcon).setStyle("-fx-font-size: 16px;");
            }
            Label youHaveLabel = new Label("You have:");
            youHaveLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
            youHaveBox.getChildren().addAll(checkIcon, youHaveLabel);
            
            FlowPane youHaveChips = new FlowPane();
            youHaveChips.setHgap(4);
            youHaveChips.setVgap(4);
            for (String ing : matchedIngredients) {
                if (ing == null || ing.trim().isEmpty()) {
                    continue;
                }
                Label chip = new Label(ing);
                chip.setStyle("-fx-background-color: #ecfccb; -fx-text-fill: #4d7c0f; -fx-background-radius: 6px; -fx-padding: 4 8; -fx-font-size: 12px;");
                youHaveChips.getChildren().add(chip);
            }
            
            ingredientsInfo.getChildren().addAll(youHaveBox, youHaveChips);
        }
        
        
        if (!ingredientsInfo.getChildren().isEmpty()) {
            contentArea.getChildren().add(ingredientsInfo);
        }
        
        // Spacer to push View Details button to bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        contentArea.getChildren().add(spacer);
        
        // 5. View Details 버튼 - 하단 고정
        Button viewDetailsBtn = new Button("View Details");
        viewDetailsBtn.setMaxWidth(Double.MAX_VALUE);
        viewDetailsBtn.setPrefHeight(48);
        viewDetailsBtn.setMinHeight(48);
        viewDetailsBtn.setStyle("-fx-background-color: #111827; -fx-text-fill: white; -fx-background-radius: 12px; -fx-font-weight: 500; -fx-font-size: 14px; -fx-cursor: hand;");
        viewDetailsBtn.setOnMouseEntered(e -> viewDetailsBtn.setStyle("-fx-background-color: #1f2937; -fx-text-fill: white; -fx-background-radius: 12px; -fx-font-weight: 500; -fx-font-size: 14px; -fx-cursor: hand;"));
        viewDetailsBtn.setOnMouseExited(e -> viewDetailsBtn.setStyle("-fx-background-color: #111827; -fx-text-fill: white; -fx-background-radius: 12px; -fx-font-weight: 500; -fx-font-size: 14px; -fx-cursor: hand;"));
        viewDetailsBtn.setOnAction(e -> openRecipeDetail(recipe));
        
        contentArea.getChildren().add(viewDetailsBtn);
        
        // Add content area to card
        card.getChildren().add(contentArea);

        return card;
    }

    private List<Recipe> applyQuickFilters(List<Recipe> recipes) {
        if (recipes == null || recipes.isEmpty()) {
            return new ArrayList<>();
        }
        if (activeFilters == null || activeFilters.isEmpty()) {
            return new ArrayList<>(recipes);
        }
        return recipes.stream()
                .filter(r -> r != null)
                .filter(this::matchesActiveFilters)
                .collect(Collectors.toList());
    }

    private boolean matchesActiveFilters(Recipe recipe) {
        if (recipe == null) {
            return false;
        }
        if (activeFilters == null || activeFilters.isEmpty()) {
            return true;
        }
        // OR condition: recipe matches if it satisfies ANY of the selected filters
        for (String filter : activeFilters) {
            if (filter != null && matchesFilter(recipe, filter)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesFilter(Recipe recipe, String filter) {
        if (recipe == null || !StringUtil.hasContent(filter)) {
            return false;
        }
        String name = recipe.getName() != null ? recipe.getName().toLowerCase() : "";
        List<String> ingredients = recipe.getIngredients() != null ? recipe.getIngredients() : List.of();
        switch (filter) {
            case "Breakfast":
                return name.contains("breakfast") || name.contains("toast") || name.contains("pancake");
            case "Lunch":
                return name.contains("salad") || name.contains("sandwich") || name.contains("lunch");
            case "Dinner":
                return name.contains("dinner") || name.contains("steak") || name.contains("pasta");
            case "Vegetarian":
                // Vegetarian과 Vegan 필터링 로직 통일 - 동물성 제품 모두 제외
                return ingredients.stream().noneMatch(this::containsAnimalProductKeyword);
            case "Vegan":
                return ingredients.stream().noneMatch(this::containsAnimalProductKeyword);
            case "Quick (< 30min)":
                return name.contains("quick") || name.contains("15") || name.contains("20");
            default:
                return true;
        }
    }

    private boolean containsMeatKeyword(String ingredient) {
        if (ingredient == null || ingredient.trim().isEmpty()) {
            return false;
        }
        String lower = ingredient.toLowerCase().trim();
        return lower.contains("chicken") || lower.contains("beef") || lower.contains("pork") ||
               lower.contains("bacon") || lower.contains("fish") || lower.contains("shrimp");
    }

    private boolean containsAnimalProductKeyword(String ingredient) {
        if (containsMeatKeyword(ingredient)) {
            return true;
        }
        if (ingredient == null || ingredient.trim().isEmpty()) {
            return false;
        }
        String lower = ingredient.toLowerCase().trim();
        return lower.contains("egg") || lower.contains("cheese") || lower.contains("milk") || lower.contains("butter");
    }

    private void openRecipeDetail(Recipe recipe) {
        if (recipe == null || recipeDetailViewModel == null || viewManagerModel == null) {
            return;
        }
        try {
            recipeDetailViewModel.setRecipe(recipe);
            viewManagerModel.setActiveView(ViewManager.RECIPE_DETAIL_VIEW);
        } catch (Exception e) {
            // 레시피 상세 화면 열기 실패 시 조용히 처리
            // 필요시 사용자에게 오류 메시지 표시 가능
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null || viewModel == null) {
            return;
        }
        
        Platform.runLater(() -> {
            String propertyName = evt.getPropertyName();
            if (propertyName == null) {
                return;
            }
            
            if (RecipeSearchViewModel.PROP_LOADING.equals(propertyName)) {
                if (viewModel.isLoading()) {
                    showView("LOADING");
                    if (searchButton != null) {
                        searchButton.setDisable(true);
                    }
                } else {
                    if (searchButton != null) {
                        searchButton.setDisable(false);
                    }
                    updateSearchButtonStyle();
                }
            } else if (RecipeSearchViewModel.PROP_RECIPES.equals(propertyName)) {
                // Clear error when recipes are successfully loaded
                if (errorLabel != null) {
                    errorLabel.setText("");
                }
                displayRecipes(viewModel.getRecipes());
            } else if (RecipeSearchViewModel.PROP_ERROR_MESSAGE.equals(propertyName)) {
                String errorMsg = viewModel.getErrorMessage();
                if (StringUtil.hasContent(errorMsg)) {
                    if (errorLabel != null) {
                        errorLabel.setText(errorMsg);
                    }
                    showView("ERROR");
                    // Check if it's a network error
                    String lowerMsg = errorMsg.toLowerCase();
                    if (lowerMsg.contains("network") || lowerMsg.contains("connection") || 
                        lowerMsg.contains("timeout") || lowerMsg.contains("인터넷")) {
                        // Update error panel message for network errors
                        if (errorPanel != null && errorPanel.getChildren().size() > 2) {
                            Node errorSubNode = errorPanel.getChildren().get(2);
                            if (errorSubNode instanceof Label) {
                                ((Label) errorSubNode).setText("인터넷 연결을 확인하고 다시 시도해주세요");
                            }
                        }
                    }
                } else {
                    if (errorLabel != null) {
                        errorLabel.setText("");
                    }
                    // If error is cleared, show empty state if no recipes
                    List<Recipe> recipes = viewModel.getRecipes();
                    if (recipes == null || recipes.isEmpty()) {
                        showView("EMPTY");
                    }
                }
            }
        });
    }
    
    private void updateSearchButtonStyle() {
        if (searchButton == null) {
            return;
        }
        
        boolean hasIngredients = ingredientList != null && !ingredientList.isEmpty();
        boolean isLoading = viewModel != null && viewModel.isLoading();
        
        if (isLoading) {
            // 검색 중 상태
            searchButton.setText("Searching...");
            searchButton.setDisable(true);
            searchButton.setStyle("-fx-text-fill: white; -fx-background-radius: 8px; -fx-font-weight: 600; -fx-padding: 10 24; -fx-font-size: 14px; -fx-background-color: #84cc16; -fx-opacity: 0.7;");
            searchButton.setGraphic(null);
        } else if (!hasIngredients) {
            // 재료 없을 때 비활성화
            searchButton.setText("Search");
            searchButton.setDisable(true);
            searchButton.setStyle("-fx-text-fill: white; -fx-background-radius: 8px; -fx-font-weight: 600; -fx-padding: 10 24; -fx-font-size: 14px; -fx-background-color: #68CA2A; -fx-opacity: 0.5;");
            searchButton.setGraphic(null);
        } else {
            // 정상 상태 - 그라데이션 효과를 위해 중간 색상 사용
            searchButton.setText("Search");
            searchButton.setDisable(false);
            searchButton.setStyle("-fx-text-fill: white; -fx-background-radius: 8px; -fx-font-weight: 600; -fx-padding: 10 24; -fx-font-size: 14px; -fx-background-color: #84cc16;");
            Node btnIcon = SvgIconLoader.loadIcon("/svg/search.svg", 16, Color.WHITE);
            if (btnIcon != null) {
                searchButton.setGraphic(btnIcon);
                searchButton.setGraphicTextGap(8);
            }
        }
    }
    
    
    private void updateResultsCountLabel(int count) {
        if (resultsCountLabel == null || resultsCountLabel.getChildren().size() < 3) return;
        
        resultsCountLabel.setVisible(true);
        Node countTextNode = resultsCountLabel.getChildren().get(1);
        Node countBadgeNode = resultsCountLabel.getChildren().get(2);
        
        if (countTextNode instanceof Label) {
            ((Label) countTextNode).setText("Found");
        }
        if (countBadgeNode instanceof Label) {
            ((Label) countBadgeNode).setText(String.valueOf(count));
        }
    }
    
    /**
     * Clean up resources and remove property change listeners to prevent memory leaks.
     * Should be called when this view is no longer needed.
     */
    public void dispose() {
        if (viewModel != null) {
            viewModel.removePropertyChangeListener(this);
        }
    }
}
