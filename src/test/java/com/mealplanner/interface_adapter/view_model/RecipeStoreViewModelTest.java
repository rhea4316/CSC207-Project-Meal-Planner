package com.mealplanner.interface_adapter.view_model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for RecipeStoreViewModel.
 */
public class RecipeStoreViewModelTest {

    private RecipeStoreViewModel viewModel;

    @BeforeEach
    public void setUp() {
        viewModel = new RecipeStoreViewModel();
    }

    @Test
    public void testInitialState() {
        // Assert
        assertEquals("", viewModel.getName());
        assertTrue(viewModel.getIngredients().isEmpty());
        assertTrue(viewModel.getSteps().isEmpty());
        assertEquals(1, viewModel.getServingSize());
        assertNull(viewModel.getSuccessMessage());
        assertNull(viewModel.getErrorMessage());
    }

    @Test
    public void testSetName() {
        // Arrange
        TestPropertyChangeListener listener = new TestPropertyChangeListener();
        viewModel.addPropertyChangeListener(listener);

        // Act
        viewModel.setName("Test Recipe");

        // Assert
        assertEquals("Test Recipe", viewModel.getName());
        assertTrue(listener.eventFired);
        assertEquals(RecipeStoreViewModel.PROP_NAME, listener.lastEvent.getPropertyName());
        assertEquals("", listener.lastEvent.getOldValue());
        assertEquals("Test Recipe", listener.lastEvent.getNewValue());
    }

    @Test
    public void testSetNameWithNull() {
        // Act
        viewModel.setName(null);

        // Assert
        assertEquals("", viewModel.getName());
    }

    @Test
    public void testSetIngredients() {
        // Arrange
        TestPropertyChangeListener listener = new TestPropertyChangeListener();
        viewModel.addPropertyChangeListener(listener);
        List<String> ingredients = Arrays.asList("Ingredient 1", "Ingredient 2");

        // Act
        viewModel.setIngredients(ingredients);

        // Assert
        assertEquals(ingredients, viewModel.getIngredients());
        assertTrue(listener.eventFired);
        assertEquals(RecipeStoreViewModel.PROP_INGREDIENTS, listener.lastEvent.getPropertyName());
    }

    @Test
    public void testSetIngredientsWithNull() {
        // Act
        viewModel.setIngredients(null);

        // Assert
        assertTrue(viewModel.getIngredients().isEmpty());
    }

    @Test
    public void testGetIngredientsReturnsDefensiveCopy() {
        // Arrange
        List<String> ingredients = Arrays.asList("Ingredient 1");
        viewModel.setIngredients(ingredients);

        // Act
        List<String> retrieved = viewModel.getIngredients();
        retrieved.add("Ingredient 2");

        // Assert
        assertEquals(1, viewModel.getIngredients().size());
    }

    @Test
    public void testSetSteps() {
        // Arrange
        TestPropertyChangeListener listener = new TestPropertyChangeListener();
        viewModel.addPropertyChangeListener(listener);
        List<String> steps = Arrays.asList("Step 1", "Step 2");

        // Act
        viewModel.setSteps(steps);

        // Assert
        assertEquals(steps, viewModel.getSteps());
        assertTrue(listener.eventFired);
        assertEquals(RecipeStoreViewModel.PROP_STEPS, listener.lastEvent.getPropertyName());
    }

    @Test
    public void testSetStepsWithNull() {
        // Act
        viewModel.setSteps(null);

        // Assert
        assertTrue(viewModel.getSteps().isEmpty());
    }

    @Test
    public void testGetStepsReturnsDefensiveCopy() {
        // Arrange
        List<String> steps = Arrays.asList("Step 1");
        viewModel.setSteps(steps);

        // Act
        List<String> retrieved = viewModel.getSteps();
        retrieved.add("Step 2");

        // Assert
        assertEquals(1, viewModel.getSteps().size());
    }

