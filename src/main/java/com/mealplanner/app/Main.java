package com.mealplanner.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.util.FontLoader;
import com.mealplanner.view.SidebarPanel;
import com.mealplanner.view.ViewManager;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load custom fonts (Poppins, Inter) if available
            FontLoader.loadFonts();
            
            // Create AppBuilder and build the application core
            AppBuilder appBuilder = new AppBuilder();
            ViewManager viewManager = appBuilder.build();
            
            // Ensure initial view is set before creating SidebarPanel
            ViewManagerModel viewManagerModel = appBuilder.getViewManagerModel();
            if (viewManagerModel.getActiveView() == null) {
                viewManagerModel.setActiveView(ViewManager.DASHBOARD_VIEW);
            }

            // Root Layout
            BorderPane root = new BorderPane();
            
            // Sidebar (created after view is set)
            SidebarPanel sidebar = new SidebarPanel(viewManagerModel);
            root.setLeft(sidebar);
            
            // Main Content Area
            root.setCenter(viewManager);

            // Scene Setup
            Scene scene = new Scene(root, 1200, 800);
            
            // Add CSS
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            
            primaryStage.setTitle("PlanEat");
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
