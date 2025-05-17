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

/**
 * Controller class for managing the student exercises view.
 * This class handles displaying exercises from a specific teacher,
 * filtering exercises, and navigating to exercise content.
 *
 * @author Sellami Mohamed Odai
 */
public class StudentExercisesController implements Initializable {

    /** Flow pane container for displaying exercise cards */
    @FXML private FlowPane exercisesFlowPane;
    
    /** Label for displaying teacher name */
    @FXML private Label teacherNameLabel;
    
    /** Button for returning to teacher list */
    @FXML private Button backButton;
    
    /** Text field for searching exercises */
    @FXML private TextField searchField;
    
    /** Image view for teacher profile image */
    @FXML private ImageView teacherProfileImage;
    
    /** Current teacher whose exercises are being displayed */
    private User teacher;
    
    /** Currently logged-in user */
    private User currentUser;
    
    /** List of exercises belonging to the current teacher */
    private List<Exercise> teacherExercises = new ArrayList<>();

    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded.
     * 
     * @param location The location used to resolve relative paths for the root object
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = AuthLoginController.getCurrentUser();
        backButton.setOnAction(e -> handleBackToTeachers());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterExercises(newValue));
    }
    
    /**
     * Sets the teacher whose exercises will be displayed
     * 
     * @param teacher The teacher user object
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
            System.err.println("Failed to load teacher profile image: " + e.getMessage());
        }
        
        loadTeacherExercises();
    }
    
    /**
     * Sets the teacher by ID and loads their information
     * 
     * @param teacherId The ID of the teacher to display exercises for
     */
    public void setTeacherId(int teacherId) {
        User teacher = AuthService.getUserById(teacherId);
        if (teacher != null) {
            setTeacher(teacher);
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Teacher not found with ID: " + teacherId);
        }
    }
    
    /**
     * Loads exercises from the specified teacher based on user role and level
     */
    private void loadTeacherExercises() {
        if (teacher == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No teacher specified.");
            return;
        }
        
        if (currentUser != null && currentUser.getRole().equals("student")) {
            String studentLevel = currentUser.getEnrollmentLevel();
            if (studentLevel == null || studentLevel.isEmpty()) {
                studentLevel = "L1";
            }
            teacherExercises = ExerciseService.getExercisesByTeacherAndLevel(teacher.getId(), studentLevel);
        } else {
            teacherExercises = ExerciseService.getExercisesByTeacherId(teacher.getId());
        }
        
        displayExercises(teacherExercises);
    }
    
    /**
     * Displays the provided list of exercises as cards in the flow pane
     * 
     * @param exercises The list of exercises to display
     */
    private void displayExercises(List<Exercise> exercises) {
        exercisesFlowPane.getChildren().clear();
        
        if (exercises.isEmpty()) {
            Label noExercisesLabel = new Label("No exercises available for your enrollment level from this teacher yet.");
            noExercisesLabel.getStyleClass().add("no-courses-message");
            noExercisesLabel.setPrefWidth(exercisesFlowPane.getPrefWidth());
            noExercisesLabel.setPrefHeight(200);
            noExercisesLabel.setAlignment(Pos.CENTER);
            noExercisesLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
            
            exercisesFlowPane.getChildren().add(noExercisesLabel);
        } else {
            exercises.forEach(exercise -> 
                exercisesFlowPane.getChildren().add(createExerciseCard(exercise))
            );
        }
    }
    
    /**
     * Filters exercises by title or description based on search text
     * 
     * @param searchText The text to search for
     */
    private void filterExercises(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            displayExercises(teacherExercises);
            return;
        }
        
        String searchLower = searchText.toLowerCase();
        List<Exercise> filteredExercises = teacherExercises.stream()
            .filter(exercise -> 
                exercise.getTitle().toLowerCase().contains(searchLower) ||
                (exercise.getDescription() != null && 
                 exercise.getDescription().toLowerCase().contains(searchLower)))
            .collect(Collectors.toList());
        
        displayExercises(filteredExercises);
    }
    
    /**
     * Handles the search action when Enter is pressed
     * 
     * @param event The action event
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        filterExercises(searchField.getText());
    }
    
    /**
     * Creates a visual card representation for an exercise
     * 
     * @param exercise The exercise to create a card for
     * @return A StackPane containing the styled exercise card
     */
    private StackPane createExerciseCard(Exercise exercise) {
        // Card dimensions
        final int CARD_WIDTH = 480;
        final int CARD_HEIGHT = 230;
        
        // Main card container
        StackPane cardPane = new StackPane();
        cardPane.getStyleClass().add("course-card");
        cardPane.setPrefWidth(CARD_WIDTH);
        cardPane.setPrefHeight(CARD_HEIGHT);
        
        // Create background
        ImageView cardBackground = createCardBackground(CARD_WIDTH);
        
        // Create content container
        VBox cardContent = createCardContentContainer(CARD_WIDTH, CARD_HEIGHT);
        
        // Create header with title and icon
        HBox headerBox = createHeaderWithTitle(exercise, CARD_WIDTH);
        
        // Create description
        Label descriptionLabel = createDescriptionLabel(exercise);
        
        // Create course info
        HBox courseBox = createCourseInfoBox(exercise);
        
        // Space filler
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // Create footer with teacher info and view button
        HBox footerBox = createFooterWithButton(exercise, CARD_WIDTH);
        
        // Assemble card
        cardContent.getChildren().addAll(headerBox, descriptionLabel, courseBox, spacer, footerBox);
        cardPane.getChildren().addAll(cardBackground, cardContent);
        
        // Make the entire card clickable
        cardPane.setOnMouseClicked(e -> openExerciseViewer(exercise));
        
        return cardPane;
    }
    
    /**
     * Creates the card background with image
     */
    private ImageView createCardBackground(int width) {
        ImageView cardBackground = new ImageView();
        try {
            Image bgImage = new Image(getClass().getResourceAsStream("/images/courseCardBackground.png"));
            cardBackground.setImage(bgImage);
            cardBackground.setFitWidth(width);
            cardBackground.setFitHeight(270);
            cardBackground.setPreserveRatio(false);
            cardBackground.setOpacity(0.7);
        } catch (Exception e) {
            System.err.println("Failed to load background image for exercise card: " + e.getMessage());
        }
        return cardBackground;
    }
    
    /**
     * Creates the main content container for the card
     */
    private VBox createCardContentContainer(int width, int height) {
        VBox cardContent = new VBox();
        cardContent.getStyleClass().add("card-content");
        cardContent.setSpacing(20);
        cardContent.setPadding(new Insets(20));
        cardContent.setPrefWidth(width);
        cardContent.setPrefHeight(height);
        return cardContent;
    }
    
    /**
     * Creates the header box with title and icon
     */
    private HBox createHeaderWithTitle(Exercise exercise, int width) {
        HBox headerBox = new HBox();
        headerBox.getStyleClass().add("card-header");
        headerBox.setAlignment(Pos.TOP_LEFT);
        headerBox.setPrefWidth(width);
        headerBox.setSpacing(20);
        
        // Title container
        VBox titleContainer = new VBox();
        titleContainer.setAlignment(Pos.TOP_LEFT);
        titleContainer.setPrefWidth(width - 90);
        HBox.setHgrow(titleContainer, Priority.ALWAYS);
        
        // Title
        Label titleLabel = new Label(exercise.getTitle());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);
        titleLabel.setMinHeight(Region.USE_PREF_SIZE);
        titleContainer.getChildren().add(titleLabel);
        
        // Exercise icon
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
            exerciseIcon.setFitWidth(50);
            exerciseIcon.setFitHeight(50);
            exerciseIcon.getStyleClass().add("course-icon");
        } catch (Exception e) {
            System.err.println("Failed to load logo for exercise: " + e.getMessage());
        }
        
        logoContainer.getChildren().add(exerciseIcon);
        headerBox.getChildren().addAll(titleContainer, logoContainer);
        
        return headerBox;
    }
    
    /**
     * Creates the description label for the exercise
     */
    private Label createDescriptionLabel(Exercise exercise) {
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
        
        return descriptionLabel;
    }
    
    /**
     * Creates the course info box
     */
    private HBox createCourseInfoBox(Exercise exercise) {
        HBox courseBox = new HBox();
        courseBox.setAlignment(Pos.CENTER_LEFT);
        courseBox.setSpacing(5);
        
        String courseName = "Unknown Course";
        try {
            app.backend.models.Course course = app.backend.services.CourseService.getCourseById(exercise.getCourseId());
            if (course != null) {
                courseName = course.getTitle();
            }
        } catch (Exception e) {
            System.err.println("Failed to get course info: " + e.getMessage());
        }
        
        Label courseLabel = new Label("Course: " + courseName);
        courseLabel.getStyleClass().add("card-instructor");
        courseLabel.setStyle("-fx-text-fill: white;");
        
        courseBox.getChildren().add(courseLabel);
        return courseBox;
    }
    
    /**
     * Creates the footer with instructor info and view button
     */
    private HBox createFooterWithButton(Exercise exercise, int width) {
        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.BOTTOM_LEFT);
        footerBox.setPrefWidth(width);
        
        // Instructor info
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
            System.err.println("Failed to load instructor icon: " + e.getMessage());
        }
        
        Label instructorLabel = new Label("Prof. " + teacher.getName());
        instructorLabel.getStyleClass().add("instructor-name");
        instructorBox.getChildren().addAll(instructorIcon, instructorLabel);
        
        // Button
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button viewButton = new Button("View Exercise");
        viewButton.getStyleClass().add("view-course-button");
        viewButton.setStyle("-fx-background-color: #be123c;");
        viewButton.setPrefWidth(120);
        viewButton.setPrefHeight(24);
        viewButton.setOnAction(e -> openExerciseViewer(exercise));
        
        buttonBox.getChildren().add(viewButton);
        footerBox.getChildren().addAll(instructorBox, buttonBox);
        
        return footerBox;
    }
    
    /**
     * Opens the exercise viewer for a specific exercise
     * 
     * @param exercise The exercise to view
     */
    private void openExerciseViewer(Exercise exercise) {
        try {
            if (exercise.getPdfPath() == null || exercise.getPdfPath().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "No PDF Available", 
                    "This exercise does not have a PDF file attached.");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PdfExerciseViewer.fxml"));
            Parent exerciseViewerParent = loader.load();
            
            ViewExerciseController controller = loader.getController();
            controller.setExercise(exercise);
            
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TeachersCards.fxml"));
            Parent teachersView = loader.load();
            
            TeachersCardsController controller = loader.getController();
            controller.setIsExerciseView(true);
            
            if (currentUser != null && currentUser.getRole().equals("teacher")) {
                controller.setExcludeCurrentTeacher(true);
                controller.setShowManageCourseButton(true);
            }
            
            StackPane contentArea = (StackPane) exercisesFlowPane.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(teachersView);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not return to teachers list.");
        }
    }
    
    /**
     * Helper method to show alerts to the user
     * 
     * @param type The alert type (e.g., ERROR, WARNING, INFORMATION)
     * @param title The title of the alert
     * @param content The content message of the alert
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 