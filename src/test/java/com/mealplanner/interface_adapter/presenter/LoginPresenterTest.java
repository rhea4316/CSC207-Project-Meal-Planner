package com.mealplanner.interface_adapter.presenter;

import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.view_model.LoginViewModel;
import com.mealplanner.use_case.login.LoginOutputData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for LoginPresenter.
 * Tests formatting and presentation of login results.
 *
 * Responsible: Mona (primary)
 */
public class LoginPresenterTest {

    private LoginPresenter presenter;
    private LoginViewModel viewModel;
    private ViewManagerModel viewManagerModel;

    @BeforeEach
    public void setUp() {
        viewModel = new LoginViewModel();
        viewManagerModel = new ViewManagerModel();
        presenter = new LoginPresenter(viewModel, viewManagerModel);
    }

    @Test
    public void testPresentLoginSuccess() {
        // Arrange
        String username = "testuser";
        String userId = "user123";
        LoginOutputData outputData = new LoginOutputData(userId, username);

        // Act
        presenter.presentLoginSuccess(outputData);

        // Assert
        assertEquals(username, viewModel.getLoggedInUser());
        assertNull(viewModel.getError());
    }

    @Test
    public void testPresentLoginFailure() {
        // Arrange
        String errorMessage = "Invalid password";

        // Act
        presenter.presentLoginFailure(errorMessage);

        // Assert
        assertNull(viewModel.getLoggedInUser());
        assertEquals(errorMessage, viewModel.getError());
    }

    @Test
    public void testPresentValidationError() {
        // Arrange
        String errorMessage = "Username cannot be empty";

        // Act
        presenter.presentLoginFailure(errorMessage);

        // Assert
        assertNull(viewModel.getLoggedInUser());
        assertEquals(errorMessage, viewModel.getError());
    }

    @Test
    public void testPresentLoginFailureNull() {
        // Act
        presenter.presentLoginFailure(null);

        // Assert
        assertNull(viewModel.getLoggedInUser());
        assertEquals("Login failed", viewModel.getError());
    }

    @Test
    public void testPresentLoginSuccessNull() {
        // Act
        presenter.presentLoginSuccess(null);

        // Assert
        assertEquals("Login data is missing", viewModel.getError());
    }

    @Test
    public void testPresentLoginSuccessClearsError() {
        // Arrange
        viewModel.setError("Previous error");
        LoginOutputData outputData = new LoginOutputData("user123", "testuser");

        // Act
        presenter.presentLoginSuccess(outputData);

        // Assert
        assertEquals("testuser", viewModel.getLoggedInUser());
        assertNull(viewModel.getError());
    }

    @Test
    public void testPresentLoginFailureClearsUser() {
        // Arrange
        viewModel.setLoggedInUser("previoususer");
        String errorMessage = "Login failed";

        // Act
        presenter.presentLoginFailure(errorMessage);

        // Assert
        assertNull(viewModel.getLoggedInUser());
        assertEquals(errorMessage, viewModel.getError());
    }
}
