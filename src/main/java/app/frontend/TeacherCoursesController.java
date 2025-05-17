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

/**
 * Controller for the Teacher Courses interface that manages course creation,
 * editing, viewing, and deletion for teachers in the application.
 *
 * @author Sellami Mohamed Odai
 */
public class TeacherCoursesController implements Initializable {

    @FXML private FlowPane courseCardsContainer;
    @FXML private TextField searchField;
    @FXML private Button addCourseButton;
    
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

    /**
     * Initializes the controller class and sets up the UI components.
     * Loads teacher's courses, configures search functionality and dialog components.
     *
     * @param location The location used to resolve relative paths for the root object
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = AuthLoginController.getCurrentUser();
        
        if (currentUser != null && currentUser.getRole().equals("teacher")) {
            loadTeacherCourses();
            
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterCourses(newValue);
            });
            
            if (levelComboBox != null) {
                levelComboBox.setItems(FXCollections.observableArrayList("L1", "L2", "L3", "M1", "M2"));
                levelComboBox.getSelectionModel().select("L1");
                setupDragAndDrop();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Access Denied", "Only teachers can access this page.");
        }
    }
    
    /**
     * Sets up drag and drop functionality for the PDF upload area.
     */
    private void setupDragAndDrop() {
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
     * Checks if the file is a PDF file.
     *
     * @param file The file to check
     * @return True if the file is valid PDF, false otherwise
     */
    private boolean isAcceptableFile(File file) {
        return file != null && file.exists() && file.getName().toLowerCase().endsWith(".pdf");
    }
    
    /**
     * Handles the file selection button action.
     *
     * @param event The action event
     */
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
    
    /**
     * Updates UI when a file is selected.
     *
     * @param file The selected file
     */
    private void handleFileSelected(File file) {
        selectedFile = file;
        selectedFileLabel.setText(file.getName());
        selectedFileLabel.setStyle("-fx-text-fill: white;");
    }
    
    /**
     * Loads all courses created by the current teacher and displays them as cards.
     */
    private void loadTeacherCourses() {
        courseCardsContainer.getChildren().clear();
        
        List<Course> teacherCourses = CourseService.getCoursesByTeacherId(currentUser.getId());
        coursesList.setAll(teacherCourses);
        
        if (coursesList.isEmpty()) {
            Label noCoursesLabel = new Label("You haven't created any courses yet. Click the 'Add new course +' button to get started!");
            noCoursesLabel.getStyleClass().add("no-courses-message");
            noCoursesLabel.setPadding(new Insets(50, 0, 0, 0));
            courseCardsContainer.getChildren().add(noCoursesLabel);
        } else {
            for (Course course : coursesList) {
                courseCardsContainer.getChildren().add(createCourseCard(course));
            }
        }
    }

    /**
     * Creates a visual card representation for a course with edit/delete options.
     *
     * @param course The course to create a card for
     * @return A styled StackPane containing the course information and action buttons
     */
    private StackPane createCourseCard(Course course) {
        StackPane cardPane = new StackPane();
        cardPane.getStyleClass().add("course-card");
        cardPane.setPrefWidth(480);
        cardPane.setPrefHeight(230);

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

        VBox cardContent = new VBox();
        cardContent.getStyleClass().add("card-content");
        cardContent.setSpacing(20);
        cardContent.setPadding(new Insets(20, 20, 20, 20));
        cardContent.setPrefWidth(480);
        cardContent.setPrefHeight(230);

        HBox headerBox = createCardHeader(course);
        
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

        HBox footerBox = createCardFooter(course);

        cardContent.getChildren().addAll(headerBox, descriptionLabel, spacer, footerBox);
        cardPane.getChildren().addAll(cardBackground, cardContent);
        cardPane.setAccessibleText("Course: " + course.getTitle() + ", " + description);

        return cardPane;
    }
    
    /**
     * Creates the header section of a course card.
     * 
     * @param course The course to create the header for
     * @return HBox containing the course title and logo
     */
    private HBox createCardHeader(Course course) {
        HBox headerBox = new HBox();
        headerBox.getStyleClass().add("card-header");
        headerBox.setAlignment(Pos.TOP_LEFT);
        headerBox.setPrefWidth(480);
        headerBox.setSpacing(20);

        VBox titleContainer = new VBox();
        titleContainer.setAlignment(Pos.TOP_LEFT);
        titleContainer.setPrefWidth(390);
        HBox.setHgrow(titleContainer, Priority.ALWAYS);

        Label titleLabel = new Label(course.getTitle());
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
        headerBox.getChildren().addAll(titleContainer, logoContainer);
        
        return headerBox;
    }
    
    /**
     * Creates the footer section of a course card with date and action buttons.
     * 
     * @param course The course to create the footer for
     * @return HBox containing the creation date and action buttons
     */
    private HBox createCardFooter(Course course) {
        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.BOTTOM_LEFT);
        footerBox.setPrefWidth(480);

        HBox dateBox = new HBox();
        dateBox.getStyleClass().add("card-date");
        dateBox.setAlignment(Pos.CENTER_LEFT);
        dateBox.setPrefHeight(30);
        dateBox.setSpacing(10);
        HBox.setHgrow(dateBox, Priority.ALWAYS);

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

        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button viewButton = createIconButton("/images/Eye.png", e -> handleViewCourse(course));
        Button editButton = createIconButton("/images/Pen.png", e -> handleEditCourse(course));
        Button deleteButton = createIconButton("/images/Trash.png", e -> handleDeleteCourse(course));

        buttonBox.getChildren().addAll(viewButton, editButton, deleteButton);
        footerBox.getChildren().addAll(dateBox, buttonBox);
        
        return footerBox;
    }
    
