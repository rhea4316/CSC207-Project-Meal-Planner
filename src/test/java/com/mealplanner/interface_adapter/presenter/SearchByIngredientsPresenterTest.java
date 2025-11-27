package com.mealplanner.interface_adapter.presenter;

import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.view_model.RecipeSearchViewModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

/**
 * Test class for SearchByIngredientsPresenter.
 * Tests formatting and presentation of search results.
 *
 * Responsible: Jerry (primary)
 */
public class SearchByIngredientsPresenterTest {

    private SearchByIngredientsPresenter presenter;

    @Mock
    private RecipeSearchViewModel viewModel;

    @Mock
    private ViewManagerModel viewManagerModel;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        presenter = new SearchByIngredientsPresenter(viewModel, viewManagerModel);
    }

    @Test
    public void testPresentSuccess() {
        com.mealplanner.entity.Recipe recipe = new com.mealplanner.entity.Recipe(
            "Pasta", java.util.Arrays.asList("pasta", "sauce"), "Cook pasta", 2, null, null, null, "recipe-1");
        java.util.List<com.mealplanner.entity.Recipe> recipes = java.util.Arrays.asList(recipe);
        com.mealplanner.use_case.search_by_ingredients.SearchByIngredientsOutputData outputData = 
            new com.mealplanner.use_case.search_by_ingredients.SearchByIngredientsOutputData(recipes);
        
        presenter.presentRecipes(outputData);
        
        verify(viewModel).setRecipes(recipes);
        verify(viewModel).setErrorMessage("");
        verify(viewModel).setLoading(false);
        verify(viewManagerModel).setActiveView("SearchByIngredientsView");
    }

    @Test
    public void testPresentError() {
        String errorMessage = "Search failed";
        
        presenter.presentError(errorMessage);
        
        verify(viewModel).setErrorMessage(errorMessage);
        verify(viewModel).setLoading(false);
    }

    @Test
    public void testPresentEmptyResults() {
        com.mealplanner.use_case.search_by_ingredients.SearchByIngredientsOutputData outputData = 
            new com.mealplanner.use_case.search_by_ingredients.SearchByIngredientsOutputData(java.util.Collections.emptyList());
        
        presenter.presentRecipes(outputData);
        
        verify(viewModel).setErrorMessage("No recipes found matching the provided ingredients");
        verify(viewModel).setLoading(false);
    }

    @Test
    public void testFormatRecipeList() {
        com.mealplanner.entity.NutritionInfo nutrition = new com.mealplanner.entity.NutritionInfo(500, 20.0, 60.0, 15.0);
        com.mealplanner.entity.Recipe recipe = new com.mealplanner.entity.Recipe(
            "Pasta", java.util.Arrays.asList("pasta", "sauce"), "Cook pasta", 2, nutrition, null, null, "recipe-1");
        java.util.List<com.mealplanner.entity.Recipe> recipes = java.util.Arrays.asList(recipe);
        com.mealplanner.use_case.search_by_ingredients.SearchByIngredientsOutputData outputData = 
            new com.mealplanner.use_case.search_by_ingredients.SearchByIngredientsOutputData(recipes);
        
        presenter.presentRecipes(outputData);
        
        verify(viewModel).setRecipes(recipes);
    }
}
