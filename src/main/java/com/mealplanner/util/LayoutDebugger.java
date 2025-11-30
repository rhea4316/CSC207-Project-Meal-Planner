package com.mealplanner.util;

import javafx.scene.Node;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for debugging JavaFX layouts.
 * Similar to browser developer tools, allows visual inspection of margins, padding, and bounds.
 * 
 * Usage:
 * - Press F12 to toggle debug mode
 * - All nodes will show red borders, padding in green, and margin in blue
 */
public class LayoutDebugger {
    
    private static final Logger logger = LoggerFactory.getLogger(LayoutDebugger.class);
    private static boolean debugMode = false;
    private static final Set<Node> debuggedNodes = new HashSet<>();
    
    private static final String DEBUG_STYLE = 
        "-fx-border-color: red; " +
        "-fx-border-width: 1; " +
        "-fx-border-style: solid;";
    
    private static final String PADDING_STYLE = 
        "-fx-background-color: rgba(0, 255, 0, 0.1);"; // Light green for padding
    
    /**
     * Toggles debug mode on/off for the entire application.
     * When enabled, all nodes will show visual debugging information.
     */
    public static void toggleDebugMode() {
        debugMode = !debugMode;
        logger.info("Layout debug mode: {}", debugMode ? "ON" : "OFF");
    }
    
    /**
     * Returns whether debug mode is currently enabled.
     */
    public static boolean isDebugMode() {
        return debugMode;
    }
    
    /**
     * Applies debug styling to a node.
     * Shows border (red), padding area (green), and margin area (blue).
     */
    public static void applyDebugStyle(Node node) {
        if (!debugMode) {
            removeDebugStyle(node);
            return;
        }
        
        if (node == null) return;
        
        // Add border to show bounds
        String currentStyle = node.getStyle();
        if (!currentStyle.contains("-fx-border-color: red")) {
            node.setStyle(currentStyle + " " + DEBUG_STYLE);
        }
        
        // For Region nodes, show padding and margin
        if (node instanceof Region) {
            Region region = (Region) node;
            showPaddingAndMargin(region);
        }
        
        debuggedNodes.add(node);
    }
    
    /**
     * Removes debug styling from a node.
     */
    public static void removeDebugStyle(Node node) {
        if (node == null) return;
        
        String currentStyle = node.getStyle();
        if (currentStyle != null) {
            // Remove debug-related styles
            currentStyle = currentStyle
                .replace("-fx-border-color: red;", "")
                .replace("-fx-border-width: 1;", "")
                .replace("-fx-border-style: solid;", "")
                .replace("-fx-background-color: rgba(0, 255, 0, 0.1);", "")
                .replace("-fx-background-color: rgba(0, 0, 255, 0.1);", "")
                .trim()
                .replaceAll("\\s+", " ");
            
            node.setStyle(currentStyle.isEmpty() ? null : currentStyle);
        }
        
        debuggedNodes.remove(node);
    }
    
    /**
     * Shows padding and margin information for a Region node.
     */
    private static void showPaddingAndMargin(Region region) {
        // Note: JavaFX doesn't have direct margin/padding properties like CSS
        // We show the insets which represent padding
        javafx.geometry.Insets padding = region.getPadding();
        if (padding != null && !padding.equals(javafx.geometry.Insets.EMPTY)) {
            // Add visual indicator for padding area
            String currentStyle = region.getStyle();
            if (!currentStyle.contains("rgba(0, 255, 0")) {
                region.setStyle(currentStyle + " " + PADDING_STYLE);
            }
        }
    }
    
    /**
     * Recursively applies debug styling to a node and all its children.
     */
    public static void applyDebugStyleRecursive(Node node) {
        if (node == null) return;
        
        applyDebugStyle(node);
        
        if (node instanceof javafx.scene.Parent) {
            javafx.scene.Parent parent = (javafx.scene.Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                applyDebugStyleRecursive(child);
            }
        }
    }
    
    /**
     * Recursively removes debug styling from a node and all its children.
     */
    public static void removeDebugStyleRecursive(Node node) {
        if (node == null) return;
        
        removeDebugStyle(node);
        
        if (node instanceof javafx.scene.Parent) {
            javafx.scene.Parent parent = (javafx.scene.Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                removeDebugStyleRecursive(child);
            }
        }
    }
    
    /**
     * Clears all debug styling from all nodes.
     */
    public static void clearAllDebugStyles() {
        Set<Node> nodesToRemove = new HashSet<>(debuggedNodes);
        for (Node node : nodesToRemove) {
            removeDebugStyle(node);
        }
        debuggedNodes.clear();
    }
    
    /**
     * Gets layout information for a node as a formatted string.
     * Useful for console debugging.
     */
    public static String getLayoutInfo(Node node) {
        if (node == null) return "Node is null";
        
        StringBuilder info = new StringBuilder();
        info.append("Node: ").append(node.getClass().getSimpleName()).append("\n");
        info.append("Bounds: ").append(node.getBoundsInLocal()).append("\n");
        info.append("Layout Bounds: ").append(node.getLayoutBounds()).append("\n");
        info.append("Layout X: ").append(node.getLayoutX()).append("\n");
        info.append("Layout Y: ").append(node.getLayoutY()).append("\n");
        
        if (node instanceof Region) {
            Region region = (Region) node;
            info.append("Padding: ").append(region.getPadding()).append("\n");
            info.append("Pref Width: ").append(region.getPrefWidth()).append("\n");
            info.append("Pref Height: ").append(region.getPrefHeight()).append("\n");
            info.append("Min Width: ").append(region.getMinWidth()).append("\n");
            info.append("Min Height: ").append(region.getMinHeight()).append("\n");
            info.append("Max Width: ").append(region.getMaxWidth()).append("\n");
            info.append("Max Height: ").append(region.getMaxHeight()).append("\n");
        }
        
        return info.toString();
    }
}

