package app.frontend;

import app.backend.models.User;
import app.backend.services.AuthService;
import app.backend.services.CourseService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
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
import javafx.scene.text.TextAlignment;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TeachersCardsController implements Initializable {

    @FXML private FlowPane teacherCardsContainer;
    @FXML private Button backToCoursesButton;
    @FXML private TextField searchField;
    @FXML private Button manageCourseButton;
    @FXML private Label viewTitleLabel;
    
    private User currentUser;
    private List<User> allTeachers = new ArrayList<>();
    private boolean excludeCurrentTeacher = false;
    private boolean showManageCourseButton = false;
    private boolean isQuizView = false;
    private boolean isExerciseView = false;
    private User lastViewedTeacher = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current logged in user
        currentUser = LoginController.getCurrentUser();
        
        // Load all teachers
        loadAllTeachers();
        
        // Set up search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTeachers(newValue);
        });
        
        // Update view title and button text based on isQuizView flag
        updateViewLabels();
    }
    
    /**
     * Updates the view labels and button text based on view type
     */
    private void updateViewLabels() {
        // Update the view title
        if (viewTitleLabel != null) {
            if (isExerciseView) {
                viewTitleLabel.setText("Exercises");
            } else if (isQuizView) {
                viewTitleLabel.setText("Quizzes");
            } else {
                viewTitleLabel.setText("Courses");
            }
        }
        
        // Update the manage button text
        if (manageCourseButton != null) {
            if (isExerciseView) {
                manageCourseButton.setText("Manage my exercises");
            } else if (isQuizView) {
                manageCourseButton.setText("Manage my quizzes");
            } else {
                manageCourseButton.setText("Manage my courses");
            }
        }
        
        // Update search field prompt
        if (searchField != null) {
            if (isExerciseView) {
                searchField.setPromptText("Search teachers for exercises...");
            } else if (isQuizView) {
                searchField.setPromptText("Search teachers for quizzes...");
            } else {
                searchField.setPromptText("Search teachers...");
            }
        }
    }
    
    /**
     * Loads all teachers and stores them
     */
    private void loadAllTeachers() {
        // Get all teachers and store for filtering
        allTeachers = AuthService.getAllTeachers();
        
        // If needed, exclude the current teacher
        if (excludeCurrentTeacher && currentUser != null) {
            allTeachers = allTeachers.stream()
                .filter(teacher -> teacher.getId() != currentUser.getId())
                .collect(Collectors.toList());
        }
        
        // Display the teachers
        displayTeachers(allTeachers);
        
        // Update manage courses button visibility
        if (manageCourseButton != null) {
            manageCourseButton.setVisible(showManageCourseButton);
            manageCourseButton.setManaged(showManageCourseButton);
        }
    }
    
    /**
     * Displays the provided list of teachers as cards
     */
    private void displayTeachers(List<User> teachers) {
        // Clear the container
        teacherCardsContainer.getChildren().clear();
        
        if (teachers.isEmpty()) {
            // Display message if no teachers
            Label noTeachersLabel = new Label("No teachers available yet.");
            noTeachersLabel.getStyleClass().add("no-teachers-message");
            noTeachersLabel.setPrefWidth(teacherCardsContainer.getPrefWidth());
            noTeachersLabel.setPrefHeight(200);
            noTeachersLabel.setAlignment(Pos.CENTER);
            noTeachersLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
            
            teacherCardsContainer.getChildren().add(noTeachersLabel);
        } else {
            // Create and add a card for each teacher
            for (User teacher : teachers) {
                teacherCardsContainer.getChildren().add(createTeacherCard(teacher));
            }
        }
    }
    
    /**
     * Filters teachers by name based on search text
     */
    private void filterTeachers(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            // If search is empty, show all teachers
            displayTeachers(allTeachers);
        } else {
            // Filter teachers whose names contain the search text (case insensitive)
            List<User> filteredTeachers = allTeachers.stream()
                .filter(teacher -> teacher.getName().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
            
            displayTeachers(filteredTeachers);
        }
    }
    
    /**
     * Handles the search action when Enter is pressed
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        filterTeachers(searchField.getText());
    }
    
    /**
     * Creates a visual card for a teacher
     */
    private StackPane createTeacherCard(User teacher) {
        // Main card container
        StackPane cardPane = new StackPane();
        cardPane.getStyleClass().add("course-card"); // Reusing course card styling
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
            System.out.println("Failed to load background image for teacher card");
        }

        // Card layout container
        VBox cardContent = new VBox();
        cardContent.getStyleClass().add("card-content");
        cardContent.setSpacing(20);
        cardContent.setPadding(new Insets(10, 10, 10, 10));
        cardContent.setPrefWidth(480);
        cardContent.setPrefHeight(230);
        cardContent.setAlignment(Pos.CENTER);

        // Profile image
        ImageView profileImage = new ImageView();
        try {
            Image profileImg = new Image(getClass().getResourceAsStream("/images/profilep.png"));
            profileImage.setImage(profileImg);
            profileImage.setFitWidth(80);
            profileImage.setFitHeight(80);
            profileImage.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Failed to load profile image for teacher: " + teacher.getName());
        }

        // Teacher name
        Label nameLabel = new Label("Prof. " + teacher.getName());
        nameLabel.getStyleClass().add("card-title");
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        nameLabel.setAlignment(Pos.CENTER);

        // Create the appropriate button based on the view
        String buttonText;
        String buttonStyle = "";
        
        if (isExerciseView) {
            buttonText = "View Exercises";
            buttonStyle = "-fx-background-color: #be123c;";
        } else if (isQuizView) {
            buttonText = "View Quizzes";
            buttonStyle = "-fx-background-color: #0095ff;";
        } else {
            buttonText = "View Courses";
            buttonStyle = "-fx-background-color: #65558f;";
        }
        
        // Debug - print out what section we're in
        System.out.println("Creating teacher card with isExerciseView: " + isExerciseView + 
                           ", isQuizView: " + isQuizView + 
                           ", buttonText: " + buttonText);
        
        Button viewButton = new Button(buttonText);
        viewButton.getStyleClass().add("view-course-button");
        viewButton.setStyle(buttonStyle);
        viewButton.setPrefWidth(150);
        viewButton.setPrefHeight(30);
        viewButton.setOnAction(e -> {
            if (isExerciseView) {
                handleViewTeacherExercises(teacher);
            } else if (isQuizView) {
                handleViewTeacherQuizzes(teacher);
            } else {
                handleViewTeacherCourses(teacher);
            }
        });

        // Add all sections to the card
        cardContent.getChildren().addAll(profileImage, nameLabel, viewButton);

        // Stack the background and content
        cardPane.getChildren().addAll(cardBackground, cardContent);

        // Add accessibility features
        cardPane.setAccessibleText("Teacher: " + teacher.getName());

        return cardPane;
    }
    
    /**
     * Handles the action when a user clicks on a teacher card to view exercises
     */
    private void handleViewTeacherExercises(User teacher) {
        try {
            // Load the teacher exercises view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teacher-exercises.fxml"));
            Parent exercisesView = loader.load();
            
            // Get the controller and set the teacher
            TeacherExercisesController controller = loader.getController();
            controller.setTeacher(teacher);
            
            // Store the last viewed teacher
            lastViewedTeacher = teacher;
            
            // Get the main layout's content area and set the exercises view
            StackPane contentArea = (StackPane) teacherCardsContainer.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(exercisesView);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to load teacher-exercises.fxml");
        }
    }
    
    /**
     * Handles the action when a user clicks on a teacher card to view courses
     */
    private void handleViewTeacherCourses(User teacher) {
        try {
            // Load the courses cards view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teacher-courses.fxml"));
            Parent coursesView = loader.load();
            
            // Get the controller and set the teacher
            TeacherCoursesController controller = loader.getController();
            controller.setTeacher(teacher);
            
            // Get the main layout's content area and set the courses view
            StackPane contentArea = (StackPane) teacherCardsContainer.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(coursesView);
            
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load teacher courses: " + e.getMessage());
        }
    }

    /**
     * Handles the action when a user clicks on a teacher card to view quizzes
     */
    private void handleViewTeacherQuizzes(User teacher) {
        try {
            // Store the teacher being viewed
            lastViewedTeacher = teacher;
            
            // Load the teacher quizzes view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teacher-quizzes.fxml"));
            Parent quizzesView = loader.load();
            
            // Get the controller and set the teacher
            TeacherQuizzesController controller = loader.getController();
            controller.setTeacher(teacher);
            
            // Get the main layout's content area and set the quizzes view
            StackPane contentArea = (StackPane) teacherCardsContainer.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(quizzesView);
            
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load teacher quizzes: " + e.getMessage());
        }
    }

    /**
     * Handles the manage button click action
     */
    @FXML
    private void handleManageCourses(ActionEvent event) {
        try {
            Parent contentView;
            
            if (isExerciseView) {
                // Load My Exercises view
                contentView = FXMLLoader.load(getClass().getResource("/fxml/my-exercises.fxml"));
            } else if (isQuizView) {
                // Load My Quizzes view
                contentView = FXMLLoader.load(getClass().getResource("/fxml/my-quizzes.fxml"));
            } else {
                // Load My Courses view
                contentView = FXMLLoader.load(getClass().getResource("/fxml/my-courses.fxml"));
            }
            
            // Get the content area and set the view
            StackPane contentArea = (StackPane) manageCourseButton.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(contentView);
            
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to load management view");
        }
    }

    /**
     * Set whether to exclude the current teacher from the displayed teachers
     */
    public void setExcludeCurrentTeacher(boolean excludeCurrentTeacher) {
        this.excludeCurrentTeacher = excludeCurrentTeacher;
        // If we're already initialized, reload teachers
        if (teacherCardsContainer != null) {
            loadAllTeachers();
        }
    }
    
    /**
     * Set whether to show the manage courses button
     */
    public void setShowManageCourseButton(boolean showManageCourseButton) {
        this.showManageCourseButton = showManageCourseButton;
        // If button and flag exist, update visibility
        if (manageCourseButton != null) {
            manageCourseButton.setVisible(showManageCourseButton);
            manageCourseButton.setManaged(showManageCourseButton);
        }
    }

    /**
     * Set whether we're in quiz view
     */
    public void setIsQuizView(boolean isQuizView) {
        this.isQuizView = isQuizView;
        // Update view labels and button text
        updateViewLabels();
        // If we're already initialized, reload teachers to update button text
        if (teacherCardsContainer != null) {
            loadAllTeachers();
        }
    }

    /**
     * Get the last viewed teacher
     */
    public User getLastViewedTeacher() {
        return lastViewedTeacher;
    }

    /**
     * Sets whether this is an exercise view
     */
    public void setIsExerciseView(boolean isExerciseView) {
        System.out.println("Setting isExerciseView to: " + isExerciseView);
        this.isExerciseView = isExerciseView;
        
        // Make sure quiz view is set to false if exercise view is true
        if (isExerciseView) {
            this.isQuizView = false;
        }
        
        updateViewLabels();
        
        // If this is set after initialization, update button text and reload teachers
        if (manageCourseButton != null) {
            manageCourseButton.setText(isExerciseView ? "Manage my exercises" : 
                (isQuizView ? "Manage my quizzes" : "Manage my courses"));
        }
        
        // Immediately update existing cards if container exists
        if (teacherCardsContainer != null) {
            loadAllTeachers();
        }
    }
    
    /**
     * Returns whether this is an exercise view
     */
    public boolean isExerciseView() {
        return isExerciseView;
    }
    
    /**
     * Returns whether this is a quiz view
     */
    public boolean isQuizView() {
        return isQuizView;
    }
}