package com.mealplanner.view;

// Swing view for creating and storing new recipes - displays recipe creation form.
// Responsible: Aaryan (functionality), Everyone (GUI implementation)
// TODO: Create JPanel with form fields for name, ingredients, steps - call StoreRecipeController on save button click
import com.mealplanner.entity.Unit;
import com.mealplanner.interface_adapter.controller.StoreRecipeController;
import com.mealplanner.interface_adapter.view_model.RecipeStoreViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Swing panel for creating and storing recipes. Minimal, self-contained UI components
 * and wiring to a provided `StoreRecipeController`.
 */
public class StoreRecipeView extends JPanel {

	private final JTextField nameField = new JTextField(30);

	// Ingredient inputs
	private final JTextField ingredientQtyField = new JTextField(6);
	private final JComboBox<Unit> unitCombo = new JComboBox<>(Unit.values());
	private final JTextField ingredientNameField = new JTextField(15);
	private final DefaultListModel<String> ingredientListModel = new DefaultListModel<>();
	private final JList<String> ingredientJList = new JList<>(ingredientListModel);

	// Steps / instructions
	private final JTextArea stepsArea = new JTextArea(8, 40);

	private final JTextField servingSizeField = new JTextField("1", 4);

	private final JButton addIngredientButton = new JButton("Add Ingredient");
	private final JButton saveButton = new JButton("Save Recipe");

	private final JLabel statusLabel = new JLabel(" ");

	public StoreRecipeView(StoreRecipeController controller, RecipeStoreViewModel viewModel) {
		super(new BorderLayout());

		JPanel form = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.WEST;

		int row = 0;

		// Name
		gbc.gridx = 0;
		gbc.gridy = row;
		form.add(new JLabel("Recipe name:"), gbc);
		gbc.gridx = 1;
		form.add(nameField, gbc);
		row++;

		// Ingredients input row
		gbc.gridx = 0;
		gbc.gridy = row;
		form.add(new JLabel("Ingredient (qty/unit/name):"), gbc);

		JPanel ingInput = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		ingInput.add(ingredientQtyField);
		ingInput.add(unitCombo);
		ingInput.add(ingredientNameField);
		ingInput.add(addIngredientButton);

		gbc.gridx = 1;
		form.add(ingInput, gbc);
		row++;

		// Ingredient list
		gbc.gridx = 0;
		gbc.gridy = row;
		form.add(new JLabel("Ingredients:"), gbc);
		gbc.gridx = 1;
		ingredientJList.setVisibleRowCount(5);
		ingredientJList.setFixedCellWidth(300);
		form.add(new JScrollPane(ingredientJList), gbc);
		row++;

		// Steps
		gbc.gridx = 0;
		gbc.gridy = row;
		form.add(new JLabel("Cooking instructions:"), gbc);
		gbc.gridx = 1;
		stepsArea.setLineWrap(true);
		stepsArea.setWrapStyleWord(true);
		form.add(new JScrollPane(stepsArea), gbc);
		row++;

		// Serving size and save
		gbc.gridx = 0;
		gbc.gridy = row;
		form.add(new JLabel("Serving size:"), gbc);
		gbc.gridx = 1;
		JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		bottomRow.add(servingSizeField);
		bottomRow.add(saveButton);
		bottomRow.add(statusLabel);
		form.add(bottomRow, gbc);

		this.add(form, BorderLayout.CENTER);

		// Actions
		addIngredientButton.addActionListener(this::onAddIngredient);
		saveButton.addActionListener(e -> onSave(controller, viewModel));

		// Observe view model changes to update status label
		if (viewModel != null) {
			viewModel.addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if (RecipeStoreViewModel.PROP_SUCCESS_MESSAGE.equals(evt.getPropertyName())) {
						statusLabel.setForeground(new Color(0, 128, 0));
						statusLabel.setText((String) evt.getNewValue());
					} else if (RecipeStoreViewModel.PROP_ERROR_MESSAGE.equals(evt.getPropertyName())) {
						statusLabel.setForeground(Color.RED);
						statusLabel.setText((String) evt.getNewValue());
					}
				}
			});
		}
	}

	private void onAddIngredient(ActionEvent e) {
		String qty = ingredientQtyField.getText().trim();
		Unit unit = (Unit) unitCombo.getSelectedItem();
		String name = ingredientNameField.getText().trim();
		if (name.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Ingredient name cannot be empty", "Validation", JOptionPane.WARNING_MESSAGE);
			return;
		}

		String entry = String.format("%s %s %s", qty.isEmpty() ? "" : qty, unit != null ? unit.getAbbreviation() : "", name).trim();
		ingredientListModel.addElement(entry);

		// Clear small inputs
		ingredientQtyField.setText("");
		ingredientNameField.setText("");
	}

	private void onSave(StoreRecipeController controller, RecipeStoreViewModel viewModel) {
		String name = nameField.getText().trim();
		List<String> ingredients = new ArrayList<>();
		for (int i = 0; i < ingredientListModel.size(); i++) {
			ingredients.add(ingredientListModel.get(i));
		}
		String stepsRaw = stepsArea.getText();
		List<String> steps = new ArrayList<>();
		if (stepsRaw != null && !stepsRaw.isBlank()) {
			for (String s : stepsRaw.split("\\r?\\n")) {
				if (!s.trim().isEmpty()) steps.add(s.trim());
			}
		}

		int servingSize = 1;
		try {
			servingSize = Integer.parseInt(servingSizeField.getText().trim());
			if (servingSize <= 0) servingSize = 1;
		} catch (NumberFormatException ignored) {
			servingSize = 1;
		}

		// Call controller - this will trigger the presenter to update the view model
		controller.execute(name, ingredients, steps, servingSize);

		// Clear form after save is initiated
		clearForm();
	}

	private void clearForm() {
		nameField.setText("");
		ingredientListModel.clear();
		ingredientQtyField.setText("");
		ingredientNameField.setText("");
		stepsArea.setText("");
		servingSizeField.setText("1");
	}

}
