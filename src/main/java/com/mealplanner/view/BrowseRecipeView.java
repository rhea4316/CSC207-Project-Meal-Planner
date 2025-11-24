package com.mealplanner.view;

// Swing view for browsing available recipes - displays recipe list and selection interface.
// Responsible: Regina (functionality), Everyone (GUI implementation)

import com.mealplanner.entity.Recipe;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.BrowseRecipeController;
import com.mealplanner.interface_adapter.view_model.RecipeBrowseViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

public class BrowseRecipeView extends JPanel implements PropertyChangeListener, ActionListener {
    private final RecipeBrowseViewModel recipeBrowseViewModel;
    private final BrowseRecipeController browseRecipeController;
    private final ViewManagerModel viewManagerModel;

    private JPanel searchPanel;
    private JTextField queryTextField;
    private JTextField ingredientsTextField;
    private JSpinner numberOfResultsSpinner;
    private JButton searchButton;

    private JPanel resultsPanel;
    private JTextArea resultsTextArea;
    private JScrollPane resultsScrollPane;

    private JLabel errorLabel;

    public BrowseRecipeView(RecipeBrowseViewModel recipeBrowseViewModel, BrowseRecipeController browseRecipeController) {
        this(recipeBrowseViewModel, browseRecipeController, null);
    }
    
    public BrowseRecipeView(RecipeBrowseViewModel recipeBrowseViewModel, BrowseRecipeController browseRecipeController, ViewManagerModel viewManagerModel) {
        if (recipeBrowseViewModel == null) {
            throw new IllegalArgumentException("ViewModel cannot be null");
        }
        if (browseRecipeController == null) {
            throw new IllegalArgumentException("Controller cannot be null");
        }
        
        this.recipeBrowseViewModel = recipeBrowseViewModel;
        this.browseRecipeController = browseRecipeController;
        this.viewManagerModel = viewManagerModel;

        recipeBrowseViewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout());
        
        // Create navigation panel
        JPanel navPanel = createNavigationPanel();
        if (navPanel != null) {
            add(navPanel, BorderLayout.NORTH);
        }
        
        createSearchPanel();
        createResultsPanel();
        createErrorLabel();
        
        // Arrange layout properly
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(searchPanel, BorderLayout.NORTH);
        contentPanel.add(resultsPanel, BorderLayout.CENTER);
        contentPanel.add(errorLabel, BorderLayout.SOUTH);
        
        add(contentPanel, BorderLayout.CENTER);

    }

    private void createSearchPanel() {
        searchPanel = new JPanel(new GridLayout(4, 2, 5, 5));

        searchPanel.add(new JLabel("Search Query:"));
        queryTextField = new JTextField();
        searchPanel.add(queryTextField);

        searchPanel.add(new JLabel("Ingredients (comma-separated, optional):"));
        ingredientsTextField = new JTextField();
        searchPanel.add(ingredientsTextField);

        searchPanel.add(new JLabel("Number of Recipes:"));
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(1, 0, 100, 1);
        numberOfResultsSpinner = new JSpinner(spinnerNumberModel);
        searchPanel.add(numberOfResultsSpinner);

        searchButton = new JButton("Search:");
        searchButton.addActionListener(this);
        searchButton.setActionCommand("search");
        searchPanel.add(searchButton);

    }

    private void createResultsPanel() {
        resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.add(new JLabel("Search Results:"), BorderLayout.NORTH);

        resultsTextArea = new JTextArea();
        resultsTextArea.setEditable(false);

        resultsScrollPane = new JScrollPane(resultsTextArea);
        resultsPanel.add(resultsScrollPane, BorderLayout.CENTER);
    }

    private void createErrorLabel() {
        errorLabel = new JLabel();
        errorLabel.setForeground(Color.RED);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("search".equals(e.getActionCommand())) {
            performSearch();
        }
    }

    private void performSearch() {
        String query = queryTextField.getText().trim();
        String ingredients = ingredientsTextField.getText().trim();
        int numberOfRecipes = (Integer) numberOfResultsSpinner.getValue();

        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a search query", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (ingredients.isEmpty()) {
                browseRecipeController.execute(query, numberOfRecipes);
            } else {
                browseRecipeController.execute(query, numberOfRecipes, ingredients);
            }
        } catch (IOException ex) {
            recipeBrowseViewModel.setErrorMessage("Network error: " + ex.getMessage());
        }
    }

    private void displayRecipes(java.util.List<Recipe> recipes) {
        if (recipes == null) {
            resultsTextArea.setText("No recipes available.");
            return;
        }
        
        StringBuilder results = new StringBuilder();

        if (recipes.isEmpty()) {
            results.append("No recipes found.");
        } else {
            for (Recipe recipe : recipes) {
                if (recipe != null) {
                    results.append("Recipe: ").append(recipe.getName()).append("\n");
                    results.append("Ingredients: ").append(recipe.getIngredients()).append("\n");
                    results.append("Serving Size: ").append(recipe.getServingSize()).append("\n");
                    String steps = recipe.getSteps();
                    if (steps != null && !steps.isEmpty()) {
                        results.append("Instructions: ").append(steps.length() > 100 ? 
                            steps.substring(0, 100) + "..." : steps).append("\n");
                    }
                    results.append("\n").append("-".repeat(50)).append("\n");
                }
            }
        }

        resultsTextArea.setText(results.toString());
        resultsTextArea.setCaretPosition(0);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "recipes":
                displayRecipes(recipeBrowseViewModel.getRecipes());
                errorLabel.setText("");
                break;
            case "errorMessage":
                resultsTextArea.setText("");
                String errorMsg = recipeBrowseViewModel.getErrorMessage();
                errorLabel.setText(errorMsg != null ? errorMsg : "");
                break;
        }

    }
    
    private JPanel createNavigationPanel() {
        if (viewManagerModel == null) {
            return null;
        }
        
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navPanel.setBorder(BorderFactory.createTitledBorder("Navigation"));
        
        JButton createButton = new JButton("Create Recipe");
        createButton.addActionListener(e -> viewManagerModel.setActiveView("StoreRecipeView"));
        
        JButton searchButton = new JButton("Search by Ingredients");
        searchButton.addActionListener(e -> viewManagerModel.setActiveView("SearchByIngredientsView"));
        
        JButton homeButton = new JButton("Home");
        homeButton.addActionListener(e -> viewManagerModel.setActiveView("StoreRecipeView"));
        
        navPanel.add(createButton);
        navPanel.add(searchButton);
        navPanel.add(homeButton);
        
        return navPanel;
    }
}


