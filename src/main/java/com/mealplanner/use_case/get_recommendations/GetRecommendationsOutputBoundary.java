package com.mealplanner.use_case.get_recommendations;

/**
 * Output boundary for getting recommendations use case.
 */
public interface GetRecommendationsOutputBoundary {
    void presentRecommendations(GetRecommendationsOutputData outputData);
    void presentError(String errorMessage);
}

