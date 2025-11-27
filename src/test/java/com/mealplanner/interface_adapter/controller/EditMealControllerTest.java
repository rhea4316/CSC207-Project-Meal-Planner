package com.mealplanner.interface_adapter.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
        // Arrange
        String dateRaw = "2024-01-15";
        String mealTypeRaw = "BREAKFAST";
        String recipeID = "recipe456";

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
    public void testEditMealWithInvalidData() {
        // Arrange
        String invalidDate = "invalid-date";
        String mealTypeRaw = "LUNCH";
        String recipeID = "recipe456";

        // Act & Assert
        try {
            controller.execute(invalidDate, mealTypeRaw, recipeID);
        } catch (Exception e) {
            // Expected: DateTimeParseException or similar
            verify(interactor, never()).execute(any(EditMealInputData.class));
        }
    }

    @Test
    public void testEditMealWithInvalidMealType() {
        // Arrange
        String dateRaw = "2024-01-15";
        String invalidMealType = "INVALID_TYPE";
        String recipeID = "recipe456";

        // Act & Assert
        try {
            controller.execute(dateRaw, invalidMealType, recipeID);
        } catch (Exception e) {
            // Expected: IllegalArgumentException from valueOf
            verify(interactor, never()).execute(any(EditMealInputData.class));
        }
    }

    @Test
    public void testEditMealWithDifferentMealTypes() {
        // Arrange
        String dateRaw = "2024-01-15";
        String recipeID = "recipe456";

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
    public void testEditMealWithLowerCaseMealType() {
        // Arrange
        String dateRaw = "2024-01-15";
        String mealTypeRaw = "dinner";
        String recipeID = "recipe456";

        // Act
        controller.execute(dateRaw, mealTypeRaw, recipeID);

        // Assert
        verify(interactor).execute(argThat(inputData -> 
            inputData.getMealType() == MealType.DINNER &&
            inputData.getRecipe().equals(recipeID)
        ));
    }

    @Test
    public void testEditMealWithDifferentRecipeIds() {
        // Arrange
        String dateRaw = "2024-01-15";
        String mealTypeRaw = "BREAKFAST";
        String recipeID1 = "recipe123";
        String recipeID2 = "recipe789";

        // Act
        controller.execute(dateRaw, mealTypeRaw, recipeID1);
        controller.execute(dateRaw, mealTypeRaw, recipeID2);

        // Assert
        verify(interactor).execute(argThat(inputData -> 
            inputData.getRecipe().equals(recipeID1)
        ));
        verify(interactor).execute(argThat(inputData -> 
            inputData.getRecipe().equals(recipeID2)
        ));
    }

    @Test
    public void testEditMealWithFutureDate() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(14);
        String dateRaw = futureDate.toString();
        String mealTypeRaw = "LUNCH";
        String recipeID = "recipe456";

        // Act
        controller.execute(dateRaw, mealTypeRaw, recipeID);

        // Assert
        verify(interactor).execute(argThat(inputData -> 
            inputData.getDate().equals(futureDate)
        ));
    }
}
