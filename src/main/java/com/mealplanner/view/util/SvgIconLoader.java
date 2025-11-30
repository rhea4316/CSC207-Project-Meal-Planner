package com.mealplanner.view.util;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Utility class for loading SVG icons and converting them to JavaFX nodes.
 * Parses SVG path data and creates JavaFX SVGPath nodes.
 */
public class SvgIconLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(SvgIconLoader.class);
    
    /**
     * Loads an SVG icon from resources and converts it to a JavaFX Node.
     * 
     * @param resourcePath Path to SVG file in resources (e.g., "/svg/dashboard.svg")
     * @param size Size of the icon in pixels
     * @param color Fill color for the icon
     * @return JavaFX Node representing the SVG icon, or null if loading fails
     */
    public static Node loadIcon(String resourcePath, double size, Color color) {
        try (InputStream inputStream = SvgIconLoader.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                logger.debug("SVG icon not found: {}", resourcePath);
                return null;
            }
            
            String svgContent;
            try (Scanner scanner = new Scanner(inputStream).useDelimiter("\\A")) {
                svgContent = scanner.hasNext() ? scanner.next() : "";
            }
            
            // Extract path data from SVG
            String pathData = extractPathData(svgContent);
            if (pathData == null || pathData.isEmpty()) {
                logger.warn("Could not extract path data from: {}", resourcePath);
                return null;
            }
            
            // Extract viewBox from SVG to calculate proper scale
            double viewBoxSize = extractViewBoxSize(svgContent);
            
            // Create SVGPath node
            SVGPath svgPath = new SVGPath();
            svgPath.setContent(pathData);
            svgPath.setFill(color);
            svgPath.setStrokeWidth(0);
            
            // Scale based on actual viewBox size to ensure uniform appearance
            double scale = size / viewBoxSize;
            svgPath.setScaleX(scale);
            svgPath.setScaleY(scale);
            
            // Wrap in StackPane to enforce fixed size for uniform icon dimensions
            StackPane container = new StackPane();
            container.setPrefWidth(size);
            container.setPrefHeight(size);
            container.setMinWidth(size);
            container.setMinHeight(size);
            container.setMaxWidth(size);
            container.setMaxHeight(size);
            container.getChildren().add(svgPath);
            container.setAlignment(javafx.geometry.Pos.CENTER);
            
            return container;
            
        } catch (Exception e) {
            logger.warn("Error loading SVG icon: {} - {}", resourcePath, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Extracts viewBox size from SVG content.
     * Returns the width/height of the viewBox (assumes square viewBox).
     */
    private static double extractViewBoxSize(String svgContent) {
        // Look for viewBox attribute
        int viewBoxStart = svgContent.indexOf("viewBox=\"");
        if (viewBoxStart == -1) {
            // Fallback: look for width/height attributes
            int widthStart = svgContent.indexOf("width=\"");
            if (widthStart != -1) {
                widthStart += 7;
                int widthEnd = svgContent.indexOf("\"", widthStart);
                if (widthEnd != -1) {
                    try {
                        String widthStr = svgContent.substring(widthStart, widthEnd);
                        // Remove "px" if present
                        widthStr = widthStr.replace("px", "").trim();
                        return Double.parseDouble(widthStr);
                    } catch (NumberFormatException e) {
                        // Fall through to default
                    }
                }
            }
            // Default to 24 if no viewBox or width found
            return 24.0;
        }
        
        viewBoxStart += 9; // Skip 'viewBox="'
        int viewBoxEnd = svgContent.indexOf("\"", viewBoxStart);
        if (viewBoxEnd == -1) {
            return 24.0;
        }
        
        String viewBox = svgContent.substring(viewBoxStart, viewBoxEnd);
        // viewBox format: "0 0 width height" or "x y width height"
        String[] parts = viewBox.split("\\s+");
        if (parts.length >= 4) {
            try {
                // Use width (index 2) as the viewBox size
                double width = Double.parseDouble(parts[2]);
                double height = Double.parseDouble(parts[3]);
                // Return the larger dimension to ensure icon fits
                return Math.max(width, height);
            } catch (NumberFormatException e) {
                // Fall through to default
            }
        }
        
        return 24.0; // Default viewBox size
    }
    
    /**
     * Extracts path data from SVG content.
     * Looks for path elements and extracts the 'd' attribute.
     */
    private static String extractPathData(String svgContent) {
        // Find all path elements and combine their data
        StringBuilder pathData = new StringBuilder();
        
        // Simple regex to find path d attributes
        int startIndex = 0;
        while (true) {
            int pathStart = svgContent.indexOf("<path", startIndex);
            if (pathStart == -1) break;
            
            int dStart = svgContent.indexOf("d=\"", pathStart);
            if (dStart == -1) {
                startIndex = pathStart + 1;
                continue;
            }
            
            dStart += 3; // Skip 'd="'
            int dEnd = svgContent.indexOf("\"", dStart);
            if (dEnd == -1) {
                startIndex = pathStart + 1;
                continue;
            }
            
            String path = svgContent.substring(dStart, dEnd);
            if (pathData.length() > 0) {
                pathData.append(" ");
            }
            pathData.append(path);
            
            startIndex = dEnd;
        }
        
        return pathData.length() > 0 ? pathData.toString() : null;
    }
    
    /**
     * Loads an SVG icon with default size (20px) and black color.
     */
    public static Node loadIcon(String resourcePath) {
        return loadIcon(resourcePath, 20, Color.BLACK);
    }
    
    /**
     * Loads an SVG icon with specified size and default black color.
     */
    public static Node loadIcon(String resourcePath, double size) {
        return loadIcon(resourcePath, size, Color.BLACK);
    }
}

