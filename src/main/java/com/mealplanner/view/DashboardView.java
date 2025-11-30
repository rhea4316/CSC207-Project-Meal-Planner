package com.mealplanner.view;

import com.mealplanner.app.SessionManager;
import com.mealplanner.entity.MealType;
import com.mealplanner.entity.NutritionGoals;
import com.mealplanner.entity.NutritionInfo;
import com.mealplanner.entity.Recipe;
import com.mealplanner.entity.Schedule;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.AddMealController;
import com.mealplanner.interface_adapter.controller.GetRecommendationsController;
import com.mealplanner.interface_adapter.view_model.RecipeBrowseViewModel;
import com.mealplanner.interface_adapter.view_model.RecipeDetailViewModel;
import com.mealplanner.interface_adapter.view_model.ScheduleViewModel;
import com.mealplanner.repository.RecipeRepository;
import com.mealplanner.view.component.*;
import com.mealplanner.view.component.Sonner;
import com.mealplanner.view.component.SelectRecipeDialog;
import com.mealplanner.view.component.AddToMealPlanDialog;
import com.mealplanner.view.util.SvgIconLoader;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DashboardView extends BorderPane implements PropertyChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(DashboardView.class);
    
    // Layout constants for consistent grid spacing
    private static final double GRID_GAP = 24.0;
    private static final double CARD_PADDING = 20.0;
    
    public final String viewName = "DashboardView";
    private final ViewManagerModel viewManagerModel;
    private final ScheduleViewModel scheduleViewModel;
    private final RecipeRepository recipeRepository;
    /**
     * Controller for getting recipe recommendations.
     * Phase 2: Injected but not yet used (infrastructure only).
     * Phase 3: Will be used to load and display recommended recipes.
     * Phase 4: Used in auto-generate functionality.
     */
    private final GetRecommendationsController recommendationsController;
    
    /**
     * ViewModel for receiving recipe recommendations.
     * Phase 3: Injected to receive recommendations from GetRecommendationsPresenter.
     * Phase 4: Used in auto-generate to get recommended recipes.
     */
    private final RecipeBrowseViewModel recommendationsViewModel;
    
    /**
     * ViewModel for recipe detail view.
     * Phase 3: Used to navigate to recipe detail when clicking on recommendation cards.
     */
    private final RecipeDetailViewModel recipeDetailViewModel;
    
    /**
     * Controller for adding meals to schedule.
     * Phase 4: Used for auto-generate functionality to add meals to today's schedule.
     */
    private final AddMealController addMealController;
    
    /**
     * Flag to track if we're waiting for recommendations for auto-generate.
     * Phase 4: Used to distinguish between user-initiated recommendation requests
     * and auto-generate requests.
     */
    private boolean isAutoGenerating = false;

    // Default nutrition goals are now retrieved from user or NutritionGoals.createDefault()

    // Dynamic UI Components
    private HBox mealsContainer; // Legacy reference, kept for backward compatibility
    private GridPane mealsGrid; // 3-column grid for meal cards
    private Label calorieValueLabel; // e.g. "1,200 of 2,000 cal"
    private StackPane circularProgressPane;
    private Label remainingCaloriesLabel;
    private Progress proteinBar, carbsBar, fatBar;
    private Label proteinValLabel, carbsValLabel, fatValLabel; // Value labels for nutrient bars
    private Label welcomeLabel; // Welcome message label (dynamically updated based on logged-in user)
    private HBox recommendedRecipeList; // Legacy reference, kept for backward compatibility
    private GridPane recommendedRecipeGrid; // 3-column grid for recommended recipe cards
    private final Sonner sonner;

    /**
     * Constructor for DashboardView.
     * Phase 2: GetRecommendationsController is injected but not yet used.
     * Phase 3: RecipeBrowseViewModel is injected to receive recommendations.
     * Phase 4: AddMealController is injected for auto-generate functionality.
     * 
     * @param viewManagerModel The view manager model for navigation
     * @param scheduleViewModel The schedule view model for meal data
     * @param recipeRepository The recipe repository for recipe data
     * @param recommendationsController The controller for recommendations
     * @param recommendationsViewModel The view model for receiving recommendations (Phase 3)
     * @param recipeDetailViewModel The view model for recipe detail navigation (Phase 3)
     * @param addMealController The controller for adding meals to schedule (Phase 4: auto-generate)
     */
    public DashboardView(ViewManagerModel viewManagerModel, ScheduleViewModel scheduleViewModel, RecipeRepository recipeRepository, GetRecommendationsController recommendationsController, RecipeBrowseViewModel recommendationsViewModel, RecipeDetailViewModel recipeDetailViewModel, AddMealController addMealController) {
        this.viewManagerModel = viewManagerModel;
        this.scheduleViewModel = scheduleViewModel;
        this.recipeRepository = recipeRepository;
        this.recommendationsController = recommendationsController;
        this.recommendationsViewModel = recommendationsViewModel;
        this.recipeDetailViewModel = recipeDetailViewModel;
        this.addMealController = addMealController;
        this.scheduleViewModel.addPropertyChangeListener(this);
        // Phase 3: Listen to recommendations changes (for display and auto-generate)
        if (recommendationsViewModel != null) {
            recommendationsViewModel.addPropertyChangeListener(this);
        }
        this.sonner = new Sonner();

        // 1. Layout & Background
        setBackground(new Background(new BackgroundFill(Color.web("#f7f8f9"), CornerRadii.EMPTY, Insets.EMPTY)));
        setPadding(new Insets(30, 40, 30, 40));

        VBox topSection = new VBox(20);
        
        // Welcome Title
        VBox titleBox = new VBox(5);
        welcomeLabel = new Label("Welcome back!");
        welcomeLabel.getStyleClass().addAll("welcome-title", "text-gray-900");
        
        Label subLabel = new Label("Let's plan your meals for today");
        subLabel.getStyleClass().addAll("welcome-subtitle", "text-gray-500");
        
        titleBox.getChildren().addAll(welcomeLabel, subLabel);
        topSection.getChildren().add(titleBox);
        
        setTop(topSection);
        BorderPane.setMargin(topSection, new Insets(0, 0, 30, 0));

        // Content Grid (2:1 ratio)
        GridPane contentGrid = new GridPane();
        contentGrid.setHgap(24);
        contentGrid.setVgap(24);

        // Left Column (65% width)
        VBox leftColumn = new VBox(24);
        // leftColumn.setPrefWidth(Region.USE_COMPUTED_SIZE); // Grid handles this
        
        // Today's Menu Section
        VBox menuSection = createTodayMenuSection();
        leftColumn.getChildren().add(menuSection);
        
        // Recommended for You Section
        VBox recipeSection = createRecommendedSection();
        leftColumn.getChildren().add(recipeSection);

        // Right Column (35% width)
        VBox rightColumn = new VBox(24);
        
        // Nutrition Progress Section
        VBox nutritionSection = createNutritionProgressSection();
        rightColumn.getChildren().add(nutritionSection);
        
        // Quick Actions Section
        VBox quickActionsSection = createQuickActionsSection();
        rightColumn.getChildren().add(quickActionsSection);

        // Add to grid
        contentGrid.add(leftColumn, 0, 0);
        contentGrid.add(rightColumn, 1, 0);

        // Column Constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.NEVER); // Do not grow beyond content width
        col1.setMinWidth(Region.USE_COMPUTED_SIZE);
        col1.setPrefWidth(Region.USE_COMPUTED_SIZE);
        col1.setMaxWidth(Region.USE_COMPUTED_SIZE);
        
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS); // Take remaining space
        col2.setMinWidth(250); 
        
        contentGrid.getColumnConstraints().addAll(col1, col2);

        setCenter(contentGrid);
        
        // Responsive Layout Listener
        widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();
            if (width < 1000) {
                // Stack Columns Vertically
                contentGrid.getChildren().clear();
                contentGrid.getColumnConstraints().clear();
                contentGrid.getRowConstraints().clear();
                
                contentGrid.add(leftColumn, 0, 0);
                contentGrid.add(rightColumn, 0, 1);
                
                ColumnConstraints c1 = new ColumnConstraints();
                c1.setPercentWidth(100);
                contentGrid.getColumnConstraints().add(c1);
                
                // Make cards flexible in vertical layout
                if (mealsContainer != null) {
                    mealsContainer.setSpacing(10);
                }
            } else {
                // Restore 2-Column Layout
                contentGrid.getChildren().clear();
                contentGrid.getColumnConstraints().clear();
                contentGrid.getRowConstraints().clear();
                
                contentGrid.add(leftColumn, 0, 0);
                contentGrid.add(rightColumn, 1, 0);
                
                // Re-apply auto-width constraints
                ColumnConstraints c1 = new ColumnConstraints();
                c1.setHgrow(Priority.NEVER);
                c1.setMinWidth(Region.USE_COMPUTED_SIZE);
                
                ColumnConstraints c2 = new ColumnConstraints();
                c2.setHgrow(Priority.ALWAYS);
                c2.setMinWidth(250);
                
                contentGrid.getColumnConstraints().addAll(c1, c2);
                
                if (mealsContainer != null) {
                    mealsContainer.setSpacing(20);
                }
            }
        });

        // Phase 1: Add ViewManagerModel listener for username changes to update welcome message
        this.viewManagerModel.addPropertyChangeListener(this);
        
        // Phase 3: Add RecipeBrowseViewModel listener for recommendations
        if (recommendationsViewModel != null) {
            recommendationsViewModel.addPropertyChangeListener(this);
        }
        
        // Initial Update
        updateWelcomeMessage();
        updateView();
    }

    private VBox createTodayMenuSection() {
        VBox container = new VBox();
        container.getStyleClass().add("card-panel");
        container.setSpacing(CARD_PADDING);
        container.setPadding(new Insets(CARD_PADDING));

        // Header Row (Title + Auto-generate)
        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Today's Menu");
        title.getStyleClass().add("section-title");
        title.setPadding(Insets.EMPTY);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button autoGenBtn = new Button("Auto-generate");
        autoGenBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2d5016; -fx-font-weight: 600; -fx-cursor: hand;");
        Node sparkIcon = SvgIconLoader.loadIcon("/svg/sparkles.svg", 16, Color.web("#4CAF50"));
        if (sparkIcon != null) autoGenBtn.setGraphic(sparkIcon);
        // Phase 4: Add click handler for auto-generate functionality
        autoGenBtn.setOnAction(e -> handleAutoGenerate());
        
        // Hover effect: change to sparkles-fill and darker text
        autoGenBtn.setOnMouseEntered(e -> {
            Node fillIcon = SvgIconLoader.loadIcon("/svg/sparkles-fill.svg", 16, Color.web("#4CAF50"));
            if (fillIcon != null) {
                autoGenBtn.setGraphic(fillIcon);
            }
            autoGenBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1a3009; -fx-font-weight: 600; -fx-cursor: hand;");
        });
        autoGenBtn.setOnMouseExited(e -> {
            if (sparkIcon != null) autoGenBtn.setGraphic(sparkIcon);
            autoGenBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2d5016; -fx-font-weight: 600; -fx-cursor: hand;");
        });
        
        headerRow.getChildren().addAll(title, spacer, autoGenBtn);
        
        Label subTitle = new Label("Plan your meals for the day");
        subTitle.setStyle("-fx-text-fill: -fx-theme-muted-foreground; -fx-font-size: 14px;");
        
        VBox headerBox = new VBox(5); // Reduced spacing
        headerBox.getChildren().addAll(headerRow, subTitle);

        // Meals Container (3-column Grid)
        this.mealsGrid = new GridPane();
        this.mealsGrid.setHgap(GRID_GAP);
        this.mealsGrid.setVgap(GRID_GAP);
        this.mealsGrid.setMaxWidth(Double.MAX_VALUE);
        
        // Store reference for updating meals
        mealsContainer = new HBox(); // Keep for backward compatibility, but use grid for layout
        
        // Column constraints for 3 equal columns
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        col1.setPercentWidth(33.33);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setPercentWidth(33.33);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setHgrow(Priority.ALWAYS);
        col3.setPercentWidth(33.33);
        this.mealsGrid.getColumnConstraints().addAll(col1, col2, col3);

        container.getChildren().addAll(headerBox, mealsGrid);
        return container;
    }

    private VBox createNutritionProgressSection() {
        VBox container = new VBox();
        container.getStyleClass().add("card-panel");
        container.setSpacing(20);
        // Allow container to shrink with window height
        // Remove fixed minHeight to allow responsive resizing

        Label title = new Label("Daily Nutrition");
        title.getStyleClass().add("section-title");
        title.setAlignment(Pos.CENTER_LEFT);

        circularProgressPane = createCircularProgress();
        
        VBox circleContainer = new VBox(10);
        circleContainer.setAlignment(Pos.CENTER);
        
        remainingCaloriesLabel = new Label("Remaining\n1150 cal");
        remainingCaloriesLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        remainingCaloriesLabel.setStyle("-fx-text-fill: -fx-theme-muted-foreground; -fx-font-size: 14px;");
        
        circleContainer.getChildren().addAll(circularProgressPane, remainingCaloriesLabel);

        VBox nutrientBars = new VBox(15);
        // VBox.setVgrow(nutrientBars, Priority.ALWAYS); // Allow this to grow if needed, but fixed size is usually fine
        
        VBox proteinBox = createNutrientBar("Protein", "0 / 0g", 0.0, "protein");
        proteinBar = (Progress) proteinBox.getChildren().get(1);
        HBox proteinLabelRow = (HBox) proteinBox.getChildren().get(0);
        proteinValLabel = (Label) proteinLabelRow.getChildren().get(2);
        
        VBox carbsBox = createNutrientBar("Carbs", "0 / 0g", 0.0, "carbs");
        carbsBar = (Progress) carbsBox.getChildren().get(1);
        HBox carbsLabelRow = (HBox) carbsBox.getChildren().get(0);
        carbsValLabel = (Label) carbsLabelRow.getChildren().get(2);
        
        VBox fatBox = createNutrientBar("Fat", "0 / 0g", 0.0, "fat");
        fatBar = (Progress) fatBox.getChildren().get(1);
        HBox fatLabelRow = (HBox) fatBox.getChildren().get(0);
        fatValLabel = (Label) fatLabelRow.getChildren().get(2);

        nutrientBars.getChildren().addAll(proteinBox, carbsBox, fatBox);

        container.getChildren().addAll(title, circleContainer, nutrientBars);
        return container;
    }

    private StackPane createCircularProgress() {
        StackPane stackPane = new StackPane();
        stackPane.setPrefSize(160, 160);

        Circle backgroundCircle = new Circle(70);
        backgroundCircle.setFill(Color.TRANSPARENT);
        backgroundCircle.setStroke(Color.web("#f3f4f6")); // -fx-theme-muted updated
        backgroundCircle.setStrokeWidth(12);

        Arc progressArc = new Arc(0, 0, 70, 70, 90, -150);
        progressArc.setType(ArcType.OPEN);
        progressArc.setFill(null);
        progressArc.setStroke(Color.web("#58c937")); // -fx-theme-primary (Green) updated
        progressArc.setStrokeWidth(12);
        progressArc.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);

        // Wrap shapes in a Group to maintain relative alignment
        javafx.scene.Group progressGroup = new javafx.scene.Group(backgroundCircle, progressArc);

        VBox textBox = new VBox(2);
        textBox.setAlignment(Pos.CENTER);
        calorieValueLabel = new Label("850");
        calorieValueLabel.getStyleClass().add("text-gray-900");
        calorieValueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 28px;");
        
        Label totalLabel = new Label("of 0 cal");
        totalLabel.getStyleClass().add("text-gray-500");
        totalLabel.setStyle("-fx-font-size: 12px;");
        
        textBox.getChildren().addAll(calorieValueLabel, totalLabel);

        stackPane.getChildren().addAll(progressGroup, textBox);
        return stackPane;
    }

    private VBox createNutrientBar(String labelText, String valueText, double progress, String type) {
        VBox container = new VBox(8);
        
        HBox labelRow = new HBox();
        labelRow.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(labelText);
        nameLabel.getStyleClass().add("text-gray-700");
        nameLabel.setStyle("-fx-font-size: 14px;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label valLabel = new Label(valueText);
        valLabel.getStyleClass().add("text-gray-500");
        valLabel.setStyle("-fx-font-size: 12px;");
        
        labelRow.getChildren().addAll(nameLabel, spacer, valLabel);

        Progress bar = new Progress(progress);
        bar.getStyleClass().add("progress-bar-" + type);
        bar.setPrefHeight(8);
        bar.setMaxWidth(Double.MAX_VALUE); // Ensure it fills width 

        container.getChildren().addAll(labelRow, bar);
        return container;
    }

    private VBox createRecommendedSection() {
        VBox container = new VBox();
        container.getStyleClass().add("card-panel");
        container.setSpacing(CARD_PADDING);
        container.setPadding(new Insets(CARD_PADDING));

        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Recommended for You");
        title.getStyleClass().add("section-title");
        title.setPadding(Insets.EMPTY);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label viewAll = new Label("View all >");
        viewAll.getStyleClass().add("text-lime-600");
        viewAll.setStyle("-fx-cursor: hand; -fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #65a30d;");
        viewAll.setOnMouseClicked(e -> viewManagerModel.setActiveView(ViewManager.BROWSE_RECIPE_VIEW));
        
        // Hover effect: change color
        viewAll.setOnMouseEntered(e -> viewAll.setStyle("-fx-cursor: hand; -fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #4d7c0f;"));
        viewAll.setOnMouseExited(e -> viewAll.setStyle("-fx-cursor: hand; -fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #65a30d;"));
        
        headerRow.getChildren().addAll(title, spacer, viewAll);

        // 3-column Grid for recipe cards (matching Today's Menu layout)
        this.recommendedRecipeGrid = new GridPane();
        this.recommendedRecipeGrid.setHgap(GRID_GAP);
        this.recommendedRecipeGrid.setVgap(GRID_GAP);
        this.recommendedRecipeGrid.setMaxWidth(Double.MAX_VALUE);
        
        // Column constraints for 3 equal columns (matching Today's Menu)
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        col1.setPercentWidth(33.33);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setPercentWidth(33.33);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setHgrow(Priority.ALWAYS);
        col3.setPercentWidth(33.33);
        this.recommendedRecipeGrid.getColumnConstraints().addAll(col1, col2, col3);

        // Phase 3: Use dynamic recipe list - now using GridPane
        recommendedRecipeList = new HBox(); // Keep for backward compatibility, but use grid for layout

        container.getChildren().addAll(headerRow, this.recommendedRecipeGrid);
        
        // Phase 3: Load recommendations on initialization
        loadRecommendations();
        
        return container;
    }
    
    /**
     * Phase 3: Loads recommendations for the current user.
     * Called on initialization and when user logs in.
     */
    private void loadRecommendations() {
        if (recommendationsController == null) {
            if (recommendedRecipeGrid != null) {
                recommendedRecipeGrid.getChildren().clear();
                Label noRecommendationsLabel = new Label("No recommendations available");
                noRecommendationsLabel.getStyleClass().add("text-gray-500");
                recommendedRecipeGrid.add(noRecommendationsLabel, 0, 0, 3, 1);
            }
            return;
        }
        
        String userId = SessionManager.getInstance().getCurrentUserId();
        if (userId == null || userId.trim().isEmpty()) {
            if (recommendedRecipeGrid != null) {
                recommendedRecipeGrid.getChildren().clear();
                Label loginPromptLabel = new Label("Please log in to see recommendations");
                loginPromptLabel.getStyleClass().add("text-gray-500");
                recommendedRecipeGrid.add(loginPromptLabel, 0, 0, 3, 1);
            }
            return;
        }
        
        // Show loading state
        if (recommendedRecipeGrid != null) {
            recommendedRecipeGrid.getChildren().clear();
            Label loadingLabel = new Label("Loading recommendations...");
            loadingLabel.getStyleClass().add("text-gray-500");
            recommendedRecipeGrid.add(loadingLabel, 0, 0, 3, 1);
        }
        
        // Request recommendations
        try {
            recommendationsController.execute(userId);
        } catch (Exception e) {
            logger.error("Failed to load recommendations", e);
            if (recommendedRecipeGrid != null) {
                recommendedRecipeGrid.getChildren().clear();
                Label errorLabel = new Label("Error loading recommendations");
                errorLabel.getStyleClass().add("text-gray-500");
                recommendedRecipeGrid.add(errorLabel, 0, 0, 3, 1);
            }
        }
    }
    
    /**
     * Phase 3: Updates the recommended recipes display when recommendations are received.
     * Called when RecipeBrowseViewModel fires "recommendations" property change.
     */
    private void updateRecommendedRecipes() {
        if (recommendedRecipeGrid == null || recommendationsViewModel == null) {
            return;
        }
        
        // Skip if we're in auto-generate mode (will be handled by processAutoGenerateRecommendations)
        if (isAutoGenerating) {
            return;
        }
        
        List<Recipe> recommendations = recommendationsViewModel.getRecommendations();
        if (recommendedRecipeGrid != null) {
            recommendedRecipeGrid.getChildren().clear();
        }
        if (recommendedRecipeList != null) {
            recommendedRecipeList.getChildren().clear();
        }
        
        if (recommendations == null || recommendations.isEmpty()) {
            Label noRecommendationsLabel = new Label("No recommendations available");
            noRecommendationsLabel.getStyleClass().add("text-gray-500");
            if (recommendedRecipeGrid != null) {
                recommendedRecipeGrid.add(noRecommendationsLabel, 0, 0, 3, 1);
            }
            return;
        }
        
        // Display up to 3 recommendations in 3-column grid (matching Today's Menu layout)
        int count = Math.min(3, recommendations.size());
        for (int i = 0; i < count; i++) {
            Recipe recipe = recommendations.get(i);
            if (recipe != null && recommendedRecipeGrid != null) {
                VBox card = createRecipeCard(recipe);
                recommendedRecipeGrid.add(card, i, 0);
                GridPane.setHgrow(card, Priority.ALWAYS);
            }
        }
    }
    
    /**
     * Phase 3: Creates a recipe card from a Recipe object.
     * Replaces the old createRecipeCard(String, String, String, String) method.
     */
    private VBox createRecipeCard(Recipe recipe) {
        if (recipe == null) {
            return createEmptyRecipeCard();
        }
        
        VBox card = new VBox(0);
        String defaultStyle = "-fx-background-color: #f9fafb; -fx-background-radius: 12px; -fx-effect: null; -fx-border-color: transparent; -fx-border-width: 1px; -fx-border-radius: 12px;";
        String hoverStyle = "-fx-background-color: #ffffff; -fx-background-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 12, 0, 0, 2); -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 12px;";
        
        card.setStyle(defaultStyle);
        // Remove fixed width to allow grid to control card width (matching meal cards)
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPrefHeight(240);
        card.setMinHeight(240);
        card.setMaxHeight(240);
        card.setCursor(javafx.scene.Cursor.HAND);
        
        // 개선된 호버 효과
        card.setOnMouseEntered(e -> {
            card.setStyle(hoverStyle);
            card.setScaleX(1.03);
            card.setScaleY(1.03);
        });
        card.setOnMouseExited(e -> {
            card.setStyle(defaultStyle);
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });
        
        // 클릭 피드백 개선
        card.setOnMousePressed(e -> {
            card.setScaleX(0.98);
            card.setScaleY(0.98);
        });
        card.setOnMouseReleased(e -> {
            card.setScaleX(1.03);
            card.setScaleY(1.03);
        });
        
        Region imagePlaceholder = new Region();
        imagePlaceholder.setPrefHeight(140);
        imagePlaceholder.setMinHeight(140);
        imagePlaceholder.setMaxHeight(140);
        imagePlaceholder.setStyle("-fx-background-color: #E5E7EB; -fx-background-radius: 12px 12px 0 0;");
        
        VBox content = new VBox(0);
        content.setPadding(new Insets(14, 14, 14, 14));
        content.setPrefHeight(100);
        content.setMinHeight(100);
        content.setMaxHeight(100);
        
        // Title container with fixed height for 2 lines max
        VBox titleContainer = new VBox();
        titleContainer.setPrefHeight(48);
        titleContainer.setMinHeight(48);
        titleContainer.setMaxHeight(48);
        
        String recipeName = recipe.getName();
        // Truncate to approximately 2 lines (roughly 40-45 characters for 14px font)
        String displayName = truncateTextToTwoLines(recipeName, 40);
        
        Label nameLabel = new Label(displayName);
        nameLabel.getStyleClass().add("text-gray-900");
        nameLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-line-spacing: 2px;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(192); // 220 - 14*2 (padding)
        nameLabel.setMaxHeight(44); // Allow 2 lines: 22px per line
        titleContainer.getChildren().add(nameLabel);
        
        HBox meta = new HBox(10);
        meta.setAlignment(Pos.CENTER_LEFT);
        meta.setStyle("-fx-alignment: center-left;");
        
        // Fire Icon for Calories
        HBox calBox = new HBox(4);
        calBox.setAlignment(Pos.CENTER_LEFT);
        Node fireIcon = SvgIconLoader.loadIcon("/svg/fire-flame.svg", 12, Color.web("#6B7280"));
        String calText = recipe.getNutritionInfo() != null 
            ? (int)recipe.getNutritionInfo().getCalories() + " cal"
            : "-- cal";
        Label calLabel = new Label(calText);
        calLabel.getStyleClass().add("text-gray-500");
        calLabel.setStyle("-fx-font-size: 11px;");
        if (fireIcon != null) calBox.getChildren().add(fireIcon);
        calBox.getChildren().add(calLabel);
        
        // Clock Icon for Time
        HBox timeBox = new HBox(4);
        timeBox.setAlignment(Pos.CENTER_LEFT);
        Node clockIcon = SvgIconLoader.loadIcon("/svg/clock.svg", 12, Color.web("#6B7280"));
        Integer cookTime = recipe.getCookTimeMinutes();
        String timeText = (cookTime != null && cookTime > 0) 
            ? cookTime + " min"
            : "--";
        Label timeLabel = new Label(timeText);
        timeLabel.getStyleClass().add("text-gray-500");
        timeLabel.setStyle("-fx-font-size: 11px;");
        if (clockIcon != null) timeBox.getChildren().add(clockIcon);
        timeBox.getChildren().add(timeLabel);
        
        meta.getChildren().addAll(calBox, timeBox);
        
        // Spacer to push metadata to bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // Metadata container pinned to bottom
        HBox metaContainer = new HBox();
        metaContainer.setAlignment(Pos.BOTTOM_LEFT);
        metaContainer.setPadding(new Insets(0, 0, 0, 0));
        metaContainer.getChildren().add(meta);
        
        content.getChildren().addAll(titleContainer, spacer, metaContainer);
        
        card.getChildren().addAll(imagePlaceholder, content);
        
        // Phase 3: Add click handler to navigate to recipe detail - 개선된 클릭 피드백
        card.setOnMouseClicked(e -> {
            // 클릭 후 약간의 딜레이를 두고 레시피 상세로 이동 (시각적 피드백)
            javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(100), card);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.8);
            fadeOut.setOnFinished(event -> {
                openRecipeDetail(recipe);
                card.setOpacity(1.0);
            });
            fadeOut.play();
        });
        
        return card;
    }
    
    /**
     * Truncates text to approximately 2 lines.
     * @param text The text to truncate
     * @param maxChars Maximum characters for 2 lines (approximately 40-45 for 14px font)
     * @return Truncated text with ellipsis if needed
     */
    private String truncateTextToTwoLines(String text, int maxChars) {
        if (text == null || text.length() <= maxChars) {
            return text;
        }
        // Find the last space before maxChars to avoid cutting words
        int lastSpace = text.lastIndexOf(' ', maxChars - 3);
        if (lastSpace > maxChars / 2) {
            return text.substring(0, lastSpace) + "...";
        }
        return text.substring(0, maxChars - 3) + "...";
    }
    
    /**
     * Creates an empty recipe card placeholder.
     */
    private VBox createEmptyRecipeCard() {
        VBox card = new VBox(0);
        // Remove fixed width to allow grid to control card width
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 12px;");
        return card;
    }
    
    /**
     * Phase 3: Opens recipe detail view when clicking on a recommendation card.
     */
    private void openRecipeDetail(Recipe recipe) {
        if (recipe == null) {
            logger.warn("Cannot open recipe detail: recipe is null");
            sonner.show("Error", "Recipe information is not available.", Sonner.Type.ERROR);
            return;
        }
        
        if (recipeDetailViewModel == null) {
            logger.error("Cannot open recipe detail: RecipeDetailViewModel is null");
            sonner.show("Error", "Recipe detail view is not available. Please try again later.", Sonner.Type.ERROR);
            return;
        }
        
        if (viewManagerModel == null) {
            logger.error("Cannot open recipe detail: ViewManagerModel is null");
            sonner.show("Error", "Navigation service is not available. Please try again later.", Sonner.Type.ERROR);
            return;
        }
        
        try {
            // Set recipe in view model
            recipeDetailViewModel.setRecipe(recipe);
            
            // Switch to recipe detail view
            viewManagerModel.setActiveView(ViewManager.RECIPE_DETAIL_VIEW);
            
            logger.debug("Successfully opened recipe detail for: {}", recipe.getName());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid recipe data when opening recipe detail: {}", recipe.getName(), e);
            sonner.show("Error", "Invalid recipe data. Please try selecting another recipe.", Sonner.Type.ERROR);
        } catch (IllegalStateException e) {
            logger.error("View manager state error when opening recipe detail: {}", recipe.getName(), e);
            sonner.show("Error", "Unable to navigate to recipe detail. Please try again.", Sonner.Type.ERROR);
        } catch (Exception e) {
            logger.error("Unexpected error when opening recipe detail for recipe: {}", recipe.getName(), e);
            sonner.show("Error", "An unexpected error occurred. Please try again later.", Sonner.Type.ERROR);
        }
    }

    private VBox createQuickActionsSection() {
        VBox container = new VBox();
        container.getStyleClass().add("card-panel"); // Added white background class
        container.setSpacing(16);

        Label title = new Label("Quick Actions");
        title.getStyleClass().add("section-title");

        GridPane grid = new GridPane();
        grid.setHgap(12); // Spacing 12px
        grid.setVgap(12);
        grid.setMaxWidth(Double.MAX_VALUE); // Use full width
        
        // Add Snack: Lime (#84cc16) -> Green (#22c55e)
        VBox btn1 = createQuickActionButton("Add Snack", "/svg/plus.svg", "#84cc16", "#22c55e");
        grid.add(btn1, 0, 0);
        GridPane.setHgrow(btn1, Priority.ALWAYS);
        
        // Log Water: Blue (#3b82f6) -> Cyan (#06b6d4)
        VBox btn2 = createQuickActionButton("Log Water", "/svg/raindrops.svg", "#3b82f6", "#06b6d4");
        grid.add(btn2, 1, 0);
        GridPane.setHgrow(btn2, Priority.ALWAYS);
        
        // New Recipe: Purple (#a855f7) -> Pink (#ec4899)
        VBox btn3 = createQuickActionButton("New Recipe", "/svg/add-book.svg", "#a855f7", "#ec4899");
        grid.add(btn3, 0, 1);
        GridPane.setHgrow(btn3, Priority.ALWAYS);
        
        // Weekly Plan: Orange (#f97316) -> Amber (#f59e0b)
        VBox btn4 = createQuickActionButton("Weekly Plan", "/svg/calendar.svg", "#f97316", "#f59e0b");
        grid.add(btn4, 1, 1);
        GridPane.setHgrow(btn4, Priority.ALWAYS);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        col1.setHgrow(Priority.ALWAYS);
        col1.setMinWidth(120); // Set minimum width for cells
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        col2.setHgrow(Priority.ALWAYS);
        col2.setMinWidth(120); // Set minimum width for cells
        grid.getColumnConstraints().addAll(col1, col2);
        
        container.getChildren().addAll(title, grid);
        return container;
    }
    
    private VBox createQuickActionButton(String text, String iconPath, String startColor, String endColor) {
        VBox btn = new VBox(10);
        btn.getStyleClass().add("quick-action-button"); 
        btn.setAlignment(Pos.CENTER);
        btn.setPrefHeight(100);
        btn.setMaxHeight(100);
        btn.setMaxWidth(Double.MAX_VALUE); // Allow button to expand to fill grid cell
        
        // Default Style: bg #fbfbfc, no shadow, no outline
        String defaultBtnStyle = "-fx-background-color: #fbfbfc; -fx-background-radius: 12px; -fx-effect: null; -fx-border-width: 0;";
        // Hover Style: bg #f7f8fa, outline #e5e7eb, shadow
        String hoverBtnStyle = "-fx-background-color: #f7f8fa; -fx-background-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 2, 0, 0, 1); -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 12px;";
        
        btn.setStyle(defaultBtnStyle);
        
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(44, 44); // Slightly reduced from 48 to make room for text
        iconBox.setMaxSize(44, 44);
        
        Rectangle iconBg = new Rectangle(44, 44); // Reduced from 48 to 44
        iconBg.setArcWidth(16);
        iconBg.setArcHeight(16);
        
        // Gradient Fill
        Stop[] stops = new Stop[] { new Stop(0, Color.web(startColor)), new Stop(1, Color.web(endColor)) };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        iconBg.setFill(gradient);
        // Update DropShadow to softer, spread out shadow if needed, or keep as is
        iconBg.setEffect(new javafx.scene.effect.DropShadow(2, 0, 1, Color.rgb(0,0,0,0.05)));
        
        Node icon = SvgIconLoader.loadIcon(iconPath, 22, Color.WHITE); // Reduced from 24 to 22
        if (icon != null) {
            icon.setStyle("-fx-stroke-width: 2px;"); // Ensure stroke width if supported or visually approximated
        } else {
            icon = new Label("?");
        }
        
        iconBox.getChildren().addAll(iconBg, icon);
        
        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("text-gray-700");
        textLabel.setStyle("-fx-font-size: 12px; -fx-alignment: center;"); // Font size 12px, center alignment
        textLabel.setWrapText(true); // Allow text wrapping
        textLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        textLabel.setAlignment(Pos.CENTER); // Center alignment for label
        textLabel.setMaxWidth(Double.MAX_VALUE); // Allow text to use available width
        
        btn.getChildren().addAll(iconBox, textLabel);
        
        // Hover Effects
        btn.setOnMouseEntered(e -> {
            btn.setStyle(hoverBtnStyle);
            iconBox.setScaleX(1.1);
            iconBox.setScaleY(1.1);
        });
        
        btn.setOnMouseExited(e -> {
            btn.setStyle(defaultBtnStyle);
            iconBox.setScaleX(1.0);
            iconBox.setScaleY(1.0);
        });
        
        btn.setOnMouseClicked(e -> handleQuickAction(text));
        
        return btn;
    }

    private void handleQuickAction(String text) {
        switch (text) {
            case "Add Snack":
                sonner.show("Snack logged", "We’ll add this to today’s nutrition summary.", Sonner.Type.INFO);
                break;
            case "Log Water":
                sonner.show("Hydration noted", "Another glass recorded. Keep it up!", Sonner.Type.SUCCESS);
                break;
            case "New Recipe":
                viewManagerModel.setActiveView(ViewManager.STORE_RECIPE_VIEW);
                break;
            case "Weekly Plan":
                viewManagerModel.setActiveView(ViewManager.SCHEDULE_VIEW);
                break;
            default:
                break;
        }
    }

    private VBox createMealCard(String mealType, String mealName, String iconPath, int calories, String time) {
        VBox card = new VBox(0);
        card.getStyleClass().add("meal-card"); 
        card.setStyle(null);
        
        // Remove fixed width to allow grid to control card width (matching recipe cards)
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPrefHeight(180);
        
        // Default style managed by CSS .meal-card, override for planned vs not planned
        
        boolean isPlanned = !mealName.equals("Not Planned") && mealName != null && !mealName.isEmpty();
        
        if (isPlanned) {
            Region imagePart = new Region();
            imagePart.setPrefHeight(100);
            imagePart.setStyle("-fx-background-color: #E5E7EB; -fx-background-radius: 12px 12px 0 0;");
            
            Label badge = new Label(mealType);
            badge.getStyleClass().add("text-lime-700");
            badge.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 4px; -fx-padding: 2 6; -fx-font-size: 10px; -fx-font-weight: bold;");
            StackPane.setAlignment(badge, Pos.TOP_LEFT);
            StackPane.setMargin(badge, new Insets(10));
            
            StackPane imageContainer = new StackPane(imagePart, badge);
            
            VBox content = new VBox(6);
            content.setPadding(new Insets(12));
            content.setPrefHeight(80); // Fixed height for content area
            content.setMinHeight(80);
            
            Label title = new Label(mealName);
            title.getStyleClass().add("text-gray-900");
            title.setStyle("-fx-font-weight: 600; -fx-font-size: 14px;");
            title.setWrapText(true);
            title.setMaxHeight(Double.MAX_VALUE);
            VBox.setVgrow(title, Priority.ALWAYS);
            
            HBox meta = new HBox(10);
            meta.setAlignment(Pos.CENTER_LEFT);
            
            // Fire Icon
            HBox calBox = new HBox(4);
            calBox.setAlignment(Pos.CENTER_LEFT);
            Node fireIcon = SvgIconLoader.loadIcon("/svg/fire-flame.svg", 12, Color.web("#ff6900"));
            Label calLabel = new Label(calories + " cal");
            calLabel.getStyleClass().add("text-gray-500");
            calLabel.setStyle("-fx-font-size: 11px;");
            if (fireIcon != null) calBox.getChildren().add(fireIcon);
            calBox.getChildren().add(calLabel);
            
            // Clock Icon
            HBox timeBox = new HBox(4);
            timeBox.setAlignment(Pos.CENTER_LEFT);
            Node clockIcon = SvgIconLoader.loadIcon("/svg/clock.svg", 12, Color.web("#2b7fff"));
            Label timeLabel = new Label(time);
            timeLabel.getStyleClass().add("text-gray-500");
            timeLabel.setStyle("-fx-font-size: 11px;");
            if (clockIcon != null) timeBox.getChildren().add(clockIcon);
            timeBox.getChildren().add(timeLabel);
            
            meta.getChildren().addAll(calBox, timeBox);
            
            // Use StackPane to position meta at bottom
            StackPane contentStack = new StackPane();
            contentStack.setPrefHeight(80);
            contentStack.setMinHeight(80);
            
            VBox titleContainer = new VBox();
            titleContainer.getChildren().add(title);
            VBox.setVgrow(titleContainer, Priority.ALWAYS);
            
            // Meta positioned at bottom
            VBox metaContainer = new VBox();
            VBox.setVgrow(metaContainer, Priority.ALWAYS);
            metaContainer.setAlignment(Pos.BOTTOM_LEFT);
            metaContainer.getChildren().add(meta);
            
            contentStack.getChildren().addAll(titleContainer, metaContainer);
            
            // Time label removed as per user request
            
            content.getChildren().addAll(contentStack);
            
            card.getChildren().addAll(imageContainer, content);
            
            // Hover Effect for Planned
            card.setOnMouseEntered(e -> {
                card.setStyle("-fx-border-color: #bbf452; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);");
            });
            card.setOnMouseExited(e -> {
                card.setStyle(null); // Reset to CSS default
            });
            
            card.setOnMouseClicked(e -> viewManagerModel.setActiveView(ViewManager.BROWSE_RECIPE_VIEW));

        } else {
            // Not Planned State - Center Alignment logic
            card.setAlignment(Pos.CENTER);
            card.setSpacing(10);
            
            // Create a flexible spacer to push content to center vertically
            Region topSpacer = new Region();
            VBox.setVgrow(topSpacer, Priority.ALWAYS);
            
            // Middle Content (Icon, Title, Time, Status)
            VBox centerBox = new VBox(10);
            centerBox.setAlignment(Pos.CENTER);
            
            // Icon Container for Hover Effect
            StackPane iconContainer = new StackPane();
            iconContainer.setMaxSize(40, 40);
            iconContainer.setPrefSize(40, 40);
            
            Rectangle iconBg = new Rectangle(40, 40);
            iconBg.setArcWidth(12);
            iconBg.setArcHeight(12);
            iconBg.setFill(Color.TRANSPARENT); // Default transparent
            
            Node mealIcon = SvgIconLoader.loadIcon(iconPath, 32, Color.web("#9CA3AF"));
            if (mealIcon == null) mealIcon = new Label("?");
            
            iconContainer.getChildren().addAll(iconBg, mealIcon);
            
            Label typeLabel = new Label(mealType);
            typeLabel.getStyleClass().add("text-gray-700");
            typeLabel.setStyle("-fx-font-weight: 500; -fx-font-size: 14px;");
            
            // Time label removed as per user request
            
            Label statusLabel = new Label("Not Planned");
            statusLabel.getStyleClass().add("text-gray-400");
            statusLabel.setStyle("-fx-font-size: 11px;");
            
            centerBox.getChildren().addAll(iconContainer, typeLabel, statusLabel);
            
            Region bottomSpacer = new Region();
            VBox.setVgrow(bottomSpacer, Priority.ALWAYS);
            
            // Bottom Button Container
            HBox bottomBox = new HBox();
            bottomBox.setAlignment(Pos.CENTER);
            bottomBox.setPadding(new Insets(0, 0, 15, 0)); // Padding from bottom edge
            
            Button addBtn = new Button();
            Node plusIcon = SvgIconLoader.loadIcon("/svg/plus-small.svg", 20, Color.web("#6B7280"));
            addBtn.setGraphic(plusIcon);
            addBtn.setPrefSize(32, 32);
            addBtn.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 16px; -fx-padding: 0; -fx-cursor: hand;");
            
            bottomBox.getChildren().add(addBtn);
            
            card.getChildren().addAll(topSpacer, centerBox, bottomSpacer, bottomBox);
            
            // Hover Effect for Not Planned - 개선된 인터랙션
            final MealType targetMealType = getMealTypeFromString(mealType);
            card.setOnMouseEntered(e -> {
                // Background Gradient
                Stop[] bgStops = new Stop[] { new Stop(0, Color.web("#f7fee7")), new Stop(1, Color.web("#f1fdf4")) };
                LinearGradient bgGradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, bgStops);
                card.setBackground(new Background(new BackgroundFill(bgGradient, new CornerRadii(12), Insets.EMPTY)));
                card.setBorder(new Border(new BorderStroke(Color.web("#bbf452"), BorderStrokeStyle.SOLID, new CornerRadii(12), new BorderWidths(1))));
                
                // Icon Background Gradient
                Stop[] iconStops = new Stop[] { new Stop(0, Color.web("#74ce00")), new Stop(1, Color.web("#00c947")) };
                LinearGradient iconGradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, iconStops);
                iconBg.setFill(iconGradient);
                
                // Icon White
                Node whiteIcon = SvgIconLoader.loadIcon(iconPath, 24, Color.WHITE); // Slightly smaller to fit
                if (whiteIcon != null && iconContainer.getChildren().size() > 1) {
                    iconContainer.getChildren().set(1, whiteIcon);
                }
                
                // Button Style
                addBtn.setStyle("-fx-background-color: #7ccf00; -fx-background-radius: 16px; -fx-padding: 0; -fx-cursor: hand;");
                Node whitePlus = SvgIconLoader.loadIcon("/svg/plus-small.svg", 20, Color.WHITE);
                addBtn.setGraphic(whitePlus);
                
                // 스케일 효과
                card.setScaleX(1.02);
                card.setScaleY(1.02);
                iconContainer.setScaleX(1.1);
                iconContainer.setScaleY(1.1);
            });
            
            card.setOnMouseExited(e -> {
                // Reset Styles
                card.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
                card.setBorder(new Border(new BorderStroke(Color.web("#f3f4f6"), BorderStrokeStyle.SOLID, new CornerRadii(12), new BorderWidths(1))));
                
                iconBg.setFill(Color.TRANSPARENT);
                Node originalIcon = SvgIconLoader.loadIcon(iconPath, 32, Color.web("#9CA3AF"));
                if (originalIcon != null && iconContainer.getChildren().size() > 1) {
                    iconContainer.getChildren().set(1, originalIcon);
                }
                
                addBtn.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 16px; -fx-padding: 0; -fx-cursor: hand;");
                addBtn.setGraphic(plusIcon); // Re-use original gray icon
                
                // 스케일 리셋
                card.setScaleX(1.0);
                card.setScaleY(1.0);
                iconContainer.setScaleX(1.0);
                iconContainer.setScaleY(1.0);
            });
            
            // 클릭 피드백 개선
            card.setOnMousePressed(e -> {
                card.setScaleX(0.98);
                card.setScaleY(0.98);
            });
            card.setOnMouseReleased(e -> {
                card.setScaleX(1.02);
                card.setScaleY(1.02);
            });
            
            // 빈 슬롯 클릭 시 레시피 선택 다이얼로그 열기
            card.setOnMouseClicked(e -> {
                if (addMealController == null || recipeRepository == null) {
                    sonner.show("Error", "Unable to add meal. Please try again later.", Sonner.Type.ERROR);
                    return;
                }
                
                // 레시피 선택 다이얼로그 열기
                javafx.stage.Stage stage = (javafx.stage.Stage) card.getScene().getWindow();
                SelectRecipeDialog selectDialog = new SelectRecipeDialog(stage, recipeRepository);
                selectDialog.setOnRecipeSelected(recipe -> {
                    if (recipe != null) {
                        // 레시피 선택 후 AddToMealPlanDialog 열기 (오늘 날짜와 해당 식사 타입으로 미리 설정)
                        AddToMealPlanDialog addDialog = new AddToMealPlanDialog(
                            stage, 
                            addMealController, 
                            String.valueOf(recipe.getRecipeId()), 
                            recipe.getName()
                        );
                        // 다이얼로그에서 날짜를 오늘로, 식사 타입을 해당 타입으로 설정
                        addDialog.setDefaultDate(LocalDate.now());
                        addDialog.setDefaultMealType(targetMealType);
                        addDialog.show();
                    }
                });
                selectDialog.show();
            });
        }

        return card;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null) {
            return; // Defensive check
        }
        
        String property = evt.getPropertyName();
        if ("currentUsername".equals(property) || "view".equals(property)) {
            Platform.runLater(this::updateWelcomeMessage);
            // Phase 3: Reload recommendations when user logs in
            if ("currentUsername".equals(property)) {
                Platform.runLater(this::loadRecommendations);
            }
        }
        
        // Phase 3 & 4: Handle recommendations
        if ("recommendations".equals(property)) {
            if (isAutoGenerating) {
                // Phase 4: Process recommendations for auto-generate
                Platform.runLater(this::processAutoGenerateRecommendations);
            } else {
                // Phase 3: Update recommended recipes display
                Platform.runLater(this::updateRecommendedRecipes);
            }
        }
        
        Platform.runLater(this::updateView);
    }

    private void updateView() {
        if (mealsGrid != null) {
            mealsGrid.getChildren().clear();
        }
        if (mealsContainer != null) {
            mealsContainer.getChildren().clear();
        }

        Schedule schedule = scheduleViewModel.getSchedule();
        LocalDate today = LocalDate.now();

        // Fetch today's meals
        Map<MealType, String> todaysMeals = null;
        if (schedule != null) {
            Map<LocalDate, Map<MealType, String>> allMeals = schedule.getAllMeals();
            todaysMeals = allMeals.get(today);
        }

        // Load all recipes once to avoid duplicate queries
        java.util.Map<String, Recipe> recipeCache = loadRecipesForMeals(todaysMeals);

        // Fetch meal data for each meal type
        MealData breakfast = fetchMealData(todaysMeals, MealType.BREAKFAST, recipeCache);
        MealData lunch = fetchMealData(todaysMeals, MealType.LUNCH, recipeCache);
        MealData dinner = fetchMealData(todaysMeals, MealType.DINNER, recipeCache);

        // Create meal cards and add to grid (3-column layout)
        if (mealsGrid != null) {
            VBox breakfastCard = createMealCard("Breakfast", breakfast.name, "/svg/mug-hot.svg", breakfast.calories, breakfast.time);
            VBox lunchCard = createMealCard("Lunch", lunch.name, "/svg/brightness.svg", lunch.calories, lunch.time);
            VBox dinnerCard = createMealCard("Dinner", dinner.name, "/svg/moon.svg", dinner.calories, dinner.time);
            
            mealsGrid.add(breakfastCard, 0, 0);
            mealsGrid.add(lunchCard, 1, 0);
            mealsGrid.add(dinnerCard, 2, 0);
            
            // Set cards to fill grid cells
            GridPane.setHgrow(breakfastCard, Priority.ALWAYS);
            GridPane.setHgrow(lunchCard, Priority.ALWAYS);
            GridPane.setHgrow(dinnerCard, Priority.ALWAYS);
        }

        // Calculate today's nutrition using cached recipes
        NutritionInfo todayNutrition = calculateTodayNutrition(recipeCache);
        int totalCalories = (int) todayNutrition.getCalories();
        
        // Get user's nutrition goals or use defaults
        NutritionGoals goals = getUserNutritionGoals();
        int dailyCalories = goals.getDailyCalories();
        int remainingCal = Math.max(0, dailyCalories - totalCalories);

        calorieValueLabel.setText(String.valueOf(totalCalories));
        remainingCaloriesLabel.setText("Remaining\n" + remainingCal + " cal");

        // Update circular progress
        updateCircularProgress(totalCalories, dailyCalories);

        // Update nutrient bars
        updateNutrientBars(todayNutrition, goals);
    }

    /**
     * Loads all recipes for today's meals once to avoid duplicate queries.
     * @param todaysMeals map of meal types to recipe IDs
     * @return map of recipe IDs to Recipe objects
     */
    private java.util.Map<String, Recipe> loadRecipesForMeals(Map<MealType, String> todaysMeals) {
        java.util.Map<String, Recipe> cache = new java.util.HashMap<>();

        if (todaysMeals != null) {
            for (String recipeId : todaysMeals.values()) {
                if (!cache.containsKey(recipeId)) {
                    Recipe recipe = getRecipeById(recipeId);
                    if (recipe != null) {
                        cache.put(recipeId, recipe);
                    }
                }
            }
        }

        return cache;
    }

    /**
     * Fetches meal data for a specific meal type using cached recipes.
     * @param todaysMeals map of today's meals
     * @param mealType the type of meal to fetch
     * @param recipeCache pre-loaded cache of recipes
     * @return MealData with name, calories, and time
     */
    private MealData fetchMealData(Map<MealType, String> todaysMeals, MealType mealType,
                                   java.util.Map<String, Recipe> recipeCache) {
        if (todaysMeals == null || !todaysMeals.containsKey(mealType)) {
            return new MealData("Not Planned", 0, "");
        }

        String recipeId = todaysMeals.get(mealType);
        Recipe recipe = recipeCache.get(recipeId);

        if (recipe == null) {
            // Fallback to recipe ID if recipe not found
            return new MealData(recipeId, 0, "");
        }

        String name = recipe.getName();
        int calories = recipe.getNutritionInfo() != null
            ? (int) recipe.getNutritionInfo().getCalories() : 0;
        String time = recipe.getCookTimeMinutes() != null
            ? recipe.getCookTimeMinutes() + " min" : "";

        return new MealData(name, calories, time);
    }

    /**
     * Simple data holder for meal information.
     */
    private static class MealData {
        final String name;
        final int calories;
        final String time;

        MealData(String name, int calories, String time) {
            this.name = name;
            this.calories = calories;
            this.time = time;
        }
    }

    /**
     * Updates the circular progress indicator for calorie consumption.
     */
    private void updateCircularProgress(int totalCalories, int dailyCalories) {
        if (circularProgressPane == null || circularProgressPane.getChildren().isEmpty()) {
            return;
        }

        Node possibleGroup = circularProgressPane.getChildren().get(0);
        if (!(possibleGroup instanceof javafx.scene.Group)) {
            return;
        }

        javafx.scene.Group group = (javafx.scene.Group) possibleGroup;
        for (Node node : group.getChildren()) {
            if (node instanceof Arc) {
                Arc progressArc = (Arc) node;
                double progress = Math.min(1.0, (double) totalCalories / dailyCalories);
                double angle = progress * 360.0;
                progressArc.setLength(-angle);
                break;
            }
        }
        
        // Update total label in the center
        if (circularProgressPane.getChildren().size() > 1) {
            Node textBox = circularProgressPane.getChildren().get(1);
            if (textBox instanceof VBox) {
                VBox vbox = (VBox) textBox;
                if (vbox.getChildren().size() > 1) {
                    Label totalLabel = (Label) vbox.getChildren().get(1);
                    totalLabel.setText("of " + dailyCalories + " cal");
                }
            }
        }
    }

    /**
     * Updates the nutrient progress bars (protein, carbs, fat).
     */
    private void updateNutrientBars(NutritionInfo nutrition, NutritionGoals goals) {
        if (proteinBar != null && proteinValLabel != null) {
            double protein = nutrition.getProtein();
            double proteinGoal = goals.getDailyProtein();
            proteinBar.setProgress(Math.min(1.0, protein / proteinGoal));
            proteinValLabel.setText(String.format("%.0f / %.0fg", protein, proteinGoal));
        }
        if (carbsBar != null && carbsValLabel != null) {
            double carbs = nutrition.getCarbs();
            double carbsGoal = goals.getDailyCarbs();
            carbsBar.setProgress(Math.min(1.0, carbs / carbsGoal));
            carbsValLabel.setText(String.format("%.0f / %.0fg", carbs, carbsGoal));
        }
        if (fatBar != null && fatValLabel != null) {
            double fat = nutrition.getFat();
            double fatGoal = goals.getDailyFat();
            fatBar.setProgress(Math.min(1.0, fat / fatGoal));
            fatValLabel.setText(String.format("%.0f / %.0fg", fat, fatGoal));
        }
    }
    
    /**
     * Gets the current user's nutrition goals, or returns default goals if not available.
     * @return NutritionGoals object
     */
    private NutritionGoals getUserNutritionGoals() {
        com.mealplanner.entity.User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getNutritionGoals() != null) {
            return currentUser.getNutritionGoals();
        }
        // Fallback to default goals
        return NutritionGoals.createDefault();
    }

    /**
     * Retrieves a recipe by its ID from the repository.
     * @param recipeId the recipe identifier
     * @return Recipe object if found, null otherwise
     */
    private Recipe getRecipeById(String recipeId) {
        try {
            return recipeRepository.findById(recipeId).orElse(null);
        } catch (Exception e) {
            logger.error("Failed to load recipe: {}", recipeId, e);
            return null;
        }
    }
    
    /**
     * 문자열로부터 MealType을 반환합니다.
     * @param mealTypeString "Breakfast", "Lunch", "Dinner" 등의 문자열
     * @return 해당하는 MealType, 없으면 BREAKFAST
     */
    private MealType getMealTypeFromString(String mealTypeString) {
        if (mealTypeString == null) {
            return MealType.BREAKFAST;
        }
        String upper = mealTypeString.toUpperCase();
        try {
            return MealType.valueOf(upper);
        } catch (IllegalArgumentException e) {
            // 문자열이 enum 값과 정확히 일치하지 않는 경우
            if (upper.contains("BREAKFAST") || upper.contains("아침")) {
                return MealType.BREAKFAST;
            } else if (upper.contains("LUNCH") || upper.contains("점심")) {
                return MealType.LUNCH;
            } else if (upper.contains("DINNER") || upper.contains("저녁")) {
                return MealType.DINNER;
            }
            return MealType.BREAKFAST; // 기본값
        }
    }

    /**
     * Calculates the total nutrition information using cached recipes.
     * @param recipeCache map of recipe IDs to Recipe objects
     * @return aggregated nutrition info
     */
    private NutritionInfo calculateTodayNutrition(java.util.Map<String, Recipe> recipeCache) {
        int totalCalories = 0;
        double totalProtein = 0;
        double totalCarbs = 0;
        double totalFat = 0;

        for (Recipe recipe : recipeCache.values()) {
            if (recipe != null && recipe.getNutritionInfo() != null) {
                NutritionInfo info = recipe.getNutritionInfo();
                totalCalories += info.getCalories();
                totalProtein += info.getProtein();
                totalCarbs += info.getCarbs();
                totalFat += info.getFat();
            }
        }

        return new NutritionInfo(totalCalories, totalProtein, totalCarbs, totalFat);
    }
    
    /**
     * Updates the welcome message with the current logged-in user's name.
     * If no user is logged in, displays a generic welcome message.
     */
    private void updateWelcomeMessage() {
        if (welcomeLabel == null) {
            return; // Defensive check: label not initialized yet
        }
        
        String username = SessionManager.getInstance().getCurrentUsername();
        if (username != null && !username.trim().isEmpty()) {
            welcomeLabel.setText("Welcome back, " + username + "!");
        } else {
            welcomeLabel.setText("Welcome back!");
        }
    }
    
    /**
     * Phase 4: Handles auto-generate button click.
     * Automatically generates meal plan for today using recommended recipes.
     * 
     * Flow:
     * 1. Check if user is logged in
     * 2. Check if auto-generate is already in progress (prevent duplicate requests)
     * 3. Check if meals already exist for today
     * 4. Request recommendations for the user
     * 5. When recommendations are received (via propertyChange), assign them to Breakfast, Lunch, Dinner
     */
    private void handleAutoGenerate() {
        // Prevent duplicate requests
        if (isAutoGenerating) {
            sonner.show("Already generating", "Please wait for the current meal plan generation to complete.", Sonner.Type.INFO);
            return;
        }
        
        String userId = SessionManager.getInstance().getCurrentUserId();
        if (userId == null || userId.trim().isEmpty()) {
            sonner.show("Please log in", "You need to be logged in to auto-generate meals.", Sonner.Type.ERROR);
            return;
        }
        
        if (recommendationsController == null) {
            sonner.show("Feature unavailable", "Recommendations service is not available.", Sonner.Type.ERROR);
            return;
        }
        
        if (addMealController == null) {
            sonner.show("Feature unavailable", "Meal planning service is not available.", Sonner.Type.ERROR);
            return;
        }
        
        if (recommendationsViewModel == null) {
            sonner.show("Feature unavailable", "Recommendations view model is not available.", Sonner.Type.ERROR);
            return;
        }
        
        // Get today's date
        LocalDate today = LocalDate.now();
        
        // Check if meals already exist for today
        Schedule schedule = scheduleViewModel.getSchedule();
        if (schedule != null) {
            Map<MealType, String> todaysMeals = schedule.getAllMeals().get(today);
            if (todaysMeals != null && !todaysMeals.isEmpty()) {
                // Ask user if they want to replace existing meals
                // For now, we'll skip if meals exist (can be enhanced later with confirmation dialog)
                sonner.show("Meals already planned", "You already have meals planned for today. Please remove them first or use the schedule view to update.", Sonner.Type.INFO);
                return;
            }
        }
        
        // Set flag to indicate we're auto-generating (prevents duplicate requests)
        isAutoGenerating = true;
        
        // Show loading message
        sonner.show("Auto-generating...", "We're creating your meal plan for today.", Sonner.Type.INFO);
        
        // Request recommendations
        // The recommendations will be received via propertyChange("recommendations")
        try {
            recommendationsController.execute(userId);
        } catch (Exception e) {
            // Reset flag on error
            isAutoGenerating = false;
            logger.error("Failed to request recommendations for auto-generate: {}", e.getMessage());
            sonner.show("Error", "Failed to request recommendations. Please try again.", Sonner.Type.ERROR);
        }
    }
    
    /**
     * Phase 4: Processes recommendations received for auto-generate.
     * Assigns the first 3 recommendations to Breakfast, Lunch, and Dinner.
     * Called when RecipeBrowseViewModel fires "recommendations" property change.
     */
    private void processAutoGenerateRecommendations() {
        if (!isAutoGenerating) {
            return; // Not in auto-generate mode
        }
        
        isAutoGenerating = false; // Reset flag
        
        if (recommendationsViewModel == null) {
            sonner.show("Error", "Failed to receive recommendations.", Sonner.Type.ERROR);
            return;
        }
        
        List<Recipe> recommendations = recommendationsViewModel.getRecommendations();
        if (recommendations == null || recommendations.isEmpty()) {
            sonner.show("No recommendations", "We couldn't find any recommendations for you. Please try again later.", Sonner.Type.INFO);
            return;
        }
        
        // Get today's date
        LocalDate today = LocalDate.now();
        
        // Assign recommendations to meals
        // First recommendation -> Breakfast
        // Second recommendation -> Lunch
        // Third recommendation -> Dinner
        MealType[] mealTypes = {MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER};
        int mealsAdded = 0;
        int mealsFailed = 0;
        StringBuilder failedMeals = new StringBuilder();
        
        for (int i = 0; i < Math.min(3, recommendations.size()); i++) {
            Recipe recipe = recommendations.get(i);
            if (recipe != null && recipe.getRecipeId() != null && !recipe.getRecipeId().trim().isEmpty()) {
                try {
                    String dateString = today.toString();
                    String mealTypeString = mealTypes[i].name();
                    String recipeId = recipe.getRecipeId();
                    
                    addMealController.execute(dateString, mealTypeString, recipeId);
                    mealsAdded++;
                } catch (IllegalArgumentException e) {
                    // Invalid date or meal type format
                    mealsFailed++;
                    failedMeals.append(mealTypes[i].getDisplayName()).append(" ");
                    logger.error("Invalid input for meal {} in auto-generate: {}", mealTypes[i], e.getMessage());
                } catch (Exception e) {
                    // Other exceptions (e.g., schedule conflicts, data access errors)
                    mealsFailed++;
                    failedMeals.append(mealTypes[i].getDisplayName()).append(" ");
                    logger.error("Failed to add meal {} for auto-generate: {}", mealTypes[i], e.getMessage(), e);
                }
            } else {
                // Recipe ID is null or empty
                mealsFailed++;
                failedMeals.append(mealTypes[i].getDisplayName()).append(" ");
                logger.warn("Skipping meal {} in auto-generate: recipe ID is null or empty", mealTypes[i]);
            }
        }
        
        // Provide feedback based on results
        if (mealsAdded > 0) {
            if (mealsFailed > 0) {
                sonner.show("Partially completed", 
                    String.format("Added %d meal(s) successfully. Failed to add %d meal(s): %s", 
                        mealsAdded, mealsFailed, failedMeals.toString().trim()), 
                    Sonner.Type.SUCCESS);
            } else {
                sonner.show("Meal plan created!", 
                    String.format("Successfully added %d meal(s) to your schedule for today.", mealsAdded), 
                    Sonner.Type.SUCCESS);
            }
            // Refresh the view to show new meals
            updateView();
        } else {
            sonner.show("Failed to create meal plan", 
                String.format("Could not add any meals to your schedule. %d meal(s) failed: %s", 
                    mealsFailed, failedMeals.toString().trim()), 
                Sonner.Type.ERROR);
        }
    }
    
    /**
     * Clean up resources and remove property change listeners to prevent memory leaks.
     * Should be called when this view is no longer needed.
     */
    public void dispose() {
        if (scheduleViewModel != null) {
            scheduleViewModel.removePropertyChangeListener(this);
        }
        if (viewManagerModel != null) {
            viewManagerModel.removePropertyChangeListener(this);
        }
        if (recommendationsViewModel != null) {
            recommendationsViewModel.removePropertyChangeListener(this);
        }
        logger.debug("DashboardView disposed - listeners removed");
    }
}
