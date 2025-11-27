package com.mealplanner.app;

// Application entry point - launches the Meal Planner application.
// Responsible: Everyone

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // Set modern FlatLaf look and feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                createAndShowGui();
            } catch (Exception e) {
                System.err.println("Error starting application: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static void createAndShowGui() {
        // Create AppBuilder and build the application
        AppBuilder appBuilder = new AppBuilder();
        com.mealplanner.view.ViewManager viewManager = appBuilder.build();

        // Create and configure main window
        JFrame frame = new JFrame("Meal Planner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Create and set menu bar
        JMenuBar menuBar = createMenuBar(viewManager);
        frame.setJMenuBar(menuBar);
        
        frame.getContentPane().add(viewManager, BorderLayout.CENTER);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    private static JMenuBar createMenuBar(com.mealplanner.view.ViewManager viewManager) {
        JMenuBar menuBar = new JMenuBar();
        
        // Create "View" menu
        JMenu viewMenu = new JMenu("View");
        
        // Store Recipe menu item
        JMenuItem storeRecipeItem = new JMenuItem("Store Recipe");
        storeRecipeItem.addActionListener(e -> viewManager.switchToView(com.mealplanner.view.ViewManager.STORE_RECIPE_VIEW));
        
        // Browse Recipes menu item
        JMenuItem browseRecipeItem = new JMenuItem("Browse Recipes");
        browseRecipeItem.addActionListener(e -> viewManager.switchToView(com.mealplanner.view.ViewManager.BROWSE_RECIPE_VIEW));
        
        // Search by Ingredients menu item
        JMenuItem searchByIngredientsItem = new JMenuItem("Search by Ingredients");
        searchByIngredientsItem.addActionListener(e -> viewManager.switchToView(com.mealplanner.view.ViewManager.SEARCH_BY_INGREDIENTS_VIEW));
        
        // Recipe Detail menu item
        JMenuItem recipeDetailItem = new JMenuItem("Recipe Detail");
        recipeDetailItem.addActionListener(e -> viewManager.switchToView(com.mealplanner.view.ViewManager.RECIPE_DETAIL_VIEW));

        //Schedule menu item
        JMenuItem scheduleItem = new JMenuItem("Schedule");
        scheduleItem.addActionListener(e -> viewManager.switchToView(com.mealplanner.view.ViewManager.SCHEDULE_VIEW));

        // Separator
        viewMenu.addSeparator();
        
        // Home menu item
        JMenuItem homeItem = new JMenuItem("Home");
        homeItem.addActionListener(e -> viewManager.switchToView(com.mealplanner.view.ViewManager.STORE_RECIPE_VIEW));
        
        // Add all items to menu
        viewMenu.add(storeRecipeItem);
        viewMenu.add(browseRecipeItem);
        viewMenu.add(searchByIngredientsItem);
        viewMenu.add(recipeDetailItem);
        viewMenu.add(scheduleItem);
        viewMenu.addSeparator();
        viewMenu.add(homeItem);
        
        menuBar.add(viewMenu);
        
        return menuBar;
    }
}
