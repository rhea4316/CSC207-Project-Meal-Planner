package com.mealplanner.view;

import com.mealplanner.data_access.api.SpoonacularApiClient;
import com.mealplanner.data_access.database.BrowseRecipeAPIParser;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.BrowseRecipeController;
import com.mealplanner.interface_adapter.controller.StoreRecipeController;
import com.mealplanner.interface_adapter.presenter.BrowseRecipePresenter;
import com.mealplanner.interface_adapter.presenter.StoreRecipePresenter;
import com.mealplanner.interface_adapter.view_model.RecipeBrowseViewModel;
import com.mealplanner.interface_adapter.view_model.RecipeDetailViewModel;
import com.mealplanner.interface_adapter.view_model.RecipeStoreViewModel;
import com.mealplanner.repository.RecipeRepository;
import com.mealplanner.repository.impl.FileRecipeRepository;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeDataAccessInterface;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeInteractor;
import com.mealplanner.use_case.store_recipe.StoreRecipeInteractor;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;

public class BrowseRecipeGUITest extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            createAndShowGUI(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createAndShowGUI(Stage stage) {
        
        // Set up the architecture components
        ViewManagerModel viewManagerModel = new ViewManagerModel();
        RecipeBrowseViewModel recipeBrowseViewModel = new RecipeBrowseViewModel();
        RecipeDetailViewModel recipeDetailViewModel = new RecipeDetailViewModel();

        // Create presenter
        BrowseRecipePresenter presenter = new BrowseRecipePresenter(recipeBrowseViewModel, viewManagerModel);

        OkHttpClient okHttpClient = new OkHttpClient();
        SpoonacularApiClient client = new SpoonacularApiClient(okHttpClient);
        BrowseRecipeDataAccessInterface browseRecipeDataAccessInterface = new BrowseRecipeAPIParser(client);

        // Create interactor
        BrowseRecipeInteractor interactor = new BrowseRecipeInteractor(browseRecipeDataAccessInterface, presenter);

        // Create controller
        BrowseRecipeController controller = new BrowseRecipeController(interactor);

        // Create RecipeRepository for local recipe access
        RecipeRepository recipeRepository = new FileRecipeRepository();

        // Create StoreRecipeController for bookmark functionality
        RecipeStoreViewModel storeViewModel = new RecipeStoreViewModel();
        StoreRecipePresenter storePresenter = new StoreRecipePresenter(storeViewModel);
        StoreRecipeInteractor storeInteractor = new StoreRecipeInteractor(storePresenter, recipeRepository);
        StoreRecipeController storeRecipeController = new StoreRecipeController(storeInteractor);

        // Create the view
        // Note: Using constructor with StoreRecipeController for bookmark functionality
        BrowseRecipeView browseRecipeView = new BrowseRecipeView(
            recipeBrowseViewModel, 
            controller, 
            viewManagerModel, 
            recipeDetailViewModel, 
            recipeRepository, 
            null, // GetRecommendationsController - optional, can be null for testing
            storeRecipeController  // StoreRecipeController - required for bookmark functionality
        );

        Scene scene = new Scene(browseRecipeView, 800, 600);
        stage.setTitle("Browse Recipe Test");
        stage.setScene(scene);
        stage.show();
    }
}