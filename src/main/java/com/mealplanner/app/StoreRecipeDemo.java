package com.mealplanner.app;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.mealplanner.interface_adapter.controller.StoreRecipeController;
import com.mealplanner.interface_adapter.presenter.StoreRecipePresenter;
import com.mealplanner.interface_adapter.view_model.RecipeStoreViewModel;
import com.mealplanner.repository.impl.FileRecipeRepository;
import com.mealplanner.use_case.store_recipe.StoreRecipeInteractor;
import com.mealplanner.view.StoreRecipeView;

/**
 * Small demo application that wires the store-recipe flow and shows the Swing UI.
 */
public class StoreRecipeDemo {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                createAndShowGui();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void createAndShowGui() {
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

        // View
        StoreRecipeView view = new StoreRecipeView(controller, viewModel);

        JFrame frame = new JFrame("Store Recipe Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(view, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

}
