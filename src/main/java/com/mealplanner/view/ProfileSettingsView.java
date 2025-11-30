package com.mealplanner.view;

import com.mealplanner.app.SessionManager;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.UpdateNutritionGoalsController;
import com.mealplanner.interface_adapter.view_model.ProfileSettingsViewModel;
import com.mealplanner.data_access.database.FileScheduleDataAccessObject;
import com.mealplanner.data_access.database.FileUserDataAccessObject;
import com.mealplanner.entity.NutritionGoals;
import com.mealplanner.entity.Schedule;
import com.mealplanner.entity.User;
import com.mealplanner.exception.DataAccessException;
import com.mealplanner.repository.RecipeRepository;
import com.mealplanner.repository.impl.FileRecipeRepository;
import com.mealplanner.util.StringUtil;
import com.mealplanner.view.util.DialogUtils;
import com.mealplanner.view.util.SvgIconLoader;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

public class ProfileSettingsView extends BorderPane implements PropertyChangeListener {
    public final String viewName = "ProfileSettingsView";
    private final ViewManagerModel viewManagerModel;
    private final ProfileSettingsViewModel profileSettingsViewModel;
    private final UpdateNutritionGoalsController updateNutritionGoalsController;
    private final SessionManager sessionManager;

    private final FileScheduleDataAccessObject scheduleDataAccessObject;
    private final RecipeRepository recipeRepository;

    // UI 컴포넌트 참조 저장
    private Label userValueLabel;
    private Label memberValueLabel;
    private Slider calorieSlider;
    private Label calorieValue;
    private Slider proteinSlider;
    private Label proteinValue;
    private Slider carbsSlider;
    private Label carbsValue;
    private Slider fatSlider;
    private Label fatValue;

