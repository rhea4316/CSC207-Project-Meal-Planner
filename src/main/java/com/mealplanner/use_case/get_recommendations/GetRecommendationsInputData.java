package com.mealplanner.use_case.get_recommendations;

/**
 * Input data for getting recommendations use case.
 */
public class GetRecommendationsInputData {
    private final String userId;
    
    public GetRecommendationsInputData(String userId) {
        this.userId = userId;
    }
    
    public String getUserId() {
        return userId;
    }
}

