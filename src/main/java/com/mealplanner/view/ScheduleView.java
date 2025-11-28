package com.mealplanner.view;

import com.mealplanner.entity.MealType;
import com.mealplanner.entity.Schedule;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.ViewScheduleController;
import com.mealplanner.interface_adapter.view_model.ScheduleViewModel;
import com.mealplanner.view.component.Calendar;
import com.mealplanner.view.component.StyledToggleGroup;
import com.mealplanner.view.component.StyledTooltip;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.Cursor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

public class ScheduleView extends BorderPane implements PropertyChangeListener {

    private final ScheduleViewModel scheduleViewModel;
    @SuppressWarnings("unused")
    private final ViewScheduleController controller;
    private final ViewManagerModel viewManagerModel;

    // UI Components
    private Label dateRangeLabel;
    private GridPane gridPane;
    private MealSlotPanel[][] mealSlots; // [row][col] -> row=meal, col=day

    // State
    private LocalDate currentWeekStart;

    public ScheduleView(ScheduleViewModel scheduleViewModel, ViewScheduleController controller, ViewManagerModel viewManagerModel) {
        this.scheduleViewModel = scheduleViewModel;
        this.scheduleViewModel.addPropertyChangeListener(this);
        this.controller = controller;
        this.viewManagerModel = viewManagerModel;
        
        this.currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // Root Styles
        getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        createHeader();
        createGrid();
        
        updateView(); 
    }

    private void createHeader() {
        HBox headerPanel = new HBox();
        headerPanel.setAlignment(Pos.CENTER_LEFT);
        headerPanel.setSpacing(20);
        headerPanel.getStyleClass().add("dashboard-header");
        BorderPane.setMargin(headerPanel, new Insets(0, 0, 20, 0));

        // Date Range Label
        dateRangeLabel = new Label();
        dateRangeLabel.getStyleClass().add("section-title");
        dateRangeLabel.setStyle("-fx-font-size: 24px;");
        
        VBox titleBox = new VBox(dateRangeLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        // View Toggle
        StyledToggleGroup viewToggle = new StyledToggleGroup(Arrays.asList("Weekly", "Daily"));
        
        // Calendar Picker
        Calendar datePicker = new Calendar();
        datePicker.setValue(currentWeekStart);
        datePicker.setOnAction(e -> {
            LocalDate selected = datePicker.getValue();
            if (selected != null) {
                currentWeekStart = selected.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                updateView();
                updateDayHeaders();
            }
        });

        // Create New Plan Button
        Button createButton = new Button("Create New Plan");
        createButton.getStyleClass().add("primary-button");
        createButton.setOnAction(e -> {
            viewManagerModel.setActiveView(ViewManager.MEAL_PLAN_VIEW);
        });

        headerPanel.getChildren().addAll(titleBox, viewToggle, datePicker, createButton);
        setTop(headerPanel);
    }

    private void createGrid() {
        gridPane = new GridPane();
        gridPane.setHgap(16);
        gridPane.setVgap(16);
        
        mealSlots = new MealSlotPanel[3][7]; 

        // 1. Top Left Corner (Empty)
        gridPane.add(new Pane(), 0, 0);

        // 2. Rows Headers
        String[] mealLabels = {"Breakfast", "Lunch", "Dinner"};
        for (int r = 0; r < 3; r++) {
            gridPane.add(createRowHeader(mealLabels[r]), 0, r + 1);
        }
        
        // 3. Slots and Day Headers setup
        for (int c = 0; c < 7; c++) {
            // Slots
            for (int r = 0; r < 3; r++) {
                MealSlotPanel slot = new MealSlotPanel(r, c);
                mealSlots[r][c] = slot;
                gridPane.add(slot, c + 1, r + 1);
            }
        }
        updateDayHeaders();
        
        // Column Constraints
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(80);
        labelCol.setPrefWidth(80);
        labelCol.setMaxWidth(80);
        gridPane.getColumnConstraints().add(labelCol);

        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setMinWidth(120);
            gridPane.getColumnConstraints().add(col);
        }
        
        // Row Constraints
        RowConstraints headerRow = new RowConstraints();
        headerRow.setMinHeight(60);
        headerRow.setPrefHeight(60);
        gridPane.getRowConstraints().add(headerRow);

        for (int i = 0; i < 3; i++) {
            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.ALWAYS);
            row.setMinHeight(160);
            gridPane.getRowConstraints().add(row);
        }

