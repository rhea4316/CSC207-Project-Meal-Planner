package com.mealplanner.app;

import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.AdjustServingSizeController;
import com.mealplanner.interface_adapter.controller.BrowseRecipeController;
import com.mealplanner.interface_adapter.controller.LoginController;
import com.mealplanner.interface_adapter.controller.SearchByIngredientsController;
import com.mealplanner.interface_adapter.controller.SignupController;
import com.mealplanner.interface_adapter.controller.StoreRecipeController;
import com.mealplanner.interface_adapter.controller.ViewScheduleController;
import com.mealplanner.interface_adapter.presenter.AdjustServingSizePresenter;
import com.mealplanner.interface_adapter.presenter.BrowseRecipePresenter;
import com.mealplanner.interface_adapter.presenter.LoginPresenter;
import com.mealplanner.interface_adapter.presenter.SearchByIngredientsPresenter;
import com.mealplanner.interface_adapter.presenter.SignupPresenter;
import com.mealplanner.interface_adapter.presenter.StoreRecipePresenter;
import com.mealplanner.interface_adapter.presenter.ViewSchedulePresenter;
import com.mealplanner.interface_adapter.view_model.LoginViewModel;
import com.mealplanner.interface_adapter.view_model.RecipeBrowseViewModel;
import com.mealplanner.interface_adapter.view_model.RecipeDetailViewModel;
import com.mealplanner.interface_adapter.view_model.RecipeSearchViewModel;
import com.mealplanner.interface_adapter.view_model.RecipeStoreViewModel;
import com.mealplanner.interface_adapter.view_model.ScheduleViewModel;
import com.mealplanner.interface_adapter.view_model.SignupViewModel;
import com.mealplanner.repository.impl.FileRecipeRepository;
import com.mealplanner.view.BrowseRecipeView;
import com.mealplanner.view.DashboardView;
import com.mealplanner.view.LoginView;
import com.mealplanner.view.ProfileSettingsView;
import com.mealplanner.view.RecipeDetailView;
import com.mealplanner.view.ScheduleView;
import com.mealplanner.view.SearchByIngredientsView;
import com.mealplanner.view.SignupView;
import com.mealplanner.view.StoreRecipeView;
import com.mealplanner.view.ViewManager;

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
        
        // Add Meal Flow (Shared Controller)
        com.mealplanner.interface_adapter.view_model.MealPlanViewModel mealPlanViewModel = new com.mealplanner.interface_adapter.view_model.MealPlanViewModel();
        com.mealplanner.interface_adapter.presenter.MealPlanPresenter mealPlanPresenter = new com.mealplanner.interface_adapter.presenter.MealPlanPresenter(mealPlanViewModel, viewManagerModel);
        var addMealInteractor = UseCaseFactory.createAddMealInteractor(mealPlanPresenter, viewManagerModel);
        com.mealplanner.interface_adapter.controller.AddMealController addMealController = new com.mealplanner.interface_adapter.controller.AddMealController(addMealInteractor);

        // Build other flows
        buildStoreRecipeFlow();
        buildBrowseRecipeFlow();
        buildSearchByIngredientsFlow();
        buildAdjustServingSizeFlow(addMealController);
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

    private void buildAdjustServingSizeFlow(com.mealplanner.interface_adapter.controller.AddMealController addMealController) {
        RecipeDetailViewModel viewModel = new RecipeDetailViewModel();
        AdjustServingSizePresenter presenter = new AdjustServingSizePresenter(viewModel);
        var interactor = UseCaseFactory.createAdjustServingSizeInteractor(presenter);
        AdjustServingSizeController controller = new AdjustServingSizeController(interactor);
        RecipeDetailView view = new RecipeDetailView(viewModel, controller, addMealController, viewManagerModel);
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
