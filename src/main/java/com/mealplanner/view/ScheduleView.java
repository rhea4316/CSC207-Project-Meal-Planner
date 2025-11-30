package com.mealplanner.view;

import com.mealplanner.entity.MealType;
import com.mealplanner.entity.NutritionInfo;
import com.mealplanner.entity.Recipe;
import com.mealplanner.entity.Schedule;
import com.mealplanner.exception.DataAccessException;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.AddMealController;
import com.mealplanner.interface_adapter.controller.GetRecommendationsController;
import com.mealplanner.interface_adapter.controller.ViewScheduleController;
import com.mealplanner.interface_adapter.view_model.RecipeBrowseViewModel;
import com.mealplanner.interface_adapter.view_model.ScheduleViewModel;
import com.mealplanner.repository.RecipeRepository;
import com.mealplanner.app.SessionManager;
import com.mealplanner.view.component.StyledTooltip;
import com.mealplanner.view.component.Sonner;
import com.mealplanner.view.util.SvgIconLoader;
import com.mealplanner.util.ImageCacheManager;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleView extends BorderPane implements PropertyChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleView.class);
    
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
        logger.debug("ScheduleView disposed - listeners removed");
    }

    private final ScheduleViewModel scheduleViewModel;
    private final ViewScheduleController controller;
    private final ViewManagerModel viewManagerModel;
    private final RecipeRepository recipeRepository;  // PHASE 2: Added for real data integration
    /**
     * Controller for getting recipe recommendations.
     * Phase 5: Used for auto-fill functionality to get recommended recipes.
     */
    private final GetRecommendationsController recommendationsController;
    /**
     * ViewModel for receiving recipe recommendations.
     * Phase 5: Used in auto-fill to get recommended recipes.
     */
    private final RecipeBrowseViewModel recommendationsViewModel;
    private final AddMealController addMealController;  // PHASE 6: Added for Copy Last Week functionality
    private final ImageCacheManager imageCache = ImageCacheManager.getInstance();
    
    /**
     * Flag to track if we're waiting for recommendations for auto-fill.
     * Phase 5: Used to distinguish between user-initiated recommendation requests
     * and auto-fill requests.
     */
    private boolean isAutoFilling = false;

    // UI Components
    private Label dateRangeLabel;
    private GridPane gridPane;
    private MealSlotPanel[][] mealSlots; // [row][col] -> row=meal, col=day
    private ProgressBar mealsPlannedProgress;
    private Label mealsPlannedLabel;
    private Label calValueLabel;
    private Label calAvgLabel;
    private Button copyBtn;
    private ProgressIndicator copyingIndicator;

    // State
    private LocalDate currentWeekStart;

    /**
     * Constructor for ScheduleView.
     * Phase 5: GetRecommendationsController and RecipeBrowseViewModel are injected for auto-fill functionality.
     * Phase 6: AddMealController is injected for Copy Last Week functionality.
     * 
     * @param scheduleViewModel The schedule view model for meal data
     * @param controller The view schedule controller
     * @param viewManagerModel The view manager model for navigation
     * @param recipeRepository The recipe repository for recipe data
     * @param recommendationsController The controller for recommendations (Phase 5: auto-fill)
     * @param recommendationsViewModel The view model for receiving recommendations (Phase 5: auto-fill)
     * @param addMealController The controller for adding meals to schedule (Phase 6: Copy Last Week)
     */
    public ScheduleView(ScheduleViewModel scheduleViewModel, ViewScheduleController controller, ViewManagerModel viewManagerModel, RecipeRepository recipeRepository, GetRecommendationsController recommendationsController, RecipeBrowseViewModel recommendationsViewModel, AddMealController addMealController) {
        this.scheduleViewModel = scheduleViewModel;
        this.scheduleViewModel.addPropertyChangeListener(this);
        this.controller = controller;
        this.viewManagerModel = viewManagerModel;
        this.viewManagerModel.addPropertyChangeListener(this);
        this.recipeRepository = recipeRepository;  // PHASE 2: Injected repository
        this.recommendationsController = recommendationsController;  // Phase 5: Injected for auto-fill
        this.recommendationsViewModel = recommendationsViewModel;  // Phase 5: Injected for auto-fill
        this.addMealController = addMealController;  // PHASE 6: Injected for Copy Last Week functionality
        
        // Phase 5: Listen to recommendations changes for auto-fill
        if (recommendationsViewModel != null) {
            recommendationsViewModel.addPropertyChangeListener(this);
        }

        this.currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // Initial Load - Schedule will be loaded automatically when currentUsername changes in propertyChange
        // No schedule is loaded if user is not logged in
        // requestScheduleForActiveUser() is called from propertyChange listener

        // Root Styles
        getStyleClass().add("root");
        setBackground(new Background(new BackgroundFill(Color.web("#f7f8f9"), CornerRadii.EMPTY, Insets.EMPTY)));
        setPadding(new Insets(30, 40, 30, 40));

        // Main Layout
        VBox mainContainer = new VBox(24);
        
        mainContainer.getChildren().add(createHeader());
        mainContainer.getChildren().add(createStatsSection());
        
        // Calendar Grid wrapped in ScrollPane if needed, but usually fits
        ScrollPane scrollPane = new ScrollPane(createGrid());
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true); // Allow full height
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        // Increase scroll speed
        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() != 0) {
                double delta = event.getDeltaY() * 3.0;
                double height = scrollPane.getContent().getBoundsInLocal().getHeight();
                double vHeight = scrollPane.getViewportBounds().getHeight();
                
                double scrollableHeight = height - vHeight;
                if (scrollableHeight > 0) {
                    double vValueShift = -delta / scrollableHeight;
                    double nextVvalue = scrollPane.getVvalue() + vValueShift;
                    
                    if (nextVvalue >= 0 && nextVvalue <= 1.0 || (scrollPane.getVvalue() > 0 && scrollPane.getVvalue() < 1.0)) {
                        scrollPane.setVvalue(Math.min(Math.max(nextVvalue, 0), 1));
                        event.consume();
                    }
                }
            }
        });

        mainContainer.getChildren().add(scrollPane);
        setCenter(mainContainer);
        
        updateView(); 
    }

    private VBox createHeader() {
        VBox container = new VBox(16);

        // Top Row: Title and Controls
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        // Title Section
        VBox titleBox = new VBox(4);
        Label title = new Label("Weekly Plan");
        title.getStyleClass().add("section-title");
        title.setStyle("-fx-font-size: 24px;");
        
        dateRangeLabel = new Label();
        dateRangeLabel.getStyleClass().add("text-gray-500");
        dateRangeLabel.setStyle("-fx-font-size: 14px;");
        
        titleBox.getChildren().addAll(title, dateRangeLabel);
        
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        
        // Navigation Center - Segmented Control Style
        HBox navBox = new HBox(0);
        navBox.setAlignment(Pos.CENTER);
        navBox.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px;");
        
        Button prevBtn = new Button();
        prevBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-border-radius: 8px 0 0 8px; -fx-background-radius: 8px 0 0 8px; -fx-cursor: hand; -fx-padding: 8;");
        Node prevIcon = SvgIconLoader.loadIcon("/svg/angle-left.svg", 16, Color.web("#374151"));
        prevBtn.setGraphic(prevIcon);
        prevBtn.setOnAction(e -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            updateView();
        });
        
        Button thisWeekBtn = new Button("This Week");
        thisWeekBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #374151; -fx-border-color: #e5e7eb; -fx-border-width: 0 1px 0 0; -fx-border-radius: 0; -fx-cursor: hand; -fx-font-weight: 600; -fx-padding: 8 16;");
        thisWeekBtn.setOnAction(e -> {
            currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            updateView();
        });
        
        Button nextBtn = new Button();
        nextBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-border-radius: 0 8px 8px 0; -fx-background-radius: 0 8px 8px 0; -fx-cursor: hand; -fx-padding: 8;");
        Node nextIcon = SvgIconLoader.loadIcon("/svg/angle-right.svg", 16, Color.web("#374151"));
        nextBtn.setGraphic(nextIcon);
        nextBtn.setOnAction(e -> {
            currentWeekStart = currentWeekStart.plusWeeks(1);
            updateView();
        });
        
        navBox.getChildren().addAll(prevBtn, thisWeekBtn, nextBtn);
        
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        // Actions Right
        HBox actionBox = new HBox(12);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        
        copyBtn = createActionButton("Copy Last Week", "/svg/copy-alt.svg");
        copyBtn.setOnAction(e -> handleCopyLastWeek());  // PHASE 6: Copy Last Week functionality
        
        // Loading indicator for copy button
        copyingIndicator = new ProgressIndicator();
        copyingIndicator.setPrefSize(14, 14);
        copyingIndicator.setVisible(false);
        copyingIndicator.setManaged(false);
        Button autoFillBtn = createActionButton("Auto-fill", "/svg/sparkles.svg");
        autoFillBtn.setOnAction(e -> handleAutoFill());  // Phase 5: Auto-fill functionality
        
        Button newPlanBtn = new Button("New Plan");
        // Apply gradient background: #8be200 -> #14cd49 (top-left to bottom-right)
        Stop[] gradientStops = new Stop[] { new Stop(0, javafx.scene.paint.Color.web("#8be200")), new Stop(1, javafx.scene.paint.Color.web("#14cd49")) };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, gradientStops);
        newPlanBtn.setStyle("-fx-text-fill: white; -fx-background-radius: 8px; -fx-font-weight: 600; -fx-cursor: hand; -fx-padding: 8 16; -fx-background-color: null;");
        newPlanBtn.setBackground(new Background(new BackgroundFill(gradient, new CornerRadii(8), Insets.EMPTY)));
        newPlanBtn.setOnAction(e -> viewManagerModel.setActiveView(ViewManager.BROWSE_RECIPE_VIEW));
        
        HBox copyBtnContainer = new HBox(8);
        copyBtnContainer.setAlignment(Pos.CENTER);
        copyBtnContainer.getChildren().addAll(copyBtn, copyingIndicator);
        actionBox.getChildren().addAll(copyBtnContainer, autoFillBtn, newPlanBtn);
        
        topRow.getChildren().addAll(titleBox, spacer1, navBox, spacer2, actionBox);
        container.getChildren().add(topRow);
        
        return container;
    }

    private Button createActionButton(String text, String iconPath) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: white; -fx-text-fill: -fx-color-gray-700; -fx-border-color: -fx-color-gray-200; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-cursor: hand; -fx-padding: 8 12; -fx-font-size: 13px;");
        Node icon = SvgIconLoader.loadIcon(iconPath, 14, Color.web("#374151"));
        if (icon != null) {
            btn.setGraphic(icon);
            btn.setGraphicTextGap(8);
        }
        return btn;
    }

    private HBox createStatsSection() {
        HBox container = new HBox(24);
        
        // Meals Planned Card
        VBox plannedCard = new VBox(12);
        plannedCard.getStyleClass().add("card-panel");
        plannedCard.setPadding(new Insets(20));
        HBox.setHgrow(plannedCard, Priority.ALWAYS);
        
        Label plannedTitle = new Label("Meals Planned");
        plannedTitle.getStyleClass().add("text-gray-500");
        plannedTitle.setStyle("-fx-font-size: 14px;");
        
        mealsPlannedLabel = new Label("7/21");
        mealsPlannedLabel.getStyleClass().add("text-gray-900");
        mealsPlannedLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        mealsPlannedProgress = new ProgressBar(0.33);
        mealsPlannedProgress.setMaxWidth(Double.MAX_VALUE);
        mealsPlannedProgress.setStyle("-fx-accent: -fx-theme-primary; -fx-control-inner-background: -fx-color-gray-100; -fx-text-box-border: transparent;");
        mealsPlannedProgress.setPrefHeight(8);
        
        plannedCard.getChildren().addAll(plannedTitle, mealsPlannedLabel, mealsPlannedProgress);
        
        // Weekly Calories Card
        VBox calCard = new VBox(12);
        calCard.getStyleClass().add("card-panel");
        calCard.setPadding(new Insets(20));
        HBox.setHgrow(calCard, Priority.ALWAYS);
        
        Label calTitle = new Label("Weekly Calories");
        calTitle.getStyleClass().add("text-gray-500");
        calTitle.setStyle("-fx-font-size: 14px;");
        
        HBox calValueBox = new HBox(4);
        calValueBox.setAlignment(Pos.BASELINE_LEFT);
        calValueLabel = new Label("0");
        calValueLabel.getStyleClass().add("text-gray-900");
        calValueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label calUnit = new Label("cal");
        calUnit.getStyleClass().add("text-gray-500");
        calUnit.setStyle("-fx-font-size: 16px;");
        calValueBox.getChildren().addAll(calValueLabel, calUnit);
        
        calAvgLabel = new Label("Average: 0 cal/day");
        calAvgLabel.getStyleClass().add("text-gray-400");
        calAvgLabel.setStyle("-fx-font-size: 13px;");
        
        calCard.getChildren().addAll(calTitle, calValueBox, calAvgLabel);
        
        container.getChildren().addAll(plannedCard, calCard);
        return container;
    }

    private GridPane createGrid() {
        gridPane = new GridPane();
        gridPane.setHgap(12);
        gridPane.setVgap(12);
        gridPane.setAlignment(Pos.TOP_CENTER);
        
        mealSlots = new MealSlotPanel[3][7]; 

        // 1. Row Headers (Meal Types)
        gridPane.add(createRowHeader("Breakfast", "/svg/mug-hot.svg", "blue"), 0, 1);
        gridPane.add(createRowHeader("Lunch", "/svg/brightness.svg", "amber"), 0, 2);
        gridPane.add(createRowHeader("Dinner", "/svg/moon.svg", "rose"), 0, 3);
        
        // 2. Slots setup
        for (int c = 0; c < 7; c++) {
            // Slots
            for (int r = 0; r < 3; r++) {
                MealSlotPanel slot = new MealSlotPanel(r, c);
                mealSlots[r][c] = slot;
                gridPane.add(slot, c + 1, r + 1);
            }
        }
        
        // Column Constraints
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(100); // Fixed width for meal type labels
        labelCol.setPrefWidth(100);
        gridPane.getColumnConstraints().add(labelCol);

        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setMinWidth(140); // Minimum width for cards
            gridPane.getColumnConstraints().add(col);
        }
        
        // Row Constraints
        RowConstraints headerRow = new RowConstraints();
        headerRow.setMinHeight(80); // Day header height
        gridPane.getRowConstraints().add(headerRow);

        for (int i = 0; i < 3; i++) {
            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.ALWAYS);
            row.setMinHeight(160); // Card height
            gridPane.getRowConstraints().add(row);
        }

        return gridPane;
    }

    private VBox createRowHeader(String text, String iconPath, String colorTheme) {
        VBox panel = new VBox(8);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(10));
        
        // Apply specific theme styling
        String bgHex, textHex;
        if (colorTheme.equals("blue")) {
            bgHex = "#eff6ff"; textHex = "#2563eb"; // blue-50, blue-600
        } else if (colorTheme.equals("amber")) {
            bgHex = "#fffbeb"; textHex = "#d97706"; // amber-50, amber-600
        } else {
            bgHex = "#fff1f2"; textHex = "#e11d48"; // rose-50, rose-600
        }
        
        // Fill entire cell with background color, connect with grid visually
        panel.setStyle("-fx-background-color: " + bgHex + "; -fx-background-radius: 12px 0 0 12px;");
        panel.setPrefHeight(160);
        panel.setMinHeight(160);
        
        Node icon = SvgIconLoader.loadIcon(iconPath, 24, Color.web(textHex));
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + textHex + "; -fx-font-weight: 600; -fx-font-size: 14px;");
        
        if (icon != null) panel.getChildren().add(icon);
        panel.getChildren().add(label);
        
        return panel;
    }

    private VBox createDayHeader(String dayName, LocalDate date, boolean isActive) {
        VBox panel = new VBox(4);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(10));
        panel.setPrefHeight(70);

        if (isActive) {
            panel.setStyle("-fx-background-color: #f7fee7; -fx-background-radius: 12px; -fx-border-color: transparent;");
        } else {
            panel.setStyle("-fx-background-color: transparent;");
        }

        Label dayLabel = new Label(dayName);
        dayLabel.getStyleClass().add(isActive ? "text-lime-700" : "text-gray-500");
        dayLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 500;");

        Label dateLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dateLabel.getStyleClass().add(isActive ? "text-lime-700" : "text-gray-900");
        dateLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // PHASE 2: Calculate actual calories from scheduled meals
        Schedule schedule = scheduleViewModel.getSchedule();
        Map<MealType, String> mealsForDate = schedule != null ? schedule.getAllMeals().get(date) : null;
        int dailyCalories = calculateDailyCalories(date, mealsForDate);
        String calText = dailyCalories > 0 ? dailyCalories + " cal" : "-- cal";

        Label calLabel = new Label(calText);
        calLabel.getStyleClass().add(isActive ? "text-lime-600" : "text-gray-400");
        calLabel.setStyle("-fx-font-size: 11px;");

        panel.getChildren().addAll(dayLabel, dateLabel, calLabel);
        return panel;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null) {
            return; // Defensive check
        }
        
        String property = evt.getPropertyName();
        switch (property) {
            case "schedule":
                Platform.runLater(this::updateView);
                break;
            case "view":
                if (ViewManager.SCHEDULE_VIEW.equals(evt.getNewValue())) {
                    requestScheduleForActiveUser();
                    Platform.runLater(this::updateView);
                }
                break;
            case "currentUsername":
                // Phase 1: Load schedule for the logged-in user when username changes
                if (ViewManager.SCHEDULE_VIEW.equals(viewManagerModel.getActiveView())) {
                    requestScheduleForActiveUser();
                    Platform.runLater(this::updateView);
                }
                break;
            case "recommendations":
                // Phase 5: Handle recommendations for auto-fill
                if (isAutoFilling) {
                    Platform.runLater(this::processAutoFillRecommendations);
                }
                break;
            default:
                break;
        }
    }

    private void requestScheduleForActiveUser() {
        if (controller == null || viewManagerModel == null) {
            return;
        }
        String username = viewManagerModel.getCurrentUsername();
        if (username == null || username.isBlank()) {
            return;
        }
        controller.execute(username);
    }

    /**
     * PHASE 2: Load a recipe by ID from the repository.
     *
     * @param recipeId Recipe ID to load
     * @return Optional containing Recipe object if found, empty otherwise
     */
    private java.util.Optional<Recipe> loadRecipe(String recipeId) {
        if (recipeId == null || recipeId.isBlank()) {
            return java.util.Optional.empty();
        }

        try {
            return recipeRepository.findById(recipeId);
        } catch (DataAccessException e) {
            logger.error("Error loading recipe {}: {}", recipeId, e.getMessage(), e);
            return java.util.Optional.empty();
        }
    }

    /**
     * PHASE 2: Calculate total calories for a specific date.
     *
     * @param date Date to calculate calories for
     * @param mealsForDate Map of meal types to recipe IDs for that date
     * @return Total calories for the day, or 0 if no meals or calculation fails
     */
    private int calculateDailyCalories(LocalDate date, Map<MealType, String> mealsForDate) {
        if (mealsForDate == null || mealsForDate.isEmpty()) {
            return 0;
        }

        int[] totalCalories = {0}; // Use array to allow modification in lambda

        for (String recipeId : mealsForDate.values()) {
            loadRecipe(recipeId)
                .map(Recipe::getNutritionInfo)
                .map(NutritionInfo::getCalories)
                .ifPresent(calories -> totalCalories[0] += (int) calories);
        }

        return totalCalories[0];
    }

    private void updateDayHeaders() {
        // Clear existing headers (row 0, col > 0)
        gridPane.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) == 0 && GridPane.getColumnIndex(node) != null && GridPane.getColumnIndex(node) > 0);

        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        LocalDate today = LocalDate.now();
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = currentWeekStart.plusDays(i);
            boolean isActive = date.equals(today);
            gridPane.add(createDayHeader(dayNames[i], date, isActive), i + 1, 0);
        }
    }

    private void updateView() {
        updateDayHeaders();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d", Locale.ENGLISH);
        LocalDate weekEnd = currentWeekStart.plusDays(6);
        dateRangeLabel.setText(currentWeekStart.format(formatter) + " - " + weekEnd.format(formatter) + ", " + currentWeekStart.getYear());
        
        // Clear all slots first
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 7; c++) {
                mealSlots[r][c].clear();
            }
        }

        int filledCount = 0;
        int totalWeeklyCalories = 0;
        Schedule schedule = scheduleViewModel.getSchedule();
        if (schedule != null) {
            Map<LocalDate, Map<MealType, String>> allMeals = schedule.getAllMeals();

            for (int i = 0; i < 7; i++) {
                LocalDate date = currentWeekStart.plusDays(i);
                Map<MealType, String> mealsForDate = allMeals.get(date);

                // Calculate daily calories
                int dailyCalories = calculateDailyCalories(date, mealsForDate);
                totalWeeklyCalories += dailyCalories;

                if (mealsForDate != null && !mealsForDate.isEmpty()) {
                    // PHASE 2: Load actual Recipe objects and pass them to MealSlotPanel
                    final int dayIndex = i; // Make effectively final for lambda
                    if (mealsForDate.containsKey(MealType.BREAKFAST)) {
                        String recipeId = mealsForDate.get(MealType.BREAKFAST);
                        loadRecipe(recipeId).ifPresentOrElse(
                            recipe -> mealSlots[0][dayIndex].setMeal(recipe),
                            () -> mealSlots[0][dayIndex].setMeal(recipeId, "-- cal")
                        );
                        filledCount++;
                    }
                    if (mealsForDate.containsKey(MealType.LUNCH)) {
                        String recipeId = mealsForDate.get(MealType.LUNCH);
                        loadRecipe(recipeId).ifPresentOrElse(
                            recipe -> mealSlots[1][dayIndex].setMeal(recipe),
                            () -> mealSlots[1][dayIndex].setMeal(recipeId, "-- cal")
                        );
                        filledCount++;
                    }
                    if (mealsForDate.containsKey(MealType.DINNER)) {
                        String recipeId = mealsForDate.get(MealType.DINNER);
                        loadRecipe(recipeId).ifPresentOrElse(
                            recipe -> mealSlots[2][dayIndex].setMeal(recipe),
                            () -> mealSlots[2][dayIndex].setMeal(recipeId, "-- cal")
                        );
                        filledCount++;
                    }
                }
            }
        }
        
        // Update Stats
        mealsPlannedLabel.setText(filledCount + "/21");
        mealsPlannedProgress.setProgress(filledCount / 21.0);
        
        // Update Weekly Calories
        calValueLabel.setText(String.valueOf(totalWeeklyCalories));
        int avgCalories = filledCount > 0 ? totalWeeklyCalories / 7 : 0;
        calAvgLabel.setText("Average: " + avgCalories + " cal/day");
    }

    private class MealSlotPanel extends VBox {
        @SuppressWarnings("unused")
        private final int row; 
        @SuppressWarnings("unused")
        private final int col; 
        
        private boolean isFilled = false;
        private Tooltip currentTooltip;

        public MealSlotPanel(int row, int col) {
            this.row = row;
            this.col = col;
            
            setAlignment(Pos.CENTER);
            setCursor(Cursor.HAND);
            setMinHeight(140);
            setPrefHeight(140);
            
            setupEmptyState();
            
            setOnMouseClicked(e -> handleSlotClick());
        }
        
        private void setupEmptyState() {
            getChildren().clear();
            
            // Very subtle border - almost invisible, only appears on hover
            setStyle("-fx-background-color: transparent; -fx-border-color: #f3f4f6; -fx-border-width: 1px; -fx-border-style: dashed; -fx-border-radius: 12px;");
            
            // Add hover effect to show border more clearly
            setOnMouseEntered(e -> {
                setStyle("-fx-background-color: #fafafa; -fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-border-style: dashed; -fx-border-radius: 12px;");
            });
            setOnMouseExited(e -> {
                setStyle("-fx-background-color: transparent; -fx-border-color: #f3f4f6; -fx-border-width: 1px; -fx-border-style: dashed; -fx-border-radius: 12px;");
            });
            
            if (currentTooltip != null) {
                Tooltip.uninstall(this, currentTooltip);
                currentTooltip = null;
            }
            
            // Subtle Circle with Plus
            StackPane circle = new StackPane();
            circle.setPrefSize(40, 40);
            circle.setMaxSize(40, 40);
            circle.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-border-style: dashed; -fx-border-radius: 50%; -fx-background-color: transparent;");
            
            Node plusIcon = SvgIconLoader.loadIcon("/svg/plus.svg", 16, Color.web("#9ca3af"));
            if (plusIcon != null) circle.getChildren().add(plusIcon);
            else circle.getChildren().add(new Label("+"));
            
            getChildren().add(circle);
        }

        /**
         * PHASE 2: Set meal using Recipe object with real nutritional data.
         *
         * @param recipe Recipe object containing name, calories, and nutrition info
         */
        public void setMeal(Recipe recipe) {
            if (recipe == null) {
                clear();
                return;
            }

            String recipeName = recipe.getName();
            String calories = "-- cal";
            if (recipe.getNutritionInfo() != null) {
                int cal = (int) recipe.getNutritionInfo().getCalories();
                calories = cal > 0 ? cal + " cal" : "-- cal";
            }

            setMeal(recipeName, calories, recipe.getImageUrl());
        }

        /**
         * Set meal using String parameters (legacy method, kept for backward compatibility).
         *
         * @param recipeName Name of the recipe
         * @param calories Calorie string (e.g., "320 cal")
         */
        public void setMeal(String recipeName, String calories) {
            setMeal(recipeName, calories, null);
        }
        
        /**
         * Set meal using String parameters with optional image URL.
         *
         * @param recipeName Name of the recipe
         * @param calories Calorie string (e.g., "320 cal")
         * @param imageUrl Optional image URL for the recipe
         */
        public void setMeal(String recipeName, String calories, String imageUrl) {
            this.isFilled = true;
            getChildren().clear();
            
            // Remove hover effects when filled
            setOnMouseEntered(null);
            setOnMouseExited(null);

            // Filled Card Style - Modern card design with rounded corners
            setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 6, 0, 0, 2); -fx-border-width: 0;");

            // Tooltip
            if (currentTooltip != null) {
                Tooltip.uninstall(this, currentTooltip);
            }
            currentTooltip = new StyledTooltip(recipeName + "\n" + calories);
            Tooltip.install(this, currentTooltip);

            // Image section - load actual image if available, otherwise use placeholder
            // Image takes up most of the card (4:3 or 16:9 ratio)
            Node imageNode;
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                Image image = imageCache.getImage(imageUrl);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(200);
                imageView.setFitHeight(100); // Increased height for better image prominence
                imageView.setPreserveRatio(false); // Fill the entire area without preserving ratio
                imageView.setSmooth(true);
                imageView.setCache(true);
                imageView.setStyle("-fx-background-color: #e5e7eb;");
                // Clip image to rounded top corners only
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(200, 100);
                clip.setArcWidth(8);
                clip.setArcHeight(8);
                imageView.setClip(clip);
                imageNode = imageView;
            } else {
                // Placeholder when no image available
                Region imagePlaceholder = new Region();
                imagePlaceholder.setPrefHeight(100);
                imagePlaceholder.setMinHeight(100);
                imagePlaceholder.setStyle("-fx-background-color: #e5e7eb;");
                // Clip placeholder to rounded top corners only
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(200, 100);
                clip.setArcWidth(8);
                clip.setArcHeight(8);
                imagePlaceholder.setClip(clip);
                imageNode = imagePlaceholder;
            }

            // Content section - Compact text at bottom
            VBox filledContent = new VBox(4);
            filledContent.setPadding(new Insets(8, 10, 10, 10));
            filledContent.setAlignment(Pos.CENTER_LEFT);
            filledContent.setMaxWidth(Double.MAX_VALUE);

            Label contentLabel = new Label(recipeName);
            contentLabel.getStyleClass().add("text-gray-900");
            contentLabel.setStyle("-fx-font-weight: 700; -fx-font-size: 13px;");
            contentLabel.setWrapText(true);
            contentLabel.setMaxWidth(Double.MAX_VALUE);

            Label calLabel = new Label(calories);
            calLabel.getStyleClass().add("text-gray-500");
            calLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 400;");

            filledContent.getChildren().addAll(contentLabel, calLabel);

            // Main container - clip to rounded corners for overflow
            VBox cardContainer = new VBox(0);
            cardContainer.getChildren().addAll(imageNode, filledContent);
            cardContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8px;");
            
            // Clip entire card to rounded corners to prevent overflow
            javafx.scene.shape.Rectangle cardClip = new javafx.scene.shape.Rectangle();
            cardClip.widthProperty().bind(widthProperty());
            cardClip.heightProperty().bind(heightProperty());
            cardClip.setArcWidth(8);
            cardClip.setArcHeight(8);
            cardContainer.setClip(cardClip);

            getChildren().add(cardContainer);
        }

        public void clear() {
            this.isFilled = false;
            setupEmptyState();
        }

        private void handleSlotClick() {
            if (isFilled) {
                viewManagerModel.setActiveView(ViewManager.RECIPE_DETAIL_VIEW);
            } else {
                viewManagerModel.setActiveView(ViewManager.BROWSE_RECIPE_VIEW);
            }
        }
    }

    /**
     * PHASE 6: Copy Last Week functionality
     * Copies meals from the previous week to the current week.
     * Only copies to empty slots (does not overwrite existing meals).
     */
    private void handleCopyLastWeek() {
        // Check if AddMealController is available
        if (addMealController == null) {
            logger.warn("Cannot copy last week: AddMealController is not available");
            showCopyFeedback("Error", "Meal planning service is not available.", Sonner.Type.ERROR);
            return;
        }

        // Check if user is logged in
        String userId = SessionManager.getInstance().getCurrentUserId();
        if (userId == null || userId.trim().isEmpty()) {
            logger.warn("Cannot copy last week: User not logged in");
            showCopyFeedback("Please log in", "You need to be logged in to copy last week's meals.", Sonner.Type.ERROR);
            return;
        }

        // Get current schedule
        Schedule currentSchedule = scheduleViewModel.getSchedule();
        if (currentSchedule == null) {
            logger.warn("Cannot copy last week: No schedule available");
            showCopyFeedback("Error", "No schedule available.", Sonner.Type.ERROR);
            return;
        }

        // Show loading state
        copyBtn.setDisable(true);
        copyingIndicator.setVisible(true);
        copyingIndicator.setManaged(true);
        copyBtn.setText("Copying...");

        // Calculate last week's date range
        LocalDate lastWeekStart = currentWeekStart.minusWeeks(1);
        LocalDate lastWeekEnd = lastWeekStart.plusDays(6);

        // Get meals from last week
        Map<LocalDate, Map<MealType, String>> lastWeekMeals = currentSchedule.getMealsBetween(lastWeekStart, lastWeekEnd);

        if (lastWeekMeals.isEmpty()) {
            // No meals to copy - this is not an error, just inform the user
            logger.info("No meals found in last week to copy");
            resetCopyButton();
            showCopyFeedback("No meals to copy", "No meals were found in last week's schedule.", Sonner.Type.INFO);
            return;
        }

        // Copy meals to current week in background thread
        new Thread(() -> {
            AtomicInteger copiedCount = new AtomicInteger(0);
            AtomicInteger skippedCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);

            for (int i = 0; i < 7; i++) {
                LocalDate currentDate = currentWeekStart.plusDays(i);
                LocalDate lastWeekDate = lastWeekStart.plusDays(i);

                Map<MealType, String> mealsForLastWeekDate = lastWeekMeals.get(lastWeekDate);
                if (mealsForLastWeekDate == null || mealsForLastWeekDate.isEmpty()) {
                    continue;
                }

                // Copy each meal type (Breakfast, Lunch, Dinner)
                for (MealType mealType : MealType.values()) {
                    String recipeId = mealsForLastWeekDate.get(mealType);
                    if (recipeId == null || recipeId.trim().isEmpty()) {
                        continue;
                    }

                    // Only copy if the current slot is empty
                    if (currentSchedule.isSlotFree(currentDate, mealType)) {
                        try {
                            addMealController.execute(
                                currentDate.toString(),
                                mealType.name(),
                                recipeId
                            );
                            copiedCount.incrementAndGet();
                        } catch (IllegalArgumentException e) {
                            // Handle invalid date/mealType format
                            logger.error("Error copying meal for {} {}: Invalid format - {}", currentDate, mealType, e.getMessage(), e);
                            errorCount.incrementAndGet();
                        } catch (Exception e) {
                            // Handle other unexpected errors
                            logger.error("Error copying meal for {} {}: {}", currentDate, mealType, e.getMessage(), e);
                            errorCount.incrementAndGet();
                        }
                    } else {
                        skippedCount.incrementAndGet();
                    }
                }
            }

            // Store final values for use in Platform.runLater
            final int finalCopiedCount = copiedCount.get();
            final int finalSkippedCount = skippedCount.get();
            final int finalErrorCount = errorCount.get();

            // Update UI on JavaFX thread
            Platform.runLater(() -> {
                resetCopyButton();
                requestScheduleForActiveUser();
                updateView();
                
                // Show feedback
                if (finalCopiedCount > 0) {
                    String message = String.format("Successfully copied %d meal(s) to this week.", finalCopiedCount);
                    if (finalSkippedCount > 0) {
                        message += String.format(" %d meal(s) were skipped (slots already filled).", finalSkippedCount);
                    }
                    if (finalErrorCount > 0) {
                        message += String.format(" %d meal(s) failed to copy.", finalErrorCount);
                    }
                    showCopyFeedback("Copy completed", message, Sonner.Type.SUCCESS);
                } else if (finalErrorCount > 0) {
                    showCopyFeedback("Copy failed", String.format("Failed to copy %d meal(s). Please try again.", finalErrorCount), Sonner.Type.ERROR);
                } else {
                    showCopyFeedback("No meals copied", "All slots are already filled for this week.", Sonner.Type.INFO);
                }
            });

            // Log summary
            if (finalCopiedCount > 0 || finalSkippedCount > 0 || finalErrorCount > 0) {
                logger.info("Copy Last Week completed: {} meals copied, {} skipped (slots already filled){}", 
                    finalCopiedCount, finalSkippedCount, (finalErrorCount > 0 ? ", " + finalErrorCount + " errors" : ""));
            }
        }).start();
    }
    
    private void resetCopyButton() {
        copyBtn.setDisable(false);
        copyingIndicator.setVisible(false);
        copyingIndicator.setManaged(false);
        copyBtn.setText("Copy Last Week");
    }
    
    private void showCopyFeedback(String title, String message, Sonner.Type type) {
        // Use Sonner if available, otherwise just log
        try {
            Sonner sonner = new Sonner();
            sonner.show(title, message, type);
        } catch (Exception e) {
            logger.info("Copy feedback: {} - {}", title, message);
        }
    }
    
    /**
     * Phase 5: Handles auto-fill button click.
     * Automatically fills empty meal slots in the current week using recommended recipes.
     * 
     * Flow:
     * 1. Check if user is logged in
     * 2. Find all empty meal slots in the current week
     * 3. Request recommendations for the user
     * 4. When recommendations are received (via propertyChange), assign them to empty slots
     */
    private void handleAutoFill() {
        // Prevent duplicate requests
        if (isAutoFilling) {
            logger.info("Auto-fill already in progress, ignoring duplicate request");
            return;
        }
        
        String userId = SessionManager.getInstance().getCurrentUserId();
        if (userId == null || userId.trim().isEmpty()) {
            logger.warn("Cannot auto-fill: User not logged in");
            return;
        }
        
        if (recommendationsController == null) {
            logger.error("Cannot auto-fill: Recommendations controller not available");
            return;
        }
        
        if (addMealController == null) {
            logger.error("Cannot auto-fill: Add meal controller not available");
            return;
        }
        
        if (recommendationsViewModel == null) {
            logger.error("Cannot auto-fill: Recommendations view model not available");
            return;
        }
        
        // Get current schedule
        Schedule schedule = scheduleViewModel.getSchedule();
        if (schedule == null) {
            logger.warn("Cannot auto-fill: No schedule available");
            return;
        }
        
        // Find all empty slots in the current week
        List<EmptySlot> emptySlots = findEmptySlots(schedule);
        if (emptySlots.isEmpty()) {
            logger.info("No empty slots to fill in the current week");
            return;
        }
        
        // Set flag to indicate we're auto-filling
        isAutoFilling = true;
        
        logger.info("Auto-filling {} empty slot(s) in the current week", emptySlots.size());
        
        // Request recommendations
        // The recommendations will be received via propertyChange("recommendations")
        try {
            recommendationsController.execute(userId);
        } catch (Exception e) {
            // Reset flag on error
            isAutoFilling = false;
            logger.error("Failed to request recommendations for auto-fill: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Phase 5: Finds all empty meal slots in the current week.
     * 
     * @param schedule The current schedule
     * @return List of empty slots (date and meal type pairs)
     */
    private List<EmptySlot> findEmptySlots(Schedule schedule) {
        List<EmptySlot> emptySlots = new ArrayList<>();
        
        // Check each day of the current week (Monday to Sunday)
        for (int i = 0; i < 7; i++) {
            LocalDate date = currentWeekStart.plusDays(i);
            
            // Check each meal type (Breakfast, Lunch, Dinner)
            for (MealType mealType : MealType.values()) {
                if (schedule.isSlotFree(date, mealType)) {
                    emptySlots.add(new EmptySlot(date, mealType));
                }
            }
        }
        
        return emptySlots;
    }
    
    /**
     * Phase 5: Processes recommendations received for auto-fill.
     * Assigns recommendations to empty meal slots in the current week.
     * Called when RecipeBrowseViewModel fires "recommendations" property change.
     */
    private void processAutoFillRecommendations() {
        if (!isAutoFilling) {
            return; // Not in auto-fill mode
        }
        
        isAutoFilling = false; // Reset flag
        
        if (recommendationsViewModel == null) {
            logger.error("Failed to receive recommendations for auto-fill: ViewModel is null");
            return;
        }
        
        List<Recipe> recommendations = recommendationsViewModel.getRecommendations();
        if (recommendations == null || recommendations.isEmpty()) {
            logger.info("No recommendations available for auto-fill");
            return;
        }
        
        // Get current schedule
        Schedule schedule = scheduleViewModel.getSchedule();
        if (schedule == null) {
            logger.warn("Cannot process auto-fill: No schedule available");
            return;
        }
        
        // Find all empty slots
        List<EmptySlot> emptySlots = findEmptySlots(schedule);
        if (emptySlots.isEmpty()) {
            logger.info("No empty slots to fill");
            return;
        }
        
        // Assign recommendations to empty slots
        int mealsAdded = 0;
        int mealsFailed = 0;
        int recommendationIndex = 0;
        
        for (EmptySlot slot : emptySlots) {
            if (recommendationIndex >= recommendations.size()) {
                // No more recommendations available
                logger.debug("No more recommendations available, filled {} out of {} empty slots", 
                    mealsAdded, emptySlots.size());
                break;
            }
            
            Recipe recipe = recommendations.get(recommendationIndex);
            if (recipe != null && recipe.getRecipeId() != null && !recipe.getRecipeId().trim().isEmpty()) {
                try {
                    String dateString = slot.date.toString();
                    String mealTypeString = slot.mealType.name();
                    String recipeId = recipe.getRecipeId();
                    
                    addMealController.execute(dateString, mealTypeString, recipeId);
                    mealsAdded++;
                    recommendationIndex++; // Move to next recommendation
                    logger.debug("Successfully added meal {} on {} for auto-fill", slot.mealType, slot.date);
                } catch (IllegalArgumentException e) {
                    // Invalid date or meal type format
                    mealsFailed++;
                    recommendationIndex++; // Skip this recommendation
                    logger.warn("Invalid input for meal {} on {} in auto-fill: {}", 
                        slot.mealType, slot.date, e.getMessage());
                } catch (Exception e) {
                    // Other exceptions (e.g., schedule conflicts, data access errors)
                    mealsFailed++;
                    recommendationIndex++; // Skip this recommendation
                    logger.error("Failed to add meal {} on {} for auto-fill: {}", 
                        slot.mealType, slot.date, e.getMessage(), e);
                }
            } else {
                // Recipe ID is null or empty, skip this recommendation
                logger.warn("Skipping recommendation {}: recipe ID is null or empty", recommendationIndex);
                recommendationIndex++;
            }
        }
        
        logger.info("Auto-fill completed: {} meal(s) added, {} failed out of {} empty slots", 
            mealsAdded, mealsFailed, emptySlots.size());
        
        // Refresh the schedule view on JavaFX thread after all meals are added
        Platform.runLater(() -> {
            requestScheduleForActiveUser();
            updateView();
        });
    }
    
    /**
     * Phase 5: Helper class to represent an empty meal slot.
     */
    private static class EmptySlot {
        final LocalDate date;
        final MealType mealType;
        
        EmptySlot(LocalDate date, MealType mealType) {
            this.date = date;
            this.mealType = mealType;
        }
    }
}
