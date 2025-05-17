package app.frontend;

import app.backend.models.PracticalWork;
import app.backend.models.PracticalWorkSubmission;
import app.backend.models.User;
import app.backend.services.PracticalWorkSubmissionService;
import app.backend.services.AuthService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

public class TeacherPracticalWorkSubmissionsController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private VBox submissionsContainer;
    @FXML private Button returnButton;
    @FXML private ScrollPane scrollPane;
    
    private PracticalWork currentPracticalWork;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up return button event handler
        returnButton.setOnAction(event -> handleReturn());
    }
    
    /**
     * Sets the practical work for this view and loads its submissions
     */
    public void setPracticalWork(PracticalWork practicalWork) {
        this.currentPracticalWork = practicalWork;
        
        // Update title
        titleLabel.setText("Submissions for: " + practicalWork.getTitle());
        
        // Load submissions
        loadSubmissions();
    }
    
    /**
     * Loads all submissions for the current practical work
     */
    private void loadSubmissions() {
        // Clear the container
        submissionsContainer.getChildren().clear();
        
        // Get all submissions for this practical work
        List<PracticalWorkSubmission> submissions = 
            PracticalWorkSubmissionService.getSubmissionsByPracticalWorkId(currentPracticalWork.getId());
        
        // If no submissions, show a message
        if (submissions.isEmpty()) {
            Label noSubmissionsLabel = new Label("No submissions yet for this practical work.");
            noSubmissionsLabel.getStyleClass().add("no-data-message");
            noSubmissionsLabel.setPadding(new Insets(20, 0, 0, 0));
            submissionsContainer.getChildren().add(noSubmissionsLabel);
        } else {
            // Create a list item for each submission
            for (PracticalWorkSubmission submission : submissions) {
                submissionsContainer.getChildren().add(createSubmissionItem(submission));
            }
        }
    }
    
    /**
     * Creates a list item for a submission
     */
    private HBox createSubmissionItem(PracticalWorkSubmission submission) {
        // Get student information
        User student = AuthService.getUserById(submission.getStudentId());
        
        // Main container
        HBox itemContainer = new HBox();
        itemContainer.getStyleClass().add("submission-item");
        itemContainer.setAlignment(Pos.CENTER_LEFT);
        itemContainer.setSpacing(15);
        itemContainer.setPadding(new Insets(15));
        
        // Student information section
        VBox studentInfo = new VBox(5);
        studentInfo.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(studentInfo, Priority.ALWAYS);
        
        // Student matricule
        Label matriculeLabel = new Label(student != null ? student.getMatricule() : "Unknown");
        matriculeLabel.getStyleClass().add("submission-matricule");
        
        // Student name
        Label nameLabel = new Label(student != null ? student.getName() : "Unknown Student");
        nameLabel.getStyleClass().add("submission-name");
        
        // Submission date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy 'at' HH:mm");
        String submissionDate = submission.getSubmittedAt() != null ? 
                                dateFormat.format(submission.getSubmittedAt()) : "Unknown date";
        Label dateLabel = new Label("Submitted: " + submissionDate);
        dateLabel.getStyleClass().add("submission-date");
        
        studentInfo.getChildren().addAll(nameLabel, matriculeLabel, dateLabel);
        
        // Download button
        Button downloadButton = new Button("Download");
        downloadButton.getStyleClass().add("download-button");
        downloadButton.setOnAction(event -> handleDownload(submission));
        
        // Add all elements to the item container
        itemContainer.getChildren().addAll(studentInfo, downloadButton);
        
        return itemContainer;
    }
    
    /**
     * Handles downloading a submission file
     */
    private void handleDownload(PracticalWorkSubmission submission) {
        try {
            // Get the file path
            String filePath = submission.getFilePath();
            if (filePath == null || filePath.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "File path is not available.");
                return;
            }
            
            // Get the app's data directory - using the submissions directory that matches StudentPracticalWorksController
            String submissionsDir = System.getProperty("user.dir") + File.separator + "submissions";
            
            // Create the directory if it doesn't exist
            Path dirPath = Paths.get(submissionsDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            
            // Get the source file
            File sourceFile = new File(submissionsDir + File.separator + filePath);
            if (!sourceFile.exists()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Submission file not found at path: " + sourceFile.getAbsolutePath());
                return;
            }
            
            // Ask user where to save the file
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Submission File");
            
            // Get student name for the default filename
            User student = AuthService.getUserById(submission.getStudentId());
            String studentName = student != null ? student.getName().replaceAll("\\s+", "_") : "unknown";
            
            // Set suggested file name
            String originalFileName = sourceFile.getName();
            String fileName = "submission_" + studentName + "_" + originalFileName;
            fileChooser.setInitialFileName(fileName);
            
            // Set extension filters based on the file type
            String fileExtension = getFileExtension(originalFileName).toLowerCase();
            if (fileExtension.equals("zip")) {
                fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("ZIP files (*.zip)", "*.zip"));
            } else if (fileExtension.equals("pdf")) {
                fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf"));
            } else {
                fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("All files", "*.*"));
            }
            
            // Show save dialog
            File targetFile = fileChooser.showSaveDialog(submissionsContainer.getScene().getWindow());
            
            if (targetFile != null) {
                // Copy the file to the user's chosen location
                Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                showAlert(Alert.AlertType.INFORMATION, "Success", "File downloaded successfully.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to download file: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to get file extension
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }
    
    /**
     * Handles the return button action
     */
    private void handleReturn() {
        try {
            // Load the practical works view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/my-practical-works.fxml"));
            Parent myPracticalWorksView = loader.load();
            
            // Get the main layout's content area and set the practical works view
            StackPane contentArea = (StackPane) submissionsContainer.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(myPracticalWorksView);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to return to practical works view: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to show alerts
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 