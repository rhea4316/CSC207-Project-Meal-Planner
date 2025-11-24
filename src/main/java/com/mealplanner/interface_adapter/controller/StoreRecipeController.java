package com.mealplanner.interface_adapter.controller;

// Controller for storing new recipes - receives recipe form data and calls interactor.
// Responsible: Aaryan
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.mealplanner.use_case.store_recipe.StoreRecipeInputBoundary;
import com.mealplanner.use_case.store_recipe.StoreRecipeInputData;


 //Controller for storing new recipes. Converts raw form input into a use-case InputData object and calls the interactor.
 
public class StoreRecipeController {

	private final StoreRecipeInputBoundary interactor;

	public StoreRecipeController(StoreRecipeInputBoundary interactor) {
		this.interactor = Objects.requireNonNull(interactor);
	}

	/**
	 * Execute the use case with already-parsed lists.
	 */
	public void execute(String name, List<String> ingredients, List<String> steps, int servingSize) {
		StoreRecipeInputData input = new StoreRecipeInputData(name, ingredients, steps, servingSize);
		interactor.execute(input);
	}

	/**
	 * Convenience method: accepts raw form strings for ingredients and steps (comma or newline separated),
	 * parses them into lists, and calls the use case. If servingSizeStr is invalid, defaults to 1.
	 */
	public void executeFromForm(String name, String ingredientsRaw, String stepsRaw, String servingSizeStr) {
		List<String> ingredients = parseListFromString(ingredientsRaw);
		List<String> steps = parseListFromString(stepsRaw);

		int servingSize = 1;
		if (servingSizeStr != null) {
			try {
				servingSize = Integer.parseInt(servingSizeStr.trim());
				if (servingSize <= 0) servingSize = 1;
			} catch (NumberFormatException ignored) {
				// makes default 1
			}
		}

		execute(name, ingredients, steps, servingSize);
	}

	private List<String> parseListFromString(String raw) {
		if (raw == null) return List.of();
		return Arrays.stream(raw.split("\\r?\\n|,"))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toList());
	}

}
