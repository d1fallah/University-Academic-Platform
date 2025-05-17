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
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for the student practical works view. Manages the display and interaction
 * with practical works assigned to students, including submission functionality.
 * 
 * @author Sellami Mohamed Odai
 */
public class StudentPracticalWorksController implements Initializable {

    @FXML private FlowPane practicalWorkCardsContainer;
    @FXML private TextField searchField;
    
    @FXML private StackPane submitPracticalWorkOverlay;
    @FXML private BorderPane dialogContainer;
    @FXML private Label practicalWorkTitleLabel;
    @FXML private Label practicalWorkDescriptionLabel;
    @FXML private Label deadlineLabel;
    @FXML private TextArea practicalWorkTitleArea;
    @FXML private TextArea practicalWorkDescriptionArea;
    @FXML private TextArea deadlineArea;
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
     * Sets the teacher for this view and loads their practical works
     * 
     * @param teacher The teacher user whose practical works will be displayed
     */
    public void setTeacher(User teacher) {
        this.teacher = teacher;

        if (teacherNameLabel != null) {
            teacherNameLabel.setText("Prof. " + teacher.getName());
            teacherNameLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");
        }

        try {
            Image profileImg = new Image(getClass().getResourceAsStream("/images/profilep.png"));
            teacherProfileImage.setImage(profileImg);
        } catch (Exception e) {
            System.out.println("Failed to load teacher profile image");
        }
        
        loadTeacherPracticalWorks();
    }
    
    /**
     * Initializes the controller class. Sets up UI components, loads current user, and 
     * configures drag-and-drop functionality.
     * 
     * @param location The location used to resolve relative paths for the root object
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = AuthLoginController.getCurrentUser();
        
        setupDragAndDrop();
        
        if (currentUser != null && currentUser.getRole().equals("student") && 
            practicalWorkCardsContainer != null && searchField != null) {
            
            loadPracticalWorks();
            
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterPracticalWorks(newValue);
            });
        } else if (currentUser != null && !currentUser.getRole().equals("student")) {
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
     * Sets up drag and drop functionality for file uploads
     */
    private void setupDragAndDrop() {
        if (dropArea == null) {
            return;
        }
        
        dropArea.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles() && isAcceptableFile(event.getDragboard().getFiles().get(0))) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
        
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
    
    /**
     * Checks if a file is acceptable for upload based on the current context
     * 
     * @param file The file to check
     * @return true if file is acceptable, false otherwise
     */
    private boolean isAcceptableFile(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        
        String fileName = file.getName().toLowerCase();
        if (submitPracticalWorkOverlay != null && submitPracticalWorkOverlay.isVisible()) {
            return fileName.endsWith(".zip");
        } else {
            return fileName.endsWith(".pdf");
        }
    }
    
