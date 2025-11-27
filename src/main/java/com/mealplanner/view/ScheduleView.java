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
import java.util.Map;

public class ScheduleView extends BorderPane implements PropertyChangeListener {

    private final ScheduleViewModel scheduleViewModel;
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
        setPadding(new Insets(20));

        createHeader();
        createGrid();
        createFooter();
        
        updateView(); 
    }

    private void createHeader() {
        BorderPane headerPanel = new BorderPane();
        headerPanel.setPadding(new Insets(0, 0, 20, 0));

        // Title
        Label titleLabel = ModernUI.createHeaderLabel("Weekly Plan");
        headerPanel.setLeft(titleLabel);

        // Navigation
        HBox navPanel = new HBox(20);
        navPanel.setAlignment(Pos.CENTER);

        Button prevBtn = ModernUI.createGhostButton("<");
        prevBtn.setPrefWidth(40); // Make small buttons square-ish
        prevBtn.setOnAction(e -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            updateView();
        });

        Button nextBtn = ModernUI.createGhostButton(">");
        nextBtn.setPrefWidth(40);
        nextBtn.setOnAction(e -> {
            currentWeekStart = currentWeekStart.plusWeeks(1);
            updateView();
        });

        dateRangeLabel = new Label();
        dateRangeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        dateRangeLabel.setTextFill(ModernUI.TEXT_COLOR);

        navPanel.getChildren().addAll(prevBtn, dateRangeLabel, nextBtn);
        headerPanel.setCenter(navPanel);

        setTop(headerPanel);
    }

    private void createGrid() {
        gridPane = new GridPane();
        gridPane.setHgap(10); // 10px Gap
        gridPane.setVgap(10);
        
        mealSlots = new MealSlotPanel[3][7]; 

        // 1. Top Left Corner (Empty)
        gridPane.add(createEmptyHeader(), 0, 0);

        // 2. Day Headers
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < 7; i++) {
            gridPane.add(createDayHeader(days[i]), i + 1, 0);
        }

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
        // Column 0 (Labels): Fixed width, Cols 1-7 (Days): Grow equally
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(80);
        labelCol.setPrefWidth(100);
        gridPane.getColumnConstraints().add(labelCol);

        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setPercentWidth(12.8); // Roughly (100-label)/7
            gridPane.getColumnConstraints().add(col);
        }
        
        // Row 0 (Header): Fixed height, Rows 1-3 (Meals): Grow equally
        RowConstraints headerRow = new RowConstraints();
        headerRow.setMinHeight(40);
        headerRow.setPrefHeight(50);
        gridPane.getRowConstraints().add(headerRow);

        for (int i = 0; i < 3; i++) {
            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.ALWAYS);
            row.setPercentHeight(31); // Roughly (100-header)/3
            gridPane.getRowConstraints().add(row);
        }

        setCenter(gridPane);
    }
    
    private Pane createEmptyHeader() {
        return new Pane(); 
    }

    // 1. Header Design (Days)
    private VBox createDayHeader(String text) {
        VBox panel = new VBox();
        panel.setAlignment(Pos.CENTER);
        panel.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
        
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        label.setTextFill(ModernUI.PRIMARY_COLOR);
        
        panel.getChildren().add(label);

        // Bottom Border: 2px Green (MatteBorder equivalent)
        panel.setBorder(new Border(new BorderStroke(
            Color.TRANSPARENT, Color.TRANSPARENT, ModernUI.PRIMARY_COLOR, Color.TRANSPARENT,
            BorderStrokeStyle.NONE, BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE,
            CornerRadii.EMPTY, new BorderWidths(0, 0, 2, 0), Insets.EMPTY
        )));

        return panel;
    }

    // 3. Row Labels
    private VBox createRowHeader(String text) {
        VBox panel = new VBox();
        panel.setAlignment(Pos.CENTER_RIGHT);
        panel.setPadding(new Insets(0, 15, 0, 0)); // Right padding
        
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        label.setTextFill(Color.GRAY);
        
        panel.getChildren().add(label);
        return panel;
    }

    private void createFooter() {
        HBox footerPanel = new HBox();
        footerPanel.setAlignment(Pos.CENTER_RIGHT);
        footerPanel.setPadding(new Insets(20, 0, 0, 0));

        Button saveButton = ModernUI.createPrimaryButton("Save Schedule");
        saveButton.setOnAction(e -> controller.saveSchedule(scheduleViewModel.getSchedule()));

        footerPanel.getChildren().add(saveButton);
        setBottom(footerPanel);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(this::updateView);
    }

    private void updateView() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");
        LocalDate weekEnd = currentWeekStart.plusDays(6);
        dateRangeLabel.setText(currentWeekStart.format(formatter) + " - " + weekEnd.format(formatter));

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
                        mealSlots[0][i].setMeal(mealsForDate.get(MealType.BREAKFAST), "500 kcal");
                    if (mealsForDate.containsKey(MealType.LUNCH)) 
                        mealSlots[1][i].setMeal(mealsForDate.get(MealType.LUNCH), "700 kcal");
                    if (mealsForDate.containsKey(MealType.DINNER)) 
                        mealSlots[2][i].setMeal(mealsForDate.get(MealType.DINNER), "600 kcal");
                }
            }
        }
    }

    // 2. Grid & Cells (Cards)
    private class MealSlotPanel extends VBox {
        private final int row; 
        private final int col; 
        
        private final Label contentLabel;
        private final Label calLabel;
        
        private boolean isFilled = false;

        public MealSlotPanel(int row, int col) {
            this.row = row;
            this.col = col;
            
            // Base Style: ModernUI Card
            VBox card = ModernUI.createCardPanel();
            // Copy style properties from factory to this VBox
            this.setBackground(card.getBackground());
            this.setBorder(card.getBorder());
            this.setEffect(card.getEffect());
            this.setPadding(new Insets(10)); // Tighter padding for grid
            
            setAlignment(Pos.CENTER);
            setSpacing(5);
            setCursor(Cursor.HAND);
            
            contentLabel = new Label("+");
            contentLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
            contentLabel.setTextFill(Color.LIGHTGRAY);
            
            calLabel = new Label("Add");
            calLabel.setFont(Font.font("Segoe UI", 12));
            calLabel.setTextFill(Color.TRANSPARENT); // Hidden initially

            getChildren().addAll(contentLabel, calLabel);

            // Hover Effects
            setOnMouseEntered(e -> {
                if (!isFilled) {
                    setBackground(new Background(new BackgroundFill(Color.web("#E8F5E9"), new CornerRadii(15), Insets.EMPTY)));
                    contentLabel.setTextFill(ModernUI.PRIMARY_COLOR);
                    calLabel.setTextFill(ModernUI.PRIMARY_COLOR);
                }
            });

            setOnMouseExited(e -> {
                if (!isFilled) {
                    setBackground(new Background(new BackgroundFill(ModernUI.SURFACE_COLOR, new CornerRadii(15), Insets.EMPTY)));
                    contentLabel.setTextFill(Color.LIGHTGRAY);
                    calLabel.setTextFill(Color.TRANSPARENT);
                }
            });

            setOnMouseClicked(e -> handleSlotClick());
        }

        public void setMeal(String recipeName, String calories) {
            this.isFilled = true;
            
            contentLabel.setText(recipeName);
            contentLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            contentLabel.setTextFill(ModernUI.TEXT_COLOR);
            contentLabel.setWrapText(true);
            
            calLabel.setText(calories);
            calLabel.setFont(Font.font("Segoe UI", 10));
            calLabel.setTextFill(Color.GRAY);
            
            // Reset Background
            setBackground(new Background(new BackgroundFill(ModernUI.SURFACE_COLOR, new CornerRadii(15), Insets.EMPTY)));
        }

        public void clear() {
            this.isFilled = false;
            
            contentLabel.setText("+");
            contentLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
            contentLabel.setTextFill(Color.LIGHTGRAY);
            
            calLabel.setText("Add");
            calLabel.setTextFill(Color.TRANSPARENT);
            
             // Reset Background
            setBackground(new Background(new BackgroundFill(ModernUI.SURFACE_COLOR, new CornerRadii(15), Insets.EMPTY)));
        }

        private void handleSlotClick() {
            LocalDate date = currentWeekStart.plusDays(col);
            MealType type = MealType.values()[row];
            
            System.out.println("Clicked: " + date + " " + type);
            // In a real app, you might show a pop-up or navigate to search
            // For now, just print to console
        }
    }
}
