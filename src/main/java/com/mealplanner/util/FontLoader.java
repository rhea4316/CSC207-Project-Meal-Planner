package com.mealplanner.util;

import javafx.scene.text.Font;

import java.io.InputStream;

/**
 * Utility class for loading custom fonts from resources.
 * Loads Poppins fonts if available.
 */
public class FontLoader {
    
    private static boolean fontsLoaded = false;
    
    /**
     * Loads custom fonts from the resources/fonts directory.
     * This method should be called once at application startup.
     */
    public static void loadFonts() {
        if (fontsLoaded) {
            return;
        }
        
        // Load Poppins fonts
        // 400 - Regular (기본 텍스트)
        loadFont("fonts/Poppins-Regular.ttf");
        
        // 500 - Medium (제목, 버튼)
        loadFont("fonts/Poppins-Medium.ttf");
        
        // 600 - SemiBold (강조)
        loadFont("fonts/Poppins-SemiBold.ttf");
        
        // 700 - Bold (큰 제목)
        loadFont("fonts/Poppins-Bold.ttf");
        
        fontsLoaded = true;
    }
    
    /**
     * Attempts to load a font file from resources.
     * Silently fails if the font file is not found (falls back to system fonts).
     */
    private static void loadFont(String fontPath) {
        try {
            InputStream fontStream = FontLoader.class.getResourceAsStream("/" + fontPath);
            if (fontStream != null) {
                Font font = Font.loadFont(fontStream, 12);
                fontStream.close();
                if (font != null) {
                    System.out.println("Loaded font: " + fontPath + " -> Family: " + font.getFamily() + ", Name: " + font.getName());
                } else {
                    System.err.println("Failed to load font (returned null): " + fontPath);
                }
            } else {
                System.err.println("Font file not found: " + fontPath);
            }
        } catch (Exception e) {
            System.err.println("Exception loading font: " + fontPath);
            e.printStackTrace();
        }
    }
    
    /**
     * Checks if custom fonts have been loaded.
     */
    public static boolean areFontsLoaded() {
        return fontsLoaded;
    }
}
