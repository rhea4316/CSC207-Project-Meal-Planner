package com.mealplanner.interface_adapter.controller;

// Controller for search by ingredients feature - receives user input and calls interactor.
// Responsible: Jerry

import com.mealplanner.use_case.search_by_ingredients.SearchByIngredientsInputBoundary;
import com.mealplanner.use_case.search_by_ingredients.SearchByIngredientsInputData;
import com.mealplanner.util.StringUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SearchByIngredientsController {
    private final SearchByIngredientsInputBoundary interactor;

    public SearchByIngredientsController(SearchByIngredientsInputBoundary interactor) {
        this.interactor = Objects.requireNonNull(interactor, "Interactor cannot be null");
    }

    /**
     * Execute the use case with a list of ingredients.
     */
    public void execute(List<String> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return; // Let interactor handle validation
        }
        SearchByIngredientsInputData inputData = new SearchByIngredientsInputData(ingredients);
        interactor.execute(inputData);
    }

    /**
     * Convenience method: accepts raw form string for ingredients (comma or newline separated),
     * parses them into a list, and calls the use case.
     */
    public void execute(String ingredientsRaw) {
        List<String> ingredients = parseListFromString(ingredientsRaw);
        execute(ingredients);
    }

    private List<String> parseListFromString(String raw) {
        if (StringUtil.isNullOrEmpty(raw)) {
            return List.of();
        }
        return Arrays.stream(raw.split("\\r?\\n|,"))
                .map(StringUtil::safeTrim)
                .filter(s -> !StringUtil.isNullOrEmpty(s))
                .collect(Collectors.toList());
    }
}
