package com.mealplanner.app;

/**
 * A safe entry point that checks for dependencies before launching the application.
 * If dependencies (like JavaFX) are missing, it prints a helpful error message.
 */
public class Launcher {
    public static void main(String[] args) {
        try {
            // Try to load a core JavaFX class to check if dependencies are present
            Class.forName("javafx.application.Application");
            Class.forName("com.google.gson.Gson");
            
            // If successful, proceed to run the actual Main class
            Main.main(args);
            
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            printMissingDependencyMessage();
        }
    }

    private static void printMissingDependencyMessage() {
        String os = System.getProperty("os.name").toLowerCase();
        
        System.err.println("\n========================================================");
        System.err.println(" [ERROR] Missing Dependencies / Libraries");
        System.err.println("========================================================\n");
        System.err.println("It seems like the required libraries (JavaFX, Gson, etc.) are not included in the classpath.");
        System.err.println("You need to run the setup script located in this project folder.\n");

        if (os.contains("win")) {
            System.err.println(" >>> OPTION 1 (Terminal):");
            System.err.println("     Open a terminal in this project folder and type:");
            System.err.println("     setup_and_run.bat");
            System.err.println("");
            System.err.println(" >>> OPTION 2 (No Terminal / Easy Way):");
            System.err.println("     Open this project folder in File Explorer.");
            System.err.println("     Find and double-click the file named 'setup_and_run.bat'.");
        } else {
            System.err.println(" >>> OPTION 1 (Terminal):");
            System.err.println("     Open a terminal in this project folder and run:");
            System.err.println("     ./setup_and_run.sh");
            System.err.println("     (Note: You might need to run 'chmod +x setup_and_run.sh' first)");
            System.err.println("");
            System.err.println(" >>> OPTION 2 (No Terminal / Easy Way):");
            System.err.println("     Open this project folder in Finder/File Manager.");
            System.err.println("     Find and double-click the file named 'setup_and_run.sh'.");
        }
        System.err.println("\n========================================================");
    }
}
