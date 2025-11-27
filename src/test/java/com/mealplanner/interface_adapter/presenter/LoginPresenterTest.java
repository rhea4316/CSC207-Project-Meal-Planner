package com.mealplanner.interface_adapter.presenter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

/**
 * Test class for LoginPresenter.
 * Tests formatting and presentation of login results.
 *
 * Responsible: Mona (primary)
 */
public class LoginPresenterTest {

    private LoginPresenter presenter;

    @Mock
    private com.mealplanner.interface_adapter.view_model.LoginViewModel viewModel;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        presenter = new LoginPresenter(viewModel);
    }

    @Test
    public void testPresentLoginSuccess() {
        com.mealplanner.use_case.login.LoginOutputData outputData = 
            new com.mealplanner.use_case.login.LoginOutputData("user-1", "testuser");
        
        presenter.presentLoginSuccess(outputData);
        
        verify(viewModel).setLoggedInUser("testuser");
        verify(viewModel).setError(null);
        verify(viewModel).firePropertyChanged();
    }

    @Test
    public void testPresentLoginFailure() {
        String errorMessage = "User not found";
        
        presenter.presentLoginFailure(errorMessage);
        
        verify(viewModel).setLoggedInUser(null);
        verify(viewModel).setError(errorMessage);
        verify(viewModel).firePropertyChanged();
    }

    @Test
    public void testPresentValidationError() {
        String errorMessage = "Username cannot be empty";
        
        presenter.presentLoginFailure(errorMessage);
        
        verify(viewModel).setError(errorMessage);
    }
}
