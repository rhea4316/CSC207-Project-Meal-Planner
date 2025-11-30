package com.mealplanner;

import com.mealplanner.data_access.api.SpoonacularApiClient;
import com.mealplanner.data_access.database.BrowseRecipeAPIParser;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.BrowseRecipeController;
import com.mealplanner.interface_adapter.presenter.BrowseRecipePresenter;
import com.mealplanner.interface_adapter.view_model.RecipeBrowseViewModel;
import com.mealplanner.interface_adapter.view_model.RecipeDetailViewModel;
import com.mealplanner.repository.RecipeRepository;
import com.mealplanner.repository.impl.FileRecipeRepository;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeDataAccessInterface;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeInteractor;
import com.mealplanner.view.BrowseRecipeView;
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

        // Create the view
        BrowseRecipeView browseRecipeView = new BrowseRecipeView(recipeBrowseViewModel, controller, viewManagerModel, recipeDetailViewModel, recipeRepository);

        Scene scene = new Scene(browseRecipeView, 800, 600);
        stage.setTitle("Browse Recipe Test");
        stage.setScene(scene);
        stage.show();
    }
}