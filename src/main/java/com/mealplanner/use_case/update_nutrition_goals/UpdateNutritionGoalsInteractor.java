package com.mealplanner.use_case.update_nutrition_goals;

import com.mealplanner.entity.NutritionGoals;
import com.mealplanner.entity.User;
import com.mealplanner.exception.UserNotFoundException;
import java.util.Objects;

/**
 * Interactor for updating user nutrition goals.
 * Contains the business logic for this use case.
 * 
 * Responsible: Use Case team
 */
public class UpdateNutritionGoalsInteractor implements UpdateNutritionGoalsInputBoundary {
    
    private final UpdateNutritionGoalsDataAccessInterface dataAccess;
    private final UpdateNutritionGoalsOutputBoundary presenter;
    
    public UpdateNutritionGoalsInteractor(
            UpdateNutritionGoalsDataAccessInterface dataAccess,
            UpdateNutritionGoalsOutputBoundary presenter) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "Data access cannot be null");
        this.presenter = Objects.requireNonNull(presenter, "Presenter cannot be null");
    }
    
    @Override
    public void execute(UpdateNutritionGoalsInputData inputData) {
        // 입력 검증
        if (inputData == null) {
            presenter.presentError("Input data cannot be null");
            return;
        }
        
        try {
            // 1. 사용자 조회
            User user = dataAccess.getUserById(inputData.getUserId());
            
            // 2. 새로운 영양 목표 생성
            NutritionGoals newGoals = new NutritionGoals(
                inputData.getDailyCalories(),
                inputData.getDailyProtein(),
                inputData.getDailyCarbs(),
                inputData.getDailyFat()
            );
            
            // 3. 사용자 엔티티 업데이트
            user.setNutritionGoals(newGoals);
            
            // 4. 데이터 저장
            dataAccess.updateUser(user);
            
            // 5. 성공 응답
            UpdateNutritionGoalsOutputData outputData = 
                new UpdateNutritionGoalsOutputData(newGoals);
            presenter.presentSuccess(outputData);
            
        } catch (UserNotFoundException e) {
            presenter.presentError("User not found: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            presenter.presentError("Invalid nutrition goals: " + e.getMessage());
        } catch (Exception e) {
            presenter.presentError("Failed to update nutrition goals: " + e.getMessage());
        }
    }
}

