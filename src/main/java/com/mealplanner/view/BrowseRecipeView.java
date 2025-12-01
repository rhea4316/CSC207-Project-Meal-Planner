package com.mealplanner.view;

import com.mealplanner.app.SessionManager;
import com.mealplanner.entity.Recipe;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.BrowseRecipeController;
import com.mealplanner.interface_adapter.controller.GetRecommendationsController;
import com.mealplanner.interface_adapter.controller.StoreRecipeController;
import com.mealplanner.interface_adapter.view_model.RecipeBrowseViewModel;
import com.mealplanner.interface_adapter.view_model.RecipeDetailViewModel;
import com.mealplanner.repository.RecipeRepository;
import com.mealplanner.exception.DataAccessException;
import com.mealplanner.util.StringUtil;
import com.mealplanner.util.ImageCacheManager;
import com.mealplanner.view.component.Sonner;
import com.mealplanner.view.util.SvgIconLoader;
import javafx.animation.PauseTransition;
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
import javafx.util.Duration;
import javafx.concurrent.Task;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrowseRecipeView extends BorderPane implements PropertyChangeListener {
    private final RecipeBrowseViewModel viewModel;
    private final BrowseRecipeController controller;
    private final ViewManagerModel viewManagerModel;
    private final RecipeDetailViewModel recipeDetailViewModel;
    private final RecipeRepository recipeRepository;
    private final ImageCacheManager imageCache = ImageCacheManager.getInstance();
    private GetRecommendationsController recommendationsController;
    private final StoreRecipeController storeRecipeController;
    private static final Logger logger = LoggerFactory.getLogger(BrowseRecipeView.class);
    
    /**
     * Clean up resources and remove property change listeners to prevent memory leaks.
     * Should be called when this view is no longer needed.
     */
    public void dispose() {
        if (viewModel != null) {
            viewModel.removePropertyChangeListener(this);
        }
    }

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
    private VBox errorPanel;
    private Label errorLabel;
    private Label countLabel; // "Showing 9 of 16 recipes"
    
    // Recommendations Section
    private VBox recommendationsSection;
    
    private Sonner sonner;
    
    // Phase 4: Saved count button reference
    private Button savedCountButton;

    /**
     * Constructor with GetRecommendationsController and StoreRecipeController (Phase 5 + Phase 1).
     */
    public BrowseRecipeView(RecipeBrowseViewModel viewModel, BrowseRecipeController controller, ViewManagerModel viewManagerModel, RecipeDetailViewModel recipeDetailViewModel, RecipeRepository recipeRepository, GetRecommendationsController recommendationsController, StoreRecipeController storeRecipeController) {
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
        this.recommendationsController = recommendationsController;
        this.storeRecipeController = storeRecipeController;
        
        initializeView();
    }

    /**
     * Constructor without GetRecommendationsController (current version).
     * @deprecated Use constructor with StoreRecipeController for bookmark functionality
     */
    @Deprecated
    public BrowseRecipeView(RecipeBrowseViewModel viewModel, BrowseRecipeController controller, ViewManagerModel viewManagerModel, RecipeDetailViewModel recipeDetailViewModel, RecipeRepository recipeRepository) {
        if (viewModel == null) throw new IllegalArgumentException("ViewModel cannot be null");
        if (controller == null) throw new IllegalArgumentException("Controller cannot be null");
        if (viewManagerModel == null) throw new IllegalArgumentException("ViewManagerModel cannot be null");
        if (recipeDetailViewModel == null) throw new IllegalArgumentException("RecipeDetailViewModel cannot be null");
        if (recipeRepository == null) throw new IllegalArgumentException("RecipeRepository cannot be null");
        
        this.storeRecipeController = null; // 북마크 기능 없음

        this.viewModel = viewModel;
        this.controller = controller;
        this.viewManagerModel = viewManagerModel;
        this.recipeDetailViewModel = recipeDetailViewModel;
        this.recipeRepository = recipeRepository;
        this.recommendationsController = null;  // Phase 5 feature, not yet implemented

        initializeView();
    }

    /**
     * Common initialization logic for both constructors.
     */
    private void initializeView() {
        
        viewModel.addPropertyChangeListener(this);

        // Initialize Sonner
        sonner = new Sonner();

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

        // Saved Button (Top Right) - Phase 4: Dynamic count
        savedCountButton = new Button("0 Saved");
        savedCountButton.setId("saved-count-btn"); // For easy access
        savedCountButton.setStyle("-fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 8px; -fx-padding: 8 16; -fx-cursor: hand; -fx-background-color: #68CA2A;");
        Node bookmarkIcon = SvgIconLoader.loadIcon("/svg/bookmark.svg", 14, Color.WHITE);
        if (bookmarkIcon != null) {
            savedCountButton.setGraphic(bookmarkIcon);
            savedCountButton.setGraphicTextGap(8);
        }
        // Phase 4: Update saved count on initialization (will be called after UI is ready)
        
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        topBar.getChildren().addAll(headerBox, spacer, savedCountButton);
        
        // Recommendations Section (only if controller is available)
        if (recommendationsController != null) {
            recommendationsSection = createRecommendedSection();
            VBox topSection = new VBox(24);
            topSection.getChildren().addAll(topBar, createSearchPanel(), recommendationsSection);
            setTop(topSection);
        } else {
            VBox topSection = new VBox(24);
            topSection.getChildren().addAll(topBar, createSearchPanel());
            setTop(topSection);
        }

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
        
        // Load recommendations on initialization
        loadRecommendations();
        
        // Load local database recipes on initialization
        loadLocalRecipes();
        
        // Phase 4: Update saved count after UI is ready
        Platform.runLater(() -> updateSavedCount());
    }
    
    /**
     * Loads recipes from local database and displays them initially.
     * This ensures local recipes are shown even without a search query.
     */
    private void loadLocalRecipes() {
        if (recipeRepository == null) {
            logger.warn("recipeRepository is null, cannot load local recipes");
            // If no repository, ensure empty state is shown
            Platform.runLater(() -> {
                if (listPanel.getChildren().isEmpty() || 
                    listPanel.getChildren().contains(loadingPanel)) {
                    listPanel.getChildren().clear();
                    listPanel.getChildren().add(emptyPanel);
                }
            });
            return;
        }
        
        logger.debug("Loading local recipes from repository");
        new Thread(() -> {
            try {
                List<Recipe> localRecipes = recipeRepository.findAll();
                logger.debug("Loaded {} recipes from local repository", localRecipes != null ? localRecipes.size() : 0);
                Platform.runLater(() -> {
                    if (localRecipes != null && !localRecipes.isEmpty()) {
                        logger.debug("Setting {} recipes to allRecipes and applying filter", localRecipes.size());
                        // Set local recipes as initial display (thread-safe)
                        synchronized (this) {
                            allRecipes = new ArrayList<>(localRecipes);
                        }
                        applyClientSideFilter();
                        // Phase 4: Update saved count and bookmark states after loading
                        updateSavedCount();
                        refreshBookmarkStates();
                    } else {
                        logger.debug("No local recipes found, showing empty state");
                        // If no local recipes, show empty state
                        // Ensure loading panel is removed if present
                        listPanel.getChildren().clear();
                        listPanel.getChildren().add(emptyPanel);
                        if (countLabel != null) {
                            countLabel.setText("Showing 0 recipes");
                        }
                    }
                });
            } catch (DataAccessException e) {
                logger.error("DataAccessException while loading local recipes: {}", e.getMessage(), e);
                // Local recipes are optional, but ensure loading state is cleared
                Platform.runLater(() -> {
                    if (listPanel.getChildren().contains(loadingPanel)) {
                        listPanel.getChildren().clear();
                        listPanel.getChildren().add(emptyPanel);
                    }
                    // If already showing empty panel, keep it
                    if (listPanel.getChildren().isEmpty()) {
                        listPanel.getChildren().add(emptyPanel);
                    }
                });
            } catch (Exception e) {
                logger.error("Exception while loading local recipes: {}", e.getMessage(), e);
                // Local recipes are optional, but ensure loading state is cleared
                Platform.runLater(() -> {
                    if (listPanel.getChildren().contains(loadingPanel)) {
                        listPanel.getChildren().clear();
                        listPanel.getChildren().add(emptyPanel);
                    }
                    // If already showing empty panel, keep it
                    if (listPanel.getChildren().isEmpty()) {
                        listPanel.getChildren().add(emptyPanel);
                    }
                });
            }
        }).start();
    }

    private void loadRecommendations() {
        if (recommendationsController == null) {
            return;  // Phase 5 not yet implemented
        }
        com.mealplanner.entity.User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUserId();
            recommendationsController.execute(userId);
        }
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
        Node filterIcon = SvgIconLoader.loadIcon("/svg/filter.svg", 14, Color.web("#6b7280"));
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
        addCategoryFilter("Snacks", false, "/svg/cookie.svg");
        addCategoryFilter("Desserts", false, "/svg/cake-slice.svg");
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
        // Selected Style (Gradient) - will be applied via Background
        String selectedStyle = "-fx-text-fill: white; -fx-border-color: transparent; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8 16; -fx-font-size: 13px; -fx-font-weight: 600; -fx-cursor: hand;";
        
        if (isSelected) {
            btn.setStyle(selectedStyle + " -fx-background-color: #68CA2A;");
        } else {
            btn.setStyle(defaultStyle);
        }
        
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
                btn.setStyle(selectedStyle + " -fx-background-color: #68CA2A;");
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
                btn.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(8), Insets.EMPTY)));
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
        
        // 4. Error View
        errorPanel = new VBox(16);
        errorPanel.setAlignment(Pos.CENTER);
        
        Label errorIconLabel = new Label("⚠️");
        errorIconLabel.setStyle("-fx-font-size: 48px;");
        
        Label errorTitle = new Label("레시피를 불러오는 중 오류가 발생했습니다");
        errorTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");
        
        Label errorSub = new Label("인터넷 연결을 확인하고 다시 시도해주세요");
        errorSub.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280;");
        
        Button retryButton = new Button("다시 시도");
        retryButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 8px; -fx-padding: 10 20; -fx-cursor: hand;");
        retryButton.setOnAction(e -> performSearch());
        
        errorPanel.getChildren().addAll(errorIconLabel, errorTitle, errorSub, retryButton);
        
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
        errorLabel.setText("");

        // Run search in background using JavaFX Task
        final String finalQuery = effectiveQuery;
        Task<Void> searchTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    logger.debug("Starting search for query: {}", finalQuery);
                    // Default to 12 results
                    controller.execute(finalQuery, 12);
                    logger.debug("Search completed for query: {}", finalQuery);
                    return null;
                } catch (Exception e) {
                    logger.error("Exception in search task: {}", e.getMessage(), e);
                    throw e;
                }
            }
        };
        
        // Handle task failure
        searchTask.setOnFailed(e -> {
            Throwable ex = searchTask.getException();
            if (ex != null) {
                logger.error("Search task failed: {}", ex.getMessage(), ex);
                String errorMessage;
                if (ex instanceof IOException) {
                    errorMessage = "Network error: " + ex.getMessage();
                    sonner.show("Network Error", "Failed to search recipes. Please check your connection and try again.", Sonner.Type.ERROR);
                } else {
                    errorMessage = "An error occurred: " + ex.getMessage();
                    sonner.show("Error", "An unexpected error occurred while searching. Please try again.", Sonner.Type.ERROR);
                }
                // ViewModel에 에러 설정하여 PropertyChangeListener가 트리거되도록 함
                Platform.runLater(() -> viewModel.setErrorMessage(errorMessage));
            }
        });
        
        // Start the task in a background thread
        new Thread(searchTask).start();
    }

    private void displayRecipes(List<Recipe> recipes) {
        Platform.runLater(() -> {
            logger.debug("displayRecipes called with {} recipes", recipes != null ? recipes.size() : 0);
            
            // OPTIMIZATION: Store all recipes for client-side filtering
            List<Recipe> apiRecipes = recipes != null ? new ArrayList<>(recipes) : new ArrayList<>();
            
            logger.debug("Initial API recipes count: {}", apiRecipes.size());
            
            // Merge with local database recipes
            if (recipeRepository != null) {
                try {
                    List<Recipe> localRecipes = recipeRepository.findAll();
                    logger.debug("Local recipes count: {}", localRecipes != null ? localRecipes.size() : 0);
                    if (localRecipes != null && !localRecipes.isEmpty()) {
                        // Merge recipes, avoiding duplicates
                        for (Recipe localRecipe : localRecipes) {
                            boolean exists = apiRecipes.stream()
                                .anyMatch(r -> r.getRecipeId() != null && r.getRecipeId().equals(localRecipe.getRecipeId()) ||
                                              (r.getName() != null && r.getName().equals(localRecipe.getName())));
                            if (!exists) {
                                apiRecipes.add(localRecipe);
                            }
                        }
                    }
                } catch (DataAccessException e) {
                    logger.warn("Failed to load local recipes: {}", e.getMessage());
                    // Silently fail - local recipes are optional
                } catch (Exception e) {
                    logger.warn("Exception while loading local recipes: {}", e.getMessage());
                    // Silently fail - local recipes are optional
                }
            }
            
            logger.debug("Total recipes after merge: {}", apiRecipes.size());
            
            // Thread-safe write
            synchronized (this) {
                allRecipes = new ArrayList<>(apiRecipes);
            }

            // Apply current filter
            applyClientSideFilter();
        });
    }

    /**
     * OPTIMIZATION: Apply client-side category filtering without re-fetching from API.
     * This method should be called from JavaFX Application Thread.
     */
    private void applyClientSideFilter() {
        if (listPanel == null) {
            logger.warn("listPanel is null, cannot apply filter");
            return;
        }
        
        // Ensure we're on JavaFX Application Thread
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::applyClientSideFilter);
            return;
        }
        
        listPanel.getChildren().clear();

        // Thread-safe read
        List<Recipe> recipesToFilter;
        synchronized (this) {
            recipesToFilter = new ArrayList<>(allRecipes);
        }

        logger.debug("Applying filter: selectedCategory={}, totalRecipes={}", selectedCategory, recipesToFilter.size());

        if (recipesToFilter == null || recipesToFilter.isEmpty()) {
            logger.debug("No recipes to filter, showing empty state");
            // Recreate empty panel with proper message
            VBox emptyMsg = new VBox(15);
            emptyMsg.setAlignment(Pos.CENTER);
            Label iconLabel = new Label("🍳");
            iconLabel.setStyle("-fx-font-size: 48px;");
            Label emptyLabel = new Label("No recipes found. Try a different term.");
            emptyLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -fx-theme-muted-foreground;");
            emptyMsg.getChildren().addAll(iconLabel, emptyLabel);
            listPanel.getChildren().add(emptyMsg);
            if (countLabel != null) {
                countLabel.setText("Showing 0 recipes");
            }
            return;
        }

        // Filter recipes based on selected category - use recipesToFilter instead of allRecipes
        List<Recipe> filteredRecipes = recipesToFilter.stream()
            .filter(recipe -> matchesCategory(recipe, selectedCategory))
            .collect(java.util.stream.Collectors.toList());

        logger.debug("Filtered recipes: {} out of {}", filteredRecipes.size(), recipesToFilter.size());

        if (filteredRecipes.isEmpty()) {
            logger.debug("No recipes match category filter, showing empty state");
            // Recreate empty panel with proper message
            VBox emptyMsg = new VBox(15);
            emptyMsg.setAlignment(Pos.CENTER);
            Label iconLabel = new Label("🍳");
            iconLabel.setStyle("-fx-font-size: 48px;");
            Label emptyLabel = new Label("No recipes found in this category.");
            emptyLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -fx-theme-muted-foreground;");
            emptyMsg.getChildren().addAll(iconLabel, emptyLabel);
            listPanel.getChildren().add(emptyMsg);
            if (countLabel != null) {
                countLabel.setText("Showing 0 recipes");
            }
        } else {
            if (countLabel != null) {
                countLabel.setText("Showing " + filteredRecipes.size() + " of " + recipesToFilter.size() + " recipes");

                // Mockup shows specific "Showing 9 of 16 recipes" style
                Node trendIcon = SvgIconLoader.loadIcon("/svg/chart.svg", 16, Color.web("#84cc16")); // Green zigzag
                if (trendIcon != null) {
                    countLabel.setGraphic(trendIcon);
                    countLabel.setGraphicTextGap(8);
                }
            }

            logger.debug("Displaying {} recipe cards", filteredRecipes.size());
            for (Recipe recipe : filteredRecipes) {
                Node card = createRecipeCard(recipe);
                if (card != null) {
                    listPanel.getChildren().add(card);
                } else {
                    logger.warn("createRecipeCard returned null for recipe: {}", recipe != null ? recipe.getName() : "null");
                }
            }
            
            // Phase 4: Update bookmark states after displaying recipes
            refreshBookmarkStates();
        }
    }

    /**
     * Check if a recipe matches the given category filter.
     */
    private boolean matchesCategory(Recipe recipe, String category) {
        if (recipe == null) {
            return false;
        }
        
        if (category == null || category.equals("All")) {
            return true;
        }

        String recipeName = recipe.getName() != null ? recipe.getName().toLowerCase() : "";

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
                // Vegetarian과 Vegan 필터링 로직 통일 - 동물성 제품 모두 제외
                return !recipeName.contains("meat") && !recipeName.contains("chicken")
                    && !recipeName.contains("beef") && !recipeName.contains("pork")
                    && !recipeName.contains("fish") && !recipeName.contains("egg")
                    && !recipeName.contains("cheese") && !recipeName.contains("milk");
            case "Vegan":
                return !recipeName.contains("meat") && !recipeName.contains("chicken")
                    && !recipeName.contains("beef") && !recipeName.contains("egg")
                    && !recipeName.contains("cheese") && !recipeName.contains("milk");
            default:
                return true;
        }
    }
    
    private VBox createRecipeCard(Recipe recipe) {
        if (recipe == null) {
            logger.warn("Cannot create recipe card for null recipe");
            return new VBox(); // Return empty card to avoid NPE
        }
        
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
                
                Image image = imageCache.getImage(imageUrl);
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
        
        // Save Button Overlay (Bookmark) - Phase 4: Dynamic state
        Button saveBtn = new Button();
        saveBtn.setStyle("-fx-background-color: white; -fx-background-radius: 50%; -fx-min-width: 32px; -fx-min-height: 32px; -fx-max-width: 32px; -fx-max-height: 32px; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        saveBtn.setUserData(recipe); // Store recipe reference for easy access
        
        // Phase 4: Check bookmark status and update icon
        updateBookmarkButtonIcon(saveBtn, recipe);
        
        StackPane.setAlignment(saveBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(saveBtn, new Insets(12));
        
        // 북마크 기능 추가
        saveBtn.setOnAction(e -> {
            bookmarkRecipe(recipe);
            e.consume();
            // Phase 4: Update icon after bookmarking
            updateBookmarkButtonIcon(saveBtn, recipe);
            updateSavedCount();
        });

        // Add overlays (they will be on top of the image)
        imageContainer.getChildren().add(saveBtn);
        if (difficultyTag != null) {
            imageContainer.getChildren().add(difficultyTag);
        }
        card.getChildren().add(imageContainer);
        
        // Image hover effect with dark overlay and scale animation
        imageContainer.setOnMouseEntered(e -> {
            // Add dark overlay gradient on hover
            Region overlay = new Region();
            overlay.setStyle(
                "-fx-background-color: linear-gradient(to bottom, rgba(0,0,0,0.2), transparent); " +
                "-fx-background-radius: 12px 12px 0 0;"
            );
            overlay.setMouseTransparent(true);
            overlay.setId("hover-overlay");
            imageContainer.getChildren().add(overlay);
            
            // Scale image slightly on hover (if ImageView exists)
            for (Node node : imageContainer.getChildren()) {
                if (node instanceof ImageView) {
                    ImageView imgView = (ImageView) node;
                    javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(Duration.millis(500), imgView);
                    scale.setToX(1.1);
                    scale.setToY(1.1);
                    scale.play();
                    break;
                }
            }
        });
        
        imageContainer.setOnMouseExited(e -> {
            // Remove overlay
            imageContainer.getChildren().removeIf(node -> "hover-overlay".equals(node.getId()));
            
            // Reset image scale
            for (Node node : imageContainer.getChildren()) {
                if (node instanceof ImageView) {
                    ImageView imgView = (ImageView) node;
                    javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(Duration.millis(500), imgView);
                    scale.setToX(1.0);
                    scale.setToY(1.0);
                    scale.play();
                    break;
                }
            }
        });

        // 2. Content Area
        VBox content = new VBox(12);
        content.setPadding(new Insets(20)); // p-5 = 20px padding
        
        // Title - 18px, Medium weight, line-clamp-1
        String recipeName = recipe.getName() != null ? recipe.getName() : "Unnamed Recipe";
        Label title = new Label(recipeName);
        title.setStyle("-fx-font-weight: 500; -fx-font-size: 18px; -fx-text-fill: #111827;");
        title.setWrapText(true);
        title.setMaxHeight(27); // line-clamp-1 = 18px * 1.5 = 27px
        
        // Meta Row (Time, Cals, Servings) - gap-2 = 8px
        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        
        // Time - Use actual cook time if available
        Integer cookTime = recipe.getCookTimeMinutes();
        String timeText = (cookTime != null && cookTime > 0) 
            ? cookTime + " min" 
            : "-- min";
        Label timeLabel = createMetaLabel(timeText, "/svg/clock.svg", "#1d4ed8", "#eff6ff"); // Blue theme
        
        // Calories - Use actual calories if available
        String cals;
        if (recipe.getNutritionInfo() != null && recipe.getNutritionInfo().getCalories() > 0) {
            cals = String.valueOf(recipe.getNutritionInfo().getCalories());
        } else {
            cals = "--";
        }
        Label calLabel = createMetaLabel(cals, "/svg/fire-flame.svg", "#c2410c", "#fff7ed"); // Orange theme
        
        // Servings
        Label servLabel = createMetaLabel(String.valueOf(recipe.getServingSize()), "/svg/users.svg", "#7e22ce", "#faf5ff"); // Purple theme
        
        metaRow.getChildren().addAll(timeLabel, calLabel, servLabel);
        
        // Tags Row - Use actual dietary restrictions, limit to 2 tags
        FlowPane tagsRow = new FlowPane();
        tagsRow.setHgap(6);
        tagsRow.setVgap(6);
        
        List<String> tagTexts = new ArrayList<>();
        if (recipe.getDietaryRestrictions() != null && !recipe.getDietaryRestrictions().isEmpty()) {
            recipe.getDietaryRestrictions().stream()
                .limit(2)
                .forEach(restriction -> tagTexts.add(restriction.getDisplayName()));
        }
        
        // Add tags (max 2)
        for (int i = 0; i < Math.min(tagTexts.size(), 2); i++) {
            tagsRow.getChildren().add(createTag(tagTexts.get(i), false));
        }
        
        // Add to Plan Button - Gradient style with icon
        Button addBtn = new Button("+ Add to Plan");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #84cc16, #22c55e); " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 500; " +
            "-fx-background-radius: 12px; " +
            "-fx-cursor: hand; " +
            "-fx-padding: 10; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 2, 0, 0, 1);"
        );
        addBtn.setTooltip(new Tooltip("Add to weekly plan"));
        Node plusIcon = SvgIconLoader.loadIcon("/svg/plus.svg", 18, Color.WHITE); // w-4.5 h-4.5 = 18px
        if (plusIcon != null) {
            addBtn.setGraphic(plusIcon);
            addBtn.setGraphicTextGap(8);
        }
        
        // Hover effect for button
        addBtn.setOnMouseEntered(e -> {
            addBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #65a30d, #16a34a); " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: 500; " +
                "-fx-background-radius: 12px; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 2, 0, 0, 1);"
            );
        });
        addBtn.setOnMouseExited(e -> {
            addBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #84cc16, #22c55e); " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: 500; " +
                "-fx-background-radius: 12px; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 2, 0, 0, 1);"
            );
        });

        content.getChildren().addAll(title, metaRow, tagsRow, addBtn);
        card.getChildren().add(content);

        // Card default style (set before hover handlers)
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 16px; " +
            "-fx-border-color: #f3f4f6; " +
            "-fx-border-width: 2px; " +
            "-fx-border-radius: 16px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 4, 0, 0, 2);"
        );
        
        // Hover Effect
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 16px; " +
                "-fx-border-color: #d9f99d; " + // lime-200
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 16px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.25), 25, 0, 0, 12);" // shadow-2xl
            );
        });
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 16px; " +
                "-fx-border-color: #f3f4f6; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 16px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 4, 0, 0, 2);"
            );
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
        if (evt == null) {
            logger.warn("propertyChange called with null event");
            return;
        }
        
        String prop = evt.getPropertyName();
        if (prop == null) {
            logger.warn("propertyChange event has null property name");
            return;
        }
        
        logger.debug("PropertyChange event received: {} (old: {}, new: {})", 
            prop, evt.getOldValue(), evt.getNewValue());
        
        // If we're already on JavaFX Application Thread, execute directly
        // Otherwise, use Platform.runLater
        Runnable updateUI = () -> {
            switch (prop) {
                case RecipeBrowseViewModel.PROP_RECIPES:
                    // Clear error when recipes are successfully loaded
                    if (errorLabel != null) {
                        errorLabel.setText("");
                    }
                    List<Recipe> recipes = viewModel.getRecipes();
                    logger.debug("PropertyChange PROP_RECIPES: viewModel.getRecipes() returned {} recipes", recipes != null ? recipes.size() : 0);
                    displayRecipes(recipes);
                    // Phase 4: Update bookmark states after recipes are displayed
                    refreshBookmarkStates();
                    break;
                case RecipeBrowseViewModel.PROP_RECOMMENDATIONS:
                    if (recommendationsController != null) {
                        updateRecommendationsSection(viewModel.getRecommendations());
                    }
                    break;
                case RecipeBrowseViewModel.PROP_ERROR_MESSAGE:
                    String msg = viewModel.getErrorMessage();
                    if (StringUtil.hasContent(msg)) {
                        if (errorLabel != null) {
                            errorLabel.setText(msg);
                        }
                        if (listPanel != null && errorPanel != null) {
                            listPanel.getChildren().clear();
                            listPanel.getChildren().add(errorPanel);
                        }
                        // Check if it's a network error
                        String lowerMsg = msg.toLowerCase();
                        if (lowerMsg.contains("network") || lowerMsg.contains("connection") || 
                            lowerMsg.contains("timeout") || lowerMsg.contains("인터넷")) {
                            // Update error panel message for network errors - safe access
                            if (errorPanel != null) {
                                try {
                                    if (errorPanel.getChildren().size() > 2) {
                                        Node labelNode = errorPanel.getChildren().get(2);
                                        if (labelNode instanceof Label) {
                                            Label errorSubLabel = (Label) labelNode;
                                            errorSubLabel.setText("인터넷 연결을 확인하고 다시 시도해주세요");
                                        }
                                    }
                                } catch (IndexOutOfBoundsException e) {
                                    logger.warn("Error accessing errorPanel children: {}", e.getMessage());
                                }
                            }
                        }
                    } else {
                        if (errorLabel != null) {
                            errorLabel.setText("");
                        }
                        // If error is cleared, show empty state if no recipes
                        if (listPanel != null && emptyPanel != null) {
                            List<Recipe> currentRecipes = viewModel.getRecipes();
                            if (currentRecipes == null || currentRecipes.isEmpty()) {
                                listPanel.getChildren().clear();
                                listPanel.getChildren().add(emptyPanel);
                            }
                        }
                    }
                    break;
                default:
                    // Unknown property, ignore
                    break;
            }
        };
        
        // Execute on JavaFX Application Thread if needed
        if (Platform.isFxApplicationThread()) {
            updateUI.run();
        } else {
            Platform.runLater(updateUI);
        }
    }
    
    private VBox createRecommendedSection() {
        VBox section = new VBox(16);
        section.setPadding(new Insets(20, 0, 20, 0));
        
        // 헤더
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Node sparkIcon = SvgIconLoader.loadIcon("/svg/sparkles.svg", 18, Color.web("#fbbf24"));
        Label title = new Label("Recommended for You");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        
        if (sparkIcon != null) header.getChildren().add(sparkIcon);
        header.getChildren().add(title);
        
        // 레시피 리스트
        HBox recipeList = new HBox(16);
        recipeList.setAlignment(Pos.CENTER_LEFT);
        
        // 초기에는 로딩 표시
        Label loadingLabel = new Label("Loading recommendations...");
        loadingLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px;");
        recipeList.getChildren().add(loadingLabel);
        
        section.getChildren().addAll(header, recipeList);
        
        return section;
    }
    
    private void updateRecommendationsSection(List<Recipe> recommendations) {
        Platform.runLater(() -> {
            if (recommendationsSection == null || recommendationsSection.getChildren().size() < 2) {
                return;
            }
            
            HBox recipeList = (HBox) recommendationsSection.getChildren().get(1);
            recipeList.getChildren().clear();
            
            if (recommendations == null || recommendations.isEmpty()) {
                Label emptyLabel = new Label("No recommendations available");
                emptyLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px;");
                recipeList.getChildren().add(emptyLabel);
            } else {
                for (Recipe recipe : recommendations) {
                    VBox card = createRecommendationCard(recipe);
                    recipeList.getChildren().add(card);
                }
            }
        });
    }
    
    private VBox createRecommendationCard(Recipe recipe) {
        VBox card = new VBox();
        card.setPrefWidth(220);
        card.setMinWidth(220);
        card.setSpacing(0);
        card.setCursor(Cursor.HAND);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 2, 2);");
        
        // 이미지
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(140);
        imageContainer.setStyle("-fx-background-color: #e5e7eb; -fx-background-radius: 8px 8px 0 0;");
        imageContainer.setClip(new javafx.scene.shape.Rectangle(0, 0, 220, 140));
        ((javafx.scene.shape.Rectangle) imageContainer.getClip()).setArcWidth(8);
        ((javafx.scene.shape.Rectangle) imageContainer.getClip()).setArcHeight(8);
        
        String imageUrl = recipe.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                ImageView imageView = new ImageView();
                imageView.setFitWidth(220);
                imageView.setFitHeight(140);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                imageView.setCache(true);
                Image image = imageCache.getImage(imageUrl);
                imageView.setImage(image);
                imageContainer.getChildren().add(imageView);
            } catch (Exception e) {
                // 이미지 로딩 실패 시 placeholder 유지
            }
        }
        
        // 내용
        VBox content = new VBox(8);
        content.setPadding(new Insets(12));
        
        String recipeName = recipe.getName() != null ? recipe.getName() : "Unnamed Recipe";
        Label title = new Label(recipeName);
        title.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #111827;");
        title.setWrapText(true);
        title.setMaxHeight(40);
        
        // 메타 정보
        HBox meta = new HBox(12);
        String calText = recipe.getNutritionInfo() != null
            ? recipe.getNutritionInfo().getCalories() + " cal"
            : "-- cal";
        Label calLabel = new Label(calText);
        calLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
        
        Integer cookTime = recipe.getCookTimeMinutes();
        String timeText = (cookTime != null && cookTime > 0)
            ? cookTime + " min"
            : "--";
        Label timeLabel = new Label(timeText);
        timeLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
        
        meta.getChildren().addAll(calLabel, timeLabel);
        content.getChildren().addAll(title, meta);
        
        card.getChildren().addAll(imageContainer, content);
        
        // 클릭 이벤트
        card.setOnMouseClicked(e -> openRecipeDetail(recipe));
        
        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 12, 0, 2, 4);");
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 2, 2);");
        });
        
        return card;
    }

    private void openRecipeDetail(Recipe recipe) {
        if (recipe == null || recipeDetailViewModel == null || viewManagerModel == null) {
            return;
        }
        recipeDetailViewModel.setRecipe(recipe);
        viewManagerModel.setActiveView(ViewManager.RECIPE_DETAIL_VIEW);
    }
    
    /**
     * Phase 1: 북마크 기능 - 레시피를 My Cookbook에 저장하고 이동
     */
    private void bookmarkRecipe(Recipe recipe) {
        if (storeRecipeController == null || recipe == null) {
            if (sonner != null) {
                sonner.show("Error", "Unable to bookmark recipe. Please try again.", Sonner.Type.ERROR);
            }
            return;
        }
        
        try {
            // Recipe를 StoreRecipeInputData로 변환
            String recipeName = recipe.getName();
            if (recipeName == null || recipeName.trim().isEmpty()) {
                if (sonner != null) {
                    sonner.show("Error", "Recipe name is missing.", Sonner.Type.ERROR);
                }
                return;
            }
            
            List<String> ingredients = recipe.getIngredients() != null 
                ? new ArrayList<>(recipe.getIngredients())
                : new ArrayList<>();
            
            // steps는 String이므로 List<String>으로 변환
            List<String> steps = new ArrayList<>();
            if (recipe.getSteps() != null && !recipe.getSteps().trim().isEmpty()) {
                String[] stepArray = recipe.getSteps().split("\\r?\\n");
                for (String step : stepArray) {
                    String trimmed = step.trim();
                    if (!trimmed.isEmpty()) {
                        steps.add(trimmed);
                    }
                }
            }
            
            int servingSize = recipe.getServingSize();
            if (servingSize <= 0) {
                servingSize = 1; // 기본값
            }
            
            // 중복 체크 (선택 사항 - Phase 4에서 개선 가능)
            try {
                List<Recipe> existingRecipes = recipeRepository.findByName(recipeName);
                if (existingRecipes != null && !existingRecipes.isEmpty()) {
                    boolean exactMatch = existingRecipes.stream()
                        .anyMatch(r -> r != null && r.getName() != null && 
                                      r.getName().equalsIgnoreCase(recipeName));
                    
                    if (exactMatch) {
                        // 이미 저장된 경우
                        if (sonner != null) {
                            sonner.show("Already Saved", "This recipe is already in your cookbook.", Sonner.Type.INFO);
                        }
                        // My Cookbook으로 이동
                        if (viewManagerModel != null) {
                            viewManagerModel.setActiveView(ViewManager.STORE_RECIPE_VIEW);
                        }
                        return;
                    }
                }
            } catch (DataAccessException e) {
                // 중복 체크 실패해도 계속 진행
                logger.debug("Failed to check duplicate: {}", e.getMessage());
            }
            
            // StoreRecipeController 호출 (recipeId는 null로 새 레시피로 저장)
            storeRecipeController.execute(
                null,  // recipeId (null = 새 레시피)
                recipeName,
                ingredients,
                steps,
                servingSize
            );
            
            // 성공 메시지는 StoreRecipePresenter에서 처리되지만,
            // 여기서도 토스트를 표시할 수 있음
            if (sonner != null) {
                sonner.show("Saved!", "Recipe saved to your cookbook.", Sonner.Type.SUCCESS);
            }
            
            // Phase 4: 북마크 상태 및 개수 업데이트
            refreshBookmarkStates();
            updateSavedCount();
            
            // My Cookbook으로 이동 (약간의 지연을 두어 토스트 메시지가 보이도록)
            if (viewManagerModel != null) {
                PauseTransition pause = new PauseTransition(Duration.millis(800));
                pause.setOnFinished(e -> viewManagerModel.setActiveView(ViewManager.STORE_RECIPE_VIEW));
                pause.play();
            }
            
        } catch (Exception e) {
            logger.error("Failed to bookmark recipe: {}", e.getMessage(), e);
            if (sonner != null) {
                sonner.show("Error", "Failed to save recipe. Please try again.", Sonner.Type.ERROR);
            }
        }
    }
    
    /**
     * Phase 4: 북마크 상태를 확인하고 버튼 아이콘을 업데이트
     */
    private void updateBookmarkButtonIcon(Button saveBtn, Recipe recipe) {
        if (saveBtn == null || recipe == null) {
            return;
        }
        
        final boolean isBookmarked;
        try {
            isBookmarked = isRecipeBookmarked(recipe);
        } catch (Exception e) {
            logger.warn("Failed to check bookmark status in updateBookmarkButtonIcon: {}", e.getMessage());
            // Use false as default if check fails - treat as not bookmarked
            // Set default style and return
            Node bookmarkIcon = SvgIconLoader.loadIcon("/svg/bookmark.svg", 18, Color.web("#4b5563"));
            if (bookmarkIcon == null) {
                bookmarkIcon = SvgIconLoader.loadIcon("/svg/book.svg", 18, Color.web("#4b5563"));
            }
            if (bookmarkIcon != null) {
                saveBtn.setGraphic(bookmarkIcon);
            }
            saveBtn.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.95); " +
                "-fx-background-radius: 12px; " +
                "-fx-min-width: 40px; " +
                "-fx-min-height: 40px; " +
                "-fx-max-width: 40px; " +
                "-fx-max-height: 40px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 3);"
            );
            return;
        }
        
        // 북마크 상태에 따라 아이콘 및 스타일 변경
        Node bookmarkIcon;
        if (isBookmarked) {
            // 북마크된 상태 - 그라데이션 배경
            bookmarkIcon = SvgIconLoader.loadIcon("/svg/bookmark.svg", 18, Color.WHITE);
            if (bookmarkIcon == null) {
                bookmarkIcon = SvgIconLoader.loadIcon("/svg/book-fill.svg", 18, Color.WHITE);
            }
            if (bookmarkIcon == null) {
                bookmarkIcon = SvgIconLoader.loadIcon("/svg/book.svg", 18, Color.WHITE);
            }
            saveBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #84cc16, #22c55e); " +
                "-fx-background-radius: 12px; " +
                "-fx-min-width: 40px; " +
                "-fx-min-height: 40px; " +
                "-fx-max-width: 40px; " +
                "-fx-max-height: 40px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 3);"
            );
        } else {
            // 북마크 안 된 상태 - 흰색 배경
            bookmarkIcon = SvgIconLoader.loadIcon("/svg/bookmark.svg", 18, Color.web("#4b5563"));
            if (bookmarkIcon == null) {
                bookmarkIcon = SvgIconLoader.loadIcon("/svg/book.svg", 18, Color.web("#4b5563"));
            }
            saveBtn.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.95); " +
                "-fx-background-radius: 12px; " +
                "-fx-min-width: 40px; " +
                "-fx-min-height: 40px; " +
                "-fx-max-width: 40px; " +
                "-fx-max-height: 40px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 3);"
            );
        }
        
        if (bookmarkIcon != null) {
            saveBtn.setGraphic(bookmarkIcon);
        }
        
        // Hover effect for bookmark button
        saveBtn.setOnMouseEntered(e -> {
            if (isBookmarked) {
                saveBtn.setStyle(
                    "-fx-background-color: linear-gradient(to right, #65a30d, #16a34a); " +
                    "-fx-background-radius: 12px; " +
                    "-fx-min-width: 40px; " +
                    "-fx-min-height: 40px; " +
                    "-fx-max-width: 40px; " +
                    "-fx-max-height: 40px; " +
                    "-fx-cursor: hand; " +
                    "-fx-scale-x: 1.1; " +
                    "-fx-scale-y: 1.1; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 3);"
                );
            } else {
                saveBtn.setStyle(
                    "-fx-background-color: white; " +
                    "-fx-background-radius: 12px; " +
                    "-fx-min-width: 40px; " +
                    "-fx-min-height: 40px; " +
                    "-fx-max-width: 40px; " +
                    "-fx-max-height: 40px; " +
                    "-fx-cursor: hand; " +
                    "-fx-scale-x: 1.1; " +
                    "-fx-scale-y: 1.1; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 3);"
                );
            }
        });
        
        saveBtn.setOnMouseExited(e -> {
            if (isBookmarked) {
                saveBtn.setStyle(
                    "-fx-background-color: linear-gradient(to right, #84cc16, #22c55e); " +
                    "-fx-background-radius: 12px; " +
                    "-fx-min-width: 40px; " +
                    "-fx-min-height: 40px; " +
                    "-fx-max-width: 40px; " +
                    "-fx-max-height: 40px; " +
                    "-fx-cursor: hand; " +
                    "-fx-scale-x: 1.0; " +
                    "-fx-scale-y: 1.0; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 3);"
                );
            } else {
                saveBtn.setStyle(
                    "-fx-background-color: rgba(255, 255, 255, 0.95); " +
                    "-fx-background-radius: 12px; " +
                    "-fx-min-width: 40px; " +
                    "-fx-min-height: 40px; " +
                    "-fx-max-width: 40px; " +
                    "-fx-max-height: 40px; " +
                    "-fx-cursor: hand; " +
                    "-fx-scale-x: 1.0; " +
                    "-fx-scale-y: 1.0; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 3);"
                );
            }
        });
    }
    
    /**
     * Phase 4: 레시피가 북마크되어 있는지 확인
     */
    private boolean isRecipeBookmarked(Recipe recipe) {
        if (recipe == null || recipeRepository == null) {
            return false;
        }
        
        try {
            List<Recipe> existingRecipes = recipeRepository.findByName(recipe.getName());
            if (existingRecipes != null && !existingRecipes.isEmpty()) {
                // 이름이 정확히 일치하는 레시피가 있는지 확인 (대소문자 무시)
                return existingRecipes.stream()
                    .anyMatch(r -> r != null && r.getName() != null && r.getName().equalsIgnoreCase(recipe.getName()));
            }
        } catch (DataAccessException e) {
            logger.debug("Failed to check bookmark status: {}", e.getMessage());
        } catch (Exception e) {
            // Catch all exceptions including JsonSyntaxException from malformed JSON files
            logger.warn("Exception while checking bookmark status for recipe '{}': {}", 
                recipe.getName(), e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Phase 4: 저장된 레시피 개수를 업데이트
     */
    private void updateSavedCount() {
        if (recipeRepository == null || savedCountButton == null) {
            return;
        }
        
        // Run in background thread to avoid blocking UI
        new Thread(() -> {
            try {
                List<Recipe> allSavedRecipes = recipeRepository.findAll();
                int count = allSavedRecipes != null ? allSavedRecipes.size() : 0;
                
                // UI 업데이트는 JavaFX Application Thread에서
                Platform.runLater(() -> {
                    if (savedCountButton != null) {
                        savedCountButton.setText(count + " Saved");
                    }
                });
            } catch (DataAccessException e) {
                logger.warn("Failed to update saved count: {}", e.getMessage());
            } catch (Exception e) {
                // Catch all exceptions including JsonSyntaxException from malformed JSON files
                logger.warn("Exception while updating saved count: {}", e.getMessage());
            }
        }).start();
    }
    
    /**
     * Phase 4: 모든 레시피 카드의 북마크 상태를 업데이트
     */
    private void refreshBookmarkStates() {
        if (listPanel == null) {
            return;
        }
        
        Platform.runLater(() -> {
            try {
                for (Node node : listPanel.getChildren()) {
                    if (node instanceof VBox) {
                        VBox card = (VBox) node;
                        // 카드에서 북마크 버튼 찾기
                        StackPane imageContainer = findImageContainer(card);
                        if (imageContainer != null) {
                            for (Node child : imageContainer.getChildren()) {
                                if (child instanceof Button) {
                                    Button saveBtn = (Button) child;
                                    Recipe recipe = (Recipe) saveBtn.getUserData();
                                    if (recipe != null) {
                                        try {
                                            updateBookmarkButtonIcon(saveBtn, recipe);
                                        } catch (Exception e) {
                                            logger.warn("Failed to update bookmark icon for recipe '{}': {}", 
                                                recipe.getName(), e.getMessage());
                                            // Continue with next recipe
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("Exception in refreshBookmarkStates: {}", e.getMessage());
            }
        });
    }
    
    /**
     * Phase 4: 카드에서 이미지 컨테이너 찾기
     */
    private StackPane findImageContainer(VBox card) {
        if (card == null || card.getChildren().isEmpty()) {
            return null;
        }
        
        Node firstChild = card.getChildren().get(0);
        if (firstChild instanceof StackPane) {
            return (StackPane) firstChild;
        }
        
        return null;
    }
}
