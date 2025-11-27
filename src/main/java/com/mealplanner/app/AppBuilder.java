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
        buildLoginFlow();
        buildSignupFlow();
        buildStoreRecipeFlow();
        buildBrowseRecipeFlow();
        buildSearchByIngredientsFlow();
        buildAdjustServingSizeFlow();
        buildScheduleFlow();
        buildMealPlanFlow();
        
        // Set initial view - use ViewManager's switchToView to ensure proper display
        viewManager.switchToView(ViewManager.LOGIN_VIEW);
        
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