    @Test
    public void testSetServingSize() {
        // Arrange
        TestPropertyChangeListener listener = new TestPropertyChangeListener();
        viewModel.addPropertyChangeListener(listener);

        // Act
        viewModel.setServingSize(4);

        // Assert
        assertEquals(4, viewModel.getServingSize());
        assertTrue(listener.eventFired);
        assertEquals(RecipeStoreViewModel.PROP_SERVING_SIZE, listener.lastEvent.getPropertyName());
        assertEquals(1, listener.lastEvent.getOldValue());
        assertEquals(4, listener.lastEvent.getNewValue());
    }

    @Test
    public void testSetServingSizeWithZeroDefaultsToOne() {
        // Act
        viewModel.setServingSize(0);

        // Assert
        assertEquals(1, viewModel.getServingSize());
    }

    @Test
    public void testSetServingSizeWithNegativeDefaultsToOne() {
        // Act
        viewModel.setServingSize(-5);

        // Assert
        assertEquals(1, viewModel.getServingSize());
    }

    @Test
    public void testSetSuccessMessage() {
        // Arrange
        TestPropertyChangeListener listener = new TestPropertyChangeListener();
        viewModel.addPropertyChangeListener(listener);

        // Act
        viewModel.setSuccessMessage("Recipe saved successfully");

        // Assert
        assertEquals("Recipe saved successfully", viewModel.getSuccessMessage());
        assertTrue(listener.eventFired);
        assertEquals(RecipeStoreViewModel.PROP_SUCCESS_MESSAGE, listener.lastEvent.getPropertyName());
    }

    @Test
    public void testSetErrorMessage() {
        // Arrange
        TestPropertyChangeListener listener = new TestPropertyChangeListener();
        viewModel.addPropertyChangeListener(listener);

        // Act
        viewModel.setErrorMessage("Error occurred");

        // Assert
        assertEquals("Error occurred", viewModel.getErrorMessage());
        assertTrue(listener.eventFired);
        assertEquals(RecipeStoreViewModel.PROP_ERROR_MESSAGE, listener.lastEvent.getPropertyName());
    }

    @Test
    public void testAddAndRemovePropertyChangeListener() {
        // Arrange
        TestPropertyChangeListener listener = new TestPropertyChangeListener();
        viewModel.addPropertyChangeListener(listener);

        // Act
        viewModel.setName("Test");
        assertTrue(listener.eventFired);

        listener.reset();
        viewModel.removePropertyChangeListener(listener);
        viewModel.setName("Test 2");

        // Assert
        assertFalse(listener.eventFired);
    }

    @Test
    public void testMultiplePropertyChanges() {
        // Arrange
        TestPropertyChangeListener listener = new TestPropertyChangeListener();
        viewModel.addPropertyChangeListener(listener);

        // Act & Assert
        viewModel.setName("Recipe");
        assertEquals(RecipeStoreViewModel.PROP_NAME, listener.lastEvent.getPropertyName());

        viewModel.setServingSize(6);
        assertEquals(RecipeStoreViewModel.PROP_SERVING_SIZE, listener.lastEvent.getPropertyName());

        viewModel.setSuccessMessage("Success");
        assertEquals(RecipeStoreViewModel.PROP_SUCCESS_MESSAGE, listener.lastEvent.getPropertyName());

        viewModel.setErrorMessage("Error");
        assertEquals(RecipeStoreViewModel.PROP_ERROR_MESSAGE, listener.lastEvent.getPropertyName());
    }

    @Test
    public void testPropertyConstants() {
        // Assert that constants are defined correctly
        assertEquals("name", RecipeStoreViewModel.PROP_NAME);
        assertEquals("ingredients", RecipeStoreViewModel.PROP_INGREDIENTS);
        assertEquals("steps", RecipeStoreViewModel.PROP_STEPS);
        assertEquals("servingSize", RecipeStoreViewModel.PROP_SERVING_SIZE);
        assertEquals("successMessage", RecipeStoreViewModel.PROP_SUCCESS_MESSAGE);
        assertEquals("errorMessage", RecipeStoreViewModel.PROP_ERROR_MESSAGE);
    }

    // Test helper class
    private static class TestPropertyChangeListener implements PropertyChangeListener {
        boolean eventFired = false;
        PropertyChangeEvent lastEvent;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            this.eventFired = true;
            this.lastEvent = evt;
        }

        void reset() {
            this.eventFired = false;
            this.lastEvent = null;
        }
    }
}
