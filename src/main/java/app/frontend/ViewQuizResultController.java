package app.frontend;

import app.backend.models.Answer;
import app.backend.models.Question;
import app.backend.models.Quiz;
import app.backend.models.User;
import app.backend.models.Course;
import app.backend.services.AnswerService;
import app.backend.services.AuthService;
import app.backend.services.CourseService;
import app.backend.services.QuizService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TabPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for displaying quiz results to students and teachers.
 * This class manages the UI for showing quiz scores, correct/incorrect answers,
 * and detailed feedback on each question.
 *
 * @author Sellami Mohamed Odai
 */
public class ViewQuizResultController implements Initializable {    
    
    /** Title label for the quiz result */
    @FXML private Label resultTitleLabel;
    
    /** Label displaying the course name */
    @FXML private Label courseNameLabel;
    
    /** Label displaying the percentage score */
    @FXML private Label percentageLabel;
    
    /** Label displaying the raw score (correct/total) */
    @FXML private Label scoreTextLabel;
    
    /** Label showing number of correct answers */
    @FXML private Label correctAnswersLabel;
    
    /** Label showing number of incorrect answers */
    @FXML private Label incorrectAnswersLabel;
    
    /** Label showing total number of questions */
    @FXML private Label totalQuestionsLabel;
    
    /** Label showing performance percentage */
    @FXML private Label performancePercentLabel;
    
    /** Label showing performance feedback message */
    @FXML private Label performanceMessage;
    
    /** Progress bar visualizing the performance */
    @FXML private ProgressBar performanceBar;
    
    /** Container for displaying question review details */
    @FXML private VBox questionsReviewContainer;
    
    /** Button to return to previous screen */
    @FXML private Button returnButton;
    
    /** Tab pane for organizing different views */
    @FXML private TabPane tabPane;

    /** List of questions in the quiz */
    private List<Question> questions;
    
    /** List of user answer IDs corresponding to questions */
    private List<Integer> userAnswers;
    
    /** List of correct answers for the questions */
    private List<Answer> correctAnswers;
    
    /** Teacher object if viewing as or for a teacher */
    private User teacher;
    
    /** ID of the quiz being viewed */
    private int quizId;
    
    /** Map storing user answer text by question ID */
    private Map<Integer, String> userAnswerTexts = new HashMap<>();    /**
     * Initializes the controller after FXML fields are injected.
     * Sets up event handlers for UI components.
     *
     * @param location The location used to resolve relative paths
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        returnButton.setOnAction(e -> handleReturn());
    }

    /**
     * Sets the quiz result data and updates the UI accordingly.
     *
     * @param correctAnswersCount Number of correct answers
     * @param totalQuestions Total number of questions
     * @param correctCount Number of correctly answered questions
     * @param incorrectCount Number of incorrectly answered questions
     * @param timeTaken Time taken to complete the quiz
     * @param questions List of questions in the quiz
     * @param userAnswers List of user's answer IDs
     * @param correctAnswers List of correct answers
     */
    public void setResultData(int correctAnswersCount, int totalQuestions, int correctCount, int incorrectCount, 
                            String timeTaken, List<Question> questions, List<Integer> userAnswers, 
                            List<Answer> correctAnswers) {
        this.questions = questions;
        this.userAnswers = userAnswers;
        this.correctAnswers = correctAnswers;
        
        prepareUserAnswerTexts();
        loadQuizData();
        updateReturnButtonForRole();

        // Calculate and display percentage
        int percentage = (correctAnswersCount * 100) / totalQuestions;
        percentageLabel.setText(percentage + "%");
        scoreTextLabel.setText(correctAnswersCount + "/" + totalQuestions);
        
        // Update statistics for overview tab
        correctAnswersLabel.setText(String.valueOf(correctCount));
        incorrectAnswersLabel.setText(String.valueOf(incorrectCount));
        totalQuestionsLabel.setText(String.valueOf(totalQuestions));
        
        updatePerformanceIndicators(percentage);

        // Populate questions review
        populateQuestionsReview();
    }

    /**
     * Updates the performance indicators (progress bar and messages) based on performance percentage.
     *
     * @param percentage The percentage score to display
     */
    private void updatePerformanceIndicators(int percentage) {
        performancePercentLabel.setText(percentage + "%");
        performanceBar.setProgress((double) percentage / 100);
        
        String barColor;
        String message;
        
        if (percentage >= 70) {
            barColor = "#10b981"; 
            message = "Great job!";
        } else if (percentage >= 40) {
            barColor = "#f59e0b"; 
            message = "Good effort, keep practicing!";
        } else {
            barColor = "#f43f5e"; 
            message = "Keep studying, you'll improve!";
        }
        
        performanceBar.setStyle("-fx-accent: " + barColor + ";");
        performanceMessage.setText(message);
    }

    /**
     * Sets the teacher for this quiz result view.
     *
     * @param teacher The teacher user object
     */
    public void setTeacher(User teacher) {
        this.teacher = teacher;
        updateReturnButtonForRole();
    }    
    
