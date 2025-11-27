package com.mealplanner.interface_adapter.presenter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mealplanner.entity.Recipe;
import com.mealplanner.interface_adapter.view_model.RecipeStoreViewModel;
import com.mealplanner.use_case.store_recipe.StoreRecipeOutputData;

/**
 * Test class for StoreRecipePresenter.
 */
public class StoreRecipePresenterTest {

    private StoreRecipePresenter presenter;
    private RecipeStoreViewModel viewModel;

    @BeforeEach
    public void setUp() {
        viewModel = new RecipeStoreViewModel();
        presenter = new StoreRecipePresenter(viewModel);
    }

    @Test
    public void testPresentSuccessWithValidRecipe() {
        // Arrange
        Recipe recipe = new Recipe(
                "Test Recipe",
                Arrays.asList("Ingredient 1", "Ingredient 2"),
                "Step 1\nStep 2\nStep 3",
                4
        );
        StoreRecipeOutputData outputData = new StoreRecipeOutputData(recipe);

        // Act
        presenter.presentSuccess(outputData);

        // Assert
        assertNotNull(viewModel.getSuccessMessage());
        assertTrue(viewModel.getSuccessMessage().contains("Test Recipe"));
        assertTrue(viewModel.getSuccessMessage().contains("serves 4"));
        assertTrue(viewModel.getSuccessMessage().contains("2 ingredients"));
        assertTrue(viewModel.getSuccessMessage().contains("3 step(s)"));
        assertNull(viewModel.getErrorMessage());
    }

    @Test
    public void testPresentSuccessWithSingleStep() {
        // Arrange
        Recipe recipe = new Recipe(
                "Simple Recipe",
                Arrays.asList("Ingredient 1"),
                "Single step",
                2
        );
        StoreRecipeOutputData outputData = new StoreRecipeOutputData(recipe);

        // Act
        presenter.presentSuccess(outputData);

        // Assert
        assertNotNull(viewModel.getSuccessMessage());
        assertTrue(viewModel.getSuccessMessage().contains("1 ingredients"));
        assertTrue(viewModel.getSuccessMessage().contains("1 step(s)"));
    }

    @Test
    public void testPresentSuccessWithNullOutputData() {
        // Act
        presenter.presentSuccess(null);

        // Assert
        assertEquals("Recipe saved.", viewModel.getSuccessMessage());
    }

    @Test
    public void testPresentSuccessWithNullRecipe() {
        // Arrange
        StoreRecipeOutputData outputData = new StoreRecipeOutputData(null);

        // Act
        presenter.presentSuccess(outputData);

        // Assert
        assertEquals("Recipe saved.", viewModel.getSuccessMessage());
    }

    @Test
    public void testPresentSuccessWithNullViewModelLogsToConsole() {
        // Arrange
        StoreRecipePresenter presenterWithoutViewModel = new StoreRecipePresenter(null);
        Recipe recipe = new Recipe(
                "Test Recipe",
                Arrays.asList("Ingredient 1"),
                "Step 1",
                2
        );
        StoreRecipeOutputData outputData = new StoreRecipeOutputData(recipe);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // Act
        presenterWithoutViewModel.presentSuccess(outputData);

        // Assert
        System.setOut(originalOut);
        String output = outContent.toString();
        assertTrue(output.contains("Saved recipe 'Test Recipe'"));
        assertTrue(output.contains("serves 2"));
    }

    @Test
    public void testPresentSuccessWithNullViewModelAndNullOutputData() {
        // Arrange
        StoreRecipePresenter presenterWithoutViewModel = new StoreRecipePresenter(null);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // Act
        presenterWithoutViewModel.presentSuccess(null);

        // Assert
        System.setOut(originalOut);
        String output = outContent.toString();
        assertTrue(output.contains("Recipe saved (no details provided)"));
    }

    @Test
    public void testPresentSuccessWithNullViewModelAndNullRecipe() {
        // Arrange
        StoreRecipePresenter presenterWithoutViewModel = new StoreRecipePresenter(null);
        StoreRecipeOutputData outputData = new StoreRecipeOutputData(null);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // Act
        presenterWithoutViewModel.presentSuccess(outputData);

        // Assert
        System.setOut(originalOut);
        String output = outContent.toString();
        assertTrue(output.contains("Recipe saved (no details provided)"));
    }

    @Test
    public void testPresentErrorWithMessage() {
        // Act
        presenter.presentError("Test error message");

        // Assert
        assertEquals("Test error message", viewModel.getErrorMessage());
        assertNull(viewModel.getSuccessMessage());
    }

    @Test
    public void testPresentErrorWithNullMessage() {
        // Act
        presenter.presentError(null);

        // Assert
        assertEquals("Failed to save recipe", viewModel.getErrorMessage());
    }

    @Test
    public void testPresentErrorWithNullViewModelLogsToConsole() {
        // Arrange
        StoreRecipePresenter presenterWithoutViewModel = new StoreRecipePresenter(null);

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        // Act
        presenterWithoutViewModel.presentError("Test error");

        // Assert
        System.setErr(originalErr);
        String output = errContent.toString();
        assertTrue(output.contains("Failed to save recipe: Test error"));
    }

    @Test
    public void testPresentErrorWithNullViewModelAndNullMessage() {
        // Arrange
        StoreRecipePresenter presenterWithoutViewModel = new StoreRecipePresenter(null);

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        // Act
        presenterWithoutViewModel.presentError(null);

        // Assert
        System.setErr(originalErr);
        String output = errContent.toString();
        assertTrue(output.contains("Failed to save recipe: Unknown error"));
    }

    @Test
    public void testPresentSuccessWithMultilineSteps() {
        // Arrange
        Recipe recipe = new Recipe(
                "Complex Recipe",
                Arrays.asList("Ing1", "Ing2", "Ing3"),
                "Step 1\nStep 2\nStep 3\nStep 4\nStep 5",
                6
        );
        StoreRecipeOutputData outputData = new StoreRecipeOutputData(recipe);

        // Act
        presenter.presentSuccess(outputData);

        // Assert
        assertTrue(viewModel.getSuccessMessage().contains("5 step(s)"));
        assertTrue(viewModel.getSuccessMessage().contains("3 ingredients"));
    }
}
