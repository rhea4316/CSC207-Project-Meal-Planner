package com.mealplanner.util;

import javafx.scene.text.Font;

import java.io.InputStream;

/**
 * Utility class for loading custom fonts from resources.
 * Loads Poppins and Inter fonts if available.
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
        
        // Load Poppins fonts (for headings)
        loadFont("fonts/Poppins-Medium.ttf");
        loadFont("fonts/Poppins-SemiBold.ttf");
        loadFont("fonts/Poppins-Bold.ttf");
        
        // Load Inter fonts (for body text)
        loadFont("fonts/Inter-Regular.ttf");
        loadFont("fonts/Inter-Medium.ttf");
        loadFont("fonts/Inter-SemiBold.ttf");
        
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
                Font.loadFont(fontStream, 12);
                fontStream.close();
                System.out.println("Loaded font: " + fontPath);
            }
        } catch (Exception e) {
            // Font not found or failed to load - will use system fallback
            // This is expected if fonts are not yet downloaded
        }
    }
    
    /**
     * Checks if custom fonts have been loaded.
     */
    public static boolean areFontsLoaded() {
        return fontsLoaded;
    }
}