    /**
     * Populates the questions review container with each question and its answers.
     */
    private void populateQuestionsReview() {
        questionsReviewContainer.getChildren().clear();
        questionsReviewContainer.setMaxWidth(700);
        questionsReviewContainer.setPrefWidth(700);

        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            Integer userAnswerId = userAnswers.get(i);
            Answer correctAnswer = correctAnswers.get(i);

            boolean answered = (userAnswerId != null && userAnswerId != -1);
            boolean correct = answered && (userAnswerId == correctAnswer.getId());
            
            VBox questionBox = createQuestionBox(i, question, answered, correct, correctAnswer);
            questionsReviewContainer.getChildren().add(questionBox);
        }
    }
    
    /**
     * Creates a question box with all necessary UI elements for review.
     * 
     * @param index The index of the question
     * @param question The question object
     * @param answered Whether the user answered this question
     * @param correct Whether the answer was correct
     * @param correctAnswer The correct answer object
     * @return A VBox containing the formatted question review
     */
    private VBox createQuestionBox(int index, Question question, boolean answered, boolean correct, Answer correctAnswer) {
        VBox questionBox = new VBox(10);
        questionBox.getStyleClass().add("question-item");
        questionBox.getStyleClass().add(correct ? "correct" : "incorrect");
        
        HBox questionHeader = createQuestionHeader(index, question, correct);
        questionBox.getChildren().add(questionHeader);
        
        if (answered) {
            HBox userAnswerBox = createAnswerBox("Your answer:", 
                userAnswerTexts.getOrDefault(question.getId(), "Unknown answer"), 
                correct ? "correct-answer" : "incorrect-answer");
            questionBox.getChildren().add(userAnswerBox);
        } else {
            HBox noAnswerBox = new HBox();
            noAnswerBox.getStyleClass().add("answer-box");
            
            Label noAnswerLabel = new Label("You did not answer this question");
            noAnswerLabel.getStyleClass().add("no-answer-text");
            
            noAnswerBox.getChildren().add(noAnswerLabel);
            questionBox.getChildren().add(noAnswerBox);
        }

        if (!correct) {
            HBox correctAnswerBox = createAnswerBox("Correct answer:", 
                correctAnswer.getAnswerText(), "correct-answer");
            questionBox.getChildren().add(correctAnswerBox);
        }
        
        questionBox.setPadding(new Insets(12));
        return questionBox;
    }
    
    /**
     * Creates the question header containing the question text and status badge.
     * 
     * @param index The question number (0-based)
     * @param question The question object
     * @param correct Whether the answer was correct
     * @return An HBox containing the question text and status badge
     */
    private HBox createQuestionHeader(int index, Question question, boolean correct) {
        HBox questionHeader = new HBox();
        questionHeader.setAlignment(Pos.TOP_LEFT);
        questionHeader.setSpacing(10);
        questionHeader.setPrefWidth(Control.USE_COMPUTED_SIZE);
        questionHeader.setMinWidth(Control.USE_COMPUTED_SIZE);
        
        VBox questionTextContainer = new VBox();
        questionTextContainer.setPrefWidth(550);
        questionTextContainer.setMaxWidth(550);
        HBox.setHgrow(questionTextContainer, Priority.ALWAYS);
        
        Label questionText = new Label((index + 1) + ". " + question.getQuestionText());
        questionText.getStyleClass().add("question-text");
        questionText.setWrapText(true);
        questionText.setMinHeight(Label.USE_PREF_SIZE);
        
        questionTextContainer.getChildren().add(questionText);
        
        Label statusBadge = new Label(correct ? "Correct" : "Incorrect");
        statusBadge.getStyleClass().addAll("status-badge", correct ? "correct-badge" : "incorrect-badge");
        statusBadge.setMinWidth(100);
        statusBadge.setPrefWidth(100);
        statusBadge.setAlignment(Pos.CENTER);
        
        questionHeader.getChildren().addAll(questionTextContainer, statusBadge);
        return questionHeader;
    }
    
    /**
     * Creates an answer box with label and text.
     * 
     * @param labelText The label text (e.g., "Your answer:", "Correct answer:")
     * @param answerText The actual answer text
     * @param styleClass The style class to apply to the answer text
     * @return An HBox containing the formatted answer
     */
    private HBox createAnswerBox(String labelText, String answerText, String styleClass) {
        HBox answerBox = new HBox(5);
        answerBox.setAlignment(Pos.CENTER_LEFT);
        answerBox.getStyleClass().add("answer-box");
        
        Label answerLabel = new Label(labelText);
        answerLabel.getStyleClass().add("answer-label");
        answerLabel.setMinWidth(100);
        
        Label answerTextLabel = new Label(answerText);
        answerTextLabel.setWrapText(true);
        answerTextLabel.getStyleClass().add(styleClass);
        answerTextLabel.setMinHeight(Label.USE_PREF_SIZE);
        
        HBox.setHgrow(answerTextLabel, Priority.ALWAYS);
        
        answerBox.getChildren().addAll(answerLabel, answerTextLabel);
        return answerBox;
    }    
    
    /**
     * Handles the return button action, navigating to the appropriate screen
     * based on the user's role and context.
     */
    private void handleReturn() {
        try {
            User currentUser = AuthLoginController.getCurrentUser();
            
            // Try navigating in this priority order:
            // 1. Teacher quiz results (if user is a teacher)
            // 2. Student quizzes (if teacher info is available)
            // 3. Default quizzes view (fallback)
            if (navigateToTeacherQuizResults(currentUser)) {
                return;
            }
            
            if (navigateToStudentQuizzes()) {
                return;
            }
            
            navigateToDefaultQuizzesView();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Attempts to navigate to the teacher quiz results view.
     * 
     * @param currentUser The current logged-in user
     * @return True if navigation was successful, false otherwise
     * @throws IOException If loading the FXML fails
     */
    private boolean navigateToTeacherQuizResults(User currentUser) throws IOException {
        if (currentUser != null && currentUser.getRole().equals("teacher") && quizId > 0) {
            Quiz quiz = QuizService.getQuizById(quizId);
            if (quiz != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TeacherQuizResults.fxml"));
                Parent quizResultsView = loader.load();
                
                TeacherQuizResultsController controller = loader.getController();
                controller.setQuiz(quiz);
                
                StackPane contentArea = findContentArea();
                if (contentArea != null) {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(quizResultsView);
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Attempts to navigate to the student quizzes view.
     * 
     * @return True if navigation was successful, false otherwise
     * @throws IOException If loading the FXML fails
     */
    private boolean navigateToStudentQuizzes() throws IOException {
        if (teacher != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StudentQuizzes.fxml"));
            Parent teacherQuizzes = loader.load();
            
            StudentQuizzesController controller = loader.getController();
            controller.setTeacher(teacher);
            
            StackPane contentArea = findContentArea();
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(teacherQuizzes);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Navigates to the default quizzes view as fallback.
     * 
     * @throws IOException If loading the FXML fails
     */
    private void navigateToDefaultQuizzesView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quizzes.fxml"));
        Parent quizzesView = loader.load();
        
        StackPane contentArea = findContentArea();
        if (contentArea != null) {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(quizzesView);
        }
    }
    
    /**
     * Finds the content area in the scene graph using multiple strategies.
     * 
     * @return The StackPane content area, or null if not found
     */
    private StackPane findContentArea() {
        StackPane contentArea = null;
        
        // Try direct scene lookup
        if (returnButton != null && returnButton.getScene() != null) {
            contentArea = (StackPane) returnButton.getScene().lookup("#contentArea");
        }
        
        // Try parent hierarchy if direct lookup failed
        if (contentArea == null && returnButton != null) {
            Parent parent = returnButton.getParent();
            while (parent != null && contentArea == null) {
                if (parent instanceof StackPane && parent.getId() != null && parent.getId().equals("contentArea")) {
                    contentArea = (StackPane) parent;
                    break;
                }
                parent = parent.getParent();
            }
        }
        
        return contentArea;
    }    
    
    /**
     * Empty method to maintain compatibility with existing code.
     * This is a placeholder for functionality that may be implemented in the future
     * or is handled elsewhere in the application.
     */
    public void disableRetakeQuiz() {
        // This method is intentionally empty
    }

    /**
     * Prepares user answer texts by matching answer IDs to their text content.
     */
    private void prepareUserAnswerTexts() {
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            Integer userAnswerId = userAnswers.get(i);
            
            if (userAnswerId != null && userAnswerId != -1) {
                List<Answer> answers = AnswerService.getAnswersByQuestionId(question.getId());
                for (Answer answer : answers) {
                    if (answer.getId() == userAnswerId) {
                        userAnswerTexts.put(question.getId(), answer.getAnswerText());
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Loads quiz data and related information (course, teacher).
     */
    private void loadQuizData() {
        if (questions != null && !questions.isEmpty()) {
            this.quizId = questions.get(0).getQuizId();
            
            Quiz quiz = QuizService.getQuizById(quizId);
            if (quiz != null) {
                resultTitleLabel.setText("Quiz Results");
                
                Course course = CourseService.getCourseById(quiz.getCourseId());
                if (course != null) {
                    courseNameLabel.setText(course.getTitle());
                    this.teacher = AuthService.getUserById(course.getTeacherId());
                }
            }
        }
    }
    
    /**
     * Updates the return button text based on the user's role.
     */
    private void updateReturnButtonForRole() {
        User currentUser = AuthLoginController.getCurrentUser();
        if (currentUser != null && currentUser.getRole().equals("teacher") && returnButton != null) {
            returnButton.setText("Back to Results List");
        }
    }
}