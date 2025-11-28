package com.mealplanner.view;

import com.mealplanner.entity.MealType;
import com.mealplanner.entity.Schedule;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.view_model.ScheduleViewModel;
import com.mealplanner.view.component.*;
import com.mealplanner.view.component.Sonner;
import com.mealplanner.view.util.SvgIconLoader;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

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
    private Label calorieValueLabel; // e.g. "1,200 of 2,000 cal"
    private StackPane circularProgressPane;
    private Label remainingCaloriesLabel;
    private Progress proteinBar, carbsBar, fatBar;
    private final Sonner sonner;

    public DashboardView(ViewManagerModel viewManagerModel, ScheduleViewModel scheduleViewModel) {
        this.viewManagerModel = viewManagerModel;
        this.scheduleViewModel = scheduleViewModel;
        this.scheduleViewModel.addPropertyChangeListener(this);
        this.sonner = new Sonner();

        // 1. Layout & Background
        setBackground(new Background(new BackgroundFill(Color.web("#f7f8f9"), CornerRadii.EMPTY, Insets.EMPTY)));
        setPadding(new Insets(30, 40, 30, 40));

        VBox topSection = new VBox(20);
        
        // Welcome Title
        VBox titleBox = new VBox(5);
        Label welcomeLabel = new Label("Welcome back, Eden!");
        welcomeLabel.getStyleClass().addAll("welcome-title", "text-gray-900");
        
        Label subLabel = new Label("Let's plan your meals for today");
        subLabel.getStyleClass().addAll("welcome-subtitle", "text-gray-500");
        
        titleBox.getChildren().addAll(welcomeLabel, subLabel);
        topSection.getChildren().add(titleBox);
        
        setTop(topSection);
        BorderPane.setMargin(topSection, new Insets(0, 0, 30, 0));

        // Content Grid (2:1 ratio)
        GridPane contentGrid = new GridPane();
        contentGrid.setHgap(24);
        contentGrid.setVgap(24);

        // Left Column (65% width)
        VBox leftColumn = new VBox(24);
        // leftColumn.setPrefWidth(Region.USE_COMPUTED_SIZE); // Grid handles this
        
        // Today's Menu Section
        VBox menuSection = createTodayMenuSection();
        leftColumn.getChildren().add(menuSection);
        
        // Recommended for You Section
        VBox recipeSection = createRecommendedSection();
        leftColumn.getChildren().add(recipeSection);

        // Right Column (35% width)
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

        // Column Constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.NEVER); // Do not grow beyond content width
        col1.setMinWidth(Region.USE_COMPUTED_SIZE);
        col1.setPrefWidth(Region.USE_COMPUTED_SIZE);
        col1.setMaxWidth(Region.USE_COMPUTED_SIZE);
        
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS); // Take remaining space
        col2.setMinWidth(250); 
        
        contentGrid.getColumnConstraints().addAll(col1, col2);

        setCenter(contentGrid);
        
        // Responsive Layout Listener
        widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();
            if (width < 1000) {
                // Stack Columns Vertically
                contentGrid.getChildren().clear();
                contentGrid.getColumnConstraints().clear();
                contentGrid.getRowConstraints().clear();
                
                contentGrid.add(leftColumn, 0, 0);
                contentGrid.add(rightColumn, 0, 1);
                
                ColumnConstraints c1 = new ColumnConstraints();
                c1.setPercentWidth(100);
                contentGrid.getColumnConstraints().add(c1);
                
                // Make cards flexible in vertical layout
                if (mealsContainer != null) {
                    mealsContainer.setSpacing(10);
                }
            } else {
                // Restore 2-Column Layout
                contentGrid.getChildren().clear();
                contentGrid.getColumnConstraints().clear();
                contentGrid.getRowConstraints().clear();
                
                contentGrid.add(leftColumn, 0, 0);
                contentGrid.add(rightColumn, 1, 0);
                
                // Re-apply auto-width constraints
                ColumnConstraints c1 = new ColumnConstraints();
                c1.setHgrow(Priority.NEVER);
                c1.setMinWidth(Region.USE_COMPUTED_SIZE);
                
                ColumnConstraints c2 = new ColumnConstraints();
                c2.setHgrow(Priority.ALWAYS);
                c2.setMinWidth(250);
                
                contentGrid.getColumnConstraints().addAll(c1, c2);
                
                if (mealsContainer != null) {
                    mealsContainer.setSpacing(20);
                }
            }
        });

        // Initial Update
        updateView();
    }

    private VBox createTodayMenuSection() {
        VBox container = new VBox();
        container.getStyleClass().add("card-panel");
        container.setSpacing(20);

        // Header Row (Title + Auto-generate)
        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Today's Menu");
        title.getStyleClass().add("section-title");
        title.setPadding(Insets.EMPTY);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button autoGenBtn = new Button("Auto-generate");
        autoGenBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: -fx-theme-primary; -fx-font-weight: 600; -fx-cursor: hand;");
        Node sparkIcon = SvgIconLoader.loadIcon("/svg/star.svg", 16, Color.web("#4CAF50"));
        if (sparkIcon != null) autoGenBtn.setGraphic(sparkIcon);
        
        headerRow.getChildren().addAll(title, spacer, autoGenBtn);
        
        Label subTitle = new Label("Plan your meals for the day");
        subTitle.setStyle("-fx-text-fill: -fx-theme-muted-foreground; -fx-font-size: 14px;");
        
        VBox headerBox = new VBox(5); // Reduced spacing
        headerBox.getChildren().addAll(headerRow, subTitle);

        // Meals Container (Horizontal Grid)
        mealsContainer = new HBox(20);
        mealsContainer.setAlignment(Pos.CENTER_LEFT);
        // Ensure HBox fills width
        mealsContainer.setMaxWidth(Double.MAX_VALUE);

        container.getChildren().addAll(headerBox, mealsContainer);
        return container;
    }

    private VBox createNutritionProgressSection() {
        VBox container = new VBox();
        container.getStyleClass().add("card-panel");
        container.setSpacing(20);
        // Set minimum height to prevent content hiding when resized
        container.setMinHeight(450); // Slightly increased to ensure visibility

        Label title = new Label("Daily Nutrition");
        title.getStyleClass().add("section-title");
        title.setAlignment(Pos.CENTER_LEFT);

        circularProgressPane = createCircularProgress();
        
        VBox circleContainer = new VBox(10);
        circleContainer.setAlignment(Pos.CENTER);
        
        remainingCaloriesLabel = new Label("Remaining\n1150 cal");
        remainingCaloriesLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        remainingCaloriesLabel.setStyle("-fx-text-fill: -fx-theme-muted-foreground; -fx-font-size: 14px;");
        
        circleContainer.getChildren().addAll(circularProgressPane, remainingCaloriesLabel);

        VBox nutrientBars = new VBox(15);
        // VBox.setVgrow(nutrientBars, Priority.ALWAYS); // Allow this to grow if needed, but fixed size is usually fine
        
        VBox proteinBox = createNutrientBar("Protein", "32 / 75g", 0.42, "protein");
        proteinBar = (Progress) proteinBox.getChildren().get(1);
        
        VBox carbsBox = createNutrientBar("Carbs", "95 / 250g", 0.38, "carbs");
        carbsBar = (Progress) carbsBox.getChildren().get(1);
        
        VBox fatBox = createNutrientBar("Fat", "28 / 70g", 0.40, "fat");
        fatBar = (Progress) fatBox.getChildren().get(1);

        nutrientBars.getChildren().addAll(proteinBox, carbsBox, fatBox);

        container.getChildren().addAll(title, circleContainer, nutrientBars);
        return container;
    }

    private StackPane createCircularProgress() {
        StackPane stackPane = new StackPane();
        stackPane.setPrefSize(160, 160);

        Circle backgroundCircle = new Circle(70);
        backgroundCircle.setFill(Color.TRANSPARENT);
        backgroundCircle.setStroke(Color.web("#f3f4f6")); // -fx-theme-muted updated
        backgroundCircle.setStrokeWidth(12);

        Arc progressArc = new Arc(0, 0, 70, 70, 90, -150);
        progressArc.setType(ArcType.OPEN);
        progressArc.setFill(null);
        progressArc.setStroke(Color.web("#58c937")); // -fx-theme-primary (Green) updated
        progressArc.setStrokeWidth(12);
        progressArc.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);

        // Wrap shapes in a Group to maintain relative alignment
        javafx.scene.Group progressGroup = new javafx.scene.Group(backgroundCircle, progressArc);

        VBox textBox = new VBox(2);
        textBox.setAlignment(Pos.CENTER);
        calorieValueLabel = new Label("850");
        calorieValueLabel.getStyleClass().add("text-gray-900");
        calorieValueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 28px;");
        
        Label totalLabel = new Label("of 2000 cal");
        totalLabel.getStyleClass().add("text-gray-500");
        totalLabel.setStyle("-fx-font-size: 12px;");
        
        textBox.getChildren().addAll(calorieValueLabel, totalLabel);

        stackPane.getChildren().addAll(progressGroup, textBox);
        return stackPane;
    }

    private VBox createNutrientBar(String labelText, String valueText, double progress, String type) {
        VBox container = new VBox(8);
        
        HBox labelRow = new HBox();
        labelRow.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(labelText);
        nameLabel.getStyleClass().add("text-gray-700");
        nameLabel.setStyle("-fx-font-size: 14px;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label valLabel = new Label(valueText);
        valLabel.getStyleClass().add("text-gray-500");
        valLabel.setStyle("-fx-font-size: 12px;");
        
        labelRow.getChildren().addAll(nameLabel, spacer, valLabel);

        Progress bar = new Progress(progress);
        bar.getStyleClass().add("progress-bar-" + type);
        bar.setPrefHeight(8);
        bar.setMaxWidth(Double.MAX_VALUE); // Ensure it fills width 

        container.getChildren().addAll(labelRow, bar);
        return container;
    }

    private VBox createRecommendedSection() {
        VBox container = new VBox();
        container.getStyleClass().add("card-panel");
        container.setSpacing(20);

        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Recommended for You");
        title.getStyleClass().add("section-title");
        title.setPadding(Insets.EMPTY);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label viewAll = new Label("View all >");
        viewAll.getStyleClass().add("text-lime-600");
        viewAll.setStyle("-fx-cursor: hand; -fx-font-weight: 600; -fx-font-size: 14px;");
        
        headerRow.getChildren().addAll(title, spacer, viewAll);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setPannable(true);

        HBox recipeList = new HBox(16);
        recipeList.setPadding(new Insets(5)); 
        
        recipeList.getChildren().add(createRecipeCard("Quinoa Buddha Bowl", "320 cal", "15 min", "/images/bowl.jpg"));
        recipeList.getChildren().add(createRecipeCard("Grilled Salmon", "450 cal", "25 min", "/images/salmon.jpg"));
        recipeList.getChildren().add(createRecipeCard("Chicken Salad", "280 cal", "10 min", "/images/salad.jpg"));

        scrollPane.setContent(recipeList);

        container.getChildren().addAll(headerRow, scrollPane);
        return container;
    }
    
    private VBox createRecipeCard(String name, String calories, String time, String imagePath) {
        VBox card = new VBox(0);
        String defaultStyle = "-fx-background-color: #f9fafb; -fx-background-radius: 12px; -fx-effect: null; -fx-border-color: transparent; -fx-border-width: 1px;";
        String hoverStyle = "-fx-background-color: #ffffff; -fx-background-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4); -fx-border-color: #e5e7eb; -fx-border-width: 1px;";
        
        card.setStyle(defaultStyle);
        card.setPrefWidth(200);
        card.setMinWidth(200);
        
        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(defaultStyle));
        
        Region imagePlaceholder = new Region();
        imagePlaceholder.setPrefHeight(120);
        imagePlaceholder.setStyle("-fx-background-color: #E5E7EB; -fx-background-radius: 12px 12px 0 0;");
        
        VBox content = new VBox(8);
        content.setPadding(new Insets(12));
        
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("text-gray-900");
        nameLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 14px;");
        nameLabel.setWrapText(true);
        
        HBox meta = new HBox(10);
        meta.setAlignment(Pos.CENTER_LEFT);
        
        // Fire Icon for Calories (Medium Gray)
        HBox calBox = new HBox(4);
        calBox.setAlignment(Pos.CENTER_LEFT);
        Node fireIcon = SvgIconLoader.loadIcon("/svg/fire-flame.svg", 12, Color.web("#6B7280"));
        Label calLabel = new Label(calories);
        calLabel.getStyleClass().add("text-gray-500");
        calLabel.setStyle("-fx-font-size: 11px;");
        if (fireIcon != null) calBox.getChildren().add(fireIcon);
        calBox.getChildren().add(calLabel);
        
        // Clock Icon for Time (Medium Gray)
        HBox timeBox = new HBox(4);
        timeBox.setAlignment(Pos.CENTER_LEFT);
        Node clockIcon = SvgIconLoader.loadIcon("/svg/clock.svg", 12, Color.web("#6B7280"));
        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("text-gray-500");
        timeLabel.setStyle("-fx-font-size: 11px;");
        if (clockIcon != null) timeBox.getChildren().add(clockIcon);
        timeBox.getChildren().add(timeLabel);
        
        meta.getChildren().addAll(calBox, timeBox);
        
        content.getChildren().addAll(nameLabel, meta);
        card.getChildren().addAll(imagePlaceholder, content);
        
        return card;
    }

    private VBox createQuickActionsSection() {
        VBox container = new VBox();
        container.getStyleClass().add("card-panel"); // Added white background class
        container.setSpacing(16);

        Label title = new Label("Quick Actions");
        title.getStyleClass().add("section-title");

        GridPane grid = new GridPane();
        grid.setHgap(12); // Spacing 12px
        grid.setVgap(12);
        
        // Add Snack: Lime (#84cc16) -> Green (#22c55e)
        grid.add(createQuickActionButton("Add Snack", "/svg/plus.svg", "#84cc16", "#22c55e"), 0, 0);
        
        // Log Water: Blue (#3b82f6) -> Cyan (#06b6d4)
        grid.add(createQuickActionButton("Log Water", "/svg/raindrops.svg", "#3b82f6", "#06b6d4"), 1, 0);
        
        // New Recipe: Purple (#a855f7) -> Pink (#ec4899)
        grid.add(createQuickActionButton("New Recipe", "/svg/add-book.svg", "#a855f7", "#ec4899"), 0, 1);
        
        // Weekly Plan: Orange (#f97316) -> Amber (#f59e0b)
        grid.add(createQuickActionButton("Weekly Plan", "/svg/calendar.svg", "#f97316", "#f59e0b"), 1, 1);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);
        
        container.getChildren().addAll(title, grid);
        return container;
    }
    
    private VBox createQuickActionButton(String text, String iconPath, String startColor, String endColor) {
        VBox btn = new VBox(10);
        btn.getStyleClass().add("quick-action-button"); 
        btn.setAlignment(Pos.CENTER);
        btn.setPrefHeight(100);
        
        // Default Style: bg #fbfbfc, no shadow, no outline
        String defaultBtnStyle = "-fx-background-color: #fbfbfc; -fx-background-radius: 12px; -fx-effect: null; -fx-border-width: 0;";
        // Hover Style: bg #f7f8fa, outline #e5e7eb, shadow
        String hoverBtnStyle = "-fx-background-color: #f7f8fa; -fx-background-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 2, 0, 0, 1); -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 12px;";
        
        btn.setStyle(defaultBtnStyle);
        
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(48, 48);
        iconBox.setMaxSize(48, 48);
        
        Rectangle iconBg = new Rectangle(48, 48);
        iconBg.setArcWidth(16); // Increased radius from 12 to 16
        iconBg.setArcHeight(16); // Increased radius from 12 to 16
        
        // Gradient Fill
        Stop[] stops = new Stop[] { new Stop(0, Color.web(startColor)), new Stop(1, Color.web(endColor)) };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        iconBg.setFill(gradient);
        // Update DropShadow to softer, spread out shadow if needed, or keep as is
        iconBg.setEffect(new javafx.scene.effect.DropShadow(2, 0, 1, Color.rgb(0,0,0,0.05)));
        
        Node icon = SvgIconLoader.loadIcon(iconPath, 24, Color.WHITE);
        if (icon != null) {
            icon.setStyle("-fx-stroke-width: 2px;"); // Ensure stroke width if supported or visually approximated
        } else {
            icon = new Label("?");
        }
        
        iconBox.getChildren().addAll(iconBg, icon);
        
        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("text-gray-700");
        textLabel.setStyle("-fx-font-size: 12px;"); // Font size 12px
        
        btn.getChildren().addAll(iconBox, textLabel);
        
        // Hover Effects
        btn.setOnMouseEntered(e -> {
            btn.setStyle(hoverBtnStyle);
            iconBox.setScaleX(1.1);
            iconBox.setScaleY(1.1);
        });
        
        btn.setOnMouseExited(e -> {
            btn.setStyle(defaultBtnStyle);
            iconBox.setScaleX(1.0);
            iconBox.setScaleY(1.0);
        });
        
        btn.setOnMouseClicked(e -> handleQuickAction(text));
        
        return btn;
    }

    private void handleQuickAction(String text) {
        switch (text) {
            case "Add Snack":
                sonner.show("Snack logged", "We’ll add this to today’s nutrition summary.", Sonner.Type.INFO);
                break;
            case "Log Water":
                sonner.show("Hydration noted", "Another glass recorded. Keep it up!", Sonner.Type.SUCCESS);
                break;
            case "New Recipe":
                viewManagerModel.setActiveView(ViewManager.STORE_RECIPE_VIEW);
                break;
            case "Weekly Plan":
                viewManagerModel.setActiveView(ViewManager.SCHEDULE_VIEW);
                break;
            default:
                break;
        }
    }

    private VBox createMealCard(String mealType, String mealName, String iconPath, int calories, String time) {
        VBox card = new VBox(0);
        card.getStyleClass().add("meal-card"); 
        card.setStyle(null);
        
        // Fixed width
        card.setPrefWidth(200);
        card.setMinWidth(200);
        card.setPrefHeight(180);
        
        // Default style managed by CSS .meal-card, override for planned vs not planned
        
        boolean isPlanned = !mealName.equals("Not Planned") && mealName != null && !mealName.isEmpty();
        
        if (isPlanned) {
            Region imagePart = new Region();
            imagePart.setPrefHeight(100);
            imagePart.setStyle("-fx-background-color: #E5E7EB; -fx-background-radius: 12px 12px 0 0;");
            
            Label badge = new Label(mealType);
            badge.getStyleClass().add("text-lime-700");
            badge.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 4px; -fx-padding: 2 6; -fx-font-size: 10px; -fx-font-weight: bold;");
            StackPane.setAlignment(badge, Pos.TOP_LEFT);
            StackPane.setMargin(badge, new Insets(10));
            
            StackPane imageContainer = new StackPane(imagePart, badge);
            
            VBox content = new VBox(6);
            content.setPadding(new Insets(12));
            
            Label title = new Label(mealName);
            title.getStyleClass().add("text-gray-900");
            title.setStyle("-fx-font-weight: 600; -fx-font-size: 14px;");
            
            HBox meta = new HBox(10);
            meta.setAlignment(Pos.CENTER_LEFT);
            
            // Fire Icon
            HBox calBox = new HBox(4);
            calBox.setAlignment(Pos.CENTER_LEFT);
            Node fireIcon = SvgIconLoader.loadIcon("/svg/fire-flame.svg", 12, Color.web("#ff6900"));
            Label calLabel = new Label(calories + " cal");
            calLabel.getStyleClass().add("text-gray-500");
            calLabel.setStyle("-fx-font-size: 11px;");
            if (fireIcon != null) calBox.getChildren().add(fireIcon);
            calBox.getChildren().add(calLabel);
            
            // Clock Icon
            HBox timeBox = new HBox(4);
            timeBox.setAlignment(Pos.CENTER_LEFT);
            Node clockIcon = SvgIconLoader.loadIcon("/svg/clock.svg", 12, Color.web("#2b7fff"));
            Label timeLabel = new Label(time);
            timeLabel.getStyleClass().add("text-gray-500");
            timeLabel.setStyle("-fx-font-size: 11px;");
            if (clockIcon != null) timeBox.getChildren().add(clockIcon);
            timeBox.getChildren().add(timeLabel);
            
            meta.getChildren().addAll(calBox, timeBox);
            
            Label scheduleTime = new Label("8:00 AM");
            scheduleTime.getStyleClass().add("text-gray-400");
            scheduleTime.setStyle("-fx-font-size: 11px;");
            
            content.getChildren().addAll(title, meta, scheduleTime);
            
            card.getChildren().addAll(imageContainer, content);
            
            // Hover Effect for Planned
            card.setOnMouseEntered(e -> {
                card.setStyle("-fx-border-color: #bbf452; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);");
            });
            card.setOnMouseExited(e -> {
                card.setStyle(null); // Reset to CSS default
            });
            
            card.setOnMouseClicked(e -> viewManagerModel.setActiveView(ViewManager.BROWSE_RECIPE_VIEW));

        } else {
            // Not Planned State - Center Alignment logic
            card.setAlignment(Pos.CENTER);
            card.setSpacing(10);
            
            // Create a flexible spacer to push content to center vertically
            Region topSpacer = new Region();
            VBox.setVgrow(topSpacer, Priority.ALWAYS);
            
            // Middle Content (Icon, Title, Time, Status)
            VBox centerBox = new VBox(10);
            centerBox.setAlignment(Pos.CENTER);
            
            // Icon Container for Hover Effect
            StackPane iconContainer = new StackPane();
            iconContainer.setMaxSize(40, 40);
            iconContainer.setPrefSize(40, 40);
            
            Rectangle iconBg = new Rectangle(40, 40);
            iconBg.setArcWidth(12);
            iconBg.setArcHeight(12);
            iconBg.setFill(Color.TRANSPARENT); // Default transparent
            
            Node mealIcon = SvgIconLoader.loadIcon(iconPath, 32, Color.web("#9CA3AF"));
            if (mealIcon == null) mealIcon = new Label("?");
            
            iconContainer.getChildren().addAll(iconBg, mealIcon);
            
            Label typeLabel = new Label(mealType);
            typeLabel.getStyleClass().add("text-gray-700");
            typeLabel.setStyle("-fx-font-weight: 500; -fx-font-size: 14px;");
            
            Label subLabel = new Label("12:30 PM");
            subLabel.getStyleClass().add("text-gray-400");
            subLabel.setStyle("-fx-font-size: 11px;");
            
            Label statusLabel = new Label("Not Planned");
            statusLabel.getStyleClass().add("text-gray-400");
            statusLabel.setStyle("-fx-font-size: 11px;");
            
            centerBox.getChildren().addAll(iconContainer, typeLabel, subLabel, statusLabel);
            
            Region bottomSpacer = new Region();
            VBox.setVgrow(bottomSpacer, Priority.ALWAYS);
            
            // Bottom Button Container
            HBox bottomBox = new HBox();
            bottomBox.setAlignment(Pos.CENTER);
            bottomBox.setPadding(new Insets(0, 0, 15, 0)); // Padding from bottom edge
            
            Button addBtn = new Button();
            Node plusIcon = SvgIconLoader.loadIcon("/svg/plus-small.svg", 20, Color.web("#6B7280"));
            addBtn.setGraphic(plusIcon);
            addBtn.setPrefSize(32, 32);
            addBtn.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 16px; -fx-padding: 0; -fx-cursor: hand;");
            
            bottomBox.getChildren().add(addBtn);
            
            card.getChildren().addAll(topSpacer, centerBox, bottomSpacer, bottomBox);
            
            // Hover Effect for Not Planned
            card.setOnMouseEntered(e -> {
                // Background Gradient
                Stop[] bgStops = new Stop[] { new Stop(0, Color.web("#f7fee7")), new Stop(1, Color.web("#f1fdf4")) };
                LinearGradient bgGradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, bgStops);
                card.setBackground(new Background(new BackgroundFill(bgGradient, new CornerRadii(12), Insets.EMPTY)));
                card.setBorder(new Border(new BorderStroke(Color.web("#bbf452"), BorderStrokeStyle.SOLID, new CornerRadii(12), new BorderWidths(1))));
                
                // Icon Background Gradient
                Stop[] iconStops = new Stop[] { new Stop(0, Color.web("#74ce00")), new Stop(1, Color.web("#00c947")) };
                LinearGradient iconGradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, iconStops);
                iconBg.setFill(iconGradient);
                
                // Icon White
                Node whiteIcon = SvgIconLoader.loadIcon(iconPath, 24, Color.WHITE); // Slightly smaller to fit
                if (whiteIcon != null && iconContainer.getChildren().size() > 1) {
                    iconContainer.getChildren().set(1, whiteIcon);
                }
                
                // Button Style
                addBtn.setStyle("-fx-background-color: #7ccf00; -fx-background-radius: 16px; -fx-padding: 0; -fx-cursor: hand;");
                Node whitePlus = SvgIconLoader.loadIcon("/svg/plus-small.svg", 20, Color.WHITE);
                addBtn.setGraphic(whitePlus);
            });
            
            card.setOnMouseExited(e -> {
                // Reset Styles
                card.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
                card.setBorder(new Border(new BorderStroke(Color.web("#f3f4f6"), BorderStrokeStyle.SOLID, new CornerRadii(12), new BorderWidths(1))));
                
                iconBg.setFill(Color.TRANSPARENT);
                Node originalIcon = SvgIconLoader.loadIcon(iconPath, 32, Color.web("#9CA3AF"));
                if (originalIcon != null && iconContainer.getChildren().size() > 1) {
                    iconContainer.getChildren().set(1, originalIcon);
                }
                
                addBtn.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 16px; -fx-padding: 0; -fx-cursor: hand;");
                addBtn.setGraphic(plusIcon); // Re-use original gray icon
            });
            
            card.setOnMouseClicked(e -> viewManagerModel.setActiveView(ViewManager.SCHEDULE_VIEW));
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

        String breakfastName = "Avocado Toast"; 
        String lunchName = "Not Planned";
        String dinnerName = "Grilled Salmon";
        
        if (schedule != null) {
             Map<LocalDate, Map<MealType, String>> allMeals = schedule.getAllMeals();
             Map<MealType, String> todaysMeals = allMeals.get(today);
             if (todaysMeals != null) {
                 if (todaysMeals.containsKey(MealType.BREAKFAST)) breakfastName = todaysMeals.get(MealType.BREAKFAST);
                 if (todaysMeals.containsKey(MealType.LUNCH)) lunchName = todaysMeals.get(MealType.LUNCH);
                 if (todaysMeals.containsKey(MealType.DINNER)) dinnerName = todaysMeals.get(MealType.DINNER);
             }
        }

        mealsContainer.getChildren().add(createMealCard("Breakfast", breakfastName, "/svg/mug-hot.svg", 320, "10 min"));
        mealsContainer.getChildren().add(createMealCard("Lunch", lunchName, "/svg/brightness.svg", 0, ""));
        mealsContainer.getChildren().add(createMealCard("Dinner", dinnerName, "/svg/moon.svg", 520, "35 min"));

        calorieValueLabel.setText("850");
        remainingCaloriesLabel.setText("Remaining\n1150 cal");
        
        if (circularProgressPane != null) {
            if (!circularProgressPane.getChildren().isEmpty()) {
                Node possibleGroup = circularProgressPane.getChildren().get(0);
                if (possibleGroup instanceof javafx.scene.Group) {
                    javafx.scene.Group group = (javafx.scene.Group) possibleGroup;
                    for (Node node : group.getChildren()) {
                        if (node instanceof Arc) {
                            Arc progressArc = (Arc) node;
                            double angle = 0.425 * 360.0;
                            progressArc.setLength(-angle);
                            break;
                        }
                    }
                }
            }
        }
        
        if (proteinBar != null) proteinBar.setProgress(32.0 / 75.0);
        if (carbsBar != null) carbsBar.setProgress(95.0 / 250.0);
        if (fatBar != null) fatBar.setProgress(28.0 / 70.0);
    }
}
