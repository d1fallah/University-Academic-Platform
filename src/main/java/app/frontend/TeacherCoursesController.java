package app.frontend;

import app.backend.models.Course;
import app.backend.models.User;
import app.backend.services.CourseService;
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
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TeacherCoursesController implements Initializable {

    @FXML private FlowPane courseCardsContainer;
    @FXML private TextField searchField;
    @FXML private Button addCourseButton;
    
    // Dialog overlay components
    @FXML private StackPane addCourseOverlay;
    @FXML private BorderPane dialogContainer;
    @FXML private TextField courseNameField;
    @FXML private TextArea courseDescriptionField;
    @FXML private ComboBox<String> levelComboBox;
    @FXML private Label selectedFileLabel;
    @FXML private Button selectFileButton;
    @FXML private StackPane dropArea;
    
    private User currentUser;
    private ObservableList<Course> coursesList = FXCollections.observableArrayList();
    private File selectedFile = null;
    private String courseFileName = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current logged in user
        currentUser = AuthLoginController.getCurrentUser();
        
        // Ensure user is a teacher
        if (currentUser != null && currentUser.getRole().equals("teacher")) {
            // Load the teacher's courses
            loadTeacherCourses();
            
            // Setup search functionality
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterCourses(newValue);
            });
            
            // Setup dialog components if they're available
            if (levelComboBox != null) {
                // Initialize education level combo box
                levelComboBox.setItems(FXCollections.observableArrayList("L1", "L2", "L3", "M1", "M2"));
                
                // Set L1 as the default selection
                levelComboBox.getSelectionModel().select("L1");
                
                // Set up drag and drop for PDF area
                setupDragAndDrop();
            }
        } else {
            // If not a teacher, show message or redirect
            showAlert(Alert.AlertType.WARNING, "Access Denied", "Only teachers can access this page.");
        }
    }
    
    /**
     * Sets up drag and drop functionality for the PDF upload area
     */
    private void setupDragAndDrop() {
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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Course PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        
        File file = fileChooser.showOpenDialog(addCourseOverlay.getScene().getWindow());
        if (file != null) {
            handleFileSelected(file);
        }
    }
    
    private void handleFileSelected(File file) {
        selectedFile = file;
        selectedFileLabel.setText(file.getName());
        selectedFileLabel.setStyle("-fx-text-fill: white;");
    }
    
    /**
     * Loads all courses created by the current teacher and displays them as cards
     */
    private void loadTeacherCourses() {
        // Clear the container
        courseCardsContainer.getChildren().clear();
        
        // Get all courses from the service for this teacher
        List<Course> teacherCourses = CourseService.getCoursesByTeacherId(currentUser.getId());
        coursesList.setAll(teacherCourses);
        
        // If no courses, show a message
        if (coursesList.isEmpty()) {
            Label noCoursesLabel = new Label("You haven't created any courses yet. Click the 'Add new course +' button to get started!");
            noCoursesLabel.getStyleClass().add("no-courses-message");
            noCoursesLabel.setPadding(new Insets(50, 0, 0, 0));
            courseCardsContainer.getChildren().add(noCoursesLabel);
        } else {
            // Create and add a card for each course
            for (Course course : coursesList) {
                courseCardsContainer.getChildren().add(createCourseCard(course));
            }
        }
    }

    /**
     * Creates a visual card representation for a course with edit/delete options
     */
    private StackPane createCourseCard(Course course) {
        // Main card container
        StackPane cardPane = new StackPane();
        cardPane.getStyleClass().add("course-card");
        cardPane.setPrefWidth(480);
        cardPane.setPrefHeight(230);

        // Add background image to card
        ImageView cardBackground = new ImageView();
        try {
            Image bgImage = new Image(getClass().getResourceAsStream("/images/courseCardBackground.png"));
            cardBackground.setImage(bgImage);
            cardBackground.setFitWidth(480);
            cardBackground.setFitHeight(270);
            cardBackground.setPreserveRatio(false);
            cardBackground.setOpacity(0.7);
        } catch (Exception e) {
            System.out.println("Failed to load background image for course card");
        }

        // Card layout container
        VBox cardContent = new VBox();
        cardContent.getStyleClass().add("card-content");
        cardContent.setSpacing(20);
        cardContent.setPadding(new Insets(20, 20, 20, 20));
        cardContent.setPrefWidth(480);
        cardContent.setPrefHeight(230);

        // Top section with course title and logo
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

        // Title on left side
        Label titleLabel = new Label(course.getTitle());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);
        titleLabel.setMinHeight(Region.USE_PREF_SIZE);

        titleContainer.getChildren().add(titleLabel);

        // Course logo/icon
        StackPane logoContainer = new StackPane();
        logoContainer.setMinWidth(50);
        logoContainer.setMaxWidth(50);
        logoContainer.setPrefHeight(50);
        logoContainer.setAlignment(Pos.TOP_CENTER);
        logoContainer.getStyleClass().add("logo-container");

        ImageView courseIcon = new ImageView();
        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/courseCardLogo.png"));
            courseIcon.setImage(logo);
        } catch (Exception e) {
            System.out.println("Failed to load logo for course: " + course.getTitle());
        }
        courseIcon.setFitWidth(50);
        courseIcon.setFitHeight(50);
        courseIcon.getStyleClass().add("course-icon");

        logoContainer.getChildren().add(courseIcon);

        // Add title and logo to header box
        headerBox.getChildren().addAll(titleContainer, logoContainer);

        // Course description
        String description = course.getDescription();
        if (description == null || description.isEmpty()) {
            description = "No description available";
        }
        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("card-description");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMinHeight(Region.USE_PREF_SIZE);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Footer section with creation date and buttons
        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.BOTTOM_LEFT);
        footerBox.setPrefWidth(480);

        // Creation date info
        HBox dateBox = new HBox();
        dateBox.getStyleClass().add("card-date");
        dateBox.setAlignment(Pos.CENTER_LEFT);
        dateBox.setPrefHeight(30);
        dateBox.setSpacing(10);
        HBox.setHgrow(dateBox, Priority.ALWAYS);

        // Date icon
        ImageView calendarIcon = new ImageView();
        try {
            Image calendarImg = new Image(getClass().getResourceAsStream("/images/Case.png"));
            calendarIcon.setImage(calendarImg);
            calendarIcon.setFitHeight(20);
            calendarIcon.setFitWidth(20);
            calendarIcon.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Failed to load calendar icon");
        }

        String createdAt = "Created: " + (course.getCreatedAt() != null ? 
                           course.getCreatedAt().toString().substring(0, 10) : "Unknown");
        Label dateLabel = new Label(createdAt);
        dateLabel.getStyleClass().add("date-label");

        dateBox.getChildren().addAll(calendarIcon, dateLabel);

        // Buttons section
        HBox buttonBox = new HBox(8); // Increased spacing between buttons
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // View button with eye icon
        Button viewButton = new Button();
        viewButton.getStyleClass().add("icon-button");
        viewButton.setPrefWidth(24);
        viewButton.setPrefHeight(24);
        viewButton.setOnAction(e -> handleViewCourse(course));
        
        // View icon
        ImageView viewIcon = new ImageView();
        try {
            Image eyeImage = new Image(getClass().getResourceAsStream("/images/Eye.png"));
            viewIcon.setImage(eyeImage);
            viewIcon.setFitWidth(15);
            viewIcon.setFitHeight(15);
            viewIcon.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Failed to load eye icon");
        }
        viewButton.setGraphic(viewIcon);

        // Edit button - now with icon
        Button editButton = new Button();
        editButton.getStyleClass().add("icon-button"); // Will add this class to CSS
        editButton.setPrefWidth(24);
        editButton.setPrefHeight(24);
        editButton.setOnAction(e -> handleEditCourse(course));
        
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

        // Delete button - now with icon
        Button deleteButton = new Button();
        deleteButton.getStyleClass().add("icon-button"); // Will add this class to CSS
        deleteButton.setPrefWidth(24);
        deleteButton.setPrefHeight(24);
        deleteButton.setOnAction(e -> handleDeleteCourse(course));
        
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

        buttonBox.getChildren().addAll(viewButton, editButton, deleteButton);

        // Add date and buttons to footer
        footerBox.getChildren().addAll(dateBox, buttonBox);

        // Add all sections to the card
        cardContent.getChildren().addAll(headerBox, descriptionLabel, spacer, footerBox);

        // Stack the background and content
        cardPane.getChildren().addAll(cardBackground, cardContent);

        // Add accessibility features
        cardPane.setAccessibleText("Course: " + course.getTitle() + ", " + description);

        return cardPane;
    }
    
    /**
     * Filters courses based on search query
     */
    private void filterCourses(String query) {
        if (query == null || query.isEmpty()) {
            // If query is empty, show all courses
            loadTeacherCourses();
        } else {
            // Filter courses based on query
            query = query.toLowerCase();
            final String searchQuery = query;
            
            List<Course> filteredList = coursesList.stream()
                .filter(course -> 
                    course.getTitle().toLowerCase().contains(searchQuery) ||
                    course.getDescription().toLowerCase().contains(searchQuery))
                .collect(Collectors.toList());
            
            courseCardsContainer.getChildren().clear();
            
            if (filteredList.isEmpty()) {
                Label noResultsLabel = new Label("No courses match your search criteria.");
                noResultsLabel.getStyleClass().add("no-courses-message");
                noResultsLabel.setPadding(new Insets(50, 0, 0, 0));
                courseCardsContainer.getChildren().add(noResultsLabel);
            } else {
                for (Course course : filteredList) {
                    courseCardsContainer.getChildren().add(createCourseCard(course));
                }
            }
        }
    }
    
    /**
     * Handles the action when the Add New Course button is clicked
     */
    @FXML
    private void handleAddNewCourse(ActionEvent event) {
        // Clear form fields
        courseNameField.clear();
        courseDescriptionField.clear();
        levelComboBox.getSelectionModel().clearSelection();
        levelComboBox.getSelectionModel().select("L1");
        selectedFile = null;
        courseFileName = null;
        selectedFileLabel.setText("No file selected");
        selectedFileLabel.setStyle("-fx-text-fill: #888888;");
        
        // Show the overlay
        addCourseOverlay.setVisible(true);
    }
    
    /**
     * Handles the cancel button action in the dialog
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        // Hide the overlay
        addCourseOverlay.setVisible(false);
    }
    
    /**
     * Handles the save button action in the dialog
     */
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
            // Hide dialog
            addCourseOverlay.setVisible(false);
            
            // Refresh the courses list
            loadTeacherCourses();
            
            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Success", "Course added successfully!");
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
    
    /**
     * Handles editing a course
     */
    private void handleEditCourse(Course course) {
        // Set the dialog title to reflect editing mode
        Label dialogTitle = (Label) dialogContainer.lookup(".dialog-title");
        if (dialogTitle != null) {
            dialogTitle.setText("Edit Course");
        }
        
        // Pre-populate form fields with course data
        courseNameField.setText(course.getTitle());
        courseDescriptionField.setText(course.getDescription());
        
        // Set the education level
        if (course.getTargetLevel() != null && !course.getTargetLevel().isEmpty()) {
            levelComboBox.getSelectionModel().select(course.getTargetLevel());
        } else {
            levelComboBox.getSelectionModel().select("L1");
        }
        
        // Display PDF filename if it exists
        if (course.getPdfPath() != null && !course.getPdfPath().isEmpty()) {
            selectedFileLabel.setText(course.getPdfPath());
            selectedFileLabel.setStyle("-fx-text-fill: white;");
            courseFileName = course.getPdfPath();
        } else {
            selectedFileLabel.setText("No file selected");
            selectedFileLabel.setStyle("-fx-text-fill: #888888;");
            courseFileName = null;
        }
        selectedFile = null; // Reset selected file since we're editing
        
        // Change the button handler for the save button to update instead of create
        Button saveButton = (Button) dialogContainer.lookup(".save-button");
        if (saveButton != null) {
            saveButton.setOnAction(e -> handleUpdateCourse(course.getId()));
        }
        
        // Show the overlay
        addCourseOverlay.setVisible(true);
    }
    
    /**
     * Handles deleting a course
     */
    private void handleDeleteCourse(Course course) {
        // Confirm deletion
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete the course: " + course.getTitle() + "?");
        
        // Process the result
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = CourseService.deleteCourse(course.getId());
                
                if (success) {
                    loadTeacherCourses(); // Reload courses after deletion
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Course deleted successfully");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete course");
                }
            }
        });
    }
    
    /**
     * Handles the search action
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField.getText().trim();
        filterCourses(query);
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
     * Handles updating an existing course
     */
    private void handleUpdateCourse(int courseId) {
        // Validate inputs (with special handling for PDF file which might not be changed)
        if (courseNameField.getText() == null || courseNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a course name.");
            return;
        }
        
        if (courseDescriptionField.getText() == null || courseDescriptionField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a course description.");
            return;
        }
        
        if (levelComboBox.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select an education level.");
            return;
        }
        
        // Process the file if a new one was selected
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
        
        // Create updated course object
        Course updatedCourse = new Course();
        updatedCourse.setId(courseId);
        updatedCourse.setTitle(courseNameField.getText());
        updatedCourse.setDescription(courseDescriptionField.getText());
        updatedCourse.setComment(""); // Not used in this version
        updatedCourse.setTeacherId(currentUser.getId());
        
        // If we have a filename, set it
        if (courseFileName != null) {
            updatedCourse.setPdfPath(courseFileName);
        }
        
        // Set the education level
        String educationLevel = levelComboBox.getSelectionModel().getSelectedItem();
        updatedCourse.setTargetLevel(educationLevel);
        
        // Update in database
        boolean success = CourseService.updateCourse(updatedCourse);
        
        if (success) {
            // Hide dialog
            addCourseOverlay.setVisible(false);
            
            // Reset save button to add mode for future uses
            Button saveButton = (Button) dialogContainer.lookup(".save-button");
            if (saveButton != null) {
                saveButton.setOnAction(this::handleSave);
            }
            
            // Reset dialog title
            Label dialogTitle = (Label) dialogContainer.lookup(".dialog-title");
            if (dialogTitle != null) {
                dialogTitle.setText("Add new Course");
            }
            
            // Refresh the courses list
            loadTeacherCourses();
            
            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Success", "Course updated successfully!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update the course.");
        }
    }

    /**
     * Handles viewing a course
     */
    private void handleViewCourse(Course course) {
        try {
            // Check if the course has a PDF file
            if (course.getPdfPath() == null || course.getPdfPath().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "No PDF Available", 
                    "This course does not have a PDF file attached.");
                return;
            }
            
            // Load the course viewer view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CourseViewer.fxml"));
            Parent courseViewerParent = loader.load();
            
            // Set up the controller and pass the course
            ViewCourseController controller = loader.getController();
            
            // Make sure we set the teacher ID to the current user's ID so the return navigation works properly
            if (currentUser != null) {
                controller.setCourse(course, currentUser.getId());
            } else {
                controller.setCourse(course);
            }
            
            // Get the main layout's content area and set the course viewer
            StackPane contentArea = (StackPane) courseCardsContainer.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(courseViewerParent);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load course viewer: " + e.getMessage());
        }
    }
}