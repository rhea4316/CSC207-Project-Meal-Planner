package com.mealplanner;

import com.mealplanner.data_access.api.SpoonacularApiClient;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.BrowseRecipeController;
import com.mealplanner.interface_adapter.presenter.BrowseRecipePresenter;
import com.mealplanner.interface_adapter.view_model.RecipeBrowseViewModel;
import com.mealplanner.data_access.database.BrowseRecipeAPIParser;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeDataAccessInterface;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeInteractor;
import com.mealplanner.view.BrowseRecipeView;
import okhttp3.OkHttpClient;

import javax.swing.*;
import java.awt.*;

public class BrowseRecipeGUITest {
    public static void main(String[] args) {
        // Set up the application on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                createAndShowGUI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void createAndShowGUI() {
        
        // Set up the architecture components
        ViewManagerModel viewManagerModel = new ViewManagerModel();
        RecipeBrowseViewModel recipeBrowseViewModel = new RecipeBrowseViewModel();

        // Create presenter
        BrowseRecipePresenter presenter = new BrowseRecipePresenter(recipeBrowseViewModel, viewManagerModel);

        OkHttpClient okHttpClient = new OkHttpClient();
        SpoonacularApiClient client = new SpoonacularApiClient(okHttpClient);
        BrowseRecipeDataAccessInterface browseRecipeDataAccessInterface = new BrowseRecipeAPIParser(client);

        // Create interactor
        BrowseRecipeInteractor interactor = new BrowseRecipeInteractor(browseRecipeDataAccessInterface, presenter);

        // Create controller
        BrowseRecipeController controller = new BrowseRecipeController(interactor);

        // Create the view
        BrowseRecipeView browseRecipeView = new BrowseRecipeView(recipeBrowseViewModel, controller);

        JFrame frame = new JFrame("Browse Recipe Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600));

        // Add view to frame
        frame.add(browseRecipeView);

        // Display the frame
        frame.pack();
        frame.setLocationRelativeTo(null); // Center the window
        frame.setVisible(true);
    }
}