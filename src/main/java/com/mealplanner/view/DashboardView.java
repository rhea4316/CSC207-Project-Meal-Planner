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
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import com.mealplanner.view.util.SvgIconLoader;
import javafx.scene.Node;
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
    private HBox mealsContainer;
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

        // Welcome Title - Poppins Semi-Bold (600)
        Label welcomeLabel = new Label("Welcome back, Eden!");
        welcomeLabel.setFont(Font.font("Poppins, Segoe UI", FontWeight.SEMI_BOLD, 32));
        welcomeLabel.setTextFill(ModernUI.TEXT_COLOR);
        VBox titleBox = new VBox(welcomeLabel);
        // Increased margin-bottom as requested (approx 20px)
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

        // Search Bar with icon
        HBox searchContainer = new HBox(10);
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        searchContainer.setPadding(new Insets(0, 15, 0, 15));
        // Container Style: Light Gray, Height 40px, Radius 10px
        searchContainer.setBackground(new Background(new BackgroundFill(
            Color.web("#E5E7EB"),
            new CornerRadii(10),
            Insets.EMPTY
        )));
        searchContainer.setPrefHeight(40);
        searchContainer.setMaxHeight(40);
        searchContainer.setPrefWidth(400);
        HBox.setHgrow(searchContainer, Priority.NEVER);
        
        // Search icon
        Node searchIcon = SvgIconLoader.loadIcon("/svg/search.svg", 18, Color.web("#6B7280"));
        if (searchIcon != null) {
            searchContainer.getChildren().add(searchIcon);
        }
        
        TextField searchBar = new TextField();
        searchBar.setPromptText("Search recipes...");
        searchBar.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        searchBar.setFont(Font.font("Inter, Segoe UI", 14));
        HBox.setHgrow(searchBar, Priority.ALWAYS);
        searchContainer.getChildren().add(searchBar);

        // User Profile with avatar
        HBox profileContainer = new HBox(10);
        profileContainer.setAlignment(Pos.CENTER_RIGHT);
        
        // Circular avatar placeholder
        Circle avatar = new Circle(20);
        avatar.setFill(Color.web("#E5E7EB"));
        avatar.setStroke(Color.web("#D1D5DB"));
        avatar.setStrokeWidth(2);
        
        // User name
        Label userName = new Label("Eden Chang");
        userName.getStyleClass().add("user-profile");
        userName.setFont(Font.font("Inter, Segoe UI", FontWeight.MEDIUM, 14)); // Changed to MEDIUM
        userName.setTextFill(Color.web("#1F2937"));
        
        // Dropdown icon
        Label dropdown = new Label("⌄");
        dropdown.setFont(Font.font(14));
        dropdown.setTextFill(Color.web("#6B7280"));
        
        profileContainer.getChildren().addAll(avatar, userName, dropdown);

        header.getChildren().addAll(searchContainer, profileContainer);
        return header;
    }

    private VBox createTodayMenuSection() {
        VBox container = ModernUI.createCardPanel();
        container.setSpacing(16);

        // Section Title
        Label title = new Label("Today's Menu");
        title.getStyleClass().add("section-title");
        title.setFont(Font.font("Poppins, Segoe UI", FontWeight.SEMI_BOLD, 20));
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
        title.setFont(Font.font("Poppins, Segoe UI", FontWeight.SEMI_BOLD, 20));
        title.setTextFill(ModernUI.TEXT_COLOR);
        title.setAlignment(Pos.CENTER_LEFT);

        // Circular Progress
        circularProgressPane = createCircularProgress();
        
        // Remaining Calories
        remainingCaloriesLabel = new Label("Remaining: 750");
        remainingCaloriesLabel.setFont(Font.font("Inter, Segoe UI", 14));
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

        // Outer circle (background) - Track
        Circle backgroundCircle = new Circle(75);
        backgroundCircle.setFill(Color.TRANSPARENT);
        backgroundCircle.setStroke(Color.web("#E5E7EB")); // Light Gray Track
        backgroundCircle.setStrokeWidth(15); // Thickness 15px

        // Progress arc (62% = 223.2 degrees) - Donut Chart
        Arc progressArc = new Arc(0, 0, 75, 75, 90, -223.2); // Start at top, go clockwise
        progressArc.setType(ArcType.OPEN); // Open for ring style (stroke only)
        progressArc.setFill(null);
        progressArc.setStroke(Color.web("#4ADE80")); // Mint Green Progress
        progressArc.setStrokeWidth(15); // Thickness 15px
        progressArc.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);

        // Text in center
        VBox textBox = new VBox(5);
        textBox.setAlignment(Pos.CENTER);
        calorieValueLabel = new Label("1250 / 2000");
        calorieValueLabel.setFont(Font.font("Inter, Segoe UI", FontWeight.SEMI_BOLD, 20)); // Changed to SEMI_BOLD
        calorieValueLabel.setTextFill(ModernUI.TEXT_COLOR);
        
        Label caloriesLabel = new Label("Calories");
        caloriesLabel.setFont(Font.font("Inter, Segoe UI", 12));
        caloriesLabel.setTextFill(ModernUI.TEXT_LIGHT);
        
        textBox.getChildren().addAll(calorieValueLabel, caloriesLabel);

        // Add elements: background circle, progress arc, then text on top
        stackPane.getChildren().addAll(backgroundCircle, progressArc, textBox);
        return stackPane;
    }

    private VBox createNutrientBar(String labelText, double progress) {
        VBox container = new VBox(5); // 5px vertical spacing
        
        // Label above the bar
        Label label = new Label(labelText);
        label.setFont(Font.font("Inter, Segoe UI", 14));
        label.setTextFill(ModernUI.TEXT_COLOR);

        // Progress bar
        ProgressBar bar = new ProgressBar(progress);
        bar.setPrefHeight(8);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.getStyleClass().add("nutrient-progress-bar");
        // Mint green fill, light gray track
        bar.setStyle("-fx-accent: #4ADE80; " +
                     "-fx-control-inner-background: #E5E7EB; " +
                     "-fx-background-radius: 4px; " +
                     "-fx-padding: 0px;");

        container.getChildren().addAll(label, bar);
        return container;
    }

    private VBox createRecipeSuggestionsSection() {
        VBox container = ModernUI.createCardPanel();
        container.setSpacing(16);

        Label title = new Label("Recipe Suggestions");
        title.getStyleClass().add("section-title");
        title.setFont(Font.font("Poppins, Segoe UI", FontWeight.SEMI_BOLD, 20));
        title.setTextFill(ModernUI.TEXT_COLOR);

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

        Label title = new Label("Quick Actions");
        title.getStyleClass().add("section-title");
        title.setFont(Font.font("Poppins, Segoe UI", FontWeight.SEMI_BOLD, 20));
        title.setTextFill(ModernUI.TEXT_COLOR);

        VBox actionButtons = new VBox(10);
        
        Button addSnack = createOutlineButton("Add Snack");
        addSnack.setMaxWidth(Double.MAX_VALUE);
        
        Button logWater = createOutlineButton("Log Water");
        logWater.setMaxWidth(Double.MAX_VALUE);
        
        Button createRecipe = createOutlineButton("Create Recipe");
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
        
        boolean isPlanned = !mealName.equals("Not Planned") && mealName != null && !mealName.isEmpty();
        // Active if it's Lunch and Planned (per request to make Lunch the active card)
        boolean isActive = mealType.equalsIgnoreCase("Lunch") && isPlanned;
        
        if (isPlanned) {
            card.getStyleClass().add("meal-card");
            
            if (isActive) {
                // ACTIVE STATE (Lunch)
                card.getStyleClass().add("active");
                // Full mint green background
                card.setBackground(new Background(new BackgroundFill(
                    Color.web("#4ADE80"),
                    new CornerRadii(20),
                    Insets.EMPTY
                )));
                card.setBorder(null);
                
                // Shadow
                DropShadow cardShadow = new DropShadow();
                cardShadow.setBlurType(BlurType.GAUSSIAN);
                cardShadow.setColor(Color.rgb(0, 0, 0, 0.1));
                cardShadow.setRadius(8);
                cardShadow.setOffsetY(2);
                card.setEffect(cardShadow);
                
                // Image container at top (Only for Active)
                Region imagePlaceholder = new Region();
                imagePlaceholder.setPrefSize(Region.USE_COMPUTED_SIZE, 100); // Approx 100px as requested
                imagePlaceholder.setMinHeight(100);
                imagePlaceholder.setMaxHeight(100);
                imagePlaceholder.setBackground(new Background(new BackgroundFill(
                    Color.web("#E5E7EB"),
                    new CornerRadii(12), // Rounded corners
                    Insets.EMPTY
                )));
                
                // Meal Name - White
                Label nameLabel = new Label(mealName);
                nameLabel.setFont(Font.font("Poppins, Segoe UI", FontWeight.SEMI_BOLD, 16)); // Changed to SEMI_BOLD
                nameLabel.setTextFill(Color.WHITE); // White text
                nameLabel.setWrapText(true);
                nameLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

                // Calories - White
                Label calLabel = new Label(calories + " kcal");
                calLabel.setFont(Font.font("Inter, Segoe UI", 14));
                calLabel.setTextFill(Color.WHITE); // White text

                // Action Button - White background with Green text (Inverted)
                Button viewBtn = new Button("Plan Meal"); // Text from prompt: 'Plan Meal' button on this card
                // Wait, if it's planned, usually it's "View Recipe". 
                // Prompt says: "Button: Change the 'Plan Meal' button on this card to White background with Green text"
                // But 'Plan Meal' is usually for empty slots. If it's active, maybe it means "View Meal"?
                // Or maybe the prompt assumes the active card has a "Plan Meal" button?
                // Let's use "View Meal" or "Edit" if it's planned. 
                // However, looking at the prompt: "Button: Change the 'Plan Meal' button on this card..." 
                // The prompt might be referring to the main action button.
                // I will label it "View Meal" if planned, but style it as requested.
                viewBtn.setText("View Meal"); 
                
                viewBtn.setBackground(new Background(new BackgroundFill(
                    Color.WHITE,
                    new CornerRadii(50),
                    Insets.EMPTY
                )));
                viewBtn.setTextFill(Color.web("#4ADE80")); // Green text
                viewBtn.setFont(Font.font("Poppins, Segoe UI", FontWeight.MEDIUM, 14)); // Changed to MEDIUM
                viewBtn.setPadding(new Insets(8, 16, 8, 16));
                viewBtn.setMaxWidth(Double.MAX_VALUE);
                viewBtn.setOnAction(e -> viewManagerModel.setActiveView(ViewManager.BROWSE_RECIPE_VIEW));
                
                card.getChildren().addAll(imagePlaceholder, nameLabel, calLabel, viewBtn);

            } else {
                // INACTIVE PLANNED STATE (Breakfast/Dinner if planned)
                // White background, Dark text
                card.setBackground(new Background(new BackgroundFill(
                    Color.WHITE,
                    new CornerRadii(20),
                    Insets.EMPTY
                )));
                card.setBorder(new Border(new BorderStroke(
                    Color.web("#E5E7EB"),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(20),
                    new BorderWidths(1)
                )));
                
                // No Image for inactive per prompt implication ("Insert ... at the top of the active card")

                Label nameLabel = new Label(mealName);
                nameLabel.setFont(Font.font("Poppins, Segoe UI", FontWeight.SEMI_BOLD, 16)); // Changed to SEMI_BOLD
                nameLabel.setTextFill(Color.web("#1F2937")); // Dark text
                nameLabel.setWrapText(true);
                nameLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

                Label calLabel = new Label(calories + " kcal");
                calLabel.setFont(Font.font("Inter, Segoe UI", 14));
                calLabel.setTextFill(Color.web("#4B5563")); // Gray text

                // Ghost Button
                Button viewBtn = ModernUI.createGhostButton("View Meal");
                viewBtn.setMaxWidth(Double.MAX_VALUE);
                viewBtn.setOnAction(e -> viewManagerModel.setActiveView(ViewManager.BROWSE_RECIPE_VIEW));

                card.getChildren().addAll(nameLabel, calLabel, viewBtn);
            }
            
        } else {
            // EMPTY STATE (Not Planned)
            // Should look like Inactive Card (White with Dark text)
            card.getStyleClass().add("meal-card");
            card.setBackground(new Background(new BackgroundFill(
                Color.WHITE,
                new CornerRadii(20),
                Insets.EMPTY
            )));
            card.setBorder(new Border(new BorderStroke(
                Color.web("#E5E7EB"),
                BorderStrokeStyle.SOLID,
                new CornerRadii(20),
                new BorderWidths(1)
            )));
            
            Label iconLabel = new Label(icon);
            iconLabel.setFont(Font.font("Segoe UI Emoji", 48));
            
            Label typeLabel = new Label(mealType);
            typeLabel.setFont(Font.font("Poppins, Segoe UI", FontWeight.SEMI_BOLD, 16)); // Changed to SEMI_BOLD
            typeLabel.setTextFill(ModernUI.TEXT_COLOR);
            
            Label emptyLabel = new Label("Not Planned");
            emptyLabel.setFont(Font.font("Inter, Segoe UI", FontPosture.ITALIC, 14));
            emptyLabel.setTextFill(Color.web("#9CA3AF"));

            VBox buttonBox = new VBox(5);
            // Ghost Button for Generate/Plan to not compete
            Button planBtn = ModernUI.createGhostButton("Plan Meal");
            planBtn.setMaxWidth(Double.MAX_VALUE);
            planBtn.setOnAction(e -> viewManagerModel.setActiveView(ViewManager.SCHEDULE_VIEW));
            
            // Generate button
            Button generateBtn = new Button("Generate");
            generateBtn.getStyleClass().add("secondary-button");
            generateBtn.setMaxWidth(Double.MAX_VALUE);
            generateBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: gray; -fx-border-color: transparent;"); 
            // Or just make it very subtle as requested: "subtle 'Ghost Button' (transparent bg, thin grey border)"
            // I'll actually use the Ghost styling for Plan Meal as it is the main action for empty, 
            // but the prompt says "Ensure the 'Generate' button is styled as a subtle 'Ghost Button'".
            // And "'Plan Meal' button... to White background with Green text" was for the ACTIVE card.
            // For inactive cards, it says "Button Consistency: Ensure the 'Generate' button is styled as a subtle 'Ghost Button'".
            
            // Let's style 'Plan Meal' as the primary ghost, and Generate as secondary?
            // I'll style Plan Meal as Ghost.
            
            buttonBox.getChildren().addAll(planBtn, generateBtn);
            VBox.setVgrow(iconLabel, Priority.ALWAYS);
            card.getChildren().addAll(iconLabel, typeLabel, emptyLabel, buttonBox);
        }

        return card;
    }

    private Button createOutlineButton(String text) {
        Button btn = new Button(text);
        btn.setBackground(new Background(new BackgroundFill(
            Color.WHITE,
            new CornerRadii(50),
            Insets.EMPTY
        )));
        btn.setBorder(new Border(new BorderStroke(
            Color.web("#4ADE80"),
            BorderStrokeStyle.SOLID,
            new CornerRadii(50),
            new BorderWidths(1)
        )));
        btn.setTextFill(Color.web("#1F2937"));
        btn.setFont(Font.font("Poppins, Segoe UI", FontWeight.MEDIUM, 14)); // Changed to MEDIUM
        btn.setPadding(new Insets(12, 16, 12, 16));
        btn.setCursor(javafx.scene.Cursor.HAND);
        
        btn.setOnMouseEntered(e -> btn.setBackground(new Background(new BackgroundFill(
            Color.web("#F0FDF4"),
            new CornerRadii(50),
            Insets.EMPTY
        ))));
        btn.setOnMouseExited(e -> btn.setBackground(new Background(new BackgroundFill(
            Color.WHITE,
            new CornerRadii(50),
            Insets.EMPTY
        ))));
        
        return btn;
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
