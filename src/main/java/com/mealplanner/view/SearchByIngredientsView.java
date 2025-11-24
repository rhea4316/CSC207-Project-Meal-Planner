package com.mealplanner.view;

// Swing view for ingredient search feature - displays ingredient input form and search results.
// Responsible: Jerry (functionality), Everyone (GUI implementation)

import com.mealplanner.entity.Recipe;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.SearchByIngredientsController;
import com.mealplanner.interface_adapter.view_model.RecipeSearchViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class SearchByIngredientsView extends JPanel implements PropertyChangeListener, ActionListener {
    private final RecipeSearchViewModel viewModel;
    private final SearchByIngredientsController controller;
    private final ViewManagerModel viewManagerModel;

    private JPanel searchPanel;
    private JTextArea ingredientsTextArea;
    private JButton searchButton;

    private JPanel resultsPanel;
    private JTextArea resultsTextArea;
    private JScrollPane resultsScrollPane;

    private JLabel errorLabel;
    private JLabel loadingLabel;

    public SearchByIngredientsView(SearchByIngredientsController controller, RecipeSearchViewModel viewModel) {
        this(controller, viewModel, null);
    }
    
    public SearchByIngredientsView(SearchByIngredientsController controller, RecipeSearchViewModel viewModel, ViewManagerModel viewManagerModel) {
        if (viewModel == null) {
            throw new IllegalArgumentException("ViewModel cannot be null");
        }
        if (controller == null) {
            throw new IllegalArgumentException("Controller cannot be null");
        }

        this.viewModel = viewModel;
        this.controller = controller;
        this.viewManagerModel = viewManagerModel;

        viewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout());
        
        createSearchPanel();
        createResultsPanel();
        createErrorLabel();
        createLoadingLabel();

        // Create navigation panel
        JPanel navPanel = createNavigationPanel();
        
        // Arrange layout: navPanel at top, then searchPanel, then results
        JPanel topPanel = new JPanel(new BorderLayout());
        if (navPanel != null) {
            topPanel.add(navPanel, BorderLayout.NORTH);
        }
        topPanel.add(searchPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        add(resultsPanel, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(loadingLabel, BorderLayout.WEST);
        bottomPanel.add(errorLabel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void createSearchPanel() {
        searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search by Ingredients"));

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JLabel("Enter ingredients (comma or newline separated):"), BorderLayout.NORTH);
        
        ingredientsTextArea = new JTextArea(3, 40);
        ingredientsTextArea.setLineWrap(true);
        ingredientsTextArea.setWrapStyleWord(true);
        inputPanel.add(new JScrollPane(ingredientsTextArea), BorderLayout.CENTER);

        searchButton = new JButton("Search Recipes");
        searchButton.addActionListener(this);
        searchButton.setActionCommand("search");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(searchButton);

        searchPanel.add(inputPanel, BorderLayout.CENTER);
        searchPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void createResultsPanel() {
        resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Search Results"));

        resultsTextArea = new JTextArea();
        resultsTextArea.setEditable(false);
        resultsTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        resultsScrollPane = new JScrollPane(resultsTextArea);
        resultsScrollPane.setPreferredSize(new Dimension(600, 400));
        resultsPanel.add(resultsScrollPane, BorderLayout.CENTER);
    }

    private void createErrorLabel() {
        errorLabel = new JLabel();
        errorLabel.setForeground(Color.RED);
    }

    private void createLoadingLabel() {
        loadingLabel = new JLabel();
        loadingLabel.setForeground(Color.BLUE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("search".equals(e.getActionCommand())) {
            performSearch();
        }
    }

    private void performSearch() {
        String ingredientsRaw = ingredientsTextArea.getText().trim();

        if (ingredientsRaw.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter at least one ingredient", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        viewModel.setLoading(true);
        viewModel.setErrorMessage("");
        controller.execute(ingredientsRaw);
    }

    private void displayRecipes(List<Recipe> recipes) {
        if (recipes == null || recipes.isEmpty()) {
            resultsTextArea.setText("No recipes found.");
            return;
        }

        StringBuilder results = new StringBuilder();
        results.append("Found ").append(recipes.size()).append(" recipe(s):\n\n");

        for (Recipe recipe : recipes) {
            if (recipe != null) {
                results.append("Recipe: ").append(recipe.getName()).append("\n");
                results.append("Serving Size: ").append(recipe.getServingSize()).append("\n");
                
                if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
                    results.append("Ingredients: ");
                    results.append(String.join(", ", recipe.getIngredients()));
                    results.append("\n");
                }
                
                if (recipe.getNutritionInfo() != null) {
                    results.append("Calories: ").append(recipe.getNutritionInfo().getCalories()).append("\n");
                }
                
                String steps = recipe.getSteps();
                if (steps != null && !steps.isEmpty()) {
                    String stepsPreview = steps.length() > 150 ? steps.substring(0, 150) + "..." : steps;
                    results.append("Instructions: ").append(stepsPreview).append("\n");
                }
                
                results.append("\n").append("-".repeat(50)).append("\n");
            }
        }

        resultsTextArea.setText(results.toString());
        resultsTextArea.setCaretPosition(0);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        
        if (RecipeSearchViewModel.PROP_RECIPES.equals(propertyName)) {
            displayRecipes(viewModel.getRecipes());
            errorLabel.setText("");
        } else if (RecipeSearchViewModel.PROP_ERROR_MESSAGE.equals(propertyName)) {
            String errorMsg = viewModel.getErrorMessage();
            if (errorMsg != null && !errorMsg.isEmpty()) {
                resultsTextArea.setText("");
                errorLabel.setText(errorMsg);
            } else {
                errorLabel.setText("");
            }
        } else if (RecipeSearchViewModel.PROP_LOADING.equals(propertyName)) {
            if (viewModel.isLoading()) {
                loadingLabel.setText("Searching...");
            } else {
                loadingLabel.setText("");
            }
        }
    }
    
    private JPanel createNavigationPanel() {
        if (viewManagerModel == null) {
            return null;
        }
        
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navPanel.setBorder(BorderFactory.createTitledBorder("Navigation"));
        
        JButton browseButton = new JButton("Browse Recipes");
        browseButton.addActionListener(e -> viewManagerModel.setActiveView("BrowseRecipeView"));
        
        JButton createButton = new JButton("Create Recipe");
        createButton.addActionListener(e -> viewManagerModel.setActiveView("StoreRecipeView"));
        
        JButton homeButton = new JButton("Home");
        homeButton.addActionListener(e -> viewManagerModel.setActiveView("StoreRecipeView"));
        
        navPanel.add(browseButton);
        navPanel.add(createButton);
        navPanel.add(homeButton);
        
        return navPanel;
    }
}