    /**
     * Creates a button with an icon.
     * 
     * @param iconPath Path to the icon resource
     * @param action The action to be executed when the button is clicked
     * @return Styled button with icon
     */
    private Button createIconButton(String iconPath, javafx.event.EventHandler<ActionEvent> action) {
        Button button = new Button();
        button.getStyleClass().add("icon-button");
        button.setPrefWidth(24);
        button.setPrefHeight(24);
        button.setOnAction(action);
        
        ImageView icon = new ImageView();
        try {
            Image image = new Image(getClass().getResourceAsStream(iconPath));
            icon.setImage(image);
            icon.setFitWidth(15);
            icon.setFitHeight(15);
            icon.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Failed to load icon: " + iconPath);
        }
        button.setGraphic(icon);
        
        return button;
    }
    
    /**
     * Filters courses based on search query.
     *
     * @param query The search query string
     */
    private void filterCourses(String query) {
        if (query == null || query.isEmpty()) {
            loadTeacherCourses();
        } else {
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
     * Handles the action when the Add New Course button is clicked.
     *
     * @param event The action event
     */
    @FXML
    private void handleAddNewCourse(ActionEvent event) {
        Label dialogTitle = (Label) dialogContainer.lookup(".dialog-title");
        if (dialogTitle != null) {
            dialogTitle.setText("Add new Course");
        }
        courseNameField.clear();
        courseDescriptionField.clear();
        levelComboBox.getSelectionModel().clearSelection();
        levelComboBox.getSelectionModel().select("L1");
        selectedFile = null;
        courseFileName = null;
        selectedFileLabel.setText("No file selected");
        selectedFileLabel.setStyle("-fx-text-fill: #888888;");
        
        addCourseOverlay.setVisible(true);
    }
    
    /**
     * Handles the cancel button action in the dialog.
     *
     * @param event The action event
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        addCourseOverlay.setVisible(false);
    }
    
    /**
     * Handles the save button action in the dialog.
     *
     * @param event The action event
     */
    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }
        
        if (selectedFile != null) {
            try {
                String coursesDir = "courses";
                Path dirPath = Paths.get(coursesDir);
                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                }
                
                courseFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path targetPath = dirPath.resolve(courseFileName);
                
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "File Error", "Could not save the course PDF file.");
                return;
            }
        }
        
        Course newCourse = new Course(
            courseNameField.getText(),
            courseDescriptionField.getText(),
            "",
            currentUser.getId()
        );
        
        if (courseFileName != null) {
            newCourse.setPdfPath(courseFileName);
        }
        
        String educationLevel = levelComboBox.getSelectionModel().getSelectedItem();
        newCourse.setTargetLevel(educationLevel);
        
        boolean success = CourseService.addCourse(newCourse);
        
        if (success) {
            addCourseOverlay.setVisible(false);
            loadTeacherCourses();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Course added successfully!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add the course.");
        }
    }
    
    /**
     * Validates form inputs for course creation or editing.
     *
     * @return True if all inputs are valid, false otherwise
     */
    private boolean validateInputs() {
        if (courseNameField.getText() == null || courseNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a course name.");
            return false;
        }
        
        if (courseDescriptionField.getText() == null || courseDescriptionField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a course description.");
            return false;
        }
        
        if (levelComboBox.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select an education level.");
            return false;
        }
        
        if (selectedFile == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a PDF file for the course.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Handles editing a course.
     *
     * @param course The course to edit
     */
    private void handleEditCourse(Course course) {
        Label dialogTitle = (Label) dialogContainer.lookup(".dialog-title");
        if (dialogTitle != null) {
            dialogTitle.setText("Edit Course");
        }
        
        courseNameField.setText(course.getTitle());
        courseDescriptionField.setText(course.getDescription());
        
        if (course.getTargetLevel() != null && !course.getTargetLevel().isEmpty()) {
            levelComboBox.getSelectionModel().select(course.getTargetLevel());
        } else {
            levelComboBox.getSelectionModel().select("L1");
        }
        
        if (course.getPdfPath() != null && !course.getPdfPath().isEmpty()) {
            selectedFileLabel.setText(course.getPdfPath());
            selectedFileLabel.setStyle("-fx-text-fill: white;");
            courseFileName = course.getPdfPath();
        } else {
            selectedFileLabel.setText("No file selected");
            selectedFileLabel.setStyle("-fx-text-fill: #888888;");
            courseFileName = null;
        }
        selectedFile = null;
        
        Button saveButton = (Button) dialogContainer.lookup(".save-button");
        if (saveButton != null) {
            saveButton.setOnAction(e -> handleUpdateCourse(course.getId()));
        }
        
        addCourseOverlay.setVisible(true);
    }
    
    /**
     * Handles deleting a course.
     *
     * @param course The course to delete
     */
    private void handleDeleteCourse(Course course) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete the course: " + course.getTitle() + "?");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = CourseService.deleteCourse(course.getId());
                
                if (success) {
                    loadTeacherCourses();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Course deleted successfully");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete course");
                }
            }
        });
    }
    
    /**
     * Handles the search action.
     *
     * @param event The action event
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField.getText().trim();
        filterCourses(query);
    }
    
    /**
     * Helper method to show alerts.
     *
     * @param alertType The type of alert to show
     * @param title The title of the alert
     * @param message The message to display in the alert
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Handles updating an existing course.
     *
     * @param courseId The ID of the course to update
     */
    private void handleUpdateCourse(int courseId) {
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
        
        if (selectedFile != null) {
            try {
                String coursesDir = "courses";
                Path dirPath = Paths.get(coursesDir);
                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                }
                
                courseFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path targetPath = dirPath.resolve(courseFileName);
                
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "File Error", "Could not save the course PDF file.");
                return;
            }
        }
        
        Course updatedCourse = new Course();
        updatedCourse.setId(courseId);
        updatedCourse.setTitle(courseNameField.getText());
        updatedCourse.setDescription(courseDescriptionField.getText());
        updatedCourse.setComment(""); 
        updatedCourse.setTeacherId(currentUser.getId());
        
        if (courseFileName != null) {
            updatedCourse.setPdfPath(courseFileName);
        }
        
        String educationLevel = levelComboBox.getSelectionModel().getSelectedItem();
        updatedCourse.setTargetLevel(educationLevel);
        
        boolean success = CourseService.updateCourse(updatedCourse);
        
        if (success) {
            addCourseOverlay.setVisible(false);
            
            Button saveButton = (Button) dialogContainer.lookup(".save-button");
            if (saveButton != null) {
                saveButton.setOnAction(this::handleSave);
            }
            
            Label dialogTitle = (Label) dialogContainer.lookup(".dialog-title");
            if (dialogTitle != null) {
                dialogTitle.setText("Add new Course");
            }
            
            loadTeacherCourses();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Course updated successfully!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update the course.");
        }
    }

    /**
     * Handles viewing a course.
     *
     * @param course The course to view
     */
    private void handleViewCourse(Course course) {
        try {
            if (course.getPdfPath() == null || course.getPdfPath().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "No PDF Available", 
                    "This course does not have a PDF file attached.");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PdfCourseViewer.fxml"));
            Parent courseViewerParent = loader.load();
            
            ViewCourseController controller = loader.getController();
            
            if (currentUser != null) {
                controller.setCourse(course, currentUser.getId());
            } else {
                controller.setCourse(course);
            }
            
            StackPane contentArea = (StackPane) courseCardsContainer.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(courseViewerParent);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load course viewer: " + e.getMessage());
        }
    }
}