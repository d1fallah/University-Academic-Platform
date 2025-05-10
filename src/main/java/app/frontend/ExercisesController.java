package app.frontend;

import app.backend.models.Course;
import app.backend.models.Exercise;
import app.backend.models.User;
import app.backend.services.CourseService;
import app.backend.services.ExerciseService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ExercisesController implements Initializable {

    @FXML private FlowPane exercisesFlowPane;
    @FXML private Label headerLabel;
    
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current logged in user
        currentUser = LoginController.getCurrentUser();
        
        if (currentUser == null) {
            showAlert(Alert.AlertType.WARNING, "Not Logged In", "Please log in to access exercises.");
            return;
        }
        
        // Different views for teachers and students
        if (currentUser.getRole().equals("teacher")) {
            initializeTeacherView();
        } else {
            initializeStudentView();
        }
    }
    
    /**
     * Initializes the teacher view of exercises
     */
    private void initializeTeacherView() {
        headerLabel.setText("Exercises - Teacher View");
        
        // Display other teachers with exercises
        loadTeachersWithExercises();
        
        // Add button to manage own exercises
        Button manageButton = new Button("Manage My Exercises");
        manageButton.getStyleClass().add("primary-button");
        manageButton.setPrefWidth(200);
        manageButton.setOnAction(event -> openMyExercises());
        
        VBox manageBox = new VBox(manageButton);
        manageBox.setAlignment(Pos.CENTER);
        manageBox.setPadding(new Insets(20));
        manageBox.setPrefWidth(exercisesFlowPane.getPrefWidth());
        
        exercisesFlowPane.getChildren().add(0, manageBox);
    }
    
    /**
     * Opens the my-exercises view for teachers to manage their exercises
     */
    private void openMyExercises() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/my-exercises.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Manage My Exercises");
            stage.setScene(new Scene(root));
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open the exercises management.");
        }
    }
    
    /**
     * Initializes the student view of exercises
     */
    private void initializeStudentView() {
        headerLabel.setText("Available Exercises");
        
        // Load teachers who have exercises for the student's level
        String studentLevel = currentUser.getEnrollmentLevel();
        if (studentLevel == null || studentLevel.isEmpty()) {
            studentLevel = "L1"; // Default level if not set
        }
        
        List<User> teachersWithExercises = ExerciseService.getTeachersWithExercisesByLevel(studentLevel);
        loadTeacherCards(teachersWithExercises, studentLevel);
    }
    
    /**
     * Loads teachers who have published exercises
     */
    private void loadTeachersWithExercises() {
        List<User> teachersWithExercises = ExerciseService.getTeachersWithExercises();
        
        if (teachersWithExercises.isEmpty()) {
            Label noTeachersLabel = new Label("No teachers have published exercises yet.");
            noTeachersLabel.getStyleClass().add("no-results-message");
            exercisesFlowPane.getChildren().add(noTeachersLabel);
            return;
        }
        
        loadTeacherCards(teachersWithExercises, null);
    }
    
    /**
     * Creates and displays teacher cards
     */
    private void loadTeacherCards(List<User> teachers, String studentLevel) {
        if (teachers.isEmpty()) {
            Label noTeachersLabel = new Label("No teachers have published exercises for your level yet.");
            noTeachersLabel.getStyleClass().add("no-results-message");
            exercisesFlowPane.getChildren().add(noTeachersLabel);
            return;
        }
        
        // Create a card for each teacher
        for (User teacher : teachers) {
            exercisesFlowPane.getChildren().add(createTeacherCard(teacher, studentLevel));
        }
    }
    
    /**
     * Creates a card for a teacher
     */
    private VBox createTeacherCard(User teacher, String studentLevel) {
        // Create card container
        VBox card = new VBox();
        card.getStyleClass().add("teacher-card");
        card.setPrefWidth(320);
        card.setPrefHeight(220);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(20));
        card.setSpacing(15);
        
        // Teacher avatar/icon
        try {
            ImageView teacherIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/teacher_avatar.png")));
            teacherIcon.setFitHeight(80);
            teacherIcon.setFitWidth(80);
            teacherIcon.setPreserveRatio(true);
            card.getChildren().add(teacherIcon);
        } catch (Exception e) {
            // If image fails to load, just show the name with more emphasis
        }
        
        // Teacher name
        Label nameLabel = new Label(teacher.getName());
        nameLabel.getStyleClass().add("teacher-card-name");
        nameLabel.setWrapText(true);
        
        // Teacher university if available
        if (teacher.getUniversityName() != null && !teacher.getUniversityName().isEmpty()) {
            Label uniLabel = new Label(teacher.getUniversityName());
            uniLabel.getStyleClass().add("teacher-card-university");
            uniLabel.setWrapText(true);
            card.getChildren().add(uniLabel);
        }
        
        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // View Exercises button
        Button viewButton = new Button("View Exercises");
        viewButton.getStyleClass().add("view-exercises-button");
        viewButton.setOnAction(event -> viewTeacherExercises(teacher, studentLevel));
        
        // Add components to card
        card.getChildren().addAll(nameLabel, spacer, viewButton);
        
        return card;
    }
    
    /**
     * Displays the exercises for a selected teacher
     */
    private void viewTeacherExercises(User teacher, String studentLevel) {
        // Clear the current content
        exercisesFlowPane.getChildren().clear();
        
        // Get exercises for this teacher, filtered by student level if applicable
        List<Exercise> teacherExercises;
        if (studentLevel != null) {
            teacherExercises = ExerciseService.getExercisesByTeacherAndLevel(teacher.getId(), studentLevel);
        } else {
            teacherExercises = ExerciseService.getExercisesByTeacherId(teacher.getId());
        }
        
        // Add a back button
        Button backButton = new Button("< Back to Teachers");
        backButton.getStyleClass().add("back-button");
        backButton.setOnAction(event -> {
            exercisesFlowPane.getChildren().clear();
            if (currentUser.getRole().equals("teacher")) {
                initializeTeacherView();
            } else {
                initializeStudentView();
            }
        });
        
        // Add the back button first
        exercisesFlowPane.getChildren().add(backButton);
        
        // Title for the exercises section
        Label teacherNameLabel = new Label("Exercises by " + teacher.getName());
        teacherNameLabel.getStyleClass().add("section-title");
        teacherNameLabel.setPrefWidth(exercisesFlowPane.getPrefWidth() - 40);
        exercisesFlowPane.getChildren().add(teacherNameLabel);
        
        if (teacherExercises.isEmpty()) {
            Label noExercisesLabel = new Label("No exercises available from this teacher" + 
                (studentLevel != null ? " for your level." : "."));
            noExercisesLabel.getStyleClass().add("no-results-message");
            exercisesFlowPane.getChildren().add(noExercisesLabel);
            return;
        }
        
        // Create a card for each exercise
        for (Exercise exercise : teacherExercises) {
            exercisesFlowPane.getChildren().add(createExerciseCard(exercise));
        }
    }
    
    /**
     * Creates a card for an exercise
     */
    private VBox createExerciseCard(Exercise exercise) {
        // Create card container
        VBox card = new VBox();
        card.getStyleClass().add("exercise-card");
        card.setPrefWidth(300);
        card.setPrefHeight(200);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(20));
        card.setSpacing(15);
        
        // Title
        Label titleLabel = new Label(exercise.getTitle());
        titleLabel.getStyleClass().add("exercise-card-title");
        titleLabel.setWrapText(true);
        
        // Description (shortened)
        String shortDesc = exercise.getDescription();
        if (shortDesc != null && shortDesc.length() > 100) {
            shortDesc = shortDesc.substring(0, 97) + "...";
        }
        Label descLabel = new Label(shortDesc);
        descLabel.getStyleClass().add("exercise-card-desc");
        descLabel.setWrapText(true);
        
        // Level indicator if available
        if (exercise.getTargetLevel() != null && !exercise.getTargetLevel().isEmpty()) {
            Label levelLabel = new Label("Level: " + exercise.getTargetLevel());
            levelLabel.getStyleClass().add("exercise-level-label");
            card.getChildren().add(levelLabel);
        }
        
        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // View Exercise button
        Button viewButton = new Button("View Exercise");
        viewButton.getStyleClass().add("view-exercise-button");
        viewButton.setOnAction(event -> openExerciseViewer(exercise));
        
        // Add components to card
        card.getChildren().addAll(titleLabel, descLabel, spacer, viewButton);
        
        return card;
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