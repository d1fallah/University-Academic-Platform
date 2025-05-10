package app.frontend;

import app.backend.models.Course;
import app.backend.models.Exercise;
import app.backend.models.User;
import app.backend.services.CourseService;
import app.backend.services.ExerciseService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

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

public class MyExercisesController implements Initializable {

    @FXML private FlowPane exerciseCardsContainer;
    @FXML private TextField searchField;
    @FXML private Button addExerciseButton;
    
    // Dialog overlay components
    @FXML private StackPane addExerciseOverlay;
    @FXML private BorderPane dialogContainer;
    @FXML private TextField exerciseNameField;
    @FXML private TextArea exerciseDescriptionField;
    @FXML private ComboBox<String> courseComboBox;
    @FXML private Label selectedFileLabel;
    @FXML private Button selectFileButton;
    @FXML private StackPane dropArea;
    
    private User currentUser;
    private ObservableList<Exercise> exercisesList = FXCollections.observableArrayList();
    private ObservableList<Course> coursesList = FXCollections.observableArrayList();
    private File selectedFile = null;
    private String exerciseFileName = null;
    private boolean isEditMode = false;
    private int editingExerciseId = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current logged in user
        currentUser = LoginController.getCurrentUser();
        
        // Ensure user is a teacher
        if (currentUser != null && currentUser.getRole().equals("teacher")) {
            // Load the teacher's exercises
            loadTeacherExercises();
            
            // Setup search functionality
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterExercises(newValue);
            });
            
            // Setup dialog components if they're available
            if (courseComboBox != null) {
                // Load courses for this teacher
                loadTeacherCourses();
                
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
        // Set up drag over event
        dropArea.setOnDragOver(event -> {
            if (event.getGestureSource() != dropArea && event.getDragboard().hasFiles()) {
                // Check if the dragged file is a PDF
                if (event.getDragboard().getFiles().get(0).getName().toLowerCase().endsWith(".pdf")) {
                    event.acceptTransferModes(TransferMode.COPY);
                }
            }
            event.consume();
        });
        
        // Set up drag entered/exited events for visual feedback
        dropArea.setOnDragEntered(event -> {
            if (event.getGestureSource() != dropArea && event.getDragboard().hasFiles()) {
                dropArea.getStyleClass().add("drag-over");
            }
            event.consume();
        });
        
        dropArea.setOnDragExited(event -> {
            dropArea.getStyleClass().remove("drag-over");
            event.consume();
        });
        
        // Set up drop event
        dropArea.setOnDragDropped((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            
            if (db.hasFiles() && db.getFiles().get(0).getName().toLowerCase().endsWith(".pdf")) {
                selectedFile = db.getFiles().get(0);
                selectedFileLabel.setText(selectedFile.getName());
                success = true;
            }
            
            event.setDropCompleted(success);
            event.consume();
        });
    }
    
    /**
     * Loads exercises created by the current teacher
     */
    private void loadTeacherExercises() {
        // Clear the container
        exerciseCardsContainer.getChildren().clear();
        
        // Get all exercises from the service for this teacher
        List<Exercise> teacherExercises = ExerciseService.getExercisesByTeacherId(currentUser.getId());
        exercisesList.setAll(teacherExercises);
        
        // If no exercises, show a message
        if (exercisesList.isEmpty()) {
            Label noExercisesLabel = new Label("You haven't created any exercises yet. Click the 'Add new exercise +' button to get started!");
            noExercisesLabel.getStyleClass().add("no-courses-message");
            noExercisesLabel.setPadding(new Insets(50, 0, 0, 0));
            exerciseCardsContainer.getChildren().add(noExercisesLabel);
        } else {
            // Create and add a card for each exercise
            for (Exercise exercise : exercisesList) {
                exerciseCardsContainer.getChildren().add(createExerciseCard(exercise));
            }
        }
    }

    /**
     * Creates a visual card representation for an exercise with edit/delete options
     */
    private StackPane createExerciseCard(Exercise exercise) {
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
            System.out.println("Failed to load background image for exercise card");
        }

        // Card layout container
        VBox cardContent = new VBox();
        cardContent.getStyleClass().add("card-content");
        cardContent.setSpacing(20);
        cardContent.setPadding(new Insets(20, 20, 20, 20));
        cardContent.setPrefWidth(480);
        cardContent.setPrefHeight(230);

        // Top section with exercise title and logo
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
        Label titleLabel = new Label(exercise.getTitle());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);
        titleLabel.setMinHeight(Region.USE_PREF_SIZE);

        titleContainer.getChildren().add(titleLabel);

        // Exercise logo/icon
        StackPane logoContainer = new StackPane();
        logoContainer.setMinWidth(50);
        logoContainer.setMaxWidth(50);
        logoContainer.setPrefHeight(50);
        logoContainer.setAlignment(Pos.TOP_CENTER);
        logoContainer.getStyleClass().add("logo-container");

        ImageView exerciseIcon = new ImageView();
        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/Ruler Cross Pen.png"));
            exerciseIcon.setImage(logo);
        } catch (Exception e) {
            System.out.println("Failed to load logo for exercise: " + exercise.getTitle());
        }
        exerciseIcon.setFitWidth(50);
        exerciseIcon.setFitHeight(50);
        exerciseIcon.getStyleClass().add("exercise-icon");

        logoContainer.getChildren().add(exerciseIcon);

        // Add title and logo to header box
        headerBox.getChildren().addAll(titleContainer, logoContainer);

        // Exercise description
        String description = exercise.getDescription();
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
        Course course = CourseService.getCourseById(exercise.getCourseId());
        String courseName = course != null ? course.getTitle() : "Unknown Course";
        
        // Course info label
        HBox courseBox = new HBox();
        courseBox.setAlignment(Pos.CENTER_LEFT);
        courseBox.setSpacing(5);
        
        Label courseLabel = new Label("Course: " + courseName);
        courseLabel.getStyleClass().add("card-instructor");
        courseLabel.setStyle("-fx-text-fill: white;");
        
        courseBox.getChildren().addAll(courseLabel);

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

        String createdAt = "Created: " + (exercise.getCreatedAt() != null ? 
                        exercise.getCreatedAt().toString().substring(0, 10) : "Unknown");
        Label dateLabel = new Label(createdAt);
        dateLabel.getStyleClass().add("date-label");

        dateBox.getChildren().addAll(calendarIcon, dateLabel);

        // Buttons section
        HBox buttonBox = new HBox(8); // Increased spacing between buttons
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // View button
        Button viewButton = new Button();
        viewButton.getStyleClass().add("icon-button");
        viewButton.setPrefWidth(24);
        viewButton.setPrefHeight(24);
        viewButton.setOnAction(e -> handleViewExercise(exercise));
        
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

        // Edit button
        Button editButton = new Button();
        editButton.getStyleClass().add("icon-button");
        editButton.setPrefWidth(24);
        editButton.setPrefHeight(24);
        editButton.setOnAction(e -> handleEditExercise(exercise));
        
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
        deleteButton.setOnAction(e -> handleDeleteExercise(exercise.getId()));
        
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
        cardContent.getChildren().addAll(headerBox, descriptionLabel, courseBox, spacer, footerBox);

        // Stack the background and content
        cardPane.getChildren().addAll(cardBackground, cardContent);

        // Add accessibility features
        cardPane.setAccessibleText("Exercise: " + exercise.getTitle() + ", " + description);

        return cardPane;
    }
    
    /**
     * Filters exercises based on search query
     */
    private void filterExercises(String searchText) {
        // If search text is empty, show all exercises
        if (searchText == null || searchText.trim().isEmpty()) {
            exerciseCardsContainer.getChildren().clear();
            for (Exercise exercise : exercisesList) {
                exerciseCardsContainer.getChildren().add(createExerciseCard(exercise));
            }
            return;
        }
        
        // Filter courses based on title or description containing the search text (case insensitive)
        List<Exercise> filteredExercises = exercisesList.stream()
            .filter(exercise -> 
                exercise.getTitle().toLowerCase().contains(searchText.toLowerCase()) ||
                exercise.getDescription().toLowerCase().contains(searchText.toLowerCase()))
            .collect(Collectors.toList());
        
        // Update UI with filtered results
        exerciseCardsContainer.getChildren().clear();
        
        if (filteredExercises.isEmpty()) {
            Label noResultsLabel = new Label("No exercises found matching your search.");
            noResultsLabel.getStyleClass().add("no-courses-message");
            noResultsLabel.setPadding(new Insets(50, 0, 0, 0));
            exerciseCardsContainer.getChildren().add(noResultsLabel);
        } else {
            for (Exercise exercise : filteredExercises) {
                exerciseCardsContainer.getChildren().add(createExerciseCard(exercise));
            }
        }
    }

    /**
     * Shows the add exercise dialog
     */
    @FXML
    private void handleAddNewExercise(ActionEvent event) {
        // Reset the dialog fields
        exerciseNameField.clear();
        exerciseDescriptionField.clear();
        selectedFileLabel.setText("No file selected");
        selectedFile = null;
        exerciseFileName = null;
        
        // Make sure we're not in edit mode
        isEditMode = false;
        editingExerciseId = -1;
        
        // Ensure the dialog title shows we're adding a new exercise
        Label dialogTitle = (Label) dialogContainer.lookup(".dialog-title");
        if (dialogTitle != null) {
            dialogTitle.setText("Add new exercise");
        }
        
        // Show the overlay
        addExerciseOverlay.setVisible(true);
    }
    
    /**
     * Loads the courses for the current teacher to populate the dropdown
     */
    private void loadTeacherCourses() {
        List<Course> courses = CourseService.getCoursesByTeacherId(currentUser.getId());
        coursesList.setAll(courses);
        
        // Create a list of course names for the dropdown
        ObservableList<String> courseNames = FXCollections.observableArrayList();
        for (Course course : courses) {
            // Store course name only as visible text, but keep ID as a hidden property
            courseNames.add(course.getTitle());
        }
        
        courseComboBox.setItems(courseNames);
    }
    
    /**
     * Handles file selection from the button
     */
    @FXML
    private void handleSelectFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Exercise PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        
        // Show open file dialog
        File file = fileChooser.showOpenDialog(null);
        
        if (file != null) {
            selectedFile = file;
            selectedFileLabel.setText(file.getName());
        }
    }
    
    /**
     * Handles the search button action
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        filterExercises(searchField.getText());
    }
    
    /**
     * Handles the cancel button action in the dialog
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        // Hide the overlay
        addExerciseOverlay.setVisible(false);
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
        
        // Get the selected course ID from the combo box
        String selectedCourse = courseComboBox.getSelectionModel().getSelectedItem();
        int courseId = extractCourseId(selectedCourse);
        
        // Copy the selected PDF to the exercises directory
        if (selectedFile != null) {
            try {
                // Ensure directory exists
                String exercisesDir = "exercises";
                Path dirPath = Paths.get(exercisesDir);
                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                }
                
                // Generate unique filename
                exerciseFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path targetPath = dirPath.resolve(exerciseFileName);
                
                // Copy the file
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "File Error", "Could not save the exercise PDF file.");
                return;
            }
        }
        
        if (isEditMode) {
            handleUpdateExercise(editingExerciseId, courseId);
        } else {
            // Create exercise object
            Exercise newExercise = new Exercise(
                courseId,
                exerciseNameField.getText(),
                exerciseDescriptionField.getText(),
                "" // No comment initially
            );
            
            // Set teacher id to fix "Field 'id' doesn't have a default value" error
            newExercise.setTeacherId(currentUser.getId());
            
            // Set target level to match the course level if available
            for (Course course : coursesList) {
                if (course.getId() == courseId && course.getTargetLevel() != null) {
                    newExercise.setTargetLevel(course.getTargetLevel());
                    break;
                }
            }
            
            // If we have a filename, set it
            if (exerciseFileName != null) {
                newExercise.setPdfPath(exerciseFileName);
            }
            
            // Save to database
            boolean success = ExerciseService.addExercise(newExercise);
            
            if (success) {
                // Hide dialog
                addExerciseOverlay.setVisible(false);
                
                // Reset the dialog title
                Label dialogTitle = (Label) dialogContainer.lookup(".dialog-title");
                if (dialogTitle != null) {
                    dialogTitle.setText("Add new exercise");
                }
                
                // Refresh the exercises list
                loadTeacherExercises();
                
                // Show success message
                showAlert(Alert.AlertType.INFORMATION, "Success", "Exercise added successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add the exercise.");
            }
        }
    }
    
    private boolean validateInputs() {
        // Check exercise name
        if (exerciseNameField.getText() == null || exerciseNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter an exercise name.");
            return false;
        }
        
        // Check description
        if (exerciseDescriptionField.getText() == null || exerciseDescriptionField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter an exercise description.");
            return false;
        }
        
        // Check course selection
        if (courseComboBox.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a course for this exercise.");
            return false;
        }
        
        // Check file - only required for new exercises, not for edits
        if (!isEditMode && selectedFile == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a PDF file for the exercise.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Handles editing an exercise
     */
    private void handleEditExercise(Exercise exercise) {
        // Set edit mode
        isEditMode = true;
        editingExerciseId = exercise.getId();
        
        // Update dialog title to show we're editing
        Label dialogTitle = (Label) dialogContainer.lookup(".dialog-title");
        if (dialogTitle != null) {
            dialogTitle.setText("Edit exercise");
        }
        
        // Load the exercise data into the dialog
        exerciseNameField.setText(exercise.getTitle());
        exerciseDescriptionField.setText(exercise.getDescription());
        
        // Select the correct course in the dropdown
        for (int i = 0; i < coursesList.size(); i++) {
            if (coursesList.get(i).getId() == exercise.getCourseId()) {
                courseComboBox.getSelectionModel().select(i);
                break;
            }
        }
        
        // If there's a PDF, show its filename
        if (exercise.getPdfPath() != null && !exercise.getPdfPath().isEmpty()) {
            selectedFileLabel.setText("Current file: " + exercise.getPdfPath());
        } else {
            selectedFileLabel.setText("No file currently attached");
        }
        
        // We don't reset the selectedFile here because we might not be changing it
        
        // Show the dialog
        addExerciseOverlay.setVisible(true);
    }
    
    /**
     * Handles updating an existing exercise
     */
    private void handleUpdateExercise(int exerciseId, int courseId) {
        // Validate inputs (with special handling for PDF file which might not be changed)
        if (exerciseNameField.getText() == null || exerciseNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter an exercise name.");
            return;
        }
        
        if (exerciseDescriptionField.getText() == null || exerciseDescriptionField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter an exercise description.");
            return;
        }
        
        // Process the file if a new one was selected
        if (selectedFile != null) {
            try {
                // Ensure directory exists
                String exercisesDir = "exercises";
                Path dirPath = Paths.get(exercisesDir);
                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                }
                
                // Generate unique filename
                exerciseFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path targetPath = dirPath.resolve(exerciseFileName);
                
                // Copy the file
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "File Error", "Could not save the exercise PDF file.");
                return;
            }
        }
        
        // Get the exercise being edited
        Exercise exerciseToUpdate = ExerciseService.getExerciseById(exerciseId);
        
        if (exerciseToUpdate != null) {
            // Update the fields
            exerciseToUpdate.setTitle(exerciseNameField.getText());
            exerciseToUpdate.setDescription(exerciseDescriptionField.getText());
            exerciseToUpdate.setCourseId(courseId);
            
            // Only update the PDF path if a new file was selected
            if (exerciseFileName != null) {
                exerciseToUpdate.setPdfPath(exerciseFileName);
            }
            
            // Save the changes
            boolean success = ExerciseService.updateExercise(exerciseToUpdate);
            
            if (success) {
                // Hide dialog
                addExerciseOverlay.setVisible(false);
                
                // Reset edit mode
                isEditMode = false;
                editingExerciseId = -1;
                
                // Reset the dialog title
                Label dialogTitle = (Label) dialogContainer.lookup(".dialog-title");
                if (dialogTitle != null) {
                    dialogTitle.setText("Add new exercise");
                }
                
                // Refresh the exercises list
                loadTeacherExercises();
                
                // Show success message
                showAlert(Alert.AlertType.INFORMATION, "Success", "Exercise updated successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update the exercise.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not find the exercise to update.");
        }
    }
    
    /**
     * Handles deleting an exercise
     */
    private void handleDeleteExercise(int exerciseId) {
        // Ask for confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Exercise");
        alert.setContentText("Are you sure you want to delete this exercise? This action cannot be undone.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Delete the exercise
                boolean success = ExerciseService.deleteExercise(exerciseId);
                
                if (success) {
                    // Refresh the exercises list
                    loadTeacherExercises();
                    
                    // Show success message
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Exercise deleted successfully!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete the exercise.");
                }
            }
        });
    }
    
    /**
     * Handles viewing an exercise - opens the PDF in a separate window
     */
    private void handleViewExercise(Exercise exercise) {
        try {
            // Check if the exercise has a PDF file
            if (exercise.getPdfPath() == null || exercise.getPdfPath().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "No PDF Available", 
                    "This exercise does not have a PDF file attached.");
                return;
            }
            
            // Load the exercise viewer view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercise-viewer.fxml"));
            Parent exerciseViewerParent = loader.load();
            
            // Set up the controller and pass the exercise
            ExerciseViewerController controller = loader.getController();
            
            // Make sure we set the teacher ID to the current user's ID so the return navigation works properly
            User currentUser = LoginController.getCurrentUser();
            if (currentUser != null) {
                controller.setExercise(exercise, currentUser.getId());
            } else {
                controller.setExercise(exercise);
            }
            
            // Get the main layout's content area and set the exercise viewer
            StackPane contentArea = (StackPane) exerciseCardsContainer.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(exerciseViewerParent);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load exercise viewer: " + e.getMessage());
        }
    }
    
    /**
     * Helper function to extract the course ID from the combobox selection
     */
    private int extractCourseId(String courseTitle) {
        // Find the course with the matching title
        for (Course course : coursesList) {
            if (course.getTitle().equals(courseTitle)) {
                return course.getId();
            }
        }
        return -1;
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