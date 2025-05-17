package app.frontend;

import app.backend.models.Course;
import app.backend.models.PracticalWork;
import app.backend.models.User;
import app.backend.services.CourseService;
import app.backend.services.PracticalWorkService;
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
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.scene.control.Tooltip;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller class for managing teacher practical works interface.
 * Allows teachers to create, view, edit, and delete practical works,
 * as well as view student submissions.
 * 
 * @author Sellami Mohamed Odai
 */
public class TeacherPracticalWorksController implements Initializable {

    /** Container for practical work cards */
    @FXML private FlowPane practicalWorkCardsContainer;
    /** Search field for filtering practical works */
    @FXML private TextField searchField;
    /** Button to add new practical work */
    @FXML private Button addPracticalWorkButton;
    
    /** Main overlay for add/edit practical work dialog */
    @FXML private StackPane addPracticalWorkOverlay;
    /** Container for the dialog content */
    @FXML private BorderPane dialogContainer;
    /** Dialog title label */
    @FXML private Label dialogTitleLabel;
    /** Field for practical work name input */
    @FXML private TextField practicalWorkNameField;
    /** Field for practical work description input */
    @FXML private TextArea practicalWorkDescriptionField;
    /** Dropdown for course selection */
    @FXML private ComboBox<String> courseComboBox;
    /** Dropdown for education level selection */
    @FXML private ComboBox<String> levelComboBox;
    /** Date picker for deadline selection */
    @FXML private DatePicker deadlinePicker;
    /** Label showing selected file name */
    @FXML private Label selectedFileLabel;
    /** Button to select PDF file */
    @FXML private Button selectFileButton;
    /** Area for drag and drop file upload */
    @FXML private StackPane dropArea;
    
    /** Current logged-in user */
    private User currentUser;
    /** List of practical works for the current teacher */
    private ObservableList<PracticalWork> practicalWorksList = FXCollections.observableArrayList();
    /** List of courses for the current teacher */
    private ObservableList<Course> coursesList = FXCollections.observableArrayList();
    /** Currently selected file for upload */
    private File selectedFile = null;
    /** Filename for the practical work PDF */
    private String practicalWorkFileName = null;
    /** Flag indicating if in edit mode */
    private boolean isEditMode = false;
    /** ID of the practical work being edited */
    private int editingPracticalWorkId = -1;

    /**
     * Initializes the controller class.
     * Sets up the UI components, loads data, and verifies user access rights.
     * 
     * @param location The location used to resolve relative paths for root object
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = AuthLoginController.getCurrentUser();
        
        if (currentUser == null || !currentUser.getRole().equals("teacher")) {
            showAlert(Alert.AlertType.WARNING, "Access Error", "Only teachers can access this page.");
            return;
        }
        
        setupDragAndDrop();
        loadTeacherPracticalWorks();
        loadTeacherCourses();
        setupDatePicker();
    }
    
    /**
     * Loads all courses created by the current teacher and 
     * populates the course combo box with their titles.
     */
    private void loadTeacherCourses() {
        if (currentUser != null) {
            coursesList.clear();
            List<Course> teacherCourses = CourseService.getCoursesByTeacherId(currentUser.getId());
            if (teacherCourses != null) {
                coursesList.addAll(teacherCourses);
                
                if (courseComboBox != null) {
                    courseComboBox.getItems().clear();
                    for (Course course : coursesList) {
                        courseComboBox.getItems().add(course.getTitle());
                    }
                }
            }
        }
    }
    
