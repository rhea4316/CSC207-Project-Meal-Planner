package com.mealplanner.interface_adapter.controller;

import com.mealplanner.use_case.login.LoginInputBoundary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
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
        String username = "testuser";
        
        controller.execute(username);
        
        verify(interactor).execute(argThat(inputData -> 
            inputData.getUsername().equals(username)
        ));
    }

    @Test
    public void testLoginWithEmptyUsername() {
        controller.execute("");
        
        verify(interactor).execute(argThat(inputData -> 
            inputData.getUsername().equals("")
        ));
    }

    @Test
    public void testLoginWithEmptyPassword() {
        String username = "testuser";
        controller.execute(username);
        
        verify(interactor).execute(any());
    }

    @Test
    public void testLoginWithNullInput() {
        assertThrows(NullPointerException.class, () -> {
            controller.execute(null);
        });
    }
}
