package com.mealplanner.view;

import com.mealplanner.entity.MealType;
import com.mealplanner.entity.Schedule;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.view_model.ScheduleViewModel;
import com.mealplanner.view.component.*;
import com.mealplanner.view.util.SvgIconLoader;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.util.Map;

public class DashboardView extends BorderPane implements PropertyChangeListener {
    public final String viewName = "DashboardView";
    private final ViewManagerModel viewManagerModel;
    private final ScheduleViewModel scheduleViewModel;

    // Dynamic UI Components
    private HBox mealsContainer;
    private Label calorieValueLabel; // e.g. "1,200 / 2,000"
    private StackPane circularProgressPane;
    private Label remainingCaloriesLabel;
    private Progress proteinBar, carbsBar, fatBar;

    public DashboardView(ViewManagerModel viewManagerModel, ScheduleViewModel scheduleViewModel) {
        this.viewManagerModel = viewManagerModel;
        this.scheduleViewModel = scheduleViewModel;
        this.scheduleViewModel.addPropertyChangeListener(this);

        // 1. Layout & Background
        setPadding(new Insets(30, 40, 30, 40));

        // Header with Search and Profile
        HBox header = createHeader();
        setTop(header);
        BorderPane.setMargin(header, new Insets(0, 0, 30, 0));

        // Welcome Title
        Label welcomeLabel = new Label("Welcome back, Eden!");
        welcomeLabel.getStyleClass().add("dashboard-header");
        welcomeLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: 600;"); 
        
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
        header.getStyleClass().add("dashboard-header");
        HBox.setHgrow(header, Priority.ALWAYS);

        // Search Bar
        HBox searchContainer = new HBox(10);
        searchContainer.getStyleClass().add("search-container");
        searchContainer.setPrefHeight(40);
        searchContainer.setMaxHeight(40);
        searchContainer.setPrefWidth(400);
        HBox.setHgrow(searchContainer, Priority.NEVER);
        
        // Search icon
        Node searchIcon = SvgIconLoader.loadIcon("/svg/search.svg", 18, Color.web("#717182"));
        if (searchIcon != null) {
            searchContainer.getChildren().add(searchIcon);
        }
        
        Input searchBar = new Input();
        searchBar.setPromptText("Search recipes...");
        searchBar.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;"); // Override Input style for container
        HBox.setHgrow(searchBar, Priority.ALWAYS);
        searchContainer.getChildren().add(searchBar);

        // User Profile
        HBox profileContainer = new HBox(10);
        profileContainer.setAlignment(Pos.CENTER_RIGHT);
        
        Avatar avatar = new Avatar(20, null, "EC");
        
        Label userName = new Label("Eden Chang");
        userName.getStyleClass().add("label");
        userName.setStyle("-fx-font-weight: 500;");
        
        Label dropdown = new Label("⌄");
        
        profileContainer.getChildren().addAll(avatar, userName, dropdown);

        header.getChildren().addAll(searchContainer, profileContainer);
        return header;
    }

    private VBox createTodayMenuSection() {
        VBox container = new VBox();
        container.getStyleClass().add("card-panel");
        container.setSpacing(16);

        // Section Title
        Label title = new Label("Today's Menu");
        title.getStyleClass().add("section-title");

        // Meals Container (Horizontal Grid)
        mealsContainer = new HBox(16);
        mealsContainer.setAlignment(Pos.CENTER);
        mealsContainer.setPrefHeight(250);

        container.getChildren().addAll(title, mealsContainer);
        return container;
    }

    private VBox createNutritionProgressSection() {
        VBox container = new VBox();
        container.getStyleClass().add("card-panel");
        container.setSpacing(20);
        container.setAlignment(Pos.CENTER);

        Label title = new Label("Nutrition Progress");
        title.getStyleClass().add("section-title");
        title.setAlignment(Pos.CENTER_LEFT);

        circularProgressPane = createCircularProgress();
        
        remainingCaloriesLabel = new Label("Remaining: 750");
        remainingCaloriesLabel.setStyle("-fx-text-fill: -fx-theme-muted-foreground;");

        VBox nutrientBars = new VBox(12);
        
        VBox proteinBox = createNutrientBar("Protein (75g / 120g)", 0.625, "protein");
        proteinBar = (Progress) proteinBox.getChildren().get(1);
        
        VBox carbsBox = createNutrientBar("Carbs (150g / 250g)", 0.60, "carbs");
        carbsBar = (Progress) carbsBox.getChildren().get(1);
        
        VBox fatBox = createNutrientBar("Fat (45g / 70g)", 0.643, "fat");
        fatBar = (Progress) fatBox.getChildren().get(1);

        nutrientBars.getChildren().addAll(proteinBox, carbsBox, fatBox);

        container.getChildren().addAll(title, circularProgressPane, remainingCaloriesLabel, nutrientBars);
        return container;
    }

