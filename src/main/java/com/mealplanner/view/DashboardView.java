package com.mealplanner.view;

import com.mealplanner.entity.MealType;
import com.mealplanner.entity.Schedule;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.view_model.ScheduleViewModel;
import com.mealplanner.view.style.ModernUI;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.util.Map;

public class DashboardView extends BorderPane implements PropertyChangeListener {
    public final String viewName = "DashboardView";
    private final ViewManagerModel viewManagerModel;
    private final ScheduleViewModel scheduleViewModel;

    // Dynamic UI Components
    private VBox mealsContainer;
    private ProgressBar progressBar;
    private Label calorieValueLabel; // e.g. "1,200 / 2,000"

    public DashboardView(ViewManagerModel viewManagerModel, ScheduleViewModel scheduleViewModel) {
        this.viewManagerModel = viewManagerModel;
        this.scheduleViewModel = scheduleViewModel;
        this.scheduleViewModel.addPropertyChangeListener(this);

        // 1. Layout & Background
        setBackground(new Background(new BackgroundFill(
            ModernUI.BACKGROUND_COLOR, 
            CornerRadii.EMPTY, 
            Insets.EMPTY
        )));
        setPadding(new Insets(30));

        // Title
        Label titleLabel = ModernUI.createHeaderLabel("Dashboard");
        setTop(titleLabel);
        BorderPane.setMargin(titleLabel, new Insets(0, 0, 20, 0));

        // Content Grid
        GridPane contentGrid = new GridPane();
        contentGrid.setHgap(30); // 30px Gap
        contentGrid.setVgap(30);

        // Left: Today's Menu
        VBox menuSection = createSectionHeader("Today's Menu");
        mealsContainer = new VBox(20); // Spacing between cards
        menuSection.getChildren().add(mealsContainer);
        
        GridPane.setHgrow(menuSection, Priority.ALWAYS);
        contentGrid.add(menuSection, 0, 0);

        // Right: Nutrition Progress
        VBox nutritionSection = createSectionHeader("Nutrition Progress");
        nutritionSection.getChildren().add(createNutritionContent());
        
        GridPane.setHgrow(nutritionSection, Priority.ALWAYS);
        contentGrid.add(nutritionSection, 1, 0);

        // Column Constraints (70% Left, 30% Right)
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(70);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(30);
        contentGrid.getColumnConstraints().addAll(col1, col2);

        setCenter(contentGrid);

        // Initial Update
        updateView();
    }

    private VBox createSectionHeader(String title) {
        VBox container = new VBox(15);
        Label label = new Label(title);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        label.setTextFill(ModernUI.TEXT_COLOR);
        container.getChildren().add(label);
        return container;
    }

    private VBox createNutritionContent() {
        VBox card = ModernUI.createCardPanel();
        card.setSpacing(15);
        card.setAlignment(Pos.CENTER_LEFT);

        // Header
        Label header = new Label("Total Calories");
        header.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        header.setTextFill(Color.GRAY);

        // Value
        calorieValueLabel = new Label("0 / 2000");
        calorieValueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        calorieValueLabel.setTextFill(ModernUI.PRIMARY_COLOR);

        // Progress Bar
        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(20);
        // CSS styling for rounded corners is tricky purely in code without CSS file update,
        // but we can try inline styles for basic properties.
        progressBar.setStyle("-fx-accent: " + toHexString(ModernUI.PRIMARY_COLOR) + "; -fx-control-inner-background: #E0E0E0; -fx-background-radius: 10; -fx-background-insets: 0; -fx-padding: 0;");

        card.getChildren().addAll(header, calorieValueLabel, progressBar);
        return card;
    }

