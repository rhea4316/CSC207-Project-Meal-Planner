package com.mealplanner.use_case.browse_recipe;

import com.mealplanner.entity.Recipe;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

/**
 * Test class for BrowseRecipeInteractor.
 * Tests recipe browsing functionality and ingredient display.
 *
 * Responsible: Regina (primary)
 */
public class BrowseRecipeInteractorTest {

    private BrowseRecipeInteractor interactor;

    @Mock
    private BrowseRecipeDataAccessInterface dataAccess;

    @Mock
    private BrowseRecipeOutputBoundary presenter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        interactor = new BrowseRecipeInteractor(dataAccess, presenter);
    }

    @Test
    public void testBrowseRecipesSuccess() throws Exception {
        BrowseRecipeInputData inputData = new BrowseRecipeInputData("pasta", 5);
        Recipe recipe1 = new Recipe("Pasta", java.util.Arrays.asList("pasta", "sauce"), "Cook pasta", 2, null, null, null, "recipe-1");
        Recipe recipe2 = new Recipe("Spaghetti", java.util.Arrays.asList("spaghetti", "sauce"), "Cook spaghetti", 2, null, null, null, "recipe-2");
        java.util.List<Recipe> recipes = java.util.Arrays.asList(recipe1, recipe2);
        
        when(dataAccess.searchRecipes(inputData)).thenReturn(recipes);
        
        interactor.execute(inputData);
        
        verify(presenter).presentRecipeDetails(any(BrowseRecipeOutputData.class));
        verify(presenter, never()).presentError(anyString());
    }

    @Test
    public void testBrowseRecipesEmpty() throws Exception {
        BrowseRecipeInputData inputData = new BrowseRecipeInputData("nonexistent", 5);
        
        when(dataAccess.searchRecipes(inputData)).thenReturn(java.util.Collections.emptyList());
        
        interactor.execute(inputData);
        
        verify(presenter).presentError(anyString());
        verify(presenter, never()).presentRecipeDetails(any(BrowseRecipeOutputData.class));
    }

    @Test
    public void testViewRecipeDetails() throws Exception {
        BrowseRecipeInputData inputData = new BrowseRecipeInputData("pasta", 5);
        Recipe recipe = new Recipe("Pasta", java.util.Arrays.asList("pasta", "sauce"), "Cook pasta", 2, null, null, null, "recipe-1");
        java.util.List<Recipe> recipes = java.util.Arrays.asList(recipe);
        
        when(dataAccess.searchRecipes(inputData)).thenReturn(recipes);
        
        interactor.execute(inputData);
        
        verify(presenter).presentRecipeDetails(argThat(outputData -> 
            outputData.getRecipes().size() == 1 && 
            outputData.getRecipes().get(0).getName().equals("Pasta")
        ));
    }

    @Test
    public void testRecipeNotFound() throws Exception {
        BrowseRecipeInputData inputData = new BrowseRecipeInputData("nonexistent", 5);
        
        when(dataAccess.searchRecipes(inputData)).thenThrow(new com.mealplanner.exception.RecipeNotFoundException("Recipe not found"));
        
        interactor.execute(inputData);
        
        verify(presenter).presentError("Recipe not found");
    }

    @Test
    public void testApiFailure() throws Exception {
        BrowseRecipeInputData inputData = new BrowseRecipeInputData("pasta", 5);
        
        when(dataAccess.searchRecipes(inputData)).thenThrow(new java.io.IOException("Network error"));
        
        interactor.execute(inputData);
        
        verify(presenter).presentError(contains("Network error"));
    }

    @Test
    public void testNullInputData() throws Exception {
        interactor.execute(null);
        
        verify(presenter).presentError("Input data cannot be null");
    }

    @Test
    public void testEmptyQuery() throws Exception {
        BrowseRecipeInputData inputData = new BrowseRecipeInputData("", 5);
        
        interactor.execute(inputData);
        
        verify(presenter).presentError("Search query cannot be empty");
    }
}
