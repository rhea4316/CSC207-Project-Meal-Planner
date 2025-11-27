package com.mealplanner.app;

// Builder class for assembling all application components and wiring dependencies.
// Responsible: Everyone

import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.*;
import com.mealplanner.interface_adapter.presenter.*;
import com.mealplanner.interface_adapter.view_model.*;
import com.mealplanner.repository.impl.FileRecipeRepository;
import com.mealplanner.view.*;

public class AppBuilder {
    private ViewManager viewManager;
    private ViewManagerModel viewManagerModel;

    public AppBuilder() {
        this.viewManagerModel = new ViewManagerModel();
        this.viewManager = new ViewManager(viewManagerModel);
    }

    /**
     * Builds the complete application with all components wired together.
     */
    public ViewManager build() {
        buildStoreRecipeFlow();
        buildBrowseRecipeFlow();
        buildSearchByIngredientsFlow();
        buildAdjustServingSizeFlow();
        buildScheduleFlow();
        
        // Set initial view - use ViewManager's switchToView to ensure proper display
        viewManager.switchToView(ViewManager.STORE_RECIPE_VIEW);
        
        return viewManager;
    }

    /**
     * Builds the StoreRecipe flow.
     */
    private void buildStoreRecipeFlow() {
        // 1. ViewModel
        RecipeStoreViewModel viewModel = new RecipeStoreViewModel();

        // 2. Presenter
        StoreRecipePresenter presenter = new StoreRecipePresenter(viewModel);

        // 3. Repository
        FileRecipeRepository repository = new FileRecipeRepository();

        // 4. Interactor (UseCaseFactory 사용)
        var interactor = UseCaseFactory.createStoreRecipeInteractor(presenter, repository);

        // 5. Controller
        StoreRecipeController controller = new StoreRecipeController(interactor);

        // 6. View (ViewManagerModel 전달)
        StoreRecipeView view = new StoreRecipeView(controller, viewModel, viewManagerModel);

        // 7. ViewManager에 등록
        viewManager.addView(ViewManager.STORE_RECIPE_VIEW, view);
    }

    /**
     * Builds the BrowseRecipe flow.
     */
    private void buildBrowseRecipeFlow() {
        // 1. ViewModel
        RecipeBrowseViewModel viewModel = new RecipeBrowseViewModel();

        // 2. Presenter (ViewManagerModel 필요)
        BrowseRecipePresenter presenter = new BrowseRecipePresenter(viewModel, viewManagerModel);

        // 3. Interactor (UseCaseFactory 사용)
        var interactor = UseCaseFactory.createBrowseRecipeInteractor(presenter);

        // 4. Controller
        BrowseRecipeController controller = new BrowseRecipeController(interactor);

        // 5. View (ViewManagerModel 전달)
        BrowseRecipeView view = new BrowseRecipeView(viewModel, controller, viewManagerModel);

        // 6. ViewManager에 등록
        viewManager.addView(ViewManager.BROWSE_RECIPE_VIEW, view);
    }

    /**
     * Builds the SearchByIngredients flow.
     */
    private void buildSearchByIngredientsFlow() {
        // 1. ViewModel
        RecipeSearchViewModel viewModel = new RecipeSearchViewModel();

        // 2. Presenter (ViewManagerModel 필요)
        SearchByIngredientsPresenter presenter = new SearchByIngredientsPresenter(viewModel, viewManagerModel);

        // 3. Interactor (UseCaseFactory 사용)
        var interactor = UseCaseFactory.createSearchByIngredientsInteractor(presenter);

        // 4. Controller
        SearchByIngredientsController controller = new SearchByIngredientsController(interactor);

        // 5. View (ViewManagerModel 전달)
        SearchByIngredientsView view = new SearchByIngredientsView(controller, viewModel, viewManagerModel);

        // 6. ViewManager에 등록
        viewManager.addView(ViewManager.SEARCH_BY_INGREDIENTS_VIEW, view);
    }

    /**
     * Builds the AdjustServingSize flow.
     */
    private void buildAdjustServingSizeFlow() {
        // 1. ViewModel
        RecipeDetailViewModel viewModel = new RecipeDetailViewModel();

        // 2. Presenter
        AdjustServingSizePresenter presenter = new AdjustServingSizePresenter(viewModel);

        // 3. Interactor (UseCaseFactory 사용)
        var interactor = UseCaseFactory.createAdjustServingSizeInteractor(presenter);

        // 4. Controller
        AdjustServingSizeController controller = new AdjustServingSizeController(interactor);

        // 5. View (ViewManagerModel 전달)
        RecipeDetailView view = new RecipeDetailView(viewModel, controller, viewManagerModel);

        // 6. ViewManager에 등록
        viewManager.addView(ViewManager.RECIPE_DETAIL_VIEW, view);
    }

    private void buildScheduleFlow() {
        //View Model
        ScheduleViewModel viewModel = new ScheduleViewModel();
        //Presenter
        ViewSchedulePresenter presenter = new ViewSchedulePresenter(viewModel);
        //Interactor
        var interactor =UseCaseFactory.createViewScheduleInteractor(presenter);
        //Controller
        ViewScheduleController controller = new ViewScheduleController(interactor);
        //View
        ScheduleView view = new ScheduleView(viewModel, controller, viewManagerModel);
        //Adding to view manager
        viewManager.addView(ViewManager.SCHEDULE_VIEW, view);
    }
}
