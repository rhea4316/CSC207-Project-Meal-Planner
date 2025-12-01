package com.mealplanner.view;

import com.mealplanner.entity.Recipe;
import com.mealplanner.exception.DataAccessException;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.StoreRecipeController;
import com.mealplanner.interface_adapter.view_model.RecipeStoreViewModel;
import com.mealplanner.repository.RecipeRepository;
import com.mealplanner.util.NumberUtil;
import com.mealplanner.util.StringUtil;
import com.mealplanner.util.ValidationUtil;
import com.mealplanner.util.IngredientParser;
import com.mealplanner.entity.NutritionInfo;
import com.mealplanner.view.component.*;
import com.mealplanner.view.util.SvgIconLoader;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

// ControlsFX imports
import org.controlsfx.control.SearchableComboBox;
import org.controlsfx.control.Notifications;

// ValidatorFX imports
import net.synedra.validatorfx.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class StoreRecipeView extends BorderPane implements PropertyChangeListener {
    
    private static final Logger logger = LoggerFactory.getLogger(StoreRecipeView.class);
    
    private final StoreRecipeController controller;
    @SuppressWarnings("unused")
    private final RecipeStoreViewModel viewModel;
    @SuppressWarnings("unused")
    private final ViewManagerModel viewManagerModel;
    private final RecipeRepository recipeRepository;

    // Editor Form Components
    private Input nameField;
    private TextArea descField;
    private Input imgUrlField;
    private SearchableComboBox<String> categoryCombo;
    private SearchableComboBox<String> difficultyCombo;
    
    private Input timeField;
    private Input caloriesField;
    private Spinner<Integer> servingSizeSpinner;
    
    // ValidatorFX
    private Validator validator;
    
    private Input proteinField;
    private Input carbsField;
    private Input fatField;
    
    // Serving size scaling state
    private int baseServingSize = 1;
    private List<String> baseIngredients = new ArrayList<>();
    private double baseCalories = 0;
    private double baseProtein = 0;
    private double baseCarbs = 0;
    private double baseFat = 0;
    private boolean isScaling = false; // Flag to prevent infinite loop
    
    // Dynamic Containers
    private FlowPane ingredientsContainer;
    private VBox instructionsContainer;
    private FlowPane tagsContainer;
    
    // Cookbook UI Components
    private VBox cookbookContent;
    private ScrollPane editorContent;
    private FlowPane cookbookGrid;
    private Label cookbookEmptyLabel;
    private List<Recipe> cookbookRecipes = new ArrayList<>();
    private Recipe editingRecipe;
    
    private Sonner sonner;
    
    // Loading state
    private Button saveBtn;
    private ProgressIndicator savingIndicator;
    private ProgressIndicator loadingIndicator;
    private volatile boolean isRefreshingCookbook = false; // Flag to prevent concurrent refresh

    public StoreRecipeView(StoreRecipeController controller, RecipeStoreViewModel viewModel, ViewManagerModel viewManagerModel, RecipeRepository recipeRepository) {
        if (controller == null) throw new IllegalArgumentException("Controller cannot be null");
        this.recipeRepository = Objects.requireNonNull(recipeRepository, "RecipeRepository cannot be null");
        
        this.controller = controller;
        this.viewModel = viewModel;
        this.viewManagerModel = viewManagerModel;

        if (viewModel != null) {
            viewModel.addPropertyChangeListener(this);
        }

        // Root Style
        getStyleClass().add("root");
        setBackground(new Background(new BackgroundFill(Color.web("#F5F7FA"), CornerRadii.EMPTY, Insets.EMPTY)));
        setPadding(new Insets(0)); // Reset padding for scroll layout

        // ValidatorFX 초기화
        validator = new Validator();

        // Initialize Views
        createCookbookView();
        createEditorView();
        
        // Setup Sonner
        sonner = new Sonner();
        refreshCookbook();
        
        // Set initial view
        setCenter(cookbookContent);
        setPadding(new Insets(30, 40, 30, 40)); // Default padding for cookbook view
    }
    
    private void toggleView(boolean showCookbook) {
        if (showCookbook) {
            setCenter(cookbookContent);
            setPadding(new Insets(30, 40, 30, 40));
            // Phase 3: Clear editing state when returning to cookbook
            editingRecipe = null;
        } else {
            setCenter(editorContent);
            setPadding(new Insets(0)); // Full width for editor scroll
            // Phase 3: Update UI based on edit mode
            updateEditorUI();
        }
    }
    
    /**
     * Phase 3: Update editor UI based on whether we're editing or creating
     */
    private void updateEditorUI() {
        Label editTitle = (Label) editorContent.lookup("#editor-title");
        Label editSub = (Label) editorContent.lookup("#editor-subtitle");
        
        if (editingRecipe != null) {
            // Edit mode
            if (editTitle != null) {
                editTitle.setText("Edit Recipe");
            }
            if (editSub != null) {
                editSub.setText("Update recipe details");
            }
            if (saveBtn != null) {
                saveBtn.setText("Update Recipe");
            }
        } else {
            // Create mode
            if (editTitle != null) {
                editTitle.setText("Create Recipe");
            }
            if (editSub != null) {
                editSub.setText("Add a new recipe to your cookbook");
            }
            if (saveBtn != null) {
                saveBtn.setText("Create Recipe");
            }
        }
    }

    // --- 1. Cookbook List View ---
    private void createCookbookView() {
        cookbookContent = new VBox(24);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(4);
        Label titleLabel = new Label("My Cookbook");
        titleLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-weight: bold; -fx-font-size: 24px; -fx-text-fill: #1A1A1A;");
        Label subLabel = new Label("Your personal collection of bookmarked and custom recipes");
        subLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-text-fill: #888888;");
        titles.getChildren().addAll(titleLabel, subLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button createBtn = new Button("Create Recipe");
        createBtn.setStyle("-fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 8px; -fx-padding: 10 20; -fx-cursor: hand; -fx-background-color: #68CA2A;");
        createBtn.setOnAction(e -> {
            // Phase 3: Clear editing state when creating new recipe
            editingRecipe = null;
            clearForm();
            toggleView(false);
        });
        header.getChildren().addAll(titles, spacer, createBtn);

        cookbookGrid = new FlowPane();
        cookbookGrid.setHgap(16);
        cookbookGrid.setVgap(16);
        cookbookGrid.setPadding(new Insets(10));

        cookbookEmptyLabel = new Label("You haven't saved any recipes yet.\nCreate your first recipe to get started!");
        cookbookEmptyLabel.getStyleClass().add("text-gray-500");
        cookbookEmptyLabel.setStyle("-fx-font-size: 14px; -fx-text-alignment: center; -fx-alignment: center;");
        cookbookEmptyLabel.setAlignment(Pos.CENTER);

        // Loading indicator for cookbook refresh
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(40, 40);
        loadingIndicator.setVisible(false);
        loadingIndicator.setManaged(false);
        loadingIndicator.setStyle("-fx-progress-color: #4CAF50;");
        
        StackPane listPane = new StackPane();
        listPane.getChildren().addAll(cookbookGrid, cookbookEmptyLabel, loadingIndicator);

        ScrollPane scrollPane = new ScrollPane(listPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        
        // Increase scroll speed
        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() != 0) {
                double delta = event.getDeltaY() * 3.0;
                double height = scrollPane.getContent().getBoundsInLocal().getHeight();
                double vHeight = scrollPane.getViewportBounds().getHeight();
                
                double scrollableHeight = height - vHeight;
                if (scrollableHeight > 0) {
                    double vValueShift = -delta / scrollableHeight;
                    double nextVvalue = scrollPane.getVvalue() + vValueShift;
                    
                    if (nextVvalue >= 0 && nextVvalue <= 1.0 || (scrollPane.getVvalue() > 0 && scrollPane.getVvalue() < 1.0)) {
                        scrollPane.setVvalue(Math.min(Math.max(nextVvalue, 0), 1));
                        event.consume();
                    }
                }
            }
        });

        cookbookContent.getChildren().addAll(header, scrollPane);
    }

    // --- 2. Recipe Editor View (New) ---
    private void createEditorView() {
        VBox mainContainer = new VBox();
        mainContainer.setStyle("-fx-background-color: #F5F7FA;");
        mainContainer.setPadding(new Insets(30, 40, 30, 40));
        
        // A. Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 24, 0));
        
        Button backBtn = new Button();
        backBtn.setStyle("-fx-background-color: white; -fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 4, 0, 0, 1); -fx-cursor: hand;");
        Node arrowIcon = SvgIconLoader.loadIcon("/svg/arrow-small-left.svg", 20, Color.GRAY);
        if (arrowIcon != null) backBtn.setGraphic(arrowIcon);
        backBtn.setOnAction(e -> toggleView(true));
        
        VBox titleBox = new VBox(2);
        // Phase 3: Dynamic title based on edit mode
        Label editTitle = new Label("Create Recipe"); // Will be updated in toggleView
        editTitle.setId("editor-title"); // For easy access
        editTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: #1A1A1A;");
        Label editSub = new Label("Add a new recipe to your cookbook"); // Will be updated in toggleView
        editSub.setId("editor-subtitle"); // For easy access
        editSub.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888;");
        titleBox.getChildren().addAll(editTitle, editSub);
        HBox.setMargin(titleBox, new Insets(0, 0, 0, 16));
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(
            "-fx-background-color: white; " +
            "-fx-text-fill: #68CA2A; " +
            "-fx-border-color: #68CA2A; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 10px 20px; " +
            "-fx-cursor: hand; " +
            "-fx-min-height: 40px; " +
            "-fx-pref-height: 40px;"
        );
        cancelBtn.setOnMouseEntered(e -> {
            cancelBtn.setStyle(
                "-fx-background-color: #f0fdf4; " +
                "-fx-text-fill: #68CA2A; " +
                "-fx-border-color: #68CA2A; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-font-weight: 600; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 10px 20px; " +
                "-fx-cursor: hand; " +
                "-fx-min-height: 40px; " +
                "-fx-pref-height: 40px;"
            );
        });
        cancelBtn.setOnMouseExited(e -> {
            cancelBtn.setStyle(
                "-fx-background-color: white; " +
                "-fx-text-fill: #68CA2A; " +
                "-fx-border-color: #68CA2A; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-font-weight: 600; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 10px 20px; " +
                "-fx-cursor: hand; " +
                "-fx-min-height: 40px; " +
                "-fx-pref-height: 40px;"
            );
        });
        cancelBtn.setOnAction(e -> {
            // Phase 3: Clear form and editing state on cancel (clearForm() already sets editingRecipe = null)
            clearForm();
            toggleView(true);
        });
        
        saveBtn = new Button("Create Recipe"); // Phase 3: Will be updated in toggleView
        saveBtn.getStyleClass().add("primary-button");
        saveBtn.setStyle(
            "-fx-text-fill: white; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 14px; " +
            "-fx-background-radius: 8px; " +
            "-fx-background-color: #68CA2A; " +
            "-fx-padding: 10px 20px; " +
            "-fx-cursor: hand; " +
            "-fx-min-height: 40px; " +
            "-fx-pref-height: 40px;"
        );
        saveBtn.setOnMouseEntered(e -> {
            saveBtn.setStyle(
                "-fx-text-fill: white; " +
                "-fx-font-weight: 600; " +
                "-fx-font-size: 14px; " +
                "-fx-background-radius: 8px; " +
                "-fx-background-color: #5ab023; " +
                "-fx-padding: 10px 20px; " +
                "-fx-cursor: hand; " +
                "-fx-min-height: 40px; " +
                "-fx-pref-height: 40px;"
            );
        });
        saveBtn.setOnMouseExited(e -> {
            saveBtn.setStyle(
                "-fx-text-fill: white; " +
                "-fx-font-weight: 600; " +
                "-fx-font-size: 14px; " +
                "-fx-background-radius: 8px; " +
                "-fx-background-color: #68CA2A; " +
                "-fx-padding: 10px 20px; " +
                "-fx-cursor: hand; " +
                "-fx-min-height: 40px; " +
                "-fx-pref-height: 40px;"
            );
        });
        saveBtn.setOnAction(e -> saveRecipe()); // Bind save action
        
        // Loading indicator for save button
        savingIndicator = new ProgressIndicator();
        savingIndicator.setPrefSize(16, 16);
        savingIndicator.setVisible(false);
        savingIndicator.setManaged(false);
        savingIndicator.setStyle("-fx-progress-color: white;");
        
        // Stack the indicator on top of the button
        StackPane saveBtnContainer = new StackPane();
        saveBtnContainer.getChildren().addAll(saveBtn, savingIndicator);
        saveBtnContainer.setAlignment(Pos.CENTER);
        
        HBox actionBtns = new HBox(10);
        actionBtns.setAlignment(Pos.CENTER_RIGHT);
        actionBtns.getChildren().addAll(cancelBtn, saveBtnContainer);
        
        header.getChildren().addAll(backBtn, titleBox, headerSpacer, actionBtns);
        
        // B. Content Grid (2 Columns)
        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(24);
        
        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(65);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(35);
        grid.getColumnConstraints().addAll(col1, col2);
        
        // -- Left Column (Ingredients & Instructions) --
        VBox leftCol = new VBox(24);
        
        // Ingredients Section
        VBox ingCard = createCardPanel("Ingredients");
        ingredientsContainer = new FlowPane();
        ingredientsContainer.setHgap(10); ingredientsContainer.setVgap(10);
        
        // Dynamic Add Row
        HBox ingAddRow = createDynamicInputRow("Add ingredient (press Enter to add)", text -> addChipItem(ingredientsContainer, text, false));
        
        ingCard.getChildren().addAll(ingredientsContainer, ingAddRow);
        
        // Instructions Section
        VBox instCard = createCardPanel("Instructions");
        instructionsContainer = new VBox(15);
        
        HBox instAddRow = createDynamicInputRow("Add instruction step (press Enter to add)", text -> addInstructionStep(text));
        
        instCard.getChildren().addAll(instructionsContainer, instAddRow);
        
        leftCol.getChildren().addAll(ingCard, instCard);
        
        // -- Right Column (Basic Info, etc) --
        VBox rightCol = new VBox(24);
        
        // Basic Information
        VBox basicCard = createCardPanel("Basic Information");
        nameField = new Input(); 
        nameField.setPromptText("Avocado Toast");
        
        descField = new TextArea(); 
        descField.setPromptText("Short description..."); 
        descField.setPrefRowCount(3); 
        descField.getStyleClass().add("text-area");
        
        imgUrlField = new Input(); 
        imgUrlField.setPromptText("https://image.url...");
        
        // ControlsFX SearchableComboBox 사용
        categoryCombo = new SearchableComboBox<>();
        categoryCombo.getItems().addAll("Breakfast", "Lunch", "Dinner", "Snack", "Dessert");
        categoryCombo.setValue("Breakfast");
        categoryCombo.setMaxWidth(Double.MAX_VALUE);
        categoryCombo.getStyleClass().add("text-field"); // Reuse text field style
        
        difficultyCombo = new SearchableComboBox<>();
        difficultyCombo.getItems().addAll("Easy", "Medium", "Hard");
        difficultyCombo.setValue("Easy");
        difficultyCombo.setMaxWidth(Double.MAX_VALUE);
        difficultyCombo.getStyleClass().add("text-field");

        basicCard.getChildren().addAll(
            createLabel("Recipe Name *"), nameField,
            createLabel("Description"), descField,
            createLabel("Image URL"), imgUrlField,
            createLabel("Category *"), categoryCombo,
            createLabel("Difficulty"), difficultyCombo
        );
        
        // Recipe Details (Time, Cals, Serving)
        VBox detailCard = createCardPanel("Recipe Details");
        timeField = new Input(); 
        timeField.setPromptText("e.g. 10 min");
        
        // Serving Size Spinner - Modern Design
        HBox servingSizeControl = new HBox(10);
        servingSizeControl.setAlignment(Pos.CENTER_LEFT);
        Label servingLabel = new Label("Servings:");
        servingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151; -fx-font-weight: 500;");
        
        servingSizeSpinner = new Spinner<>(1, 100, 1);
        servingSizeSpinner.setPrefWidth(120);
        servingSizeSpinner.setEditable(true);
        servingSizeSpinner.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-background-color: white; " +
            "-fx-background-radius: 8px; " +
            "-fx-border-color: #e5e7eb; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 8px; " +
            "-fx-padding: 8px 12px;"
        );
        
        // Style the spinner editor (text field inside spinner)
        servingSizeSpinner.getEditor().setStyle(
            "-fx-background-color: transparent; " +
            "-fx-border-width: 0; " +
            "-fx-padding: 0; " +
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #111827;"
        );
        
        // Style spinner buttons (increment/decrement) - Apply after spinner is added to scene
        Platform.runLater(() -> {
            Node incrementBtn = servingSizeSpinner.lookup(".increment-button");
            Node decrementBtn = servingSizeSpinner.lookup(".decrement-button");
            if (incrementBtn != null) {
                incrementBtn.setStyle(
                    "-fx-background-color: #f3f4f6; " +
                    "-fx-background-radius: 0 8px 0 0; " +
                    "-fx-border-color: #e5e7eb; " +
                    "-fx-border-width: 0 0 1px 0; " +
                    "-fx-cursor: hand;"
                );
            }
            if (decrementBtn != null) {
                decrementBtn.setStyle(
                    "-fx-background-color: #f3f4f6; " +
                    "-fx-background-radius: 0 0 8px 0; " +
                    "-fx-border-color: #e5e7eb; " +
                    "-fx-border-width: 1px 0 0 0; " +
                    "-fx-cursor: hand;"
                );
            }
        });
        
        // Hover effects for spinner
        servingSizeSpinner.setOnMouseEntered(e -> {
            servingSizeSpinner.setStyle(
                "-fx-font-size: 14px; " +
                "-fx-background-color: white; " +
                "-fx-background-radius: 8px; " +
                "-fx-border-color: #68CA2A; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 8px; " +
                "-fx-padding: 8px 12px;"
            );
        });
        servingSizeSpinner.setOnMouseExited(e -> {
            servingSizeSpinner.setStyle(
                "-fx-font-size: 14px; " +
                "-fx-background-color: white; " +
                "-fx-background-radius: 8px; " +
                "-fx-border-color: #e5e7eb; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 8px; " +
                "-fx-padding: 8px 12px;"
            );
        });
        
        // Add listener to scale ingredients and nutrition when serving size changes
        servingSizeSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isScaling && newVal != null && newVal > 0 && !newVal.equals(oldVal)) {
                scaleRecipeForServingSize(newVal);
            }
        });
        
        // Handle Enter key press in editable spinner
        servingSizeSpinner.getEditor().setOnAction(e -> {
            try {
                String text = servingSizeSpinner.getEditor().getText();
                if (text != null && !text.isEmpty()) {
                    int value = Integer.parseInt(text);
                    if (value > 0 && value <= 100) {
                        servingSizeSpinner.getValueFactory().setValue(value);
                    } else {
                        // Reset to valid range
                        int clampedValue = Math.max(1, Math.min(100, value));
                        servingSizeSpinner.getValueFactory().setValue(clampedValue);
                    }
                }
            } catch (NumberFormatException ex) {
                // Invalid input, reset to current value
                servingSizeSpinner.getValueFactory().setValue(servingSizeSpinner.getValue());
            }
        });
        
        servingSizeControl.getChildren().addAll(servingLabel, servingSizeSpinner);
        
        Label servingSizeHelpLabel = new Label("Serving size affects ingredient quantities and nutrition values");
        servingSizeHelpLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280; -fx-padding: 2 0 0 0;");
        servingSizeHelpLabel.setWrapText(true);
        
        detailCard.getChildren().addAll(
            createLabel("Time"), timeField,
            createLabel("Servings"), servingSizeControl, servingSizeHelpLabel
        );
        
        // ValidatorFX 검증 설정
        setupValidations();
        
        // Initialize base serving size
        baseServingSize = 1;
        
        // Nutrition (Manual Entry)
        VBox nutritionCard = createCardPanel("Nutrition (per serving)");
        caloriesField = new Input(); caloriesField.setPromptText("kcal");
        proteinField = new Input(); proteinField.setPromptText("g");
        carbsField = new Input(); carbsField.setPromptText("g");
        fatField = new Input(); fatField.setPromptText("g");
        
        // Add listeners to update base nutrition when user manually edits
        caloriesField.textProperty().addListener((obs, old, val) -> {
            if (!isScaling && val != null && !val.equals(old)) {
                Platform.runLater(() -> updateBaseNutrition());
            }
        });
        proteinField.textProperty().addListener((obs, old, val) -> {
            if (!isScaling && val != null && !val.equals(old)) {
                Platform.runLater(() -> updateBaseNutrition());
            }
        });
        carbsField.textProperty().addListener((obs, old, val) -> {
            if (!isScaling && val != null && !val.equals(old)) {
                Platform.runLater(() -> updateBaseNutrition());
            }
        });
        fatField.textProperty().addListener((obs, old, val) -> {
            if (!isScaling && val != null && !val.equals(old)) {
                Platform.runLater(() -> updateBaseNutrition());
            }
        });
        
        nutritionCard.getChildren().addAll(
            createLabel("Total Calories"), caloriesField,
            createLabel("Protein (g)"), proteinField,
            createLabel("Carbs (g)"), carbsField,
            createLabel("Fat (g)"), fatField
        );
        
        // Tags
        VBox tagCard = createCardPanel("Tags");
        tagsContainer = new FlowPane();
        tagsContainer.setHgap(8); tagsContainer.setVgap(8);
        
        HBox tagAddRow = createDynamicInputRow("Add a tag", text -> addChipItem(tagsContainer, text, true));
        
        tagCard.getChildren().addAll(tagsContainer, tagAddRow);
        
        rightCol.getChildren().addAll(basicCard, detailCard, nutritionCard, tagCard);
        
        grid.add(leftCol, 0, 0);
        grid.add(rightCol, 1, 0);
        
        mainContainer.getChildren().addAll(header, grid);
        
        editorContent = new ScrollPane(mainContainer);
        editorContent.setFitToWidth(true);
        editorContent.setStyle("-fx-background: #F5F7FA; -fx-background-color: #F5F7FA; -fx-padding: 0;");
        editorContent.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        // Increase scroll speed
        editorContent.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() != 0) {
                double delta = event.getDeltaY() * 3.0;
                double height = editorContent.getContent().getBoundsInLocal().getHeight();
                double vHeight = editorContent.getViewportBounds().getHeight();
                
                double scrollableHeight = height - vHeight;
                if (scrollableHeight > 0) {
                    double vValueShift = -delta / scrollableHeight;
                    double nextVvalue = editorContent.getVvalue() + vValueShift;
                    
                    if (nextVvalue >= 0 && nextVvalue <= 1.0 || (editorContent.getVvalue() > 0 && editorContent.getVvalue() < 1.0)) {
                        editorContent.setVvalue(Math.min(Math.max(nextVvalue, 0), 1));
                        event.consume();
                    }
                }
            }
        });
    }

    /**
     * Refresh the cookbook list with optional callback
     * @param onComplete Optional callback to execute after refresh completes
     */
    private void refreshCookbook(Runnable onComplete) {
        // Prevent concurrent refresh operations
        if (isRefreshingCookbook) {
            logger.debug("Cookbook refresh already in progress, skipping duplicate request");
            if (onComplete != null) {
                // If refresh is already in progress, execute callback after a short delay
                Platform.runLater(() -> {
                    try {
                        onComplete.run();
                    } catch (Exception e) {
                        logger.error("Error executing refreshCookbook callback", e);
                    }
                });
            }
            return;
        }
        
        if (recipeRepository == null) {
            logger.warn("RecipeRepository is null, cannot refresh cookbook");
            Platform.runLater(() -> {
                sonner.show("Error", "Recipe repository is not available", Sonner.Type.ERROR);
                if (onComplete != null) {
                    try {
                        onComplete.run();
                    } catch (Exception e) {
                        logger.error("Error executing refreshCookbook callback", e);
                    }
                }
            });
            return;
        }
        
        isRefreshingCookbook = true;
        
        // Show loading indicator
        Platform.runLater(() -> {
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(true);
                loadingIndicator.setManaged(true);
            }
        });
        
        new Thread(() -> {
            try {
                List<Recipe> recipes = recipeRepository.findAll();
                recipes.sort(Comparator.comparing(Recipe::getName, String.CASE_INSENSITIVE_ORDER));
                Platform.runLater(() -> {
                    try {
                        if (loadingIndicator != null) {
                            loadingIndicator.setVisible(false);
                            loadingIndicator.setManaged(false);
                        }
                        updateCookbook(recipes);
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    } catch (Exception e) {
                        logger.error("Error updating cookbook UI", e);
                        if (onComplete != null) {
                            try {
                                onComplete.run();
                            } catch (Exception ex) {
                                logger.error("Error executing refreshCookbook callback", ex);
                            }
                        }
                    } finally {
                        isRefreshingCookbook = false;
                    }
                });
            } catch (DataAccessException e) {
                logger.error("Data access error while loading recipes", e);
                Platform.runLater(() -> {
                    try {
                        if (loadingIndicator != null) {
                            loadingIndicator.setVisible(false);
                            loadingIndicator.setManaged(false);
                        }
                        sonner.show("Error", "Failed to load recipes. Please try again.", Sonner.Type.ERROR);
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    } catch (Exception ex) {
                        logger.error("Error handling data access exception", ex);
                    } finally {
                        isRefreshingCookbook = false;
                    }
                });
            } catch (Exception e) {
                logger.error("Unexpected error while loading recipes", e);
                Platform.runLater(() -> {
                    try {
                        if (loadingIndicator != null) {
                            loadingIndicator.setVisible(false);
                            loadingIndicator.setManaged(false);
                        }
                        sonner.show("Error", "An unexpected error occurred while loading recipes.", Sonner.Type.ERROR);
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    } catch (Exception ex) {
                        logger.error("Error handling unexpected exception", ex);
                    } finally {
                        isRefreshingCookbook = false;
                    }
                });
            }
        }).start();
    }
    
    /**
     * Refresh the cookbook list without callback
     */
    private void refreshCookbook() {
        refreshCookbook(null);
    }

    private void updateCookbook(List<Recipe> recipes) {
        cookbookRecipes = recipes != null ? recipes : new ArrayList<>();
        cookbookGrid.getChildren().clear();
        boolean hasRecipes = cookbookRecipes != null && !cookbookRecipes.isEmpty();
        cookbookEmptyLabel.setVisible(!hasRecipes);
        cookbookEmptyLabel.setManaged(!hasRecipes);
        if (!hasRecipes) {
            return;
        }
        for (Recipe recipe : cookbookRecipes) {
            cookbookGrid.getChildren().add(createCookbookCard(recipe));
        }
    }

    private VBox createCookbookCard(Recipe recipe) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card-panel");
        card.setPrefWidth(260);
        card.setMinHeight(180);
        card.setPadding(new Insets(16));

        String recipeName = recipe.getName() != null ? recipe.getName() : "Unnamed Recipe";
        Label nameLabel = new Label(recipeName);
        nameLabel.getStyleClass().add("text-gray-900");
        nameLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 16px;");
        nameLabel.setWrapText(true);

        Label servingsLabel = new Label("Serves " + recipe.getServingSize());
        servingsLabel.getStyleClass().add("text-gray-500");

        int ingredientCount = recipe.getIngredients() != null ? recipe.getIngredients().size() : 0;
        int stepCount = recipe.getSteps() != null ? (int) java.util.Arrays.stream(recipe.getSteps().split("\\n")).filter(s -> !s.isBlank()).count() : 0;

        Label metaLabel = new Label(ingredientCount + " ingredients · " + stepCount + " step(s)");
        metaLabel.getStyleClass().add("text-gray-400");

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button editBtn = new Button("Edit");
        editBtn.setStyle(
            "-fx-background-color: #68CA2A; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 14px; " +
            "-fx-background-radius: 6px; " +
            "-fx-padding: 8px 16px; " +
            "-fx-cursor: hand;"
        );
        editBtn.setOnMouseEntered(e -> {
            editBtn.setStyle(
                "-fx-background-color: #5ab023; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: 600; " +
                "-fx-font-size: 14px; " +
                "-fx-background-radius: 6px; " +
                "-fx-padding: 8px 16px; " +
                "-fx-cursor: hand;"
            );
        });
        editBtn.setOnMouseExited(e -> {
            editBtn.setStyle(
                "-fx-background-color: #68CA2A; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: 600; " +
                "-fx-font-size: 14px; " +
                "-fx-background-radius: 6px; " +
                "-fx-padding: 8px 16px; " +
                "-fx-cursor: hand;"
            );
        });
        editBtn.setOnAction(e -> {
            e.consume();
            openRecipeInEditor(recipe);
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle(
            "-fx-background-color: white; " +
            "-fx-text-fill: #ef4444; " +
            "-fx-border-color: #ef4444; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 6px; " +
            "-fx-background-radius: 6px; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 8px 16px; " +
            "-fx-cursor: hand;"
        );
        deleteBtn.setOnAction(e -> {
            e.consume();
            deleteRecipe(recipe);
        });

        actions.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(nameLabel, servingsLabel, metaLabel, actions);
        card.setOnMouseClicked(e -> openRecipeInEditor(recipe));
        return card;
    }
    
    private void openRecipeInEditor(Recipe recipe) {
        if (recipe == null) {
            return;
        }
        editingRecipe = recipe;
        populateEditorFromRecipe(recipe);
        toggleView(false);
        sonner.show("Editor", "Loaded '" + recipe.getName() + "' for editing", Sonner.Type.INFO);
    }

    private void deleteRecipe(Recipe recipe) {
        if (recipeRepository == null || recipe == null || StringUtil.isNullOrEmpty(recipe.getRecipeId())) {
            logger.warn("Cannot delete recipe: repository={}, recipe={}", recipeRepository != null, recipe != null);
            sonner.show("Error", "Unable to delete recipe", Sonner.Type.ERROR);
            return;
        }
        new Thread(() -> {
            try {
                boolean deleted = recipeRepository.delete(recipe.getRecipeId());
                Platform.runLater(() -> {
                    if (deleted) {
                        sonner.show("Deleted", "Removed '" + recipe.getName() + "'", Sonner.Type.SUCCESS);
                        refreshCookbook();
                    } else {
                        sonner.show("Not found", "Recipe could not be deleted", Sonner.Type.WARNING);
                    }
                });
            } catch (DataAccessException e) {
                logger.error("Data access error while deleting recipe: {}", recipe.getRecipeId(), e);
                Platform.runLater(() -> sonner.show("Error", "Failed to delete recipe. Please try again.", Sonner.Type.ERROR));
            } catch (Exception e) {
                logger.error("Unexpected error while deleting recipe: {}", recipe.getRecipeId(), e);
                Platform.runLater(() -> sonner.show("Error", "An unexpected error occurred while deleting recipe.", Sonner.Type.ERROR));
            }
        }).start();
    }

    private void populateEditorFromRecipe(Recipe recipe) {
        if (recipe == null) {
            return;
        }
        nameField.setText(recipe.getName());
        
        // Set serving size spinner
        int servingSize = recipe.getServingSize();
        if (servingSizeSpinner != null) {
            isScaling = true;
            try {
                servingSizeSpinner.getValueFactory().setValue(servingSize);
            } finally {
                isScaling = false;
            }
        }
        
        // Store base values for scaling
        baseServingSize = servingSize;
        baseIngredients = new ArrayList<>(recipe.getIngredients());
        
        // Load image URL if available
        if (recipe.getImageUrl() != null && !recipe.getImageUrl().trim().isEmpty()) {
            imgUrlField.setText(recipe.getImageUrl());
        } else {
            imgUrlField.clear();
        }
        
        // Load cook time if available
        if (recipe.getCookTimeMinutes() != null) {
            timeField.setText(String.valueOf(recipe.getCookTimeMinutes()) + " min");
        } else {
            timeField.clear();
        }
        
        // Load nutrition info if available
        // Note: Recipe's NutritionInfo is for the entire recipe (all servings)
        // We need to convert to per-serving values for the editor
        if (recipe.getNutritionInfo() != null) {
            NutritionInfo nutrition = recipe.getNutritionInfo();
            int recipeServingSize = recipe.getServingSize();
            
            // Convert total nutrition to per-serving (divide by serving size)
            if (recipeServingSize > 0) {
                baseCalories = (double) nutrition.getCalories() / recipeServingSize;
                baseProtein = nutrition.getProtein() / recipeServingSize;
                baseCarbs = nutrition.getCarbs() / recipeServingSize;
                baseFat = nutrition.getFat() / recipeServingSize;
            } else {
                // Fallback if serving size is invalid
                baseCalories = nutrition.getCalories();
                baseProtein = nutrition.getProtein();
                baseCarbs = nutrition.getCarbs();
                baseFat = nutrition.getFat();
            }
            
            // Display per-serving values
            caloriesField.setText(String.valueOf((int) Math.round(baseCalories)));
            proteinField.setText(String.valueOf((int) Math.round(baseProtein)));
            carbsField.setText(String.valueOf((int) Math.round(baseCarbs)));
            fatField.setText(String.valueOf((int) Math.round(baseFat)));
        } else {
            baseCalories = 0;
            baseProtein = 0;
            baseCarbs = 0;
            baseFat = 0;
            caloriesField.clear();
            proteinField.clear();
            carbsField.clear();
            fatField.clear();
        }
        
        // Description field is not part of Recipe entity, so keep it empty
        descField.clear();
        
        // Category and difficulty are not part of Recipe entity, so use defaults
        categoryCombo.setValue("Breakfast");
        difficultyCombo.setValue("Easy");

        ingredientsContainer.getChildren().clear();
        if (recipe.getIngredients() != null) {
            for (String ing : recipe.getIngredients()) {
                addChipItem(ingredientsContainer, ing, false);
            }
        }

        instructionsContainer.getChildren().clear();
        if (recipe.getSteps() != null) {
            String[] steps = recipe.getSteps().split("\\n");
            for (String step : steps) {
                if (!step.isBlank()) {
                    addInstructionStep(step.trim());
                }
            }
        }
        
        // Tags are not part of Recipe entity (dietaryRestrictions could be used but not implemented in UI)
        tagsContainer.getChildren().clear();
    }
    
    // --- Helper Methods for Editor Components ---
    
    private VBox createCardPanel(String title) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card-panel");
        card.setPadding(new Insets(20));
        
        if (title != null) {
            Label lbl = new Label(title);
            lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #374151;");
            card.getChildren().add(lbl);
        }
        return card;
    }
    
    private Label createLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280; -fx-padding: 5 0 2 0;");
        return l;
    }
    
    /**
     * ValidatorFX를 사용한 통합 검증 설정
     */
    private void setupValidations() {
        // Recipe Name 검증
        validator.createCheck()
            .dependsOn("name", nameField.textProperty())
            .withMethod(context -> {
                String name = context.get("name");
                if (StringUtil.isNullOrEmpty(name)) {
                    context.error("Recipe name is required");
                } else if (!ValidationUtil.validateRecipeName(name)) {
                    context.error("Recipe name must be between 1 and 100 characters");
                }
            })
            .decorates(nameField)
            .immediate();

        // Description 검증 (선택사항이지만 길이 제한 있음)
        validator.createCheck()
            .dependsOn("description", descField.textProperty())
            .withMethod(context -> {
                String desc = context.get("description");
                if (desc != null && !ValidationUtil.validateRecipeDescription(desc)) {
                    int currentLength = desc.length();
                    context.error(String.format("Description is too long (%d/500 characters)", currentLength));
                }
            })
            .decorates(descField)
            .immediate();

        // Serving Size 검증 (Spinner는 자동으로 범위 검증하지만, 추가 검증 필요시)
        // Spinner는 이미 1-100 범위로 제한되어 있으므로 추가 검증은 선택적
    }
    
    private HBox createDynamicInputRow(String placeholder, java.util.function.Consumer<String> onAdd) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        TextField input = new TextField();
        input.setPromptText(placeholder);
        input.getStyleClass().add("text-field");
        HBox.setHgrow(input, Priority.ALWAYS);
        
        // Get input field height to match button height
        input.heightProperty().addListener((obs, oldHeight, newHeight) -> {
            if (newHeight.doubleValue() > 0) {
                // Button height will be set to match input height
            }
        });
        
        Button addBtn = new Button("Add");
        // Match button height with input field (TextField default is ~32px with padding)
        addBtn.setPrefHeight(32);
        addBtn.setMinHeight(32);
        addBtn.setMaxHeight(32);
        addBtn.setStyle(
            "-fx-background-color: #68CA2A; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 14px; " +
            "-fx-background-radius: 8px; " +
            "-fx-padding: 0 16px; " +
            "-fx-cursor: hand;"
        );
        addBtn.setOnMouseEntered(e -> {
            addBtn.setStyle(
                "-fx-background-color: #5ab023; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: 600; " +
                "-fx-font-size: 14px; " +
                "-fx-background-radius: 8px; " +
                "-fx-padding: 0 16px; " +
                "-fx-cursor: hand;"
            );
        });
        addBtn.setOnMouseExited(e -> {
            addBtn.setStyle(
                "-fx-background-color: #68CA2A; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: 600; " +
                "-fx-font-size: 14px; " +
                "-fx-background-radius: 8px; " +
                "-fx-padding: 0 16px; " +
                "-fx-cursor: hand;"
            );
        });
        Node plusIcon = SvgIconLoader.loadIcon("/svg/plus.svg", 14, Color.WHITE);
        if (plusIcon != null) {
            addBtn.setGraphic(plusIcon);
            addBtn.setGraphicTextGap(6);
        }
        
        Runnable triggerAdd = () -> {
            String text = input.getText().trim();
            if (!text.isEmpty()) {
                onAdd.accept(text);
                input.clear();
            }
        };
        
        addBtn.setOnAction(e -> triggerAdd.run());
        input.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) triggerAdd.run();
        });
        
        row.getChildren().addAll(input, addBtn);
        return row;
    }
    
    // Core 1: Editable Chip
    private void addChipItem(FlowPane container, String text, boolean isTag) {
        HBox chip = new HBox(5);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.getStyleClass().add(isTag ? "tag-chip" : "ingredient-chip");
        
        TextField input = new TextField(text);
        input.getStyleClass().add("chip-input");
        // Auto-resize width roughly based on text length (simple logic)
        input.setPrefWidth(Math.max(60, text.length() * 8 + 20));
        input.textProperty().addListener((obs, old, val) -> {
            input.setPrefWidth(Math.max(60, val.length() * 8 + 20));
            // Update base ingredients when user manually edits (only for ingredient chips)
            if (!isTag && !isScaling) {
                Platform.runLater(() -> updateBaseIngredients());
            }
        });
        
        Button deleteBtn = new Button("✕"); // or load icon
        deleteBtn.getStyleClass().add("chip-delete-btn");
        deleteBtn.setVisible(false);
        deleteBtn.setOnAction(e -> {
            container.getChildren().remove(chip);
            // Update base ingredients when ingredient is deleted
            if (!isTag && !isScaling) {
                Platform.runLater(() -> updateBaseIngredients());
            }
        });
        
        // Hover logic
        chip.setOnMouseEntered(e -> deleteBtn.setVisible(true));
        chip.setOnMouseExited(e -> {
            if (!input.isFocused()) deleteBtn.setVisible(false);
        });
        
        chip.getChildren().addAll(input, deleteBtn);
        container.getChildren().add(chip);
    }
    
    // Core 2: Instruction Item
    private void addInstructionStep(String text) {
        int stepNum = instructionsContainer.getChildren().size() + 1;
        
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("instruction-row");
        
        Label badge = new Label(String.valueOf(stepNum));
        badge.getStyleClass().add("instruction-badge");
        
        HBox inputBox = new HBox();
        inputBox.getStyleClass().add("instruction-input-box");
        HBox.setHgrow(inputBox, Priority.ALWAYS);
        
        TextField input = new TextField(text);
        input.getStyleClass().add("chip-input");
        input.setPromptText("Enter instruction step...");
        HBox.setHgrow(input, Priority.ALWAYS);
        
        Button deleteBtn = new Button("✕");
        deleteBtn.getStyleClass().add("chip-delete-btn");
        deleteBtn.setVisible(false);
        deleteBtn.setOnAction(e -> {
            instructionsContainer.getChildren().remove(row);
            renumberSteps();
        });
        
        inputBox.getChildren().addAll(input, deleteBtn);
        
        // Hover logic
        inputBox.setOnMouseEntered(e -> deleteBtn.setVisible(true));
        inputBox.setOnMouseExited(e -> {
            if (!input.isFocused()) deleteBtn.setVisible(false);
        });
        
        row.getChildren().addAll(badge, inputBox);
        instructionsContainer.getChildren().add(row);
    }
    
    private void renumberSteps() {
        int count = 1;
        for (Node node : instructionsContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox row = (HBox) node;
                if (!row.getChildren().isEmpty() && row.getChildren().get(0) instanceof Label) {
                    ((Label) row.getChildren().get(0)).setText(String.valueOf(count++));
                }
            }
        }
    }

    private void saveRecipe() {
        // ValidatorFX로 폼 검증
        if (!validator.validate()) {
            // ControlsFX Notification으로 에러 표시
            Notifications.create()
                .title("Validation Error")
                .text("Please fix the errors in the form")
                .showError();
            return;
        }
        
        // Show loading state
        saveBtn.setDisable(true);
        savingIndicator.setVisible(true);
        savingIndicator.setManaged(true);
        saveBtn.setText("Saving...");
        
        String name = StringUtil.safeTrim(nameField.getText());
        
        // Harvest Ingredients
        List<String> ingredients = harvestChips(ingredientsContainer);
        
        // Harvest Steps
        List<String> steps = new ArrayList<>();
        for (Node node : instructionsContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox row = (HBox) node;
                if (row.getChildren().size() > 1 && row.getChildren().get(1) instanceof HBox) {
                    HBox inputBox = (HBox) row.getChildren().get(1);
                    if (!inputBox.getChildren().isEmpty() && inputBox.getChildren().get(0) instanceof TextField) {
                        String s = ((TextField) inputBox.getChildren().get(0)).getText();
                        if (!s.isBlank()) steps.add(s.trim());
                    }
                }
            }
        }
        
        int servingSize = servingSizeSpinner != null ? servingSizeSpinner.getValue() : 1;
        
        // TODO: StoreRecipeController와 StoreRecipeInputData가 다음 필드들을 지원하도록 확장 필요:
        // - imageUrl: imgUrlField에서 수집
        // - cookTimeMinutes: timeField에서 파싱 (예: "10 min" -> 10)
        // - nutritionInfo: caloriesField, proteinField, carbsField, fatField에서 수집
        // 현재는 controller.execute()가 이 필드들을 받지 않으므로, 기본 동작만 유지
        String imageUrl = StringUtil.safeTrim(imgUrlField.getText());
        String timeText = StringUtil.safeTrim(timeField.getText());
        // Parse time (e.g., "10 min" -> 10)
        Integer cookTimeMinutes = null;
        if (!timeText.isEmpty()) {
            // Remove "min" and extract number
            String timeNum = timeText.replaceAll("(?i)\\s*min\\s*", "").trim();
            int parsedTime = NumberUtil.parseInt(timeNum, -1);
            if (parsedTime > 0) {
                cookTimeMinutes = parsedTime;
            }
        }
        
        // Parse nutrition values
        int calories = NumberUtil.parseInt(caloriesField.getText(), 0);
        double protein = NumberUtil.parseDouble(proteinField.getText(), 0.0);
        double carbs = NumberUtil.parseDouble(carbsField.getText(), 0.0);
        double fat = NumberUtil.parseDouble(fatField.getText(), 0.0);
        
        // Note: Currently StoreRecipeController.execute() only accepts basic fields
        // The nutrition, imageUrl, and cookTimeMinutes are collected but not passed
        // This is a placeholder for future enhancement when the use case layer supports these fields
        String recipeId = editingRecipe != null ? editingRecipe.getRecipeId() : null;
        controller.execute(recipeId, name, ingredients, steps, servingSize);
        
        // Log collected optional fields for debugging
        if (logger.isDebugEnabled()) {
            logger.debug("Collected optional fields - imageUrl: {}, cookTime: {}, calories: {}, protein: {}, carbs: {}, fat: {}",
                imageUrl, cookTimeMinutes, calories, protein, carbs, fat);
        }
    }
    
    private void resetSaveButton() {
        Platform.runLater(() -> {
            saveBtn.setDisable(false);
            savingIndicator.setVisible(false);
            savingIndicator.setManaged(false);
            // Phase 3: Update button text based on edit mode
            if (editingRecipe != null) {
                saveBtn.setText("Update Recipe");
            } else {
                saveBtn.setText("Create Recipe");
            }
        });
    }
    
    private List<String> harvestChips(FlowPane container) {
        List<String> list = new ArrayList<>();
        for (Node node : container.getChildren()) {
            if (node instanceof HBox) {
                HBox chip = (HBox) node;
                if (!chip.getChildren().isEmpty() && chip.getChildren().get(0) instanceof TextField) {
                    String s = ((TextField) chip.getChildren().get(0)).getText();
                    if (!s.isBlank()) list.add(s.trim());
                }
            }
        }
        return list;
    }

    private void clearForm() {
        nameField.clear(); descField.clear(); imgUrlField.clear();
        ingredientsContainer.getChildren().clear();
        instructionsContainer.getChildren().clear();
        tagsContainer.getChildren().clear();
        
        // Reset serving size spinner
        if (servingSizeSpinner != null) {
            isScaling = true;
            try {
                servingSizeSpinner.getValueFactory().setValue(1);
            } finally {
                isScaling = false;
            }
        }
        
        // Reset base values
        baseServingSize = 1;
        baseIngredients.clear();
        baseCalories = 0;
        baseProtein = 0;
        baseCarbs = 0;
        baseFat = 0;
        
        timeField.clear(); caloriesField.clear(); proteinField.clear(); carbsField.clear(); fatField.clear();
        categoryCombo.setValue("Breakfast");
        difficultyCombo.setValue("Easy");
        editingRecipe = null;
    }
    
    /**
     * Scale recipe ingredients and nutrition based on serving size change.
     * This method updates the UI to reflect the new serving size.
     */
    private void scaleRecipeForServingSize(int newServingSize) {
        if (newServingSize <= 0) {
            return;
        }
        
        // Calculate scale factor
        double scaleFactor = (double) newServingSize / baseServingSize;
        
        // Scale ingredients
        if (!baseIngredients.isEmpty()) {
            // Update ingredient chips with scaled values
            List<String> scaledIngredients = new ArrayList<>();
            for (String ingredient : baseIngredients) {
                try {
                    String scaled = IngredientParser.scaleIngredient(ingredient, scaleFactor);
                    scaledIngredients.add(scaled);
                } catch (Exception e) {
                    // If scaling fails, keep original
                    logger.warn("Failed to scale ingredient '{}': {}", ingredient, e.getMessage());
                    scaledIngredients.add(ingredient);
                }
            }
            
            // Update UI
            ingredientsContainer.getChildren().clear();
            for (String ing : scaledIngredients) {
                addChipItem(ingredientsContainer, ing, false);
            }
        } else {
            // If base ingredients not set, harvest current ingredients as base
            List<String> currentIngredients = harvestChips(ingredientsContainer);
            if (!currentIngredients.isEmpty()) {
                baseIngredients = new ArrayList<>(currentIngredients);
                baseServingSize = newServingSize;
                
                // Recursively call with same serving size to update base
                scaleRecipeForServingSize(newServingSize);
                return;
            }
        }
        
        // Scale nutrition values
        if (baseCalories > 0 || baseProtein > 0 || baseCarbs > 0 || baseFat > 0) {
            double scaledCalories = baseCalories * scaleFactor;
            double scaledProtein = baseProtein * scaleFactor;
            double scaledCarbs = baseCarbs * scaleFactor;
            double scaledFat = baseFat * scaleFactor;
            
            caloriesField.setText(String.valueOf((int) Math.round(scaledCalories)));
            proteinField.setText(String.valueOf((int) Math.round(scaledProtein)));
            carbsField.setText(String.valueOf((int) Math.round(scaledCarbs)));
            fatField.setText(String.valueOf((int) Math.round(scaledFat)));
        } else {
            // If base nutrition not set, harvest current values as base
            try {
                String calText = caloriesField.getText();
                String protText = proteinField.getText();
                String carbText = carbsField.getText();
                String fatText = fatField.getText();
                
                if (!StringUtil.isNullOrEmpty(calText) || !StringUtil.isNullOrEmpty(protText) || 
                    !StringUtil.isNullOrEmpty(carbText) || !StringUtil.isNullOrEmpty(fatText)) {
                    baseCalories = NumberUtil.parseDouble(calText, 0);
                    baseProtein = NumberUtil.parseDouble(protText, 0);
                    baseCarbs = NumberUtil.parseDouble(carbText, 0);
                    baseFat = NumberUtil.parseDouble(fatText, 0);
                    baseServingSize = newServingSize;
                    
                    // Recursively call with same serving size to update base
                    scaleRecipeForServingSize(newServingSize);
                    return;
                }
            } catch (Exception e) {
                logger.warn("Failed to parse nutrition values for scaling: {}", e.getMessage());
            }
        }
        
        // Update base serving size for next scaling operation
        baseServingSize = newServingSize;
    }
    
    /**
     * Store current ingredient values as base for future scaling.
     * Should be called when user manually edits ingredients.
     */
    private void updateBaseIngredients() {
        List<String> currentIngredients = harvestChips(ingredientsContainer);
        if (!currentIngredients.isEmpty()) {
            baseIngredients = new ArrayList<>(currentIngredients);
            if (servingSizeSpinner != null) {
                baseServingSize = servingSizeSpinner.getValue();
            }
        }
    }
    
    /**
     * Store current nutrition values as base for future scaling.
     * Should be called when user manually edits nutrition values.
     */
    private void updateBaseNutrition() {
        try {
            String calText = caloriesField.getText();
            String protText = proteinField.getText();
            String carbText = carbsField.getText();
            String fatText = fatField.getText();
            
            if (!StringUtil.isNullOrEmpty(calText)) {
                baseCalories = NumberUtil.parseDouble(calText, 0);
            }
            if (!StringUtil.isNullOrEmpty(protText)) {
                baseProtein = NumberUtil.parseDouble(protText, 0);
            }
            if (!StringUtil.isNullOrEmpty(carbText)) {
                baseCarbs = NumberUtil.parseDouble(carbText, 0);
            }
            if (!StringUtil.isNullOrEmpty(fatText)) {
                baseFat = NumberUtil.parseDouble(fatText, 0);
            }
            
            if (servingSizeSpinner != null) {
                baseServingSize = servingSizeSpinner.getValue();
            }
        } catch (Exception e) {
            logger.warn("Failed to update base nutrition values: {}", e.getMessage());
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(() -> {
            if (RecipeStoreViewModel.PROP_SUCCESS_MESSAGE.equals(evt.getPropertyName())) {
                resetSaveButton();
                String message = (String) evt.getNewValue();
                if (message != null && sonner != null) {
                    sonner.show("Success", message, Sonner.Type.SUCCESS);
                }
                clearForm();
                // Phase 3: clearForm() already clears editingRecipe, refresh cookbook and return to list
                // Use Platform.runLater to ensure toggleView is called after refreshCookbook completes
                refreshCookbook(() -> Platform.runLater(() -> toggleView(true))); // Return to list on success
            } else if (RecipeStoreViewModel.PROP_ERROR_MESSAGE.equals(evt.getPropertyName())) {
                resetSaveButton();
                String message = (String) evt.getNewValue();
                if (message != null && sonner != null) {
                    sonner.show("Error", message, Sonner.Type.ERROR);
                }
            }
        });
    }
    
    /**
     * Clean up resources and remove property change listeners to prevent memory leaks.
     * Should be called when this view is no longer needed.
     */
    public void dispose() {
        if (viewModel != null) {
            viewModel.removePropertyChangeListener(this);
        }
    }
}
