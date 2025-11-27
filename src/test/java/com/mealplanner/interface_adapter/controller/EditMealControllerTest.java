package com.mealplanner.interface_adapter.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mealplanner.use_case.manage_meal_plan.edit.EditMealInputBoundary;
import com.mealplanner.use_case.manage_meal_plan.edit.EditMealInputData;
import com.mealplanner.entity.MealType;

import java.time.LocalDate;

/**
 * Test class for EditMealController.
 * Tests controller input validation and interactor invocation.
 *
 * Responsible: Grace (primary)
 */
public class EditMealControllerTest {

    private EditMealController controller;

    @Mock
    private EditMealInputBoundary interactor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new EditMealController(interactor);
    }

    @Test
    public void testEditMealWithValidData() {
        String date = java.time.LocalDate.now().toString();
        String mealType = "BREAKFAST";
        String recipeID = "recipe-1";
        
        controller.execute(date, mealType, recipeID);
        
        verify(interactor).execute(argThat(inputData -> 
            inputData.getDate().equals(java.time.LocalDate.parse(date)) &&
            inputData.getMealType() == com.mealplanner.entity.MealType.BREAKFAST &&
            inputData.getRecipe().equals(recipeID)
        ));
    }

    @Test
    public void testEditMealWithInvalidData() {
        String invalidDate = "invalid-date";
        String mealType = "BREAKFAST";
        String recipeID = "recipe-1";
        
        assertThrows(Exception.class, () -> {
            controller.execute(invalidDate, mealType, recipeID);
        });
    }

    @Test
    public void testEditMealWithInvalidMealType() {
        String date = java.time.LocalDate.now().toString();
        String invalidMealType = "INVALID";
        String recipeID = "recipe-1";
        
        assertThrows(Exception.class, () -> {
            controller.execute(date, invalidMealType, recipeID);
        });
    }
}
