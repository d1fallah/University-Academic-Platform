package app.frontend;

import app.backend.models.Quiz;
import app.backend.models.QuizResult;
import app.backend.models.User;
import app.backend.models.Question;
import app.backend.models.StudentAnswer;
import app.backend.models.Answer;
import app.backend.models.Course;
import app.backend.services.QuizResultService;
import app.backend.services.AuthService;
import app.backend.services.CourseService;
import app.backend.services.QuestionService;
import app.backend.services.StudentAnswerService;
import app.backend.services.AnswerService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for managing and displaying quiz results for teachers.
 * This class handles viewing quiz results, searching students, and displaying detailed results.
 *
 * @author Sellami Mohamed Odai
 */
public class TeacherQuizResultsController implements Initializable {    
    
    /** Title label for the quiz results */
    @FXML private Label titleLabel;
    
    /** Container for displaying quiz results */
    @FXML private VBox resultsContainer;
    
    /** Button to return to previous view */
    @FXML private Button returnButton;
    
    /** Scroll pane containing the results */
    @FXML private ScrollPane scrollPane;
    
    /** Search field for finding specific students */
    @FXML private TextField searchStudentField;
    
    /** Label for quiz description */
    @FXML private Label quizInfoLabel;
    
    /** Label for course name */
    @FXML private Label courseNameLabel;
    
    /** Label for displaying average score */
    @FXML private Label averageScoreLabel;
    
    /** Currently selected quiz */
    private Quiz currentQuiz;
    
    /** All quiz results for the current quiz */
    private List<QuizResult> allResults;
    
    /** Questions belonging to the current quiz */
    private List<Question> quizQuestions;
    
    /** Current teacher user */
    private User currentUser;
    
    /**
     * Initializes the controller.
     * Sets up UI components and event handlers.
     *
     * @param location The location used to resolve relative paths for the root object
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = AuthLoginController.getCurrentUser();
        returnButton.setOnAction(event -> handleReturn());
        
        if (searchStudentField != null) {
            searchStudentField.textProperty().addListener((observable, oldValue, newValue) -> 
                handleStudentSearch()
            );
        }
    }
    
    /**
     * Filters and displays quiz results based on student name search.
     * Handles the student search functionality when text is entered in the search field.
     */
    @FXML
    public void handleStudentSearch() {
        if (allResults == null || allResults.isEmpty()) {
            return;
        }
        
        String searchQuery = searchStudentField.getText().toLowerCase().trim();
        
        if (searchQuery.isEmpty()) {
            displayResults(allResults);
        } else {
            List<QuizResult> filteredResults = allResults.stream()
                .filter(result -> {
                    User student = AuthService.getUserById(result.getStudentId());
                    return student != null && 
                           student.getName().toLowerCase().contains(searchQuery);
                })
                .collect(Collectors.toList());
            
            displayResults(filteredResults);
        }
    }
    
    /**
     * Sets the current quiz and loads its results.
     * Updates UI elements with quiz information and loads results.
     *
     * @param quiz The quiz to display results for
     */
    public void setQuiz(Quiz quiz) {
        this.currentQuiz = quiz;
        titleLabel.setText("Results for: " + quiz.getTitle());
        
        if (quizInfoLabel != null) {
            quizInfoLabel.setText(quiz.getDescription());
        }
        
        if (courseNameLabel != null && quiz.getCourseId() > 0) {
            String courseName = "Unknown Course";
            Course course = CourseService.getCourseById(quiz.getCourseId());
            if (course != null) {
                courseName = course.getTitle();
            }
            courseNameLabel.setText("Course: " + courseName);
        }
        
        this.quizQuestions = QuestionService.getQuestionsByQuizId(quiz.getId());
        loadResults();
    }
    
    /**
     * Loads all quiz results for the current quiz.
     * Fetches results, calculates average score, and displays the results list.
     */
    private void loadResults() {
        resultsContainer.getChildren().clear();
        allResults = QuizResultService.getResultsByQuizId(currentQuiz.getId());
        
        if (averageScoreLabel != null && !allResults.isEmpty()) {
            double averageScore = allResults.stream()
                .mapToInt(QuizResult::getScore)
                .average()
                .orElse(0.0);
            averageScoreLabel.setText(String.format("Average Score: %.1f%%", averageScore));
        }
        
        displayResults(allResults);
    }
    
    /**
     * Displays quiz results in the container.
     * Shows a message if no results exist, otherwise displays sorted results.
     *
     * @param results The list of quiz results to display
     */
    private void displayResults(List<QuizResult> results) {
        resultsContainer.getChildren().clear();
        
        if (results.isEmpty()) {
            Label noResultsLabel = new Label("No quiz results found.");
            noResultsLabel.getStyleClass().add("no-data-message");
            noResultsLabel.setPadding(new Insets(20, 0, 0, 0));
            resultsContainer.getChildren().add(noResultsLabel);
        } else {
            List<QuizResult> sortedResults = new ArrayList<>(results);
            sortedResults.sort(Comparator.comparing(QuizResult::getScore).reversed());
            
            for (QuizResult result : sortedResults) {
                resultsContainer.getChildren().add(createResultItem(result));
            }
        }
    }
    
