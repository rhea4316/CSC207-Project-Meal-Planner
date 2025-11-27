package com.mealplanner.view;

import com.mealplanner.interface_adapter.controller.LoginController;
import com.mealplanner.interface_adapter.view_model.LoginViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

// Swing view for user login - displays username input and login button.
// Responsible: Mona (functionality), Everyone (GUI implementation)

public class LoginView extends JPanel implements PropertyChangeListener, ActionListener {

    private final LoginViewModel loginViewModel;
    private final LoginController loginController;
    private final com.mealplanner.interface_adapter.ViewManagerModel viewManagerModel;

    private JTextField usernameTextField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton signupButton;
    private JLabel errorLabel;
    private JLabel statusLabel;

    public LoginView(LoginViewModel loginViewModel, LoginController loginController) {
        this(loginViewModel, loginController, null);
    }

    public LoginView(LoginViewModel loginViewModel, LoginController loginController, 
                     com.mealplanner.interface_adapter.ViewManagerModel viewManagerModel) {
        if (loginViewModel == null) {
            throw new IllegalArgumentException("LoginViewModel cannot be null");
        }
        if (loginController == null) {
            throw new IllegalArgumentException("LoginController cannot be null");
        }

        this.loginViewModel = loginViewModel;
        this.loginController = loginController;
        this.viewManagerModel = viewManagerModel;

        this.loginViewModel.addPropertyChangeListener(this);

        initializeComponents();
        layoutComponents();
    }

    private void initializeComponents() {
        usernameTextField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");
        loginButton.addActionListener(this);
        loginButton.setActionCommand("login");

        signupButton = new JButton("Sign Up");
        signupButton.addActionListener(this);
        signupButton.setActionCommand("signup");

        errorLabel = new JLabel();
        errorLabel.setForeground(Color.RED);

        statusLabel = new JLabel("Please enter your username and password");
        statusLabel.setForeground(Color.GRAY);
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);

        // Input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 2, 10, 10));
        inputPanel.add(new JLabel("Username:"));
        inputPanel.add(usernameTextField);
        inputPanel.add(new JLabel("Password:"));
        inputPanel.add(passwordField);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(loginButton);
        buttonPanel.add(signupButton);
        inputPanel.add(new JLabel(""));
        inputPanel.add(buttonPanel);

        // Status panel
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.add(statusLabel);
        statusPanel.add(errorLabel);

        // Add all panels to main panel
        add(titlePanel, BorderLayout.NORTH);
        add(inputPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("login".equals(e.getActionCommand())) {
            performLogin();
        } else if ("signup".equals(e.getActionCommand())) {
            if (viewManagerModel != null) {
                viewManagerModel.setActiveView("SignupView");
            }
        }
    }

    private void performLogin() {
        String username = usernameTextField.getText();
        String password = new String(passwordField.getPassword());

        if (username == null || username.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a username",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a password",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        loginController.execute(username.trim(), password);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("login".equals(evt.getPropertyName())) {
            String error = loginViewModel.getError();
            String loggedInUser = loginViewModel.getLoggedInUser();

            if (error != null && !error.isEmpty()) {
                errorLabel.setText(error);
                statusLabel.setText("");
            } else if (loggedInUser != null && !loggedInUser.isEmpty()) {
                errorLabel.setText("");
                statusLabel.setText("Welcome, " + loggedInUser + "!");
                statusLabel.setForeground(Color.GREEN);
                JOptionPane.showMessageDialog(this,
                        "Login successful! Welcome, " + loggedInUser,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    public JTextField getUsernameTextField() {
        return usernameTextField;
    }

    public JButton getLoginButton() {
        return loginButton;
    }
}
