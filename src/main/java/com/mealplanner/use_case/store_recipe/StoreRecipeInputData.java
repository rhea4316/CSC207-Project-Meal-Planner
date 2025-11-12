package com.mealplanner.use_case.store_recipe;

// Data transfer object carrying new recipe data from user input.
// Responsible: Aaryan
// TODO: Implement with recipe name, ingredients list, cooking instructions, and other recipe details
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal input DTO for creating a recipe.
 */
public class StoreRecipeInputData {

	private final String name;
	private final List<String> ingredients;
	private final List<String> steps;
	private final int servingSize;

	public StoreRecipeInputData(String name, List<String> ingredients, List<String> steps, int servingSize) {
		this.name = name;
		this.ingredients = ingredients != null ? new ArrayList<>(ingredients) : new ArrayList<>();
		this.steps = steps != null ? new ArrayList<>(steps) : new ArrayList<>();
		this.servingSize = servingSize;
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
