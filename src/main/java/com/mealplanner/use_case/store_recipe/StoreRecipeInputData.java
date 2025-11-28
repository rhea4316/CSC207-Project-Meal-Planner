package com.mealplanner.use_case.store_recipe;

// Data transfer object carrying new recipe data from user input.
// Responsible: Aaryan
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal input DTO for creating a recipe.
 */
public class StoreRecipeInputData {

	private final String recipeId;
	private final String name;
	private final List<String> ingredients;
	private final List<String> steps;
	private final int servingSize;

	public StoreRecipeInputData(String recipeId, String name, List<String> ingredients, List<String> steps, int servingSize) {
		this.recipeId = recipeId;
		this.name = name;
		this.ingredients = ingredients != null ? new ArrayList<>(ingredients) : new ArrayList<>();
		this.steps = steps != null ? new ArrayList<>(steps) : new ArrayList<>();
		this.servingSize = servingSize;
	}

	public StoreRecipeInputData(String name, List<String> ingredients, List<String> steps, int servingSize) {
		this(null, name, ingredients, steps, servingSize);
	}

	public String getRecipeId() {
		return recipeId;
	}

	public String getName() {
		return name;
	}

	public List<String> getIngredients() {
		return new ArrayList<>(ingredients);
	}

	public List<String> getSteps() {
		return new ArrayList<>(steps);
	}

	public int getServingSize() {
		return servingSize;
	}

}
