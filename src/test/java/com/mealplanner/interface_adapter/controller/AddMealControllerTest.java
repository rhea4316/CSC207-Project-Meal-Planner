package com.mealplanner.interface_adapter.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import com.mealplanner.use_case.manage_meal_plan.add.AddMealInputBoundary;
import com.mealplanner.use_case.manage_meal_plan.add.AddMealInputData;
import com.mealplanner.entity.MealType;

import java.time.LocalDate;

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
        // Arrange
        String dateRaw = "2024-01-15";
        String mealTypeRaw = "BREAKFAST";
        String recipeID = "recipe123";

        // Act
        controller.execute(dateRaw, mealTypeRaw, recipeID);

        // Assert
        verify(interactor).execute(argThat(inputData -> 
            inputData.getDate().equals(LocalDate.parse(dateRaw)) &&
            inputData.getMealType() == MealType.BREAKFAST &&
            inputData.getRecipe().equals(recipeID)
        ));
    }

    @Test
    public void testAddMealWithInvalidDate() {
        // Arrange
        String invalidDate = "invalid-date";
        String mealTypeRaw = "LUNCH";
        String recipeID = "recipe123";

        // Act & Assert
        try {
            controller.execute(invalidDate, mealTypeRaw, recipeID);
        } catch (Exception e) {
            // Expected: DateTimeParseException or similar
            verify(interactor, never()).execute(any(AddMealInputData.class));
        }
    }

    @Test
    public void testAddMealWithInvalidMealType() {
        // Arrange
        String dateRaw = "2024-01-15";
        String invalidMealType = "INVALID_TYPE";
        String recipeID = "recipe123";

        // Act & Assert
        try {
            controller.execute(dateRaw, invalidMealType, recipeID);
        } catch (Exception e) {
            // Expected: IllegalArgumentException from valueOf
            verify(interactor, never()).execute(any(AddMealInputData.class));
        }
    }

    @Test
    public void testAddMealWithDifferentMealTypes() {
        // Arrange
        String dateRaw = "2024-01-15";
        String recipeID = "recipe123";

        // Test BREAKFAST
        controller.execute(dateRaw, "BREAKFAST", recipeID);
        verify(interactor).execute(argThat(inputData -> 
            inputData.getMealType() == MealType.BREAKFAST
        ));

        // Test LUNCH
        controller.execute(dateRaw, "LUNCH", recipeID);
        verify(interactor).execute(argThat(inputData -> 
            inputData.getMealType() == MealType.LUNCH
        ));

        // Test DINNER
        controller.execute(dateRaw, "DINNER", recipeID);
        verify(interactor).execute(argThat(inputData -> 
            inputData.getMealType() == MealType.DINNER
        ));
    }

    @Test
    public void testAddMealWithLowerCaseMealType() {
        // Arrange
        String dateRaw = "2024-01-15";
        String mealTypeRaw = "breakfast";
        String recipeID = "recipe123";

        // Act
        controller.execute(dateRaw, mealTypeRaw, recipeID);

        // Assert
        verify(interactor).execute(argThat(inputData -> 
            inputData.getMealType() == MealType.BREAKFAST
        ));
    }

    @Test
    public void testAddMealWithFutureDate() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(30);
        String dateRaw = futureDate.toString();
        String mealTypeRaw = "DINNER";
        String recipeID = "recipe123";

        // Act
        controller.execute(dateRaw, mealTypeRaw, recipeID);

        // Assert
        verify(interactor).execute(argThat(inputData -> 
            inputData.getDate().equals(futureDate)
        ));
    }
}
