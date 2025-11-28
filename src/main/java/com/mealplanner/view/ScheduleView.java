package com.mealplanner.view;

import com.mealplanner.entity.MealType;
import com.mealplanner.entity.Schedule;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.ViewScheduleController;
import com.mealplanner.interface_adapter.view_model.ScheduleViewModel;
import com.mealplanner.view.style.ModernUI;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.Cursor;

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
    @SuppressWarnings("unused")
    private final ViewScheduleController controller;
    @SuppressWarnings("unused")
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

        // ModernUI Background
        setBackground(new Background(new BackgroundFill(
            ModernUI.BACKGROUND_COLOR, 
            CornerRadii.EMPTY, 
            Insets.EMPTY
        )));
        setPadding(new Insets(30, 40, 30, 40));

        createHeader();
        createGrid();
        createFooter();
        
        updateView(); 
    }

    private void createHeader() {
        HBox headerPanel = new HBox();
        headerPanel.setAlignment(Pos.CENTER_LEFT);
        headerPanel.setSpacing(20);
        headerPanel.setPadding(new Insets(0, 0, 30, 0));

        // Date Range Label
        dateRangeLabel = new Label();
        dateRangeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        dateRangeLabel.setTextFill(ModernUI.TEXT_COLOR);
        HBox.setHgrow(dateRangeLabel, Priority.ALWAYS);

        // Create New Plan Button
        Button createButton = ModernUI.createPrimaryButton("Create New Plan");
        createButton.setOnAction(e -> {
            // Navigate to meal plan view or show dialog
            viewManagerModel.setActiveView(ViewManager.MEAL_PLAN_VIEW);
        });

        headerPanel.getChildren().addAll(dateRangeLabel, createButton);
        setTop(headerPanel);
    }

    private void createGrid() {
        gridPane = new GridPane();
        gridPane.setHgap(16);
        gridPane.setVgap(16);
        
        mealSlots = new MealSlotPanel[3][7]; 

        // 1. Top Left Corner (Empty)
        gridPane.add(createEmptyHeader(), 0, 0);

        // 2. Day Headers (will be updated in updateView)
        updateDayHeaders();

        // 3. Rows
        // MealType[] mealTypes = {MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER};
        String[] mealLabels = {"Breakfast", "Lunch", "Dinner"};
        
        for (int r = 0; r < 3; r++) {
            // Row Label
            gridPane.add(createRowHeader(mealLabels[r]), 0, r + 1);

            for (int c = 0; c < 7; c++) {
                MealSlotPanel slot = new MealSlotPanel(r, c);
                mealSlots[r][c] = slot;
                gridPane.add(slot, c + 1, r + 1);
            }
        }
        
        // Grow constraints
        // Column 0 (Labels): Fixed width 80px, Cols 1-7 (Days): Grow equally
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
        
        // Row 0 (Header): Fixed height, Rows 1-3 (Meals): Grow equally
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
    
    private Pane createEmptyHeader() {
        return new Pane(); 
    }

    // 1. Header Design (Days with dates)
    private VBox createDayHeader(String dayName, LocalDate date, boolean isActive) {
        VBox panel = new VBox(4);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(0, 0, 10, 0));
        panel.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
        
        // Day Name (Mon, Tue, etc.)
        Label dayLabel = new Label(dayName);
        dayLabel.setFont(Font.font("Segoe UI", 14));
        dayLabel.setTextFill(ModernUI.TEXT_LIGHT);
        
        // Date Number
        Label dateLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dateLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        dateLabel.setTextFill(ModernUI.TEXT_COLOR);
        
        panel.getChildren().addAll(dayLabel, dateLabel);

        // Bottom Border: 3px Green for active days
        if (isActive) {
            panel.setBorder(new Border(new BorderStroke(
                Color.TRANSPARENT, Color.TRANSPARENT, ModernUI.PRIMARY_COLOR, Color.TRANSPARENT,
                BorderStrokeStyle.NONE, BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE,
                CornerRadii.EMPTY, new BorderWidths(0, 0, 3, 0), Insets.EMPTY
            )));
        }

        return panel;
    }

    // 3. Row Labels
    private VBox createRowHeader(String text) {
        VBox panel = new VBox();
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(0, 0, 0, 0));
        
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        label.setTextFill(ModernUI.TEXT_LIGHT);
        
        panel.getChildren().add(label);
        return panel;
    }

    private void createFooter() {
        // Footer removed - Create New Plan button is in header
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(this::updateView);
    }

    private void updateDayHeaders() {
        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        LocalDate today = LocalDate.now();
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = currentWeekStart.plusDays(i);
            boolean isActive = date.equals(today) || date.equals(today.plusDays(1)) || date.equals(today.minusDays(1));
            gridPane.add(createDayHeader(dayNames[i], date, isActive), i + 1, 0);
        }
    }

    private void updateView() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d", Locale.ENGLISH);
        LocalDate weekEnd = currentWeekStart.plusDays(6);
        dateRangeLabel.setText("Weekly Plan: " + currentWeekStart.format(formatter) + " - " + weekEnd.format(formatter) + ", " + currentWeekStart.getYear());
        
        // Update day headers
        updateDayHeaders();

        // Clear all slots
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 7; c++) {
                mealSlots[r][c].clear();
            }
        }

        // Populate with schedule data
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

    // 2. Grid & Cells (Cards)
    private class MealSlotPanel extends VBox {
        @SuppressWarnings("unused")
        private final int row; 
        @SuppressWarnings("unused")
        private final int col; 
        
        private Label contentLabel;
        private Label calLabel;
        private Region imagePlaceholder;
        private VBox emptyContent;
        private VBox filledContent;
        
        private boolean isFilled = false;

        public MealSlotPanel(int row, int col) {
            this.row = row;
            this.col = col;
            
            setAlignment(Pos.CENTER);
            setSpacing(0);
            setCursor(Cursor.HAND);
            setMinHeight(160);
            setPrefHeight(160);
            
            // Empty state (default)
            setupEmptyState();
            
            // Hover Effects
            setOnMouseEntered(e -> {
                if (!isFilled) {
                    setBackground(new Background(new BackgroundFill(
                        Color.web("#F0FDF4"), 
                        new CornerRadii(12), 
                        Insets.EMPTY
                    )));
                    setBorder(new Border(new BorderStroke(
                        ModernUI.PRIMARY_COLOR,
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(12),
                        new BorderWidths(2)
                    )));
                    if (contentLabel != null) {
                        contentLabel.setTextFill(ModernUI.PRIMARY_COLOR);
                    }
                }
            });

            setOnMouseExited(e -> {
                if (!isFilled) {
                    setBackground(new Background(new BackgroundFill(
                        Color.TRANSPARENT, 
                        new CornerRadii(12), 
                        Insets.EMPTY
                    )));
                    setBorder(new Border(new BorderStroke(
                        Color.web("#D1D5DB"),
                        BorderStrokeStyle.DASHED,
                        new CornerRadii(12),
                        new BorderWidths(2)
                    )));
                    if (contentLabel != null) {
                        contentLabel.setTextFill(Color.web("#9CA3AF"));
                    }
                }
            });

            setOnMouseClicked(e -> handleSlotClick());
        }
        
        private void setupEmptyState() {
            getChildren().clear();
            
            emptyContent = new VBox(5);
            emptyContent.setAlignment(Pos.CENTER);
            
            contentLabel = new Label("+");
            contentLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
            contentLabel.setTextFill(Color.web("#9CA3AF"));
            
            Label planLabel = new Label("Plan Meal");
            planLabel.setFont(Font.font("Segoe UI", 14));
            planLabel.setTextFill(Color.web("#9CA3AF"));
            
            emptyContent.getChildren().addAll(contentLabel, planLabel);
            getChildren().add(emptyContent);
            
            // Dashed border for empty state
            setBackground(new Background(new BackgroundFill(
                Color.TRANSPARENT, 
                new CornerRadii(12), 
                Insets.EMPTY
            )));
            setBorder(new Border(new BorderStroke(
                Color.web("#D1D5DB"),
                BorderStrokeStyle.DASHED,
                new CornerRadii(12),
                new BorderWidths(2)
            )));
        }

        public void setMeal(String recipeName, String calories) {
            this.isFilled = true;
            getChildren().clear();
            
            // Image placeholder
            imagePlaceholder = new Region();
            imagePlaceholder.setPrefHeight(90);
            imagePlaceholder.setMaxHeight(90);
            imagePlaceholder.setBackground(new Background(new BackgroundFill(
                Color.web("#E5E7EB"),
                new CornerRadii(8, 8, 0, 0, false),
                Insets.EMPTY
            )));
            HBox.setHgrow(imagePlaceholder, Priority.ALWAYS);
            
            // Content section
            filledContent = new VBox(4);
            filledContent.setPadding(new Insets(10));
            filledContent.setAlignment(Pos.CENTER_LEFT);
            
            contentLabel = new Label(recipeName);
            contentLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            contentLabel.setTextFill(ModernUI.TEXT_COLOR);
            contentLabel.setWrapText(true);
            contentLabel.setMaxWidth(Double.MAX_VALUE);
            
            calLabel = new Label(calories);
            calLabel.setFont(Font.font("Segoe UI", 12));
            calLabel.setTextFill(ModernUI.TEXT_LIGHT);
            
            filledContent.getChildren().addAll(contentLabel, calLabel);
            
            getChildren().addAll(imagePlaceholder, filledContent);
            
            // Filled card style
            setBackground(new Background(new BackgroundFill(
                ModernUI.SURFACE_COLOR, 
                new CornerRadii(12), 
                Insets.EMPTY
            )));
            setBorder(new Border(new BorderStroke(
                Color.web("#E5E7EB"),
                BorderStrokeStyle.SOLID,
                new CornerRadii(12),
                new BorderWidths(1)
            )));
            setEffect(new javafx.scene.effect.DropShadow(
                javafx.scene.effect.BlurType.GAUSSIAN,
                Color.rgb(0, 0, 0, 0.05),
                4, 0, 0, 2
            ));
        }

        public void clear() {
            this.isFilled = false;
            setupEmptyState();
            setEffect(null);
        }

        private void handleSlotClick() {
            if (isFilled) {
                // Navigate to recipe detail or edit
                viewManagerModel.setActiveView(ViewManager.RECIPE_DETAIL_VIEW);
            } else {
                // Navigate to meal plan view to add meal
                viewManagerModel.setActiveView(ViewManager.MEAL_PLAN_VIEW);
            }
        }
    }
}
