package com.mealplanner.view;

import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.SignupController;
import com.mealplanner.interface_adapter.view_model.SignupViewModel;
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

public class SignupView extends BorderPane implements PropertyChangeListener {
    public final String viewName = "SignupView";
    private final SignupViewModel signupViewModel;
    private final SignupController signupController;
    private final ViewManagerModel viewManagerModel;

    private Input usernameField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private AlertBanner errorBanner;

    public SignupView(SignupViewModel signupViewModel, SignupController signupController, ViewManagerModel viewManagerModel) {
        if (signupViewModel == null) throw new IllegalArgumentException("SignupViewModel cannot be null");
        if (signupController == null) throw new IllegalArgumentException("SignupController cannot be null");
        
        this.signupViewModel = signupViewModel;
        this.signupController = signupController;
        this.viewManagerModel = viewManagerModel;

        this.signupViewModel.addPropertyChangeListener(this);

        // Root Styles
        getStyleClass().add("root");
        setPadding(new Insets(40));

        VBox centerBox = new VBox(24);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setMaxWidth(400);
        centerBox.getStyleClass().add("card-panel");
        centerBox.setPadding(new Insets(30));

        // Title
        Label titleLabel = new Label("Create Account");
        titleLabel.getStyleClass().add("section-title");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-alignment: center;");
        
        // Error Banner
        errorBanner = new AlertBanner("Error", "", AlertBanner.Type.DESTRUCTIVE);
        errorBanner.setVisible(false);
        errorBanner.setManaged(false);

        // Form
        Form form = new Form();
        form.setPadding(new Insets(0));

        usernameField = new Input();
        usernameField.setPromptText("Choose a username");
        form.addField("Username", usernameField);

        passwordField = new PasswordField();
        passwordField.setPromptText("Choose a password");
        passwordField.getStyleClass().add("input-field");
        form.addField("Password", passwordField);

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your password");
        confirmPasswordField.getStyleClass().add("input-field");
        form.addField("Confirm Password", confirmPasswordField);

        // Buttons
        VBox buttonBox = new VBox(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button signupButton = new Button("Sign Up");
        signupButton.getStyleClass().add("primary-button");
        signupButton.setOnAction(e -> performSignup());
        signupButton.setMaxWidth(Double.MAX_VALUE);

        Button backButton = new Button("Back to Login");
        backButton.getStyleClass().add("ghost-button");
        backButton.setOnAction(e -> {
            if (this.viewManagerModel != null) {
                this.viewManagerModel.setActiveView(ViewManager.LOGIN_VIEW);
            }
        });
        backButton.setMaxWidth(Double.MAX_VALUE);

        buttonBox.getChildren().addAll(signupButton, backButton);

        centerBox.getChildren().addAll(titleLabel, errorBanner, form, buttonBox);
        setCenter(centerBox);
    }

    private void performSignup() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (username == null || username.isBlank()) {
            showError("Please enter a username");
            return;
        }
        if (password == null || password.isBlank()) {
            showError("Please enter a password");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Passwords do not match");
            return;
        }

        hideError();
        signupController.execute(username.trim(), password);
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
            if ("signup".equals(evt.getPropertyName())) {
                String error = signupViewModel.getError();
                
                if (error != null && !error.isEmpty()) {
                    showError(error);
                } else {
                    hideError();
                    // On success, maybe show dialog or auto-redirect? 
                    // Logic here is just clearing error based on original view.
                    // Typically the Presenter would switch views.
                }
            }
        });
    }
}
