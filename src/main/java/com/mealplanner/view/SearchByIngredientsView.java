package com.mealplanner.view;

import com.mealplanner.entity.Recipe;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.SearchByIngredientsController;
import com.mealplanner.interface_adapter.view_model.RecipeSearchViewModel;
import com.mealplanner.util.StringUtil;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class SearchByIngredientsView extends BorderPane implements PropertyChangeListener {
    private final RecipeSearchViewModel viewModel;
    private final SearchByIngredientsController controller;
    @SuppressWarnings("unused")
    private final ViewManagerModel viewManagerModel;

    private TextArea ingredientsTextArea;
    private Button searchButton;

    // Result Components
    private StackPane resultsContainer; 
    private VBox listPanel; 
    private VBox loadingPanel;
    private VBox emptyPanel;
    private Label errorLabel;

    public SearchByIngredientsView(SearchByIngredientsController controller, RecipeSearchViewModel viewModel, ViewManagerModel viewManagerModel) {
        if (viewModel == null) throw new IllegalArgumentException("ViewModel cannot be null");
        if (controller == null) throw new IllegalArgumentException("Controller cannot be null");

        this.viewModel = viewModel;
        this.controller = controller;
        this.viewManagerModel = viewManagerModel;

        viewModel.addPropertyChangeListener(this);

        setPadding(new Insets(20));
        getStyleClass().add("bg-white");

        // Title
        Label titleLabel = new Label("Search by Ingredients");
        titleLabel.getStyleClass().add("title-label");
        
        // Search Panel
        VBox searchPanel = createSearchPanel();
        
        VBox topBox = new VBox(10);
        topBox.getChildren().addAll(titleLabel, searchPanel);
        setTop(topBox);

        // Results Area
        createResultsPanel();
        setCenter(resultsContainer);
        
        // Error Label
        errorLabel = new Label("");
        errorLabel.getStyleClass().add("error-label");
        setBottom(errorLabel);
    }

    private VBox createSearchPanel() {
        VBox panel = new VBox(10);
        
        Label label = new Label("Enter ingredients (comma or newline separated):");
        
        ingredientsTextArea = new TextArea();
        ingredientsTextArea.setPrefRowCount(3);
        ingredientsTextArea.setWrapText(true);
        
        searchButton = new Button("Search Recipes");
        searchButton.getStyleClass().add("modern-button");
        searchButton.setOnAction(e -> performSearch());
        
        HBox btnBox = new HBox(searchButton);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        panel.getChildren().addAll(label, ingredientsTextArea, btnBox);
        return panel;
    }

    private void createResultsPanel() {
        resultsContainer = new StackPane();
        resultsContainer.setPadding(new Insets(20, 0, 0, 0));

        // 1. List View
        listPanel = new VBox(15);
        ScrollPane scrollPane = new ScrollPane(listPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane-transparent");

        // 2. Loading View
        loadingPanel = new VBox();
        loadingPanel.setAlignment(Pos.CENTER);
        Label loadingLabel = new Label("Loading...");
        loadingLabel.getStyleClass().add("loading-label");
        loadingPanel.getChildren().add(loadingLabel);

        // 3. Empty View
        emptyPanel = new VBox();
        emptyPanel.setAlignment(Pos.CENTER);
        Label emptyLabel = new Label("No results found.");
        emptyLabel.getStyleClass().add("empty-label");
        emptyPanel.getChildren().add(emptyLabel);

        resultsContainer.getChildren().addAll(emptyPanel, loadingPanel, scrollPane);
        
        showView("EMPTY");
    }
    
    private void showView(String viewName) {
        loadingPanel.setVisible(false);
        emptyPanel.setVisible(false);
        listPanel.getParent().setVisible(false); // Hide scrollpane
        
        switch(viewName) {
            case "LOADING": loadingPanel.setVisible(true); break;
            case "EMPTY": emptyPanel.setVisible(true); break;
            case "LIST": listPanel.getParent().setVisible(true); break;
        }
    }

    private void performSearch() {
        String ingredientsRaw = StringUtil.safeTrim(ingredientsTextArea.getText());

        if (StringUtil.isNullOrEmpty(ingredientsRaw)) {
            errorLabel.setText("Please enter at least one ingredient");
            return;
        }
        
        errorLabel.setText("");
        viewModel.setLoading(true);
        viewModel.setErrorMessage("");
        
        controller.execute(ingredientsRaw);
    }

    private void displayRecipes(List<Recipe> recipes) {
        listPanel.getChildren().clear();

        if (recipes == null || recipes.isEmpty()) {
            showView("EMPTY");
        } else {
            for (Recipe recipe : recipes) {
                listPanel.getChildren().add(createRecipeCard(recipe));
            }
            showView("LIST");
        }
    }

    private HBox createRecipeCard(Recipe recipe) {
        HBox card = new HBox(15);
        card.getStyleClass().add("card-panel");
        card.setAlignment(Pos.CENTER_LEFT);

        // Placeholder Image
        Region imgPlaceholder = new Region();
        imgPlaceholder.setPrefSize(80, 80);
        imgPlaceholder.getStyleClass().add("image-panel");
        card.getChildren().add(imgPlaceholder);

        // Info
        VBox infoBox = new VBox(5);
        Label title = new Label(recipe.getName());
        title.getStyleClass().add("recipe-card-title");
        
        String summary = "Ingredients: " + recipe.getIngredients().size();
        if (recipe.getNutritionInfo() != null) {
            summary += " | " + recipe.getNutritionInfo().getCalories() + " kcal";
        }
        Label subTitle = new Label(summary);
        subTitle.getStyleClass().add("recipe-card-subtitle");
        
        infoBox.getChildren().addAll(title, subTitle);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        card.getChildren().add(infoBox);

        // Button
        Button viewBtn = new Button("View");
        viewBtn.getStyleClass().add("modern-button");
        viewBtn.setOnAction(e -> System.out.println("View Recipe: " + recipe.getName()));
        card.getChildren().add(viewBtn);

        return card;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(() -> {
            String propertyName = evt.getPropertyName();
            
            if (RecipeSearchViewModel.PROP_LOADING.equals(propertyName)) {
                if (viewModel.isLoading()) {
                    showView("LOADING");
                    searchButton.setDisable(true);
                } else {
                    searchButton.setDisable(false);
                }
            } else if (RecipeSearchViewModel.PROP_RECIPES.equals(propertyName)) {
                displayRecipes(viewModel.getRecipes());
            } else if (RecipeSearchViewModel.PROP_ERROR_MESSAGE.equals(propertyName)) {
                String errorMsg = viewModel.getErrorMessage();
                if (StringUtil.hasContent(errorMsg)) {
                    errorLabel.setText(errorMsg);
                } else {
                    errorLabel.setText("");
                }
            }
        });
    }
}
