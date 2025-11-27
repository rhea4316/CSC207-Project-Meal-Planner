package com.mealplanner.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import com.mealplanner.view.SidebarPanel;
import com.mealplanner.view.ViewManager;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Create AppBuilder and build the application core
            AppBuilder appBuilder = new AppBuilder();
            ViewManager viewManager = appBuilder.build();

            // Root Layout
            BorderPane root = new BorderPane();
            
            // Sidebar
            SidebarPanel sidebar = new SidebarPanel(appBuilder.getViewManagerModel());
            root.setLeft(sidebar);
            
            // Main Content Area
            root.setCenter(viewManager);

            // Scene Setup
            Scene scene = new Scene(root, 1200, 800);
            
            // Add CSS
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            
            primaryStage.setTitle("Meal Planner");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