    public ProfileSettingsView(ViewManagerModel viewManagerModel, 
                              ProfileSettingsViewModel profileSettingsViewModel,
                              UpdateNutritionGoalsController updateNutritionGoalsController) {
        this.viewManagerModel = viewManagerModel;
        this.profileSettingsViewModel = profileSettingsViewModel;
        this.updateNutritionGoalsController = updateNutritionGoalsController;
        this.sessionManager = SessionManager.getInstance();
        this.scheduleDataAccessObject = new FileScheduleDataAccessObject(new FileUserDataAccessObject());
        this.recipeRepository = new FileRecipeRepository();
        
        // ViewModel 리스너 등록
        if (profileSettingsViewModel != null) {
            profileSettingsViewModel.addPropertyChangeListener(this);
        }
        
        // ViewManagerModel 리스너 등록 (뷰 활성화 시 저장된 값으로 복원)
        if (viewManagerModel != null) {
            viewManagerModel.addPropertyChangeListener(this);
        }
        
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
        
        // 사용자 정보 로드
        User currentUser = sessionManager.getCurrentUser();
        String username = currentUser != null ? currentUser.getUsername() : "Guest";
        
        content.getChildren().add(createProfileCard(username));
        content.getChildren().add(createNutritionGoalsCard());
        content.getChildren().add(createDataManagementCard());
        
        // 초기 데이터 로드
        if (currentUser != null) {
            loadUserData(currentUser);
            loadNutritionGoals(currentUser.getNutritionGoals());
        }
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        // Increase scroll speed
        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() != 0) {
                double delta = event.getDeltaY() * 3.0;
                double height = scrollPane.getContent().getBoundsInLocal().getHeight();
                double vHeight = scrollPane.getViewportBounds().getHeight();
                
                double scrollableHeight = height - vHeight;
                if (scrollableHeight > 0) {
                    double vValueShift = -delta / scrollableHeight;
                    double nextVvalue = scrollPane.getVvalue() + vValueShift;
                    
                    if (nextVvalue >= 0 && nextVvalue <= 1.0 || (scrollPane.getVvalue() > 0 && scrollPane.getVvalue() < 1.0)) {
                        scrollPane.setVvalue(Math.min(Math.max(nextVvalue, 0), 1));
                        event.consume();
                    }
                }
            }
        });
        
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
        this.userValueLabel = new Label(username != null ? username : "Guest");
        this.userValueLabel.setStyle("-fx-text-fill: #1A1A1A; -fx-font-weight: bold; -fx-font-size: 18px;");
        userRow.getChildren().addAll(userLabel, this.userValueLabel);
        
        // Member Since
        VBox memberRow = new VBox(4);
        Label memberLabel = new Label("Member Since");
        memberLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");
        HBox memberValBox = new HBox(8);
        memberValBox.setAlignment(Pos.CENTER_LEFT);
        Node calendarIcon = SvgIconLoader.loadIcon("/svg/calendar.svg", 14, Color.web("#888888"));
        this.memberValueLabel = new Label("November 2024");
        this.memberValueLabel.setStyle("-fx-text-fill: #444444; -fx-font-size: 14px;");
        memberValBox.getChildren().addAll(calendarIcon != null ? calendarIcon : new Label(), this.memberValueLabel);
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
        this.calorieValue = new Label("2000 kcal");
        this.calorieValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #4CAF50;");
        calLabels.getChildren().addAll(calTitle, spacer, this.calorieValue);
        
        this.calorieSlider = createCustomSlider("#64DD17", 1000, 4000, 2000);
        
        // 칼로리 슬라이더 값 변경 리스너
        this.calorieSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int cal = (int) newVal.doubleValue();
            this.calorieValue.setText(cal + " kcal");
        });
        
        HBox rangeLabels = new HBox();
        Label minLabel = new Label("1000");
        Label maxLabel = new Label("4000");
        minLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888888;");
        maxLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888888;");
        Region rangeSpacer = new Region();
        HBox.setHgrow(rangeSpacer, Priority.ALWAYS);
        rangeLabels.getChildren().addAll(minLabel, rangeSpacer, maxLabel);
        
        calSliderBox.getChildren().addAll(calLabels, this.calorieSlider, rangeLabels);
        
        // B. Macro Sliders
        HBox macrosBox = new HBox(20);
        VBox proteinBox = createMacroSlider("Protein", "150g", "#448AFF", 50, 300, 150);
        VBox carbsBox = createMacroSlider("Carbs", "250g", "#FFA726", 50, 500, 250);
        VBox fatBox = createMacroSlider("Fat", "65g", "#FF4081", 20, 150, 65);
        macrosBox.getChildren().addAll(proteinBox, carbsBox, fatBox);
        
        // Save Button
        Button saveBtn = new Button("Save Nutrition Goals");
        saveBtn.setStyle("-fx-background-color: #00C853; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 8px; -fx-padding: 10 20; -fx-cursor: hand;");
        Node saveIcon = SvgIconLoader.loadIcon("/svg/book-fill.svg", 16, Color.WHITE);
        if (saveIcon != null) {
            saveBtn.setGraphic(saveIcon);
            saveBtn.setGraphicTextGap(8);
        }
        saveBtn.setOnAction(e -> saveNutritionGoals());
        
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
        
        // 슬라이더와 라벨을 필드에 저장
        switch (label) {
            case "Protein":
                this.proteinSlider = slider;
                this.proteinValue = val;
                break;
            case "Carbs":
                this.carbsSlider = slider;
                this.carbsValue = val;
                break;
            case "Fat":
                this.fatSlider = slider;
                this.fatValue = val;
                break;
        }
        
        // 슬라이더 값 변경 리스너
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            String unit = label.equals("Protein") || label.equals("Carbs") || label.equals("Fat") ? "g" : "";
            val.setText(String.format("%.0f%s", newVal.doubleValue(), unit));
        });
        
        container.getChildren().addAll(header, slider);
        return container;
    }
    
    private Slider createCustomSlider(String colorHex, double min, double max, double val) {
        Slider slider = new Slider(min, max, val);
        
        // 모던한 슬라이더 스타일 적용
        slider.getStyleClass().add("modern-slider");
        slider.setStyle(String.format("-fx-accent: %s;", colorHex));
        
        // 슬라이더 높이 설정 (더 두껍게)
        slider.setPrefHeight(40);
        slider.setMinHeight(40);
        slider.setMaxHeight(40);
        
        // 스크롤 보정을 위한 변수들
        final long[] lastChangeTime = {System.currentTimeMillis()};
        final double[] lastValue = {val};
        final double[] accumulatedDelta = {0.0};
        
        // 스크롤 이벤트 핸들러 추가 (드래그 속도 감지 및 단위 조정)
        slider.addEventFilter(ScrollEvent.SCROLL, event -> {
            event.consume(); // 기본 스크롤 동작 방지
            
            long currentTime = System.currentTimeMillis();
            long timeDelta = currentTime - lastChangeTime[0];
            double scrollDelta = event.getDeltaY();
            
            // 스크롤 속도 계산 (시간 간격이 짧을수록 빠름)
            double speed = timeDelta > 0 ? Math.abs(scrollDelta) / timeDelta : 1000.0;
            
            // 속도에 따른 단위 결정
            // 빠르게 스크롤 (시간 간격이 50ms 미만): 10단위 또는 더 큰 단위
            // 천천히 스크롤 (시간 간격이 50ms 이상): 5단위
            double stepSize;
            if (timeDelta < 50 && speed > 0.5) {
                // 빠른 스크롤: 10단위 또는 더 큰 단위 (속도에 비례)
                stepSize = Math.max(10, Math.min(50, speed * 20));
            } else {
                // 느린 스크롤: 5단위
                stepSize = 5;
            }
            
            // 누적 델타 계산
            accumulatedDelta[0] += scrollDelta;
            
            // 단위에 도달했을 때만 값 변경
            double threshold = stepSize * 10; // 스크롤 감도 조정
            if (Math.abs(accumulatedDelta[0]) >= threshold) {
                double currentValue = slider.getValue();
                double newValue;
                
                if (scrollDelta < 0) {
                    // 아래로 스크롤 (값 감소)
                    newValue = Math.max(min, currentValue - stepSize);
                } else {
                    // 위로 스크롤 (값 증가)
                    newValue = Math.min(max, currentValue + stepSize);
                }
                
                // 5단위 또는 10단위로 스냅
                if (stepSize == 5) {
                    newValue = Math.round(newValue / 5.0) * 5.0;
                } else {
                    newValue = Math.round(newValue / 10.0) * 10.0;
                }
                
                // 범위 내로 제한
                newValue = Math.max(min, Math.min(max, newValue));
                
                slider.setValue(newValue);
                accumulatedDelta[0] = 0.0; // 누적 델타 리셋
                lastChangeTime[0] = currentTime;
                lastValue[0] = newValue;
            }
        });
        
        // 마우스 드래그 이벤트 핸들러 추가 (드래그 속도 감지)
        slider.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            long currentTime = System.currentTimeMillis();
            long timeDelta = currentTime - lastChangeTime[0];
            double currentValue = slider.getValue();
            double valueDelta = Math.abs(currentValue - lastValue[0]);
            
            // 드래그 속도 계산
            double dragSpeed = timeDelta > 0 ? valueDelta / timeDelta : 0;
            
            // 속도에 따른 단위 조정
            if (timeDelta < 50 && dragSpeed > 0.5) {
                // 빠른 드래그: 10단위로 스냅
                double snappedValue = Math.round(currentValue / 10.0) * 10.0;
                if (Math.abs(snappedValue - currentValue) > 1) {
                    slider.setValue(snappedValue);
                }
            } else {
                // 느린 드래그: 5단위로 스냅
                double snappedValue = Math.round(currentValue / 5.0) * 5.0;
                if (Math.abs(snappedValue - currentValue) > 0.5) {
                    slider.setValue(snappedValue);
                }
            }
            
            lastChangeTime[0] = currentTime;
            lastValue[0] = slider.getValue();
        });
        
        // 값 변경 리스너에서도 5단위 또는 10단위로 스냅
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            long currentTime = System.currentTimeMillis();
            long timeDelta = currentTime - lastChangeTime[0];
            
            // 속도에 따른 단위 결정
            double stepSize = (timeDelta < 50) ? 10 : 5;
            double calculatedValue = Math.round(newVal.doubleValue() / stepSize) * stepSize;
            
            // 범위 내로 제한
            final double snappedValue = Math.max(min, Math.min(max, calculatedValue));
            
            // 스냅된 값과 현재 값이 다르면 업데이트
            if (Math.abs(snappedValue - newVal.doubleValue()) > 0.1) {
                Platform.runLater(() -> {
                    slider.setValue(snappedValue);
                });
            }
            
            lastChangeTime[0] = currentTime;
            lastValue[0] = snappedValue;
        });
        
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
        logoutBtn.setOnAction(e -> {
            // Clear session from SessionManager
            SessionManager.getInstance().clearSession();

            // Clear ViewManagerModel
            if (viewManagerModel != null) {
                viewManagerModel.setCurrentUserId(null);
                viewManagerModel.setCurrentUsername(null);
                viewManagerModel.setActiveView(ViewManager.LOGIN_VIEW);
            }
        });
        
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
        
        btn.setOnAction(e -> handleDataAction(title));

        row.getChildren().addAll(textBox, spacer, btn);
        return row;
    }

    private void handleDataAction(String title) {
        boolean confirmed = DialogUtils.showConfirmation(title, "This action cannot be undone. Continue?");
        if (!confirmed) {
            return;
        }

        try {
            switch (title) {
                case "Reset Meal Plan":
                    resetMealPlan();
                    DialogUtils.showInfoAlert("Completed", "Meal plan has been cleared.");
                    break;
                case "Clear Saved Recipes":
                    clearSavedRecipes();
                    DialogUtils.showInfoAlert("Completed", "All saved recipes have been removed.");
                    break;
                default:
                    DialogUtils.showInfoAlert("Completed", title + " processed successfully.");
            }
        } catch (DataAccessException | IllegalStateException ex) {
            DialogUtils.showErrorAlert("Operation Failed", ex.getMessage());
        }
    }

    private void resetMealPlan() {
        if (viewManagerModel == null || StringUtil.isNullOrEmpty(viewManagerModel.getCurrentUserId())) {
            throw new IllegalStateException("You must be logged in to reset your meal plan.");
        }
        String userId = viewManagerModel.getCurrentUserId();
        Schedule existing = scheduleDataAccessObject.findScheduleByUserId(userId);
        String scheduleId = existing != null ? existing.getScheduleId() : UUID.randomUUID().toString();
        Schedule cleared = new Schedule(scheduleId, userId);
        scheduleDataAccessObject.saveSchedule(cleared);
    }

    private void clearSavedRecipes() throws DataAccessException {
        recipeRepository.clear();
    }

    private VBox createCard() {
        VBox card = new VBox();
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 12, 0, 0, 4);");
        return card;
    }

    private void loadUserData(User user) {
        if (user == null) return;
        
        // 사용자 이름 업데이트
        if (userValueLabel != null) {
            userValueLabel.setText(user.getUsername());
        }
        
        // 가입 날짜 포맷팅
        if (memberValueLabel != null && user.getCreatedAt() != null) {
            LocalDateTime createdAt = user.getCreatedAt();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
            memberValueLabel.setText(formatter.format(createdAt));
        }
    }
    
    private void loadNutritionGoals(NutritionGoals goals) {
        if (goals == null) {
            // 기본값 사용
            goals = NutritionGoals.createDefault();
        }
        
        // 슬라이더 값 설정
        if (calorieSlider != null) {
            calorieSlider.setValue(goals.getDailyCalories());
        }
        if (proteinSlider != null) {
            proteinSlider.setValue(goals.getDailyProtein());
        }
        if (carbsSlider != null) {
            carbsSlider.setValue(goals.getDailyCarbs());
        }
        if (fatSlider != null) {
            fatSlider.setValue(goals.getDailyFat());
        }
        
        // 라벨 업데이트
        updateNutritionLabels();
    }
    
    private void updateNutritionLabels() {
        if (calorieSlider != null && calorieValue != null) {
            int cal = (int) calorieSlider.getValue();
            calorieValue.setText(cal + " kcal");
        }
        if (proteinSlider != null && proteinValue != null) {
            proteinValue.setText(String.format("%.0fg", proteinSlider.getValue()));
        }
        if (carbsSlider != null && carbsValue != null) {
            carbsValue.setText(String.format("%.0fg", carbsSlider.getValue()));
        }
        if (fatSlider != null && fatValue != null) {
            fatValue.setText(String.format("%.0fg", fatSlider.getValue()));
        }
    }
    
    private void saveNutritionGoals() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            DialogUtils.showErrorAlert("Error", "You must be logged in to save nutrition goals");
            return;
        }
        
        if (updateNutritionGoalsController == null) {
            DialogUtils.showErrorAlert("Error", "Update controller is not available");
            return;
        }
        
        // 슬라이더 값 가져오기
        int calories = (int) calorieSlider.getValue();
        double protein = proteinSlider.getValue();
        double carbs = carbsSlider.getValue();
        double fat = fatSlider.getValue();
        
        // Controller 호출
        updateNutritionGoalsController.execute(
            currentUser.getUserId(),
            calories,
            protein,
            carbs,
            fat
        );
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        
        Platform.runLater(() -> {
            // ViewManagerModel의 view 변경 감지 (Settings 페이지 활성화 시 저장된 값으로 복원)
            if ("view".equals(propertyName) && ViewManager.PROFILE_SETTINGS_VIEW.equals(evt.getNewValue())) {
                // Settings 페이지가 활성화될 때마다 저장된 영양 목표를 다시 로드
                User currentUser = sessionManager.getCurrentUser();
                if (currentUser != null) {
                    loadNutritionGoals(currentUser.getNutritionGoals());
                }
            }
            // ProfileSettingsViewModel의 변경 감지
            else if ("nutritionGoalsUpdated".equals(propertyName)) {
                // 성공 메시지 표시
                DialogUtils.showInfoAlert("Success", "Nutrition goals saved successfully!");
                
                // SessionManager의 사용자 정보도 업데이트
                User currentUser = sessionManager.getCurrentUser();
                if (currentUser != null && profileSettingsViewModel.getNutritionGoals() != null) {
                    currentUser.setNutritionGoals(profileSettingsViewModel.getNutritionGoals());
                    sessionManager.setCurrentUser(currentUser);
                }
            } else if ("error".equals(propertyName)) {
                // 에러 메시지 표시
                String error = profileSettingsViewModel.getError();
                if (error != null && !error.isEmpty()) {
                    DialogUtils.showErrorAlert("Error", error);
                }
            }
        });
    }
}
