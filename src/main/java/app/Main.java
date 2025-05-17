package app;

import app.backend.database.DataBaseConnection;
import app.backend.database.DatabaseInitializer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database with tables and default valid IDs
            DatabaseInitializer.initializeDatabase();

            // Load fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AuthLogin.fxml"));
            Parent root = loader.load();

            // Create the scene
            Scene scene = new Scene(root, 1900, 1080);

            // Add css
            scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());

            // Configure the stage
            primaryStage.setTitle("AOPFE Login");
            primaryStage.setScene(scene);
            
            // Set stage to maximized
            primaryStage.setMaximized(true);
            
            // Show stage
            primaryStage.show();
        
            Platform.runLater(() -> {
                root.requestLayout();
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Failed to start the application.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void stop() {
        // Close database connection when application exits
        DataBaseConnection.closeConnection();
    }
}