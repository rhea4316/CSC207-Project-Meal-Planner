package com.mealplanner.view.components;

import com.mealplanner.entity.NutritionInfo;

import javax.swing.*;
import java.awt.*;

// Reusable Swing component for displaying nutritional information in a formatted layout.
// Responsible: Everyone (GUI implementation)

public class NutritionPanel extends JPanel {
    private final NutritionInfo nutritionInfo;
    private JLabel caloriesLabel;
    private JLabel proteinLabel;
    private JLabel carbsLabel;
    private JLabel fatLabel;

    public NutritionPanel(NutritionInfo nutritionInfo) {
        this.nutritionInfo = nutritionInfo != null ? nutritionInfo : NutritionInfo.empty();
        initializeComponents();
        layoutComponents();
    }

    private void initializeComponents() {
        setBorder(BorderFactory.createTitledBorder("Nutrition Information"));
        setLayout(new GridLayout(4, 2, 10, 5));

        // Calories
        JLabel caloriesTitle = new JLabel("Calories:");
        caloriesTitle.setFont(new Font("Arial", Font.BOLD, 12));
        caloriesLabel = new JLabel(String.valueOf(this.nutritionInfo.getCalories()));
        caloriesLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        // Protein
        JLabel proteinTitle = new JLabel("Protein:");
        proteinTitle.setFont(new Font("Arial", Font.BOLD, 12));
        proteinLabel = new JLabel(String.format("%.1f g", this.nutritionInfo.getProtein()));
        proteinLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        // Carbs
        JLabel carbsTitle = new JLabel("Carbohydrates:");
        carbsTitle.setFont(new Font("Arial", Font.BOLD, 12));
        carbsLabel = new JLabel(String.format("%.1f g", this.nutritionInfo.getCarbs()));
        carbsLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        // Fat
        JLabel fatTitle = new JLabel("Fat:");
        fatTitle.setFont(new Font("Arial", Font.BOLD, 12));
        fatLabel = new JLabel(String.format("%.1f g", this.nutritionInfo.getFat()));
        fatLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        add(caloriesTitle);
        add(caloriesLabel);
        add(proteinTitle);
        add(proteinLabel);
        add(carbsTitle);
        add(carbsLabel);
        add(fatTitle);
        add(fatLabel);
    }

    private void layoutComponents() {
        // Layout is handled in initializeComponents using GridLayout
    }

    public void updateNutrition(NutritionInfo newNutritionInfo) {
        if (newNutritionInfo != null) {
            caloriesLabel.setText(String.valueOf(newNutritionInfo.getCalories()));
            proteinLabel.setText(String.format("%.1f g", newNutritionInfo.getProtein()));
            carbsLabel.setText(String.format("%.1f g", newNutritionInfo.getCarbs()));
            fatLabel.setText(String.format("%.1f g", newNutritionInfo.getFat()));
        }
    }

    public NutritionInfo getNutritionInfo() {
        return nutritionInfo;
    }
}
