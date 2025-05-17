package app.frontend;

import app.backend.models.User;
import app.backend.models.PracticalWork;
import app.backend.services.AuthService;
import app.backend.services.CourseService;
import app.backend.services.ExerciseService;
import app.backend.services.PracticalWorkService;
import app.backend.services.QuizService;
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

/**
 * Controller for the Teachers Cards view that displays teachers in card format.
 * Supports different view modes: courses, exercises, quizzes, and practical works.
 * 
 * @author Sellami Mohamed Odai
 */
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
    private boolean isPracticalWorkView = false;
    private User lastViewedTeacher = null;

    /**
     * Initializes the controller, sets up the UI components and loads the data.
     *
     * @param location  The location used to resolve relative paths for the root object
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = AuthLoginController.getCurrentUser();
        loadAllTeachers();
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTeachers(newValue);
        });
        
        updateViewLabels();
    }
    
    /**
     * Updates view labels and button text based on current view type (courses, exercises, quizzes, or practical works).
     */
    private void updateViewLabels() {
        if (viewTitleLabel != null) {
            if (isExerciseView) {
                viewTitleLabel.setText("Exercises");
            } else if (isQuizView) {
                viewTitleLabel.setText("Quizzes");
            } else if (isPracticalWorkView) {
                viewTitleLabel.setText("Practical Works");
            } else {
                viewTitleLabel.setText("Courses");
            }
        }
        
        if (manageCourseButton != null) {
            if (isExerciseView) {
                manageCourseButton.setText("Manage my exercises");
            } else if (isQuizView) {
                manageCourseButton.setText("Manage my quizzes");
            } else if (isPracticalWorkView) {
                manageCourseButton.setText("Manage my practical works");
            } else {
                manageCourseButton.setText("Manage my courses");
            }
        }
        
        if (searchField != null) {
            if (isExerciseView) {
                searchField.setPromptText("Search teachers for exercises...");
            } else if (isQuizView) {
                searchField.setPromptText("Search teachers for quizzes...");
            } else if (isPracticalWorkView) {
                searchField.setPromptText("Search teachers for practical works...");
            } else {
                searchField.setPromptText("Search teachers...");
            }
        }
    }
    
    /**
     * Loads all relevant teachers based on view type and user role.
     */
    private void loadAllTeachers() {
        String studentLevel = null;
        boolean isStudent = false;
        
        if (currentUser != null && currentUser.getRole().equals("student")) {
            studentLevel = currentUser.getEnrollmentLevel();
            isStudent = true;
            if (studentLevel == null || studentLevel.isEmpty()) {
                studentLevel = "L1";
            }
        }
        
        if (isStudent) {
            if (isExerciseView) {
                allTeachers = ExerciseService.getTeachersWithExercisesByLevel(studentLevel);
            } else if (isQuizView) {
                allTeachers = QuizService.getTeachersWithQuizzesByLevel(studentLevel);
            } else if (isPracticalWorkView) {
                allTeachers = PracticalWorkService.getTeachersWithPracticalWorksByLevel(studentLevel);
            } else {
                allTeachers = CourseService.getTeachersWithCoursesByLevel(studentLevel);
            }
        } else {
            allTeachers = AuthService.getAllTeachers();
        }
        
        if (excludeCurrentTeacher && currentUser != null) {
            allTeachers = allTeachers.stream()
                .filter(teacher -> teacher.getId() != currentUser.getId())
                .collect(Collectors.toList());
        }
        
        displayTeachers(allTeachers);
        
        if (manageCourseButton != null) {
            manageCourseButton.setVisible(showManageCourseButton);
            manageCourseButton.setManaged(showManageCourseButton);
        }
    }
    
    /**
     * Displays teacher cards in the UI based on the provided list of teachers.
     * 
     * @param teachers The list of teachers to display
     */
    private void displayTeachers(List<User> teachers) {
        teacherCardsContainer.getChildren().clear();
        
        if (teachers.isEmpty()) {
            Label noTeachersLabel = new Label("No teachers available yet.");
            noTeachersLabel.getStyleClass().add("no-teachers-message");
            noTeachersLabel.setPrefWidth(teacherCardsContainer.getPrefWidth());
            noTeachersLabel.setPrefHeight(200);
            noTeachersLabel.setAlignment(Pos.CENTER);
            noTeachersLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
            
            teacherCardsContainer.getChildren().add(noTeachersLabel);
        } else {
            for (User teacher : teachers) {
                teacherCardsContainer.getChildren().add(createTeacherCard(teacher));
            }
        }
    }
    
    /**
     * Filters the displayed teachers based on search text.
     * 
     * @param searchText The search text to filter teachers by name
     */
    private void filterTeachers(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            displayTeachers(allTeachers);
        } else {
            List<User> filteredTeachers = allTeachers.stream()
                .filter(teacher -> teacher.getName().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
            
            displayTeachers(filteredTeachers);
        }
    }
    
    /**
     * Handles search action when the Enter key is pressed.
     * 
     * @param event The action event
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        filterTeachers(searchField.getText());
    }
    
    /**
     * Creates a visual card for a teacher.
     * 
     * @param teacher The teacher to create a card for
     * @return A StackPane containing the teacher card UI
     */
    private StackPane createTeacherCard(User teacher) {
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
            System.out.println("Failed to load background image for teacher card");
        }

        VBox cardContent = new VBox();
        cardContent.getStyleClass().add("card-content");
        cardContent.setSpacing(20);
        cardContent.setPadding(new Insets(10, 10, 10, 10));
        cardContent.setPrefWidth(480);
        cardContent.setPrefHeight(230);
        cardContent.setAlignment(Pos.CENTER);

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

        Label nameLabel = new Label("Prof. " + teacher.getName());
        nameLabel.getStyleClass().add("card-title");
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        nameLabel.setAlignment(Pos.CENTER);

        String buttonText;
        String buttonStyle = "";
        
        if (isExerciseView) {
            buttonText = "View Exercises";
            buttonStyle = "-fx-background-color: #be123c;";
        } else if (isQuizView) {
            buttonText = "View Quizzes";
            buttonStyle = "-fx-background-color: #0095ff;";
        } else if (isPracticalWorkView) {
            buttonText = "View Practical Works";
            buttonStyle = "-fx-background-color: #d97706;";
        } else {
            buttonText = "View Courses";
            buttonStyle = "-fx-background-color: #65558f;";
        }
        
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
            } else if (isPracticalWorkView) {
                handleViewTeacherPracticalWorks(teacher);
            } else {
                handleViewTeacherCourses(teacher);
            }
        });

        cardContent.getChildren().addAll(profileImage, nameLabel, viewButton);
        cardPane.getChildren().addAll(cardBackground, cardContent);
        cardPane.setAccessibleText("Teacher: " + teacher.getName());

        return cardPane;
    }
    
    /**
     * Handles the action when a user clicks to view a teacher's exercises.
     * 
     * @param teacher The teacher whose exercises should be displayed
     */
    private void handleViewTeacherExercises(User teacher) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StudentExercises.fxml"));
            Parent exercisesView = loader.load();
            
            StudentExercisesController controller = loader.getController();
            controller.setTeacher(teacher);
            
            lastViewedTeacher = teacher;
            
            StackPane contentArea = (StackPane) teacherCardsContainer.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(exercisesView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Handles the action when a user clicks to view a teacher's content (courses, quizzes, etc).
     * 
     * @param teacher The teacher whose content should be displayed
     */
    private void handleViewTeacherCourses(User teacher) {
        try {
            lastViewedTeacher = teacher;
            
            String fxmlPath;
            if (isExerciseView) {
                fxmlPath = "/fxml/StudentExercises.fxml";
            } else if (isQuizView) {
                fxmlPath = "/fxml/StudentQuizzes.fxml";
            } else if (isPracticalWorkView) {
                fxmlPath = "/fxml/StudentPracticalWorks.fxml";
            } else {
                fxmlPath = "/fxml/StudentCourses.fxml";
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent teacherView = loader.load();
            
            if (isExerciseView) {
                StudentExercisesController controller = loader.getController();
                controller.setTeacher(teacher);
            } else if (isQuizView) {
                StudentQuizzesController controller = loader.getController();
                controller.setTeacher(teacher);
            } else if (isPracticalWorkView) {
                StudentPracticalWorksController controller = loader.getController();
                controller.setTeacher(teacher);
            } else {
                StudentCoursesController controller = loader.getController();
                controller.setTeacher(teacher);
            }
            
            StackPane contentArea = (StackPane) teacherCardsContainer.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(teacherView);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the action when a user clicks to view a teacher's quizzes.
     * 
     * @param teacher The teacher whose quizzes should be displayed
     */
    private void handleViewTeacherQuizzes(User teacher) {
        try {
            lastViewedTeacher = teacher;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StudentQuizzes.fxml"));
            Parent quizzesView = loader.load();

            StudentQuizzesController controller = loader.getController();
            controller.setTeacher(teacher);

            StackPane contentArea = (StackPane) teacherCardsContainer.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(quizzesView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the action when a user clicks to view a teacher's practical works.
     * 
     * @param teacher The teacher whose practical works should be displayed
     */
    private void handleViewTeacherPracticalWorks(User teacher) {
        try {
            lastViewedTeacher = teacher;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StudentPracticalWorks.fxml"));
            Parent practicalWorksView = loader.load();

            StudentPracticalWorksController controller = loader.getController();
            controller.setTeacher(teacher);

            StackPane contentArea = (StackPane) teacherCardsContainer.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(practicalWorksView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the manage button click action to manage courses, quizzes, 
     * exercises, or practical works.
     * 
     * @param event The action event
     */
    @FXML
    private void handleManageCourses(ActionEvent event) {
        try {
            Parent contentView;

            if (isExerciseView) {
                contentView = FXMLLoader.load(getClass().getResource("/fxml/TeacherExercises.fxml"));
            } else if (isQuizView) {
                contentView = FXMLLoader.load(getClass().getResource("/fxml/TeacherQuizzes.fxml"));
            } else if (isPracticalWorkView) {
                contentView = FXMLLoader.load(getClass().getResource("/fxml/TeacherPracticalWorks.fxml"));
            } else {
                contentView = FXMLLoader.load(getClass().getResource("/fxml/TeacherCourses.fxml"));
            }

            StackPane contentArea = (StackPane) manageCourseButton.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(contentView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets whether to exclude the current teacher from the displayed teachers.
     * 
     * @param excludeCurrentTeacher Whether to exclude the current teacher
     */
    public void setExcludeCurrentTeacher(boolean excludeCurrentTeacher) {
        this.excludeCurrentTeacher = excludeCurrentTeacher;
        if (teacherCardsContainer != null) {
            loadAllTeachers();
        }
    }

    /**
     * Sets whether to show the manage course button.
     * 
     * @param showManageCourseButton Whether to show the manage course button
     */
    public void setShowManageCourseButton(boolean showManageCourseButton) {
        this.showManageCourseButton = showManageCourseButton;
        if (manageCourseButton != null) {
            manageCourseButton.setVisible(showManageCourseButton);
            manageCourseButton.setManaged(showManageCourseButton);
        }
    }

    /**
     * Sets whether this is a quiz view.
     * 
     * @param isQuizView Whether this is a quiz view
     */
    public void setIsQuizView(boolean isQuizView) {
        this.isQuizView = isQuizView;
        updateViewLabels();
        if (teacherCardsContainer != null) {
            loadAllTeachers();
        }
    }

    /**
     * Returns the last viewed teacher.
     * 
     * @return The last viewed teacher
     */
    public User getLastViewedTeacher() {
        return lastViewedTeacher;
    }

    /**
     * Sets whether this is an exercise view.
     * 
     * @param isExerciseView Whether this is an exercise view
     */
    public void setIsExerciseView(boolean isExerciseView) {
        this.isExerciseView = isExerciseView;

        if (isExerciseView) {
            this.isQuizView = false;
        }

        updateViewLabels();

        if (teacherCardsContainer != null) {
            loadAllTeachers();
        }
    }

    /**
     * Returns whether this is an exercise view.
     * 
     * @return Whether this is an exercise view
     */
    public boolean isExerciseView() {
        return isExerciseView;
    }

    /**
     * Returns whether this is a quiz view.
     * 
     * @return Whether this is a quiz view
     */
    public boolean isQuizView() {
        return isQuizView;
    }

    /**
     * Sets whether this is a practical work view.
     * 
     * @param isPracticalWorkView Whether this is a practical work view
     */
    public void setIsPracticalWorkView(boolean isPracticalWorkView) {
        this.isPracticalWorkView = isPracticalWorkView;
        updateViewLabels();
        if (teacherCardsContainer != null) {
            loadAllTeachers();
        }
    }

    /**
     * Returns whether this is a practical work view.
     * 
     * @return Whether this is a practical work view
     */
    public boolean isPracticalWorkView() {
        return isPracticalWorkView;
    }
}
