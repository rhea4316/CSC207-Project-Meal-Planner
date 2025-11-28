package com.mealplanner.view.util;

import javafx.scene.Node;

/**
 * Utility methods for UI components.
 * Corresponds to utils.ts and use-mobile.ts
 */
public class UIUtils {

    /**
     * Combines style classes into a space-separated string (similar to cn()).
     * Note: JavaFX uses addAll for collections, but this can be useful for dynamic string construction.
     */
    public static String cn(String... classes) {
        StringBuilder sb = new StringBuilder();
        for (String c : classes) {
            if (c != null && !c.isEmpty()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Checks if the window width suggests a mobile/narrow layout.
     * @param node Any node in the scene to get window reference
     * @return true if window width < 768
     */
    public static boolean isMobile(Node node) {
        if (node == null || node.getScene() == null || node.getScene().getWindow() == null) {
            return false;
        }
        return node.getScene().getWindow().getWidth() < 768;
    }
}

