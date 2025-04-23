package app.frontend;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

public class LoadingController {

    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;

    private Timeline timeline;
    private final List<String> loadingMessages = List.of(
            "starting...",
            "loading modules...",
            "initializing...",
            "almost there..."
    );

    @FXML
    private void initialize() {
        // Start with progress bar at 0
        progressBar.setProgress(0);

        // Create timeline for progress animation
        timeline = new Timeline();

        // Add keyframes for progress bar animation (0% to 100% over 4 seconds)
        timeline.getKeyFrames().add(
            new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 0))
        );

        // Add keyframes with status messages
        for (int i = 1 ; i < loadingMessages.size() ; i++) {
            final int index = i;
            double progress = (double) i / loadingMessages.size();

            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.seconds(progress * 4), e -> {
                        statusLabel.setText(loadingMessages.get(index));
                    }, new KeyValue(progressBar.progressProperty(), progress))
            );
        }

        // Adding the final frame
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(4),
                        new KeyValue(progressBar.progressProperty(), 1))
        );

        // Call the method to switch to the main app after loading
        timeline.setOnFinished(event -> {
            onLoadingComplete();
        });

        // Start the animation
        timeline.play();
    }

    private void onLoadingComplete() {
        // Small delay before switching to main app
        Platform.runLater(() -> {
            try {
                Thread.sleep(500);

                Parent mainView = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
                Scene mainScene = new Scene(mainView, 1900, 1080);

                Stage stage = (Stage) progressBar.getScene().getWindow();

                stage.setScene(mainScene);
                stage.setMaximized(true);
                stage.centerOnScreen();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
