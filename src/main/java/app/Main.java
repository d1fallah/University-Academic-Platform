package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {

            // Load fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            // Create the scene
            Scene scene = new Scene(root, 1900, 1080);

            // Add css
            scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());

            // Configure the stage
            primaryStage.setTitle("AOPFE Login");
            primaryStage.setScene(scene);


            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Failed to start the application.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}