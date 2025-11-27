package com.mealplanner.use_case;

import com.mealplanner.entity.Recipe;
import com.mealplanner.use_case.search_by_ingredients.SearchByIngredientsDataAccessInterface;
import com.mealplanner.use_case.search_by_ingredients.SearchByIngredientsInteractor;
import com.mealplanner.use_case.search_by_ingredients.SearchByIngredientsInputData;
import com.mealplanner.use_case.search_by_ingredients.SearchByIngredientsOutputBoundary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Integration tests for search by ingredients use case.
// Responsible: Jerry (primary), Everyone (testing)
public class SearchByIngredientsInteractorTest {

    private SearchByIngredientsInteractor interactor;

    @Mock
    private SearchByIngredientsDataAccessInterface dataAccess;

    @Mock
    private SearchByIngredientsOutputBoundary presenter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        interactor = new SearchByIngredientsInteractor(dataAccess, presenter);
    }

    @Test
    public void testSearchByIngredientsSuccess() throws IOException {
        // Arrange
        List<String> ingredients = Arrays.asList("chicken", "rice");
        SearchByIngredientsInputData inputData = new SearchByIngredientsInputData(ingredients);
        
        List<Recipe> recipes = Arrays.asList(
            new Recipe("Chicken and Rice", Arrays.asList("chicken", "rice"), "Cook", 2),
            new Recipe("Chicken Fried Rice", Arrays.asList("chicken", "rice", "eggs"), "Fry", 4)
        );
        
        when(dataAccess.searchByIngredients(ingredients)).thenReturn(recipes);

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess).searchByIngredients(ingredients);
        verify(presenter).presentRecipes(argThat(outputData -> 
            outputData.getRecipes().size() == 2 &&
            outputData.getRecipes().equals(recipes)
        ));
        verify(presenter, never()).presentError(anyString());
    }

    @Test
    public void testSearchByIngredientsEmptyResult() throws IOException {
        // Arrange
        List<String> ingredients = Arrays.asList("unicorn", "dragon");
        SearchByIngredientsInputData inputData = new SearchByIngredientsInputData(ingredients);
        
        when(dataAccess.searchByIngredients(ingredients)).thenReturn(Collections.emptyList());

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess).searchByIngredients(ingredients);
        verify(presenter).presentError("No recipes found matching the provided ingredients");
        verify(presenter, never()).presentRecipes(any());
    }

    @Test
    public void testSearchByIngredientsNullResult() throws IOException {
        // Arrange
        List<String> ingredients = Arrays.asList("chicken");
        SearchByIngredientsInputData inputData = new SearchByIngredientsInputData(ingredients);
        
        when(dataAccess.searchByIngredients(ingredients)).thenReturn(null);

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess).searchByIngredients(ingredients);
        verify(presenter).presentError("No recipes found matching the provided ingredients");
        verify(presenter, never()).presentRecipes(any());
    }

    @Test
    public void testSearchByIngredientsNullInput() throws IOException {
        // Act
        interactor.execute(null);

        // Assert
        verify(dataAccess, never()).searchByIngredients(any());
        verify(presenter).presentError("Input data cannot be null");
        verify(presenter, never()).presentRecipes(any());
    }

    @Test
    public void testSearchByIngredientsEmptyList() throws IOException {
        // Arrange
        SearchByIngredientsInputData inputData = new SearchByIngredientsInputData(Collections.emptyList());

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess, never()).searchByIngredients(any());
        verify(presenter).presentError("Please provide at least one ingredient");
        verify(presenter, never()).presentRecipes(any());
    }

    @Test
    public void testSearchByIngredientsIOException() throws IOException {
        // Arrange
        List<String> ingredients = Arrays.asList("chicken");
        SearchByIngredientsInputData inputData = new SearchByIngredientsInputData(ingredients);
        
        when(dataAccess.searchByIngredients(ingredients)).thenThrow(new IOException("Network error"));

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess).searchByIngredients(ingredients);
        verify(presenter).presentError("Network error: Network error");
        verify(presenter, never()).presentRecipes(any());
    }

    @Test
    public void testSearchByIngredientsIllegalArgumentException() throws IOException {
        // Arrange
        List<String> ingredients = Arrays.asList("chicken");
        SearchByIngredientsInputData inputData = new SearchByIngredientsInputData(ingredients);
        
        when(dataAccess.searchByIngredients(ingredients)).thenThrow(new IllegalArgumentException("Invalid ingredient"));

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess).searchByIngredients(ingredients);
        verify(presenter).presentError("Invalid input: Invalid ingredient");
        verify(presenter, never()).presentRecipes(any());
    }

    @Test
    public void testSearchByIngredientsUnexpectedException() throws IOException {
        // Arrange
        List<String> ingredients = Arrays.asList("chicken");
        SearchByIngredientsInputData inputData = new SearchByIngredientsInputData(ingredients);
        
        when(dataAccess.searchByIngredients(ingredients)).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess).searchByIngredients(ingredients);
        verify(presenter).presentError("An error occurred: Unexpected error");
        verify(presenter, never()).presentRecipes(any());
    }

    @Test
    public void testSearchByIngredientsSingleIngredient() throws IOException {
        // Arrange
        List<String> ingredients = Arrays.asList("chicken");
        SearchByIngredientsInputData inputData = new SearchByIngredientsInputData(ingredients);
        
        List<Recipe> recipes = Arrays.asList(
            new Recipe("Chicken", Arrays.asList("chicken"), "Cook", 1)
        );
        
        when(dataAccess.searchByIngredients(ingredients)).thenReturn(recipes);

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess).searchByIngredients(ingredients);
        verify(presenter).presentRecipes(any());
    }

    @Test
    public void testSearchByIngredientsMultipleIngredients() throws IOException {
        // Arrange
        List<String> ingredients = Arrays.asList("chicken", "rice", "broccoli");
        SearchByIngredientsInputData inputData = new SearchByIngredientsInputData(ingredients);
        
        List<Recipe> recipes = Arrays.asList(
            new Recipe("Chicken Rice Bowl", Arrays.asList("chicken", "rice", "broccoli"), "Cook", 2)
        );
        
        when(dataAccess.searchByIngredients(ingredients)).thenReturn(recipes);

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess).searchByIngredients(ingredients);
        verify(presenter).presentRecipes(any());
    }
}
