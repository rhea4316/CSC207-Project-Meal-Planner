package com.mealplanner.interface_adapter.controller;

import com.mealplanner.use_case.login.LoginInputBoundary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

/**
 * Test class for LoginController.
 * Tests controller input validation and interactor invocation.
 *
 * Responsible: Mona (primary)
 */
public class LoginControllerTest {

    private LoginController controller;

    @Mock
    private LoginInputBoundary interactor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new LoginController(interactor);
    }

    @Test
    public void testLoginWithValidCredentials() {
        // Arrange
        String username = "testuser";
        String password = "password123";

        // Act
        controller.execute(username, password);

        // Assert
        verify(interactor).execute(argThat(inputData -> 
            inputData.getUsername().equals(username) &&
            inputData.getPassword().equals(password)
        ));
    }

    @Test
    public void testLoginWithEmptyUsername() {
        // Arrange
        String username = "";
        String password = "password123";

        // Act
        controller.execute(username, password);

        // Assert
        verify(interactor).execute(argThat(inputData -> 
            inputData.getUsername().equals(username) &&
            inputData.getPassword().equals(password)
        ));
    }

    @Test
    public void testLoginWithEmptyPassword() {
        // Arrange
        String username = "testuser";
        String password = "";

        // Act
        controller.execute(username, password);

        // Assert
        verify(interactor).execute(argThat(inputData -> 
            inputData.getUsername().equals(username) &&
            inputData.getPassword().equals(password)
        ));
    }

    @Test
    public void testLoginWithNullInput() {
        // Arrange
        String username = null;
        String password = "password123";

        // Act
        controller.execute(username, password);

        // Assert
        verify(interactor).execute(argThat(inputData -> 
            inputData.getUsername() == null &&
            inputData.getPassword().equals(password)
        ));
    }

    @Test
    public void testLoginWithNullPassword() {
        // Arrange
        String username = "testuser";
        String password = null;

        // Act
        controller.execute(username, password);

        // Assert
        verify(interactor).execute(argThat(inputData -> 
            inputData.getUsername().equals(username) &&
            inputData.getPassword() == null
        ));
    }
}
