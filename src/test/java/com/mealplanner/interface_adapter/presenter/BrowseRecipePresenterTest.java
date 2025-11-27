package com.mealplanner.interface_adapter.presenter;

import com.mealplanner.entity.Recipe;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.view_model.RecipeBrowseViewModel;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeOutputData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for BrowseRecipePresenter.
 * Tests formatting and presentation of recipe browsing.
 *
 * Responsible: Regina (primary)
 *
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
        List<Recipe> mockRecipes = Arrays.asList(
                new Recipe("Chicken Alfredo", Arrays.asList("pasta", "chicken", "cream"), "steps", 1),
                new Recipe("Creamy Garlic Chicken Pasta", Arrays.asList("pasta", "chicken", "garlic"), "steps", 1));
        BrowseRecipeOutputData outputData = new BrowseRecipeOutputData(mockRecipes);

        presenter.presentRecipeDetails(outputData);
        assertEquals(mockRecipes, viewModel.getRecipes());
        assertEquals("BrowseRecipeView", viewManager.getActiveView());
        assertEquals("", viewModel.getErrorMessage());
    }

    @Test
    public void testPresentRecipeDetailsWithNullOutputData() {
        // Arrange
        BrowseRecipeOutputData outputData = null;

        // Act
        presenter.presentRecipeDetails(outputData);

        assertEquals("No recipe data available", viewModel.getErrorMessage());
        assertTrue(viewModel.getRecipes().isEmpty());
        assertNull(viewManager.getActiveView());

    }

    @Test
    public void testPresentRecipeDetailsWithNullRecipes() {
        // Arrange
        BrowseRecipeOutputData outputData = null;

        // Act
        presenter.presentRecipeDetails(outputData);

        assertEquals("No recipe data available", viewModel.getErrorMessage());
        assertTrue(viewModel.getRecipes().isEmpty());
        assertNull(viewManager.getActiveView());
    }

    @Test
    public void testPresentRecipeDetailsWithEmptyRecipes() {
        // Arrange
        List<Recipe> emptyRecipes = Collections.emptyList();
        BrowseRecipeOutputData outputData = new BrowseRecipeOutputData(emptyRecipes);

        // Act
        presenter.presentRecipeDetails(outputData);

        assertEquals(emptyRecipes, viewModel.getRecipes());
        assertEquals("BrowseRecipeView", viewManager.getActiveView());
    }


    @Test
    public void testPresentError() {
        String errorMessage = "No recipes found";

        presenter.presentError(errorMessage);
        assertEquals("No recipes found", viewModel.getErrorMessage());
        assertTrue(viewModel.getRecipes().isEmpty());
        assertNull(viewManager.getActiveView());
    }

    @Test
    public void testPresentErrorWithEmptyMessage() {
        String errorMessage = "";

        presenter.presentError(errorMessage);

        assertEquals(anyString(), viewModel.getErrorMessage());
        assertTrue(viewModel.getRecipes().isEmpty());
        assertNull(viewManager.getActiveView());
    }
}