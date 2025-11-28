package com.mealplanner.view.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.Region;
import javafx.stage.StageStyle;

/**
 * Utility for creating styled Dialogs/Alerts.
 * Corresponds to alert-dialog.tsx
 */
public class DialogUtils {

    /**
     * Shows a styled information alert.
     */
    public static void showInfoAlert(String title, String content) {
        createAlert(Alert.AlertType.INFORMATION, title, content).showAndWait();
    }

    /**
     * Shows a styled error alert.
     */
    public static void showErrorAlert(String title, String content) {
        createAlert(Alert.AlertType.ERROR, title, content).showAndWait();
    }

    /**
     * Creates a styled Alert object.
     */
    public static Alert createAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null); // Modern style often omits header
        alert.setContentText(content);
        alert.initStyle(StageStyle.UTILITY);

        // Apply CSS
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(DialogUtils.class.getResource("/style.css").toExternalForm());
        dialogPane.getStyleClass().add("alert-dialog");
        
        // Fix minimum size
        dialogPane.setMinHeight(Region.USE_PREF_SIZE);
        
        return alert;
    }
    
    /**
     * Shows a confirmation dialog. Returns true if confirmed.
     */
    public static boolean showConfirmation(String title, String content) {
        Alert alert = createAlert(Alert.AlertType.CONFIRMATION, title, content);
        return alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .isPresent();
    }
}