    /**
     * Creates a list item for a quiz result.
     * Builds a UI component displaying student information and quiz score.
     *
     * @param result The quiz result to display
     * @return HBox container with the formatted result information
     */
    private HBox createResultItem(QuizResult result) {
        User student = AuthService.getUserById(result.getStudentId());
        
        HBox itemContainer = new HBox();
        itemContainer.getStyleClass().add("submission-item");
        itemContainer.setAlignment(Pos.CENTER_LEFT);
        itemContainer.setSpacing(15);
        itemContainer.setPadding(new Insets(15));
        
        VBox studentInfo = new VBox(5);
        studentInfo.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(studentInfo, Priority.ALWAYS);
        
        Label nameLabel = new Label(student != null ? student.getName() : "Unknown Student");
        nameLabel.getStyleClass().add("submission-name");
        
        Label matriculeLabel = new Label(student != null ? student.getMatricule() : "Unknown");
        matriculeLabel.getStyleClass().add("submission-matricule");
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy 'at' HH:mm");
        String submissionDate = result.getSubmittedAt() != null ? 
                              dateFormat.format(result.getSubmittedAt()) : "Unknown date";
        Label dateLabel = new Label("Submitted: " + submissionDate);
        dateLabel.getStyleClass().add("submission-date");
        
        studentInfo.getChildren().addAll(nameLabel, matriculeLabel, dateLabel);
        
        HBox scoreContainer = new HBox(5);
        scoreContainer.setAlignment(Pos.CENTER);
        scoreContainer.setPrefWidth(100);
        
        int totalQuestions = quizQuestions != null ? quizQuestions.size() : 0;
        int correctAnswers = Math.round((result.getScore() * totalQuestions) / 100.0f);
        
        String scoreText = correctAnswers + "/" + totalQuestions;
        Label scoreLabel = new Label(scoreText);
        scoreLabel.getStyleClass().add("submission-score");
        
        String scoreColor;
        if (result.getScore() >= 80) {
            scoreColor = "#10b981";
        } else if (result.getScore() >= 60) {
            scoreColor = "#f59e0b";
        } else {
            scoreColor = "#f43f5e";
        }
        
        scoreLabel.setStyle("-fx-text-fill: " + scoreColor + "; -fx-font-size: 20px; -fx-font-weight: bold;");
        scoreContainer.getChildren().add(scoreLabel);
        
        Button viewButton = new Button("View Details");
        viewButton.getStyleClass().add("download-button");
        viewButton.setOnAction(event -> openQuizDetails(result));
        
        itemContainer.getChildren().addAll(studentInfo, scoreContainer, viewButton);
        
        return itemContainer;
    }
    
    /**
     * Opens the quiz details view for a specific quiz result.
     * Prepares and loads detailed view of a student's quiz responses.
     *
     * @param result The quiz result to view in detail
     */
    private void openQuizDetails(QuizResult result) {
        try {
            List<Question> questions = QuestionService.getQuestionsByQuizId(currentQuiz.getId());
            if (questions == null) {
                questions = new ArrayList<>();
            }
            
            List<StudentAnswer> studentAnswers = StudentAnswerService.getStudentAnswers(result.getId());
            
            if (questions.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "No Questions", "This quiz has no questions to display.");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuizResult.fxml"));
            Parent resultView = loader.load();
            ViewQuizResultController controller = loader.getController();
            
            Course course = CourseService.getCourseById(currentQuiz.getCourseId());
            User teacher = (course != null) ? AuthService.getUserById(course.getTeacherId()) : currentUser;
            controller.setTeacher(teacher);
            
            List<Integer> userAnswerList = new ArrayList<>();
            List<Answer> correctAnswerList = new ArrayList<>();
            int correctAnswers = 0;
            int incorrectAnswers = 0;
            
            for (Question question : questions) {
                Integer selectedAnswerId = null;
                boolean foundCorrect = false;
                
                for (StudentAnswer ans : studentAnswers) {
                    if (ans.getQuestionId() == question.getId()) {
                        selectedAnswerId = ans.getSelectedAnswerId();
                        if (ans.isCorrect()) {
                            correctAnswers++;
                            foundCorrect = true;
                        }
                        break;
                    }
                }
                
                if (!foundCorrect) {
                    incorrectAnswers++;
                }
                
                userAnswerList.add(selectedAnswerId != null ? selectedAnswerId : -1);
                
                Answer correctAnswer = findCorrectAnswer(question.getId());
                correctAnswerList.add(correctAnswer);
            }
            
            controller.setResultData(correctAnswers, questions.size(), correctAnswers, incorrectAnswers, 
                null, questions, userAnswerList, correctAnswerList);
            controller.disableRetakeQuiz();
            
            StackPane contentArea = (StackPane) resultsContainer.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(resultView);
            } else {
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Cannot navigate to result view.");
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load quiz details: " + e.getMessage());
        }
    }
    
    /**
     * Finds the correct answer for a given question.
     * Returns a placeholder if no correct answer is found.
     * 
     * @param questionId The ID of the question
     * @return The correct answer or a placeholder
     */
    private Answer findCorrectAnswer(int questionId) {
        List<Answer> answers = AnswerService.getAnswersByQuestionId(questionId);
        
        for (Answer answer : answers) {
            if (answer.isCorrect()) {
                return answer;
            }
        }
        
        Answer placeholder = new Answer();
        placeholder.setQuestionId(questionId);
        placeholder.setAnswerText("No correct answer found");
        placeholder.setCorrect(true);
        return placeholder;
    }
    
    /**
     * Handles the return to quizzes button action.
     * Navigates back to the teacher quizzes view.
     */
    private void handleReturn() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TeacherQuizzes.fxml"));
            Parent quizzesView = loader.load();
            
            StackPane contentArea = (StackPane) resultsContainer.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(quizzesView);
            } else {
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Cannot navigate back to quizzes.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate back to quizzes: " + e.getMessage());
        }
    }
    
    /**
     * Displays an alert dialog with the given parameters.
     * 
     * @param type The type of alert to show
     * @param title The title of the alert
     * @param content The content message for the alert
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}