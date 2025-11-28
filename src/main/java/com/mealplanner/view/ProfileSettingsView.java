package com.mealplanner.view;

import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.view.util.DialogUtils;
import com.mealplanner.view.util.SvgIconLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ProfileSettingsView extends BorderPane implements PropertyChangeListener {
    public final String viewName = "ProfileSettingsView";
    private final ViewManagerModel viewManagerModel;

    public ProfileSettingsView(ViewManagerModel viewManagerModel, String username) {
        this.viewManagerModel = viewManagerModel;
        
        getStyleClass().add("root");
        setStyle("-fx-background-color: #F5F7FA;");
        setPadding(new Insets(30, 40, 30, 40));

        // Main Layout: Header + Scrollable Content
        VBox mainContainer = new VBox(24);
        mainContainer.setAlignment(Pos.TOP_LEFT);

        // 1. Header
        VBox header = new VBox(4);
        Label titleLabel = new Label("Profile & Settings");
        titleLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-weight: bold; -fx-font-size: 24px; -fx-text-fill: #1A1A1A;");
        Label subTitleLabel = new Label("Manage your account and preferences");
        subTitleLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-text-fill: #888888;");
        header.getChildren().addAll(titleLabel, subTitleLabel);
        
        mainContainer.getChildren().add(header);

        // 2. Scrollable Content Area
        VBox content = new VBox(24);
        content.setAlignment(Pos.TOP_LEFT);
        
        content.getChildren().add(createProfileCard(username));
        content.getChildren().add(createNutritionGoalsCard());
        content.getChildren().add(createDataManagementCard());
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        mainContainer.getChildren().add(scrollPane);
        
        setCenter(mainContainer);
    }

    // --- Section 1: User Profile Card ---
    private VBox createProfileCard(String username) {
        VBox card = createCard();
        
        HBox layout = new HBox(24);
        layout.setAlignment(Pos.CENTER_LEFT);
        
        // A. Avatar (Custom Squircle shape)
        StackPane avatarContainer = new StackPane();
        Rectangle squircle = new Rectangle(100, 100);
        squircle.setArcWidth(40); // 20px radius visual (doubled for arc)
        squircle.setArcHeight(40);
        
        Stop[] stops = new Stop[] { new Stop(0, Color.web("#76FF03")), new Stop(1, Color.web("#64DD17")) };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        squircle.setFill(gradient);
        
        Label initial = new Label("EC");
        initial.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 32px;");
        
        avatarContainer.getChildren().addAll(squircle, initial);
        
        // B. Info Fields
        VBox infoBox = new VBox(16);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        
        // Username
        VBox userRow = new VBox(4);
        Label userLabel = new Label("Username");
        userLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");
        Label userValue = new Label(username != null ? username : "Eden Chang");
        userValue.setStyle("-fx-text-fill: #1A1A1A; -fx-font-weight: bold; -fx-font-size: 18px;");
        userRow.getChildren().addAll(userLabel, userValue);
        
        // Member Since
        VBox memberRow = new VBox(4);
        Label memberLabel = new Label("Member Since");
        memberLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");
        HBox memberValBox = new HBox(8);
        memberValBox.setAlignment(Pos.CENTER_LEFT);
        Node calendarIcon = SvgIconLoader.loadIcon("/svg/calendar.svg", 14, Color.web("#888888"));
        Label memberValue = new Label("November 2024");
        memberValue.setStyle("-fx-text-fill: #444444; -fx-font-size: 14px;");
        memberValBox.getChildren().addAll(calendarIcon != null ? calendarIcon : new Label(), memberValue);
        memberRow.getChildren().addAll(memberLabel, memberValBox);
        
        // Password Change
        VBox passRow = new VBox(4);
        Label passLabel = new Label("Password");
        passLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");
        
        Button changePassBtn = new Button("Change Password");
        changePassBtn.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #444444; -fx-background-radius: 8px; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand;");
        Node lockIcon = SvgIconLoader.loadIcon("/svg/lock.svg", 14, Color.web("#444444"));
        if (lockIcon != null) {
            changePassBtn.setGraphic(lockIcon);
            changePassBtn.setGraphicTextGap(8);
        }
        changePassBtn.setOnMouseEntered(e -> changePassBtn.setStyle("-fx-background-color: #EEEEEE; -fx-text-fill: #444444; -fx-background-radius: 8px; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand;"));
        changePassBtn.setOnMouseExited(e -> changePassBtn.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #444444; -fx-background-radius: 8px; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand;"));
        
        passRow.getChildren().addAll(passLabel, changePassBtn);
        
        infoBox.getChildren().addAll(userRow, memberRow, passRow);
        
        // C. Edit Icon
        VBox editBox = new VBox();
        editBox.setAlignment(Pos.TOP_RIGHT);
        Node editIcon = SvgIconLoader.loadIcon("/svg/pencil-fill.svg", 20, Color.web("#4CAF50")); // Green pen
        editBox.getChildren().add(editIcon);
        
        layout.getChildren().addAll(avatarContainer, infoBox, editBox);
        card.getChildren().add(layout);
        
        return card;
    }

    // --- Section 2: Nutrition Goals Card ---
    private VBox createNutritionGoalsCard() {
        VBox card = createCard();
        
        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBg = new StackPane();
        Circle bgCircle = new Circle(16, Color.web("#64DD17"));
        Node targetIcon = SvgIconLoader.loadIcon("/svg/cross-small.svg", 20, Color.WHITE); // Target/Crosshair fallback
        iconBg.getChildren().addAll(bgCircle, targetIcon != null ? targetIcon : new Label());
        
        VBox titleBox = new VBox(2);
        Label title = new Label("Nutrition Goals");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1A1A1A;");
        Label subTitle = new Label("Set your daily nutrition targets");
        subTitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");
        titleBox.getChildren().addAll(title, subTitle);
        
        header.getChildren().addAll(iconBg, titleBox);
        
        // A. Daily Calories Slider
        VBox calSliderBox = new VBox(10);
        calSliderBox.setPadding(new Insets(20, 0, 20, 0));
        
        HBox calLabels = new HBox();
        Label calTitle = new Label("Daily Calories");
        calTitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #444444;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label calValue = new Label("2000 kcal");
        calValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #4CAF50;");
        calLabels.getChildren().addAll(calTitle, spacer, calValue);
        
        Slider mainSlider = createCustomSlider("#64DD17", 1000, 4000, 2000);
        
        HBox rangeLabels = new HBox();
        Label minLabel = new Label("1000");
        Label maxLabel = new Label("4000");
        minLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888888;");
        maxLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888888;");
        Region rangeSpacer = new Region();
        HBox.setHgrow(rangeSpacer, Priority.ALWAYS);
        rangeLabels.getChildren().addAll(minLabel, rangeSpacer, maxLabel);
        
        calSliderBox.getChildren().addAll(calLabels, mainSlider, rangeLabels);
        
        // B. Macro Sliders
        HBox macrosBox = new HBox(20);
        macrosBox.getChildren().add(createMacroSlider("Protein", "150g", "#448AFF", 50, 300, 150));
        macrosBox.getChildren().add(createMacroSlider("Carbs", "250g", "#FFA726", 50, 500, 250));
        macrosBox.getChildren().add(createMacroSlider("Fat", "65g", "#FF4081", 20, 150, 65));
        
        // Save Button
        Button saveBtn = new Button("Save Nutrition Goals");
        saveBtn.setStyle("-fx-background-color: #00C853; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 8px; -fx-padding: 10 20; -fx-cursor: hand;");
        Node saveIcon = SvgIconLoader.loadIcon("/svg/book-fill.svg", 16, Color.WHITE); // Floppy/Save fallback
        if (saveIcon != null) {
            saveBtn.setGraphic(saveIcon);
            saveBtn.setGraphicTextGap(8);
        }
        saveBtn.setOnAction(e -> DialogUtils.showInfoAlert("Success", "Nutrition goals updated!"));
        
        VBox.setMargin(saveBtn, new Insets(20, 0, 0, 0));
        
        card.getChildren().addAll(header, calSliderBox, macrosBox, saveBtn);
        return card;
    }
    
    private VBox createMacroSlider(String label, String value, String colorHex, double min, double max, double current) {
        VBox container = new VBox(8);
        HBox.setHgrow(container, Priority.ALWAYS);
        
        HBox header = new HBox(6);
        header.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(4, Color.web(colorHex));
        Label name = new Label(label);
        name.setStyle("-fx-font-size: 13px; -fx-text-fill: #444444;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label val = new Label(value);
        val.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: " + colorHex + ";");
        header.getChildren().addAll(dot, name, spacer, val);
        
        Slider slider = createCustomSlider(colorHex, min, max, current);
        
        container.getChildren().addAll(header, slider);
        return container;
    }
    
    private Slider createCustomSlider(String colorHex, double min, double max, double val) {
        Slider slider = new Slider(min, max, val);
        // Styling via inline CSS for colors isn't fully supported for sub-structures (track/thumb) directly without lookup.
        // We will use style class and update CSS or set style on skin components if possible.
        // Best approach for dynamic colors in JavaFX: bind style property or use specific IDs.
        // Let's assume we update style.css to handle a generic custom-slider class, 
        // but for specific colors, we might need a workaround or simple inline lookup if JavaFX version allows.
        // Workaround: We'll use a unique ID or style class suffix and add to CSS, OR use `setStyle` on the thumb/track after skin is loaded.
        // Simplified: Just styling the control accent color if supported, or using basic styling.
        
        // Modern JavaFX themes often use -fx-accent. Let's try setting that.
        slider.setStyle("-fx-accent: " + colorHex + "; -fx-control-inner-background: #E0E0E0;");
        // Note: Default JavaFX slider doesn't use -fx-accent for thumb color directly in standard Modena.
        // We will add a CSS rule that maps -fx-base or similar to the thumb.
        // Or better, we use a helper to apply style classes.
        
        slider.getStyleClass().add("custom-slider");
        // We can embed the color into the node's user data or ID to pick up in CSS if we wrote complex CSS, 
        // but for this specific task, let's try inline style on the thumb via lookup (after scene load) or just rely on -fx-base/accent if customized in CSS.
        
        // Force the specific color using a direct style that CSS can pick up variables from
        slider.setStyle("-fx-color-slider: " + colorHex + ";"); 
        
        return slider;
    }

    // --- Section 3: Data Management Card ---
    private VBox createDataManagementCard() {
        VBox card = createCard();
        
        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBg = new StackPane();
        Rectangle bgRect = new Rectangle(32, 32);
        bgRect.setArcWidth(12); bgRect.setArcHeight(12);
        bgRect.setFill(Color.web("#FF5252")); // Red/Coral
        Node shieldIcon = SvgIconLoader.loadIcon("/svg/lock-fill.svg", 18, Color.WHITE); // Shield fallback
        iconBg.getChildren().addAll(bgRect, shieldIcon != null ? shieldIcon : new Label());
        
        VBox titleBox = new VBox(2);
        Label title = new Label("Data Management");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1A1A1A;");
        Label subTitle = new Label("Manage your saved data");
        subTitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");
        titleBox.getChildren().addAll(title, subTitle);
        
        header.getChildren().addAll(iconBg, titleBox);
        
        VBox actionList = new VBox(15);
        actionList.setPadding(new Insets(20, 0, 20, 0));
        
        // Row 1
        actionList.getChildren().add(createActionRow("Reset Meal Plan", "Clear your entire weekly meal schedule", "Reset", "/svg/trash.svg"));
        // Row 2
        actionList.getChildren().add(createActionRow("Clear Saved Recipes", "Remove all recipes from your cookbook", "Clear", "/svg/trash.svg"));
        
        // Logout
        Button logoutBtn = new Button("Log Out");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setPrefHeight(45);
        logoutBtn.setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #D32F2F; -fx-border-color: #FFCDD2; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-font-weight: 600; -fx-cursor: hand;");
        Node logoutIcon = SvgIconLoader.loadIcon("/svg/arrow-right.svg", 16, Color.web("#D32F2F")); // Exit icon fallback
        if (logoutIcon != null) {
            logoutBtn.setGraphic(logoutIcon);
            logoutBtn.setGraphicTextGap(8);
        }
        logoutBtn.setOnAction(e -> viewManagerModel.setActiveView(ViewManager.LOGIN_VIEW));
        
        card.getChildren().addAll(header, actionList, logoutBtn);
        return card;
    }
    
    private HBox createActionRow(String title, String desc, String btnText, String iconPath) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(15));
        row.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 8px;");
        
        VBox textBox = new VBox(4);
        Label t = new Label(title);
        t.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1A1A1A;");
        Label d = new Label(desc);
        d.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");
        textBox.getChildren().addAll(t, d);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button btn = new Button(btnText);
        btn.setStyle("-fx-background-color: white; -fx-text-fill: #444444; -fx-border-color: #E0E0E0; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 6 12; -fx-font-size: 13px; -fx-cursor: hand;");
        Node icon = SvgIconLoader.loadIcon(iconPath, 14, Color.web("#444444"));
        if (icon != null) {
            btn.setGraphic(icon);
            btn.setGraphicTextGap(6);
        }
        
        row.getChildren().addAll(textBox, spacer, btn);
        return row;
    }

    private VBox createCard() {
        VBox card = new VBox();
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 12, 0, 0, 4);");
        return card;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Handle updates
    }
}
