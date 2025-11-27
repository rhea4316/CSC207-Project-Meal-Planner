package com.mealplanner.interface_adapter.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
        // Arrange
        String dateRaw = "2024-01-15";
        String mealTypeRaw = "BREAKFAST";

        // Act
        controller.execute(dateRaw, mealTypeRaw);

        // Assert
        verify(interactor).execute(argThat(inputData -> 
            inputData.getDate().equals(LocalDate.parse(dateRaw)) &&
            inputData.getMealType() == MealType.BREAKFAST
        ));
    }

    @Test
    public void testDeleteMealWithInvalidData() {
        // Arrange
        String invalidDate = "invalid-date";
        String mealTypeRaw = "LUNCH";

        // Act & Assert
        try {
            controller.execute(invalidDate, mealTypeRaw);
        } catch (Exception e) {
            // Expected: DateTimeParseException or similar
            verify(interactor, never()).execute(any(DeleteMealInputData.class));
        }
    }

    @Test
    public void testDeleteMealWithInvalidMealType() {
        // Arrange
        String dateRaw = "2024-01-15";
        String invalidMealType = "INVALID_TYPE";

        // Act & Assert
        try {
            controller.execute(dateRaw, invalidMealType);
        } catch (Exception e) {
            // Expected: IllegalArgumentException from valueOf
            verify(interactor, never()).execute(any(DeleteMealInputData.class));
        }
    }

    @Test
    public void testDeleteMealWithDifferentMealTypes() {
        // Arrange
        String dateRaw = "2024-01-15";

        // Test BREAKFAST
        controller.execute(dateRaw, "BREAKFAST");
        verify(interactor).execute(argThat(inputData -> 
            inputData.getMealType() == MealType.BREAKFAST
        ));

        // Test LUNCH
        controller.execute(dateRaw, "LUNCH");
        verify(interactor).execute(argThat(inputData -> 
            inputData.getMealType() == MealType.LUNCH
        ));

        // Test DINNER
        controller.execute(dateRaw, "DINNER");
        verify(interactor).execute(argThat(inputData -> 
            inputData.getMealType() == MealType.DINNER
        ));
    }

    @Test
    public void testDeleteMealWithLowerCaseMealType() {
        // Arrange
        String dateRaw = "2024-01-15";
        String mealTypeRaw = "lunch";

        // Act
        controller.execute(dateRaw, mealTypeRaw);

        // Assert
        verify(interactor).execute(argThat(inputData -> 
            inputData.getMealType() == MealType.LUNCH
        ));
    }

    @Test
    public void testDeleteMealWithPastDate() {
        // Arrange
        LocalDate pastDate = LocalDate.now().minusDays(7);
        String dateRaw = pastDate.toString();
        String mealTypeRaw = "DINNER";

        // Act
        controller.execute(dateRaw, mealTypeRaw);

        // Assert
        verify(interactor).execute(argThat(inputData -> 
            inputData.getDate().equals(pastDate)
        ));
    }
}
