package com.mealplanner.app;

// Factory class for creating use case interactors with properly wired dependencies.
// Responsible: Everyone

import com.mealplanner.data_access.api.EdamamApiClient;
import com.mealplanner.data_access.api.SpoonacularApiClient;
import com.mealplanner.data_access.database.AdjustServingSizeDataAccessObject;
import com.mealplanner.data_access.database.BrowseRecipeAPIParser;
import com.mealplanner.data_access.database.FileScheduleDataAccessObject;
import com.mealplanner.data_access.database.SearchByIngredientsDataAccessObject;
import com.mealplanner.repository.RecipeRepository;
import com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeDataAccessInterface;
import com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeInputBoundary;
import com.mealplanner.use_case.adjust_serving_size.AdjustServingSizeOutputBoundary;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeDataAccessInterface;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeInputBoundary;
import com.mealplanner.use_case.browse_recipe.BrowseRecipeOutputBoundary;
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

    public static FileScheduleDataAccessObject createViewScheduleDataAccess() {
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
        FileScheduleDataAccessObject dataAccess = createViewScheduleDataAccess();
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
}
