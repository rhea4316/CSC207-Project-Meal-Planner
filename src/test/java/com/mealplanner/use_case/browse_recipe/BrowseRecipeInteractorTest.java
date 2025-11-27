package com.mealplanner.use_case.browse_recipe;

import com.mealplanner.config.ApiConfig;
import com.mealplanner.entity.Recipe;
import com.mealplanner.exception.RecipeNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test class for BrowseRecipeInteractor.
 * Tests recipe browsing functionality and ingredient display.
 *
 * Responsible: Regina (primary)
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
    public void testBrowseRecipesSuccess() throws IOException {
        // Arrange
        String query = "pasta";
        BrowseRecipeInputData inputData = new BrowseRecipeInputData(query, 5);
        
        List<Recipe> recipes = Arrays.asList(
            new Recipe("Spaghetti", Arrays.asList("pasta", "sauce"), "Cook pasta", 2),
            new Recipe("Lasagna", Arrays.asList("pasta", "cheese"), "Layer and bake", 4)
        );
        
        when(dataAccess.searchRecipes(inputData)).thenReturn(recipes);
        
        try (MockedStatic<ApiConfig> apiConfigMock = mockStatic(ApiConfig.class)) {
            apiConfigMock.when(ApiConfig::isSpoonacularConfigured).thenReturn(true);
            
            // Act
            interactor.execute(inputData);
            
            // Assert
            verify(dataAccess).searchRecipes(inputData);
            verify(presenter).presentRecipeDetails(argThat(outputData -> 
                outputData.getRecipes().size() == 2 &&
                outputData.getRecipes().equals(recipes)
            ));
            verify(presenter, never()).presentError(anyString());
        }
    }

    @Test
    public void testBrowseRecipesEmpty() throws IOException {
        // Arrange
        String query = "nonexistent";
        BrowseRecipeInputData inputData = new BrowseRecipeInputData(query, 5);
        
        when(dataAccess.searchRecipes(inputData)).thenReturn(Collections.emptyList());
        
        try (MockedStatic<ApiConfig> apiConfigMock = mockStatic(ApiConfig.class)) {
            apiConfigMock.when(ApiConfig::isSpoonacularConfigured).thenReturn(true);
            
            // Act
            interactor.execute(inputData);
            
            // Assert
            verify(dataAccess).searchRecipes(inputData);
            verify(presenter).presentError(contains("No recipes found"));
            verify(presenter, never()).presentRecipeDetails(any());
        }
    }

    @Test
    public void testBrowseRecipesNullResult() throws IOException {
        // Arrange
        String query = "pasta";
        BrowseRecipeInputData inputData = new BrowseRecipeInputData(query, 5);
        
        when(dataAccess.searchRecipes(inputData)).thenReturn(null);
        
        try (MockedStatic<ApiConfig> apiConfigMock = mockStatic(ApiConfig.class)) {
            apiConfigMock.when(ApiConfig::isSpoonacularConfigured).thenReturn(true);
            
            // Act
            interactor.execute(inputData);
            
            // Assert
            verify(dataAccess).searchRecipes(inputData);
            verify(presenter).presentError(contains("No recipes found"));
            verify(presenter, never()).presentRecipeDetails(any());
        }
    }

    @Test
    public void testBrowseRecipesWithIngredients() throws IOException {
        // Arrange
        String query = "pasta";
        String ingredients = "tomato, cheese";
        BrowseRecipeInputData inputData = new BrowseRecipeInputData(query, 5, ingredients);
        
        List<Recipe> recipes = Arrays.asList(
            new Recipe("Pasta", Arrays.asList("pasta", "tomato", "cheese"), "Cook", 2)
        );
        
        when(dataAccess.searchRecipes(inputData)).thenReturn(recipes);
        
        try (MockedStatic<ApiConfig> apiConfigMock = mockStatic(ApiConfig.class)) {
            apiConfigMock.when(ApiConfig::isSpoonacularConfigured).thenReturn(true);
            
            // Act
            interactor.execute(inputData);
            
            // Assert
            verify(dataAccess).searchRecipes(inputData);
            verify(presenter).presentRecipeDetails(any());
        }
    }

    @Test
    public void testBrowseRecipesNullInput() throws IOException {
        // Act
        interactor.execute(null);
        
        // Assert
        verify(dataAccess, never()).searchRecipes(any());
        verify(presenter).presentError("Input data cannot be null");
        verify(presenter, never()).presentRecipeDetails(any());
    }

    @Test
    public void testBrowseRecipesEmptyQuery() throws IOException {
        // Arrange
        BrowseRecipeInputData inputData = new BrowseRecipeInputData("", 5);
        
        // Act
        interactor.execute(inputData);
        
        // Assert
        verify(dataAccess, never()).searchRecipes(any());
        verify(presenter).presentError("Search query cannot be empty");
        verify(presenter, never()).presentRecipeDetails(any());
    }

    @Test
    public void testBrowseRecipesNullQuery() throws IOException {
        // Arrange
        BrowseRecipeInputData inputData = new BrowseRecipeInputData(null, 5);
        
        // Act
        interactor.execute(inputData);
        
        // Assert
        verify(dataAccess, never()).searchRecipes(any());
        verify(presenter).presentError("Search query cannot be empty");
        verify(presenter, never()).presentRecipeDetails(any());
    }

    @Test
    public void testBrowseRecipesWhitespaceQuery() throws IOException {
        // Arrange
        BrowseRecipeInputData inputData = new BrowseRecipeInputData("   ", 5);
        
        // Act
        interactor.execute(inputData);
        
        // Assert
        verify(dataAccess, never()).searchRecipes(any());
        verify(presenter).presentError("Search query cannot be empty");
        verify(presenter, never()).presentRecipeDetails(any());
    }

    @Test
    public void testRecipeNotFound() throws IOException {
        // Arrange
        String query = "pasta";
        BrowseRecipeInputData inputData = new BrowseRecipeInputData(query, 5);
        
        when(dataAccess.searchRecipes(inputData)).thenThrow(new RecipeNotFoundException("Recipe not found"));
        
        try (MockedStatic<ApiConfig> apiConfigMock = mockStatic(ApiConfig.class)) {
            apiConfigMock.when(ApiConfig::isSpoonacularConfigured).thenReturn(true);
            
            // Act
            interactor.execute(inputData);
            
            // Assert
            verify(dataAccess).searchRecipes(inputData);
            verify(presenter).presentError("Recipe not found");
            verify(presenter, never()).presentRecipeDetails(any());
        }
    }

    @Test
    public void testApiFailure() throws IOException {
        // Arrange
        String query = "pasta";
        BrowseRecipeInputData inputData = new BrowseRecipeInputData(query, 5);
        
        when(dataAccess.searchRecipes(inputData)).thenThrow(new IOException("Network error"));
        
        try (MockedStatic<ApiConfig> apiConfigMock = mockStatic(ApiConfig.class)) {
            apiConfigMock.when(ApiConfig::isSpoonacularConfigured).thenReturn(true);
            
            // Act
            interactor.execute(inputData);
            
            // Assert
            verify(dataAccess).searchRecipes(inputData);
            verify(presenter).presentError("Network error: Network error");
            verify(presenter, never()).presentRecipeDetails(any());
        }
    }

    @Test
    public void testApiNotConfigured() throws IOException {
        // Arrange
        String query = "pasta";
        BrowseRecipeInputData inputData = new BrowseRecipeInputData(query, 5);
        
        try (MockedStatic<ApiConfig> apiConfigMock = mockStatic(ApiConfig.class)) {
            apiConfigMock.when(ApiConfig::isSpoonacularConfigured).thenReturn(false);
            
            // Act
            interactor.execute(inputData);
            
            // Assert
            verify(dataAccess, never()).searchRecipes(any());
            verify(presenter).presentError("API key is not configured. Please check your configuration.");
            verify(presenter, never()).presentRecipeDetails(any());
        }
    }

    @Test
    public void testInvalidInput() throws IOException {
        // Arrange
        String query = "pasta";
        BrowseRecipeInputData inputData = new BrowseRecipeInputData(query, 5);
        
        when(dataAccess.searchRecipes(inputData)).thenThrow(new IllegalArgumentException("Invalid input"));
        
        try (MockedStatic<ApiConfig> apiConfigMock = mockStatic(ApiConfig.class)) {
            apiConfigMock.when(ApiConfig::isSpoonacularConfigured).thenReturn(true);
            
            // Act
            interactor.execute(inputData);
            
            // Assert
            verify(dataAccess).searchRecipes(inputData);
            verify(presenter).presentError("Invalid input: Invalid input");
            verify(presenter, never()).presentRecipeDetails(any());
        }
    }
}
