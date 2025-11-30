package com.mealplanner.app;

import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.AdjustServingSizeController;
import com.mealplanner.interface_adapter.controller.BrowseRecipeController;
import com.mealplanner.interface_adapter.controller.GetRecommendationsController;
import com.mealplanner.interface_adapter.controller.LoginController;
import com.mealplanner.interface_adapter.controller.SearchByIngredientsController;
import com.mealplanner.interface_adapter.controller.SignupController;
import com.mealplanner.interface_adapter.controller.StoreRecipeController;
import com.mealplanner.interface_adapter.controller.UpdateNutritionGoalsController;
import com.mealplanner.interface_adapter.controller.ViewScheduleController;
import com.mealplanner.interface_adapter.presenter.AdjustServingSizePresenter;
import com.mealplanner.interface_adapter.presenter.BrowseRecipePresenter;
import com.mealplanner.interface_adapter.presenter.GetRecommendationsPresenter;
import com.mealplanner.interface_adapter.presenter.LoginPresenter;
import com.mealplanner.interface_adapter.presenter.SearchByIngredientsPresenter;
import com.mealplanner.interface_adapter.presenter.SignupPresenter;
import com.mealplanner.interface_adapter.presenter.StoreRecipePresenter;
import com.mealplanner.interface_adapter.presenter.UpdateNutritionGoalsPresenter;
import com.mealplanner.interface_adapter.presenter.ViewSchedulePresenter;
import com.mealplanner.interface_adapter.view_model.LoginViewModel;
import com.mealplanner.interface_adapter.view_model.ProfileSettingsViewModel;
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
    private final ViewManager viewManager;
    private final ViewManagerModel viewManagerModel;
    private final RecipeDetailViewModel recipeDetailViewModel;

    public AppBuilder() {
        this.viewManagerModel = new ViewManagerModel();
        this.viewManager = new ViewManager(viewManagerModel);
        this.recipeDetailViewModel = new RecipeDetailViewModel();
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
        buildSignupFlow(scheduleController);
        buildLoginFlow(scheduleController); // Ensure login is built

        // PHASE 2: Create shared RecipeRepository for Schedule and Dashboard
        FileRecipeRepository recipeRepository = new FileRecipeRepository();

        // Build Schedule View - PHASE 2: Inject RecipeRepository for real data
        // PHASE 5: Inject GetRecommendationsController and RecipeBrowseViewModel for Auto-fill functionality
        // PHASE 6: Inject AddMealController for Copy Last Week functionality
        // Create a separate RecipeBrowseViewModel for ScheduleView's auto-fill feature
        RecipeBrowseViewModel scheduleRecommendationsViewModel = new RecipeBrowseViewModel();
        GetRecommendationsPresenter scheduleRecommendationsPresenter = new GetRecommendationsPresenter(scheduleRecommendationsViewModel);
        var scheduleRecommendationsInteractor = UseCaseFactory.createGetRecommendationsInteractor(scheduleRecommendationsPresenter);
        GetRecommendationsController scheduleRecommendationsController = new GetRecommendationsController(scheduleRecommendationsInteractor);
        
        ScheduleView scheduleView = new ScheduleView(
            scheduleViewModel, 
            scheduleController, 
            viewManagerModel, 
            recipeRepository, 
            scheduleRecommendationsController,  // Phase 5: Added for auto-fill
            scheduleRecommendationsViewModel,   // Phase 5: Added for auto-fill
            addMealController                   // Phase 6: Added for Copy Last Week
        );
        viewManager.addView(ViewManager.SCHEDULE_VIEW, scheduleView);

        // PHASE 3: Create GetRecommendations flow for DashboardView
        // Using a separate ViewModel for DashboardView (can be shared with BrowseRecipeView in future if needed)
        RecipeBrowseViewModel recommendationsViewModel = new RecipeBrowseViewModel();
        GetRecommendationsPresenter recommendationsPresenter = new GetRecommendationsPresenter(recommendationsViewModel);
        var recommendationsInteractor = UseCaseFactory.createGetRecommendationsInteractor(recommendationsPresenter);
        GetRecommendationsController recommendationsController = new GetRecommendationsController(recommendationsInteractor);

        // Build Dashboard View - PHASE 3: Inject GetRecommendationsController and ViewModel
        // PHASE 4: Inject AddMealController for auto-generate functionality
        DashboardView dashboardView = new DashboardView(
            viewManagerModel, 
            scheduleViewModel, 
            recipeRepository, 
            recommendationsController,
            recommendationsViewModel,  // Phase 3: Added for recommendations display
            recipeDetailViewModel,     // Phase 3: Added for recipe detail navigation
            addMealController          // Phase 4: Added for auto-generate
        );
        viewManager.addView(ViewManager.DASHBOARD_VIEW, dashboardView);

        // Build Profile View
        ProfileSettingsViewModel profileViewModel = new ProfileSettingsViewModel();
        UpdateNutritionGoalsPresenter updateNutritionGoalsPresenter = new UpdateNutritionGoalsPresenter(profileViewModel);
        var updateNutritionGoalsInteractor = UseCaseFactory.createUpdateNutritionGoalsInteractor(updateNutritionGoalsPresenter);
        UpdateNutritionGoalsController updateNutritionGoalsController = new UpdateNutritionGoalsController(updateNutritionGoalsInteractor);
        ProfileSettingsView profileView = new ProfileSettingsView(viewManagerModel, profileViewModel, updateNutritionGoalsController);
        viewManager.addView(ViewManager.PROFILE_SETTINGS_VIEW, profileView);

        // Set initial view to Login so authentication flow is the first experience
        viewManagerModel.setActiveView(ViewManager.LOGIN_VIEW);
        
        return viewManager;
    }

    private void buildStoreRecipeFlow() {
        RecipeStoreViewModel viewModel = new RecipeStoreViewModel();
        StoreRecipePresenter presenter = new StoreRecipePresenter(viewModel);
        FileRecipeRepository repository = new FileRecipeRepository();
        var interactor = UseCaseFactory.createStoreRecipeInteractor(presenter, repository);
        StoreRecipeController controller = new StoreRecipeController(interactor);
        StoreRecipeView view = new StoreRecipeView(controller, viewModel, viewManagerModel, repository);
        viewManager.addView(ViewManager.STORE_RECIPE_VIEW, view);
    }

    private void buildBrowseRecipeFlow() {
        RecipeBrowseViewModel viewModel = new RecipeBrowseViewModel();
        BrowseRecipePresenter presenter = new BrowseRecipePresenter(viewModel, viewManagerModel);
        var interactor = UseCaseFactory.createBrowseRecipeInteractor(presenter);
        BrowseRecipeController controller = new BrowseRecipeController(interactor);

        // Phase 5: GetRecommendations flow
        GetRecommendationsPresenter recommendationsPresenter = new GetRecommendationsPresenter(viewModel);
        var recommendationsInteractor = UseCaseFactory.createGetRecommendationsInteractor(recommendationsPresenter);
        GetRecommendationsController recommendationsController = new GetRecommendationsController(recommendationsInteractor);
        
        // Create RecipeRepository for local database recipes
        FileRecipeRepository recipeRepository = new FileRecipeRepository();
        
        BrowseRecipeView view = new BrowseRecipeView(viewModel, controller, viewManagerModel, recipeDetailViewModel, recipeRepository, recommendationsController);
        viewManager.addView(ViewManager.BROWSE_RECIPE_VIEW, view);
    }

    private void buildSearchByIngredientsFlow() {
        RecipeSearchViewModel viewModel = new RecipeSearchViewModel();
        SearchByIngredientsPresenter presenter = new SearchByIngredientsPresenter(viewModel, viewManagerModel);
        var interactor = UseCaseFactory.createSearchByIngredientsInteractor(presenter);
        SearchByIngredientsController controller = new SearchByIngredientsController(interactor);
        
        // Create RecipeRepository for local database recipes
        FileRecipeRepository recipeRepository = new FileRecipeRepository();
        
        SearchByIngredientsView view = new SearchByIngredientsView(controller, viewModel, viewManagerModel, recipeDetailViewModel, recipeRepository);
        viewManager.addView(ViewManager.SEARCH_BY_INGREDIENTS_VIEW, view);
    }

    private void buildAdjustServingSizeFlow(com.mealplanner.interface_adapter.controller.AddMealController addMealController) {
        AdjustServingSizePresenter presenter = new AdjustServingSizePresenter(recipeDetailViewModel);
        var interactor = UseCaseFactory.createAdjustServingSizeInteractor(presenter);
        AdjustServingSizeController controller = new AdjustServingSizeController(interactor);
        RecipeDetailView view = new RecipeDetailView(recipeDetailViewModel, controller, addMealController, viewManagerModel);
        viewManager.addView(ViewManager.RECIPE_DETAIL_VIEW, view);
    }

    private void buildSignupFlow(ViewScheduleController scheduleController) {
        SignupViewModel viewModel = new SignupViewModel();
        SignupPresenter presenter = new SignupPresenter(viewModel, viewManagerModel, scheduleController);
        var interactor = UseCaseFactory.createSignupInteractor(presenter);
        SignupController controller = new SignupController(interactor);
        SignupView view = new SignupView(viewModel, controller, viewManagerModel);
        viewManager.addView(ViewManager.SIGNUP_VIEW, view);
    }
    
    private void buildLoginFlow(ViewScheduleController scheduleController) {
        LoginViewModel viewModel = new LoginViewModel();
        LoginPresenter presenter = new LoginPresenter(viewModel, viewManagerModel, scheduleController);
        var interactor = UseCaseFactory.createLoginInteractor(presenter);
        LoginController controller = new LoginController(interactor);
        LoginView view = new LoginView(viewModel, controller, viewManagerModel);
        viewManager.addView(ViewManager.LOGIN_VIEW, view);
    }
}
