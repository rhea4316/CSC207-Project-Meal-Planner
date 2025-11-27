package com.mealplanner.view.util;

import javafx.scene.Node;
import javafx.scene.shape.SVGPath;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Utility class for loading SVG icons and converting them to JavaFX nodes.
 * Parses SVG path data and creates JavaFX SVGPath nodes.
 */
public class SvgIconLoader {
    
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
                System.err.println("SVG icon not found: " + resourcePath);
                return null;
            }
            
            String svgContent;
            try (Scanner scanner = new Scanner(inputStream).useDelimiter("\\A")) {
                svgContent = scanner.hasNext() ? scanner.next() : "";
            }
            
            // Extract path data from SVG
            String pathData = extractPathData(svgContent);
            if (pathData == null || pathData.isEmpty()) {
                System.err.println("Could not extract path data from: " + resourcePath);
                return null;
            }
            
            // Create SVGPath node
            SVGPath svgPath = new SVGPath();
            svgPath.setContent(pathData);
            svgPath.setFill(color);
            svgPath.setStrokeWidth(0);
            
            // Scale to desired size (assuming viewBox is 24x24 or 512x512)
            double scale = size / 24.0; // Most icons use 24x24 viewBox
            svgPath.setScaleX(scale);
            svgPath.setScaleY(scale);
            
            return svgPath;
            
        } catch (Exception e) {
            System.err.println("Error loading SVG icon: " + resourcePath + " - " + e.getMessage());
            return null;
        }
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

