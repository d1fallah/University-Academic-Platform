package app.frontend;

import app.backend.models.User;
import app.backend.services.CourseService;
import app.backend.services.ExerciseService;
import app.backend.services.PracticalWorkService;
import app.backend.services.QuizService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controller for the student dashboard view.
 * Handles the interaction between the student dashboard UI and the application logic.
 * Displays student information, course statistics, and provides navigation to learning resources.
 * 
 * @author Sellami Mohamed Odai
 */
public class StudentDashboardController {
    /** Current date display label */
    @FXML private Label dateLabel;
    
    /** Student name display label */
    @FXML private Label studentNameLabel;
    
    /** Courses count display label */
    @FXML private Label coursesCountLabel;
    
    /** Quizzes count display label */
    @FXML private Label quizzesCountLabel;
    
    /** Exercises count display label */
    @FXML private Label exercisesCountLabel;
    
    /** Practical works count display label */
    @FXML private Label practicalCountLabel;

    /** Course navigation access point */
    @FXML private StackPane courseAccess;
    
    /** Quiz navigation access point */
    @FXML private StackPane quizAccess;
    
    /** Exercise navigation access point */
    @FXML private StackPane exerciseAccess;
    
    /** Practical work navigation access point */
    @FXML private StackPane practicalWorkAccess;
    
    /** Currently logged-in user */
    private User currentUser;
    
    /** Reference to parent application controller */
    private ApplicationController parentController;
    
    /**
     * Initializes the controller.
     * Sets up the UI with current date, user information, and statistics.
     */
    @FXML
    public void initialize() {
        currentUser = AuthLoginController.getCurrentUser();
        
        initializeDate();
        initializeUserName();
        updateStatistics();
    }
    
    /**
     * Sets the current date in the dashboard.
     */
    private void initializeDate() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        dateLabel.setText(now.format(formatter));
    }
    
    /**
     * Sets the user's name in the dashboard.
     */
    private void initializeUserName() {
        if (currentUser != null) {
            studentNameLabel.setText(currentUser.getName() + "!");
        } else {
            studentNameLabel.setText("Student!");
        }
    }
    
    /**
     * Sets the reference to the parent ApplicationController.
     * 
     * @param controller The ApplicationController to set
     */
    public void setApplicationController(ApplicationController controller) {
        this.parentController = controller;
        System.out.println("ApplicationController successfully set on StudentDashboardController");
    }
    
    /**
     * Updates the statistics displayed in the dashboard.
     * Fetches course, quiz, exercise, and practical work counts from services.
     */
    private void updateStatistics() {
        try {
            if (currentUser == null) {
                return;
            }
            
            String studentLevel = currentUser.getEnrollmentLevel();
            
            int courseCount = CourseService.getCourseCountByLevel(studentLevel);
            int quizCount = QuizService.getQuizCountByLevel(studentLevel);
            int exerciseCount = ExerciseService.getExerciseCountByLevel(studentLevel);
            int practicalCount = PracticalWorkService.getPracticalWorkCountByLevel(studentLevel);
            
            coursesCountLabel.setText(String.valueOf(courseCount));
            quizzesCountLabel.setText(String.valueOf(quizCount));
            exercisesCountLabel.setText(String.valueOf(exerciseCount));
            practicalCountLabel.setText(String.valueOf(practicalCount));
        } catch (Exception e) {
            System.err.println("Error fetching statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Retrieves the parent ApplicationController instance.
     * First tries the direct reference, then looks in scene properties if needed.
     * 
     * @return The ApplicationController instance or null if not found
     */
    private ApplicationController getParentController() {
        if (parentController != null) {
            return parentController;
        }
        
        Scene scene = dateLabel.getScene();
        if (scene != null) {
            Parent root = scene.getRoot();
            if (root != null && root.getProperties().containsKey("parentController")) {
                parentController = (ApplicationController) root.getProperties().get("parentController");
                return parentController;
            }
        }
          System.out.println("Could not find parent controller in scene properties");
        return null;
    }
    
    /**
     * Handles click event on the courses section.
     * Navigates to the courses view.
     */
    @FXML
    public void handleCoursesClick() {
        navigateToView("Courses", controller -> controller.loadCourses());
    }
    
    /**
     * Handles click event on the quizzes section.
     * Navigates to the quizzes view.
     */
    @FXML
    public void handleQuizzesClick() {
        navigateToView("Quizzes", controller -> controller.loadQuizzes());
    }
    
    /**
     * Handles click event on the exercises section.
     * Navigates to the exercises view.
     */
    @FXML
    public void handleExercisesClick() {
        navigateToView("Exercises", controller -> controller.loadExercises());
    }
    
    /**
     * Handles click event on the practical works section.
     * Navigates to the practical works view.
     */
    @FXML
    public void handlePracticalWorkClick() {
        navigateToView("Practical Works", controller -> controller.loadPracticalWorks());
    }
    
    /**
     * Helper method to navigate to different views.
     * 
     * @param destination The name of the destination view
     * @param action The action to perform on the controller
     */
    private void navigateToView(String destination, java.util.function.Consumer<ApplicationController> action) {
        System.out.println("Navigating to " + destination);
        ApplicationController controller = getParentController();
        if (controller != null) {
            action.accept(controller);
        } else {
            System.out.println("ERROR: Cannot navigate - ApplicationController is null");
        }
    }
}
