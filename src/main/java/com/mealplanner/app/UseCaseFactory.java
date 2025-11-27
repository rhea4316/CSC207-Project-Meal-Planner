package com.mealplanner.app;

// Factory class for creating use case interactors with properly wired dependencies.
// Responsible: Everyone

import com.mealplanner.data_access.api.EdamamApiClient;
import com.mealplanner.data_access.api.SpoonacularApiClient;
import com.mealplanner.data_access.database.AdjustServingSizeDataAccessObject;
import com.mealplanner.data_access.database.BrowseRecipeAPIParser;
import com.mealplanner.data_access.database.FileScheduleDataAccessObject;
import com.mealplanner.data_access.database.FileUserDataAccessObject;
import com.mealplanner.data_access.database.SearchByIngredientsDataAccessObject;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.repository.RecipeRepository;
import com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeDataAccessInterface;
import com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeInputBoundary;
import com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeOutputBoundary;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeDataAccessInterface;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeInputBoundary;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeOutputBoundary;
import com.mealplanner.use_case.login.LoginDataAccessInterface;
import com.mealplanner.use_case.login.LoginInputBoundary;
import com.mealplanner.use_case.login.LoginOutputBoundary;
import com.mealplanner.use_case.signup.SignupDataAccessInterface;
import com.mealplanner.use_case.signup.SignupInputBoundary;
import com.mealplanner.use_case.signup.SignupOutputBoundary;
import com.mealplanner.use_case.manage_meal_plan.add.AddMealDataAccessInterface;
import com.mealplanner.use_case.manage_meal_plan.add.AddMealInputBoundary;
import com.mealplanner.use_case.manage_meal_plan.add.AddMealOutputBoundary;
import com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealDataAccessInterface;
import com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealInputBoundary;
import com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealOutputBoundary;
import com.mealplanner.use_case.manage_meal_plan.edit.EditMealDataAccessInterface;
import com.mealplanner.use_case.manage_meal_plan.edit.EditMealInputBoundary;
import com.mealplanner.use_case.manage_meal_plan.edit.EditMealOutputBoundary;
import com.mealplanner.use_case.search_by_ingredients.SearchByIngredientsDataAccessInterface;
import com.mealplanner.use_case.search_by_ingredients.SearchByIngredientsInputBoundary;
import com.mealplanner.use_case.search_by_ingredients.SearchByIngredientsOutputBoundary;
import com.mealplanner.use_case.store_recipe.StoreRecipeInputBoundary;
import com.mealplanner.use_case.store_recipe.StoreRecipeOutputBoundary;

import com.mealplanner.use_case.view_schedule.ViewScheduleInputBoundary;
import com.mealplanner.use_case.view_schedule.ViewScheduleOutputBoundary;
import okhttp3.OkHttpClient;

public class UseCaseFactory {

    // Private constructor to prevent instantiation
    private UseCaseFactory() {
        throw new AssertionError("UseCaseFactory should not be instantiated");
    }

    // ========== 공통 인스턴스 생성 메서드 ==========

    /**
     * Creates a new OkHttpClient instance.
     */
    public static OkHttpClient createOkHttpClient() {
        return new OkHttpClient();
    }

    /**
     * Creates a SpoonacularApiClient instance.
     */
    public static SpoonacularApiClient createSpoonacularApiClient() {
        OkHttpClient client = createOkHttpClient();
        return new SpoonacularApiClient(client);
    }

    /**
     * Creates an EdamamApiClient instance.
     */
    public static EdamamApiClient createEdamamApiClient() {
        OkHttpClient client = createOkHttpClient();
        return new EdamamApiClient(client);
    }

    // ========== DataAccessObject 생성 메서드 ==========

    /**
     * Creates a BrowseRecipeDataAccessInterface instance.
     */
    public static BrowseRecipeDataAccessInterface createBrowseRecipeDataAccess() {
        SpoonacularApiClient apiClient = createSpoonacularApiClient();
        return new BrowseRecipeAPIParser(apiClient);
    }

    /**
     * Creates an AdjustServingSizeDataAccessInterface instance.
     */
    public static AdjustServingSizeDataAccessInterface createAdjustServingSizeDataAccess() {
        SpoonacularApiClient apiClient = createSpoonacularApiClient();
        return new AdjustServingSizeDataAccessObject(apiClient);
    }

    public static ViewScheduleDataAccessInterface createViewScheduleDataAccess() {
        return new FileScheduleDataAccessObject();
    }

    /**
     * Creates a SearchByIngredientsDataAccessInterface instance.
     */
    public static SearchByIngredientsDataAccessInterface createSearchByIngredientsDataAccess() {
        SpoonacularApiClient apiClient = createSpoonacularApiClient();
        return new SearchByIngredientsDataAccessObject(apiClient);
    }

    // ========== Interactor 생성 메서드 ==========

    /**
     * Creates a BrowseRecipeInteractor with properly wired dependencies.
     */
    public static BrowseRecipeInputBoundary createBrowseRecipeInteractor(BrowseRecipeOutputBoundary presenter) {
        if (presenter == null) {
            throw new IllegalArgumentException("Presenter cannot be null");
        }
        BrowseRecipeDataAccessInterface dataAccess = createBrowseRecipeDataAccess();
        return new com.mealplanner.use_case.browse_recipe.BrowseRecipeInteractor(dataAccess, presenter);
    }