    /**
     * Handles the file selection action
     * 
     * @param event The action event
     */
    @FXML
    private void handleSelectFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        
        if (submitPracticalWorkOverlay != null && submitPracticalWorkOverlay.isVisible()) {
            fileChooser.setTitle("Select ZIP File");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("ZIP Files", "*.zip")
            );
        } else {
            fileChooser.setTitle("Select PDF File");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
        }
        
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        File file = fileChooser.showOpenDialog(source.getScene().getWindow());
                
        if (file != null) {
            handleFileSelected(file);
        }
    }
    
    /**
     * Updates the UI when a file has been selected
     * 
     * @param file The selected file
     */
    private void handleFileSelected(File file) {
        if (selectedFileLabel == null) {
            return;
        }
        
        selectedFile = file;
        selectedFileLabel.setText(file.getName());
        selectedFileLabel.setStyle("-fx-text-fill: white;");
    }
    
    /**
     * Loads and displays all practical works accessible to the current student
     */
    private void loadPracticalWorks() {
        practicalWorkCardsContainer.getChildren().clear();
        
        List<PracticalWork> allPracticalWorks = getAllPracticalWorks();
        practicalWorksList.setAll(allPracticalWorks);
        
        if (practicalWorksList.isEmpty()) {
            Label noPracticalWorksLabel = new Label("There are no practical works available at the moment.");
            noPracticalWorksLabel.getStyleClass().add("no-courses-message");
            noPracticalWorksLabel.setPadding(new Insets(50, 0, 0, 0));
            practicalWorkCardsContainer.getChildren().add(noPracticalWorksLabel);
        } else {
            for (PracticalWork practicalWork : practicalWorksList) {
                practicalWorkCardsContainer.getChildren().add(createPracticalWorkCard(practicalWork));
            }
        }
    }
    
    /**
     * Retrieves all practical works from all courses
     * 
     * @return List of practical works
     */
    private List<PracticalWork> getAllPracticalWorks() {
        List<PracticalWork> allWorks = new ArrayList<>();
        List<Course> courses = CourseService.getAllCourses();
        
        for (Course course : courses) {
            allWorks.addAll(PracticalWorkService.getPracticalWorksByCourseId(course.getId()));
        }
        
        return allWorks;
    }

    /**
     * Creates a visual card representation for a practical work
     * 
     * @param practicalWork The practical work to create a card for
     * @return A StackPane containing the practical work card UI
     */
    private StackPane createPracticalWorkCard(PracticalWork practicalWork) {
        StackPane cardPane = new StackPane();
        cardPane.getStyleClass().add("course-card");
        cardPane.setPrefWidth(480);
        cardPane.setPrefHeight(250);

        ImageView cardBackground = new ImageView();
        try {
            Image bgImage = new Image(getClass().getResourceAsStream("/images/courseCardBackground.png"));
            cardBackground.setImage(bgImage);
            cardBackground.setFitWidth(480);
            cardBackground.setFitHeight(310);
            cardBackground.setPreserveRatio(false);
            cardBackground.setOpacity(0.7);
        } catch (Exception e) {
            cardPane.setStyle("-fx-background-color: #353535;");
        }

        VBox cardContent = new VBox();
        cardContent.getStyleClass().add("card-content");
        cardContent.setSpacing(15);
        cardContent.setPadding(new Insets(18, 20, 18, 20));
        cardContent.setPrefWidth(480);
        cardContent.setPrefHeight(250);

        // Header with title and icon
        HBox headerBox = createHeaderBox(practicalWork);
        
        // Description
        Label descriptionLabel = createDescriptionLabel(practicalWork);
        
        // Course info
        HBox courseBox = createCourseBox(practicalWork);
        
        // Progress tracking
        VBox progressBox = createProgressBox(practicalWork);
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Footer with deadline and buttons
        HBox footerBox = createFooterBox(practicalWork);

        cardContent.getChildren().addAll(headerBox, descriptionLabel, courseBox, progressBox, spacer, footerBox);
        cardPane.getChildren().addAll(cardBackground, cardContent);
        cardPane.setAccessibleText("Practical Work: " + practicalWork.getTitle() + ", " + 
                                  (practicalWork.getDescription() != null ? practicalWork.getDescription() : ""));

        return cardPane;
    }
    
    /**
     * Creates the header box for a practical work card
     * 
     * @param practicalWork The practical work
     * @return HBox containing the header elements
     */
    private HBox createHeaderBox(PracticalWork practicalWork) {
        HBox headerBox = new HBox();
        headerBox.getStyleClass().add("card-header");
        headerBox.setAlignment(Pos.TOP_LEFT);
        headerBox.setPrefWidth(480);
        headerBox.setSpacing(20);

        VBox titleContainer = new VBox();
        titleContainer.setAlignment(Pos.TOP_LEFT);
        titleContainer.setPrefWidth(390);
        HBox.setHgrow(titleContainer, Priority.ALWAYS);

        Label titleLabel = new Label(practicalWork.getTitle());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);
        titleLabel.setMinHeight(Region.USE_PREF_SIZE);
        titleContainer.getChildren().add(titleLabel);

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

        headerBox.getChildren().addAll(titleContainer, logoContainer);
        return headerBox;
    }
    
    /**
     * Creates the description label for a practical work card
     * 
     * @param practicalWork The practical work
     * @return Label containing the description
     */
    private Label createDescriptionLabel(PracticalWork practicalWork) {
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
        return descriptionLabel;
    }
    
    /**
     * Creates the course info box for a practical work card
     * 
     * @param practicalWork The practical work
     * @return HBox containing course information
     */
    private HBox createCourseBox(PracticalWork practicalWork) {
        HBox courseBox = new HBox();
        courseBox.setAlignment(Pos.CENTER_LEFT);
        courseBox.setSpacing(5);

        Course course = CourseService.getCourseById(practicalWork.getCourseId());
        String courseName = course != null ? course.getTitle() : "Unknown Course";

        Label courseLabel = new Label("Course: " + courseName);
        courseLabel.getStyleClass().add("card-instructor");
        courseLabel.setStyle("-fx-text-fill: white;");

        courseBox.getChildren().add(courseLabel);
        return courseBox;
    }
    
    /**
     * Creates the progress tracking box for a practical work card
     * 
     * @param practicalWork The practical work
     * @return VBox containing progress information and visualization
     */
    private VBox createProgressBox(PracticalWork practicalWork) {
        VBox progressBox = new VBox(3);
        progressBox.setAlignment(Pos.CENTER_LEFT);
        progressBox.setPadding(new Insets(5, 0, 0, 0));

        DeadlineInfo deadlineInfo = calculateDeadlineInfo(practicalWork);

        HBox progressLabelBox = new HBox();
        progressLabelBox.setAlignment(Pos.CENTER_LEFT);

        Label progressLabel = new Label("Duration Passed " + deadlineInfo.progressPercentage + "%");
        progressLabel.setStyle("-fx-text-fill: " + deadlineInfo.progressColor + 
                             "; -fx-font-size: 13px; -fx-font-weight: bold;");
        progressLabelBox.getChildren().add(progressLabel);

        ProgressBar deadlineBar = new ProgressBar((double) deadlineInfo.progressPercentage / 100);
        deadlineBar.setPrefWidth(400);
        deadlineBar.setPrefHeight(10);
        deadlineBar.setMinHeight(10);
        deadlineBar.setMaxHeight(10);
        deadlineBar.getStyleClass().add("performance-bar");

        String barStyle = "-fx-accent: " + deadlineInfo.progressColor + ";" +
                        "-fx-background-color: #333333;" +
                        "-fx-background-radius: 5px;" +
                        "-fx-background-insets: 0;" +
                        "-fx-border-radius: 5px;";
        deadlineBar.setStyle(barStyle);
        HBox.setHgrow(deadlineBar, Priority.ALWAYS);

        progressBox.getChildren().addAll(progressLabelBox, deadlineBar);
        return progressBox;
    }
    
    /**
     * Helper class to hold deadline calculation results
     */
    private static class DeadlineInfo {
        int progressPercentage;
        String timeStatus;
        String progressColor;
        boolean deadlinePassed;
    }
    
    /**
     * Calculates deadline-related information for a practical work
     * 
     * @param practicalWork The practical work
     * @return DeadlineInfo object with calculated values
     */
    private DeadlineInfo calculateDeadlineInfo(PracticalWork practicalWork) {
        DeadlineInfo info = new DeadlineInfo();
        info.progressPercentage = 0;
        info.timeStatus = "No deadline set";
        info.progressColor = "#10b981"; // Default green
        info.deadlinePassed = false;

        if (practicalWork.getDeadline() != null) {
            java.util.Date currentDate = new java.util.Date();

            long creationTime = practicalWork.getCreatedAt() != null ?
                    practicalWork.getCreatedAt().getTime() : currentDate.getTime();
            long deadlineTime = practicalWork.getDeadline().getTime();
            long currentTime = currentDate.getTime();

            long totalDuration = deadlineTime - creationTime;
            long elapsedTime = currentTime - creationTime;
            long remainingTime = deadlineTime - currentTime;

            info.deadlinePassed = remainingTime <= 0;

            if (totalDuration > 0) {
                info.progressPercentage = (int)((elapsedTime * 100) / totalDuration);

                if (info.progressPercentage > 100) {
                    info.progressPercentage = 100;
                    info.timeStatus = "Finished";
                    info.progressColor = "#f43f5e"; // Red for overdue
                } else {
                    if (remainingTime > 0) {
                        info.timeStatus = formatRemainingTime(remainingTime);
                    }

                    if (info.progressPercentage >= 75) {
                        info.progressColor = "#f43f5e"; // Red for near deadline
                    } else if (info.progressPercentage >= 50) {
                        info.progressColor = "#f59e0b"; // Orange/Yellow
                    } else {
                        info.progressColor = "#10b981"; // Green
                    }
                }
            }
        }
        
        return info;
    }
    
    /**
     * Formats remaining time into a human-readable string
     * 
     * @param remainingTime Time in milliseconds
     * @return Formatted time string
     */
    private String formatRemainingTime(long remainingTime) {
        long days = remainingTime / (1000 * 60 * 60 * 24);
        long hours = (remainingTime % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        
        if (days > 0) {
            return days + (days == 1 ? " day " : " days ") + 
                  hours + (hours == 1 ? " hour" : " hours") + " remaining";
        } else {
            long minutes = (remainingTime % (1000 * 60 * 60)) / (1000 * 60);
            if (hours > 0) {
                return hours + (hours == 1 ? " hour " : " hours ") + 
                      minutes + (minutes == 1 ? " minute" : " minutes") + " remaining";
            } else {
                return minutes + (minutes == 1 ? " minute" : " minutes") + " remaining";
            }
        }
    }
    
    /**
     * Creates the footer box for a practical work card with deadline and action buttons
     * 
     * @param practicalWork The practical work
     * @return HBox containing the footer elements
     */
    private HBox createFooterBox(PracticalWork practicalWork) {
        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.BOTTOM_LEFT);
        footerBox.setPrefWidth(480);

        DeadlineInfo deadlineInfo = calculateDeadlineInfo(practicalWork);
        
        // Date section
        HBox dateBox = new HBox();
        dateBox.getStyleClass().add("card-date");
        dateBox.setAlignment(Pos.CENTER_LEFT);
        dateBox.setPrefHeight(24);
        dateBox.setSpacing(8);
        HBox.setHgrow(dateBox, Priority.ALWAYS);

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

        Label dateLabel = new Label(deadlineInfo.timeStatus);
        dateLabel.getStyleClass().add("date-label");
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + deadlineInfo.progressColor + ";");
        dateBox.getChildren().addAll(calendarIcon, dateLabel);

        // Buttons section
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button viewButton = new Button("View PW");
        viewButton.getStyleClass().add("view-course-button");
        viewButton.setStyle("-fx-background-color: #d97706;");
        viewButton.setPrefWidth(120);
        viewButton.setPrefHeight(24);
        viewButton.setOnAction(e -> handleViewPracticalWork(practicalWork));

        Button submitButton = new Button("Submit");
        submitButton.getStyleClass().add("view-course-button");
        submitButton.setStyle("-fx-background-color: #059669;");
        submitButton.setPrefWidth(120);
        submitButton.setPrefHeight(24);
        submitButton.setOnAction(e -> handleSubmitPracticalWork(practicalWork));
        
        boolean hasSubmitted = false;
        if (currentUser != null && currentUser.getRole().equals("student")) {
            hasSubmitted = PracticalWorkSubmissionService.hasStudentSubmitted(
                practicalWork.getId(), currentUser.getId());
        }
        
        if (hasSubmitted) {
            submitButton.setDisable(true);
            submitButton.setText("Submitted");
            submitButton.setStyle("-fx-background-color: #6B7280;");
        } else if (deadlineInfo.deadlinePassed) {
            submitButton.setDisable(true);
            submitButton.setText("Finished");
            submitButton.setStyle("-fx-background-color: #f43f5e;");
        }

        buttonBox.getChildren().addAll(viewButton, submitButton);
        footerBox.getChildren().addAll(dateBox, buttonBox);
        
        return footerBox;
    }
    
    /**
     * Filters practical works based on search query
     * 
     * @param query The search query
     */
    private void filterPracticalWorks(String query) {
        if (query == null || query.isEmpty()) {
            loadPracticalWorks();
        } else {
            query = query.toLowerCase();
            final String searchQuery = query;
            
            List<PracticalWork> filteredList = practicalWorksList.stream()
                .filter(practicalWork -> 
                    practicalWork.getTitle().toLowerCase().contains(searchQuery) ||
                    (practicalWork.getDescription() != null &&
                     practicalWork.getDescription().toLowerCase().contains(searchQuery))
                )
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
     * 
     * @param practicalWork The practical work to submit
     */
    private void handleSubmitPracticalWork(PracticalWork practicalWork) {
        if (submitPracticalWorkOverlay == null || 
            selectedFileLabel == null || 
            practicalWorkTitleArea == null || 
            practicalWorkDescriptionArea == null || 
            deadlineArea == null) {
            
            System.out.println("Required UI components for submission are missing");
            if (practicalWorkCardsContainer != null && practicalWorkCardsContainer.getScene() != null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Submission form cannot be displayed");
            }
            return;
        }
        
        if (currentUser != null && currentUser.getRole().equals("student") && 
            PracticalWorkSubmissionService.hasStudentSubmitted(practicalWork.getId(), currentUser.getId())) {
            showAlert(Alert.AlertType.INFORMATION, "Already Submitted", 
                     "You have already submitted this practical work. You cannot submit again.");
            return;
        }
        
        if (practicalWork.getDeadline() != null) {
            java.util.Date currentDate = new java.util.Date();
            if (currentDate.after(practicalWork.getDeadline())) {
                showAlert(Alert.AlertType.WARNING, "Deadline Passed", 
                         "The deadline for this practical work has passed. You can no longer submit.");
                return;
            }
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Submission");
        confirmAlert.setHeaderText("You can only submit once");
        confirmAlert.setContentText("You will not be able to submit this practical work again. Are you sure you want to continue?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            selectedFile = null;
            selectedFileLabel.setText("No file selected");
            selectedFileLabel.setStyle("-fx-text-fill: #888888;");
            
            currentPracticalWork = practicalWork;
            
            practicalWorkTitleArea.setText(practicalWork.getTitle());
            practicalWorkDescriptionArea.setText(practicalWork.getDescription());
            deadlineArea.setText(practicalWork.getDeadline() != null ? 
                                practicalWork.getDeadline().toString() : "No deadline");
            
            submitPracticalWorkOverlay.setVisible(true);
        }
    }
    
    /**
     * Handles the cancel button action in the submission dialog
     * 
     * @param event The action event
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        if (submitPracticalWorkOverlay != null) {
            submitPracticalWorkOverlay.setVisible(false);
        }
    }
    
    /**
     * Handles the search action
     * 
     * @param event The action event
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
     * 
     * @param event The action event
     */
    @FXML
    private void handleSubmit(ActionEvent event) {
        if (submitPracticalWorkOverlay == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Submission form is not available");
            return;
        }
        
        if (selectedFile == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a ZIP file to submit.");
            return;
        }
        
        if (!selectedFile.getName().toLowerCase().endsWith(".zip")) {
            showAlert(Alert.AlertType.WARNING, "Invalid File Type", "Please select a ZIP file");
            return;
        }
        
        if (currentPracticalWork == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No practical work selected for submission.");
            return;
        }
        
        String submissionFileName = null;
        try {
            String submissionsDir = "submissions";
            Path dirPath = Paths.get(submissionsDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            
            submissionFileName = currentUser.getId() + "_" + currentPracticalWork.getId() + "_" + 
                             System.currentTimeMillis() + "_" + selectedFile.getName();
            Path targetPath = dirPath.resolve(submissionFileName);
            
            Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "File Error", "Could not save your submission file.");
            return;
        }
        
        PracticalWorkSubmission submission = new PracticalWorkSubmission(
            currentPracticalWork.getId(),
            currentUser.getId(),
            submissionFileName
        );
        
        boolean success = PracticalWorkSubmissionService.submitPracticalWork(submission);
        
        if (success) {
            submitPracticalWorkOverlay.setVisible(false);
            
            selectedFile = null;
            selectedFileLabel.setText("No file selected");
            selectedFileLabel.setStyle("-fx-text-fill: #888888;");
            
            showAlert(Alert.AlertType.INFORMATION, "Success", 
                     "Your work has been submitted successfully! You cannot submit again for this practical work.");
            
            // If we're in teacher-specific view, reload only that teacher's practical works
            if (teacher != null) {
                loadTeacherPracticalWorks();
            } else {
                loadPracticalWorks();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit your work.");
        }
    }
    
    /**
     * Handles viewing a practical work
     * 
     * @param practicalWork The practical work to view
     */
    private void handleViewPracticalWork(PracticalWork practicalWork) {
        try {
            if (practicalWork.getPdfPath() == null || practicalWork.getPdfPath().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "No PDF Available", 
                    "This practical work does not have a PDF file attached.");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PdfPracticalWorkViewer.fxml"));
            Parent practicalWorkViewerParent = loader.load();
            
            ViewPracticalWorkController controller = loader.getController();
            
            User currentUser = AuthLoginController.getCurrentUser();
            if (currentUser != null && currentUser.getRole().equals("teacher")) {
                if (teacher != null && teacher.getId() == currentUser.getId()) {
                    controller.setPracticalWork(practicalWork, teacher, true);
                } else {
                    controller.setPracticalWork(practicalWork, teacher, false);
                }
            } else {
                controller.setPracticalWork(practicalWork, teacher, false);
            }
            
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
     * 
     * @param alertType The type of alert
     * @param title The alert title
     * @param message The alert message
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
     * 
     * @param event The action event
     */
    @FXML
    private void handleBackToTeachers(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TeachersCards.fxml"));
            Parent teachersView = loader.load();
            
            TeachersCardsController controller = loader.getController();
            controller.setIsPracticalWorkView(true);
            
            if (currentUser != null && currentUser.getRole().equals("teacher")) {
                controller.setExcludeCurrentTeacher(true);
                controller.setShowManageCourseButton(true);
            }
            
            StackPane contentArea = (StackPane) practicalWorkCardsContainer.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(teachersView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate back to teachers view: " + e.getMessage());
        }
    }

    /**
     * Loads practical works from a specific teacher filtered by student's educational level
     */
    private void loadTeacherPracticalWorks() {
        if (teacher == null) {
            return;
        }
        
        practicalWorkCardsContainer.getChildren().clear();
        
        List<PracticalWork> teacherPracticalWorks;
        
        if (currentUser != null && currentUser.getRole().equals("student")) {
            String studentLevel = currentUser.getEnrollmentLevel();
            if (studentLevel == null || studentLevel.isEmpty()) {
                studentLevel = "L1";
            }
            teacherPracticalWorks = PracticalWorkService.getPracticalWorksByTeacherAndLevel(teacher.getId(), studentLevel);
        } else {
            teacherPracticalWorks = PracticalWorkService.getPracticalWorksByTeacherId(teacher.getId());
        }
        
        practicalWorksList.setAll(teacherPracticalWorks);
        
        if (practicalWorksList.isEmpty()) {
            Label noPracticalWorksLabel = new Label("No practical works available for your enrollment level from this teacher yet.");
            noPracticalWorksLabel.getStyleClass().add("no-courses-message");
            noPracticalWorksLabel.setPadding(new Insets(50, 0, 0, 0));
            practicalWorkCardsContainer.getChildren().add(noPracticalWorksLabel);
        } else {
            for (PracticalWork practicalWork : practicalWorksList) {
                practicalWorkCardsContainer.getChildren().add(createPracticalWorkCard(practicalWork));
            }
        }
    }
}
