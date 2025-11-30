package com.mealplanner.interface_adapter.controller;

import com.mealplanner.use_case.get_recommendations.GetRecommendationsInputBoundary;
import com.mealplanner.use_case.get_recommendations.GetRecommendationsInputData;

/**
 * Controller for getting recipe recommendations.
 */
public class GetRecommendationsController {
    private final GetRecommendationsInputBoundary interactor;
    
    public GetRecommendationsController(GetRecommendationsInputBoundary interactor) {
        this.interactor = interactor;
    }
    
    public void execute(String userId) {
        GetRecommendationsInputData inputData = new GetRecommendationsInputData(userId);
        interactor.execute(inputData);
    }
}

