package com.mealplanner.view;

import com.mealplanner.entity.NutritionInfo;
import com.mealplanner.entity.Recipe;
import com.mealplanner.interface_adapter.controller.AdjustServingSizeController;
import com.mealplanner.interface_adapter.view_model.RecipeDetailViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

// Swing view for detailed recipe display with serving size adjustment.
// Responsible: Eden (serving size functionality), Everyone (GUI implementation)

public class RecipeDetailView extends JPanel implements PropertyChangeListener, ActionListener {
    private final RecipeDetailViewModel viewModel;
    private final AdjustServingSizeController controller;

    private JLabel recipeNameLabel;
    private JTextField servingSizeField;
    private JButton adjustButton;
    private JTextArea ingredientsArea;
    private JTextArea nutritionArea;
    private JLabel errorLabel;

    public RecipeDetailView(RecipeDetailViewModel viewModel, AdjustServingSizeController controller) {
        this.viewModel = viewModel;
        this.controller = controller;

        viewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout());
        createComponents();
    }

    private void createComponents() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        recipeNameLabel = new JLabel("Recipe Name");
        topPanel.add(recipeNameLabel);
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Serving size input
        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(new JLabel("Serving Size:"), gbc);
        gbc.gridx = 1;
        servingSizeField = new JTextField(10);
        centerPanel.add(servingSizeField, gbc);
        gbc.gridx = 2;
        adjustButton = new JButton("Adjust");
        adjustButton.addActionListener(this);
        adjustButton.setActionCommand("adjust");
        centerPanel.add(adjustButton, gbc);

        // Ingredients display
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        centerPanel.add(new JLabel("Ingredients:"), gbc);
        gbc.gridy = 2;
        ingredientsArea = new JTextArea(10, 30);
        ingredientsArea.setEditable(false);
        centerPanel.add(new JScrollPane(ingredientsArea), gbc);

        // Nutrition display
        gbc.gridy = 3;
        centerPanel.add(new JLabel("Nutrition:"), gbc);
        gbc.gridy = 4;
        gbc.weighty = 0.3;
        nutritionArea = new JTextArea(5, 30);
        nutritionArea.setEditable(false);
        centerPanel.add(new JScrollPane(nutritionArea), gbc);

        add(centerPanel, BorderLayout.CENTER);

        // Error label
        errorLabel = new JLabel();
        errorLabel.setForeground(Color.RED);
        add(errorLabel, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("adjust".equals(e.getActionCommand())) {
            performAdjust();
        }
    }

    private void performAdjust() {
        String servingSizeText = servingSizeField.getText().trim();
        if (servingSizeText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a serving size", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int newServingSize = Integer.parseInt(servingSizeText);
            Recipe recipe = viewModel.getRecipe();
            if (recipe == null) {
                JOptionPane.showMessageDialog(this, "No recipe selected", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            controller.execute(recipe.getRecipeId(), newServingSize);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid serving size. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayRecipe(Recipe recipe) {
        if (recipe == null) {
            recipeNameLabel.setText("No recipe selected");
            servingSizeField.setText("");
            ingredientsArea.setText("");
            nutritionArea.setText("");
            return;
        }

        recipeNameLabel.setText(recipe.getName());
        servingSizeField.setText(String.valueOf(recipe.getServingSize()));

        // Display ingredients
        List<String> ingredients = recipe.getIngredients();
        StringBuilder ingredientsText = new StringBuilder();
        for (String ingredient : ingredients) {
            ingredientsText.append(ingredient).append("\n");
        }
        ingredientsArea.setText(ingredientsText.toString());

        // Display nutrition
        NutritionInfo nutrition = recipe.getNutritionInfo();
        if (nutrition != null) {
            String nutritionText = String.format(
                "Calories: %d\nProtein: %.1fg\nCarbs: %.1fg\nFat: %.1fg",
                nutrition.getCalories(),
                nutrition.getProtein(),
                nutrition.getCarbs(),
                nutrition.getFat()
            );
            nutritionArea.setText(nutritionText);
        } else {
            nutritionArea.setText("Nutrition information not available");
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        switch (propertyName) {
            case RecipeDetailViewModel.PROP_RECIPE:
                displayRecipe(viewModel.getRecipe());
                errorLabel.setText("");
                break;
            case RecipeDetailViewModel.PROP_SERVING_SIZE:
                servingSizeField.setText(String.valueOf(viewModel.getServingSize()));
                break;
            case RecipeDetailViewModel.PROP_INGREDIENTS:
                List<String> ingredients = viewModel.getIngredients();
                StringBuilder ingredientsText = new StringBuilder();
                for (String ingredient : ingredients) {
                    ingredientsText.append(ingredient).append("\n");
                }
                ingredientsArea.setText(ingredientsText.toString());
                break;
            case RecipeDetailViewModel.PROP_NUTRITION:
                NutritionInfo nutrition = viewModel.getNutrition();
                if (nutrition != null) {
                    String nutritionText = String.format(
                        "Calories: %d\nProtein: %.1fg\nCarbs: %.1fg\nFat: %.1fg",
                        nutrition.getCalories(),
                        nutrition.getProtein(),
                        nutrition.getCarbs(),
                        nutrition.getFat()
                    );
                    nutritionArea.setText(nutritionText);
                }
                break;
            case RecipeDetailViewModel.PROP_ERROR_MESSAGE:
                String errorMessage = viewModel.getErrorMessage();
                errorLabel.setText(errorMessage != null && !errorMessage.isEmpty() ? errorMessage : "");
                break;
        }
    }
}
