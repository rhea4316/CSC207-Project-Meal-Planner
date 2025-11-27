package com.mealplanner.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.StoreRecipeController;
import com.mealplanner.interface_adapter.presenter.StoreRecipePresenter;
import com.mealplanner.interface_adapter.view_model.RecipeStoreViewModel;
import com.mealplanner.repository.impl.FileRecipeRepository;
import com.mealplanner.use_case.store_recipe.StoreRecipeInteractor;
import com.mealplanner.view.StoreRecipeView;

/**
 * Small demo application that wires the store-recipe flow and shows the JavaFX UI.
 */
public class StoreRecipeDemo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // ViewManagerModel (for navigation consistency)
            ViewManagerModel viewManagerModel = new ViewManagerModel();

            // ViewModel
            RecipeStoreViewModel viewModel = new RecipeStoreViewModel();

            // Presenter
            StoreRecipePresenter presenter = new StoreRecipePresenter(viewModel);

            // Repository - using FileRecipeRepository to persist to disk
            FileRecipeRepository repo = new FileRecipeRepository();

            // Interactor (uses RecipeRepository implementation)
            StoreRecipeInteractor interactor = new StoreRecipeInteractor(presenter, repo);

            // Controller
            StoreRecipeController controller = new StoreRecipeController(interactor);

            // View (with ViewManagerModel for navigation)
            StoreRecipeView view = new StoreRecipeView(controller, viewModel, viewManagerModel);

            // Scene Setup
            Scene scene = new Scene(view, 800, 600);
            
            primaryStage.setTitle("Store Recipe Demo");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Error starting demo: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