    private StackPane createCircularProgress() {
        StackPane stackPane = new StackPane();
        stackPane.setPrefSize(150, 150);

        Circle backgroundCircle = new Circle(75);
        backgroundCircle.setFill(Color.TRANSPARENT);
        backgroundCircle.setStroke(Color.web("#ececf0")); // -fx-theme-muted
        backgroundCircle.setStrokeWidth(15);

        Arc progressArc = new Arc(0, 0, 75, 75, 90, -223.2);
        progressArc.setType(ArcType.OPEN);
        progressArc.setFill(null);
        progressArc.setStroke(Color.web("#030213")); // -fx-theme-primary
        progressArc.setStrokeWidth(15);
        progressArc.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);

        VBox textBox = new VBox(5);
        textBox.setAlignment(Pos.CENTER);
        calorieValueLabel = new Label("1250 / 2000");
        calorieValueLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 20px;");
        
        Label caloriesLabel = new Label("Calories");
        caloriesLabel.setStyle("-fx-text-fill: -fx-theme-muted-foreground; -fx-font-size: 12px;");
        
        textBox.getChildren().addAll(calorieValueLabel, caloriesLabel);

        stackPane.getChildren().addAll(backgroundCircle, progressArc, textBox);
        return stackPane;
    }

    private VBox createNutrientBar(String labelText, double progress, String type) {
        VBox container = new VBox(5);
        
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 14px;");

        Progress bar = new Progress(progress);
        // Optional: set color based on type via CSS or setStyle, currently Progress uses primary.
        // For now, keep default.

        container.getChildren().addAll(label, bar);
        return container;
    }

    private VBox createRecipeSuggestionsSection() {
        VBox container = new VBox();
        container.getStyleClass().add("card-panel");
        container.setSpacing(16);

        Label title = new Label("Recipe Suggestions");
        title.getStyleClass().add("section-title");

        GridPane recipeGrid = new GridPane();
        recipeGrid.setHgap(16);
        recipeGrid.setVgap(16);

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

        Region imagePlaceholder = new Region();
        imagePlaceholder.setPrefSize(60, 60);
        imagePlaceholder.setStyle("-fx-background-color: -fx-theme-muted; -fx-background-radius: 8px;");

        VBox textBox = new VBox(4);
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: normal; -fx-font-size: 14px;");
        
        Label calLabel = new Label(calories);
        calLabel.setStyle("-fx-text-fill: -fx-theme-muted-foreground; -fx-font-size: 12px;");

        textBox.getChildren().addAll(nameLabel, calLabel);
        item.getChildren().addAll(imagePlaceholder, textBox);
        return item;
    }

    private VBox createQuickActionsSection() {
        VBox container = new VBox();
        container.getStyleClass().add("card-panel");
        container.setSpacing(16);

        Label title = new Label("Quick Actions");
        title.getStyleClass().add("section-title");

        VBox actionButtons = new VBox(10);
        
        Button addSnack = new Button("Add Snack");
        addSnack.getStyleClass().add("outline-button");
        addSnack.setMaxWidth(Double.MAX_VALUE);
        
        Button logWater = new Button("Log Water");
        logWater.getStyleClass().add("outline-button");
        logWater.setMaxWidth(Double.MAX_VALUE);
        
        Button createRecipe = new Button("Create Recipe");
        createRecipe.getStyleClass().add("outline-button");
        createRecipe.setMaxWidth(Double.MAX_VALUE);
        createRecipe.setOnAction(e -> viewManagerModel.setActiveView(ViewManager.STORE_RECIPE_VIEW));

        actionButtons.getChildren().addAll(addSnack, logWater, createRecipe);

        container.getChildren().addAll(title, actionButtons);
        return container;
    }

    private VBox createMealCard(String mealType, String mealName, String icon, int calories) {
        VBox card = new VBox(10);
        card.getStyleClass().add("meal-card");
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);
        card.setMinHeight(250);
        
        boolean isPlanned = !mealName.equals("Not Planned") && mealName != null && !mealName.isEmpty();
        boolean isActive = mealType.equalsIgnoreCase("Lunch") && isPlanned; // Demo logic
        
        if (isPlanned) {
            if (isActive) {
                card.getStyleClass().add("active");
                
                DropShadow cardShadow = new DropShadow();
                cardShadow.setBlurType(BlurType.GAUSSIAN);
                cardShadow.setColor(Color.rgb(0, 0, 0, 0.1));
                cardShadow.setRadius(8);
                cardShadow.setOffsetY(2);
                card.setEffect(cardShadow);
                
                // Image placeholder via Skeleton (simulating loading or just placeholder)
                Skeleton imagePlaceholder = new Skeleton(100, 100);
                imagePlaceholder.setPrefWidth(Region.USE_COMPUTED_SIZE);
                
                Label nameLabel = new Label(mealName);
                nameLabel.getStyleClass().add("meal-card-title");
                nameLabel.setWrapText(true);
                nameLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

                Label calLabel = new Label(calories + " kcal");
                calLabel.getStyleClass().add("meal-card-subtitle");
                calLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8);");

                Button viewBtn = new Button("View Meal");
                viewBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #030213; -fx-background-radius: 50px; -fx-font-weight: 500;");
                viewBtn.setMaxWidth(Double.MAX_VALUE);
                viewBtn.setOnAction(e -> viewManagerModel.setActiveView(ViewManager.BROWSE_RECIPE_VIEW));
                
                card.getChildren().addAll(imagePlaceholder, nameLabel, calLabel, viewBtn);

            } else {
                Label nameLabel = new Label(mealName);
                nameLabel.getStyleClass().add("meal-card-title");
                nameLabel.setWrapText(true);
                nameLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

                Label calLabel = new Label(calories + " kcal");
                calLabel.getStyleClass().add("meal-card-subtitle");

                Button viewBtn = new Button("View Meal");
                viewBtn.getStyleClass().add("ghost-button");
                viewBtn.setMaxWidth(Double.MAX_VALUE);
                viewBtn.setOnAction(e -> viewManagerModel.setActiveView(ViewManager.BROWSE_RECIPE_VIEW));

                card.getChildren().addAll(nameLabel, calLabel, viewBtn);
            }
            
        } else {
            // Empty State
            Label iconLabel = new Label(icon);
            iconLabel.setStyle("-fx-font-size: 48px;");
            
            Label typeLabel = new Label(mealType);
            typeLabel.getStyleClass().add("meal-card-title");
            
            Label emptyLabel = new Label("Not Planned");
            emptyLabel.getStyleClass().add("meal-card-subtitle");
            emptyLabel.setStyle("-fx-font-style: italic;");

            VBox buttonBox = new VBox(5);
            Button planBtn = new Button("Plan Meal");
            planBtn.getStyleClass().add("ghost-button");
            planBtn.setMaxWidth(Double.MAX_VALUE);
            planBtn.setOnAction(e -> viewManagerModel.setActiveView(ViewManager.SCHEDULE_VIEW));
            
            Button generateBtn = new Button("Generate");
            generateBtn.getStyleClass().add("secondary-button");
            generateBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: -fx-theme-muted-foreground;");
            generateBtn.setMaxWidth(Double.MAX_VALUE);
            
            buttonBox.getChildren().addAll(planBtn, generateBtn);
            VBox.setVgrow(iconLabel, Priority.ALWAYS);
            card.getChildren().addAll(iconLabel, typeLabel, emptyLabel, buttonBox);
        }

        return card;
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
                    lunchCals = 550;
                    currentCalories += lunchCals;
                }
                if (todaysMeals.containsKey(MealType.DINNER)) {
                    dinnerName = todaysMeals.get(MealType.DINNER);
                    dinnerCals = 600;
                    currentCalories += dinnerCals;
                }
            }
        }

        mealsContainer.getChildren().add(createMealCard("Breakfast", breakfastName, "🍳", breakfastCals));
        mealsContainer.getChildren().add(createMealCard("Lunch", lunchName, "🥗", lunchCals));
        mealsContainer.getChildren().add(createMealCard("Dinner", dinnerName, "🥩", dinnerCals));

        int targetCalories = 2000;
        double progress = Math.min((double) currentCalories / targetCalories, 1.0);
        int remaining = Math.max(targetCalories - currentCalories, 0);
        
        calorieValueLabel.setText(currentCalories + " / " + targetCalories);
        remainingCaloriesLabel.setText("Remaining: " + remaining);
        
        if (circularProgressPane != null) {
            for (javafx.scene.Node node : circularProgressPane.getChildren()) {
                if (node instanceof Arc) {
                    Arc progressArc = (Arc) node;
                    double angle = progress * 360.0;
                    progressArc.setLength(-angle);
                    break;
                }
            }
        }
        
        if (proteinBar != null) proteinBar.setProgress(0.625);
        if (carbsBar != null) carbsBar.setProgress(0.60);
        if (fatBar != null) fatBar.setProgress(0.643);
    }
}
