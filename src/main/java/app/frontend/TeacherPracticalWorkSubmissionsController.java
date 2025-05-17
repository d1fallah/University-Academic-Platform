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
import javafx.scene.control.TextField;
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
import java.util.stream.Collectors;

/**
 * Controller for the teacher practical work submissions view.
 * This class manages the display of student submissions for a specific practical work
 * and provides functionalities to filter, view, and download submission files.
 *
 * @author Sellami Mohamed Odai
 */
public class TeacherPracticalWorkSubmissionsController implements Initializable {    
    
    /** Title label for the view */
    @FXML private Label titleLabel;
    
    /** Container for submission items */
    @FXML private VBox submissionsContainer;
    
    /** Button to return to previous view */
    @FXML private Button returnButton;
    
    /** Scroll pane for submissions */
    @FXML private ScrollPane scrollPane;
    
    /** Search field for filtering students */
    @FXML private TextField searchStudentField;
    
    /** Current practical work being viewed */
    private PracticalWork currentPracticalWork;
    
    /** List of all submissions for the current practical work */
    private List<PracticalWorkSubmission> allSubmissions;
    
    /**
     * Initializes the controller.
     * Sets up event handlers and listeners for UI components.
     *
     * @param location The location used to resolve relative paths for the root object
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        returnButton.setOnAction(event -> handleReturn());
        
        if (searchStudentField != null) {
            searchStudentField.textProperty().addListener((observable, oldValue, newValue) -> handleStudentSearch());
        }
    }
    
    /**
     * Handles student search action.
     * Filters submissions based on student name.
     */
    @FXML    
    public void handleStudentSearch() {
        if (allSubmissions == null || allSubmissions.isEmpty()) {
            return;
        }
        
        String searchQuery = searchStudentField.getText().toLowerCase().trim();
        
        if (searchQuery.isEmpty()) {
            displaySubmissions(allSubmissions);
            return;
        }
        
        List<PracticalWorkSubmission> filteredSubmissions = allSubmissions.stream()
            .filter(submission -> {
                User student = AuthService.getUserById(submission.getStudentId());
                return student != null && student.getName().toLowerCase().contains(searchQuery);
            })
            .collect(Collectors.toList());
        
        displaySubmissions(filteredSubmissions);
    }
    
    /**
     * Sets the practical work for this view and loads its submissions.
     *
     * @param practicalWork The practical work to display submissions for
     */    
    public void setPracticalWork(PracticalWork practicalWork) {
        this.currentPracticalWork = practicalWork;
        titleLabel.setText("Submissions for: " + practicalWork.getTitle());
        loadSubmissions();
    }
    
    /**
     * Loads all submissions for the current practical work.
     * Retrieves submissions from the service and displays them.
     */
    private void loadSubmissions() {
        submissionsContainer.getChildren().clear();
        allSubmissions = PracticalWorkSubmissionService.getSubmissionsByPracticalWorkId(currentPracticalWork.getId());
        displaySubmissions(allSubmissions);
    }
    
    /**
     * Displays submissions in the container.
     * Creates and adds submission items to the container.
     *
     * @param submissions The list of submissions to display
     */    
    private void displaySubmissions(List<PracticalWorkSubmission> submissions) {
        submissionsContainer.getChildren().clear();
        
        if (submissions.isEmpty()) {
            Label noSubmissionsLabel = new Label("No submissions found.");
            noSubmissionsLabel.getStyleClass().add("no-data-message");
            noSubmissionsLabel.setPadding(new Insets(20, 0, 0, 0));
            submissionsContainer.getChildren().add(noSubmissionsLabel);
            return;
        }
        
        submissions.forEach(submission -> 
            submissionsContainer.getChildren().add(createSubmissionItem(submission))
        );
    }
    
    /**
     * Creates a list item for a submission.
     * Builds UI components for displaying submission details and download button.
     *
     * @param submission The submission to create an item for
     * @return An HBox containing the submission item UI
     */    
    private HBox createSubmissionItem(PracticalWorkSubmission submission) {
        User student = AuthService.getUserById(submission.getStudentId());
        
        HBox itemContainer = new HBox();
        itemContainer.getStyleClass().add("submission-item");
        itemContainer.setAlignment(Pos.CENTER_LEFT);
        itemContainer.setSpacing(15);
        itemContainer.setPadding(new Insets(15));
        
        VBox studentInfo = new VBox(5);
        studentInfo.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(studentInfo, Priority.ALWAYS);
        
        Label matriculeLabel = new Label(student != null ? student.getMatricule() : "Unknown");
        matriculeLabel.getStyleClass().add("submission-matricule");
        
        Label nameLabel = new Label(student != null ? student.getName() : "Unknown Student");
        nameLabel.getStyleClass().add("submission-name");
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy 'at' HH:mm");
        String submissionDate = submission.getSubmittedAt() != null ? 
                               dateFormat.format(submission.getSubmittedAt()) : "Unknown date";
        Label dateLabel = new Label("Submitted: " + submissionDate);
        dateLabel.getStyleClass().add("submission-date");
        
        studentInfo.getChildren().addAll(nameLabel, matriculeLabel, dateLabel);
        
        Button downloadButton = new Button("Download");
        downloadButton.getStyleClass().add("download-button");
        downloadButton.setOnAction(event -> handleDownload(submission));
        
        itemContainer.getChildren().addAll(studentInfo, downloadButton);
        
        return itemContainer;
    }
    
    /**
     * Handles downloading a submission file.
     * Gets the file from the submissions directory and saves it to a user-selected location.
     *
     * @param submission The submission to download
     */    
    private void handleDownload(PracticalWorkSubmission submission) {
        try {
            String filePath = submission.getFilePath();
            if (filePath == null || filePath.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "File path is not available.");
                return;
            }
            
            String submissionsDir = System.getProperty("user.dir") + File.separator + "submissions";
            
            Path dirPath = Paths.get(submissionsDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            
            File sourceFile = new File(submissionsDir + File.separator + filePath);
            if (!sourceFile.exists()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Submission file not found at path: " + sourceFile.getAbsolutePath());
                return;
            }
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Submission File");
            
            User student = AuthService.getUserById(submission.getStudentId());
            String studentName = student != null ? student.getName().replaceAll("\\s+", "_") : "unknown";
            
            String originalFileName = sourceFile.getName();
            String fileName = "submission_" + studentName + "_" + originalFileName;
            fileChooser.setInitialFileName(fileName);
            
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
            
            File targetFile = fileChooser.showSaveDialog(submissionsContainer.getScene().getWindow());
            
            if (targetFile != null) {
                Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                showAlert(Alert.AlertType.INFORMATION, "Success", "File downloaded successfully.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to download file: " + e.getMessage());
        }
    }    
    /**
     * Extracts the file extension from a filename.
     *
     * @param fileName The name of the file
     * @return The file extension or an empty string if no extension exists
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }
    
    /**
     * Handles the return button action.
     * Navigates back to the practical works view.
     */    
    private void handleReturn() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TeacherPracticalWorks.fxml"));
            Parent myPracticalWorksView = loader.load();
            
            StackPane contentArea = (StackPane) submissionsContainer.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(myPracticalWorksView);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to return to practical works view: " + e.getMessage());
        }
    }
    
    /**
     * Displays an alert dialog with the specified parameters.
     *
     * @param type The type of alert (information, warning, error, etc.)
     * @param title The title of the alert
     * @param content The message content of the alert
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 