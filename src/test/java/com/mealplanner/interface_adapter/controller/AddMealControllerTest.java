package com.mealplanner.interface_adapter.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mealplanner.use_case.manage_meal_plan.add.AddMealInputBoundary;

/**
 * Test class for AddMealController.
 * Tests controller input validation and interactor invocation.
 *
 * Responsible: Grace (primary)
 */
public class AddMealControllerTest {

    private AddMealController controller;

    @Mock
    private AddMealInputBoundary interactor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new AddMealController(interactor);
    }

    @Test
    public void testAddMealWithValidData() {
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
    public void testAddMealWithInvalidDate() {
        String invalidDate = "invalid-date";
        String mealType = "BREAKFAST";
        String recipeID = "recipe-1";
        
        assertThrows(Exception.class, () -> {
            controller.execute(invalidDate, mealType, recipeID);
        });
    }

    @Test
    public void testAddMealWithInvalidMealType() {
        String date = java.time.LocalDate.now().toString();
        String invalidMealType = "INVALID";
        String recipeID = "recipe-1";
        
        assertThrows(Exception.class, () -> {
            controller.execute(date, invalidMealType, recipeID);
        });
    }
}
