package com.mealplanner.view;

import com.mealplanner.interface_adapter.controller.SignupController;
import com.mealplanner.interface_adapter.view_model.SignupViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

// Swing view for user registration - displays username and password input and signup button.
// Responsible: Everyone

public class SignupView extends JPanel implements PropertyChangeListener, ActionListener {
    private final SignupViewModel signupViewModel;
    private final SignupController signupController;
    private final com.mealplanner.interface_adapter.ViewManagerModel viewManagerModel;

    private JTextField usernameTextField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton signupButton;
    private JButton backToLoginButton;
    private JLabel errorLabel;
    private JLabel statusLabel;

    public SignupView(SignupViewModel signupViewModel, SignupController signupController, 
                     com.mealplanner.interface_adapter.ViewManagerModel viewManagerModel) {
        if (signupViewModel == null) {
            throw new IllegalArgumentException("SignupViewModel cannot be null");
        }
        if (signupController == null) {
            throw new IllegalArgumentException("SignupController cannot be null");
        }

        this.signupViewModel = signupViewModel;
        this.signupController = signupController;
        this.viewManagerModel = viewManagerModel;

        this.signupViewModel.addPropertyChangeListener(this);

        initializeComponents();
        layoutComponents();
    }

    private void initializeComponents() {
        usernameTextField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        
        signupButton = new JButton("Sign Up");
        signupButton.addActionListener(this);
        signupButton.setActionCommand("signup");

        backToLoginButton = new JButton("Back to Login");
        backToLoginButton.addActionListener(this);
        backToLoginButton.setActionCommand("back");

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
        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);

        // Input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(4, 2, 10, 10));
        inputPanel.add(new JLabel("Username:"));
        inputPanel.add(usernameTextField);
        inputPanel.add(new JLabel("Password:"));
        inputPanel.add(passwordField);
        inputPanel.add(new JLabel("Confirm Password:"));
        inputPanel.add(confirmPasswordField);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(signupButton);
        buttonPanel.add(backToLoginButton);
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
        if ("signup".equals(e.getActionCommand())) {
            performSignup();
        } else if ("back".equals(e.getActionCommand())) {
            if (viewManagerModel != null) {
                viewManagerModel.setActiveView("LoginView");
            }
        }
    }

    private void performSignup() {
        String username = usernameTextField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

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

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                    "Passwords do not match",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        signupController.execute(username.trim(), password);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("signup".equals(evt.getPropertyName())) {
            String error = signupViewModel.getError();
            String registeredUser = signupViewModel.getRegisteredUser();

            if (error != null && !error.isEmpty()) {
                errorLabel.setText(error);
                statusLabel.setText("");
            } else if (registeredUser != null && !registeredUser.isEmpty()) {
                errorLabel.setText("");
                statusLabel.setText("Account created successfully! Welcome, " + registeredUser + "!");
                statusLabel.setForeground(Color.GREEN);
                JOptionPane.showMessageDialog(this,
                        "Account created successfully! Welcome, " + registeredUser,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    public JTextField getUsernameTextField() {
        return usernameTextField;
    }

    public JButton getSignupButton() {
        return signupButton;
    }
}

