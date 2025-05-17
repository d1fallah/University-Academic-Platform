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

/**
 * Controller for the authentication loading screen.
 * <p>
 * This controller manages the animated loading screen that appears during application
 * initialization. It handles a progress bar animation and displays sequential status
 * messages to inform the user about the loading progress.
 * </p>
 * 
 * @author Sellami Mohamed Odai
 * @version 1.0
 */
public class AuthLoadingController {

    /** Progress bar showing loading advancement */
    @FXML private ProgressBar progressBar;
    
    /** Label displaying current loading status message */
    @FXML private Label statusLabel;

    /** Animation timeline for the loading sequence */
    private Timeline timeline;
    
    /** Sequential messages displayed during the loading process */
    private final List<String> loadingMessages = List.of(
            "starting...",
            "loading modules...",
            "initializing...",
            "almost there..."
    );    /**
     * Initializes the loading screen with animated progress bar and status messages.
     * <p>
     * This method creates and configures a Timeline animation that:
     * <ul>
     *   <li>Animates the progress bar from 0% to 100% over 4 seconds</li>
     *   <li>Updates status messages at specific progress points</li>
     *   <li>Automatically transitions to the main application when complete</li>
     * </ul>
     * </p>
     */
    @FXML
    private void initialize() {
        progressBar.setProgress(0);
        timeline = new Timeline();
        
        final double ANIMATION_DURATION = 4.0; // seconds
        
        // Initial keyframe (0% progress)
        timeline.getKeyFrames().add(
            new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 0))
        );
        
        // Add intermediate keyframes with status messages
        for (int i = 1; i < loadingMessages.size(); i++) {
            final int index = i;
            double progress = (double) i / loadingMessages.size();
            
            timeline.getKeyFrames().add(
                new KeyFrame(
                    Duration.seconds(progress * ANIMATION_DURATION), 
                    e -> statusLabel.setText(loadingMessages.get(index)),
                    new KeyValue(progressBar.progressProperty(), progress)
                )
            );
        }
        
        // Final keyframe (100% progress)
        timeline.getKeyFrames().add(
            new KeyFrame(
                Duration.seconds(ANIMATION_DURATION),
                new KeyValue(progressBar.progressProperty(), 1)
            )
        );
        
        timeline.setOnFinished(event -> onLoadingComplete());
        timeline.play();
    }    /**
     * Transitions to the main application view after loading is complete.
     * <p>
     * This method loads the main application FXML, creates a new scene,
     * and applies it to the current stage. It uses Platform.runLater to ensure
     * the UI transition happens on the JavaFX application thread.
     * </p>
     * 
     * @see Platform#runLater(Runnable)
     * @see FXMLLoader#load(java.net.URL)
     */
    private void onLoadingComplete() {
        Platform.runLater(() -> {
            try {
                Parent mainView = FXMLLoader.load(getClass().getResource("/fxml/App.fxml"));
                Scene mainScene = new Scene(mainView, 1900, 1080);
                Stage stage = (Stage) progressBar.getScene().getWindow();
                
                stage.setScene(mainScene);
                stage.setMaximized(true);
                stage.centerOnScreen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
