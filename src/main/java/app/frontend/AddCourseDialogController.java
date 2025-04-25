package app.frontend;

import app.backend.models.Course;
import app.backend.models.User;
import app.backend.services.CourseService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

public class AddCourseDialogController implements Initializable {

    @FXML private StackPane dialogOverlay;
    @FXML private BorderPane dialogContainer;
    @FXML private TextField courseNameField;
    @FXML private TextArea courseDescriptionField;
    @FXML private ComboBox<String> levelComboBox;
    @FXML private Label selectedFileLabel;
    @FXML private Button selectFileButton;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;
    
    private User currentUser;
    private File selectedFile = null;
    private String courseFileName = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up current user
        currentUser = LoginController.getCurrentUser();
        
        // Initialize education level combo box
        levelComboBox.setItems(FXCollections.observableArrayList("L1", "L2", "L3", "M1", "M2"));
        
        // Set up drag and drop for PDF area
        setupDragAndDrop();
    }
    
    private void setupDragAndDrop() {
        // Get the drag target area
        StackPane dragTarget = (StackPane) dialogContainer.lookup(".center > HBox > StackPane");
        if (dragTarget == null) {
            System.out.println("Warning: Could not find the drag area");
            return;
        }
        
        // Set up the drag over event
        dragTarget.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles() && isAcceptableFile(event.getDragboard().getFiles().get(0))) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
        
        // Set up the drag dropped event
        dragTarget.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                File file = db.getFiles().get(0);
                if (isAcceptableFile(file)) {
                    handleFileSelected(file);
                    success = true;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }
    
    private boolean isAcceptableFile(File file) {
        return file != null && file.exists() && file.getName().toLowerCase().endsWith(".pdf");
    }
    
    @FXML
    private void handleSelectFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Course PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        
        File file = fileChooser.showOpenDialog(dialogContainer.getScene().getWindow());
        if (file != null) {
            handleFileSelected(file);
        }
    }
    
    private void handleFileSelected(File file) {
        selectedFile = file;
        selectedFileLabel.setText(file.getName());
        selectedFileLabel.setStyle("-fx-text-fill: white;");
    }
    
    @FXML
    private void handleSave(ActionEvent event) {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }
        
        // Copy the selected PDF to the courses directory
        if (selectedFile != null) {
            try {
                // Ensure directory exists
                String coursesDir = "courses";
                Path dirPath = Paths.get(coursesDir);
                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                }
                
                // Generate unique filename
                courseFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path targetPath = dirPath.resolve(courseFileName);
                
                // Copy the file
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "File Error", "Could not save the course PDF file.");
                return;
            }
        }
        
        // Create course object
        Course newCourse = new Course(
            courseNameField.getText(),
            courseDescriptionField.getText(),
            "", // No longer using duration
            currentUser.getId()
        );
        
        // If we have a filename, set it
        if (courseFileName != null) {
            newCourse.setPdfPath(courseFileName);
        }
        
        // Set the education level
        String educationLevel = levelComboBox.getSelectionModel().getSelectedItem();
        newCourse.setTargetLevel(educationLevel);
        
        // Save to database
        boolean success = CourseService.addCourse(newCourse);
        
        if (success) {
            closeDialog();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add the course.");
        }
    }
    
    private boolean validateInputs() {
        // Check course name
        if (courseNameField.getText() == null || courseNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a course name.");
            return false;
        }
        
        // Check description
        if (courseDescriptionField.getText() == null || courseDescriptionField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a course description.");
            return false;
        }
        
        // Check education level
        if (levelComboBox.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select an education level.");
            return false;
        }
        
        // Check file
        if (selectedFile == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a PDF file for the course.");
            return false;
        }
        
        return true;
    }
    
    @FXML
    private void handleCancel(ActionEvent event) {
        closeDialog();
    }
    
    private void closeDialog() {
        Stage stage = (Stage) dialogContainer.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}