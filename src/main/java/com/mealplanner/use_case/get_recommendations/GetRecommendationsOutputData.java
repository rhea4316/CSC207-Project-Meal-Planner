package com.mealplanner.use_case.get_recommendations;

import com.mealplanner.entity.Recipe;
import java.util.ArrayList;
import java.util.List;

/**
 * Output data for getting recommendations use case.
 */
public class GetRecommendationsOutputData {
    private final List<Recipe> recommendations;
    
    public GetRecommendationsOutputData(List<Recipe> recommendations) {
        this.recommendations = recommendations != null 
            ? new ArrayList<>(recommendations) 
            : new ArrayList<>();
    }
    
    public List<Recipe> getRecommendations() {
        return new ArrayList<>(recommendations);
    }
}

