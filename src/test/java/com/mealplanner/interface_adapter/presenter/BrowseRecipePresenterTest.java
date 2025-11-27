package com.mealplanner.interface_adapter.presenter;

import com.mealplanner.entity.Recipe;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.view_model.RecipeBrowseViewModel;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeOutputData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test class for BrowseRecipePresenter.
 * Tests formatting and presentation of recipe browsing.
 *
 * Responsible: Regina (primary)
 */
public class BrowseRecipePresenterTest {

    private BrowseRecipePresenter presenter;
    private RecipeBrowseViewModel viewModel;
    private ViewManagerModel viewManager;

    @BeforeEach
    public void setUp() {
        viewModel = new RecipeBrowseViewModel();
        viewManager = new ViewManagerModel();
        presenter = new BrowseRecipePresenter(viewModel, viewManager);
    }

    @Test
    public void testPresentRecipeList() {
        // Arrange
        List<Recipe> recipes = Arrays.asList(
            new Recipe("Pasta", Arrays.asList("pasta", "sauce"), "Cook pasta", 2),
            new Recipe("Pizza", Arrays.asList("dough", "cheese"), "Bake pizza", 4)
        );
        BrowseRecipeOutputData outputData = new BrowseRecipeOutputData(recipes);

        // Act
        presenter.presentRecipeDetails(outputData);

        // Assert
        assertEquals(2, viewModel.getRecipes().size());
        assertEquals("Pasta", viewModel.getRecipes().get(0).getName());
        assertEquals("Pizza", viewModel.getRecipes().get(1).getName());
        assertTrue(viewModel.isDisplayRecipes());
        assertEquals("BrowseRecipeView", viewManager.getActiveView());
    }

    @Test
    public void testPresentRecipeDetails() {
        // Arrange
        List<Recipe> recipes = Arrays.asList(
            new Recipe("Chicken", Arrays.asList("chicken", "rice"), "Cook chicken and rice", 2)
        );
        BrowseRecipeOutputData outputData = new BrowseRecipeOutputData(recipes);

        // Act
        presenter.presentRecipeDetails(outputData);

        // Assert
        assertEquals(1, viewModel.getRecipes().size());
        Recipe recipe = viewModel.getRecipes().get(0);
        assertEquals("Chicken", recipe.getName());
        assertEquals(2, recipe.getIngredients().size());
        assertTrue(recipe.getIngredients().contains("chicken"));
        assertTrue(recipe.getIngredients().contains("rice"));
        assertTrue(viewModel.isDisplayRecipes());
    }

    @Test
    public void testPresentError() {
        // Arrange
        String errorMessage = "No recipes found";

        // Act
        presenter.presentError(errorMessage);

        // Assert
        assertEquals(errorMessage, viewModel.getErrorMessage());
        assertFalse(viewModel.isDisplayRecipes());
    }

    @Test
    public void testPresentErrorNull() {
        // Act
        presenter.presentError(null);

        // Assert
        assertEquals("An error occurred", viewModel.getErrorMessage());
        assertFalse(viewModel.isDisplayRecipes());
    }

    @Test
    public void testPresentRecipeDetailsNull() {
        // Act
        presenter.presentRecipeDetails(null);

        // Assert
        assertEquals("No recipe data available", viewModel.getErrorMessage());
        assertFalse(viewModel.isDisplayRecipes());
    }

    @Test
    public void testPresentRecipeDetailsNullRecipes() {
        // Arrange
        BrowseRecipeOutputData outputData = new BrowseRecipeOutputData(null);

        // Act
        presenter.presentRecipeDetails(outputData);

        // Assert
        assertEquals("No recipe data available", viewModel.getErrorMessage());
        assertFalse(viewModel.isDisplayRecipes());
    }

    @Test
    public void testPresentRecipeDetailsEmptyList() {
        // Arrange
        BrowseRecipeOutputData outputData = new BrowseRecipeOutputData(Collections.emptyList());

        // Act
        presenter.presentRecipeDetails(outputData);

        // Assert
        assertTrue(viewModel.getRecipes().isEmpty());
        assertTrue(viewModel.isDisplayRecipes());
    }

    @Test
    public void testPresentRecipeDetailsUpdatesViewManager() {
        // Arrange
        List<Recipe> recipes = Arrays.asList(
            new Recipe("Test", Arrays.asList("ingredient"), "Cook", 1)
        );
        BrowseRecipeOutputData outputData = new BrowseRecipeOutputData(recipes);

        // Act
        presenter.presentRecipeDetails(outputData);

        // Assert
        assertEquals("BrowseRecipeView", viewManager.getActiveView());
    }
}
