package com.mealplanner.use_case.store_recipe;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mealplanner.entity.Recipe;
import com.mealplanner.exception.DataAccessException;
import com.mealplanner.repository.RecipeRepository;

/**
 * Test class for StoreRecipeInteractor.
 * Tests recipe storage, validation, and nutrition calculation.
 */
public class StoreRecipeInteractorTest {

    private StoreRecipeInteractor interactor;
    private TestPresenter presenter;
    private TestRepository repository;

    @BeforeEach
    public void setUp() {
        presenter = new TestPresenter();
        repository = new TestRepository();
        interactor = new StoreRecipeInteractor(presenter, repository);
    }

    @Test
    public void testStoreRecipeSuccess() {
        // Arrange
        StoreRecipeInputData inputData = new StoreRecipeInputData(
                "Test Recipe",
                Arrays.asList("Ingredient 1", "Ingredient 2"),
                Arrays.asList("Step 1", "Step 2"),
                4
        );

        // Act
        interactor.execute(inputData);

        // Assert
        assertTrue(presenter.successCalled, "Success should be called");
        assertFalse(presenter.errorCalled, "Error should not be called");
        assertNotNull(presenter.outputData);
        assertNotNull(presenter.outputData.getSavedRecipe());
        assertEquals("Test Recipe", presenter.outputData.getSavedRecipe().getName());
        assertTrue(repository.saveCalled);
    }

    @Test
    public void testStoreRecipeNullInput() {
        // Act
        interactor.execute(null);

        // Assert
        assertTrue(presenter.errorCalled, "Error should be called");
        assertEquals("Input data cannot be null", presenter.errorMessage);
        assertFalse(repository.saveCalled);
    }

    @Test
    public void testStoreRecipeNullName() {
        // Arrange
        StoreRecipeInputData inputData = new StoreRecipeInputData(
                null,
                Arrays.asList("Ingredient 1"),
                Arrays.asList("Step 1"),
                4
        );

        // Act
        interactor.execute(inputData);

        // Assert
        assertTrue(presenter.errorCalled, "Error should be called");
        assertEquals("Recipe name cannot be empty", presenter.errorMessage);
        assertFalse(repository.saveCalled);
    }

    @Test
    public void testStoreRecipeEmptyName() {
        // Arrange
        StoreRecipeInputData inputData = new StoreRecipeInputData(
                "   ",
                Arrays.asList("Ingredient 1"),
                Arrays.asList("Step 1"),
                4
        );

        // Act
        interactor.execute(inputData);

        // Assert
        assertTrue(presenter.errorCalled, "Error should be called");
        assertEquals("Recipe name cannot be empty", presenter.errorMessage);
        assertFalse(repository.saveCalled);
    }

    @Test
    public void testStoreRecipeNullIngredients() {
        // Arrange
        StoreRecipeInputData inputData = new StoreRecipeInputData(
                "Test Recipe",
                null,
                Arrays.asList("Step 1"),
                4
        );

        // Act
        interactor.execute(inputData);

        // Assert
        assertTrue(presenter.errorCalled, "Error should be called");
        assertEquals("Ingredients list cannot be empty", presenter.errorMessage);
        assertFalse(repository.saveCalled);
    }

    @Test
    public void testStoreRecipeEmptyIngredients() {
        // Arrange
        StoreRecipeInputData inputData = new StoreRecipeInputData(
                "Test Recipe",
                Collections.emptyList(),
                Arrays.asList("Step 1"),
                4
        );

        // Act
        interactor.execute(inputData);

        // Assert
        assertTrue(presenter.errorCalled, "Error should be called");
        assertEquals("Ingredients list cannot be empty", presenter.errorMessage);
        assertFalse(repository.saveCalled);
    }

    @Test
    public void testStoreRecipeNullSteps() {
        // Arrange
        StoreRecipeInputData inputData = new StoreRecipeInputData(
                "Test Recipe",
                Arrays.asList("Ingredient 1"),
                null,
                4
        );

        // Act
        interactor.execute(inputData);

        // Assert
        assertTrue(presenter.errorCalled, "Error should be called");
        assertEquals("Steps list cannot be empty", presenter.errorMessage);
        assertFalse(repository.saveCalled);
    }

    @Test
    public void testStoreRecipeEmptySteps() {
        // Arrange
        StoreRecipeInputData inputData = new StoreRecipeInputData(
                "Test Recipe",
                Arrays.asList("Ingredient 1"),
                Collections.emptyList(),
                4
        );

        // Act
        interactor.execute(inputData);

        // Assert
        assertTrue(presenter.errorCalled, "Error should be called");
        assertEquals("Steps list cannot be empty", presenter.errorMessage);
        assertFalse(repository.saveCalled);
    }

