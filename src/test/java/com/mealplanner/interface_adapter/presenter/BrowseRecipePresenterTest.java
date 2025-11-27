package com.mealplanner.interface_adapter.presenter;

import com.mealplanner.entity.Recipe;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.view_model.RecipeBrowseViewModel;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeOutputData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

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

    private RecipeBrowseViewModel viewModel;

    private ViewManagerModel viewManager;

    @Mock
    private com.mealplanner.interface_adapter.view_model.RecipeBrowseViewModel viewModel;

    @Mock
    private com.mealplanner.interface_adapter.ViewManagerModel viewManager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        presenter = new BrowseRecipePresenter(viewModel, viewManager);
    }

    @Test
    public void testPresentRecipeList() {
        com.mealplanner.entity.Recipe recipe = new com.mealplanner.entity.Recipe(
            "Pasta", java.util.Arrays.asList("pasta", "sauce"), "Cook pasta", 2, null, null, null, "recipe-1");
        java.util.List<com.mealplanner.entity.Recipe> recipes = java.util.Arrays.asList(recipe);
        com.mealplanner.use_case.browse_recipe.BrowseRecipeOutputData outputData = 
            new com.mealplanner.use_case.browse_recipe.BrowseRecipeOutputData(recipes);
        
        presenter.presentRecipeDetails(outputData);
        
        verify(viewModel).setRecipes(recipes);
        verify(viewManager).setActiveView("BrowseRecipeView");
    }

    @Test
    public void testPresentRecipeDetails() {
        com.mealplanner.entity.Recipe recipe = new com.mealplanner.entity.Recipe(
            "Pasta", java.util.Arrays.asList("pasta", "sauce"), "Cook pasta", 2, null, null, null, "recipe-1");
        java.util.List<com.mealplanner.entity.Recipe> recipes = java.util.Arrays.asList(recipe);
        com.mealplanner.use_case.browse_recipe.BrowseRecipeOutputData outputData = 
            new com.mealplanner.use_case.browse_recipe.BrowseRecipeOutputData(recipes);
        
        presenter.presentRecipeDetails(outputData);
        
        verify(viewModel).setRecipes(recipes);
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
        String errorMessage = "Recipe not found";
        
        presenter.presentError(errorMessage);
        
        verify(viewModel).setErrorMessage(errorMessage);
    }

    @Test
    public void testPresentNullOutputData() {
        presenter.presentRecipeDetails(null);
        
        verify(viewModel).setErrorMessage("No recipe data available");
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