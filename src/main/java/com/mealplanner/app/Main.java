package com.mealplanner.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
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
                viewManagerModel.setActiveView(ViewManager.LOGIN_VIEW);
            }

            // Root Layout
            BorderPane root = new BorderPane();
            
            // Sidebar (created after view is set)
            SidebarPanel sidebar = new SidebarPanel(viewManagerModel);
            
            // Wrap Sidebar in ScrollPane for scrolling on small screens
            ScrollPane sidebarScroll = new ScrollPane(sidebar);
            sidebarScroll.setFitToWidth(true);
            sidebarScroll.setFitToHeight(true);
            sidebarScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            sidebarScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            sidebarScroll.getStyleClass().add("scroll-area");
            sidebarScroll.setStyle("-fx-background-color: -fx-theme-sidebar; -fx-background: -fx-theme-sidebar; -fx-padding: 0;");
            
            root.setLeft(sidebarScroll);
            
            // Main Content Area
            // Wrap ViewManager in ScrollPane for scrolling on small screens
            ScrollPane contentScroll = new ScrollPane(viewManager);
            contentScroll.setFitToWidth(true);
            contentScroll.setFitToHeight(true);
            contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            contentScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            contentScroll.getStyleClass().add("scroll-area");
            contentScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");
            
            // Increase scroll speed using EventFilter
            contentScroll.addEventFilter(ScrollEvent.SCROLL, event -> {
                if (event.getDeltaY() != 0) {
                    double delta = event.getDeltaY() * 3.0; // Speed multiplier (adjust as needed)
                    double height = contentScroll.getContent().getBoundsInLocal().getHeight();
                    double vHeight = contentScroll.getViewportBounds().getHeight();
                    
                    double scrollableHeight = height - vHeight;
                    if (scrollableHeight > 0) {
                        double vValueShift = -delta / scrollableHeight;
                        // Check if scrolling is possible
                        double nextVvalue = contentScroll.getVvalue() + vValueShift;
                        
                        // Only consume if we are actually scrolling
                        if (nextVvalue >= 0 && nextVvalue <= 1.0 || (contentScroll.getVvalue() > 0 && contentScroll.getVvalue() < 1.0)) {
                             contentScroll.setVvalue(Math.min(Math.max(nextVvalue, 0), 1));
                             event.consume();
                        }
                    }
                }
            });
            
            root.setCenter(contentScroll);

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
        // Improve font rendering
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");
        // Increase scroll speed for the entire application (if supported by JavaFX version, fallback to event handler above)
        System.setProperty("javafx.animation.fullSpeed", "true"); 
        launch(args);
    }
}
