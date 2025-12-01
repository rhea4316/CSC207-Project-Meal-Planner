package com.mealplanner.view.component;

import com.mealplanner.entity.Recipe;
import com.mealplanner.repository.RecipeRepository;
import com.mealplanner.util.ImageCacheManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

/**
 * Dialog for selecting a recipe from the repository.
 * Used when adding meals to schedule from dashboard.
 */
public class SelectRecipeDialog {
    
    private static final Logger logger = LoggerFactory.getLogger(SelectRecipeDialog.class);
    
    private final Dialog dialog;
    private final RecipeRepository recipeRepository;
    private final ImageCacheManager imageCache = ImageCacheManager.getInstance();
    private Consumer<Recipe> onRecipeSelected;
    
    public SelectRecipeDialog(Stage owner, RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
        this.dialog = new Dialog(owner, "Select Recipe", "Choose a recipe to add to your meal plan.");
        
        createContent();
        
        dialog.addFooterButton("Cancel", () -> {}, false);
    }
    
    private void createContent() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(10, 0, 0, 0));
        
        // Loading state
        Label loadingLabel = new Label("Loading recipes...");
        loadingLabel.getStyleClass().add("text-gray-500");
        container.getChildren().add(loadingLabel);
        
        // Recipe list container
        FlowPane recipeGrid = new FlowPane();
        recipeGrid.setHgap(12);
        recipeGrid.setVgap(12);
        recipeGrid.setPadding(new Insets(10));
        
        ScrollPane scrollPane = new ScrollPane(recipeGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        // Replace loading label with scroll pane
        if (!container.getChildren().isEmpty()) {
            container.getChildren().set(0, scrollPane);
        } else {
            container.getChildren().add(scrollPane);
        }
        dialog.setContent(container);
        
        // Load recipes in background
        new Thread(() -> {
            try {
                if (recipeRepository == null) {
                    throw new IllegalStateException("Recipe repository is not initialized");
                }
                List<Recipe> recipes = recipeRepository.findAll();
                Platform.runLater(() -> {
                    try {
                        // Clear container and add appropriate content
                        container.getChildren().clear();
                        
                        if (recipes == null || recipes.isEmpty()) {
                            Label emptyLabel = new Label("No recipes available. Create a recipe first!");
                            emptyLabel.getStyleClass().add("text-gray-500");
                            emptyLabel.setAlignment(Pos.CENTER);
                            emptyLabel.setPadding(new Insets(40));
                            container.getChildren().add(emptyLabel);
                        } else {
                            for (Recipe recipe : recipes) {
                                if (recipe != null) {
                                    VBox card = createRecipeCard(recipe);
                                    recipeGrid.getChildren().add(card);
                                }
                            }
                            container.getChildren().add(scrollPane);
                        }
                    } catch (Exception e) {
                        logger.error("Error updating UI with recipes", e);
                        container.getChildren().clear();
                        Label errorLabel = new Label("Failed to display recipes: " + e.getMessage());
                        errorLabel.getStyleClass().add("text-gray-500");
                        errorLabel.setAlignment(Pos.CENTER);
                        errorLabel.setPadding(new Insets(40));
                        container.getChildren().add(errorLabel);
                    }
                });
            } catch (IllegalStateException e) {
                Platform.runLater(() -> {
                    container.getChildren().clear();
                    Label errorLabel = new Label("Failed to load recipes: " + e.getMessage());
                    errorLabel.getStyleClass().add("text-gray-500");
                    errorLabel.setAlignment(Pos.CENTER);
                    errorLabel.setPadding(new Insets(40));
                    container.getChildren().add(errorLabel);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    container.getChildren().clear();
                    String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                    Label errorLabel = new Label("Failed to load recipes: " + errorMessage);
                    errorLabel.getStyleClass().add("text-gray-500");
                    errorLabel.setAlignment(Pos.CENTER);
                    errorLabel.setPadding(new Insets(40));
                    container.getChildren().add(errorLabel);
                });
            }
        }).start();
    }
    
    private VBox createRecipeCard(Recipe recipe) {
        VBox card = new VBox(0);
        card.setPrefWidth(150);
        card.setMinWidth(150);
        card.setPrefHeight(180);
        card.setCursor(javafx.scene.Cursor.HAND);
        
        String defaultStyle = "-fx-background-color: #f9fafb; -fx-background-radius: 12px; -fx-effect: null; -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 12px;";
        String hoverStyle = "-fx-background-color: #ffffff; -fx-background-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 2); -fx-border-color: #4CAF50; -fx-border-width: 2px; -fx-border-radius: 12px;";
        
        card.setStyle(defaultStyle);
        
        // Image container with placeholder
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(100);
        imageContainer.setMinHeight(100);
        imageContainer.setMaxHeight(100);
        
        // Placeholder background
        Region imagePlaceholder = new Region();
        imagePlaceholder.setPrefHeight(100);
        imagePlaceholder.setStyle("-fx-background-color: #E5E7EB; -fx-background-radius: 12px 12px 0 0;");
        imageContainer.getChildren().add(imagePlaceholder);
        
        // Apply clipping mask to keep image inside card
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.setArcWidth(24); // 12px radius on top corners
        clip.setArcHeight(24);
        clip.widthProperty().bind(imageContainer.widthProperty());
        clip.heightProperty().bind(imageContainer.heightProperty());
        imageContainer.setClip(clip);
        
        // Load image if available
        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            new Thread(() -> {
                try {
                    Image image = imageCache.getImage(recipe.getImageUrl());
                    if (image != null) {
                        Platform.runLater(() -> {
                            // Check if card still exists and has children
                            if (card.getChildren().isEmpty() || card.getChildren().get(0) != imageContainer) {
                                return; // Card structure changed, skip image update
                            }
                            
                            ImageView imageView = new ImageView(image);
                            imageView.fitWidthProperty().bind(imageContainer.widthProperty());
                            imageView.fitHeightProperty().bind(imageContainer.heightProperty());
                            imageView.setPreserveRatio(false); // Fill entire container
                            imageView.setSmooth(true);
                            imageView.setCache(true);
                            
                            // Replace placeholder with image view
                            imageContainer.getChildren().set(0, imageView);
                        });
                    }
                } catch (Exception e) {
                    // Keep placeholder if image fails to load
                    logger.debug("Failed to load recipe image: {}", recipe.getImageUrl());
                }
            }).start();
        }
        
        // Content
        VBox content = new VBox(6);
        content.setPadding(new Insets(10));
        content.setPrefHeight(80);
        
        Label nameLabel = new Label(recipe.getName());
        nameLabel.getStyleClass().add("text-gray-900");
        nameLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 13px;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(nameLabel, Priority.ALWAYS);
        
        HBox meta = new HBox(8);
        meta.setAlignment(Pos.CENTER_LEFT);
        
        // Calories
        if (recipe.getNutritionInfo() != null) {
            int calories = (int) recipe.getNutritionInfo().getCalories();
            Label calLabel = new Label(calories + " cal");
            calLabel.getStyleClass().add("text-gray-500");
            calLabel.setStyle("-fx-font-size: 10px;");
            meta.getChildren().add(calLabel);
        }
        
        content.getChildren().addAll(nameLabel, meta);
        
        card.getChildren().addAll(imageContainer, content);
        
        // Hover effects
        card.setOnMouseEntered(e -> {
            card.setStyle(hoverStyle);
            card.setScaleX(1.02);
            card.setScaleY(1.02);
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle(defaultStyle);
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });
        
        // Click handler
        card.setOnMouseClicked(e -> {
            if (onRecipeSelected != null) {
                onRecipeSelected.accept(recipe);
            }
            dialog.close();
        });
        
        return card;
    }
    
    public void setOnRecipeSelected(Consumer<Recipe> handler) {
        this.onRecipeSelected = handler;
    }
    
    public void show() {
        dialog.show();
    }
}

