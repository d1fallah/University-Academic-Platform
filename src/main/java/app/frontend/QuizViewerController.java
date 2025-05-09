package app.frontend;

import app.backend.models.Answer;
import app.backend.models.Course;
import app.backend.models.Question;
import app.backend.models.Quiz;
import app.backend.models.QuizResult;
import app.backend.models.StudentAnswer;
import app.backend.models.User;
import app.backend.services.AnswerService;
import app.backend.services.AuthService;
import app.backend.services.CourseService;
import app.backend.services.QuestionService;
import app.backend.services.QuizResultService;
import app.backend.services.StudentAnswerService;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class QuizViewerController implements Initializable {

    @FXML private BorderPane quizViewerContainer;
    @FXML private Label quizTitleLabel;
    @FXML private Label courseNameLabel;
    @FXML private VBox questionsContainer;
    @FXML private Label loadingQuestionsLabel;
    @FXML private Button returnButton;
    @FXML private Button nextButton;
    @FXML private Label progressLabel;
    @FXML private ToggleGroup answerGroup;
    @FXML private VBox answersVBox;
    @FXML private Label questionTextLabel;
    
    private Quiz currentQuiz;
    private List<Question> questions;
    private Map<Integer, Integer> selectedAnswers;
    private int currentQuestionIndex = 0;
    private User teacher;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize collections
        questions = new ArrayList<>();
        selectedAnswers = new HashMap<>();
        
        // Set action for the return button
        returnButton.setOnAction(e -> handleReturn());
        
        // Set action for the next button
        nextButton.setOnAction(e -> handleNextQuestion());
    }
    
    /**
     * Sets the quiz to be displayed and loads its questions
     */
    public void setQuiz(Quiz quiz) {
        this.currentQuiz = quiz;
        
        // Set quiz title and course name
        quizTitleLabel.setText(quiz.getTitle());
        String courseName = CourseService.getCourseById(quiz.getCourseId()).getTitle();
        courseNameLabel.setText(courseName);
        
        // Check if the student has already taken this quiz
        if (LoginController.getCurrentUser() != null && 
            LoginController.getCurrentUser().getRole().equals("student")) {
            int studentId = LoginController.getCurrentUser().getId();
            int quizId = quiz.getId();
            
            if (QuizResultService.hasStudentTakenQuiz(studentId, quizId)) {
                showQuizResult(studentId, quizId);
                return;
            }
        }
        
        // Continue with loading quiz questions for a new attempt
        loadQuestions();
    }
    
    /**
     * Loads all questions for the current quiz
     */
    private void loadQuestions() {
        if (currentQuiz == null) return;
        
        // Clear any previous content
        questions.clear();
        selectedAnswers.clear();
        
        // Fetch questions from the database
        List<Question> quizQuestions = QuestionService.getQuestionsByQuizId(currentQuiz.getId());
        
        if (quizQuestions != null && !quizQuestions.isEmpty()) {
            questions.addAll(quizQuestions);
            
            // Remove the loading label
            loadingQuestionsLabel.setVisible(false);
            answersVBox.setVisible(true);
            
            // Show the first question
            showCurrentQuestion();
            
            // Initialize progress label
            updateProgressLabel();
            
        } else {
            // No questions found
            loadingQuestionsLabel.setText("No questions available for this quiz.");
            answersVBox.setVisible(false);
            nextButton.setDisable(true);
        }
    }
    
    /**
     * Updates the progress label with current question number and total questions
     */
    private void updateProgressLabel() {
        if (questions.isEmpty()) {
            progressLabel.setText("No questions");
            return;
        }
        
        progressLabel.setText(String.format("Question %d/%d", currentQuestionIndex + 1, questions.size()));
        
        // Update the next button text based on whether this is the last question
        if (currentQuestionIndex >= questions.size() - 1) {
            nextButton.setText("Submit Quiz");
        } else {
            nextButton.setText("Next Question");
        }
    }
    
    /**
     * Shows the current question based on the currentQuestionIndex
     */
    private void showCurrentQuestion() {
        if (questions.isEmpty() || currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) {
            return;
        }
        
        // First, fade out the current content
        fadeOutContent(() -> {
            // Get the current question
            Question currentQuestion = questions.get(currentQuestionIndex);
            
            // Set the question text
            questionTextLabel.setText(currentQuestion.getQuestionText());
            
            // Clear previous answer options
            answersVBox.getChildren().clear();
            
            // Reset the toggle group
            if (answerGroup != null) {
                answerGroup.selectToggle(null);
            }
            
            // Create new answer options
            createAnswerOptions(currentQuestion);
            
            // Fade in the new content
            fadeInContent();
        });
    }
    
    /**
     * Creates radio button options for the current question's answers
     */
    private void createAnswerOptions(Question question) {
        // Get answers for this question
        List<Answer> answers = AnswerService.getAnswersByQuestionId(question.getId());
        
        if (answers == null || answers.isEmpty()) {
            Label noAnswersLabel = new Label("No answers available for this question.");
            noAnswersLabel.getStyleClass().add("dialog-helper-text");
            answersVBox.getChildren().add(noAnswersLabel);
            return;
        }
        
        // Create radio buttons for each answer
        for (Answer answer : answers) {
            // Create HBox container for radio + answer text
            HBox answerContainer = new HBox();
            answerContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            answerContainer.setSpacing(15); // Increase space between radio and text
            answerContainer.setPadding(new javafx.geometry.Insets(8, 0, 8, 0));
            answerContainer.setMaxWidth(Double.MAX_VALUE);
            
            // Create radio button with constrained width
            RadioButton radio = new RadioButton();
            radio.setToggleGroup(answerGroup);
            radio.setUserData(answer.getId());
            radio.getStyleClass().add("answer-radio");
            radio.setMinWidth(20);
            radio.setPrefWidth(20);
            radio.setMaxWidth(20);
            
            // Check if this answer was previously selected
            Integer selectedAnswerId = selectedAnswers.get(question.getId());
            if (selectedAnswerId != null && selectedAnswerId.equals(answer.getId())) {
                radio.setSelected(true);
            }
            
            // Add listener to track selected answers
            radio.selectedProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue) {
                    selectedAnswers.put(question.getId(), (Integer) radio.getUserData());
                }
            });
            
            // Create answer text label with proper styling
            Label answerText = new Label(answer.getAnswerText());
            answerText.setWrapText(true);
            answerText.getStyleClass().add("dialog-input-field");
            HBox.setHgrow(answerText, javafx.scene.layout.Priority.ALWAYS);
            answerText.setPrefWidth(600);
            answerText.setMaxWidth(Double.MAX_VALUE);
            
            // Make the label clickable to select the radio button
            answerText.setOnMouseClicked(e -> {
                radio.setSelected(true);
            });
            
            // Add to container (radio button first, then the text)
            answerContainer.getChildren().clear();
            answerContainer.getChildren().addAll(radio, answerText);
            
            // Add to answers VBox
            answersVBox.getChildren().add(answerContainer);
        }
    }
    
    /**
     * Fades out the question content and executes an action afterward
     */
    private void fadeOutContent(Runnable afterFadeOut) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), questionsContainer);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.5);
        fadeOut.setOnFinished(e -> {
            afterFadeOut.run();
        });
        fadeOut.play();
    }
    
    /**
     * Fades in the question content
     */
    private void fadeInContent() {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), questionsContainer);
        fadeIn.setFromValue(0.5);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
    
    /**
     * Handles the next question or submit quiz button action
     */
    private void handleNextQuestion() {
        Question currentQuestion = questions.get(currentQuestionIndex);
        
        // Check if current question has been answered
        if (!selectedAnswers.containsKey(currentQuestion.getId())) {
            showAlert(Alert.AlertType.WARNING, "No Answer Selected", "Please select an answer before proceeding.");
            return;
        }
        
        // Check if this is the last question
        if (currentQuestionIndex >= questions.size() - 1) {
            handleSubmitQuiz();
        } else {
            // Move to the next question
            currentQuestionIndex++;
            showCurrentQuestion();
            updateProgressLabel();
        }
    }
    
    /**
     * Handles the return button action
     */
    private void handleReturn() {
        try {
            if (teacher != null) {
                // Load the teacher quizzes view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teacher-quizzes.fxml"));
                Parent quizzesView = loader.load();
                
                // Get the controller and set the teacher
                TeacherQuizzesController controller = loader.getController();
                controller.setTeacher(teacher);
                
                // Get the content area from the scene and set the quizzes view
                StackPane contentArea = (StackPane) quizViewerContainer.getScene().lookup("#contentArea");
                if (contentArea != null) {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(quizzesView);
                }
            } else {
                // Fallback to main quizzes view if teacher is not available
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quizzes.fxml"));
                Parent quizzesView = loader.load();
                
                // Get the content area from the scene and set the quizzes view
                StackPane contentArea = (StackPane) quizViewerContainer.getScene().lookup("#contentArea");
                if (contentArea != null) {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(quizzesView);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, 
                     "Navigation Error", 
                     "Failed to return to quizzes: " + e.getMessage());
        }
    }
    
    /**
     * Handles the submit quiz action
     */
    private void handleSubmitQuiz() {
        // Count unanswered questions
        int unanswered = 0;
        for (Question question : questions) {
            if (!selectedAnswers.containsKey(question.getId())) {
                unanswered++;
            }
        }
        
        // Warn if there are unanswered questions
        if (unanswered > 0) {
            String message = String.format("You have %d unanswered question%s. Do you want to submit anyway?", 
                                          unanswered, unanswered > 1 ? "s" : "");
            
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Submission");
            confirmation.setHeaderText(null);
            confirmation.setContentText(message);
            
            if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
        }
        
        // Calculate the score
        int correctAnswers = 0;
        int totalQuestions = questions.size();
        int incorrectAnswers = 0;
        List<Integer> userAnswerList = new java.util.ArrayList<>();
        List<Answer> correctAnswerList = new java.util.ArrayList<>();
        List<StudentAnswer> studentAnswersList = new ArrayList<>();
        
        // Initialize quizResultId for student answers (will be set after the quiz result is saved)
        int quizResultId = -1;
        
        for (Question question : questions) {
            Integer selectedAnswerId = selectedAnswers.get(question.getId());
            userAnswerList.add(selectedAnswerId != null ? selectedAnswerId : -1);
            
            List<Answer> answers = AnswerService.getAnswersByQuestionId(question.getId());
            Answer correctAnswer = null;
            for (Answer answer : answers) {
                if (answer.isCorrect()) {
                    correctAnswer = answer;
                }
            }
            correctAnswerList.add(correctAnswer);
            
            boolean isCorrect = false;
            if (selectedAnswerId != null && correctAnswer != null && selectedAnswerId == correctAnswer.getId()) {
                correctAnswers++;
                isCorrect = true;
            } else {
                incorrectAnswers++;
            }
            
            // Create StudentAnswer object for later saving
            StudentAnswer studentAnswer = new StudentAnswer(
                quizResultId,
                question.getId(),
                selectedAnswerId,
                isCorrect
            );
            studentAnswersList.add(studentAnswer);
        }
        
        // Calculate percentage score
        int scorePercentage = totalQuestions > 0 ? (correctAnswers * 100 / totalQuestions) : 0;
        
        // Save the result if the user is a student
        boolean resultSaved = false;
        if (LoginController.getCurrentUser() != null && 
            LoginController.getCurrentUser().getRole().equals("student")) {
            QuizResult result = new QuizResult(
                currentQuiz.getId(),
                LoginController.getCurrentUser().getId(),
                scorePercentage
            );
            resultSaved = QuizResultService.submitQuizResult(result);
            
            // If the result was saved, get the ID and update student answers
            if (resultSaved) {
                QuizResult savedResult = QuizResultService.getQuizResult(
                    LoginController.getCurrentUser().getId(),
                    currentQuiz.getId()
                );
                
                if (savedResult != null) {
                    // Update quiz result ID in all student answers
                    for (StudentAnswer answer : studentAnswersList) {
                        answer.setQuizResultId(savedResult.getId());
                    }
                    
                    // Save all student answers
                    StudentAnswerService.saveStudentAnswers(studentAnswersList);
                }
            }
        }
        
        // Show the new result page
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz-result.fxml"));
            Parent resultView = loader.load();
            QuizResultController controller = loader.getController();
            controller.setResultData(correctAnswers, totalQuestions, correctAnswers, incorrectAnswers, null, questions, userAnswerList, correctAnswerList);
            controller.disableRetakeQuiz();
            
            // Replace the current view with the result view without trying to add a return button
            // The quiz-result.fxml already has a return button in its action buttons section
            StackPane contentArea = (StackPane) quizViewerContainer.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(resultView);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Result Error", "Failed to load result page: " + e.getMessage());
        }
    }
    
    /**
     * Sets the teacher reference for proper navigation
     */
    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    private void showQuizResult(int studentId, int quizId) {
        try {
            // Get the quiz result data
            QuizResult result = QuizResultService.getQuizResult(studentId, quizId);
            
            if (result == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not retrieve quiz result.");
                return;
            }
            
            // Load questions for this quiz if not already loaded
            if (questions == null || questions.isEmpty()) {
                questions = QuestionService.getQuestionsByQuizId(quizId);
                if (questions == null) {
                    questions = new ArrayList<>();
                }
            }
            
            // Get the student answers
            List<StudentAnswer> studentAnswers = StudentAnswerService.getStudentAnswers(result.getId());
            
            // Convert to lists needed by the quiz result controller
            List<Integer> userAnswerList = new ArrayList<>();
            List<Answer> correctAnswerList = new ArrayList<>();
            
            for (Question question : questions) {
                // Find the student's answer for this question
                Integer selectedAnswerId = null;
                for (StudentAnswer ans : studentAnswers) {
                    if (ans.getQuestionId() == question.getId()) {
                        selectedAnswerId = ans.getSelectedAnswerId();
                        break;
                    }
                }
                
                userAnswerList.add(selectedAnswerId != null ? selectedAnswerId : -1);
                
                // Find the correct answer
                List<Answer> answers = AnswerService.getAnswersByQuestionId(question.getId());
                Answer correctAnswer = null;
                for (Answer answer : answers) {
                    if (answer.isCorrect()) {
                        correctAnswer = answer;
                        break;
                    }
                }
                correctAnswerList.add(correctAnswer);
            }
            
            // Show the quiz result view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz-result.fxml"));
            Parent resultView = loader.load();
            QuizResultController controller = loader.getController();
            
            // Calculate metrics for display
            int totalQuestions = questions.size();
            int correctAnswers = 0;
            int incorrectAnswers = 0;
            
            for (StudentAnswer ans : studentAnswers) {
                if (ans.isCorrect()) {
                    correctAnswers++;
                } else {
                    incorrectAnswers++;
                }
            }
            
            controller.setResultData(correctAnswers, totalQuestions, correctAnswers, incorrectAnswers, null, questions, userAnswerList, correctAnswerList);
            controller.disableRetakeQuiz(); // Disable retaking the quiz
            if (teacher != null) {
                controller.setTeacher(teacher);
            }
            
            // Replace current view with result view
            // Try different methods to find the content area
            StackPane contentArea = null;
            
            // Method 1: Try through the scene directly if available
            if (quizViewerContainer != null && quizViewerContainer.getScene() != null) {
                contentArea = (StackPane) quizViewerContainer.getScene().lookup("#contentArea");
            }
            
            // Method 2: Try through the parent hierarchy
            if (contentArea == null && quizViewerContainer != null) {
                Parent parent = quizViewerContainer.getParent();
                while (parent != null && contentArea == null) {
                    if (parent instanceof StackPane && parent.getId() != null && parent.getId().equals("contentArea")) {
                        contentArea = (StackPane) parent;
                    }
                    parent = parent.getParent();
                }
            }
            
            // If content area was found, update the UI
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(resultView);
                return; // Exit the method after successful navigation
            }
            
            // If we got here, we couldn't find the content area
            // Use the fallback alert
            showAlert(Alert.AlertType.INFORMATION, "Quiz Result", 
                "Score: " + correctAnswers + "/" + totalQuestions + 
                "\nPercentage: " + (totalQuestions > 0 ? (correctAnswers * 100 / totalQuestions) : 0) + "%");
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load quiz result: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to show alerts
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}