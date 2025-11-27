package com.mealplanner.interface_adapter.view_model;

// ViewModel for recipe creation form - holds data for StoreRecipeView.
// Responsible: Aaryan, Everyone (GUI)
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * View model backing the recipe creation view. Holds current form values and
 * presentation messages (success / error). Uses {@link PropertyChangeSupport}
 * so views can observe changes.
 */
public class RecipeStoreViewModel {

	public static final String PROP_NAME = "name";
	public static final String PROP_INGREDIENTS = "ingredients";
	public static final String PROP_STEPS = "steps";
	public static final String PROP_SERVING_SIZE = "servingSize";
	public static final String PROP_SUCCESS_MESSAGE = "successMessage";
	public static final String PROP_ERROR_MESSAGE = "errorMessage";

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	private String name = "";
	private List<String> ingredients = new ArrayList<>();
	private List<String> steps = new ArrayList<>();
	private int servingSize = 1;

	private String successMessage;
	private String errorMessage;

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	// Name
	public String getName() {
		return name;
	}

	public void setName(String name) {
		String old = this.name;
		this.name = name != null ? name : "";
		pcs.firePropertyChange(PROP_NAME, old, this.name);
	}

	// Ingredients
	public List<String> getIngredients() {
		return new ArrayList<>(ingredients);
	}

	public void setIngredients(List<String> ingredients) {
		List<String> old = new ArrayList<>(this.ingredients);
		this.ingredients = ingredients != null ? new ArrayList<>(ingredients) : new ArrayList<>();
		pcs.firePropertyChange(PROP_INGREDIENTS, old, new ArrayList<>(this.ingredients));
	}

	// Steps
	public List<String> getSteps() {
		return new ArrayList<>(steps);
	}

	public void setSteps(List<String> steps) {
		List<String> old = new ArrayList<>(this.steps);
		this.steps = steps != null ? new ArrayList<>(steps) : new ArrayList<>();
		pcs.firePropertyChange(PROP_STEPS, old, new ArrayList<>(this.steps));
	}

	// Serving size
	public int getServingSize() {
		return servingSize;
	}

	public void setServingSize(int servingSize) {
		int old = this.servingSize;
		this.servingSize = Math.max(1, servingSize);
		pcs.firePropertyChange(PROP_SERVING_SIZE, old, this.servingSize);
	}

	// Success / Error messages
	public String getSuccessMessage() {
		return successMessage;
	}

	public void setSuccessMessage(String successMessage) {
		String old = this.successMessage;
		this.successMessage = successMessage;
		pcs.firePropertyChange(PROP_SUCCESS_MESSAGE, old, this.successMessage);
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		String old = this.errorMessage;
		this.errorMessage = errorMessage;
		pcs.firePropertyChange(PROP_ERROR_MESSAGE, old, this.errorMessage);
	}

}
