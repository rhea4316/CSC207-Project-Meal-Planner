package com.mealplanner.use_case.get_recommendations;

import com.mealplanner.entity.Recipe;
import java.util.List;

/**
 * Data access interface for getting recommendations.
 * Provides methods to retrieve saved recipes by user.
 */
public interface GetRecommendationsDataAccessInterface {
    /**
     * Get all recipes saved by a specific user.
     * 
     * @param userId User ID
     * @return List of recipes saved by the user
     */
    List<Recipe> getSavedRecipesByUser(String userId);
}

