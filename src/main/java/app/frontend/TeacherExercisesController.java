package app.frontend;

import app.backend.models.Exercise;
import app.backend.models.User;
import app.backend.services.ExerciseService;
import app.backend.services.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TeacherExercisesController implements Initializable {

    @FXML private FlowPane exercisesFlowPane;
    @FXML private Label teacherNameLabel;
    @FXML private Button backButton;
    @FXML private TextField searchField;
    @FXML private ImageView teacherProfileImage;
    
    private User teacher;
    private User currentUser;
    private List<Exercise> teacherExercises = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current logged in user
        currentUser = LoginController.getCurrentUser();
        
        // Set up back button
        backButton.setOnAction(e -> handleBackToTeachers());
        
        // Set up search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterExercises(newValue);
        });
    }
    
    /**
     * Sets the teacher whose exercises will be displayed
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
        
        // Load exercises for this teacher
        loadTeacherExercises();
    }
    
    /**
     * Sets the teacher ID directly
     */
    public void setTeacherId(int teacherId) {
        // Fetch the teacher from the database
        User teacher = AuthService.getUserById(teacherId);
        if (teacher != null) {
            setTeacher(teacher);
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Teacher not found with ID: " + teacherId);
        }
    }
    
    /**
     * Loads exercises from the specified teacher
     */
    private void loadTeacherExercises() {
        if (teacher == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No teacher specified.");
            return;
        }
        
        // Get exercises filtered by teacher ID and student enrollment level
        if (currentUser != null && currentUser.getRole().equals("student")) {
            // For students, filter by their level
            String studentLevel = currentUser.getEnrollmentLevel();
            if (studentLevel == null || studentLevel.isEmpty()) {
                studentLevel = "L1"; // Default level if not set
            }
            teacherExercises = ExerciseService.getExercisesByTeacherAndLevel(teacher.getId(), studentLevel);
        } else {
            // For teachers or admins, show all
            teacherExercises = ExerciseService.getExercisesByTeacherId(teacher.getId());
        }
        
        // Display the exercises
        displayExercises(teacherExercises);
    }
    
    /**
     * Displays the provided list of exercises as cards
     */
    private void displayExercises(List<Exercise> exercises) {
        // Clear the container
        exercisesFlowPane.getChildren().clear();
        
        // Create and add a card for each exercise
        if (exercises.isEmpty()) {
            // Display "No exercises available" message
            Label noExercisesLabel = new Label("No exercises available for your enrollment level from this teacher yet.");
            noExercisesLabel.getStyleClass().add("no-courses-message");
            noExercisesLabel.setPrefWidth(exercisesFlowPane.getPrefWidth());
            noExercisesLabel.setPrefHeight(200);
            noExercisesLabel.setAlignment(Pos.CENTER);
            noExercisesLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
            
            exercisesFlowPane.getChildren().add(noExercisesLabel);
        } else {
            for (Exercise exercise : exercises) {
                exercisesFlowPane.getChildren().add(createExerciseCard(exercise));
            }
        }
    }
    
    /**
     * Filters exercises by title based on search text
     */
    private void filterExercises(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            // If search is empty, show all teacher's exercises
            displayExercises(teacherExercises);
        } else {
            // Filter exercises based on title or description containing search text
            List<Exercise> filteredExercises = teacherExercises.stream()
                .filter(exercise -> 
                    exercise.getTitle().toLowerCase().contains(searchText.toLowerCase()) ||
                    (exercise.getDescription() != null && exercise.getDescription().toLowerCase().contains(searchText.toLowerCase())))
                .collect(Collectors.toList());
            
            displayExercises(filteredExercises);
        }
    }
    
    /**
     * Handles the search action when Enter is pressed
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        filterExercises(searchField.getText());
    }
    
    /**
     * Creates a visual card representation for an exercise
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
        exerciseIcon.getStyleClass().add("course-icon");

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

        // Course info instead of level
        HBox courseBox = new HBox();
        courseBox.setAlignment(Pos.CENTER_LEFT);
        courseBox.setSpacing(5);
        
        // Get course name from course ID
        String courseName = "Unknown Course";
        try {
            app.backend.models.Course course = app.backend.services.CourseService.getCourseById(exercise.getCourseId());
            if (course != null) {
                courseName = course.getTitle();
            }
        } catch (Exception e) {
            System.out.println("Failed to get course info: " + e.getMessage());
        }
        
        Label courseLabel = new Label("Course: " + courseName);
        courseLabel.getStyleClass().add("card-instructor");
        courseLabel.setStyle("-fx-text-fill: white;");
        
        courseBox.getChildren().add(courseLabel);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Footer section with instructor info and button
        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.BOTTOM_LEFT);
        footerBox.setPrefWidth(480);

        // Instructor info section - now showing the teacher's name
        HBox instructorBox = new HBox();
        instructorBox.getStyleClass().add("card-instructor");
        instructorBox.setAlignment(Pos.CENTER_LEFT);
        instructorBox.setPrefHeight(30);
        instructorBox.setSpacing(10);
        HBox.setHgrow(instructorBox, Priority.ALWAYS);

        // Instructor icon
        ImageView instructorIcon = new ImageView();
        try {
            Image instructorImg = new Image(getClass().getResourceAsStream("/images/Case.png"));
            instructorIcon.setImage(instructorImg);
            instructorIcon.setFitHeight(20);
            instructorIcon.setFitWidth(20);
            instructorIcon.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Failed to load instructor icon");
        }
        
        Label instructorLabel = new Label("Prof. " + teacher.getName());
        instructorLabel.getStyleClass().add("instructor-name");

        instructorBox.getChildren().addAll(instructorIcon, instructorLabel);

        // Button section
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button viewButton = new Button("View Exercise");
        viewButton.getStyleClass().add("view-course-button");
        viewButton.setStyle("-fx-background-color: #be123c;");
        viewButton.setPrefWidth(120);
        viewButton.setPrefHeight(24);
        viewButton.setOnAction(e -> openExerciseViewer(exercise));

        buttonBox.getChildren().add(viewButton);

        // Add instructor and button to footer
        footerBox.getChildren().addAll(instructorBox, buttonBox);

        // Add all sections to the card
        cardContent.getChildren().addAll(headerBox, descriptionLabel, courseBox, spacer, footerBox);

        // Stack the background and content
        cardPane.getChildren().addAll(cardBackground, cardContent);

        // Make the entire card clickable
        cardPane.setOnMouseClicked(e -> openExerciseViewer(exercise));

        return cardPane;
    }
    
    /**
     * Opens the exercise viewer for a specific exercise
     */
    private void openExerciseViewer(Exercise exercise) {
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
            controller.setExercise(exercise);
            
            // Get the main layout's content area and set the exercise viewer
            StackPane contentArea = (StackPane) exercisesFlowPane.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(exerciseViewerParent);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load exercise viewer: " + e.getMessage());
        }
    }
    
    /**
     * Handles the back button action to return to the teachers list
     */
    private void handleBackToTeachers() {
        try {
            // Load the teachers cards view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teachers-cards.fxml"));
            Parent teachersView = loader.load();
            
            // Get the controller and set exercise view
            TeachersCardsController controller = loader.getController();
            controller.setIsExerciseView(true);
            
            // Get the main layout's content area and set the teachers view
            StackPane contentArea = (StackPane) exercisesFlowPane.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(teachersView);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not return to teachers list.");
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