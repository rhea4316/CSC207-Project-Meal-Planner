package com.mealplanner.interface_adapter.presenter;

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
}
