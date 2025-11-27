package com.mealplanner.use_case.browse_recipe;

import com.mealplanner.entity.Recipe;
import com.mealplanner.exception.RecipeNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Test class for BrowseRecipeInteractor.
 * Tests recipe browsing functionality and ingredient display.
 *
 * Responsible: Regina (primary)
 *
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
    public void testBrowseRecipesSuccess() throws IOException, RecipeNotFoundException {
        BrowseRecipeInputData inputData = new BrowseRecipeInputData("pasta",
                2,
                "chicken");
        List<Recipe> mockRecipes = Arrays.asList(
                new Recipe("Chicken Alfredo", Arrays.asList("pasta", "chicken", "cream"), "steps", 1),
                new Recipe("Creamy Garlic Chicken Pasta", Arrays.asList("pasta", "chicken", "garlic"), "steps", 1));

        when(dataAccess.searchRecipes(inputData)).thenReturn(mockRecipes);
        interactor.execute(inputData);

        verify(dataAccess).searchRecipes(inputData);
        verify(presenter).presentRecipeDetails(any(BrowseRecipeOutputData.class));
        verify(presenter, never()).presentError(anyString());
    }

    @Test
    public void testBrowseRecipesEmpty() throws RecipeNotFoundException, IOException {
        BrowseRecipeInputData inputData = new BrowseRecipeInputData("pasta", 0);
        when(dataAccess.searchRecipes(inputData)).thenReturn(Collections.emptyList());

        interactor.execute(inputData);
        verify(dataAccess).searchRecipes(inputData);
        verify(presenter).presentError("No recipes found, please try different wording " +
                "in your search query.");
        verify(presenter, never()).presentRecipeDetails(any(BrowseRecipeOutputData.class));
    }

    @Test
    public void testInvalidInput() throws RecipeNotFoundException, IOException {
        BrowseRecipeInputData inputData = new BrowseRecipeInputData("pasta", 2, "chicken");
        when(dataAccess.searchRecipes(inputData)).thenThrow(new IllegalArgumentException("Invalid parameters"));

        interactor.execute(inputData);
        verify(dataAccess).searchRecipes(inputData);
        verify(presenter).presentError("Invalid input: Invalid parameters");
        verify(presenter, never()).presentRecipeDetails(any());
    }

    @Test
    public void testRecipeNotFound() throws IOException, RecipeNotFoundException {
        BrowseRecipeInputData inputData = new BrowseRecipeInputData("pasta", 0);
        when(dataAccess.searchRecipes(inputData)).thenThrow(new RecipeNotFoundException(""));

        interactor.execute(inputData);
        verify(dataAccess).searchRecipes(inputData);
        verify(presenter).presentError("Recipe not found");
        verify(presenter, never()).presentRecipeDetails(any());
    }

    @Test
    public void testApiFailure() throws IOException, RecipeNotFoundException {
        BrowseRecipeInputData inputData = new BrowseRecipeInputData("pasta", 2, "chicken");
        when(dataAccess.searchRecipes(inputData)).thenThrow(new IOException("API connection failed"));

        interactor.execute(inputData);
        verify(dataAccess).searchRecipes(inputData);
        verify(presenter).presentError("Network error: API connection failed");
        verify(presenter, never()).presentRecipeDetails(any());
    }

    @Test
    public void testNullInputData() throws IOException, RecipeNotFoundException {
        // Act
        interactor.execute(null);

        // Assert
        verify(dataAccess, never()).searchRecipes(any());
        verify(presenter).presentError("Input data cannot be null");
        verify(presenter, never()).presentRecipeDetails(any());
    }

    @Test
    public void testEmptyQuery() throws RecipeNotFoundException, IOException {
        // Arrange
        BrowseRecipeInputData inputData = new BrowseRecipeInputData("", 2, "chicken");

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess, never()).searchRecipes(any());
        verify(presenter).presentError("Search query cannot be empty");
        verify(presenter, never()).presentRecipeDetails(any());
    }

    @Test
    public void testNullQuery() throws RecipeNotFoundException, IOException {
        // Arrange
        BrowseRecipeInputData inputData = new BrowseRecipeInputData(null, 2, "chicken");

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess, never()).searchRecipes(any());
        verify(presenter).presentError("Search query cannot be empty");
        verify(presenter, never()).presentRecipeDetails(any());
    }
}
