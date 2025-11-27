package com.mealplanner.view;

import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.LoginController;
import com.mealplanner.interface_adapter.view_model.LoginViewModel;
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

public class LoginView extends BorderPane implements PropertyChangeListener {
    private final LoginViewModel loginViewModel;
    private final LoginController loginController;
    private final ViewManagerModel viewManagerModel;

    private TextField usernameField;
    private PasswordField passwordField;
    private Label errorLabel;
    private Label statusLabel;

    public LoginView(LoginViewModel loginViewModel, LoginController loginController, ViewManagerModel viewManagerModel) {
        if (loginViewModel == null) throw new IllegalArgumentException("LoginViewModel cannot be null");
        if (loginController == null) throw new IllegalArgumentException("LoginController cannot be null");
        
        this.loginViewModel = loginViewModel;
        this.loginController = loginController;
        this.viewManagerModel = viewManagerModel;

        this.loginViewModel.addPropertyChangeListener(this);

        setPadding(new Insets(40));
        setStyle("-fx-background-color: white;");

        VBox centerBox = new VBox(20);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setMaxWidth(400);

        // Title
        Label titleLabel = new Label("Login");
        titleLabel.getStyleClass().add("title-label");
        
        // Form
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(15);
        formGrid.setAlignment(Pos.CENTER);

        formGrid.add(new Label("Username:"), 0, 0);
        usernameField = new TextField();
        usernameField.getStyleClass().add("text-field");
        formGrid.add(usernameField, 1, 0);

        formGrid.add(new Label("Password:"), 0, 1);
        passwordField = new PasswordField();
        passwordField.getStyleClass().add("text-field");
        formGrid.add(passwordField, 1, 1);

        // Buttons
        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("modern-button");
        loginButton.setOnAction(e -> performLogin());
        loginButton.setMaxWidth(Double.MAX_VALUE);

        Button signupButton = new Button("Sign Up");
        signupButton.getStyleClass().add("secondary-button");
        signupButton.setOnAction(e -> {
            if (this.viewManagerModel != null) {
                this.viewManagerModel.setActiveView(ViewManager.SIGNUP_VIEW);
            }
        });
        signupButton.setMaxWidth(Double.MAX_VALUE);

        VBox buttonBox = new VBox(10);
        buttonBox.getChildren().addAll(loginButton, signupButton);

        // Status
        statusLabel = new Label("Please enter your username and password");
        statusLabel.setStyle("-fx-text-fill: gray;");
        
        errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        centerBox.getChildren().addAll(titleLabel, formGrid, buttonBox, statusLabel, errorLabel);
        setCenter(centerBox);
    }

    private void performLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.isBlank()) {
            errorLabel.setText("Please enter a username");
            return;
        }
        if (password == null || password.isBlank()) {
            errorLabel.setText("Please enter a password");
            return;
        }

        errorLabel.setText("");
        loginController.execute(username.trim(), password);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(() -> {
            if ("login".equals(evt.getPropertyName())) {
                String error = loginViewModel.getError();
                String loggedInUser = loginViewModel.getLoggedInUser();

                if (error != null && !error.isEmpty()) {
                    errorLabel.setText(error);
                    statusLabel.setText("");
                } else if (loggedInUser != null && !loggedInUser.isEmpty()) {
                    errorLabel.setText("");
                    statusLabel.setText("Welcome, " + loggedInUser + "!");
                    statusLabel.setStyle("-fx-text-fill: green;");
                    // Navigation is handled by LoginPresenter
                }
            }
        });
    }
}

