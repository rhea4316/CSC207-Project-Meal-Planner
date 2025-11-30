package com.mealplanner.interface_adapter.presenter;

import com.mealplanner.interface_adapter.view_model.RecipeBrowseViewModel;
import com.mealplanner.use_case.get_recommendations.GetRecommendationsOutputBoundary;
import com.mealplanner.use_case.get_recommendations.GetRecommendationsOutputData;
import java.util.ArrayList;

/**
 * Presenter for getting recommendations use case.
 */
public class GetRecommendationsPresenter implements GetRecommendationsOutputBoundary {
    private final RecipeBrowseViewModel viewModel;
    
    public GetRecommendationsPresenter(RecipeBrowseViewModel viewModel) {
        this.viewModel = viewModel;
    }
    
    @Override
    public void presentRecommendations(GetRecommendationsOutputData outputData) {
        viewModel.setRecommendations(outputData.getRecommendations());
    }
    
    @Override
    public void presentError(String errorMessage) {
        // 에러는 조용히 처리 (추천이 없어도 앱은 정상 동작)
        viewModel.setRecommendations(new ArrayList<>());
    }
}