    /**
     * Loads all practical works created by the current teacher and displays them
     * as cards in the container. Shows a message if no practical works exist.
     */
    private void loadTeacherPracticalWorks() {
        practicalWorkCardsContainer.getChildren().clear();
        
        List<PracticalWork> teacherPracticalWorks = PracticalWorkService.getPracticalWorksByTeacherId(currentUser.getId());
        practicalWorksList.setAll(teacherPracticalWorks);
        
        if (practicalWorksList.isEmpty()) {
            Label noPracticalWorksLabel = new Label("You haven't created any practical works yet. Click the 'Add new practical work +' button to get started!");
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
     * Creates a visual card representation for a practical work that includes title,
     * description, deadline progress, and action buttons for viewing, editing, and deletion.
     * 
     * @param practicalWork The practical work to display as a card
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
            cardBackground.setFitHeight(295);
            cardBackground.setPreserveRatio(false);
            cardBackground.setOpacity(0.7);
        } catch (Exception e) {
            System.out.println("Failed to load background image for practical work card");
            cardPane.setStyle("-fx-background-color: #353535;");
        }

        VBox cardContent = new VBox();
        cardContent.getStyleClass().add("card-content");
        cardContent.setSpacing(15);
        cardContent.setPadding(new Insets(18, 20, 18, 20));
        cardContent.setPrefWidth(480);
        cardContent.setPrefHeight(250);

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
        String courseName = "Unknown Course";
        if (course != null) {
            courseName = course.getTitle();
            System.out.println("Card for practical work: " + practicalWork.getTitle() + " is using course: " + courseName + " (ID: " + course.getId() + ")");
        } else {
            System.out.println("Warning: Could not find course with ID: " + practicalWork.getCourseId() + " for practical work: " + practicalWork.getTitle());
        }
        
        // Course info label
        HBox courseBox = new HBox();
        courseBox.setAlignment(Pos.CENTER_LEFT);
        courseBox.setSpacing(5);
        
        Label courseLabel = new Label("Course: " + courseName);
        courseLabel.getStyleClass().add("card-instructor");
        courseLabel.setStyle("-fx-text-fill: white;");
        
        courseBox.getChildren().addAll(courseLabel);
        
        VBox progressBox = new VBox(3);
        progressBox.setAlignment(Pos.CENTER_LEFT);
        progressBox.setPadding(new Insets(5, 0, 0, 0));
        
        int progressPercentage = 0;
        String timeStatus = "No deadline set";
        String progressColor = "#10b981";
        
        if (practicalWork.getDeadline() != null) {
            java.util.Date currentDate = new java.util.Date();
            
            long creationTime = practicalWork.getCreatedAt() != null ? 
                practicalWork.getCreatedAt().getTime() : currentDate.getTime();
            long deadlineTime = practicalWork.getDeadline().getTime();
            long currentTime = currentDate.getTime();
            
            long totalDuration = deadlineTime - creationTime;
            long elapsedTime = currentTime - creationTime;
            long remainingTime = deadlineTime - currentTime;
            
            if (totalDuration > 0) {
                progressPercentage = (int)((elapsedTime * 100) / totalDuration);
                
                if (progressPercentage > 100) {
                    progressPercentage = 100;
                    timeStatus = "Finished";
                    progressColor = "#f43f5e";
                } else {
                    if (remainingTime > 0) {
                        long days = remainingTime / (1000 * 60 * 60 * 24);
                        long hours = (remainingTime % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
                        
                        if (days > 0) {
                            timeStatus = days + (days == 1 ? " day " : " days ") + hours + (hours == 1 ? " hour" : " hours") + " remaining";
                        } else {
                            long minutes = (remainingTime % (1000 * 60 * 60)) / (1000 * 60);
                            if (hours > 0) {
                                timeStatus = hours + (hours == 1 ? " hour " : " hours ") + minutes + (minutes == 1 ? " minute" : " minutes") + " remaining";
                            } else {
                                timeStatus = minutes + (minutes == 1 ? " minute" : " minutes") + " remaining";
                            }
                        }
                    }
                    
                    if (progressPercentage >= 75) {
                        progressColor = "#f43f5e";
                    } else if (progressPercentage >= 50) {
                        progressColor = "#f59e0b";
                    } else {
                        progressColor = "#10b981";
                    }
                }
            }
        }
        
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
        
        String barStyle = "-fx-accent: " + progressColor + ";" +
                          "-fx-background-color: #333333;" +
                          "-fx-background-radius: 5px;" + 
                          "-fx-background-insets: 0;" +
                          "-fx-border-radius: 5px;";
        
        deadlineBar.setStyle(barStyle);
        HBox.setHgrow(deadlineBar, Priority.ALWAYS);
        
        progressBox.getChildren().addAll(progressLabelBox, deadlineBar);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Footer section with creation date and buttons
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
        HBox buttonBox = new HBox(8); // Same spacing as exercise cards
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // View button
        Button viewButton = new Button();
        viewButton.getStyleClass().add("icon-button");
        viewButton.setPrefWidth(24);
        viewButton.setPrefHeight(24);
        viewButton.setOnAction(e -> handleViewPracticalWork(practicalWork));
        
        // View icon
        ImageView viewIcon = new ImageView();
        try {
            Image eyeImage = new Image(getClass().getResourceAsStream("/images/eye.png"));
            viewIcon.setImage(eyeImage);
            viewIcon.setFitWidth(15);
            viewIcon.setFitHeight(15);
            viewIcon.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Failed to load eye icon");
        }
        viewButton.setGraphic(viewIcon);

        // View Submissions button
        Button viewSubmissionsButton = new Button();
        viewSubmissionsButton.getStyleClass().add("icon-button");
        viewSubmissionsButton.setPrefWidth(24);
        viewSubmissionsButton.setPrefHeight(24);
        viewSubmissionsButton.setOnAction(e -> handleViewSubmissions(practicalWork));
        
        // Use emoji for the icon
        Label usersLabel = new Label("👥");
        usersLabel.setStyle("-fx-font-size: 13px;");
        viewSubmissionsButton.setGraphic(usersLabel);
        
        // Add tooltip to make function clear
        Tooltip submissionsTooltip = new Tooltip("View Student Submissions");
        viewSubmissionsButton.setTooltip(submissionsTooltip);

        // Edit button
        Button editButton = new Button();
        editButton.getStyleClass().add("icon-button");
        editButton.setPrefWidth(24);
        editButton.setPrefHeight(24);
        editButton.setOnAction(e -> handleEditPracticalWork(practicalWork));
        
        // Edit icon
        ImageView editIcon = new ImageView();
        try {
            Image penImage = new Image(getClass().getResourceAsStream("/images/Pen.png"));
            editIcon.setImage(penImage);
            editIcon.setFitWidth(15);
            editIcon.setFitHeight(15);
            editIcon.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Failed to load pen icon");
        }
        editButton.setGraphic(editIcon);

        // Delete button
        Button deleteButton = new Button();
        deleteButton.getStyleClass().add("icon-button");
        deleteButton.setPrefWidth(24);
        deleteButton.setPrefHeight(24);
        deleteButton.setOnAction(e -> handleDeletePracticalWork(practicalWork));
        
        // Delete icon
        ImageView deleteIcon = new ImageView();
        try {
            Image trashImage = new Image(getClass().getResourceAsStream("/images/Trash.png"));
            deleteIcon.setImage(trashImage);
            deleteIcon.setFitWidth(15);
            deleteIcon.setFitHeight(15);
            deleteIcon.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Failed to load trash icon");
        }
        deleteButton.setGraphic(deleteIcon);

        buttonBox.getChildren().addAll(viewButton, viewSubmissionsButton, editButton, deleteButton);

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
     * Sets up file drag and drop functionality for PDF uploads.
     * Configures event handlers for handling file dragging and dropping.
     */
    private void setupDragAndDrop() {
        if (dropArea == null) return;
        
        dropArea.setOnDragOver(event -> {
            if (event.getGestureSource() != dropArea && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });
        
        // Handle drag entered (visual feedback)
        dropArea.setOnDragEntered(event -> {
            if (event.getGestureSource() != dropArea && event.getDragboard().hasFiles()) {
                dropArea.getStyleClass().add("drag-over");
            }
            event.consume();
        });
        
        // Handle drag exited (remove visual feedback)
        dropArea.setOnDragExited(event -> {
            dropArea.getStyleClass().remove("drag-over");
            event.consume();
        });
        
        // Handle drag dropped (process the file)
        dropArea.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            
            if (db.hasFiles()) {
                // Get the first file (if multiple are dropped)
                File file = db.getFiles().get(0);
                
                // Check if it's a PDF file
                if (file.getName().toLowerCase().endsWith(".pdf")) {
                    selectedFile = file;
                    selectedFileLabel.setText(file.getName());
                    success = true;
                } else {
                    showAlert(Alert.AlertType.WARNING, "Invalid File", "Please select a PDF file only.");
                }
            }
            
            event.setDropCompleted(success);
            event.consume();
        });
    }

    /**
     * Handles the search action by filtering practical works based on search text.
     * Displays all works if search field is empty, or filters by title and description.
     * 
     * @param event The action event triggered by the search button
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String searchText = searchField.getText().trim().toLowerCase();
        
        if (searchText.isEmpty()) {
            loadTeacherPracticalWorks();
        } else {
            practicalWorkCardsContainer.getChildren().clear();
            
            List<PracticalWork> filteredList = practicalWorksList.stream()
                .filter(practicalWork -> 
                    practicalWork.getTitle().toLowerCase().contains(searchText) ||
                    (practicalWork.getDescription() != null && 
                     practicalWork.getDescription().toLowerCase().contains(searchText)))
                .collect(Collectors.toList());
            
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
     * Handles the add new practical work button click.
     * Opens the dialog overlay in create mode with empty fields.
     * 
     * @param event The action event triggered by the add button
     */
    @FXML
    private void handleAddNewPracticalWork(ActionEvent event) {
        isEditMode = false;
        editingPracticalWorkId = -1;
        
        // Set dialog title
        dialogTitleLabel.setText("Add New Practical Work");
        
        // Clear all fields
        clearInputFields();
        
        // Show the overlay
        addPracticalWorkOverlay.setVisible(true);
    }
    
    /**
     * Handles the select file button click to open a file chooser dialog.
     * Allows user to select a PDF file for the practical work.
     * 
     * @param event The action event triggered by the select file button
     */
    @FXML
    private void handleSelectFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select PDF File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        
        File file = fileChooser.showOpenDialog(
            ((Button) event.getSource()).getScene().getWindow()
        );
        
        if (file != null) {
            selectedFile = file;
            selectedFileLabel.setText(file.getName());
        }
    }
    