        setCenter(gridPane);
    }

    private VBox createDayHeader(String dayName, LocalDate date, boolean isActive) {
        VBox panel = new VBox(4);
        panel.getStyleClass().add("day-header");
        if (isActive) {
            panel.getStyleClass().add("active");
        }
        
        Label dayLabel = new Label(dayName);
        dayLabel.getStyleClass().add("day-header-label");
        
        Label dateLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dateLabel.getStyleClass().add("day-header-date");
        
        panel.getChildren().addAll(dayLabel, dateLabel);
        return panel;
    }

    private VBox createRowHeader(String text) {
        VBox panel = new VBox();
        panel.setAlignment(Pos.CENTER_LEFT);
        
        Label label = new Label(text);
        label.getStyleClass().add("label");
        label.setStyle("-fx-font-weight: 600; -fx-text-fill: -fx-theme-muted-foreground;");
        
        panel.getChildren().add(label);
        return panel;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(this::updateView);
    }

    private void updateDayHeaders() {
        gridPane.getChildren().removeIf(node -> GridPane.getRowIndex(node) == 0 && GridPane.getColumnIndex(node) > 0);

        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        LocalDate today = LocalDate.now();
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = currentWeekStart.plusDays(i);
            boolean isActive = date.equals(today);
            gridPane.add(createDayHeader(dayNames[i], date, isActive), i + 1, 0);
        }
    }

    private void updateView() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);
        LocalDate weekEnd = currentWeekStart.plusDays(6);
        dateRangeLabel.setText("Weekly Plan: " + currentWeekStart.format(formatter) + " - " + weekEnd.format(formatter));
        
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 7; c++) {
                mealSlots[r][c].clear();
            }
        }

        Schedule schedule = scheduleViewModel.getSchedule();
        if (schedule != null) {
            Map<LocalDate, Map<MealType, String>> allMeals = schedule.getAllMeals();
            
            for (int i = 0; i < 7; i++) {
                LocalDate date = currentWeekStart.plusDays(i);
                Map<MealType, String> mealsForDate = allMeals.get(date);
                
                if (mealsForDate != null) {
                    if (mealsForDate.containsKey(MealType.BREAKFAST)) 
                        mealSlots[0][i].setMeal(mealsForDate.get(MealType.BREAKFAST), "150 kcal");
                    if (mealsForDate.containsKey(MealType.LUNCH)) 
                        mealSlots[1][i].setMeal(mealsForDate.get(MealType.LUNCH), "180 kcal");
                    if (mealsForDate.containsKey(MealType.DINNER)) 
                        mealSlots[2][i].setMeal(mealsForDate.get(MealType.DINNER), "250 kcal");
                }
            }
        }
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
            setMinHeight(160);
            setPrefHeight(160);
            
            setupEmptyState();
            
            setOnMouseClicked(e -> handleSlotClick());
        }
        
        private void setupEmptyState() {
            getChildren().clear();
            getStyleClass().setAll("meal-slot", "empty");
            
            if (currentTooltip != null) {
                Tooltip.uninstall(this, currentTooltip);
                currentTooltip = null;
            }
            
            VBox emptyContent = new VBox(5);
            emptyContent.setAlignment(Pos.CENTER);
            
            Label contentLabel = new Label("+");
            contentLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: -fx-theme-muted-foreground;");
            
            Label planLabel = new Label("Plan Meal");
            planLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: -fx-theme-muted-foreground;");
            
            emptyContent.getChildren().addAll(contentLabel, planLabel);
            getChildren().add(emptyContent);
        }

        public void setMeal(String recipeName, String calories) {
            this.isFilled = true;
            getChildren().clear();
            getStyleClass().setAll("meal-slot");
            
            // Tooltip
            if (currentTooltip != null) {
                Tooltip.uninstall(this, currentTooltip);
            }
            currentTooltip = new StyledTooltip(recipeName + "\n" + calories);
            Tooltip.install(this, currentTooltip);
            
            // Image placeholder
            Region imagePlaceholder = new Region();
            imagePlaceholder.setPrefHeight(90);
            imagePlaceholder.setMaxHeight(90);
            imagePlaceholder.setStyle("-fx-background-color: -fx-theme-muted; -fx-background-radius: 8px 8px 0 0;");
            VBox.setVgrow(imagePlaceholder, Priority.ALWAYS);
            
            // Content section
            VBox filledContent = new VBox(4);
            filledContent.setPadding(new Insets(10));
            filledContent.setAlignment(Pos.CENTER_LEFT);
            
            Label contentLabel = new Label(recipeName);
            contentLabel.getStyleClass().add("meal-card-title");
            contentLabel.setWrapText(true);
            
            Label calLabel = new Label(calories);
            calLabel.getStyleClass().add("meal-card-subtitle");
            
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
                viewManagerModel.setActiveView(ViewManager.MEAL_PLAN_VIEW);
            }
        }
    }
}
