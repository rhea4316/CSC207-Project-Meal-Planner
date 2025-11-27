package com.mealplanner.use_case.login;

import com.mealplanner.entity.User;
import com.mealplanner.exception.UserNotFoundException;
import com.mealplanner.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for LoginInteractor.
 * Tests user authentication and login validation.
 *
 * Responsible: Mona (primary)
 */
public class LoginInteractorTest {

    private LoginInteractor interactor;

    @Mock
    private LoginDataAccessInterface dataAccess;

    @Mock
    private LoginOutputBoundary presenter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        interactor = new LoginInteractor(dataAccess, presenter);
    }

    @Test
    public void testLoginSuccess() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        String hashedPassword = PasswordUtil.hashPassword(password);
        User user = new User("user123", username, hashedPassword);
        
        when(dataAccess.getUserByUsername(username)).thenReturn(user);

        LoginInputData inputData = new LoginInputData(username, password);

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess).getUserByUsername(username);
        verify(presenter).presentLoginSuccess(argThat(outputData -> 
            outputData.getUsername().equals(username) && 
            outputData.getUserUId().equals("user123")
        ));
        verify(presenter, never()).presentLoginFailure(anyString());
    }

    @Test
    public void testLoginInvalidUsername() {
        // Arrange
        String username = "nonexistent";
        String password = "password123";
        
        when(dataAccess.getUserByUsername(username)).thenThrow(new UserNotFoundException(username));

        LoginInputData inputData = new LoginInputData(username, password);

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess).getUserByUsername(username);
        verify(presenter).presentLoginFailure("User not found");
        verify(presenter, never()).presentLoginSuccess(any());
    }

    @Test
    public void testLoginInvalidPassword() {
        // Arrange
        String username = "testuser";
        String correctPassword = "password123";
        String wrongPassword = "wrongpassword";
        String hashedPassword = PasswordUtil.hashPassword(correctPassword);
        User user = new User("user123", username, hashedPassword);
        
        when(dataAccess.getUserByUsername(username)).thenReturn(user);

        LoginInputData inputData = new LoginInputData(username, wrongPassword);

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess).getUserByUsername(username);
        verify(presenter).presentLoginFailure("Invalid password");
        verify(presenter, never()).presentLoginSuccess(any());
    }

    @Test
    public void testLoginEmptyUsername() {
        // Arrange
        LoginInputData inputData = new LoginInputData("", "password123");

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess, never()).getUserByUsername(anyString());
        verify(presenter).presentLoginFailure("Username cannot be empty");
        verify(presenter, never()).presentLoginSuccess(any());
    }

    @Test
    public void testLoginEmptyPassword() {
        // Arrange
        LoginInputData inputData = new LoginInputData("testuser", "");

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess, never()).getUserByUsername(anyString());
        verify(presenter).presentLoginFailure("Password cannot be empty");
        verify(presenter, never()).presentLoginSuccess(any());
    }

    @Test
    public void testLoginNullUsername() {
        // Arrange
        LoginInputData inputData = new LoginInputData(null, "password123");

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess, never()).getUserByUsername(anyString());
        verify(presenter).presentLoginFailure("Username cannot be empty");
        verify(presenter, never()).presentLoginSuccess(any());
    }

    @Test
    public void testLoginNullPassword() {
        // Arrange
        LoginInputData inputData = new LoginInputData("testuser", null);

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess, never()).getUserByUsername(anyString());
        verify(presenter).presentLoginFailure("Password cannot be empty");
        verify(presenter, never()).presentLoginSuccess(any());
    }

    @Test
    public void testLoginWhitespaceOnlyUsername() {
        // Arrange
        LoginInputData inputData = new LoginInputData("   ", "password123");

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess, never()).getUserByUsername(anyString());
        verify(presenter).presentLoginFailure("Username cannot be empty");
        verify(presenter, never()).presentLoginSuccess(any());
    }

    @Test
    public void testLoginWhitespaceOnlyPassword() {
        // Arrange
        LoginInputData inputData = new LoginInputData("testuser", "   ");

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess, never()).getUserByUsername(anyString());
        verify(presenter).presentLoginFailure("Password cannot be empty");
        verify(presenter, never()).presentLoginSuccess(any());
    }

    @Test
    public void testDataAccessFailure() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        
        when(dataAccess.getUserByUsername(username)).thenThrow(new RuntimeException("Database error"));

        LoginInputData inputData = new LoginInputData(username, password);

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess).getUserByUsername(username);
        verify(presenter).presentLoginFailure("Unexpected error during login");
        verify(presenter, never()).presentLoginSuccess(any());
    }

    @Test
    public void testPasswordSecurity() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        String hashedPassword = PasswordUtil.hashPassword(password);
        User user = new User("user123", username, hashedPassword);
        
        when(dataAccess.getUserByUsername(username)).thenReturn(user);

        LoginInputData inputData = new LoginInputData(username, password);

        // Act
        interactor.execute(inputData);

        // Assert
        // Verify that password stored in user is hashed (contains delimiter)
        assertTrue(user.getPassword().contains(":"));
        assertNotEquals(password, user.getPassword());
        
        // Verify login succeeds with correct password
        verify(presenter).presentLoginSuccess(any());
    }

    @Test
    public void testLoginWithTrimmedUsername() {
        // Arrange
        String username = "  testuser  ";
        String password = "password123";
        String hashedPassword = PasswordUtil.hashPassword(password);
        User user = new User("user123", "testuser", hashedPassword);
        
        when(dataAccess.getUserByUsername("testuser")).thenReturn(user);

        LoginInputData inputData = new LoginInputData(username, password);

        // Act
        interactor.execute(inputData);

        // Assert
        verify(dataAccess).getUserByUsername("testuser"); // Should be trimmed
        verify(presenter).presentLoginSuccess(any());
    }
}