    private VBox createMealCard(String mealType, String mealName, String icon, int calories) {
        VBox card = ModernUI.createCardPanel();
        card.setSpacing(10);

        // Header Row: Icon + Meal Type
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Segoe UI Emoji", 20)); // Use Emoji font if available
        
        Label typeLabel = new Label(mealType);
        typeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        typeLabel.setTextFill(Color.GRAY);
        
        headerBox.getChildren().addAll(iconLabel, typeLabel);
        card.getChildren().add(headerBox);

        // Content
        boolean isPlanned = !mealName.equals("Not Planned") && mealName != null && !mealName.isEmpty();

        if (isPlanned) {
            // Meal Name
            Label nameLabel = new Label(mealName);
            nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
            nameLabel.setTextFill(ModernUI.TEXT_COLOR);
            nameLabel.setWrapText(true);

            // Calories (Mock)
            Label calLabel = new Label(calories + " kcal");
            calLabel.setFont(Font.font("Segoe UI", 12));
            calLabel.setTextFill(Color.GRAY);

            // Action Button
            Button viewBtn = ModernUI.createPrimaryButton("View Recipe");
            viewBtn.setMaxWidth(Double.MAX_VALUE);
            viewBtn.setOnAction(e -> viewManagerModel.setActiveView(ViewManager.BROWSE_RECIPE_VIEW));

            card.getChildren().addAll(nameLabel, calLabel, viewBtn);
        } else {
            // Empty State
            Label emptyLabel = new Label("Not Planned");
            emptyLabel.setFont(Font.font("Segoe UI", FontPosture.ITALIC, 14));
            emptyLabel.setTextFill(Color.GRAY);

            Button planBtn = ModernUI.createGhostButton("Plan Meal");
            planBtn.setMaxWidth(Double.MAX_VALUE);
            planBtn.setOnAction(e -> viewManagerModel.setActiveView(ViewManager.SCHEDULE_VIEW));

            card.getChildren().addAll(emptyLabel, planBtn);
        }

        return card;
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(this::updateView);
    }

    private void updateView() {
        mealsContainer.getChildren().clear();

        Schedule schedule = scheduleViewModel.getSchedule();
        LocalDate today = LocalDate.now();

        String breakfastName = "Not Planned";
        String lunchName = "Not Planned";
        String dinnerName = "Not Planned";
        
        int currentCalories = 0;
        // Estimated calories for display (matching previous logic)
        int breakfastCals = 0;
        int lunchCals = 0;
        int dinnerCals = 0;

        if (schedule != null) {
            Map<LocalDate, Map<MealType, String>> allMeals = schedule.getAllMeals();
            Map<MealType, String> todaysMeals = allMeals.get(today);

            if (todaysMeals != null) {
                if (todaysMeals.containsKey(MealType.BREAKFAST)) {
                    breakfastName = todaysMeals.get(MealType.BREAKFAST);
                    breakfastCals = 500;
                    currentCalories += breakfastCals;
                }
                if (todaysMeals.containsKey(MealType.LUNCH)) {
                    lunchName = todaysMeals.get(MealType.LUNCH);
                    lunchCals = 700;
                    currentCalories += lunchCals;
                }
                if (todaysMeals.containsKey(MealType.DINNER)) {
                    dinnerName = todaysMeals.get(MealType.DINNER);
                    dinnerCals = 600;
                    currentCalories += dinnerCals;
                }
            }
        }

        // Add Cards with Icons
        mealsContainer.getChildren().add(createMealCard("Breakfast", breakfastName, "☀", breakfastCals));
        mealsContainer.getChildren().add(createMealCard("Lunch", lunchName, "☁", lunchCals));
        mealsContainer.getChildren().add(createMealCard("Dinner", dinnerName, "☾", dinnerCals));

        // Update Nutrition Progress
        double progress = (double) currentCalories / 2000.0;
        progressBar.setProgress(progress > 1.0 ? 1.0 : progress);
        calorieValueLabel.setText(currentCalories + " / 2000");

        // Dynamic Color for Progress Bar
        String colorHex;
        if (progress > 1.0) {
            colorHex = "#F44336"; // Red
        } else if (progress > 0.8) {
            colorHex = "#FF9800"; // Orange
        } else {
            colorHex = "#4CAF50"; // Green
        }
        
        // Update bar color
        progressBar.setStyle("-fx-accent: " + colorHex + "; -fx-control-inner-background: #E0E0E0; -fx-background-radius: 10; -fx-padding: 0;");
    }
}
