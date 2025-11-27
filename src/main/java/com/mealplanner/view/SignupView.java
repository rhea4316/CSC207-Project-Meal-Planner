package com.mealplanner.view;

import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.SignupController;
import com.mealplanner.interface_adapter.view_model.SignupViewModel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class SignupView extends BorderPane implements PropertyChangeListener {
    private final SignupViewModel signupViewModel;
    private final SignupController signupController;
    private final ViewManagerModel viewManagerModel;

    private TextField usernameField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Label errorLabel;
    private Label statusLabel;

    public SignupView(SignupViewModel signupViewModel, SignupController signupController, ViewManagerModel viewManagerModel) {
        if (signupViewModel == null) throw new IllegalArgumentException("SignupViewModel cannot be null");
        if (signupController == null) throw new IllegalArgumentException("SignupController cannot be null");
        
        this.signupViewModel = signupViewModel;
        this.signupController = signupController;
        // Stored for navigation (e.g., back to login)
        this.viewManagerModel = viewManagerModel;

        this.signupViewModel.addPropertyChangeListener(this);

        setPadding(new Insets(40));
        getStyleClass().add("bg-white");

        VBox centerBox = new VBox(20);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setMaxWidth(400);

        // Title
        Label titleLabel = new Label("Create Account");
        titleLabel.getStyleClass().add("title-label");
        
        // Form
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(15);
        formGrid.setAlignment(Pos.CENTER);

        formGrid.add(new Label("Username:"), 0, 0);
        usernameField = new TextField();
        formGrid.add(usernameField, 1, 0);

        formGrid.add(new Label("Password:"), 0, 1);
        passwordField = new PasswordField();
        formGrid.add(passwordField, 1, 1);

        formGrid.add(new Label("Confirm Password:"), 0, 2);
        confirmPasswordField = new PasswordField();
        formGrid.add(confirmPasswordField, 1, 2);

        // Buttons
        Button signupButton = new Button("Sign Up");
        signupButton.getStyleClass().add("modern-button");
        signupButton.setOnAction(e -> performSignup());
        signupButton.setMaxWidth(Double.MAX_VALUE);

        Button backButton = new Button("Back to Login");
        backButton.getStyleClass().add("secondary-button");
        backButton.setOnAction(e -> {
            if (this.viewManagerModel != null) {
                this.viewManagerModel.setActiveView(ViewManager.LOGIN_VIEW);
            }
        });
        backButton.setMaxWidth(Double.MAX_VALUE);

        VBox buttonBox = new VBox(10);
        buttonBox.getChildren().addAll(signupButton, backButton);

        // Status
        statusLabel = new Label("Please enter your username and password");
        statusLabel.getStyleClass().add("status-label");
        
        errorLabel = new Label("");
        errorLabel.getStyleClass().add("error-label");

        centerBox.getChildren().addAll(titleLabel, formGrid, buttonBox, statusLabel, errorLabel);
        setCenter(centerBox);
    }

    private void performSignup() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (username == null || username.isBlank()) {
            errorLabel.setText("Please enter a username");
            return;
        }
        if (password == null || password.isBlank()) {
            errorLabel.setText("Please enter a password");
            return;
        }
        if (!password.equals(confirm)) {
            errorLabel.setText("Passwords do not match");
            return;
        }

        errorLabel.setText("");
        signupController.execute(username.trim(), password);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(() -> {
            if ("signup".equals(evt.getPropertyName())) {
                String error = signupViewModel.getError();
                String registeredUser = signupViewModel.getRegisteredUser();

                if (error != null && !error.isEmpty()) {
                    errorLabel.setText(error);
                    statusLabel.setText("");
                } else if (registeredUser != null && !registeredUser.isEmpty()) {
                    errorLabel.setText("");
                    statusLabel.setText("Success! Welcome, " + registeredUser + "!");
                    statusLabel.getStyleClass().remove("status-label");
                    statusLabel.getStyleClass().add("success-label");
                    // Optional: Auto-redirect to login or dashboard
                }
            }
        });
    }
}
