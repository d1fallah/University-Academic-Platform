package app.frontend;

import app.backend.models.Course;
import app.backend.models.PracticalWork;
import app.backend.models.PracticalWorkSubmission;
import app.backend.models.User;
import app.backend.services.CourseService;
import app.backend.services.PracticalWorkService;
import app.backend.services.PracticalWorkSubmissionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TeacherPracticalWorksViewController implements Initializable {

    @FXML private FlowPane practicalWorkCardsContainer;
    @FXML private TextField searchField;
    
    // Dialog overlay components
    @FXML private StackPane submitPracticalWorkOverlay;
    @FXML private BorderPane dialogContainer;
    @FXML private Label practicalWorkTitleLabel;
    @FXML private Label practicalWorkDescriptionLabel;
    @FXML private Label deadlineLabel;
    @FXML private Label selectedFileLabel;
    @FXML private Button selectFileButton;
    @FXML private StackPane dropArea;
    @FXML private Label teacherNameLabel;
    @FXML private ImageView teacherProfileImage;
    
    private User currentUser;
    private User teacher;
    private ObservableList<PracticalWork> practicalWorksList = FXCollections.observableArrayList();
    private File selectedFile = null;
    private PracticalWork currentPracticalWork = null;

    /**
     * Sets the teacher for this view
     * @param teacher The teacher user
     */
    public void setTeacher(User teacher) {
        this.teacher = teacher;

        // Update teacher name label
        if (teacherNameLabel != null) {
            teacherNameLabel.setText("Prof. " + teacher.getName());
            teacherNameLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");
        }

        // Set the teacher profile image
        try {
            Image profileImg = new Image(getClass().getResourceAsStream("/images/profilep.png"));
            teacherProfileImage.setImage(profileImg);
        } catch (Exception e) {
            System.out.println("Failed to load teacher profile image");
        }
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current logged in user
        currentUser = LoginController.getCurrentUser();
        
        // Set up drag and drop for PDF area if available
        setupDragAndDrop();
        
        // Only proceed with student-specific initialization if user is a student and required UI components exist
        if (currentUser != null && currentUser.getRole().equals("student") && practicalWorkCardsContainer != null && searchField != null) {
            // Load all practical works
            loadPracticalWorks();
            
            // Setup search functionality
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterPracticalWorks(newValue);
            });
        } else if (currentUser != null && !currentUser.getRole().equals("student")) {
            // If not a student and we have the container, show message or redirect
            if (practicalWorkCardsContainer != null) {
                practicalWorkCardsContainer.getChildren().clear();
                Label accessDeniedLabel = new Label("Only students can access this page.");
                accessDeniedLabel.getStyleClass().add("no-courses-message");
                accessDeniedLabel.setPadding(new Insets(50, 0, 0, 0));
                practicalWorkCardsContainer.getChildren().add(accessDeniedLabel);
            }
        }
    }
    
    /**
     * Sets up drag and drop functionality for the PDF upload area
     */
    private void setupDragAndDrop() {
        // Check if dropArea exists (it might not be in all views)
        if (dropArea == null) {
            return; // Exit if dropArea is not defined in the FXML
        }
        
        // Set up the drag over event
        dropArea.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles() && isAcceptableFile(event.getDragboard().getFiles().get(0))) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
        
        // Set up the drag dropped event
        dropArea.setOnDragDropped(event -> {
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
        // Check if submitPracticalWorkOverlay exists (it might not be in all views)
        if (submitPracticalWorkOverlay == null) {
            return; // Exit if submitPracticalWorkOverlay is not defined in the FXML
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Solution File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
            new FileChooser.ExtensionFilter("ZIP Files", "*.zip"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        File file = fileChooser.showOpenDialog(submitPracticalWorkOverlay.getScene().getWindow());
        if (file != null) {
            handleFileSelected(file);
        }
    }
    
    private void handleFileSelected(File file) {
        if (selectedFileLabel == null) {
            return; // Exit if selectedFileLabel is not defined in the FXML
        }
        
        selectedFile = file;
        selectedFileLabel.setText(file.getName());
        selectedFileLabel.setStyle("-fx-text-fill: white;");
    }
    
    /**
     * Loads all practical works
     */
    private void loadPracticalWorks() {
        // Clear the container
        practicalWorkCardsContainer.getChildren().clear();
        
        // Get all practical works from the service
        List<PracticalWork> allPracticalWorks = getAllPracticalWorks();
        practicalWorksList.setAll(allPracticalWorks);
        
        // If no practical works, show a message
        if (practicalWorksList.isEmpty()) {
            Label noPracticalWorksLabel = new Label("There are no practical works available at the moment.");
            noPracticalWorksLabel.getStyleClass().add("no-courses-message");
            noPracticalWorksLabel.setPadding(new Insets(50, 0, 0, 0));
            practicalWorkCardsContainer.getChildren().add(noPracticalWorksLabel);
        } else {
            // Create and add a card for each practical work
            for (PracticalWork practicalWork : practicalWorksList) {
                practicalWorkCardsContainer.getChildren().add(createPracticalWorkCard(practicalWork));
            }
        }
    }
    
    /**
     * Helper method to get all practical works
     * In a real application, you might want to filter by student's courses or education level
     */
    private List<PracticalWork> getAllPracticalWorks() {
        // For now, we'll just get all practical works
        // You could implement filtering by student's courses or education level
        List<PracticalWork> allWorks = new ArrayList<>();
        
        // Get all courses (this should ideally be filtered by student's enrollment)
        List<Course> courses = CourseService.getAllCourses();
        
        // Get practical works for each course
        for (Course course : courses) {
            allWorks.addAll(PracticalWorkService.getPracticalWorksByCourseId(course.getId()));
        }
        
        return allWorks;
    }

    /**
     * Creates a visual card representation for a practical work with submit option
     */
    private StackPane createPracticalWorkCard(PracticalWork practicalWork) {
        // Main card container
        StackPane cardPane = new StackPane();
        cardPane.getStyleClass().add("course-card");
        cardPane.setPrefWidth(480);
        cardPane.setPrefHeight(250); // Increased height for better spacing

        // Add background image to card
        ImageView cardBackground = new ImageView();
        try {
            Image bgImage = new Image(getClass().getResourceAsStream("/images/courseCardBackground.png"));
            cardBackground.setImage(bgImage);
            cardBackground.setFitWidth(480);
            cardBackground.setFitHeight(310);
            cardBackground.setPreserveRatio(false);
            cardBackground.setOpacity(0.7);
        } catch (Exception e) {
            System.out.println("Failed to load background image for practical work card");
            // Set a fallback background color
            cardPane.setStyle("-fx-background-color: #353535;");
        }

        // Card layout container
        VBox cardContent = new VBox();
        cardContent.getStyleClass().add("card-content");
        cardContent.setSpacing(15); // Reduced spacing for better layout
        cardContent.setPadding(new Insets(18, 20, 18, 20));
        cardContent.setPrefWidth(480);
        cardContent.setPrefHeight(250);

        // Top section with practical work title and logo
        HBox headerBox = new HBox();
        headerBox.getStyleClass().add("card-header");
        headerBox.setAlignment(Pos.TOP_LEFT);
        headerBox.setPrefWidth(480);
        headerBox.setSpacing(20);

        // Create container for title
        VBox titleContainer = new VBox();
        titleContainer.setAlignment(Pos.TOP_LEFT);
        titleContainer.setPrefWidth(390);
        HBox.setHgrow(titleContainer, Priority.ALWAYS);

        // Title on left side - Set format as "Practical Work No. X"
        Label titleLabel = new Label(practicalWork.getTitle());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);
        titleLabel.setMinHeight(Region.USE_PREF_SIZE);

        titleContainer.getChildren().add(titleLabel);

        // Practical work logo/icon
        StackPane logoContainer = new StackPane();
        logoContainer.setMinWidth(50);
        logoContainer.setMaxWidth(50);
        logoContainer.setPrefHeight(50);
        logoContainer.setAlignment(Pos.TOP_CENTER);
        logoContainer.getStyleClass().add("logo-container");

        ImageView practicalWorkIcon = new ImageView();
        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/Keyboard.png"));
            practicalWorkIcon.setImage(logo);
        } catch (Exception e) {
            System.out.println("Failed to load logo for practical work: " + practicalWork.getTitle());
        }
        practicalWorkIcon.setFitWidth(50);
        practicalWorkIcon.setFitHeight(50);
        practicalWorkIcon.getStyleClass().add("practical-work-icon");

        logoContainer.getChildren().add(practicalWorkIcon);

        // Add title and logo to header box
        headerBox.getChildren().addAll(titleContainer, logoContainer);

        // Practical work description
        String description = practicalWork.getDescription();
        if (description == null || description.isEmpty()) {
            description = "No description available";
        } else if (description.length() > 100) {
            description = description.substring(0, 97) + "...";
        }
        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("card-description");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMinHeight(Region.USE_PREF_SIZE);

        // Get course info
        Course course = CourseService.getCourseById(practicalWork.getCourseId());
        String courseName = course != null ? course.getTitle() : "Unknown Course";

        // Course info label
        HBox courseBox = new HBox();
        courseBox.setAlignment(Pos.CENTER_LEFT);
        courseBox.setSpacing(5);

        Label courseLabel = new Label("Course: " + courseName);
        courseLabel.getStyleClass().add("card-instructor");
        courseLabel.setStyle("-fx-text-fill: white;");

        courseBox.getChildren().addAll(courseLabel);

        // Add progress bar to show deadline progress
        VBox progressBox = new VBox(3); // Reduced spacing
        progressBox.setAlignment(Pos.CENTER_LEFT);
        progressBox.setPadding(new Insets(5, 0, 0, 0)); // Add some top padding

        // Calculate progress percentage based on days passed vs total days
        int progressPercentage = 0;
        String timeStatus = "No deadline set";
        String progressColor = "#10b981"; // Default green

        if (practicalWork.getDeadline() != null) {
            // Get current date and time
            java.util.Date currentDate = new java.util.Date();

            // Calculate the total duration and elapsed time
            long creationTime = practicalWork.getCreatedAt() != null ?
                    practicalWork.getCreatedAt().getTime() : currentDate.getTime();
            long deadlineTime = practicalWork.getDeadline().getTime();
            long currentTime = currentDate.getTime();

            // Calculate total duration in milliseconds
            long totalDuration = deadlineTime - creationTime;
            // Calculate elapsed time in milliseconds
            long elapsedTime = currentTime - creationTime;
            // Calculate remaining time in milliseconds
            long remainingTime = deadlineTime - currentTime;

            // Calculate progress percentage
            if (totalDuration > 0) {
                progressPercentage = (int)((elapsedTime * 100) / totalDuration);

                // Cap progress at 100%
                if (progressPercentage > 100) {
                    progressPercentage = 100;
                    timeStatus = "Deadline passed";
                    progressColor = "#f43f5e"; // Red for overdue
                } else {
                    // Create dynamic countdown display
                    if (remainingTime > 0) {
                        // Calculate days, hours, minutes
                        long days = remainingTime / (1000 * 60 * 60 * 24);
                        long hours = (remainingTime % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);

                        if (days > 0) {
                            timeStatus = days + (days == 1 ? " day " : " days ") + hours + (hours == 1 ? " hour" : " hours") + " remaining";
                        } else {
                            // Less than a day remaining
                            long minutes = (remainingTime % (1000 * 60 * 60)) / (1000 * 60);
                            if (hours > 0) {
                                timeStatus = hours + (hours == 1 ? " hour " : " hours ") + minutes + (minutes == 1 ? " minute" : " minutes") + " remaining";
                            } else {
                                timeStatus = minutes + (minutes == 1 ? " minute" : " minutes") + " remaining";
                            }
                        }
                    }

                    // Set color based on progress
                    if (progressPercentage >= 75) {
                        progressColor = "#f43f5e"; // Red for near deadline
                    } else if (progressPercentage >= 50) {
                        progressColor = "#f59e0b"; // Orange/Yellow for approaching deadline
                    } else {
                        progressColor = "#10b981"; // Green for good time
                    }
                }
            }
        }

        // Create progress bar styled like the one in the screenshot
        // Add progress percentage label
        HBox progressLabelBox = new HBox();
        progressLabelBox.setAlignment(Pos.CENTER_LEFT);

        Label progressLabel = new Label("Duration Passed " + progressPercentage + "%");
        progressLabel.setStyle("-fx-text-fill: " + progressColor + "; -fx-font-size: 13px; -fx-font-weight: bold;");

        progressLabelBox.getChildren().add(progressLabel);

        ProgressBar deadlineBar = new ProgressBar((double) progressPercentage / 100);
        deadlineBar.setPrefWidth(400);
        deadlineBar.setPrefHeight(10);
        deadlineBar.setMinHeight(10);
        deadlineBar.setMaxHeight(10);
        deadlineBar.getStyleClass().add("performance-bar");

        // Override any styles as needed to match the screenshot perfectly
        String barStyle = "-fx-accent: " + progressColor + ";" +
                "-fx-background-color: #333333;" +
                "-fx-background-radius: 5px;" +
                "-fx-background-insets: 0;" +
                "-fx-border-radius: 5px;";

        deadlineBar.setStyle(barStyle);
        HBox.setHgrow(deadlineBar, Priority.ALWAYS);

        // Add both the label and progress bar
        progressBox.getChildren().addAll(progressLabelBox, deadlineBar);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Footer section with deadline date and buttons
        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.BOTTOM_LEFT);
        footerBox.setPrefWidth(480);

        // Date icon with deadline date
        HBox dateBox = new HBox();
        dateBox.getStyleClass().add("card-date");
        dateBox.setAlignment(Pos.CENTER_LEFT);
        dateBox.setPrefHeight(24);
        dateBox.setSpacing(8);
        HBox.setHgrow(dateBox, Priority.ALWAYS);

        // Date icon
        ImageView calendarIcon = new ImageView();
        try {
            Image calendarImg = new Image(getClass().getResourceAsStream("/images/Case.png"));
            calendarIcon.setImage(calendarImg);
            calendarIcon.setFitHeight(16);
            calendarIcon.setFitWidth(16);
            calendarIcon.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Failed to load calendar icon");
        }

        // Use the timeStatus (countdown timer) as the date label text
        Label dateLabel = new Label(timeStatus);
        dateLabel.getStyleClass().add("date-label");
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + progressColor + ";");

        dateBox.getChildren().addAll(calendarIcon, dateLabel);

        // Buttons section
        HBox buttonBox = new HBox(10); // Same spacing as exercise cards
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Create View PW button styled exactly like the image
        Button viewButton = new Button("View PW");
        viewButton.getStyleClass().add("view-course-button");
        viewButton.setStyle("-fx-background-color: #d97706;");
        viewButton.setPrefWidth(120);
        viewButton.setPrefHeight(24);
        viewButton.setOnAction(e -> handleViewPracticalWork(practicalWork));

        // Submit button styled to match the image
        Button submitButton = new Button("Submit");
        submitButton.getStyleClass().add("view-course-button");
        submitButton.setStyle("-fx-background-color: #059669;");
        submitButton.setPrefWidth(120);
        submitButton.setPrefHeight(24);
        submitButton.setOnAction(e -> handleSubmitPracticalWork(practicalWork));

        buttonBox.getChildren().addAll(viewButton, submitButton);

        // Add date and buttons to footer
        footerBox.getChildren().addAll(dateBox, buttonBox);

        // Add all sections to the card
        cardContent.getChildren().addAll(headerBox, descriptionLabel, courseBox, progressBox, spacer, footerBox);

        // Stack the background and content
        cardPane.getChildren().addAll(cardBackground, cardContent);

        // Add accessibility features
        cardPane.setAccessibleText("Practical Work: " + practicalWork.getTitle() + ", " + description);

        return cardPane;
    }
    
    /**
     * Filters practical works based on search query
     */
    private void filterPracticalWorks(String query) {
        if (query == null || query.isEmpty()) {
            // If query is empty, show all practical works
            loadPracticalWorks();
        } else {
            // Filter practical works based on query
            query = query.toLowerCase();
            final String searchQuery = query;
            
            List<PracticalWork> filteredList = practicalWorksList.stream()
                .filter(practicalWork -> 
                    practicalWork.getTitle().toLowerCase().contains(searchQuery) ||
                    practicalWork.getDescription().toLowerCase().contains(searchQuery))
                .collect(Collectors.toList());
            
            practicalWorkCardsContainer.getChildren().clear();
            
            if (filteredList.isEmpty()) {
                Label noResultsLabel = new Label("No practical works match your search criteria.");
                noResultsLabel.getStyleClass().add("no-courses-message");
                noResultsLabel.setPadding(new Insets(50, 0, 0, 0));
                practicalWorkCardsContainer.getChildren().add(noResultsLabel);
            } else {
                for (PracticalWork practicalWork : filteredList) {
                    practicalWorkCardsContainer.getChildren().add(createPracticalWorkCard(practicalWork));
                }
            }
        }
    }
    
    /**
     * Handles the action when the Submit Practical Work button is clicked
     */
    private void handleSubmitPracticalWork(PracticalWork practicalWork) {
        // Check if required UI components exist
        if (submitPracticalWorkOverlay == null || 
            selectedFileLabel == null || 
            practicalWorkTitleLabel == null || 
            practicalWorkDescriptionLabel == null || 
            deadlineLabel == null) {
            // If any required component is missing, show an error or return
            System.out.println("Required UI components for submission are missing");
            if (practicalWorkCardsContainer != null && practicalWorkCardsContainer.getScene() != null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Submission form cannot be displayed");
            }
            return;
        }
        
        // Reset submission form
        selectedFile = null;
        selectedFileLabel.setText("No file selected");
        selectedFileLabel.setStyle("-fx-text-fill: #888888;");
        
        // Set the current practical work
        currentPracticalWork = practicalWork;
        
        // Update dialog labels
        practicalWorkTitleLabel.setText(practicalWork.getTitle());
        practicalWorkDescriptionLabel.setText(practicalWork.getDescription());
        deadlineLabel.setText(practicalWork.getDeadline() != null ? 
                              practicalWork.getDeadline().toString() : "No deadline");
        
        // Show the overlay
        submitPracticalWorkOverlay.setVisible(true);
    }
    
    /**
     * Handles the cancel button action in the dialog
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        // Check if overlay exists before trying to hide it
        if (submitPracticalWorkOverlay != null) {
            // Hide the overlay
            submitPracticalWorkOverlay.setVisible(false);
        }
    }
    
    /**
     * Handles the search action
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        if (searchField != null) {
            String query = searchField.getText().trim();
            filterPracticalWorks(query);
        }
    }
    
    /**
     * Handles submitting a practical work
     */
    @FXML
    private void handleSubmit(ActionEvent event) {
        // Check if required UI components exist
        if (submitPracticalWorkOverlay == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Submission form is not available");
            return;
        }
        
        // Validate that a file is selected
        if (selectedFile == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a PDF file to submit.");
            return;
        }
        
        // Make sure a practical work is selected
        if (currentPracticalWork == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No practical work selected for submission.");
            return;
        }
        
        // Copy the selected PDF to the submissions directory
        String submissionFileName = null;
        try {
            // Ensure directory exists
            String submissionsDir = "submissions";
            Path dirPath = Paths.get(submissionsDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            
            // Generate unique filename
            submissionFileName = currentUser.getId() + "_" + currentPracticalWork.getId() + "_" + 
                             System.currentTimeMillis() + "_" + selectedFile.getName();
            Path targetPath = dirPath.resolve(submissionFileName);
            
            // Copy the file
            Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "File Error", "Could not save your submission file.");
            return;
        }
        
        // Create submission object
        PracticalWorkSubmission submission = new PracticalWorkSubmission(
            currentPracticalWork.getId(),
            currentUser.getId(),
            submissionFileName
        );
        
        // Save to database
        boolean success = PracticalWorkSubmissionService.submitPracticalWork(submission);
        
        if (success) {
            // Hide dialog
            submitPracticalWorkOverlay.setVisible(false);
            
            // Refresh the practical works list
            loadPracticalWorks();
            
            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your work has been submitted successfully!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit your work.");
        }
    }
    
    /**
     * Handles viewing a practical work
     */
    private void handleViewPracticalWork(PracticalWork practicalWork) {
        try {
            // Check if the practical work has a PDF file
            if (practicalWork.getPdfPath() == null || practicalWork.getPdfPath().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "No PDF Available", 
                    "This practical work does not have a PDF file attached.");
                return;
            }
            
            // Load the practical work viewer view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/practical-work-viewer.fxml"));
            Parent practicalWorkViewerParent = loader.load();
            
            // Set up the controller and pass the practical work
            PracticalWorkViewerController controller = loader.getController();
            
            // Check if viewing as teacher or student
            User currentUser = LoginController.getCurrentUser();
            if (currentUser != null && currentUser.getRole().equals("teacher")) {
                // If current user is the teacher who owns this content
                if (teacher != null && teacher.getId() == currentUser.getId()) {
                    // Teacher viewing their own content
                    controller.setPracticalWork(practicalWork, teacher, true);
                } else {
                    // Teacher viewing another teacher's content
                    controller.setPracticalWork(practicalWork, teacher, false);
                }
            } else {
                // Student viewing teacher's content
                controller.setPracticalWork(practicalWork, teacher, false);
            }
            
            // Get the main layout's content area and set the practical work viewer
            StackPane contentArea = (StackPane) practicalWorkCardsContainer.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(practicalWorkViewerParent);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load practical work viewer: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to show alerts
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Handles the action when the back to teachers button is clicked
     */
    @FXML
    private void handleBackToTeachers(ActionEvent event) {
        try {
            // Load the teachers view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teacher-practical-works.fxml"));
            Parent teachersView = loader.load();
            
            // Get the main layout's content area and set the teachers view
            StackPane contentArea = (StackPane) practicalWorkCardsContainer.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(teachersView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate back to teachers view: " + e.getMessage());
        }
    }
} 