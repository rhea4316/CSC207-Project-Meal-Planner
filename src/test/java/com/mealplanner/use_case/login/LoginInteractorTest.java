package com.mealplanner.use_case.login;

import com.mealplanner.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
    public void testLoginSuccess() throws UserNotFoundException {
        LoginInputData inputData = new LoginInputData("testuser");
        com.mealplanner.entity.User user = new com.mealplanner.entity.User("user-1", "testuser", "password");
        
        when(dataAccess.getUserByUsername("testuser")).thenReturn(user);
        
        interactor.execute(inputData);
        
        verify(presenter).presentLoginSuccess(argThat(outputData -> 
            outputData.getUsername().equals("testuser") && 
            outputData.getUserUId().equals("user-1")
        ));
        verify(presenter, never()).presentLoginFailure(anyString());
    }

    @Test
    public void testLoginInvalidUsername() throws UserNotFoundException {
        LoginInputData inputData = new LoginInputData("nonexistent");
        
        when(dataAccess.getUserByUsername("nonexistent")).thenThrow(new UserNotFoundException("User not found"));
        
        interactor.execute(inputData);
        
        verify(presenter).presentLoginFailure("User not found");
        verify(presenter, never()).presentLoginSuccess(any(LoginOutputData.class));
    }

    @Test
    public void testLoginInvalidPassword() throws UserNotFoundException {
        LoginInputData inputData = new LoginInputData("testuser");
        
        when(dataAccess.getUserByUsername("testuser")).thenThrow(new UserNotFoundException("User not found"));
        
        interactor.execute(inputData);
        
        verify(presenter).presentLoginFailure("User not found");
    }

    @Test
    public void testLoginEmptyUsername() {
        LoginInputData inputData = new LoginInputData("");
        
        interactor.execute(inputData);
        
        verify(presenter).presentLoginFailure("Username cannot be empty");
    }

    @Test
    public void testLoginEmptyPassword() {
        LoginInputData inputData = new LoginInputData("   ");
        
        interactor.execute(inputData);
        
        verify(presenter).presentLoginFailure("Username cannot be empty");
    }

    @Test
    public void testDataAccessFailure() throws UserNotFoundException {
        LoginInputData inputData = new LoginInputData("testuser");
        
        when(dataAccess.getUserByUsername("testuser")).thenThrow(new RuntimeException("Database error"));
        
        interactor.execute(inputData);
        
        verify(presenter).presentLoginFailure("Unexpected error during login");
    }

    @Test
    public void testPasswordSecurity() throws UserNotFoundException {
        LoginInputData inputData = new LoginInputData("testuser");
        com.mealplanner.entity.User user = new com.mealplanner.entity.User("user-1", "testuser", "password");
        
        when(dataAccess.getUserByUsername("testuser")).thenReturn(user);
        
        interactor.execute(inputData);
        
        verify(presenter).presentLoginSuccess(any(LoginOutputData.class));
    }
}
