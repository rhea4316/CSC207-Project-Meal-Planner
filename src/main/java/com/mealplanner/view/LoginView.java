package com.mealplanner.view;

import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.LoginController;
import com.mealplanner.interface_adapter.view_model.LoginViewModel;
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

public class LoginView extends BorderPane implements PropertyChangeListener {
    public final String viewName = "LoginView";
    private final LoginViewModel loginViewModel;
    private final LoginController loginController;
    private final ViewManagerModel viewManagerModel;

    private Input usernameField;
    private PasswordField passwordField;
    private AlertBanner errorBanner;
    private Validator validator;

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

        // ValidatorFX 초기화 및 검증 설정
        validator = new Validator();
        setupValidations();

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

    /**
     * ValidatorFX를 사용한 로그인 폼 검증 설정
     */
    private void setupValidations() {
        // Username 검증 (로그인은 형식보다는 필수 입력만 체크)
        validator.createCheck()
            .dependsOn("username", usernameField.textProperty())
            .withMethod(context -> {
                String username = context.get("username");
                if (StringUtil.isNullOrEmpty(username)) {
                    context.error("Please enter a username");
                }
                // 로그인 시에는 형식 검증을 하지 않음 (기존 사용자를 찾는 것이므로)
            })
            .decorates(usernameField)
            .immediate();

        // Password 검증 (로그인은 필수 입력만 체크)
        validator.createCheck()
            .dependsOn("password", passwordField.textProperty())
            .withMethod(context -> {
                String password = context.get("password");
                if (StringUtil.isNullOrEmpty(password)) {
                    context.error("Please enter a password");
                }
                // 로그인 시에는 길이 검증을 하지 않음 (기존 사용자 비밀번호 검증은 서버에서 수행)
            })
            .decorates(passwordField)
            .immediate();
    }

    /**
     * 로그인 수행
     * ValidatorFX를 사용하여 폼 검증 후 로그인 컨트롤러 실행
     */
    private void performLogin() {
        // ValidatorFX로 폼 검증
        if (!validator.validate()) {
            // 검증 실패 시 ControlsFX Notification으로 에러 표시
            Notifications.create()
                .title("Validation Error")
                .text("Please fix the errors in the form")
                .showError();
            return;
        }

        // 검증 성공 시 로그인 처리
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        hideError();
        loginController.execute(username, password);
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
