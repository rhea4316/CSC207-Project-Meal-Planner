package com.mealplanner.view;

import com.mealplanner.entity.MealType;
import com.mealplanner.entity.Schedule;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.ViewScheduleController;
import com.mealplanner.interface_adapter.view_model.ScheduleViewModel;
import com.mealplanner.view.component.StyledTooltip;
import com.mealplanner.view.util.SvgIconLoader;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;
import java.util.Map;

public class ScheduleView extends BorderPane implements PropertyChangeListener {

    private final ScheduleViewModel scheduleViewModel;
    private final ViewScheduleController controller;
    private final ViewManagerModel viewManagerModel;

    // UI Components
    private Label dateRangeLabel;
    private GridPane gridPane;
    private MealSlotPanel[][] mealSlots; // [row][col] -> row=meal, col=day
    private ProgressBar mealsPlannedProgress;
    private Label mealsPlannedLabel;

    // State
    private LocalDate currentWeekStart;

    public ScheduleView(ScheduleViewModel scheduleViewModel, ViewScheduleController controller, ViewManagerModel viewManagerModel) {
        this.scheduleViewModel = scheduleViewModel;
        this.scheduleViewModel.addPropertyChangeListener(this);
        this.controller = controller;
        this.viewManagerModel = viewManagerModel;
        this.viewManagerModel.addPropertyChangeListener(this);
        
        this.currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // Initial Load
        this.controller.execute("Eden Chang");

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
        
        // Navigation Center
        HBox navBox = new HBox(8);
        navBox.setAlignment(Pos.CENTER);
        
        Button prevBtn = createIconButton("/svg/angle-left.svg");
        prevBtn.setOnAction(e -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            updateView();
        });
        
        Button thisWeekBtn = new Button("This Week");
        thisWeekBtn.setStyle("-fx-background-color: white; -fx-text-fill: -fx-theme-primary; -fx-border-color: -fx-theme-primary; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-cursor: hand; -fx-font-weight: 600;");
        thisWeekBtn.setOnAction(e -> {
            currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            updateView();
        });
        
        Button nextBtn = createIconButton("/svg/angle-right.svg");
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
        
        Button copyBtn = createActionButton("Copy Last Week", "/svg/calendar.svg"); // Fallback icon
        Button autoFillBtn = createActionButton("Auto-fill", "/svg/star.svg");
        
        Button newPlanBtn = new Button("New Plan");
        newPlanBtn.setStyle("-fx-background-color: -fx-theme-primary; -fx-text-fill: white; -fx-background-radius: 8px; -fx-font-weight: 600; -fx-cursor: hand; -fx-padding: 8 16;");
        newPlanBtn.setOnAction(e -> viewManagerModel.setActiveView(ViewManager.BROWSE_RECIPE_VIEW));
        
        actionBox.getChildren().addAll(copyBtn, autoFillBtn, newPlanBtn);
        
        topRow.getChildren().addAll(titleBox, spacer1, navBox, spacer2, actionBox);
        container.getChildren().add(topRow);
        
