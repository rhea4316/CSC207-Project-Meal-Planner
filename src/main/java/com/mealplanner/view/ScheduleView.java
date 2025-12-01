package com.mealplanner.view;

import com.mealplanner.entity.MealType;
import com.mealplanner.entity.NutritionInfo;
import com.mealplanner.entity.Recipe;
import com.mealplanner.entity.Schedule;
import com.mealplanner.exception.DataAccessException;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.AddMealController;
import com.mealplanner.interface_adapter.controller.DeleteMealController;
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
    private final RecipeRepository recipeRepository;
    private final GetRecommendationsController recommendationsController;
    private final RecipeBrowseViewModel recommendationsViewModel;
    private final AddMealController addMealController;
    private final DeleteMealController deleteMealController;
    private final ImageCacheManager imageCache = ImageCacheManager.getInstance();
    
    /**
     * Flag to track if we're waiting for recommendations for auto-fill.
     * Used to distinguish between user-initiated recommendation requests and auto-fill requests.
     */
    private boolean isAutoFilling = false;

    // UI Components
    private Label dateRangeLabel;
    private GridPane gridPane;
    private MealSlotPanel[][] mealSlots; // [row][col] -> row=meal, col=day
    private ProgressBar mealsPlannedProgress;
    private Label mealsPlannedLabel;
    private Label mealsPlannedSlash;
    private Label mealsPlannedTotal;
    private Label calValueLabel;
    private Label calAvgLabel;
    private Button copyBtn;
    private ProgressIndicator copyingIndicator;

    // State
    private LocalDate currentWeekStart;

    /**
     * Constructor for ScheduleView.
     * 
     * @param scheduleViewModel The schedule view model for meal data
     * @param controller The view schedule controller
     * @param viewManagerModel The view manager model for navigation
     * @param recipeRepository The recipe repository for recipe data
     * @param recommendationsController The controller for recommendations (used for auto-fill)
     * @param recommendationsViewModel The view model for receiving recommendations (used for auto-fill)
     * @param addMealController The controller for adding meals to schedule (used for Copy Last Week)
     * @param deleteMealController The controller for deleting meals from schedule
     */
    public ScheduleView(ScheduleViewModel scheduleViewModel, ViewScheduleController controller, ViewManagerModel viewManagerModel, RecipeRepository recipeRepository, GetRecommendationsController recommendationsController, RecipeBrowseViewModel recommendationsViewModel, AddMealController addMealController, DeleteMealController deleteMealController) {
        this.scheduleViewModel = scheduleViewModel;
        this.scheduleViewModel.addPropertyChangeListener(this);
        this.controller = controller;
        this.viewManagerModel = viewManagerModel;
        this.viewManagerModel.addPropertyChangeListener(this);
        this.recipeRepository = recipeRepository;
        this.recommendationsController = recommendationsController;
        this.recommendationsViewModel = recommendationsViewModel;
        this.addMealController = addMealController;
        this.deleteMealController = deleteMealController;
        
        // Listen to recommendations changes for auto-fill
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
        // Use StackPane to keep navigation centered regardless of title/action width
        StackPane topRowContainer = new StackPane();
        topRowContainer.setAlignment(Pos.CENTER);
        topRowContainer.setMaxWidth(Double.MAX_VALUE);
        
        // Background layer: Title on left, Actions on right
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.setMaxWidth(Double.MAX_VALUE);
        
        // Title Section - Left side
        VBox titleBox = new VBox(4);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Weekly Plan");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: 500; -fx-text-fill: #000000;");
        
        dateRangeLabel = new Label();
        dateRangeLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6b7280; -fx-font-weight: 400;");
        
        titleBox.getChildren().addAll(title, dateRangeLabel);
        
        // Spacer to push actions to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Actions Right
        HBox actionBox = new HBox(12);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        
        copyBtn = createActionButton("Copy Last Week", "/svg/copy-alt.svg");
        copyBtn.setOnAction(e -> handleCopyLastWeek());
        
        copyingIndicator = new ProgressIndicator();
        copyingIndicator.setPrefSize(14, 14);
        copyingIndicator.setVisible(false);
        copyingIndicator.setManaged(false);
        Button autoFillBtn = createActionButton("Auto-fill", "/svg/sparkles.svg");
        autoFillBtn.setOnAction(e -> handleAutoFill());
        
        Button newPlanBtn = new Button("New Plan");
        // Solid background color: #84CC16
        newPlanBtn.setStyle("-fx-text-fill: white; -fx-background-color: #84CC16; -fx-background-radius: 12px; -fx-font-size: 14px; -fx-font-weight: 400; -fx-cursor: hand; -fx-padding: 10 24;");
        newPlanBtn.setOnMouseEntered(e -> {
            newPlanBtn.setStyle("-fx-text-fill: white; -fx-background-color: #65a30d; -fx-background-radius: 12px; -fx-font-size: 14px; -fx-font-weight: 400; -fx-cursor: hand; -fx-padding: 10 24;");
        });
        newPlanBtn.setOnMouseExited(e -> {
            newPlanBtn.setStyle("-fx-text-fill: white; -fx-background-color: #84CC16; -fx-background-radius: 12px; -fx-font-size: 14px; -fx-font-weight: 400; -fx-cursor: hand; -fx-padding: 10 24;");
        });
        newPlanBtn.setOnAction(e -> viewManagerModel.setActiveView(ViewManager.BROWSE_RECIPE_VIEW));
        
        HBox copyBtnContainer = new HBox(8);
        copyBtnContainer.setAlignment(Pos.CENTER);
        copyBtnContainer.getChildren().addAll(copyBtn, copyingIndicator);
        actionBox.getChildren().addAll(copyBtnContainer, autoFillBtn, newPlanBtn);
        
        topRow.getChildren().addAll(titleBox, spacer, actionBox);
        
        // Navigation Center - Always centered on top of the StackPane
        HBox navBox = new HBox(8);
        navBox.setAlignment(Pos.CENTER);
        
        Button prevBtn = new Button();
        prevBtn.setPrefSize(36, 36);
        prevBtn.setMinSize(36, 36);
        prevBtn.setMaxSize(36, 36);
        prevBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-cursor: hand;");
        Node prevIcon = SvgIconLoader.loadIcon("/svg/angle-left.svg", 16, Color.web("#000000"));
        prevBtn.setGraphic(prevIcon);
        prevBtn.setOnMouseEntered(e -> prevBtn.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-cursor: hand;"));
        prevBtn.setOnMouseExited(e -> prevBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-cursor: hand;"));
        prevBtn.setOnAction(e -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            updateView();
        });
        
        Button thisWeekBtn = new Button("This Week");
        thisWeekBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #4d7c0f; -fx-border-color: #84cc16; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-cursor: hand; -fx-font-size: 14px; -fx-font-weight: 500; -fx-padding: 8 16;");
        thisWeekBtn.setOnAction(e -> {
            currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            updateView();
        });
        
        Button nextBtn = new Button();
        nextBtn.setPrefSize(36, 36);
        nextBtn.setMinSize(36, 36);
        nextBtn.setMaxSize(36, 36);
        nextBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-cursor: hand;");
        Node nextIcon = SvgIconLoader.loadIcon("/svg/angle-right.svg", 16, Color.web("#000000"));
        nextBtn.setGraphic(nextIcon);
        nextBtn.setOnMouseEntered(e -> nextBtn.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-cursor: hand;"));
        nextBtn.setOnMouseExited(e -> nextBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-cursor: hand;"));
        nextBtn.setOnAction(e -> {
            currentWeekStart = currentWeekStart.plusWeeks(1);
            updateView();
        });
        
        navBox.getChildren().addAll(prevBtn, thisWeekBtn, nextBtn);
        
        // StackPane layers: background (topRow) and foreground (navBox centered)
        topRowContainer.getChildren().addAll(topRow, navBox);
        StackPane.setAlignment(navBox, Pos.CENTER);
        
        container.getChildren().add(topRowContainer);
        
        return container;
    }

    private Button createActionButton(String text, String iconPath) {
        Button btn = new Button(text);
        // Default style: white background, #0c5906 text and icon
        btn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #0c5906; -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-cursor: hand; -fx-padding: 8 16; -fx-font-size: 14px;");
        Node icon = SvgIconLoader.loadIcon(iconPath, 16, Color.web("#0c5906"));
        if (icon != null) {
            btn.setGraphic(icon);
            btn.setGraphicTextGap(8);
        }
        // Hover style: #f2f2f2 background, keep text and icon color (#0c5906)
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #f2f2f2; -fx-text-fill: #0c5906; -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-cursor: hand; -fx-padding: 8 16; -fx-font-size: 14px;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #0c5906; -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-cursor: hand; -fx-padding: 8 16; -fx-font-size: 14px;"));
        return btn;
    }

    private HBox createStatsSection() {
        HBox container = new HBox(16);
        container.setStyle("-fx-padding: 0 0 24 0;");
        
        // Meals Planned Card
        VBox plannedCard = new VBox(8);
        plannedCard.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 2, 0, 0, 1);");
        plannedCard.setPadding(new Insets(16));
        HBox.setHgrow(plannedCard, Priority.ALWAYS);
        
        Label plannedTitle = new Label("Meals Planned");
        plannedTitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280; -fx-font-weight: 400;");
        
        // Meals Planned 숫자 표시: 왼쪽 숫자 24px, 슬래시+오른쪽 숫자 18px
        HBox mealsPlannedBox = new HBox(0);
        mealsPlannedBox.setAlignment(Pos.BASELINE_LEFT);
        mealsPlannedLabel = new Label("7");
        mealsPlannedLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #000000; -fx-font-weight: 400;");
        mealsPlannedSlash = new Label("/");
        mealsPlannedSlash.setStyle("-fx-font-size: 18px; -fx-text-fill: #9ca3af; -fx-font-weight: 400;");
        mealsPlannedTotal = new Label("21");
        mealsPlannedTotal.setStyle("-fx-font-size: 18px; -fx-text-fill: #9ca3af; -fx-font-weight: 400;");
        mealsPlannedBox.getChildren().addAll(mealsPlannedLabel, mealsPlannedSlash, mealsPlannedTotal);
        
        mealsPlannedProgress = new ProgressBar(0.33);
        mealsPlannedProgress.setMaxWidth(Double.MAX_VALUE);
        mealsPlannedProgress.setPrefHeight(8);
        // JavaFX ProgressBar는 그라데이션을 직접 지원하지 않으므로 중간 색상 사용
        // #84cc16 (lime-500)와 #22c55e (green-500)의 중간 색상인 #a3d636 사용
        mealsPlannedProgress.setStyle("-fx-control-inner-background: #f3f4f6; -fx-text-box-border: transparent; -fx-accent: #84cc16;");
        
        plannedCard.getChildren().addAll(plannedTitle, mealsPlannedBox, mealsPlannedProgress);
        
        // Weekly Calories Card
        VBox calCard = new VBox(8);
        calCard.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 2, 0, 0, 1);");
        calCard.setPadding(new Insets(16));
        HBox.setHgrow(calCard, Priority.ALWAYS);
        
        Label calTitle = new Label("Weekly Calories");
        calTitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280; -fx-font-weight: 400;");
        
        HBox calValueBox = new HBox(4);
        calValueBox.setAlignment(Pos.BASELINE_LEFT);
        calValueLabel = new Label("0");
        calValueLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #000000; -fx-font-weight: 400;");
        
        Label calUnit = new Label(" cal");
        calUnit.setStyle("-fx-font-size: 18px; -fx-text-fill: #9ca3af; -fx-font-weight: 400;");
        calValueBox.getChildren().addAll(calValueLabel, calUnit);
        
        calAvgLabel = new Label("Average: 0 cal/day");
        calAvgLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280; -fx-font-weight: 400;");
        
        calCard.getChildren().addAll(calTitle, calValueBox, calAvgLabel);
        
        container.getChildren().addAll(plannedCard, calCard);
        return container;
    }

    private GridPane createGrid() {
        gridPane = new GridPane();
        gridPane.setHgap(0);
        gridPane.setVgap(0);
        gridPane.setAlignment(Pos.TOP_LEFT);
        gridPane.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 16px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 2, 0, 0, 1);");
        
        // GridPane 크기 제약 명시적 설정
        gridPane.setMinWidth(900);
        gridPane.setPrefWidth(Region.USE_COMPUTED_SIZE);
        gridPane.setMaxWidth(Double.MAX_VALUE);
        
        mealSlots = new MealSlotPanel[3][7]; 

        // Column Constraints - 명시적으로 설정
        // Column 0: 고정 너비 (Meal type labels)
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(100);
        labelCol.setPrefWidth(100);
        labelCol.setMaxWidth(100);
        labelCol.setHgrow(Priority.NEVER);
        gridPane.getColumnConstraints().add(labelCol);

        // Column 1-7: 동적 너비 (Day columns)
        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setMinWidth(140);
            col.setPrefWidth(140);
            col.setMaxWidth(Double.MAX_VALUE);
            col.setHgrow(Priority.ALWAYS);
            gridPane.getColumnConstraints().add(col);
        }
        
        // Row Constraints - 명시적으로 설정
        // Row 0: Day headers
        RowConstraints headerRow = new RowConstraints();
        headerRow.setMinHeight(80);
        headerRow.setPrefHeight(80);
        headerRow.setMaxHeight(80);
        headerRow.setVgrow(Priority.NEVER);
        gridPane.getRowConstraints().add(headerRow);

        // Row 1-3: Meal rows
        for (int i = 0; i < 3; i++) {
            RowConstraints row = new RowConstraints();
            row.setMinHeight(160);
            row.setPrefHeight(160);
            row.setMaxHeight(Double.MAX_VALUE);
            row.setVgrow(Priority.ALWAYS);
            gridPane.getRowConstraints().add(row);
        }

        // Day headers will be added by updateDayHeaders() called from updateView()
        // Don't initialize here to avoid duplication
        
        // 1. Row Headers (Meal Types) - 명시적으로 row/column 설정
        VBox breakfastHeader = createRowHeader("Breakfast", "/svg/mug-hot.svg", "blue");
        GridPane.setRowIndex(breakfastHeader, 1);
        GridPane.setColumnIndex(breakfastHeader, 0);
        gridPane.getChildren().add(breakfastHeader);
        
        VBox lunchHeader = createRowHeader("Lunch", "/svg/brightness.svg", "amber");
        GridPane.setRowIndex(lunchHeader, 2);
        GridPane.setColumnIndex(lunchHeader, 0);
        gridPane.getChildren().add(lunchHeader);
        
        VBox dinnerHeader = createRowHeader("Dinner", "/svg/moon.svg", "rose");
        GridPane.setRowIndex(dinnerHeader, 3);
        GridPane.setColumnIndex(dinnerHeader, 0);
        gridPane.getChildren().add(dinnerHeader);
        
        // 2. Slots setup - 명시적으로 row/column 설정
        for (int c = 0; c < 7; c++) {
            for (int r = 0; r < 3; r++) {
                MealSlotPanel slot = new MealSlotPanel(r, c);
                mealSlots[r][c] = slot;
                GridPane.setRowIndex(slot, r + 1);
                GridPane.setColumnIndex(slot, c + 1);
                gridPane.getChildren().add(slot);
            }
        }

        return gridPane;
    }

    private VBox createRowHeader(String text, String iconPath, String colorTheme) {
        VBox panel = new VBox(8);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(16));
        
        // 크기 명시적 설정
        panel.setMinWidth(100);
        panel.setPrefWidth(100);
        panel.setMaxWidth(100);
        panel.setMinHeight(160);
        panel.setPrefHeight(160);
        panel.setMaxHeight(Double.MAX_VALUE);
        
        // Apply specific theme styling
        String bgHex, iconBgHex, textHex;
        if (colorTheme.equals("blue")) {
            bgHex = "#eff6ff"; iconBgHex = "#dbeafe"; textHex = "#2563eb"; // blue-50, blue-100, blue-600
        } else if (colorTheme.equals("amber")) {
            bgHex = "#fffbeb"; iconBgHex = "#fef3c7"; textHex = "#d97706"; // amber-50, amber-100, amber-600
        } else {
            bgHex = "#fff1f2"; iconBgHex = "#ffe4e6"; textHex = "#e11d48"; // rose-50, rose-100, rose-600
        }
        
        // Fill entire cell with background color, connect with grid visually
        panel.setStyle("-fx-background-color: " + bgHex + "; -fx-border-color: #f3f4f6; -fx-border-width: 0 1px 0 0;");
        
        // Icon container with background
        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(32, 32);
        iconContainer.setMinSize(32, 32);
        iconContainer.setMaxSize(32, 32);
        iconContainer.setStyle("-fx-background-color: " + iconBgHex + "; -fx-background-radius: 8px;");
        
        Node icon = SvgIconLoader.loadIcon(iconPath, 16, Color.web(textHex));
        if (icon != null) {
            iconContainer.getChildren().add(icon);
        }
        
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + textHex + "; -fx-font-weight: 600; -fx-font-size: 14px; -fx-text-transform: capitalize;");
        
        panel.getChildren().addAll(iconContainer, label);
        
        return panel;
    }

    private VBox createDayHeader(String dayName, LocalDate date, boolean isActive, int dailyCalories) {
        VBox panel = new VBox(4);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(16));
        
        // 크기 명시적 설정
        panel.setMinHeight(80);
        panel.setPrefHeight(80);
        panel.setMaxHeight(80);
        panel.setMinWidth(140);
        panel.setPrefWidth(Region.USE_COMPUTED_SIZE);
        panel.setMaxWidth(Double.MAX_VALUE);

        // Apply gradient background for header row
        if (isActive) {
            panel.setStyle("-fx-background-color: #ecfccb; -fx-border-color: #f3f4f6; -fx-border-width: 0 1px 1px 0;");
        } else {
            // Gradient from #f9fafb to #ffffff
            LinearGradient headerGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#f9fafb")), new Stop(1, Color.web("#ffffff")));
            panel.setBackground(new Background(new BackgroundFill(headerGradient, CornerRadii.EMPTY, Insets.EMPTY)));
            panel.setStyle("-fx-border-color: #f3f4f6; -fx-border-width: 0 1px 1px 0;");
        }

        Label dayLabel = new Label(dayName);
        if (isActive) {
            dayLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 500; -fx-text-fill: #4d7c0f;");
        } else {
            dayLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 500; -fx-text-fill: #6b7280;");
        }

        Label dateLabel = new Label(String.valueOf(date.getDayOfMonth()));
        if (isActive) {
            dateLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #4d7c0f;");
        } else {
            dateLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #000000;");
        }

        // Display daily calories if available
        String calText = dailyCalories > 0 ? dailyCalories + " cal" : "";

        if (!calText.isEmpty()) {
            Label calLabel = new Label(calText);
            if (isActive) {
                calLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #65a30d; -fx-font-weight: 400;");
            } else {
                calLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280; -fx-font-weight: 400;");
            }
            panel.getChildren().addAll(dayLabel, dateLabel, calLabel);
        } else {
            panel.getChildren().addAll(dayLabel, dateLabel);
        }
        
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
                if (ViewManager.SCHEDULE_VIEW.equals(viewManagerModel.getActiveView())) {
                    requestScheduleForActiveUser();
                    Platform.runLater(this::updateView);
                }
                break;
            case "recommendations":
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
     * Load a recipe by ID from the repository.
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
     * Calculate total calories for a specific date.
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

    /**
     * Update day headers when week changes or schedule updates.
     * This method is called from updateView() to ensure day headers are always up-to-date.
     */
    private void updateDayHeaders() {
        if (gridPane == null) {
            return; // Grid not initialized yet
        }
        
        // Clear existing headers (row 0) - more robust removal
        // Use a copy of the list to avoid ConcurrentModificationException
        List<Node> nodesToRemove = new ArrayList<>();
        for (Node node : gridPane.getChildren()) {
            Integer rowIndex = GridPane.getRowIndex(node);
            if (rowIndex != null && rowIndex == 0) {
                nodesToRemove.add(node);
            }
        }
        gridPane.getChildren().removeAll(nodesToRemove);

        // Re-add day headers with explicit row/column indices
        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        LocalDate today = LocalDate.now();
        Schedule schedule = scheduleViewModel.getSchedule();
        
        // 빈 좌측 셀 추가 (스펙에 따라)
        Region emptyCell = new Region();
        emptyCell.setStyle("-fx-background-color: transparent; -fx-border-color: #f3f4f6; -fx-border-width: 0 1px 1px 0;");
        emptyCell.setMinWidth(100);
        emptyCell.setPrefWidth(100);
        emptyCell.setMaxWidth(100);
        emptyCell.setMinHeight(80);
        emptyCell.setPrefHeight(80);
        emptyCell.setMaxHeight(80);
        GridPane.setRowIndex(emptyCell, 0);
        GridPane.setColumnIndex(emptyCell, 0);
        gridPane.getChildren().add(emptyCell);
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = currentWeekStart.plusDays(i);
            boolean isActive = date.equals(today);
            
            // Calculate daily calories once
            Map<MealType, String> mealsForDate = schedule != null ? schedule.getAllMeals().get(date) : null;
            int dailyCalories = calculateDailyCalories(date, mealsForDate);
            
            VBox dayHeader = createDayHeader(dayNames[i], date, isActive, dailyCalories);
            GridPane.setRowIndex(dayHeader, 0);
            GridPane.setColumnIndex(dayHeader, i + 1);
            gridPane.getChildren().add(dayHeader);
        }
        
        // 레이아웃 강제 업데이트
        gridPane.requestLayout();
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
        
        // 레이아웃 강제 업데이트
        if (gridPane != null) {
            gridPane.requestLayout();
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
                    // Load actual Recipe objects and pass them to MealSlotPanel
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
        mealsPlannedLabel.setText(String.valueOf(filledCount));
        mealsPlannedProgress.setProgress(filledCount / 21.0);
        
        // Update Weekly Calories with comma formatting
        String formattedCalories = String.format("%,d", totalWeeklyCalories);
        calValueLabel.setText(formattedCalories);
        int avgCalories = filledCount > 0 ? Math.round((float) totalWeeklyCalories / 7) : 0;
        calAvgLabel.setText("Average: " + avgCalories + " cal/day");
    }

    private class MealSlotPanel extends VBox {
        private final int row; 
        private final int col; 
        
        private boolean isFilled = false;
        private Tooltip currentTooltip;

        public MealSlotPanel(int row, int col) {
            this.row = row;
            this.col = col;
            
            setAlignment(Pos.CENTER);
            setCursor(Cursor.HAND);
            
            // 크기 명시적 설정 - GridPane이 제대로 계산하도록
            setMinWidth(140);
            setPrefWidth(Region.USE_COMPUTED_SIZE);
            setMaxWidth(Double.MAX_VALUE);
            setMinHeight(140);
            setPrefHeight(140);
            setMaxHeight(Double.MAX_VALUE);
            
            setupEmptyState();
            
            setOnMouseClicked(e -> handleSlotClick());
        }
        
        private void setupEmptyState() {
            getChildren().clear();
            
            // Check if this is today's date
            LocalDate slotDate = currentWeekStart.plusDays(col);
            boolean isToday = slotDate.equals(LocalDate.now());
            
            // Set background based on whether it's today
            if (isToday) {
                setStyle("-fx-background-color: rgba(247, 254, 231, 0.5); -fx-border-color: #f3f4f6; -fx-border-width: 1px; -fx-border-style: solid; -fx-border-radius: 0;");
            } else {
                setStyle("-fx-background-color: transparent; -fx-border-color: #f3f4f6; -fx-border-width: 1px; -fx-border-style: solid; -fx-border-radius: 0;");
            }
            
            // Add hover effect
            setOnMouseEntered(e -> {
                if (isToday) {
                    setStyle("-fx-background-color: rgba(247, 254, 231, 0.7); -fx-border-color: #f3f4f6; -fx-border-width: 1px; -fx-border-style: solid; -fx-border-radius: 0;");
                } else {
                    setStyle("-fx-background-color: #f9fafb; -fx-border-color: #f3f4f6; -fx-border-width: 1px; -fx-border-style: solid; -fx-border-radius: 0;");
                }
            });
            setOnMouseExited(e -> {
                if (isToday) {
                    setStyle("-fx-background-color: rgba(247, 254, 231, 0.5); -fx-border-color: #f3f4f6; -fx-border-width: 1px; -fx-border-style: solid; -fx-border-radius: 0;");
                } else {
                    setStyle("-fx-background-color: transparent; -fx-border-color: #f3f4f6; -fx-border-width: 1px; -fx-border-style: solid; -fx-border-radius: 0;");
                }
            });
            
            if (currentTooltip != null) {
                Tooltip.uninstall(this, currentTooltip);
                currentTooltip = null;
            }
            
            // Add button container
            VBox addButton = new VBox(8);
            addButton.setAlignment(Pos.CENTER);
            addButton.setPrefWidth(Double.MAX_VALUE);
            addButton.setPrefHeight(Double.MAX_VALUE);
            addButton.setMinHeight(100);
            
            // Plus icon container
            StackPane iconContainer = new StackPane();
            iconContainer.setPrefSize(32, 32);
            iconContainer.setMinSize(32, 32);
            iconContainer.setMaxSize(32, 32);
            iconContainer.setStyle("-fx-border-color: #d1d5db; -fx-border-width: 2px; -fx-border-style: dashed; -fx-border-radius: 8px; -fx-background-color: transparent;");
            
            Node plusIcon = SvgIconLoader.loadIcon("/svg/plus.svg", 16, Color.web("#d1d5db"));
            if (plusIcon != null) {
                iconContainer.getChildren().add(plusIcon);
            } else {
                iconContainer.getChildren().add(new Label("+"));
            }
            
            // Add text label (hidden by default, shown on hover)
            Label addLabel = new Label("Add");
            addLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #d1d5db; -fx-opacity: 0;");
            
            addButton.getChildren().addAll(iconContainer, addLabel);
            
            // Hover effects for add button
            addButton.setOnMouseEntered(e -> {
                iconContainer.setStyle("-fx-border-color: #84cc16; -fx-border-width: 2px; -fx-border-style: dashed; -fx-border-radius: 8px; -fx-background-color: #f7fee7;");
                if (plusIcon != null) {
                    iconContainer.getChildren().clear();
                    Node hoverIcon = SvgIconLoader.loadIcon("/svg/plus.svg", 16, Color.web("#84cc16"));
                    if (hoverIcon != null) {
                        iconContainer.getChildren().add(hoverIcon);
                    }
                }
                addLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #84cc16; -fx-opacity: 1;");
            });
            addButton.setOnMouseExited(e -> {
                iconContainer.setStyle("-fx-border-color: #d1d5db; -fx-border-width: 2px; -fx-border-style: dashed; -fx-border-radius: 8px; -fx-background-color: transparent;");
                if (plusIcon != null) {
                    iconContainer.getChildren().clear();
                    iconContainer.getChildren().add(plusIcon);
                }
                addLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #d1d5db; -fx-opacity: 0;");
            });
            
            getChildren().add(addButton);
        }

        /**
         * Set meal using Recipe object with real nutritional data.
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

            // Filled Card Style - Modern card design
            setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-style: solid; -fx-border-radius: 0;");
            setPadding(new Insets(8));

            // Tooltip
            if (currentTooltip != null) {
                Tooltip.uninstall(this, currentTooltip);
            }
            currentTooltip = new StyledTooltip(recipeName + "\n" + calories);
            Tooltip.install(this, currentTooltip);

            // Main card container with relative positioning for X button
            StackPane cardContainer = new StackPane();
            cardContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 2, 0, 0, 1);");
            cardContainer.setPrefWidth(Double.MAX_VALUE);
            cardContainer.setPrefHeight(Double.MAX_VALUE);
            cardContainer.setMinHeight(100);

            // X delete button (positioned absolutely, shown on hover)
            Button deleteBtn = new Button();
            deleteBtn.setPrefSize(24, 24);
            deleteBtn.setMinSize(24, 24);
            deleteBtn.setMaxSize(24, 24);
            deleteBtn.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 50%; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 2); -fx-cursor: hand; -fx-opacity: 0;");
            Node xIcon = SvgIconLoader.loadIcon("/svg/cross-small.svg", 12, Color.web("#000000"));
            if (xIcon != null) {
                deleteBtn.setGraphic(xIcon);
            }
            deleteBtn.setOnAction(e -> {
                e.consume(); // Prevent card click
                handleDeleteMeal();
            });
            deleteBtn.setOnMouseEntered(e -> {
                deleteBtn.setStyle("-fx-background-color: #fef2f2; -fx-background-radius: 50%; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 2); -fx-cursor: hand;");
                Node redIcon = SvgIconLoader.loadIcon("/svg/cross-small.svg", 12, Color.web("#dc2626"));
                if (redIcon != null) {
                    deleteBtn.setGraphic(redIcon);
                }
            });
            deleteBtn.setOnMouseExited(e -> {
                deleteBtn.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 50%; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 2); -fx-cursor: hand;");
                if (xIcon != null) {
                    deleteBtn.setGraphic(xIcon);
                }
            });
            
            // Position delete button in top-right corner
            StackPane.setAlignment(deleteBtn, Pos.TOP_RIGHT);
            StackPane.setMargin(deleteBtn, new Insets(4, 4, 0, 0));

            // Image section - load actual image if available, otherwise use placeholder
            VBox contentBox = new VBox(0);
            contentBox.setPrefWidth(Double.MAX_VALUE);
            contentBox.setPrefHeight(Double.MAX_VALUE);
            
            Node imageNode;
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                Image image = imageCache.getImage(imageUrl);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(Double.MAX_VALUE);
                imageView.setFitHeight(64);
                imageView.setPreserveRatio(false);
                imageView.setSmooth(true);
                imageView.setCache(true);
                // Clip image to rounded top corners only
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
                clip.widthProperty().bind(imageView.fitWidthProperty());
                clip.setHeight(64);
                clip.setArcWidth(8);
                clip.setArcHeight(8);
                imageView.setClip(clip);
                imageNode = imageView;
            } else {
                // Placeholder when no image available
                Region imagePlaceholder = new Region();
                imagePlaceholder.setPrefHeight(64);
                imagePlaceholder.setMinHeight(64);
                imagePlaceholder.setMaxHeight(64);
                LinearGradient placeholderGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#f3f4f6")), new Stop(1, Color.web("#f9fafb")));
                imagePlaceholder.setBackground(new Background(new BackgroundFill(placeholderGradient, CornerRadii.EMPTY, Insets.EMPTY)));
                // Clip placeholder to rounded top corners only
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
                clip.widthProperty().bind(imagePlaceholder.widthProperty());
                clip.setHeight(64);
                clip.setArcWidth(8);
                clip.setArcHeight(8);
                imagePlaceholder.setClip(clip);
                imageNode = imagePlaceholder;
            }

            // Content section - Compact text at bottom
            VBox filledContent = new VBox(4);
            filledContent.setPadding(new Insets(8));
            filledContent.setAlignment(Pos.CENTER_LEFT);
            filledContent.setMaxWidth(Double.MAX_VALUE);

            Label contentLabel = new Label(recipeName);
            contentLabel.setStyle("-fx-text-fill: #000000; -fx-font-weight: 400; -fx-font-size: 12px;");
            contentLabel.setWrapText(true);
            contentLabel.setMaxWidth(Double.MAX_VALUE);
            contentLabel.setMaxHeight(16);

            Label calLabel = new Label(calories);
            calLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px; -fx-font-weight: 400;");

            filledContent.getChildren().addAll(contentLabel, calLabel);
            contentBox.getChildren().addAll(imageNode, filledContent);
            
            cardContainer.getChildren().addAll(contentBox, deleteBtn);
            
            // Show delete button and change border color on hover
            setOnMouseEntered(e -> {
                setStyle("-fx-background-color: white; -fx-border-color: #bef264; -fx-border-width: 1px; -fx-border-style: solid; -fx-border-radius: 0;");
                deleteBtn.setOpacity(1);
            });
            setOnMouseExited(e -> {
                setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-style: solid; -fx-border-radius: 0;");
                deleteBtn.setOpacity(0);
            });

            getChildren().add(cardContainer);
        }
        
        private void handleDeleteMeal() {
            if (deleteMealController == null) {
                logger.warn("Cannot delete meal: DeleteMealController is not available");
                return;
            }
            
            // Check if user is logged in
            String userId = SessionManager.getInstance().getCurrentUserId();
            if (userId == null || userId.trim().isEmpty()) {
                logger.warn("Cannot delete meal: User not logged in");
                return;
            }
            
            // Calculate the date for this slot
            LocalDate slotDate = currentWeekStart.plusDays(col);
            
            // Determine meal type based on row (0=Breakfast, 1=Lunch, 2=Dinner)
            MealType mealType;
            switch (row) {
                case 0:
                    mealType = MealType.BREAKFAST;
                    break;
                case 1:
                    mealType = MealType.LUNCH;
                    break;
                case 2:
                    mealType = MealType.DINNER;
                    break;
                default:
                    logger.error("Invalid row index: {}", row);
                    return;
            }
            
            try {
                // Call delete meal controller
                String dateString = slotDate.toString();
                String mealTypeString = mealType.name();
                
                deleteMealController.execute(dateString, mealTypeString);
                
                logger.info("Delete meal requested for {} on {}", mealType, slotDate);
                
                // The schedule will be updated via propertyChange listener when the delete operation completes
                // Refresh the view after a short delay to allow the operation to complete
                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
                delay.setOnFinished(e -> {
                    requestScheduleForActiveUser();
                    updateView();
                });
                delay.play();
            } catch (IllegalArgumentException e) {
                logger.error("Invalid input for delete meal {} on {}: {}", mealType, slotDate, e.getMessage(), e);
            } catch (Exception e) {
                logger.error("Failed to delete meal {} on {}: {}", mealType, slotDate, e.getMessage(), e);
            }
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
     * Copy Last Week functionality.
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
     * Handles auto-fill button click.
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
     * Finds all empty meal slots in the current week.
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
     * Processes recommendations received for auto-fill.
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
     * Helper class to represent an empty meal slot.
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
