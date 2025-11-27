package com.mealplanner.interface_adapter.presenter;

import com.mealplanner.entity.Recipe;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.view_model.RecipeSearchViewModel;
import com.mealplanner.use_case.search_by_ingredients.SearchByIngredientsOutputData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test class for SearchByIngredientsPresenter.
 * Tests formatting and presentation of search results.
 *
 * Responsible: Jerry (primary)
 */
public class SearchByIngredientsPresenterTest {

    private SearchByIngredientsPresenter presenter;
    private RecipeSearchViewModel viewModel;
    private ViewManagerModel viewManager;

    @BeforeEach
    public void setUp() {
        viewModel = new RecipeSearchViewModel();
        viewManager = new ViewManagerModel();
        presenter = new SearchByIngredientsPresenter(viewModel, viewManager);
    }

    @Test
    public void testPresentSuccess() {
        // Arrange
        List<Recipe> recipes = Arrays.asList(
            new Recipe("Chicken and Rice", Arrays.asList("chicken", "rice"), "Cook", 2),
            new Recipe("Chicken Salad", Arrays.asList("chicken", "lettuce"), "Mix", 1)
        );
        SearchByIngredientsOutputData outputData = new SearchByIngredientsOutputData(recipes);

        // Act
        presenter.presentRecipes(outputData);

        // Assert
        assertEquals(2, viewModel.getRecipes().size());
        assertEquals("Chicken and Rice", viewModel.getRecipes().get(0).getName());
        assertEquals("Chicken Salad", viewModel.getRecipes().get(1).getName());
        assertEquals("", viewModel.getErrorMessage());
        assertFalse(viewModel.isLoading());
        assertEquals("SearchByIngredientsView", viewManager.getActiveView());
    }

    @Test
    public void testPresentError() {
        // Arrange
        String errorMessage = "Network error occurred";

        // Act
        presenter.presentError(errorMessage);

        // Assert
        assertEquals(errorMessage, viewModel.getErrorMessage());
        assertFalse(viewModel.isLoading());
    }

    @Test
    public void testPresentEmptyResults() {
        // Arrange
        SearchByIngredientsOutputData outputData = new SearchByIngredientsOutputData(Collections.emptyList());

        // Act
        presenter.presentRecipes(outputData);

        // Assert
        assertEquals("No recipes found matching the provided ingredients", viewModel.getErrorMessage());
        assertFalse(viewModel.isLoading());
    }

    @Test
    public void testFormatRecipeList() {
        // Arrange
        List<Recipe> recipes = Arrays.asList(
            new Recipe("Pasta", Arrays.asList("pasta", "sauce"), "Cook pasta", 2)
        );
        SearchByIngredientsOutputData outputData = new SearchByIngredientsOutputData(recipes);

        // Act
        presenter.presentRecipes(outputData);

        // Assert
        assertEquals(1, viewModel.getRecipes().size());
        Recipe recipe = viewModel.getRecipes().get(0);
        assertEquals("Pasta", recipe.getName());
        assertEquals(2, recipe.getIngredients().size());
    }

    @Test
    public void testPresentRecipesNull() {
        // Act
        presenter.presentRecipes(null);

        // Assert
        assertEquals("No recipes found matching the provided ingredients", viewModel.getErrorMessage());
        assertFalse(viewModel.isLoading());
    }

    @Test
    public void testPresentErrorNull() {
        // Act
        presenter.presentError(null);

        // Assert
        assertEquals("An error occurred", viewModel.getErrorMessage());
        assertFalse(viewModel.isLoading());
    }

    @Test
    public void testPresentRecipesWithNullList() {
        // Arrange
        SearchByIngredientsOutputData outputData = new SearchByIngredientsOutputData(null);

        // Act
        presenter.presentRecipes(outputData);

        // Assert
        assertEquals("No recipes found matching the provided ingredients", viewModel.getErrorMessage());
        assertFalse(viewModel.isLoading());
    }
}
