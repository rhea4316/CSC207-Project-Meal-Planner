package com.mealplanner.interface_adapter.controller;

import com.mealplanner.use_case.browse_recipe.BrowseRecipeOutputData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;

import static org.mockito.Mockito.*;

import java.io.IOException;

/**
 * Test class for BrowseRecipeController.
 * Tests controller input validation and interactor invocation.
 *
 * Responsible: Regina (primary)
 *
 */
public class BrowseRecipeControllerTest {

    private BrowseRecipeController controller;

    @Mock
    private BrowseRecipeInputBoundary interactor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new BrowseRecipeController(interactor);
    }

    @Test
    public void testBrowseRecipes() throws IOException {
        String query = "pasta";
        int numberOfRecipes = 1;
        String ingredients = "tomato";

        controller.execute(query, numberOfRecipes, ingredients);
        verify(interactor).execute(argThat(inputData ->
                inputData != null &&
                query.equals(inputData.getQuery()) &&
                numberOfRecipes == inputData.getNumberOfRecipesInt() &&
                ingredients.equals(inputData.getIncludedIngredients())
        ));
    }

    @Test
    public void testBrowseRecipesWithoutIngredients() throws IOException {
        String query = "pasta";
        int numberOfRecipes = 3;

        controller.execute(query, numberOfRecipes);
        verify(interactor).execute(argThat(inputData ->
                inputData != null &&
                query.equals(inputData.getQuery()) &&
                numberOfRecipes == inputData.getNumberOfRecipesInt() &&
                inputData.getIncludedIngredients() == null
        ));
    }

    @Test
    public void testBrowseRecipesWithEmptyQuery() throws IOException {
        // Arrange
        String query = "";
        int numberOfRecipes = 5;
        String ingredients = "chicken";

        // Act
        controller.execute(query, numberOfRecipes, ingredients);

        // Assert
        verify(interactor, never()).execute(any());
    }

    @Test
    public void testBrowseRecipesWithNullQuery() throws IOException {
        // Arrange
        String query = null;
        int numberOfRecipes = 5;
        String ingredients = "chicken";

        // Act
        controller.execute(query, numberOfRecipes, ingredients);

        // Assert
        verify(interactor, never()).execute(any());
    }
}