    /**
     * Creates an AdjustServingSizeInteractor with properly wired dependencies.
     */
    public static AdjustServingSizeInputBoundary createAdjustServingSizeInteractor(AdjustServingSizeOutputBoundary presenter) {
        if (presenter == null) {
            throw new IllegalArgumentException("Presenter cannot be null");
        }
        AdjustServingSizeDataAccessInterface dataAccess = createAdjustServingSizeDataAccess();
        return new com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeInteractor(dataAccess, presenter);
    }

    public static ViewScheduleInputBoundary createViewScheduleInteractor(ViewScheduleOutputBoundary presenter) {
        if (presenter == null) {
            throw new IllegalArgumentException("Presenter cannot be null");
        }
        ViewScheduleDataAccessInterface dataAccess = createViewScheduleDataAccess();
        return new com.mealplanner.use_case.view_schedule.ViewScheduleInteractor(dataAccess, presenter);
    }

    /**
     * Creates a SearchByIngredientsInteractor with properly wired dependencies.
     */
    public static SearchByIngredientsInputBoundary createSearchByIngredientsInteractor(SearchByIngredientsOutputBoundary presenter) {
        if (presenter == null) {
            throw new IllegalArgumentException("Presenter cannot be null");
        }
        SearchByIngredientsDataAccessInterface dataAccess = createSearchByIngredientsDataAccess();
        return new com.mealplanner.use_case.search_by_ingredients.SearchByIngredientsInteractor(dataAccess, presenter);
    }

    /**
     * Creates a StoreRecipeInteractor with properly wired dependencies.
     */
    public static StoreRecipeInputBoundary createStoreRecipeInteractor(StoreRecipeOutputBoundary presenter, RecipeRepository repository) {
        if (presenter == null) {
            throw new IllegalArgumentException("Presenter cannot be null");
        }
        if (repository == null) {
            throw new IllegalArgumentException("Repository cannot be null");
        }
        return new com.mealplanner.use_case.store_recipe.StoreRecipeInteractor(presenter, repository);
    }

    /**
     * Creates a LoginInteractor with properly wired dependencies.
     */
    public static LoginInputBoundary createLoginInteractor(LoginOutputBoundary presenter) {
        if (presenter == null) {
            throw new IllegalArgumentException("Presenter cannot be null");
        }
        LoginDataAccessInterface dataAccess = new FileUserDataAccessObject();
        return new com.mealplanner.use_case.login.LoginInteractor(dataAccess, presenter);
    }

    /**
     * Creates a SignupInteractor with properly wired dependencies.
     */
    public static SignupInputBoundary createSignupInteractor(SignupOutputBoundary presenter) {
        if (presenter == null) {
            throw new IllegalArgumentException("Presenter cannot be null");
        }
        SignupDataAccessInterface dataAccess = new FileUserDataAccessObject();
        return new com.mealplanner.use_case.signup.SignupInteractor(dataAccess, presenter);
    }

    /**
     * Creates an AddMealInteractor with properly wired dependencies.
     */
    public static AddMealInputBoundary createAddMealInteractor(AddMealOutputBoundary presenter, ViewManagerModel viewManagerModel) {
        if (presenter == null) {
            throw new IllegalArgumentException("Presenter cannot be null");
        }
        if (viewManagerModel == null) {
            throw new IllegalArgumentException("ViewManagerModel cannot be null");
        }
        AddMealDataAccessInterface dataAccess = new FileScheduleDataAccessObject(new FileUserDataAccessObject(), viewManagerModel);
        return new com.mealplanner.use_case.manage_meal_plan.add.AddMealInteractor(dataAccess, presenter);
    }

    /**
     * Creates an EditMealInteractor with properly wired dependencies.
     */
    public static EditMealInputBoundary createEditMealInteractor(EditMealOutputBoundary presenter, ViewManagerModel viewManagerModel) {
        if (presenter == null) {
            throw new IllegalArgumentException("Presenter cannot be null");
        }
        if (viewManagerModel == null) {
            throw new IllegalArgumentException("ViewManagerModel cannot be null");
        }
        EditMealDataAccessInterface dataAccess = new FileScheduleDataAccessObject(new FileUserDataAccessObject(), viewManagerModel);
        return new com.mealplanner.use_case.manage_meal_plan.edit.EditMealInteractor(dataAccess, presenter);
    }

    /**
     * Creates a DeleteMealInteractor with properly wired dependencies.
     */
    public static DeleteMealInputBoundary createDeleteMealInteractor(DeleteMealOutputBoundary presenter, ViewManagerModel viewManagerModel) {
        if (presenter == null) {
            throw new IllegalArgumentException("Presenter cannot be null");
        }
        if (viewManagerModel == null) {
            throw new IllegalArgumentException("ViewManagerModel cannot be null");
        }
        DeleteMealDataAccessInterface dataAccess = new FileScheduleDataAccessObject(new FileUserDataAccessObject(), viewManagerModel);
        return new com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealInteractor(dataAccess, presenter);
    }
}