        return container;
    }

    private Button createIconButton(String iconPath) {
        Button btn = new Button();
        btn.setStyle("-fx-background-color: white; -fx-border-color: -fx-color-gray-200; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-cursor: hand; -fx-padding: 8;");
        Node icon = SvgIconLoader.loadIcon(iconPath, 16, Color.web("#374151"));
        btn.setGraphic(icon);
        return btn;
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
        Label calValue = new Label("2770");
        calValue.getStyleClass().add("text-gray-900");
        calValue.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label calUnit = new Label("cal");
        calUnit.getStyleClass().add("text-gray-500");
        calUnit.setStyle("-fx-font-size: 16px;");
        calValueBox.getChildren().addAll(calValue, calUnit);
        
        Label calAvg = new Label("Average: 396 cal/day");
        calAvg.getStyleClass().add("text-gray-400");
        calAvg.setStyle("-fx-font-size: 13px;");
        
        calCard.getChildren().addAll(calTitle, calValueBox, calAvg);
        
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
        
        panel.setStyle("-fx-background-color: " + bgHex + "; -fx-background-radius: 12px;");
        panel.setPrefHeight(160);
        
        Node icon = SvgIconLoader.loadIcon(iconPath, 24, Color.web(textHex));
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + textHex + "; -fx-font-weight: 500; -fx-font-size: 14px;");
        
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
        
        Label calLabel = new Label("770 cal"); // Placeholder, real logic would sum cals
        calLabel.getStyleClass().add(isActive ? "text-lime-600" : "text-gray-400");
        calLabel.setStyle("-fx-font-size: 11px;");
        
        panel.getChildren().addAll(dayLabel, dateLabel, calLabel);
        return panel;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
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
        Schedule schedule = scheduleViewModel.getSchedule();
        if (schedule != null) {
            Map<LocalDate, Map<MealType, String>> allMeals = schedule.getAllMeals();
            
            for (int i = 0; i < 7; i++) {
                LocalDate date = currentWeekStart.plusDays(i);
                Map<MealType, String> mealsForDate = allMeals.get(date);
                
                if (mealsForDate != null) {
                    if (mealsForDate.containsKey(MealType.BREAKFAST)) {
                        mealSlots[0][i].setMeal(mealsForDate.get(MealType.BREAKFAST), "320 cal"); // Mock cal
                        filledCount++;
                    }
                    if (mealsForDate.containsKey(MealType.LUNCH)) {
                        mealSlots[1][i].setMeal(mealsForDate.get(MealType.LUNCH), "450 cal");
                        filledCount++;
                    }
                    if (mealsForDate.containsKey(MealType.DINNER)) {
                        mealSlots[2][i].setMeal(mealsForDate.get(MealType.DINNER), "520 cal");
                        filledCount++;
                    }
                }
            }
        }
        
        // Update Stats
        mealsPlannedLabel.setText(filledCount + "/21");
        mealsPlannedProgress.setProgress(filledCount / 21.0);
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
            
            // Dashed Border Style
            setStyle("-fx-background-color: transparent; -fx-border-color: #e5e7eb; -fx-border-width: 2px; -fx-border-style: dashed; -fx-border-radius: 12px;");
            
            if (currentTooltip != null) {
                Tooltip.uninstall(this, currentTooltip);
                currentTooltip = null;
            }
            
            // Dashed Circle with Plus
            StackPane circle = new StackPane();
            circle.setPrefSize(40, 40);
            circle.setMaxSize(40, 40);
            circle.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 2px; -fx-border-style: dashed; -fx-border-radius: 50%; -fx-background-color: transparent;");
            
            Node plusIcon = SvgIconLoader.loadIcon("/svg/plus.svg", 16, Color.web("#9ca3af"));
            if (plusIcon != null) circle.getChildren().add(plusIcon);
            else circle.getChildren().add(new Label("+"));
            
            getChildren().add(circle);
        }

        public void setMeal(String recipeName, String calories) {
            this.isFilled = true;
            getChildren().clear();
            
            // Filled Card Style
            setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 4, 0, 0, 2); -fx-border-width: 0;");
            
            // Tooltip
            if (currentTooltip != null) {
                Tooltip.uninstall(this, currentTooltip);
            }
            currentTooltip = new StyledTooltip(recipeName + "\n" + calories);
            Tooltip.install(this, currentTooltip);
            
            // Image placeholder
            Region imagePlaceholder = new Region();
            imagePlaceholder.setPrefHeight(80);
            imagePlaceholder.setMinHeight(80);
            imagePlaceholder.setStyle("-fx-background-color: #e5e7eb; -fx-background-radius: 12px 12px 0 0;");
            // TODO: Load actual image if available
            
            // Content section
            VBox filledContent = new VBox(4);
            filledContent.setPadding(new Insets(10));
            filledContent.setAlignment(Pos.CENTER_LEFT);
            
            Label contentLabel = new Label(recipeName);
            contentLabel.getStyleClass().add("text-gray-900");
            contentLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 13px;");
            contentLabel.setWrapText(true);
            
            Label calLabel = new Label(calories);
            calLabel.getStyleClass().add("text-gray-500");
            calLabel.setStyle("-fx-font-size: 11px;");
            
            filledContent.getChildren().addAll(contentLabel, calLabel);
            
            getChildren().addAll(imagePlaceholder, filledContent);
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
}
