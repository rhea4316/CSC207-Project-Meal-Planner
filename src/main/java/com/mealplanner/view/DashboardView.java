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
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
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
    private HBox mealsContainer; // Changed to HBox for horizontal layout
    private Label calorieValueLabel; // e.g. "1,200 / 2,000"
    private StackPane circularProgressPane;
    private Label remainingCaloriesLabel;
    private ProgressBar proteinBar, carbsBar, fatBar;

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
        setPadding(new Insets(30, 40, 30, 40));

        // Header with Search and Profile
        HBox header = createHeader();
        setTop(header);
        BorderPane.setMargin(header, new Insets(0, 0, 30, 0));

        // Welcome Title
        Label welcomeLabel = new Label("Welcome back, Eden!");
        welcomeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        welcomeLabel.setTextFill(ModernUI.TEXT_COLOR);
        VBox titleBox = new VBox(welcomeLabel);
        titleBox.setPadding(new Insets(0, 0, 20, 0));
        setTop(new VBox(header, titleBox));
        BorderPane.setMargin(titleBox, new Insets(20, 0, 0, 0));

        // Content Grid (2:1 ratio)
        GridPane contentGrid = new GridPane();
        contentGrid.setHgap(24);
        contentGrid.setVgap(24);

        // Left Column (2/3 width)
        VBox leftColumn = new VBox(24);
        leftColumn.setPrefWidth(Region.USE_COMPUTED_SIZE);
        
        // Today's Menu Section
        VBox menuSection = createTodayMenuSection();
        leftColumn.getChildren().add(menuSection);
        
        // Recipe Suggestions Section
        VBox recipeSection = createRecipeSuggestionsSection();
        leftColumn.getChildren().add(recipeSection);

        // Right Column (1/3 width)
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

        // Column Constraints (2:1 ratio)
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(66.67);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(33.33);
        contentGrid.getColumnConstraints().addAll(col1, col2);

        setCenter(contentGrid);

        // Initial Update
        updateView();
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);
        HBox.setHgrow(header, Priority.ALWAYS);

        // Search Bar
        TextField searchBar = new TextField();
        searchBar.setPromptText("Search recipes...");
        searchBar.getStyleClass().add("search-bar");
        searchBar.setPrefWidth(400);
        HBox.setHgrow(searchBar, Priority.NEVER);

        // User Profile
        Label userProfile = new Label("👤 Eden Chang ⌄");
        userProfile.getStyleClass().add("user-profile");
        userProfile.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));

        header.getChildren().addAll(searchBar, userProfile);
        return header;
    }

    private VBox createTodayMenuSection() {
        VBox container = ModernUI.createCardPanel();
        container.setSpacing(16);

        // Section Title
        Label title = new Label("Today's Menu");
        title.getStyleClass().add("section-title");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setTextFill(ModernUI.TEXT_COLOR);

        // Meals Container (Horizontal Grid)
        mealsContainer = new HBox(16);
        mealsContainer.setAlignment(Pos.CENTER);
        mealsContainer.setPrefHeight(250);

        container.getChildren().addAll(title, mealsContainer);
        return container;
    }

    private VBox createNutritionProgressSection() {
        VBox container = ModernUI.createCardPanel();
        container.setSpacing(20);
        container.setAlignment(Pos.CENTER);

        // Section Title
        Label title = new Label("Nutrition Progress");
        title.getStyleClass().add("section-title");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setTextFill(ModernUI.TEXT_COLOR);
        title.setAlignment(Pos.CENTER_LEFT);

        // Circular Progress
        circularProgressPane = createCircularProgress();
        
        // Remaining Calories
        remainingCaloriesLabel = new Label("Remaining: 750");
        remainingCaloriesLabel.setFont(Font.font("Segoe UI", 14));
        remainingCaloriesLabel.setTextFill(ModernUI.TEXT_LIGHT);

        // Nutrient Bars
        VBox nutrientBars = new VBox(12);
        
        // Protein
        VBox proteinBox = createNutrientBar("Protein (75g / 120g)", 0.625);
        proteinBar = (ProgressBar) proteinBox.getChildren().get(1);
        
        // Carbs
        VBox carbsBox = createNutrientBar("Carbs (150g / 250g)", 0.60);
        carbsBar = (ProgressBar) carbsBox.getChildren().get(1);
        
        // Fat
        VBox fatBox = createNutrientBar("Fat (45g / 70g)", 0.643);
        fatBar = (ProgressBar) fatBox.getChildren().get(1);

        nutrientBars.getChildren().addAll(proteinBox, carbsBox, fatBox);

        container.getChildren().addAll(title, circularProgressPane, remainingCaloriesLabel, nutrientBars);
        return container;
    }

    private StackPane createCircularProgress() {
        StackPane stackPane = new StackPane();
        stackPane.setPrefSize(150, 150);

        // Outer circle (background)
        Circle backgroundCircle = new Circle(75);
        backgroundCircle.setFill(Color.web("#E5E7EB"));
        backgroundCircle.setStroke(Color.TRANSPARENT);

        // Progress arc (62% = 223.2 degrees)
        Arc progressArc = new Arc(0, 0, 70, 70, 90, -223.2); // Start at top, go clockwise
        progressArc.setType(ArcType.ROUND);
        progressArc.setFill(ModernUI.PRIMARY_COLOR);
        progressArc.setStrokeWidth(0);

        // Inner circle (white background for text)
        Circle innerCircle = new Circle(65);
        innerCircle.setFill(Color.WHITE);

        // Text in center
        VBox textBox = new VBox(5);
        textBox.setAlignment(Pos.CENTER);
        calorieValueLabel = new Label("1250 / 2000");
        calorieValueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        calorieValueLabel.setTextFill(ModernUI.TEXT_COLOR);
        
        Label caloriesLabel = new Label("Calories");
        caloriesLabel.setFont(Font.font("Segoe UI", 12));
        caloriesLabel.setTextFill(ModernUI.TEXT_LIGHT);
        
        textBox.getChildren().addAll(calorieValueLabel, caloriesLabel);

        stackPane.getChildren().addAll(backgroundCircle, progressArc, innerCircle, textBox);
        return stackPane;
    }

    private VBox createNutrientBar(String labelText, double progress) {
        VBox container = new VBox(4);
        
        Label label = new Label(labelText);
        label.setFont(Font.font("Segoe UI", 14));
        label.setTextFill(ModernUI.TEXT_COLOR);

        ProgressBar bar = new ProgressBar(progress);
        bar.setPrefHeight(8);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.getStyleClass().add("nutrient-progress-bar");
        bar.setStyle("-fx-accent: " + toHexString(ModernUI.PRIMARY_COLOR) + ";");

        container.getChildren().addAll(label, bar);
        return container;
    }

    private VBox createRecipeSuggestionsSection() {
        VBox container = ModernUI.createCardPanel();
        container.setSpacing(16);

        // Section Title
        Label title = new Label("Recipe Suggestions");
        title.getStyleClass().add("section-title");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setTextFill(ModernUI.TEXT_COLOR);

        // Recipe Grid (2 columns)
        GridPane recipeGrid = new GridPane();
        recipeGrid.setHgap(16);
        recipeGrid.setVgap(16);

        // Sample recipes
        HBox recipe1 = createRecipeItem("Quinoa Bowl", "350 kcal");
        HBox recipe2 = createRecipeItem("Salmon with Veggies", "450 kcal");

        recipeGrid.add(recipe1, 0, 0);
        recipeGrid.add(recipe2, 1, 0);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        recipeGrid.getColumnConstraints().addAll(col1, col2);

        container.getChildren().addAll(title, recipeGrid);
        return container;
    }

    private HBox createRecipeItem(String name, String calories) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);

        // Placeholder image
        Region imagePlaceholder = new Region();
        imagePlaceholder.setPrefSize(60, 60);
        imagePlaceholder.setBackground(new Background(new BackgroundFill(
            Color.web("#E5E7EB"),
            new CornerRadii(8),
            Insets.EMPTY
        )));

        VBox textBox = new VBox(4);
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        nameLabel.setTextFill(ModernUI.TEXT_COLOR);
        
        Label calLabel = new Label(calories);
        calLabel.setFont(Font.font("Segoe UI", 12));
        calLabel.setTextFill(ModernUI.TEXT_LIGHT);

        textBox.getChildren().addAll(nameLabel, calLabel);
        item.getChildren().addAll(imagePlaceholder, textBox);
        return item;
    }

    private VBox createQuickActionsSection() {
        VBox container = ModernUI.createCardPanel();
        container.setSpacing(16);

        // Section Title
        Label title = new Label("Quick Actions");
        title.getStyleClass().add("section-title");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setTextFill(ModernUI.TEXT_COLOR);

        // Action Buttons
        VBox actionButtons = new VBox(10);
        
        Button addSnack = new Button("Add Snack");
        addSnack.getStyleClass().add("action-button");
        addSnack.setMaxWidth(Double.MAX_VALUE);
        
        Button logWater = new Button("Log Water");
        logWater.getStyleClass().add("action-button");
        logWater.setMaxWidth(Double.MAX_VALUE);
        
        Button createRecipe = new Button("Create Recipe");
        createRecipe.getStyleClass().add("action-button");
        createRecipe.setMaxWidth(Double.MAX_VALUE);
        createRecipe.setOnAction(e -> viewManagerModel.setActiveView(ViewManager.STORE_RECIPE_VIEW));

        actionButtons.getChildren().addAll(addSnack, logWater, createRecipe);

        container.getChildren().addAll(title, actionButtons);
        return container;
    }

    private VBox createMealCard(String mealType, String mealName, String icon, int calories) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);
        card.setMinHeight(250);
        
        // Style based on whether meal is planned
        boolean isPlanned = !mealName.equals("Not Planned") && mealName != null && !mealName.isEmpty();
        
        if (isPlanned) {
            card.getStyleClass().add("meal-card");
            card.getStyleClass().add("active");
            card.setBackground(new Background(new BackgroundFill(
                Color.web("#D1FAE5"),
                new CornerRadii(12),
                Insets.EMPTY
            )));
            card.setBorder(new Border(new BorderStroke(
                ModernUI.PRIMARY_COLOR,
                BorderStrokeStyle.SOLID,
                new CornerRadii(12),
                new BorderWidths(1)
            )));
        } else {
            card.getStyleClass().add("meal-card");
            card.setBackground(new Background(new BackgroundFill(
                Color.WHITE,
                new CornerRadii(12),
                Insets.EMPTY
            )));
            card.setBorder(new Border(new BorderStroke(
                Color.web("#E5E7EB"),
                BorderStrokeStyle.SOLID,
                new CornerRadii(12),
                new BorderWidths(1)
            )));
        }

        if (isPlanned) {
            // Image placeholder (in real app, would load actual recipe image)
            Region imagePlaceholder = new Region();
            imagePlaceholder.setPrefSize(100, 100);
            imagePlaceholder.setBackground(new Background(new BackgroundFill(
                Color.web("#E5E7EB"),
                new CornerRadii(8),
                Insets.EMPTY
            )));
            
            // Meal Name
            Label nameLabel = new Label(mealName);
            nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
            nameLabel.setTextFill(ModernUI.TEXT_COLOR);
            nameLabel.setWrapText(true);
            nameLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

            // Calories
            Label calLabel = new Label(calories + " kcal");
            calLabel.setFont(Font.font("Segoe UI", 14));
            calLabel.setTextFill(ModernUI.TEXT_LIGHT);

            // Action Button
            Button viewBtn = new Button("View Recipe");
            viewBtn.getStyleClass().add("secondary-button");
            viewBtn.setMaxWidth(Double.MAX_VALUE);
            viewBtn.setOnAction(e -> viewManagerModel.setActiveView(ViewManager.BROWSE_RECIPE_VIEW));

            VBox.setVgrow(imagePlaceholder, Priority.ALWAYS);
            card.getChildren().addAll(imagePlaceholder, nameLabel, calLabel, viewBtn);
        } else {
            // Empty State
            Label iconLabel = new Label(icon);
            iconLabel.setFont(Font.font("Segoe UI Emoji", 48));
            
            Label typeLabel = new Label(mealType);
            typeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
            typeLabel.setTextFill(ModernUI.TEXT_COLOR);
            
            Label emptyLabel = new Label("Not Planned");
            emptyLabel.setFont(Font.font("Segoe UI", FontPosture.ITALIC, 14));
            emptyLabel.setTextFill(ModernUI.TEXT_LIGHT);

            VBox buttonBox = new VBox(5);
            Button planBtn = ModernUI.createPrimaryButton("Plan Meal");
            planBtn.setMaxWidth(Double.MAX_VALUE);
            planBtn.setOnAction(e -> viewManagerModel.setActiveView(ViewManager.SCHEDULE_VIEW));
            
            Button generateBtn = new Button("Generate");
            generateBtn.getStyleClass().add("secondary-button");
            generateBtn.setMaxWidth(Double.MAX_VALUE);
            
            buttonBox.getChildren().addAll(planBtn, generateBtn);
            VBox.setVgrow(iconLabel, Priority.ALWAYS);
            card.getChildren().addAll(iconLabel, typeLabel, emptyLabel, buttonBox);
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
                    lunchCals = 550; // Example: Grilled Chicken Salad
                    currentCalories += lunchCals;
                }
                if (todaysMeals.containsKey(MealType.DINNER)) {
                    dinnerName = todaysMeals.get(MealType.DINNER);
                    dinnerCals = 600;
                    currentCalories += dinnerCals;
                }
            }
        }

        // Add Cards with Icons (Horizontal layout)
        mealsContainer.getChildren().add(createMealCard("Breakfast", breakfastName, "🍳", breakfastCals));
        mealsContainer.getChildren().add(createMealCard("Lunch", lunchName, "🥗", lunchCals));
        mealsContainer.getChildren().add(createMealCard("Dinner", dinnerName, "🥩", dinnerCals));

        // Update Nutrition Progress
        int targetCalories = 2000;
        double progress = Math.min((double) currentCalories / targetCalories, 1.0);
        int remaining = Math.max(targetCalories - currentCalories, 0);
        
        // Update circular progress
        calorieValueLabel.setText(currentCalories + " / " + targetCalories);
        remainingCaloriesLabel.setText("Remaining: " + remaining);
        
        // Update circular arc (progress * 360 degrees, starting from top)
        if (circularProgressPane != null && circularProgressPane.getChildren().size() > 1) {
            Arc progressArc = (Arc) circularProgressPane.getChildren().get(1);
            double angle = progress * 360.0;
            progressArc.setLength(-angle); // Negative for clockwise
        }
        
        // Update nutrient bars (example values - in real app, calculate from actual nutrition data)
        if (proteinBar != null) proteinBar.setProgress(0.625);
        if (carbsBar != null) carbsBar.setProgress(0.60);
        if (fatBar != null) fatBar.setProgress(0.643);
    }
}
