package app.frontend;

import app.backend.models.User;
import app.backend.services.CourseService;
import app.backend.services.ExerciseService;
import app.backend.services.PracticalWorkService;
import app.backend.services.QuizService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controller for the teacher dashboard view that displays statistics and
 * provides navigation to other teacher-specific pages.
 * 
 * @author Sellami Mohamed Odai
 */
public class TeacherDashboardController {
    
    /** Label displaying current date */
    @FXML private Label dateLabel;
    
    /** Label displaying teacher's name */
    @FXML private Label teacherNameLabel;
    
    /** Label displaying number of courses */
    @FXML private Label coursesCountLabel;
    
    /** Label displaying number of quizzes */
    @FXML private Label quizzesCountLabel;
    
    /** Label displaying number of exercises */
    @FXML private Label exercisesCountLabel;
    
    /** Label displaying number of practical works */
    @FXML private Label practicalCountLabel;    /** Currently logged in user */
    private User currentUser;
    
    /** Reference to the parent application controller */
    private ApplicationController parentController;
    
    /**
     * Initializes the controller after its root element has been completely processed.
     * Sets up the UI with current date, teacher name, and statistics.
     */
    @FXML
    public void initialize() {
        currentUser = AuthLoginController.getCurrentUser();
        
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        dateLabel.setText(now.format(formatter));
        
        teacherNameLabel.setText(currentUser != null ? currentUser.getName() + "!" : "Teacher!");
          updateStatistics();
    }
    
    /**
     * Sets the reference to the parent ApplicationController.
     *
     * @param controller The parent ApplicationController to set
     */
    public void setApplicationController(ApplicationController controller) {
        this.parentController = controller;        System.out.println("ApplicationController successfully set on TeacherDashboardController");
    }
    
    /**
     * Updates the dashboard statistics with counts of courses, quizzes, exercises, 
     * and practical works associated with the current teacher.
     */
    private void updateStatistics() {
        try {
            if (currentUser != null) {
                int teacherId = currentUser.getId();
                
                int courseCount = CourseService.getCourseCountByTeacher(teacherId);
                int quizCount = QuizService.getQuizCountByTeacher(teacherId);
                int exerciseCount = ExerciseService.getExerciseCountByTeacher(teacherId);
                int practicalCount = PracticalWorkService.getPracticalWorkCountByTeacher(teacherId);
                
                coursesCountLabel.setText(String.valueOf(courseCount));
                quizzesCountLabel.setText(String.valueOf(quizCount));
                exercisesCountLabel.setText(String.valueOf(exerciseCount));
                practicalCountLabel.setText(String.valueOf(practicalCount));
            }
        } catch (Exception e) {
            System.err.println("Error fetching statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Retrieves the ApplicationController from the stored reference or scene properties.
     * First attempts to use the direct reference, then tries to find it in scene properties.
     *
     * @return The ApplicationController or null if not found
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
     * Handles click event on Courses section, navigating to teacher courses management page.
     */
    @FXML
    public void handleCoursesClick() {
        System.out.println("Navigating to Teacher Courses management");
        ApplicationController controller = getParentController();
        if (controller != null) {
            loadTeacherPage("TeacherCourses.fxml", controller);
        } else {
            System.out.println("ERROR: Cannot navigate - ApplicationController is null");
        }
    }
    
    /**
     * Handles click event on Quizzes section, navigating to teacher quizzes management page.
     */
    @FXML
    public void handleQuizzesClick() {
        System.out.println("Navigating to Teacher Quizzes management");
        ApplicationController controller = getParentController();
        if (controller != null) {
            loadTeacherPage("TeacherQuizzes.fxml", controller);
        } else {
            System.out.println("ERROR: Cannot navigate - ApplicationController is null");
        }
    }
    
    /**
     * Handles click event on Exercises section, navigating to teacher exercises management page.
     */
    @FXML
    public void handleExercisesClick() {
        System.out.println("Navigating to Teacher Exercises management");
        ApplicationController controller = getParentController();
        if (controller != null) {
            loadTeacherPage("TeacherExercises.fxml", controller);
        } else {
            System.out.println("ERROR: Cannot navigate - ApplicationController is null");
        }
    }
    
    /**
     * Handles click event on Practical Work section, navigating to teacher practical works management page.
     */
    @FXML
    public void handlePracticalWorkClick() {
        System.out.println("Navigating to Teacher Practical Work management");
        ApplicationController controller = getParentController();
        if (controller != null) {
            loadTeacherPage("TeacherPracticalWorks.fxml", controller);
        } else {
            System.out.println("ERROR: Cannot navigate - ApplicationController is null");
        }
    }
    
    /**
     * Loads a specific teacher page using the appropriate method in ApplicationController.
     * Falls back to general navigation methods if the specific method fails.
     *
     * @param fxmlPage The FXML page name to load
     * @param controller The ApplicationController reference
     */
    private void loadTeacherPage(String fxmlPage, ApplicationController controller) {
        try {
            switch (fxmlPage) {
                case "TeacherCourses.fxml":
                    controller.loadTeacherCourses();
                    break;
                case "TeacherQuizzes.fxml":
                    controller.loadTeacherQuizzes();
                    break;
                case "TeacherExercises.fxml":
                    controller.loadTeacherExercises();
                    break;
                case "TeacherPracticalWorks.fxml":
                    controller.loadTeacherPracticalWorks();
                    break;
                default:
                    System.out.println("Unknown page: " + fxmlPage);
                    loadFallbackPage(fxmlPage, controller);
            }
        } catch (Exception e) {
            System.out.println("Error loading " + fxmlPage + ": " + e.getMessage());
            e.printStackTrace();
            loadFallbackPage(fxmlPage, controller);
        }
    }
      /**
     * Loads a fallback page when specific teacher page loading fails.
     * 
     * @param fxmlPage The FXML page name to check
     * @param controller The ApplicationController reference
     */
    private void loadFallbackPage(String fxmlPage, ApplicationController controller) {
        if (fxmlPage.contains("Courses")) {
            controller.loadCourses();
        } else if (fxmlPage.contains("Quizzes")) {
            controller.loadQuizzes();
        } else if (fxmlPage.contains("Exercises")) {
            controller.loadExercises();
        } else if (fxmlPage.contains("PracticalWorks")) {
            controller.loadPracticalWorks();
        }
    }
}