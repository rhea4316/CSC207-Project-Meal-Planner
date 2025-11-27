package com.mealplanner.view;

import com.mealplanner.entity.MealType;
import com.mealplanner.entity.Schedule;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.ViewScheduleController;
import com.mealplanner.interface_adapter.view_model.ScheduleViewModel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

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

        setPadding(new Insets(20));
        getStyleClass().add("bg-light-gray");

        createHeader();
        createGrid();
        createFooter();
        
        updateView(); 
    }

    private void createHeader() {
        BorderPane headerPanel = new BorderPane();
        headerPanel.setPadding(new Insets(0, 0, 20, 0));

        // Title
        Label titleLabel = new Label("Weekly Plan");
        titleLabel.getStyleClass().add("title-label");
        titleLabel.setPadding(new Insets(0));
        headerPanel.setLeft(titleLabel);

        // Navigation
        HBox navPanel = new HBox(20);
        navPanel.setAlignment(Pos.CENTER);

        Button prevBtn = new Button("<");
        prevBtn.getStyleClass().add("secondary-button");
        prevBtn.setOnAction(e -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            updateView();
        });

        Button nextBtn = new Button(">");
        nextBtn.getStyleClass().add("secondary-button");
        nextBtn.setOnAction(e -> {
            currentWeekStart = currentWeekStart.plusWeeks(1);
            updateView();
        });

        dateRangeLabel = new Label();
        dateRangeLabel.getStyleClass().add("date-range-label");

        navPanel.getChildren().addAll(prevBtn, dateRangeLabel, nextBtn);
        headerPanel.setCenter(navPanel);

        setTop(headerPanel);
    }

    private void createGrid() {
        gridPane = new GridPane();
        gridPane.setHgap(1);
        gridPane.setVgap(1);
        gridPane.getStyleClass().add("grid-gap");
        
        mealSlots = new MealSlotPanel[3][7]; 

        // 1. Top Left Corner
        gridPane.add(createHeaderCell(""), 0, 0);

        // 2. Day Headers
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < 7; i++) {
            gridPane.add(createHeaderCell(days[i]), i + 1, 0);
        }

        // 3. Rows
        MealType[] mealTypes = {MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER};
        for (int r = 0; r < 3; r++) {
            gridPane.add(createHeaderCell(mealTypes[r].toString()), 0, r + 1);

            for (int c = 0; c < 7; c++) {
                MealSlotPanel slot = new MealSlotPanel(r, c);
                mealSlots[r][c] = slot;
                gridPane.add(slot, c + 1, r + 1);
            }
        }
        
        // Grow constraints
        for (int i = 0; i < 8; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setPercentWidth(12.5);
            gridPane.getColumnConstraints().add(col);
        }
        for (int i = 0; i < 4; i++) {
            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.ALWAYS);
            row.setPercentHeight(25);
            gridPane.getRowConstraints().add(row);
        }

        setCenter(gridPane);
    }
    
    private StackPane createHeaderCell(String text) {
        StackPane panel = new StackPane();
        panel.getStyleClass().add("grid-header");
        panel.getChildren().add(new Label(text));
        return panel;
    }

    private void createFooter() {
        HBox footerPanel = new HBox();
        footerPanel.setAlignment(Pos.CENTER_RIGHT);
        footerPanel.setPadding(new Insets(20, 0, 0, 0));

        Button saveButton = new Button("Save Schedule");
        saveButton.getStyleClass().add("modern-button");
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

    private class MealSlotPanel extends VBox {
        private final int row; 
        private final int col; 
        
        private final Label contentLabel;
        private final Label calLabel;
        
        public MealSlotPanel(int row, int col) {
            this.row = row;
            this.col = col;
            
            getStyleClass().add("grid-cell");
            setAlignment(Pos.CENTER);
            setSpacing(5);
            
            contentLabel = new Label("+");
            contentLabel.getStyleClass().add("meal-slot-empty");
            
            calLabel = new Label("");
            calLabel.getStyleClass().add("meal-slot-cal");

            getChildren().addAll(contentLabel, calLabel);

            setOnMouseClicked(e -> handleSlotClick());
        }

        public void setMeal(String recipeName, String calories) {
            contentLabel.setText(recipeName);
            contentLabel.getStyleClass().remove("meal-slot-empty");
            contentLabel.getStyleClass().add("meal-slot-filled");
            calLabel.setText(calories);
        }

        public void clear() {
            contentLabel.setText("+");
            contentLabel.getStyleClass().remove("meal-slot-filled");
            contentLabel.getStyleClass().add("meal-slot-empty");
            calLabel.setText("");
        }

        private void handleSlotClick() {
            LocalDate date = currentWeekStart.plusDays(col);
            MealType type = MealType.values()[row];
            
            System.out.println("Clicked: " + date + " " + type);
            // Future: Open recipe selector dialog
        }
    }
}