    @Test
    public void testStoreRecipeZeroServingSize() {
        // Arrange
        StoreRecipeInputData inputData = new StoreRecipeInputData(
                "Test Recipe",
                Arrays.asList("Ingredient 1"),
                Arrays.asList("Step 1"),
                0
        );

        // Act
        interactor.execute(inputData);

        // Assert
        assertTrue(presenter.errorCalled, "Error should be called");
        assertEquals("Serving size must be between 1 and 100", presenter.errorMessage);
        assertFalse(repository.saveCalled);
    }

    @Test
    public void testStoreRecipeNegativeServingSize() {
        // Arrange
        StoreRecipeInputData inputData = new StoreRecipeInputData(
                "Test Recipe",
                Arrays.asList("Ingredient 1"),
                Arrays.asList("Step 1"),
                -1
        );

        // Act
        interactor.execute(inputData);

        // Assert
        assertTrue(presenter.errorCalled, "Error should be called");
        assertEquals("Serving size must be between 1 and 100", presenter.errorMessage);
        assertFalse(repository.saveCalled);
    }

    @Test
    public void testDataAccessException() {
        // Arrange
        repository.shouldThrowException = true;
        StoreRecipeInputData inputData = new StoreRecipeInputData(
                "Test Recipe",
                Arrays.asList("Ingredient 1"),
                Arrays.asList("Step 1"),
                4
        );

        // Act
        interactor.execute(inputData);

        // Assert
        assertTrue(presenter.errorCalled, "Error should be called");
        assertTrue(presenter.errorMessage.contains("Failed to save recipe"));
        assertTrue(repository.saveCalled);
    }

    @Test
    public void testRuntimeException() {
        // Arrange
        repository.shouldThrowRuntimeException = true;
        StoreRecipeInputData inputData = new StoreRecipeInputData(
                "Test Recipe",
                Arrays.asList("Ingredient 1"),
                Arrays.asList("Step 1"),
                4
        );

        // Act
        interactor.execute(inputData);

        // Assert
        assertTrue(presenter.errorCalled, "Error should be called");
        assertTrue(presenter.errorMessage.contains("An error occurred while saving recipe"));
        assertTrue(repository.saveCalled);
    }

    @Test
    public void testRecipeIdGeneration() {
        // Arrange
        StoreRecipeInputData inputData = new StoreRecipeInputData(
                "Test Recipe",
                Arrays.asList("Ingredient 1"),
                Arrays.asList("Step 1"),
                4
        );

        // Act
        interactor.execute(inputData);

        // Assert
        assertNotNull(repository.savedRecipe);
        assertNotNull(repository.savedRecipe.getRecipeId());
        assertTrue(repository.savedRecipe.getRecipeId().startsWith("recipe-"));
    }

    @Test
    public void testStepsConvertedToString() {
        // Arrange
        List<String> stepsList = Arrays.asList("Step 1", "Step 2", "Step 3");
        StoreRecipeInputData inputData = new StoreRecipeInputData(
                "Test Recipe",
                Arrays.asList("Ingredient 1"),
                stepsList,
                4
        );

        // Act
        interactor.execute(inputData);

        // Assert
        assertNotNull(repository.savedRecipe);
        assertEquals("Step 1\nStep 2\nStep 3", repository.savedRecipe.getSteps());
    }

    // Test doubles
    private static class TestPresenter implements StoreRecipeOutputBoundary {
        boolean successCalled = false;
        boolean errorCalled = false;
        StoreRecipeOutputData outputData;
        String errorMessage;

        @Override
        public void presentSuccess(StoreRecipeOutputData outputData) {
            this.successCalled = true;
            this.outputData = outputData;
        }

        @Override
        public void presentError(String errorMessage) {
            this.errorCalled = true;
            this.errorMessage = errorMessage;
        }
    }

    private static class TestRepository implements RecipeRepository {
        boolean saveCalled = false;
        Recipe savedRecipe;
        boolean shouldThrowException = false;
        boolean shouldThrowRuntimeException = false;

        @Override
        public void save(Recipe recipe) throws DataAccessException {
            this.saveCalled = true;
            if (shouldThrowException) {
                throw new DataAccessException("Test exception");
            }
            if (shouldThrowRuntimeException) {
                throw new RuntimeException("Test runtime exception");
            }
            this.savedRecipe = recipe;
        }

        @Override
        public Optional<Recipe> findById(String id) {
            return Optional.empty();
        }

        @Override
        public List<Recipe> findAll() {
            return Collections.emptyList();
        }

        @Override
        public List<Recipe> findByName(String name) {
            return Collections.emptyList();
        }

        @Override
        public boolean delete(String id) {
            return false;
        }

        @Override
        public boolean exists(String id) {
            return false;
        }

        @Override
        public int count() {
            return 0;
        }

        @Override
        public void clear() {
        }
    }
}
