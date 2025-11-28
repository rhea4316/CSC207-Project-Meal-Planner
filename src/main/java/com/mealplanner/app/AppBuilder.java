package com.mealplanner.app;

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

    public ViewManagerModel getViewManagerModel() {
        return viewManagerModel;
    }

    /**
     * Builds the complete application with all components wired together.
     */
    public ViewManager build() {
        // Note: We need to defer view creation until JavaFX toolkit is initialized if they use controls directly.
        // Since AppBuilder is called from start(), toolkit is ready.
        
        // Schedule flow is special due to shared ViewModel
        ScheduleViewModel scheduleViewModel = new ScheduleViewModel();
        ViewSchedulePresenter schedulePresenter = new ViewSchedulePresenter(scheduleViewModel);
        var scheduleInteractor = UseCaseFactory.createViewScheduleInteractor(schedulePresenter);
        ViewScheduleController scheduleController = new ViewScheduleController(scheduleInteractor);
        
        // Build other flows
        buildStoreRecipeFlow();
        buildBrowseRecipeFlow();
        buildSearchByIngredientsFlow();
        buildAdjustServingSizeFlow();
        buildSignupFlow();
        buildLoginFlow(); // Ensure login is built
        
        // Build Schedule View
        ScheduleView scheduleView = new ScheduleView(scheduleViewModel, scheduleController, viewManagerModel);
        viewManager.addView(ViewManager.SCHEDULE_VIEW, scheduleView);

        // Build Dashboard View
        DashboardView dashboardView = new DashboardView(viewManagerModel, scheduleViewModel);
        viewManager.addView(ViewManager.DASHBOARD_VIEW, dashboardView);
        
        // Build Profile View (Dummy for now, matching Sidebar)
        ProfileSettingsView profileView = new ProfileSettingsView(viewManagerModel, "Eden Chang");
        viewManager.addView(ViewManager.PROFILE_SETTINGS_VIEW, profileView);

        // Set initial view - use ViewManager's switchToView to ensure proper display
        viewManager.switchToView(ViewManager.DASHBOARD_VIEW);
        
        return viewManager;
    }

    private void buildStoreRecipeFlow() {
        RecipeStoreViewModel viewModel = new RecipeStoreViewModel();
        StoreRecipePresenter presenter = new StoreRecipePresenter(viewModel);
        FileRecipeRepository repository = new FileRecipeRepository();
        var interactor = UseCaseFactory.createStoreRecipeInteractor(presenter, repository);
        StoreRecipeController controller = new StoreRecipeController(interactor);
        StoreRecipeView view = new StoreRecipeView(controller, viewModel, viewManagerModel);
        viewManager.addView(ViewManager.STORE_RECIPE_VIEW, view);
    }

    private void buildBrowseRecipeFlow() {
        RecipeBrowseViewModel viewModel = new RecipeBrowseViewModel();
        BrowseRecipePresenter presenter = new BrowseRecipePresenter(viewModel, viewManagerModel);
        var interactor = UseCaseFactory.createBrowseRecipeInteractor(presenter);
        BrowseRecipeController controller = new BrowseRecipeController(interactor);
        BrowseRecipeView view = new BrowseRecipeView(viewModel, controller, viewManagerModel);
        viewManager.addView(ViewManager.BROWSE_RECIPE_VIEW, view);
    }

    private void buildSearchByIngredientsFlow() {
        RecipeSearchViewModel viewModel = new RecipeSearchViewModel();
        SearchByIngredientsPresenter presenter = new SearchByIngredientsPresenter(viewModel, viewManagerModel);
        var interactor = UseCaseFactory.createSearchByIngredientsInteractor(presenter);
        SearchByIngredientsController controller = new SearchByIngredientsController(interactor);
        SearchByIngredientsView view = new SearchByIngredientsView(controller, viewModel, viewManagerModel);
        viewManager.addView(ViewManager.SEARCH_BY_INGREDIENTS_VIEW, view);
    }

    private void buildAdjustServingSizeFlow() {
        RecipeDetailViewModel viewModel = new RecipeDetailViewModel();
        AdjustServingSizePresenter presenter = new AdjustServingSizePresenter(viewModel);
        var interactor = UseCaseFactory.createAdjustServingSizeInteractor(presenter);
        AdjustServingSizeController controller = new AdjustServingSizeController(interactor);
        RecipeDetailView view = new RecipeDetailView(viewModel, controller, viewManagerModel);
        viewManager.addView(ViewManager.RECIPE_DETAIL_VIEW, view);
    }

    private void buildSignupFlow() {
        SignupViewModel viewModel = new SignupViewModel();
        SignupPresenter presenter = new SignupPresenter(viewModel, viewManagerModel);
        var interactor = UseCaseFactory.createSignupInteractor(presenter);
        SignupController controller = new SignupController(interactor);
        SignupView view = new SignupView(viewModel, controller, viewManagerModel);
        viewManager.addView(ViewManager.SIGNUP_VIEW, view);
    }
    
    private void buildLoginFlow() {
        LoginViewModel viewModel = new LoginViewModel();
        LoginPresenter presenter = new LoginPresenter(viewModel, viewManagerModel);
        var interactor = UseCaseFactory.createLoginInteractor(presenter);
        LoginController controller = new LoginController(interactor);
        LoginView view = new LoginView(viewModel, controller, viewManagerModel);
        viewManager.addView(ViewManager.LOGIN_VIEW, view);
    }
}
