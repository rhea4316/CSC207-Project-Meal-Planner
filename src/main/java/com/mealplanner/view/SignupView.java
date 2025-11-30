package com.mealplanner.view;

import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.SignupController;
import com.mealplanner.interface_adapter.view_model.SignupViewModel;
import com.mealplanner.util.StringUtil;
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
import net.synedra.validatorfx.Validator;
import org.controlsfx.control.Notifications;

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
    private Validator validator;

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

        // ValidatorFX 초기화 및 검증 설정
        validator = new Validator();
        setupValidations();

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

    /**
     * ValidatorFX를 사용한 회원가입 폼 검증 설정
     */
    private void setupValidations() {
        // Username 검증
        validator.createCheck()
            .dependsOn("username", usernameField.textProperty())
            .withMethod(context -> {
                String username = context.get("username");
                if (StringUtil.isNullOrEmpty(username)) {
                    context.error("Please enter a username");
                } else if (!StringUtil.isValidUsername(username)) {
                    context.error("Username must be 3-20 characters (letters, numbers, underscore, hyphen only)");
                }
            })
            .decorates(usernameField)
            .immediate();

        // Password 검증
        validator.createCheck()
            .dependsOn("password", passwordField.textProperty())
            .withMethod(context -> {
                String password = context.get("password");
                if (StringUtil.isNullOrEmpty(password)) {
                    context.error("Please enter a password");
                } else if (password.length() < 6) {
                    context.error("Password must be at least 6 characters");
                }
            })
            .decorates(passwordField)
            .immediate();

        // Confirm Password 검증
        validator.createCheck()
            .dependsOn("password", passwordField.textProperty())
            .dependsOn("confirmPassword", confirmPasswordField.textProperty())
            .withMethod(context -> {
                String password = context.get("password");
                String confirmPassword = context.get("confirmPassword");
                if (StringUtil.isNullOrEmpty(confirmPassword)) {
                    context.error("Please confirm your password");
                } else if (!StringUtil.isNullOrEmpty(password) && !password.equals(confirmPassword)) {
                    context.error("Passwords do not match");
                }
            })
            .decorates(confirmPasswordField)
            .immediate();
    }

    /**
     * 회원가입 수행
     * ValidatorFX를 사용하여 폼 검증 후 회원가입 컨트롤러 실행
     */
    private void performSignup() {
        // ValidatorFX로 폼 검증
        if (!validator.validate()) {
            // 검증 실패 시 ControlsFX Notification으로 에러 표시
            Notifications.create()
                .title("Validation Error")
                .text("Please fix the errors in the form")
                .showError();
            return;
        }

        // 검증 성공 시 회원가입 처리
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        hideError();
        signupController.execute(username, password);
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
