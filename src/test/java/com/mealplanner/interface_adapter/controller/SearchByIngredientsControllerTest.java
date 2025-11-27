package com.mealplanner.interface_adapter.controller;

import com.mealplanner.use_case.search_by_ingredients.SearchByIngredientsInputBoundary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test class for SearchByIngredientsController.
 * Tests controller input validation and interactor invocation.
 *
 * Responsible: Jerry (primary)
 */
public class SearchByIngredientsControllerTest {

    private SearchByIngredientsController controller;

    @Mock
    private SearchByIngredientsInputBoundary interactor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new SearchByIngredientsController(interactor);
    }

    @Test
    public void testExecuteWithValidInput() {
        // Arrange
        List<String> ingredients = Arrays.asList("chicken", "rice");

        // Act
        controller.execute(ingredients);

        // Assert
        verify(interactor).execute(argThat(inputData -> 
            inputData.getIngredients().size() == 2 &&
            inputData.getIngredients().contains("chicken") &&
            inputData.getIngredients().contains("rice")
        ));
    }

    @Test
    public void testExecuteWithEmptyIngredients() {
        // Arrange
        List<String> ingredients = Collections.emptyList();

        // Act
        controller.execute(ingredients);

        // Assert
        verify(interactor, never()).execute(any());
    }

    @Test
    public void testExecuteWithNullInput() {
        // Arrange
        List<String> ingredients = null;

        // Act
        controller.execute(ingredients);

        // Assert
        verify(interactor, never()).execute(any());
    }

    @Test
    public void testExecuteWithStringInput() {
        // Arrange
        String ingredientsRaw = "chicken, rice, broccoli";

        // Act
        controller.execute(ingredientsRaw);

        // Assert
        verify(interactor).execute(argThat(inputData -> 
            inputData.getIngredients().size() == 3 &&
            inputData.getIngredients().contains("chicken") &&
            inputData.getIngredients().contains("rice") &&
            inputData.getIngredients().contains("broccoli")
        ));
    }

    @Test
    public void testExecuteWithStringInputNewlineSeparated() {
        // Arrange
        String ingredientsRaw = "chicken\nrice\nbroccoli";

        // Act
        controller.execute(ingredientsRaw);

        // Assert
        verify(interactor).execute(argThat(inputData -> 
            inputData.getIngredients().size() == 3
        ));
    }

    @Test
    public void testExecuteWithEmptyString() {
        // Arrange
        String ingredientsRaw = "";

        // Act
        controller.execute(ingredientsRaw);

        // Assert
        verify(interactor, never()).execute(any());
    }

    @Test
    public void testExecuteWithNullString() {
        // Arrange
        String ingredientsRaw = null;

        // Act
        controller.execute(ingredientsRaw);

        // Assert
        verify(interactor, never()).execute(any());
    }
}