    /**
     * Handles the cancel button click in the dialog overlay.
     * Hides the overlay and clears all input fields.
     * 
     * @param event The action event triggered by the cancel button
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        // Hide the overlay
        addPracticalWorkOverlay.setVisible(false);
        
        // Clear all fields
        clearInputFields();
    }
    
    /**
     * Handles the save button click in the dialog overlay.
     * Validates inputs and either creates a new practical work or updates an existing one.
     * 
     * @param event The action event triggered by the save button
     */
    @FXML
    private void handleSave(ActionEvent event) {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }
        
        // Get the selected course title from the combo box
        String selectedCourse = courseComboBox.getSelectionModel().getSelectedItem();
        System.out.println("Selected course from dropdown: " + selectedCourse);
        
        // Get the course ID
        int courseId = extractCourseId(selectedCourse);
        System.out.println("Extracted course ID: " + courseId);
        
        if (courseId == -1) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not determine the selected course. Please select a course again.");
            return;
        }
        
        if (isEditMode) {
            // Update the existing practical work
            handleUpdatePracticalWork(editingPracticalWorkId, courseId);
        } else {
            // Create a new practical work
            // Get the course's education level
            String targetLevel = getCourseLevel(courseId);
            
            // Copy the selected PDF to the practical works directory
            if (selectedFile != null) {
                try {
                    // Ensure directory exists
                    String practicalWorksDir = "practical_works";
                    Path dirPath = Paths.get(practicalWorksDir);
                    if (!Files.exists(dirPath)) {
                        Files.createDirectories(dirPath);
                    }
                    
                    // Generate unique filename
                    practicalWorkFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                    Path targetPath = dirPath.resolve(practicalWorkFileName);
                    
                    // Copy the file
                    Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "File Error", "Could not save the practical work PDF file.");
                    return;
                }
            }
            
            // Create and save the new practical work
            PracticalWork newPracticalWork = new PracticalWork();
            newPracticalWork.setTitle(practicalWorkNameField.getText().trim());
            newPracticalWork.setDescription(practicalWorkDescriptionField.getText().trim());
            newPracticalWork.setCourseId(courseId);
            newPracticalWork.setTeacherId(currentUser.getId());
            newPracticalWork.setTargetLevel(targetLevel);
            newPracticalWork.setComment(""); // Set an empty comment by default
            
            // Set deadline if selected
            if (deadlinePicker.getValue() != null) {
                newPracticalWork.setDeadline(Date.valueOf(deadlinePicker.getValue()));
            }
            
            // Set PDF path if file was uploaded
            if (practicalWorkFileName != null) {
                newPracticalWork.setPdfPath("practical_works/" + practicalWorkFileName);
            }
            
            // Save to database
            boolean success = PracticalWorkService.addPracticalWork(newPracticalWork);
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Practical work created successfully!");
                
                // Reload practical works
                loadTeacherPracticalWorks();
                
                // Hide the overlay
                addPracticalWorkOverlay.setVisible(false);
                
                // Clear all fields
                clearInputFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create practical work.");
            }
        }
    }
    
    /**
     * Handles editing an existing practical work.
     * Opens the dialog overlay in edit mode and populates fields with existing data.
     * 
     * @param practicalWork The practical work to be edited
     */
    private void handleEditPracticalWork(PracticalWork practicalWork) {
        isEditMode = true;
        editingPracticalWorkId = practicalWork.getId();
        
        // Set dialog title
        dialogTitleLabel.setText("Edit Practical Work");
        
        // Fill in the fields with existing data
        practicalWorkNameField.setText(practicalWork.getTitle());
        practicalWorkDescriptionField.setText(practicalWork.getDescription());
        
        // Set the course in the combo box
        boolean courseFound = false;
        for (Course course : coursesList) {
            if (course.getId() == practicalWork.getCourseId()) {
                courseComboBox.getSelectionModel().select(course.getTitle());
                courseFound = true;
                System.out.println("Selected course: " + course.getTitle() + " (ID: " + course.getId() + ")");
                break;
            }
        }
        
        if (!courseFound) {
            System.out.println("Warning: Could not find course with ID: " + practicalWork.getCourseId());
            System.out.println("Available courses: ");
            for (Course course : coursesList) {
                System.out.println(" - " + course.getTitle() + " (ID: " + course.getId() + ")");
            }
        }
        
        // Set the deadline
        if (practicalWork.getDeadline() != null) {
            deadlinePicker.setValue(practicalWork.getDeadline().toLocalDate());
        }
        
        // Show the PDF filename if available
        if (practicalWork.getPdfPath() != null && !practicalWork.getPdfPath().isEmpty()) {
            String filename = practicalWork.getPdfPath().substring(practicalWork.getPdfPath().lastIndexOf('/') + 1);
            selectedFileLabel.setText(filename + " (current file)");
        } else {
            selectedFileLabel.setText("No file selected");
        }
        
        // Show the overlay
        addPracticalWorkOverlay.setVisible(true);
    }
    
    /**
     * Updates an existing practical work with new values from the form.
     * Also handles file uploads and database updates.
     * 
     * @param practicalWorkId The ID of the practical work to update
     * @param courseId The ID of the course associated with this practical work
     */
    private void handleUpdatePracticalWork(int practicalWorkId, int courseId) {
        // Validate inputs (with special handling for PDF file which might not be changed)
        if (practicalWorkNameField.getText() == null || practicalWorkNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a practical work title.");
            return;
        }
        
        if (practicalWorkDescriptionField.getText() == null || practicalWorkDescriptionField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a practical work description.");
            return;
        }
        
        // Get the course's education level
        String targetLevel = getCourseLevel(courseId);
        
        // Process the file if a new one was selected
        if (selectedFile != null) {
            try {
                // Ensure directory exists
                String practicalWorksDir = "practical_works";
                Path dirPath = Paths.get(practicalWorksDir);
                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                }
                
                // Generate unique filename
                practicalWorkFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path targetPath = dirPath.resolve(practicalWorkFileName);
                
                // Copy the file
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "File Error", "Could not save the practical work PDF file.");
                return;
            }
        }
        
        // Get the practical work being edited
        PracticalWork practicalWorkToUpdate = PracticalWorkService.getPracticalWorkById(practicalWorkId);
        
        if (practicalWorkToUpdate == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not find the practical work to update.");
            return;
        }
        
        // Update the fields
        practicalWorkToUpdate.setTitle(practicalWorkNameField.getText().trim());
        practicalWorkToUpdate.setDescription(practicalWorkDescriptionField.getText().trim());
        practicalWorkToUpdate.setCourseId(courseId);
        practicalWorkToUpdate.setTargetLevel(targetLevel);
        
        // Set deadline if selected
        if (deadlinePicker.getValue() != null) {
            practicalWorkToUpdate.setDeadline(Date.valueOf(deadlinePicker.getValue()));
        } else {
            practicalWorkToUpdate.setDeadline(null);
        }
        
        // Update PDF path only if a new file was uploaded
        if (practicalWorkFileName != null) {
            practicalWorkToUpdate.setPdfPath("practical_works/" + practicalWorkFileName);
        }
        
        // Save to database
        boolean success = PracticalWorkService.updatePracticalWork(practicalWorkToUpdate);
        
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Practical work updated successfully!");
            
            // Reload practical works
            loadTeacherPracticalWorks();
            
            // Hide the overlay
            addPracticalWorkOverlay.setVisible(false);
            
            // Clear all fields
            clearInputFields();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update practical work.");
        }
    }
    
    /**
     * Handles deleting a practical work after confirmation.
     * Shows a confirmation dialog and removes the practical work if confirmed.
     * 
     * @param practicalWork The practical work to delete
     */
    private void handleDeletePracticalWork(PracticalWork practicalWork) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to delete the practical work: " + practicalWork.getTitle() + "?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = PracticalWorkService.deletePracticalWork(practicalWork.getId());
                
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Practical work deleted successfully!");
                    loadTeacherPracticalWorks();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete practical work.");
                }
            }
        });
    }
    
    /**
     * Handles viewing a practical work's PDF content.
     * Navigates to the PDF viewer screen with the selected practical work.
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
            controller.setPracticalWork(practicalWork);
            
            StackPane contentArea = (StackPane) practicalWorkCardsContainer.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(practicalWorkViewerParent);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load practical work viewer: " + e.getMessage());
        }
    }
    
    /**
     * Handles viewing student submissions for a practical work.
     * Navigates to the submissions view screen with the selected practical work.
     * 
     * @param practicalWork The practical work to view submissions for
     */
    private void handleViewSubmissions(PracticalWork practicalWork) {
        try {
            // Load the submissions view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TeacherPracticalWorkSubmissions.fxml"));
            Parent submissionsView = loader.load();
            
            // Set up the controller and pass the practical work
            TeacherPracticalWorkSubmissionsController controller = loader.getController();
            controller.setPracticalWork(practicalWork);
            
            // Get the main layout's content area and set the submissions view
            StackPane contentArea = (StackPane) practicalWorkCardsContainer.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(submissionsView);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load submissions view: " + e.getMessage());
        }
    }
    
    /**
     * Helper function to extract the course ID from the combobox selection.
     * Looks up the course ID by matching the course title.
     * 
     * @param courseTitle The title of the course to look up
     * @return The ID of the course, or -1 if not found
     */
    private int extractCourseId(String courseTitle) {
        // Find the course with the matching title
        for (Course course : coursesList) {
            if (course.getTitle().equals(courseTitle)) {
                return course.getId();
            }
        }
        // Log the course title and available courses if no match found
        System.out.println("Could not find course ID for title: " + courseTitle);
        System.out.println("Available courses: ");
        for (Course course : coursesList) {
            System.out.println(" - " + course.getTitle() + " (ID: " + course.getId() + ")");
        }
        return -1;
    }
    
    /**
     * Clears all input fields in the practical work form.
     * Resets the form to its initial state.
     */
    private void clearInputFields() {
        practicalWorkNameField.clear();
        practicalWorkDescriptionField.clear();
        courseComboBox.getSelectionModel().clearSelection();
        deadlinePicker.setValue(null);
        selectedFileLabel.setText("No file selected");
        selectedFile = null;
        practicalWorkFileName = null;
    }
    
    /**
     * Validates all inputs before saving a practical work.
     * Checks if required fields are filled out correctly.
     * 
     * @return true if all inputs are valid, false otherwise
     */
    private boolean validateInputs() {
        if (practicalWorkNameField.getText() == null || practicalWorkNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a practical work title.");
            return false;
        }
        
        if (practicalWorkDescriptionField.getText() == null || practicalWorkDescriptionField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a practical work description.");
            return false;
        }
        
        if (courseComboBox.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a course.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Helper method to show alerts with the specified type, title, and content.
     * 
     * @param type The type of alert (information, warning, error, etc.)
     * @param title The title of the alert dialog
     * @param content The content message of the alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Helper function to get the education level from a course ID.
     * Looks up the course by ID and returns its education level.
     * 
     * @param courseId The ID of the course to look up
     * @return The education level of the course, or "Unknown" if not found
     */
    private String getCourseLevel(int courseId) {
        for (Course course : coursesList) {
            if (course.getId() == courseId) {
                return course.getTargetLevel();
            }
        }
        return "L1"; // Default to L1 if not found
    }

    /**
     * Sets up and styles the date picker for deadline selection.
     * Configures default date values and disables past dates.
     */
    private void setupDatePicker() {
        if (deadlinePicker != null) {
            // Set default date to a week from now
            deadlinePicker.setValue(LocalDate.now().plusDays(7));
            
            // Ensure the StyleClass contains dialog-input-field
            if (!deadlinePicker.getStyleClass().contains("dialog-input-field")) {
                deadlinePicker.getStyleClass().add("dialog-input-field");
            }
            
            // Disable past dates
            deadlinePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    
                    if (empty || date == null) {
                        setDisable(true);
                    } else {
                        // Disable all dates before today
                        LocalDate today = LocalDate.now();
                        setDisable(date.isBefore(today));
                        
                        if (date.isBefore(today)) {
                            setStyle("-fx-background-color: #1a1a1a;");
                            getStyleClass().add("disabled");
                        }
                    }
                }
            });
        }
    }
} 