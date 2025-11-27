package com.mealplanner.interface_adapter.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealInputBoundary;
import com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealInputData;
import com.mealplanner.entity.MealType;

import java.time.LocalDate;

/**
 * Test class for DeleteMealController.
 * Tests controller input validation and interactor invocation.
 *
 * Responsible: Grace (primary)
 */
public class DeleteMealControllerTest {

    private DeleteMealController controller;

    @Mock
    private DeleteMealInputBoundary interactor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new DeleteMealController(interactor);
    }

    @Test
    public void testDeleteMealWithValidData() {
        String date = java.time.LocalDate.now().toString();
        String mealType = "BREAKFAST";
        
        controller.execute(date, mealType);
        
        verify(interactor).execute(argThat(inputData -> 
            inputData.getDate().equals(java.time.LocalDate.parse(date)) &&
            inputData.getMealType() == com.mealplanner.entity.MealType.BREAKFAST
        ));
    }

    @Test
    public void testDeleteMealWithInvalidData() {
        String invalidDate = "invalid-date";
        String mealType = "BREAKFAST";
        
        assertThrows(Exception.class, () -> {
            controller.execute(invalidDate, mealType);
        });
    }

    @Test
    public void testDeleteMealWithInvalidMealType() {
        String date = java.time.LocalDate.now().toString();
        String invalidMealType = "INVALID";
        
        assertThrows(Exception.class, () -> {
            controller.execute(date, invalidMealType);
        });
    }
}
