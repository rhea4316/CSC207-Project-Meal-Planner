package com.mealplanner.view;

import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.view.component.*;
import com.mealplanner.view.util.DialogUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ProfileSettingsView extends BorderPane implements PropertyChangeListener {
    public final String viewName = "ProfileSettingsView";

    public ProfileSettingsView(ViewManagerModel viewManagerModel, String username) {
        getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        // Title
        Label titleLabel = new Label("Profile Settings");
        titleLabel.getStyleClass().add("section-title");
        titleLabel.setStyle("-fx-font-size: 32px;");
        setTop(titleLabel);

        // Main Content Container
        VBox content = new VBox(24);
        content.getStyleClass().add("card-panel");
        content.setMaxWidth(600);
        content.setAlignment(Pos.TOP_LEFT);
        content.setPadding(new Insets(30));

        // 1. Avatar Section
        VBox avatarSection = new VBox(10);
        avatarSection.setAlignment(Pos.CENTER);
        
        Avatar avatar = new Avatar(50, null, "EC"); // 50px radius = 100px size
        Label changePhoto = new Label("Change Photo");
        changePhoto.getStyleClass().add("link-label"); // Assuming link style or just default
        changePhoto.setStyle("-fx-text-fill: -fx-theme-primary; -fx-cursor: hand; -fx-font-size: 14px;");
        
        avatarSection.getChildren().addAll(avatar, changePhoto);

        // 2. Personal Info Form
        Form form = new Form();
        form.setPadding(new Insets(0)); // Reset padding as container has it

        Input userField = new Input(username);
        userField.setEditable(false);
        form.addField("Username", userField);

        Input emailField = new Input("eden@example.com");
        form.addField("Email", emailField);
        
        // 3. Settings Section
        VBox settingsSection = new VBox(15);
        Label settingsTitle = new Label("Preferences");
        settingsTitle.setStyle("-fx-font-weight: 600; -fx-font-size: 16px;");
        
        // Notifications Toggle
        VBox notifBox = new VBox(5);
        Label notifLabel = new Label("Enable Notifications");
        notifLabel.getStyleClass().add("form-label");
        Switch notifSwitch = new Switch();
        notifBox.getChildren().addAll(notifLabel, notifSwitch);
        
        // Two-Factor Auth Mock
        VBox mfaBox = new VBox(5);
        Label mfaLabel = new Label("Two-Factor Authentication (OTP)");
        mfaLabel.getStyleClass().add("form-label");
        InputOTP otpInput = new InputOTP(6);
        mfaBox.getChildren().addAll(mfaLabel, otpInput);
        
        settingsSection.getChildren().addAll(settingsTitle, notifBox, mfaBox);

        // 4. Buttons
        VBox actions = new VBox(10);
        actions.setPadding(new Insets(20, 0, 0, 0));
        
        Button saveBtn = new Button("Save Changes");
        saveBtn.getStyleClass().add("primary-button");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> {
            DialogUtils.showInfoAlert("Changes Saved", "Your profile has been updated successfully.");
        });
        
        Button logoutBtn = new Button("Log Out");
        logoutBtn.getStyleClass().add("secondary-button");
        logoutBtn.setStyle("-fx-text-fill: -fx-theme-destructive;");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> {
            viewManagerModel.setActiveView(ViewManager.LOGIN_VIEW);
        });

        actions.getChildren().addAll(saveBtn, logoutBtn);

        // Add all to content
        content.getChildren().addAll(avatarSection, form, new Separator(), settingsSection, actions);
        
        setCenter(content);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Handle updates
    }
}
