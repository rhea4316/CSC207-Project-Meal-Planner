package com.mealplanner.use_case.adjust_serving_size;

import com.mealplanner.entity.NutritionInfo;
import com.mealplanner.entity.Recipe;
import com.mealplanner.exception.RecipeNotFoundException;
import com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeDataAccessInterface;
import com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeInputData;
import com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeInteractor;
import com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeOutputBoundary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for AdjustServingSizeInteractor.
 * Tests serving size scaling and validation.
 *
 * Responsible: Eden (primary), Everyone (testing)
 */
public class AdjustServingSizeInteractorTest {

    private AdjustServingSizeInteractor interactor;

    @Mock
    private AdjustServingSizeDataAccessInterface dataAccess;

    @Mock
    private AdjustServingSizeOutputBoundary presenter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        interactor = new AdjustServingSizeInteractor(dataAccess, presenter);
    }

    @Test
    public void testServingSizeScaling_Double() throws RecipeNotFoundException {
        // Create test recipe: 2 servings, 200 calories
        Recipe originalRecipe = createTestRecipe("Test Recipe", 2, 200, 20.0, 30.0, 10.0);
        when(dataAccess.getRecipeById("recipe1")).thenReturn(originalRecipe);

        // Adjust to 4 servings (double)
        AdjustServingSizeInputData inputData = new AdjustServingSizeInputData("recipe1", 4);
        interactor.execute(inputData);

        // Verify presenter was called with adjusted recipe
        ArgumentCaptor<com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeOutputData> outputCaptor =
                ArgumentCaptor.forClass(com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeOutputData.class);
        verify(presenter).presentAdjustedRecipe(outputCaptor.capture());

        Recipe adjustedRecipe = outputCaptor.getValue().getAdjustedRecipe();
        assertEquals(4, adjustedRecipe.getServingSize());
        
        // Verify nutrition was scaled (doubled)
        NutritionInfo adjustedNutrition = adjustedRecipe.getNutritionInfo();
        assertNotNull(adjustedNutrition);
        assertEquals(400, adjustedNutrition.getCalories());
        assertEquals(40.0, adjustedNutrition.getProtein(), 0.01);
        assertEquals(60.0, adjustedNutrition.getCarbs(), 0.01);
        assertEquals(20.0, adjustedNutrition.getFat(), 0.01);
    }

    @Test
    public void testServingSizeScaling_Half() throws RecipeNotFoundException {
        // Create test recipe: 4 servings, 400 calories
        Recipe originalRecipe = createTestRecipe("Test Recipe", 4, 400, 40.0, 60.0, 20.0);
        when(dataAccess.getRecipeById("recipe1")).thenReturn(originalRecipe);

        // Adjust to 2 servings (half)
        AdjustServingSizeInputData inputData = new AdjustServingSizeInputData("recipe1", 2);
        interactor.execute(inputData);

        ArgumentCaptor<com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeOutputData> outputCaptor =
                ArgumentCaptor.forClass(com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeOutputData.class);
        verify(presenter).presentAdjustedRecipe(outputCaptor.capture());

        Recipe adjustedRecipe = outputCaptor.getValue().getAdjustedRecipe();
        assertEquals(2, adjustedRecipe.getServingSize());
        
        // Verify nutrition was scaled (halved)
        NutritionInfo adjustedNutrition = adjustedRecipe.getNutritionInfo();
        assertNotNull(adjustedNutrition);
        assertEquals(200, adjustedNutrition.getCalories());
        assertEquals(20.0, adjustedNutrition.getProtein(), 0.01);
        assertEquals(30.0, adjustedNutrition.getCarbs(), 0.01);
        assertEquals(10.0, adjustedNutrition.getFat(), 0.01);
    }

    @Test
    public void testServingSizeScaling_SameSize() throws RecipeNotFoundException {
        // Create test recipe: 3 servings
        Recipe originalRecipe = createTestRecipe("Test Recipe", 3, 300, 30.0, 45.0, 15.0);
        when(dataAccess.getRecipeById("recipe1")).thenReturn(originalRecipe);

        // Adjust to same size (3 servings)
        AdjustServingSizeInputData inputData = new AdjustServingSizeInputData("recipe1", 3);
        interactor.execute(inputData);

        ArgumentCaptor<com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeOutputData> outputCaptor =
                ArgumentCaptor.forClass(com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeOutputData.class);
        verify(presenter).presentAdjustedRecipe(outputCaptor.capture());

        Recipe adjustedRecipe = outputCaptor.getValue().getAdjustedRecipe();
        assertEquals(3, adjustedRecipe.getServingSize());
        
        // Verify nutrition remains the same
        NutritionInfo adjustedNutrition = adjustedRecipe.getNutritionInfo();
        assertNotNull(adjustedNutrition);
        assertEquals(300, adjustedNutrition.getCalories());
        assertEquals(30.0, adjustedNutrition.getProtein(), 0.01);
    }

    @Test
    public void testInvalidServingSize_Zero() throws RecipeNotFoundException {
        AdjustServingSizeInputData inputData = new AdjustServingSizeInputData("recipe1", 0);
        interactor.execute(inputData);

        verify(presenter).presentError("Serving size must be greater than zero");
        verify(presenter, never()).presentAdjustedRecipe(any());
        verify(dataAccess, never()).getRecipeById(anyString());
    }

    @Test
    public void testInvalidServingSize_Negative() throws RecipeNotFoundException {
        AdjustServingSizeInputData inputData = new AdjustServingSizeInputData("recipe1", -1);
        interactor.execute(inputData);

        verify(presenter).presentError("Serving size must be greater than zero");
        verify(presenter, never()).presentAdjustedRecipe(any());
        verify(dataAccess, never()).getRecipeById(anyString());
    }

    @Test
    public void testInvalidRecipeId_Null() throws RecipeNotFoundException {
        AdjustServingSizeInputData inputData = new AdjustServingSizeInputData(null, 4);
        interactor.execute(inputData);

        verify(presenter).presentError("Recipe ID cannot be empty");
        verify(presenter, never()).presentAdjustedRecipe(any());
        verify(dataAccess, never()).getRecipeById(anyString());
    }

    @Test
    public void testInvalidRecipeId_Empty() throws RecipeNotFoundException {
        AdjustServingSizeInputData inputData = new AdjustServingSizeInputData("", 4);
        interactor.execute(inputData);

        verify(presenter).presentError("Recipe ID cannot be empty");
        verify(presenter, never()).presentAdjustedRecipe(any());
        verify(dataAccess, never()).getRecipeById(anyString());
    }

    @Test
    public void testInvalidRecipeId_Whitespace() throws RecipeNotFoundException {
        AdjustServingSizeInputData inputData = new AdjustServingSizeInputData("   ", 4);
        interactor.execute(inputData);

        verify(presenter).presentError("Recipe ID cannot be empty");
        verify(presenter, never()).presentAdjustedRecipe(any());
        verify(dataAccess, never()).getRecipeById(anyString());
    }

    @Test
    public void testRecipeNotFound() throws RecipeNotFoundException {
        when(dataAccess.getRecipeById("nonexistent")).thenThrow(new RecipeNotFoundException("nonexistent"));

        AdjustServingSizeInputData inputData = new AdjustServingSizeInputData("nonexistent", 4);
        interactor.execute(inputData);

        verify(presenter).presentError(contains("Recipe not found"));
        verify(presenter, never()).presentAdjustedRecipe(any());
    }

    @Test
    public void testRecipeWithNullNutrition() throws RecipeNotFoundException {
        // Recipe without nutrition info
        List<String> ingredients = new ArrayList<>();
        ingredients.add("Ingredient 1");
        Recipe recipe = new Recipe("Test Recipe", ingredients, "Steps", 2, null, null, null, "recipe1");
        when(dataAccess.getRecipeById("recipe1")).thenReturn(recipe);

        AdjustServingSizeInputData inputData = new AdjustServingSizeInputData("recipe1", 4);
        interactor.execute(inputData);

        ArgumentCaptor<com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeOutputData> outputCaptor =
                ArgumentCaptor.forClass(com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeOutputData.class);
        verify(presenter).presentAdjustedRecipe(outputCaptor.capture());

        Recipe adjustedRecipe = outputCaptor.getValue().getAdjustedRecipe();
        assertEquals(4, adjustedRecipe.getServingSize());
        assertNull(adjustedRecipe.getNutritionInfo());
    }

    @Test
    public void testNullInputData() {
        interactor.execute(null);

        verify(presenter).presentError("Input data cannot be null");
        verify(presenter, never()).presentAdjustedRecipe(any());
        verify(dataAccess, never()).getRecipeById(anyString());
    }

    @Test
    public void testIllegalArgumentException() throws RecipeNotFoundException {
        // dataAccess.getRecipeById() throws IllegalArgumentException
        when(dataAccess.getRecipeById("recipe1")).thenThrow(new IllegalArgumentException("Invalid recipe data"));

        AdjustServingSizeInputData inputData = new AdjustServingSizeInputData("recipe1", 4);
        interactor.execute(inputData);

        verify(presenter).presentError(contains("Invalid serving size"));
        verify(presenter, never()).presentAdjustedRecipe(any());
    }

    @Test
    public void testGeneralException() throws RecipeNotFoundException {
        when(dataAccess.getRecipeById("recipe1")).thenThrow(new RuntimeException("Database connection error"));

        AdjustServingSizeInputData inputData = new AdjustServingSizeInputData("recipe1", 4);
        interactor.execute(inputData);

        verify(presenter).presentError(contains("An error occurred while adjusting serving size"));
        verify(presenter, never()).presentAdjustedRecipe(any());
    }

    @Test
    public void testGeneralExceptionWithNullMessage() throws RecipeNotFoundException {
        // Exception with null message to test e.getMessage() == null case
        RuntimeException exception = new RuntimeException();
        when(dataAccess.getRecipeById("recipe1")).thenThrow(exception);

        AdjustServingSizeInputData inputData = new AdjustServingSizeInputData("recipe1", 4);
        interactor.execute(inputData);

        verify(presenter).presentError(contains("An error occurred while adjusting serving size"));
        verify(presenter, never()).presentAdjustedRecipe(any());
    }

    @Test
    public void testConstructorWithNullDataAccess() {
        assertThrows(NullPointerException.class, () -> {
            new AdjustServingSizeInteractor(null, presenter);
        });
    }

    @Test
    public void testConstructorWithNullPresenter() {
        assertThrows(NullPointerException.class, () -> {
            new AdjustServingSizeInteractor(dataAccess, null);
        });
    }

    // Helper method to create test recipe
    private Recipe createTestRecipe(String name, int servingSize, int calories, 
                                   double protein, double carbs, double fat) {
        List<String> ingredients = new ArrayList<>();
        ingredients.add("Ingredient 1");
        ingredients.add("Ingredient 2");
        NutritionInfo nutrition = new NutritionInfo(calories, protein, carbs, fat);
        return new Recipe(name, ingredients, "Cooking steps", servingSize, nutrition, null, null, "recipe1");
    }
}
