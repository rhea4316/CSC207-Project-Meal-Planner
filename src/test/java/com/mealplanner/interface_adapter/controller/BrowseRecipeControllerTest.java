package com.mealplanner.interface_adapter.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

/**
 * Test class for BrowseRecipeController.
 * Tests controller input validation and interactor invocation.
 *
 * Responsible: Regina (primary)
 */
public class BrowseRecipeControllerTest {

    private BrowseRecipeController controller;

    @Mock
    private com.mealplanner.use_case.browse_recipe.BrowseRecipeInputBoundary interactor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new BrowseRecipeController(interactor);
    }

    @Test
    public void testBrowseRecipes() throws Exception {
        String query = "pasta";
        int numberOfRecipes = 5;
        
        controller.execute(query, numberOfRecipes);
        
        verify(interactor).execute(argThat(inputData -> 
            inputData.getQuery().equals(query) &&
            inputData.getNumberOfRecipesInt() == numberOfRecipes
        ));
    }

    @Test
    public void testViewRecipeDetails() throws Exception {
        String query = "pasta";
        int numberOfRecipes = 5;
        String ingredients = "tomato, cheese";
        
        controller.execute(query, numberOfRecipes, ingredients);
        
        verify(interactor).execute(argThat(inputData -> 
            inputData.getQuery().equals(query) &&
            inputData.getNumberOfRecipesInt() == numberOfRecipes &&
            inputData.getIncludedIngredients().equals(ingredients)
        ));
    }

    @Test
    public void testBrowseRecipesWithEmptyQuery() throws Exception {
        controller.execute("", 5);
        verify(interactor, never()).execute(any());
    }
}
