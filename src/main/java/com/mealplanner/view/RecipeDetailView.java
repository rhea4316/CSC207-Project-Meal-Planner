package com.mealplanner.view;

import com.mealplanner.app.SessionManager;
import com.mealplanner.entity.NutritionGoals;
import com.mealplanner.entity.NutritionInfo;
import com.mealplanner.entity.Recipe;
import com.mealplanner.entity.User;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.AdjustServingSizeController;
import com.mealplanner.interface_adapter.controller.StoreRecipeController;
import com.mealplanner.interface_adapter.view_model.RecipeDetailViewModel;
import com.mealplanner.repository.RecipeRepository;
import com.mealplanner.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mealplanner.view.component.*;
import com.mealplanner.view.util.SvgIconLoader;
import com.mealplanner.util.ImageCacheManager;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import org.controlsfx.control.Notifications;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class RecipeDetailView extends ScrollPane implements PropertyChangeListener {
    private final RecipeDetailViewModel viewModel;
    private final AdjustServingSizeController controller;
    private final com.mealplanner.interface_adapter.controller.AddMealController addMealController;
    private final ViewManagerModel viewManagerModel;
    private final StoreRecipeController storeRecipeController;
    private final RecipeRepository recipeRepository;
    private final ImageCacheManager imageCache = ImageCacheManager.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(RecipeDetailView.class);

    // UI Components
    private StackPane heroSection;
    private ImageView heroImageView;

    private Label recipeNameLabel;
    private Label subtitleLabel;
    private HBox metaChipsContainer;
    
    private VBox ingredientsList;
    private VBox instructionsList;
    
    private Label caloriesValueLabel;
    private Progress proteinBar, carbsBar, fatBar;
    private Label proteinVal, carbsVal, fatVal;
    private Label servingValueLabel; // Modern serving size display
    private Label servingNote;
    private boolean isUpdatingFromViewModel = false; // Flag to prevent infinite loop

    public RecipeDetailView(RecipeDetailViewModel viewModel, AdjustServingSizeController controller, com.mealplanner.interface_adapter.controller.AddMealController addMealController, ViewManagerModel viewManagerModel, StoreRecipeController storeRecipeController, RecipeRepository recipeRepository) {
        if (viewModel == null) throw new IllegalArgumentException("ViewModel cannot be null");
        if (controller == null) throw new IllegalArgumentException("Controller cannot be null");
        if (addMealController == null) throw new IllegalArgumentException("AddMealController cannot be null");
        if (storeRecipeController == null) throw new IllegalArgumentException("StoreRecipeController cannot be null");
        if (recipeRepository == null) throw new IllegalArgumentException("RecipeRepository cannot be null");
        
        this.viewModel = viewModel;
        this.controller = controller;
        this.addMealController = addMealController;
        this.viewManagerModel = viewManagerModel;
        this.storeRecipeController = storeRecipeController;
        this.recipeRepository = recipeRepository;

        viewModel.addPropertyChangeListener(this);

        // Root Styles
        getStyleClass().add("root");
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setStyle("-fx-background-color: #F5F7FA; -fx-background: #F5F7FA; -fx-padding: 0;"); // Light gray bg
        
        // Increase scroll speed
        addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() != 0) {
                double delta = event.getDeltaY() * 3.0;
                double height = getContent().getBoundsInLocal().getHeight();
                double vHeight = getViewportBounds().getHeight();
                
                double scrollableHeight = height - vHeight;
                if (scrollableHeight > 0) {
                    double vValueShift = -delta / scrollableHeight;
                    double nextVvalue = getVvalue() + vValueShift;
                    
                    if (nextVvalue >= 0 && nextVvalue <= 1.0 || (getVvalue() > 0 && getVvalue() < 1.0)) {
                        setVvalue(Math.min(Math.max(nextVvalue, 0), 1));
                        event.consume();
                    }
                }
            }
        });

        VBox mainContent = new VBox();
        mainContent.setSpacing(0);
        
        createHeroSection();
        GridPane contentGrid = createContentGrid();
        
        mainContent.getChildren().addAll(heroSection, contentGrid);
        
        setContent(mainContent);
    }

    private void createHeroSection() {
        heroSection = new StackPane();
        heroSection.setPrefHeight(350);
        heroSection.setMinHeight(350);
        heroSection.setAlignment(Pos.BOTTOM_LEFT);

        // 1. Background Image - Fill entire hero section
        heroImageView = new ImageView();
        heroImageView.fitWidthProperty().bind(heroSection.widthProperty());
        heroImageView.fitHeightProperty().bind(heroSection.heightProperty());
        heroImageView.setPreserveRatio(false); // Fill the entire area without preserving ratio
        heroImageView.setSmooth(true);
        heroImageView.setCache(true);
        heroImageView.setStyle("-fx-background-color: #ddd;"); // Fallback color
        
        // 2. Gradient Overlay (Transparent -> Black 70%)
        Rectangle overlay = new Rectangle();
        overlay.widthProperty().bind(heroSection.widthProperty());
        overlay.heightProperty().bind(heroSection.heightProperty());
        Stop[] stops = new Stop[] { new Stop(0, Color.TRANSPARENT), new Stop(0.6, Color.rgb(0,0,0,0.0)), new Stop(1, Color.rgb(0,0,0,0.7)) };
        LinearGradient lg = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
        overlay.setFill(lg);

        // 3. Content Container
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(30, 40, 30, 40));
        contentBox.setAlignment(Pos.BOTTOM_LEFT);
        
        // Back Button (Top Left absolute)
        Button backBtn = new Button();
        backBtn.setStyle("-fx-background-color: white; -fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 2); -fx-cursor: hand;");
        Node backIcon = SvgIconLoader.loadIcon("/svg/arrow-small-left.svg", 24, Color.BLACK);
        if (backIcon != null) backBtn.setGraphic(backIcon);
        backBtn.setOnAction(e -> navigateBack());
        
        StackPane.setAlignment(backBtn, Pos.TOP_LEFT);
        StackPane.setMargin(backBtn, new Insets(30, 0, 0, 40));

        // Titles
        recipeNameLabel = new Label("Fluffy Pancakes");
        recipeNameLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-weight: bold; -fx-font-size: 32px; -fx-text-fill: white;");
        
        subtitleLabel = new Label("Light and fluffy buttermilk pancakes perfect for a weekend breakfast");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #e5e7eb;");
        
        // Meta Chips
        metaChipsContainer = new HBox(10);
        
        contentBox.getChildren().addAll(recipeNameLabel, subtitleLabel, metaChipsContainer);
        
        heroSection.getChildren().addAll(heroImageView, overlay, contentBox, backBtn);
    }

    private GridPane createContentGrid() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(30, 40, 30, 40));
        grid.setHgap(30);
        grid.setVgap(30);
        
        // Column Constraints (Left: 70%, Right: 30%)
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(70);
        
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(30);
        col2.setMinWidth(300); // Min width for right panel
        
        grid.getColumnConstraints().addAll(col1, col2);

        // --- LEFT COLUMN ---
        VBox leftCol = new VBox(30);
        
        // 1. Ingredients Card
        VBox ingredientsCard = createCard();
        HBox ingHeader = createCardHeader("Ingredients", "/svg/book-fill.svg");
        ingredientsList = new VBox(12);
        ingredientsCard.getChildren().addAll(ingHeader, new javafx.scene.control.Separator(), ingredientsList);
        
        // 2. Instructions Card
        VBox instructionsCard = createCard();
        HBox instHeader = createCardHeader("Instructions", "/svg/restaurant.svg"); // Chef hat fallback
        instructionsList = new VBox(20);
        instructionsCard.getChildren().addAll(instHeader, new javafx.scene.control.Separator(), instructionsList);
        
        leftCol.getChildren().addAll(ingredientsCard, instructionsCard);
        grid.add(leftCol, 0, 0);

        // --- RIGHT COLUMN ---
        VBox rightCol = new VBox(20);
        
        // 1. Quick Actions
        VBox actionsPanel = new VBox(10);
        
        Button addPlanBtn = createActionButton("Add to Weekly Plan", "primary", "/svg/plus.svg");
        addPlanBtn.setOnAction(e -> {
            Recipe recipe = viewModel.getRecipe();
            if (recipe != null) {
                new AddToMealPlanDialog((javafx.stage.Stage) getScene().getWindow(), addMealController, String.valueOf(recipe.getRecipeId()), recipe.getName()).show();
            }
        });
        
        Button saveBookBtn = createActionButton("Save to Cookbook", "secondary", "/svg/book.svg");
        saveBookBtn.setOnAction(e -> {
            Recipe recipe = viewModel.getRecipe();
            if (recipe != null) {
                saveRecipeToCookbook(recipe);
            }
        });
        Button shareBtn = createActionButton("Share Recipe", "default", "/svg/paper-plane.svg"); // Share icon fallback
        Button deleteBtn = createActionButton("Delete Recipe", "danger", "/svg/trash.svg");
        
        actionsPanel.getChildren().addAll(addPlanBtn, saveBookBtn, shareBtn, deleteBtn);
        
        // 2. Nutrition Facts
        VBox nutritionCard = createCard();
        
        Label nutTitle = new Label("Nutrition Facts");
        nutTitle.setStyle("-fx-font-weight: 600; -fx-font-size: 16px;");
        
        // Total Calories
        HBox calsBox = new HBox(10);
        calsBox.setAlignment(Pos.CENTER_RIGHT);
        calsBox.setPadding(new Insets(10, 0, 20, 0));
        Label totalCalsLabel = new Label("Total Calories");
        totalCalsLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        caloriesValueLabel = new Label("350");
        caloriesValueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 32px; -fx-text-fill: #111827;");
        Label kcalUnit = new Label("kcal");
        kcalUnit.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280; -fx-padding: 0 0 4 0;");
        HBox.setMargin(kcalUnit, new Insets(12, 0, 0, 0)); // Align baseline
        
        calsBox.getChildren().addAll(totalCalsLabel, spacer, caloriesValueLabel, kcalUnit);

        // Bars
        VBox barsContainer = new VBox(16);
        
        // Protein (Blue)
        VBox pBox = createNutrientBarUI("Protein", "#3b82f6");
        proteinBar = (Progress) pBox.getChildren().get(1);
        proteinVal = (Label) ((HBox)pBox.getChildren().get(0)).getChildren().get(2);
        
        // Carbs (Orange)
        VBox cBox = createNutrientBarUI("Carbs", "#f97316");
        carbsBar = (Progress) cBox.getChildren().get(1);
        carbsVal = (Label) ((HBox)cBox.getChildren().get(0)).getChildren().get(2);
        
        // Fat (Pink/Red)
        VBox fBox = createNutrientBarUI("Fat", "#ec4899");
        fatBar = (Progress) fBox.getChildren().get(1);
        fatVal = (Label) ((HBox)fBox.getChildren().get(0)).getChildren().get(2);
        
        barsContainer.getChildren().addAll(pBox, cBox, fBox);
        
        // Serving Size Adjustment Control - Modern Design
        VBox servingSizeControl = new VBox(8);
        servingSizeControl.setPadding(new Insets(15, 0, 10, 0));
        
        Label servingLabel = new Label("Servings");
        servingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280; -fx-font-weight: 500;");
        
        // Modern counter control with +/- buttons
        HBox counterBox = new HBox(0);
        counterBox.setAlignment(Pos.CENTER);
        counterBox.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8px; -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 8px;");
        
        // Initialize serving size
        int initialServingSize = viewModel.getRecipe() != null ? viewModel.getRecipe().getServingSize() : 2;
        Label servingValueLabel = new Label(String.valueOf(initialServingSize));
        servingValueLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #111827; -fx-font-weight: 600; -fx-min-width: 60px; -fx-alignment: center;");
        servingValueLabel.setPadding(new Insets(12, 16, 12, 16));
        
        // Minus button
        Button minusBtn = new Button();
        minusBtn.setStyle("-fx-background-color: transparent; -fx-background-radius: 8px 0 0 8px; -fx-border-width: 0; -fx-cursor: hand; -fx-padding: 12 16;");
        minusBtn.setPrefHeight(44);
        minusBtn.setMinWidth(44);
        Label minusLabel = new Label("−");
        minusLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #6b7280; -fx-font-weight: 600;");
        minusBtn.setGraphic(minusLabel);
        minusBtn.setOnMouseEntered(e -> {
            minusBtn.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8px 0 0 8px; -fx-border-width: 0; -fx-cursor: hand; -fx-padding: 12 16;");
            minusLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #111827; -fx-font-weight: 600;");
        });
        minusBtn.setOnMouseExited(e -> {
            minusBtn.setStyle("-fx-background-color: transparent; -fx-background-radius: 8px 0 0 8px; -fx-border-width: 0; -fx-cursor: hand; -fx-padding: 12 16;");
            minusLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #6b7280; -fx-font-weight: 600;");
        });
        minusBtn.setOnAction(e -> {
            int current = Integer.parseInt(servingValueLabel.getText());
            if (current > 1) {
                int newValue = current - 1;
                updateServingSize(newValue);
            }
        });
        
        // Plus button
        Button plusBtn = new Button();
        plusBtn.setStyle("-fx-background-color: transparent; -fx-background-radius: 0 8px 8px 0; -fx-border-width: 0; -fx-cursor: hand; -fx-padding: 12 16;");
        plusBtn.setPrefHeight(44);
        plusBtn.setMinWidth(44);
        Node plusIcon = SvgIconLoader.loadIcon("/svg/plus-small.svg", 16, Color.web("#6b7280"));
        if (plusIcon == null) {
            Label plusLabel = new Label("+");
            plusLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #6b7280; -fx-font-weight: 600;");
            plusBtn.setGraphic(plusLabel);
        } else {
            plusBtn.setGraphic(plusIcon);
        }
        plusBtn.setOnMouseEntered(e -> {
            plusBtn.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 0 8px 8px 0; -fx-border-width: 0; -fx-cursor: hand; -fx-padding: 12 16;");
            if (plusBtn.getGraphic() instanceof Label) {
                ((Label) plusBtn.getGraphic()).setStyle("-fx-font-size: 20px; -fx-text-fill: #111827; -fx-font-weight: 600;");
            } else if (plusBtn.getGraphic() != null) {
                Node hoverIcon = SvgIconLoader.loadIcon("/svg/plus-small.svg", 16, Color.web("#111827"));
                if (hoverIcon != null) plusBtn.setGraphic(hoverIcon);
            }
        });
        plusBtn.setOnMouseExited(e -> {
            plusBtn.setStyle("-fx-background-color: transparent; -fx-background-radius: 0 8px 8px 0; -fx-border-width: 0; -fx-cursor: hand; -fx-padding: 12 16;");
            if (plusBtn.getGraphic() instanceof Label) {
                ((Label) plusBtn.getGraphic()).setStyle("-fx-font-size: 20px; -fx-text-fill: #6b7280; -fx-font-weight: 600;");
            } else if (plusBtn.getGraphic() != null) {
                Node normalIcon = SvgIconLoader.loadIcon("/svg/plus-small.svg", 16, Color.web("#6b7280"));
                if (normalIcon != null) plusBtn.setGraphic(normalIcon);
            }
        });
        plusBtn.setOnAction(e -> {
            int current = Integer.parseInt(servingValueLabel.getText());
            if (current < 50) {
                int newValue = current + 1;
                updateServingSize(newValue);
            }
        });
        
        counterBox.getChildren().addAll(minusBtn, servingValueLabel, plusBtn);
        
        // Store reference to label for updates
        this.servingValueLabel = servingValueLabel;
        
        servingSizeControl.getChildren().addAll(servingLabel, counterBox);
        
        servingNote = new Label("Nutrition values per serving");
        servingNote.setWrapText(true);
        servingNote.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af; -fx-padding: 10 0 0 0;");
        
        nutritionCard.getChildren().addAll(nutTitle, calsBox, barsContainer, servingSizeControl, servingNote);

        rightCol.getChildren().addAll(actionsPanel, nutritionCard);
        grid.add(rightCol, 1, 0);
        
        return grid;
    }
    
    private VBox createNutrientBarUI(String label, String colorHex) {
        VBox container = new VBox(6);
        
        HBox header = new HBox();
        Label name = new Label(label);
        name.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label val = new Label("0g (0%)");
        val.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #111827;");
        
        header.getChildren().addAll(name, spacer, val);
        
        Progress bar = new Progress(0);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(8);
        // Custom styling for bar color
        bar.setStyle("-fx-accent: " + colorHex + "; -fx-control-inner-background: #f3f4f6; -fx-text-box-border: transparent; -fx-background-radius: 4px;");
        
        container.getChildren().addAll(header, bar);
        return container;
    }
    
    private Button createActionButton(String text, String type, String iconPath) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(45);
        btn.setCursor(Cursor.HAND);
        
        String baseStyle = "-fx-font-size: 14px; -fx-font-weight: 600; -fx-background-radius: 8px; -fx-alignment: center;";
        
        switch (type) {
            case "primary":
                btn.setStyle(baseStyle + "-fx-background-color: #4CAF50; -fx-text-fill: white;");
                break;
            case "secondary":
                btn.setStyle(baseStyle + "-fx-background-color: white; -fx-text-fill: #65a30d; -fx-border-color: #8BC34A; -fx-border-width: 1px; -fx-border-radius: 8px;");
                break;
            case "danger":
                btn.setStyle(baseStyle + "-fx-background-color: white; -fx-text-fill: #ef4444; -fx-border-color: #FF5252; -fx-border-width: 1px; -fx-border-radius: 8px;");
                break;
            default: // default/gray
                btn.setStyle(baseStyle + "-fx-background-color: white; -fx-text-fill: #4b5563; -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 8px;");
                break;
        }
        
        Node icon = SvgIconLoader.loadIcon(iconPath, 18, 
            type.equals("primary") ? Color.WHITE : 
            type.equals("secondary") ? Color.web("#65a30d") :
            type.equals("danger") ? Color.web("#ef4444") : Color.web("#4b5563"));
            
        if (icon != null) {
            btn.setGraphic(icon);
            btn.setGraphicTextGap(8);
        }
        return btn;
    }

    private VBox createCard() {
        VBox card = new VBox(16); // Padding handled by internal margins if needed, or set padding here
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 2, 0);");
        return card;
    }
    
    private HBox createCardHeader(String title, String iconPath) {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Node icon = SvgIconLoader.loadIcon(iconPath, 20, Color.web("#4CAF50")); // Green icon
        Label label = new Label(title);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #111827;");
        
        if (icon != null) header.getChildren().add(icon);
        header.getChildren().add(label);
        return header;
    }

    private void addMetaChip(String text, String iconPath) {
        HBox chip = new HBox(6);
        chip.setAlignment(Pos.CENTER);
        chip.setPadding(new Insets(6, 12, 6, 12));
        chip.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 20px;");
        
        Node icon = SvgIconLoader.loadIcon(iconPath, 14, Color.WHITE);
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 500;");
        
        if (icon != null) chip.getChildren().add(icon);
        chip.getChildren().add(label);
        
        metaChipsContainer.getChildren().add(chip);
    }

    private void updateView() {
        Recipe recipe = viewModel.getRecipe();
        if (recipe == null) {
            return;
        }
        
        try {
            // Update recipe name
            String recipeName = recipe.getName();
            if (recipeName != null && !recipeName.trim().isEmpty()) {
                recipeNameLabel.setText(recipeName);
            } else {
                recipeNameLabel.setText("Untitled Recipe");
            }
            
            // Update hero image (async to prevent blocking)
            String imageUrl = recipe.getImageUrl();
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                // Load image asynchronously to prevent blocking
                new Thread(() -> {
                    try {
                        Image image = imageCache.getImage(imageUrl);
                        Platform.runLater(() -> {
                            if (heroImageView != null) {
                                heroImageView.setImage(image);
                            }
                        });
                    } catch (Exception e) {
                        // If image loading fails, keep the placeholder
                        // Silently fail - placeholder will remain
                    }
                }).start();
            }
            
            // Update meta chips
            metaChipsContainer.getChildren().clear();
            
            // Actual cook time from recipe
            Integer cookTime = recipe.getCookTimeMinutes();
            String timeText = (cookTime != null && cookTime > 0)
                ? cookTime + " min"
                : "Time not available";
            addMetaChip(timeText, "/svg/clock.svg");
            
            // Calories from nutrition info
            String calText = "350 Cal"; // Default fallback
            if (recipe.getNutritionInfo() != null) {
                int calories = recipe.getNutritionInfo().getCalories();
                calText = calories + " Cal";
            }
            addMetaChip(calText, "/svg/fire-flame.svg");
            
            int servingSize = recipe.getServingSize();
            addMetaChip(servingSize + " Servings", "/svg/users.svg");
            
            // Update serving size label if it exists (set flag to prevent infinite loop)
            if (servingValueLabel != null) {
                isUpdatingFromViewModel = true;
                try {
                    servingValueLabel.setText(String.valueOf(servingSize));
                } finally {
                    isUpdatingFromViewModel = false;
                }
            }
            
            // Update serving note
            if (servingNote != null) {
                servingNote.setText("Nutrition values per serving (" + servingSize + " servings total)");
            }
            
            addMetaChip("Breakfast", "/svg/mug-hot.svg");
            
            // Ingredients
            ingredientsList.getChildren().clear();
            if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
                for (String ing : recipe.getIngredients()) {
                    if (ing == null || ing.trim().isEmpty()) {
                        continue; // Skip null or empty ingredients
                    }
                    
                    HBox item = new HBox(12);
                    item.setAlignment(Pos.TOP_LEFT);
                    
                    // Checkbox style circle
                    Circle checkCircle = new Circle(10);
                    checkCircle.setFill(Color.TRANSPARENT);
                    checkCircle.setStroke(Color.web("#e5e7eb"));
                    checkCircle.setStrokeWidth(2);
                    checkCircle.setCursor(Cursor.HAND);
                    
                    // Toggle logic visual only
                    checkCircle.setOnMouseClicked(e -> {
                        if (checkCircle.getFill() == Color.TRANSPARENT) {
                            checkCircle.setFill(Color.web("#4CAF50"));
                            checkCircle.setStroke(Color.web("#4CAF50"));
                        } else {
                            checkCircle.setFill(Color.TRANSPARENT);
                            checkCircle.setStroke(Color.web("#e5e7eb"));
                        }
                    });
                    
                    Label text = new Label(ing.trim());
                    text.setWrapText(true);
                    text.setStyle("-fx-font-size: 15px; -fx-text-fill: #374151; -fx-line-spacing: 4px;");
                    
                    item.getChildren().addAll(checkCircle, text);
                    ingredientsList.getChildren().add(item);
                }
            } else {
                // Show message if no ingredients
                Label noIngredientsLabel = new Label("No ingredients listed");
                noIngredientsLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 14px;");
                ingredientsList.getChildren().add(noIngredientsLabel);
            }
            
            // Instructions
            instructionsList.getChildren().clear();
            String stepsText = recipe.getSteps();
            if (stepsText != null && !stepsText.trim().isEmpty()) {
                String[] steps = stepsText.split("\n");
                int stepNum = 1;
                for (String step : steps) {
                    if (step == null || step.trim().isEmpty()) {
                        continue;
                    }
                    
                    HBox item = new HBox(16);
                    item.setAlignment(Pos.TOP_LEFT);
                    
                    // Number Badge
                    Label numBadge = new Label(String.valueOf(stepNum++));
                    numBadge.setPrefSize(28, 28);
                    numBadge.setMinSize(28, 28);
                    numBadge.setAlignment(Pos.CENTER);
                    numBadge.setStyle("-fx-background-color: #F0F2F5; -fx-background-radius: 50%; -fx-text-fill: #111827; -fx-font-weight: bold; -fx-font-size: 13px;");
                    
                    Label text = new Label(step.trim());
                    text.setWrapText(true);
                    text.setStyle("-fx-font-size: 15px; -fx-text-fill: #4b5563; -fx-line-spacing: 4px;");
                    
                    item.getChildren().addAll(numBadge, text);
                    instructionsList.getChildren().add(item);
                }
            } else {
                // Show message if no instructions
                Label noInstructionsLabel = new Label("No instructions available");
                noInstructionsLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 14px;");
                instructionsList.getChildren().add(noInstructionsLabel);
            }
            
            // Nutrition
            NutritionInfo info = recipe.getNutritionInfo();
            if (info != null) {
                caloriesValueLabel.setText(String.valueOf(info.getCalories()));

                // Get user's nutrition goals from SessionManager
                User currentUser = SessionManager.getInstance().getCurrentUser();
                NutritionGoals goals;

                if (currentUser != null && currentUser.getNutritionGoals() != null) {
                    goals = currentUser.getNutritionGoals();
                } else {
                    // Use default goals if user doesn't have custom goals set
                    goals = NutritionGoals.createDefault();
                }

                // Assume 1 meal is 1/3 of daily goals
                double maxProtein = goals.getDailyProtein() / 3.0;
                double maxCarbs = goals.getDailyCarbs() / 3.0;
                double maxFat = goals.getDailyFat() / 3.0;

                // Calculate progress and percentage (with division by zero protection)
                double proteinProgress = maxProtein > 0 ? info.getProtein() / maxProtein : 0.0;
                double proteinPercent = maxProtein > 0 ? (info.getProtein() / maxProtein) * 100 : 0.0;
                if (proteinBar != null && proteinVal != null) {
                    proteinBar.setProgress(Math.min(1.0, proteinProgress));
                    proteinVal.setText(String.format("%.0fg (%.0f%%)", info.getProtein(), proteinPercent));
                }

                double carbsProgress = maxCarbs > 0 ? info.getCarbs() / maxCarbs : 0.0;
                double carbsPercent = maxCarbs > 0 ? (info.getCarbs() / maxCarbs) * 100 : 0.0;
                if (carbsBar != null && carbsVal != null) {
                    carbsBar.setProgress(Math.min(1.0, carbsProgress));
                    carbsVal.setText(String.format("%.0fg (%.0f%%)", info.getCarbs(), carbsPercent));
                }

                double fatProgress = maxFat > 0 ? info.getFat() / maxFat : 0.0;
                double fatPercent = maxFat > 0 ? (info.getFat() / maxFat) * 100 : 0.0;
                if (fatBar != null && fatVal != null) {
                    fatBar.setProgress(Math.min(1.0, fatProgress));
                    fatVal.setText(String.format("%.0fg (%.0f%%)", info.getFat(), fatPercent));
                }
            }
        } catch (Exception e) {
            // Log error and show fallback UI
            System.err.println("Error updating recipe detail view: " + e.getMessage());
            e.printStackTrace();
            // At least show the recipe name if available
            if (recipe.getName() != null) {
                recipeNameLabel.setText(recipe.getName());
            }
        }
    }

    /**
     * Helper method to update serving size and trigger controller
     */
    private void updateServingSize(int newValue) {
        if (isUpdatingFromViewModel) {
            return; // Prevent infinite loop
        }
        
        if (servingValueLabel != null) {
            servingValueLabel.setText(String.valueOf(newValue));
        }
        
        Recipe recipe = viewModel.getRecipe();
        if (recipe != null) {
            String recipeId = String.valueOf(recipe.getRecipeId());
            controller.execute(recipeId, newValue);
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(() -> {
            if (RecipeDetailViewModel.PROP_RECIPE.equals(evt.getPropertyName())) {
                updateView();
            }
        });
    }

    private void navigateBack() {
        if (viewManagerModel == null) {
            return;
        }
        String target = viewManagerModel.getPreviousView();
        if (target == null || ViewManager.RECIPE_DETAIL_VIEW.equals(target)) {
            target = ViewManager.BROWSE_RECIPE_VIEW;
        }
        viewManagerModel.setActiveView(target);
    }
    
    /**
     * Phase 2: 레시피를 My Cookbook에 저장하는 기능
     */
    private void saveRecipeToCookbook(Recipe recipe) {
        if (storeRecipeController == null || recipe == null) {
            logger.error("Cannot save recipe: storeRecipeController or recipe is null");
            return;
        }
        
        try {
            // Recipe를 StoreRecipeInputData로 변환
            String recipeName = recipe.getName();
            if (recipeName == null || recipeName.trim().isEmpty()) {
                logger.warn("Recipe name is missing, cannot save");
                return;
            }
            
            List<String> ingredients = recipe.getIngredients() != null 
                ? new ArrayList<>(recipe.getIngredients())
                : new ArrayList<>();
            
            // steps는 String이므로 List<String>으로 변환
            List<String> steps = new ArrayList<>();
            if (recipe.getSteps() != null && !recipe.getSteps().trim().isEmpty()) {
                String[] stepArray = recipe.getSteps().split("\\r?\\n");
                for (String step : stepArray) {
                    String trimmed = step.trim();
                    if (!trimmed.isEmpty()) {
                        steps.add(trimmed);
                    }
                }
            }
            
            int servingSize = recipe.getServingSize();
            if (servingSize <= 0) {
                servingSize = 1; // 기본값
            }
            
            // 중복 체크
            try {
                List<Recipe> existingRecipes = recipeRepository.findByName(recipeName);
                if (existingRecipes != null && !existingRecipes.isEmpty()) {
                    boolean exactMatch = existingRecipes.stream()
                        .anyMatch(r -> r != null && r.getName() != null && r.getName().equalsIgnoreCase(recipeName));
                    
                    if (exactMatch) {
                        // 이미 저장된 경우 - Phase 3 완료: 업데이트 기능은 My Cookbook에서 사용 가능
                        logger.info("Recipe '{}' already exists in cookbook", recipeName);
                        // 사용자에게 알림
                        Platform.runLater(() -> {
                            Notifications.create()
                                .title("Already Saved")
                                .text("This recipe is already in your cookbook. You can edit it from My Cookbook.")
                                .showInformation();
                        });
                        return;
                    }
                }
            } catch (DataAccessException e) {
                // 중복 체크 실패해도 계속 진행
                logger.debug("Failed to check duplicate: {}", e.getMessage());
            }
            
            // StoreRecipeController 호출 (recipeId는 null로 새 레시피로 저장)
            storeRecipeController.execute(
                null,  // recipeId (null = 새 레시피)
                recipeName,
                ingredients,
                steps,
                servingSize
            );
            
            // 성공 메시지는 StoreRecipePresenter에서 처리됨
            logger.info("Recipe '{}' saved to cookbook successfully", recipeName);
            
            // Phase 2: 사용자에게 성공 피드백 제공
            Platform.runLater(() -> {
                Notifications.create()
                    .title("Saved!")
                    .text("Recipe saved to your cookbook successfully.")
                    .showInformation();
            });
            
        } catch (Exception e) {
            logger.error("Failed to save recipe to cookbook: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Clean up resources and remove property change listeners to prevent memory leaks.
     * Should be called when this view is no longer needed.
     */
    public void dispose() {
        if (viewModel != null) {
            viewModel.removePropertyChangeListener(this);
        }
        if (viewManagerModel != null) {
            viewManagerModel.removePropertyChangeListener(this);
        }
    }
}
