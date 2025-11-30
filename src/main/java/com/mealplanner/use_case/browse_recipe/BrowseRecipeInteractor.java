package com.mealplanner.use_case.browse_recipe;

// Main business logic for browsing recipe details and viewing ingredients.
// Responsible: Regina
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import com.mealplanner.config.ApiConfig;
import com.mealplanner.entity.Recipe;
import com.mealplanner.exception.RecipeNotFoundException;
import com.mealplanner.util.StringUtil;

/**
 * The Interactor class for the BrowseRecipe use case.
 */
public class BrowseRecipeInteractor implements BrowseRecipeInputBoundary {
    // get the recipe specifications from DataAccessInterface
    // instantiate the OutputData
    private final BrowseRecipeDataAccessInterface browseRecipeDataAccessObject;
    private final BrowseRecipeOutputBoundary browseRecipePresenter;

    public BrowseRecipeInteractor(BrowseRecipeDataAccessInterface browseRecipeDataAccessObject,
                                  BrowseRecipeOutputBoundary browseRecipePresenter) {
        this.browseRecipeDataAccessObject = Objects.requireNonNull(browseRecipeDataAccessObject, 
                "Data access object cannot be null");
        this.browseRecipePresenter = Objects.requireNonNull(browseRecipePresenter, 
                "Presenter cannot be null");
    }

    /**
     * Executes the BrowseRecipe use case.
     * @param browseRecipeInputData the input data carrying the search criteria
     * @throws IOException if the API fails
     */
    public void execute(BrowseRecipeInputData browseRecipeInputData) throws IOException {
        if (browseRecipeInputData == null) {
            browseRecipePresenter.presentError("Input data cannot be null");
            return;
        }

        if (StringUtil.isNullOrEmpty(browseRecipeInputData.getQuery())) {
            browseRecipePresenter.presentError("Search query cannot be empty");
            return;
        }

        // Check API configuration
        if (!ApiConfig.isSpoonacularConfigured()) {
            browseRecipePresenter.presentError("API key is not configured. Please check your configuration.");
            return;
        }

        try {
            // Delegate API call to data access layer
            final List<Recipe> recipes = browseRecipeDataAccessObject.searchRecipes(browseRecipeInputData);

            if (recipes == null || recipes.isEmpty()) {
                browseRecipePresenter.presentError("No recipes found, please try different wording "
                        + "in your search query"
                        + (browseRecipeInputData.getIncludedIngredients() != null
                        ? " or input different ingredients." : "."));
            }
            else {
                final BrowseRecipeOutputData browseRecipeOutputData = new BrowseRecipeOutputData(recipes);
                browseRecipePresenter.presentRecipeDetails(browseRecipeOutputData);
            }
        }
        catch (RecipeNotFoundException exception) {
            browseRecipePresenter.presentError("Recipe not found");
        }
        catch (IOException exception) {
            browseRecipePresenter.presentError("Network error: " + exception.getMessage());
        }
        catch (IllegalArgumentException exception) {
            browseRecipePresenter.presentError("Invalid input: " + exception.getMessage());
        }
    }
}
