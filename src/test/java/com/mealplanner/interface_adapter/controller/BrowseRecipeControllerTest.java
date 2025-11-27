package com.mealplanner.interface_adapter.controller;

import com.mealplanner.use_case.browse_recipe.BrowseRecipeInputBoundary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import java.io.IOException;

/**
 * Test class for BrowseRecipeController.
 * Tests controller input validation and interactor invocation.
 *
 * Responsible: Regina (primary)
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
        // Arrange
        String query = "pasta";
        int numberOfRecipes = 5;

        // Act
        controller.execute(query, numberOfRecipes);

        // Assert
        verify(interactor).execute(argThat(inputData -> 
            inputData.getQuery().equals(query) &&
            inputData.getNumberOfRecipesInt() == numberOfRecipes
        ));
    }

    @Test
    public void testViewRecipeDetails() throws IOException {
        // Arrange
        String query = "chicken";
        int numberOfRecipes = 10;
        String ingredients = "chicken, rice";

        // Act
        controller.execute(query, numberOfRecipes, ingredients);

        // Assert
        verify(interactor).execute(argThat(inputData -> 
            inputData.getQuery().equals(query) &&
            inputData.getNumberOfRecipesInt() == numberOfRecipes &&
            inputData.getIncludedIngredients().equals(ingredients)
        ));
    }

    @Test
    public void testBrowseRecipesWithEmptyQuery() throws IOException {
        // Arrange
        String query = "";
        int numberOfRecipes = 5;

        // Act
        controller.execute(query, numberOfRecipes);

        // Assert
        verify(interactor, never()).execute(any());
    }

    @Test
    public void testBrowseRecipesWithNullQuery() throws IOException {
        // Arrange
        String query = null;
        int numberOfRecipes = 5;

        // Act
        controller.execute(query, numberOfRecipes);

        // Assert
        verify(interactor, never()).execute(any());
    }
}
