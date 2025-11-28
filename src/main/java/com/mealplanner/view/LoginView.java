package com.mealplanner.view;

import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.LoginController;
import com.mealplanner.interface_adapter.view_model.LoginViewModel;
import com.mealplanner.view.component.AlertBanner;
import com.mealplanner.view.component.Form;
import com.mealplanner.view.component.Input;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class LoginView extends BorderPane implements PropertyChangeListener {
    public final String viewName = "LoginView";
    private final LoginViewModel loginViewModel;
    private final LoginController loginController;
    private final ViewManagerModel viewManagerModel;

    private Input usernameField;
    private PasswordField passwordField;
    private AlertBanner errorBanner;

    public LoginView(LoginViewModel loginViewModel, LoginController loginController, ViewManagerModel viewManagerModel) {
        if (loginViewModel == null) throw new IllegalArgumentException("LoginViewModel cannot be null");
        if (loginController == null) throw new IllegalArgumentException("LoginController cannot be null");
        
        this.loginViewModel = loginViewModel;
        this.loginController = loginController;
        this.viewManagerModel = viewManagerModel;

        this.loginViewModel.addPropertyChangeListener(this);

        // Root Styles
        getStyleClass().add("root");
        setPadding(new Insets(40));

        // Card Container
        VBox centerBox = new VBox(24);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setMaxWidth(400);
        centerBox.getStyleClass().add("card-panel");
        centerBox.setPadding(new Insets(30));

        // Title
        Label titleLabel = new Label("Login");
        titleLabel.getStyleClass().add("section-title");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-alignment: center;");
        
        // Error Banner (Hidden by default)
        errorBanner = new AlertBanner("Error", "", AlertBanner.Type.DESTRUCTIVE);
        errorBanner.setVisible(false);
        errorBanner.setManaged(false);

        // Form
        Form form = new Form();
        form.setPadding(new Insets(0));

        usernameField = new Input();
        usernameField.setPromptText("Enter your username");
        form.addField("Username", usernameField);

        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.getStyleClass().add("input-field"); // Reuse Input style
        form.addField("Password", passwordField);

        // Buttons
        VBox buttonBox = new VBox(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("primary-button");
        loginButton.setOnAction(e -> performLogin());
        loginButton.setMaxWidth(Double.MAX_VALUE);

        Button signupButton = new Button("Sign Up");
        signupButton.getStyleClass().add("ghost-button");
        signupButton.setOnAction(e -> {
            if (this.viewManagerModel != null) {
                this.viewManagerModel.setActiveView(ViewManager.SIGNUP_VIEW);
            }
        });
        signupButton.setMaxWidth(Double.MAX_VALUE);

        buttonBox.getChildren().addAll(loginButton, signupButton);

        centerBox.getChildren().addAll(titleLabel, errorBanner, form, buttonBox);
        setCenter(centerBox);
    }

    private void performLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.isBlank()) {
            showError("Please enter a username");
            return;
        }
        if (password == null || password.isBlank()) {
            showError("Please enter a password");
            return;
        }

        hideError();
        loginController.execute(username.trim(), password);
    }
    
    private void showError(String message) {
        errorBanner.setDescription(message);
        errorBanner.setVisible(true);
        errorBanner.setManaged(true);
    }
    
    private void hideError() {
        errorBanner.setVisible(false);
        errorBanner.setManaged(false);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(() -> {
            if ("login".equals(evt.getPropertyName())) {
                String error = loginViewModel.getError();
                
                if (error != null && !error.isEmpty()) {
                    showError(error);
                } else {
                    hideError();
                }
            }
        });
    }
}
