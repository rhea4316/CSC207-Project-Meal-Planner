package com.mealplanner.use_case.get_recommendations;

import com.mealplanner.data_access.api.SpoonacularApiClient;
import com.mealplanner.entity.Recipe;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Interactor for getting recipe recommendations.
 * Implements hybrid approach: saved recipes first, then API popular recipes if needed.
 */
public class GetRecommendationsInteractor implements GetRecommendationsInputBoundary {
    
    private final GetRecommendationsDataAccessInterface dataAccess;
    private final SpoonacularApiClient apiClient;
    private final GetRecommendationsOutputBoundary presenter;
    
    public GetRecommendationsInteractor(
            GetRecommendationsDataAccessInterface dataAccess,
            SpoonacularApiClient apiClient,
            GetRecommendationsOutputBoundary presenter) {
        this.dataAccess = dataAccess;
        this.apiClient = apiClient;
        this.presenter = presenter;
    }
    
    @Override
    public void execute(GetRecommendationsInputData inputData) {
        try {
            List<Recipe> recommendations = new ArrayList<>();
            String userId = inputData.getUserId();
            
            if (userId == null || userId.trim().isEmpty()) {
                presenter.presentError("User ID is required");
                return;
            }
            
            // 1. 먼저 사용자가 저장한 레시피 확인
            List<Recipe> savedRecipes = dataAccess.getSavedRecipesByUser(userId);
            
            if (savedRecipes != null && savedRecipes.size() >= 3) {
                // 랜덤 3개 선택
                List<Recipe> shuffled = new ArrayList<>(savedRecipes);
                Collections.shuffle(shuffled);
                recommendations = shuffled.subList(0, Math.min(3, shuffled.size()));
            } else {
                // 2. 저장 레시피가 부족하면 인기 레시피 API 호출
                recommendations.addAll(savedRecipes != null ? savedRecipes : new ArrayList<>());
                
                int needed = 3 - recommendations.size();
                if (needed > 0) {
                    try {
                        List<Recipe> popularRecipes = apiClient.getPopularRecipes(needed);
                        recommendations.addAll(popularRecipes);
                    } catch (IOException e) {
                        // API 호출 실패 시 저장 레시피만 반환
                        // 에러는 로그만 남기고 사용자에게는 표시하지 않음
                    }
                }
            }
            
            GetRecommendationsOutputData outputData = 
                new GetRecommendationsOutputData(recommendations);
            presenter.presentRecommendations(outputData);
            
        } catch (Exception e) {
            presenter.presentError("Failed to load recommendations: " + e.getMessage());
        }
    }
}

