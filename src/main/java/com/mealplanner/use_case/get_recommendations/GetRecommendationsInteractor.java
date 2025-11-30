package com.mealplanner.use_case.get_recommendations;

import com.mealplanner.data_access.api.SpoonacularApiClient;
import com.mealplanner.entity.Recipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Interactor for getting recipe recommendations.
 * Implements hybrid approach: saved recipes first, then API popular recipes if needed.
 */
public class GetRecommendationsInteractor implements GetRecommendationsInputBoundary {
    
    private static final Logger logger = LoggerFactory.getLogger(GetRecommendationsInteractor.class);
    
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
                // 2. 저장 레시피가 부족하면 모든 레시피에서 가져오기
                recommendations.addAll(savedRecipes != null ? savedRecipes : new ArrayList<>());
                
                int needed = 3 - recommendations.size();
                if (needed > 0) {
                    // 2-1. 먼저 모든 레시피에서 가져오기
                    try {
                        List<Recipe> allRecipes = dataAccess.getAllRecipes();
                        logger.debug("Retrieved {} recipes from getAllRecipes()", allRecipes != null ? allRecipes.size() : 0);
                        if (allRecipes != null && !allRecipes.isEmpty()) {
                            // 이미 추가된 레시피 제외
                            List<Recipe> availableRecipes = new ArrayList<>(allRecipes);
                            availableRecipes.removeAll(recommendations);
                            
                            logger.debug("Available recipes after removing duplicates: {}", availableRecipes.size());
                            if (!availableRecipes.isEmpty()) {
                                Collections.shuffle(availableRecipes);
                                int toAdd = Math.min(needed, availableRecipes.size());
                                recommendations.addAll(availableRecipes.subList(0, toAdd));
                                logger.info("Added {} recipes from database to recommendations", toAdd);
                                needed = 3 - recommendations.size();
                            }
                        } else {
                            logger.warn("getAllRecipes() returned null or empty list");
                        }
                    } catch (Exception e) {
                        logger.error("Error while getting all recipes: {}", e.getMessage(), e);
                        // 에러 발생 시 무시하고 API 호출 시도
                    }
                    
                    // 2-2. 여전히 부족하면 API 호출
                    if (needed > 0) {
                        try {
                            List<Recipe> popularRecipes = apiClient.getPopularRecipes(needed);
                            recommendations.addAll(popularRecipes);
                        } catch (IOException e) {
                            // API 호출 실패 시 로그 기록
                            logger.warn("Failed to fetch popular recipes from API: {}", e.getMessage(), e);
                            
                            // 부분 성공 시에는 조용히 처리 (이미 일부 레시피가 있으므로)
                            // 완전 실패 시에만 사용자에게 알림
                            if (recommendations.isEmpty()) {
                                // 레시피를 전혀 수집하지 못함 - 사용자에게 알림
                                presenter.presentError("Failed to load recommendations. Please check your internet connection and try again.");
                            }
                            // 부분 성공 시에는 조용히 처리 (GetRecommendationsPresenter가 빈 리스트로 처리)
                            // 사용자는 이미 수집된 레시피를 볼 수 있음
                        }
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

