package com.mealplanner.view;

import com.mealplanner.entity.MealType;
import com.mealplanner.entity.Schedule;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.view_model.ScheduleViewModel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.util.Map;

public class DashboardView extends BorderPane implements PropertyChangeListener {
    public final String viewName = "DashboardView";
    private final ViewManagerModel viewManagerModel;
    private final ScheduleViewModel scheduleViewModel;

    // Dynamic UI Components
    private HBox mealsBox;
    private ProgressBar progressBar;
    private Label progressLabel;

    public DashboardView(ViewManagerModel viewManagerModel, ScheduleViewModel scheduleViewModel) {
        this.viewManagerModel = viewManagerModel;
        this.scheduleViewModel = scheduleViewModel;
        this.scheduleViewModel.addPropertyChangeListener(this);
        
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #F5F5F5;");

        // Title
        Label titleLabel = new Label("Dashboard");
        titleLabel.getStyleClass().add("title-label");
        setTop(titleLabel);

        // Content Area (Grid)
        GridPane contentGrid = new GridPane();
        contentGrid.setHgap(20);
        contentGrid.setVgap(20);
        
        // Left: Today's Menu
        VBox menuPanel = createSectionPanel("Today's Menu");
        mealsBox = new HBox(15);
        mealsBox.setAlignment(Pos.CENTER_LEFT);
        menuPanel.getChildren().add(mealsBox);
        
        GridPane.setHgrow(menuPanel, Priority.ALWAYS);
        contentGrid.add(menuPanel, 0, 0);

        // Right: Nutrition Progress
        VBox nutritionPanel = createSectionPanel("Nutrition Progress");
        nutritionPanel.getChildren().add(createNutritionContent());
        
        GridPane.setHgrow(nutritionPanel, Priority.ALWAYS);
        contentGrid.add(nutritionPanel, 1, 0);
        
        // Column Constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(70);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(30);
        contentGrid.getColumnConstraints().addAll(col1, col2);

        setCenter(contentGrid);
        
        // Initial Update
        updateView();
    }

    private VBox createSectionPanel(String title) {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("card-panel");
        panel.setPadding(new Insets(20));
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");
        panel.getChildren().add(titleLabel);

        return panel;
    }

    private VBox createMealCard(String mealType, String mealName) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card-panel"); // Nested card look
        card.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E0E0E0; -fx-border-radius: 10;");
        card.setPrefWidth(200);
        card.setAlignment(Pos.CENTER);
        
        Label typeLabel = new Label(mealType);
        typeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50; -fx-font-size: 14px;");
        
        Label nameLabel = new Label(mealName);
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-font-size: 14px;");

        Button viewBtn = new Button("View Recipe");
        viewBtn.getStyleClass().add("modern-button");
        viewBtn.setOnAction(e -> viewManagerModel.setActiveView(ViewManager.BROWSE_RECIPE_VIEW));
        
        if (mealName.equals("Not Planned")) {
             nameLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 14px;");
             viewBtn.setText("Plan Meal");
             viewBtn.getStyleClass().add("action-button");
             viewBtn.setOnAction(e -> viewManagerModel.setActiveView(ViewManager.SCHEDULE_VIEW));
        }

        card.getChildren().addAll(typeLabel, nameLabel, viewBtn);
        return card;
    }

    private VBox createNutritionContent() {
        VBox panel = new VBox(10);
        
        progressLabel = new Label("Calories: 0 / 2000 kcal");
        progressLabel.setStyle("-fx-font-size: 16px;");
        
        progressBar = new ProgressBar(0);
        progressBar.getStyleClass().add("progress-bar");
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(20);
        
        panel.getChildren().addAll(progressLabel, progressBar);
        return panel;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(this::updateView);
    }

    private void updateView() {
        mealsBox.getChildren().clear();

        Schedule schedule = scheduleViewModel.getSchedule();
        LocalDate today = LocalDate.now();

        String breakfastName = "Not Planned";
        String lunchName = "Not Planned";
        String dinnerName = "Not Planned";
        
        int currentCalories = 0;

        if (schedule != null) {
            Map<LocalDate, Map<MealType, String>> allMeals = schedule.getAllMeals();
            Map<MealType, String> todaysMeals = allMeals.get(today);

            if (todaysMeals != null) {
                if (todaysMeals.containsKey(MealType.BREAKFAST)) {
                    breakfastName = todaysMeals.get(MealType.BREAKFAST);
                    currentCalories += 500; 
                }
                if (todaysMeals.containsKey(MealType.LUNCH)) {
                    lunchName = todaysMeals.get(MealType.LUNCH);
                    currentCalories += 700; 
                }
                if (todaysMeals.containsKey(MealType.DINNER)) {
                    dinnerName = todaysMeals.get(MealType.DINNER);
                    currentCalories += 600; 
                }
            }
        }

        mealsBox.getChildren().add(createMealCard("Breakfast", breakfastName));
        mealsBox.getChildren().add(createMealCard("Lunch", lunchName));
        mealsBox.getChildren().add(createMealCard("Dinner", dinnerName));

        double progress = (double) currentCalories / 2000.0;
        progressBar.setProgress(progress > 1.0 ? 1.0 : progress);
        progressLabel.setText("Calories: " + currentCalories + " / 2000 kcal");
    }
}
