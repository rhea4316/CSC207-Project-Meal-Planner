package com.mealplanner.app;

import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.*;
import com.mealplanner.interface_adapter.presenter.*;
import com.mealplanner.interface_adapter.view_model.*;
import com.mealplanner.repository.impl.FileRecipeRepository;
import com.mealplanner.view.*;
import javafx.scene.Node;

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
        
        buildStoreRecipeFlow();
        buildBrowseRecipeFlow();
        buildSearchByIngredientsFlow();
        buildAdjustServingSizeFlow();
        // Schedule flow is special due to shared ViewModel
        
        ScheduleViewModel scheduleViewModel = new ScheduleViewModel();
        ViewSchedulePresenter schedulePresenter = new ViewSchedulePresenter(scheduleViewModel);
        var scheduleInteractor = UseCaseFactory.createViewScheduleInteractor(schedulePresenter);
        ViewScheduleController scheduleController = new ViewScheduleController(scheduleInteractor);
        // ScheduleView will need to be JavaFX
        // ScheduleView scheduleView = new ScheduleView(scheduleViewModel, scheduleController, viewManagerModel);
        // viewManager.addView(ViewManager.SCHEDULE_VIEW, scheduleView);

        // DashboardView will need to be JavaFX
        // DashboardView dashboardView = new DashboardView(viewManagerModel, scheduleViewModel);
        // viewManager.addView(ViewManager.DASHBOARD_VIEW, dashboardView);

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
        // StoreRecipeView view = new StoreRecipeView(controller, viewModel, viewManagerModel);
        // viewManager.addView(ViewManager.STORE_RECIPE_VIEW, view);
    }

    private void buildBrowseRecipeFlow() {
        RecipeBrowseViewModel viewModel = new RecipeBrowseViewModel();
        BrowseRecipePresenter presenter = new BrowseRecipePresenter(viewModel, viewManagerModel);
        var interactor = UseCaseFactory.createBrowseRecipeInteractor(presenter);
        BrowseRecipeController controller = new BrowseRecipeController(interactor);
        // BrowseRecipeView view = new BrowseRecipeView(viewModel, controller, viewManagerModel);
        // viewManager.addView(ViewManager.BROWSE_RECIPE_VIEW, view);
    }

    private void buildSearchByIngredientsFlow() {
        RecipeSearchViewModel viewModel = new RecipeSearchViewModel();
        SearchByIngredientsPresenter presenter = new SearchByIngredientsPresenter(viewModel, viewManagerModel);
        var interactor = UseCaseFactory.createSearchByIngredientsInteractor(presenter);
        SearchByIngredientsController controller = new SearchByIngredientsController(interactor);
        // SearchByIngredientsView view = new SearchByIngredientsView(controller, viewModel, viewManagerModel);
        // viewManager.addView(ViewManager.SEARCH_BY_INGREDIENTS_VIEW, view);
    }

    private void buildAdjustServingSizeFlow() {
        RecipeDetailViewModel viewModel = new RecipeDetailViewModel();
        AdjustServingSizePresenter presenter = new AdjustServingSizePresenter(viewModel);
        var interactor = UseCaseFactory.createAdjustServingSizeInteractor(presenter);
        AdjustServingSizeController controller = new AdjustServingSizeController(interactor);
        // RecipeDetailView view = new RecipeDetailView(viewModel, controller, viewManagerModel);
        // viewManager.addView(ViewManager.RECIPE_DETAIL_VIEW, view);
    }

    /**
     * Builds the Login flow.
     */
    private void buildLoginFlow() {
        // 1. ViewModel
        LoginViewModel viewModel = new LoginViewModel();

        // 2. Presenter (ViewManagerModel 필요)
        LoginPresenter presenter = new LoginPresenter(viewModel, viewManagerModel);

        // 3. Interactor (UseCaseFactory 사용)
        var interactor = UseCaseFactory.createLoginInteractor(presenter);

        // 4. Controller
        LoginController controller = new LoginController(interactor);

        // 5. View (ViewManagerModel 전달)
        LoginView view = new LoginView(viewModel, controller, viewManagerModel);

        // 6. ViewManager에 등록
        viewManager.addView(ViewManager.LOGIN_VIEW, view);
    }

    /**
     * Builds the Signup flow.
     */
    private void buildSignupFlow() {
        // 1. ViewModel
        SignupViewModel viewModel = new SignupViewModel();

        // 2. Presenter (ViewManagerModel 필요)
        SignupPresenter presenter = new SignupPresenter(viewModel, viewManagerModel);

        // 3. Interactor (UseCaseFactory 사용)
        com.mealplanner.use_case.signup.SignupInputBoundary interactor = 
            UseCaseFactory.createSignupInteractor(presenter);

        // 4. Controller
        SignupController controller = new SignupController(interactor);

        // 5. View (ViewManagerModel 전달)
        SignupView view = new SignupView(viewModel, controller, viewManagerModel);

        // 6. ViewManager에 등록
        viewManager.addView(ViewManager.SIGNUP_VIEW, view);
    }

    /**
     * Builds the MealPlan flow.
     */
    private void buildMealPlanFlow() {
        // 1. ViewModel
        MealPlanViewModel viewModel = new MealPlanViewModel();

        // 2. Presenter
        MealPlanPresenter presenter = new MealPlanPresenter(viewModel, viewManagerModel);

        // 3. Interactors (UseCaseFactory 사용, ViewManagerModel 전달)
        var addMealInteractor = UseCaseFactory.createAddMealInteractor(presenter, viewManagerModel);
        var editMealInteractor = UseCaseFactory.createEditMealInteractor(presenter, viewManagerModel);
        var deleteMealInteractor = UseCaseFactory.createDeleteMealInteractor(presenter, viewManagerModel);

        // 4. Controllers
        AddMealController addMealController = new AddMealController(addMealInteractor);
        EditMealController editMealController = new EditMealController(editMealInteractor);
        DeleteMealController deleteMealController = new DeleteMealController(deleteMealInteractor);

        // 5. View (컨트롤러들 전달)
        MealPlanView view = new MealPlanView(viewModel, viewManagerModel,
                addMealController, editMealController, deleteMealController);

        // 6. ViewManager에 등록
        viewManager.addView(ViewManager.MEAL_PLAN_VIEW, view);
    }
}
