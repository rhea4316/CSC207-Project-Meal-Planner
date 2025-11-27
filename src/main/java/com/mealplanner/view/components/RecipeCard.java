package com.mealplanner.view.components;

import com.mealplanner.entity.Recipe;
import com.mealplanner.entity.NutritionInfo;

import javax.swing.*;
import java.awt.*;

// Reusable Swing component for displaying recipe summary in a card format.
// Responsible: Everyone (GUI implementation)

public class RecipeCard extends JPanel {
    private final Recipe recipe;
    private JLabel nameLabel;
    private JLabel imageLabel;
    private JLabel nutritionLabel;

    public RecipeCard(Recipe recipe) {
        if (recipe == null) {
            throw new IllegalArgumentException("Recipe cannot be null");
        }
        this.recipe = recipe;
        initializeComponents();
        layoutComponents();
    }

    private void initializeComponents() {
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(250, 300));

        // Recipe name
        nameLabel = new JLabel(recipe.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Image placeholder (can be replaced with actual image loading later)
        imageLabel = new JLabel("No Image", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(200, 150));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        imageLabel.setBackground(Color.LIGHT_GRAY);
        imageLabel.setOpaque(true);

        // Basic nutrition info
        NutritionInfo nutrition = recipe.getNutritionInfo();
        if (nutrition != null) {
            String nutritionText = String.format(
                    "<html>Calories: %d<br>Protein: %.1fg<br>Carbs: %.1fg<br>Fat: %.1fg</html>",
                    nutrition.getCalories(),
                    nutrition.getProtein(),
                    nutrition.getCarbs(),
                    nutrition.getFat()
            );
            nutritionLabel = new JLabel(nutritionText);
        } else {
            nutritionLabel = new JLabel("Nutrition info not available");
        }
        nutritionLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        nutritionLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(5, 5));

        add(nameLabel, BorderLayout.NORTH);
        add(imageLabel, BorderLayout.CENTER);
        add(nutritionLabel, BorderLayout.SOUTH);
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setImage(ImageIcon icon) {
        if (icon != null) {
            imageLabel.setIcon(icon);
            imageLabel.setText("");
        }
    }
}
