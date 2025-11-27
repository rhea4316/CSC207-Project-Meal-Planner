package com.mealplanner.interface_adapter.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

/**
 * Test class for SearchByIngredientsController.
 * Tests controller input validation and interactor invocation.
 *
 * Responsible: Jerry (primary)
 */
public class SearchByIngredientsControllerTest {

    private SearchByIngredientsController controller;

    @Mock
    private com.mealplanner.use_case.search_by_ingredients.SearchByIngredientsInputBoundary interactor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new SearchByIngredientsController(interactor);
    }

    @Test
    public void testExecuteWithValidInput() throws Exception {
        java.util.List<String> ingredients = java.util.Arrays.asList("tomato", "cheese", "pasta");
        
        controller.execute(ingredients);
        
        Thread.sleep(100);
        
        verify(interactor, timeout(1000)).execute(argThat(inputData -> 
            inputData.getIngredients().size() == 3 &&
            inputData.getIngredients().contains("tomato")
        ));
    }

    @Test
    public void testExecuteWithEmptyIngredients() {
        java.util.List<String> emptyList = java.util.Collections.emptyList();
        
        controller.execute(emptyList);
        
        verify(interactor, never()).execute(any());
    }

    @Test
    public void testExecuteWithNullInput() {
        controller.execute((java.util.List<String>) null);
        
        verify(interactor, never()).execute(any());
    }

    @Test
    public void testExecuteWithStringInput() throws Exception {
        String ingredientsRaw = "tomato, cheese, pasta";
        
        controller.execute(ingredientsRaw);
        
        Thread.sleep(100);
        
        verify(interactor, timeout(1000)).execute(any());
    }
}
