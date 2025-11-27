package com.mealplanner.view.components;

import javax.swing.*;
import java.awt.*;
import java.util.List;

// Reusable Swing component for displaying list of ingredients with quantities.
// Responsible: Everyone (GUI implementation)

public class IngredientListPanel extends JPanel {
    private final DefaultListModel<String> listModel;
    private final JList<String> ingredientList;
    private final JScrollPane scrollPane;

    public IngredientListPanel() {
        this.listModel = new DefaultListModel<>();
        this.ingredientList = new JList<>(listModel);
        this.scrollPane = new JScrollPane(ingredientList);
        initializeComponents();
        layoutComponents();
    }

    public IngredientListPanel(List<String> ingredients) {
        this();
        if (ingredients != null) {
            setIngredients(ingredients);
        }
    }

    private void initializeComponents() {
        setBorder(BorderFactory.createTitledBorder("Ingredients"));
        ingredientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ingredientList.setFont(new Font("Arial", Font.PLAIN, 12));
        ingredientList.setVisibleRowCount(8);
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setIngredients(List<String> ingredients) {
        listModel.clear();
        if (ingredients != null) {
            for (String ingredient : ingredients) {
                if (ingredient != null && !ingredient.trim().isEmpty()) {
                    listModel.addElement(ingredient);
                }
            }
        }
    }

    public void addIngredient(String ingredient) {
        if (ingredient != null && !ingredient.trim().isEmpty()) {
            listModel.addElement(ingredient);
        }
    }

    public void removeIngredient(int index) {
        if (index >= 0 && index < listModel.size()) {
            listModel.remove(index);
        }
    }

    public void clearIngredients() {
        listModel.clear();
    }

    public List<String> getIngredients() {
        List<String> ingredients = new java.util.ArrayList<>();
        for (int i = 0; i < listModel.size(); i++) {
            ingredients.add(listModel.getElementAt(i));
        }
        return ingredients;
    }

    public JList<String> getIngredientList() {
        return ingredientList;
    }
}
