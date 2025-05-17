package app.frontend;

import app.backend.models.Answer;
import app.backend.models.Question;
import app.backend.models.Quiz;
import app.backend.models.QuizResult;
import app.backend.models.StudentAnswer;
import app.backend.models.User;
import app.backend.services.AnswerService;
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

/**
 * Controller class for quiz viewing and interaction. Handles displaying quiz questions,
 * collecting answers, and submitting quiz results.
 * 
 * @author Sellami Mohamed Odai
 */
public class ViewQuizController implements Initializable {

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
    
    /**
     * Initializes the controller and sets up event handlers.
     *
     * @param location The location used to resolve relative paths for the root object
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        questions = new ArrayList<>();
        selectedAnswers = new HashMap<>();
        
        returnButton.setOnAction(e -> handleReturn());
        nextButton.setOnAction(e -> handleNextQuestion());
    }
    
    /**
     * Sets the quiz to be displayed and loads its questions.
     * If the student has already taken the quiz, shows the result instead.
     *
     * @param quiz The quiz to display
     */
    public void setQuiz(Quiz quiz) {
        this.currentQuiz = quiz;
        
        quizTitleLabel.setText(quiz.getTitle());
        quizTitleLabel.setWrapText(true);
        quizTitleLabel.setAlignment(javafx.geometry.Pos.CENTER);
        String courseName = CourseService.getCourseById(quiz.getCourseId()).getTitle();
        courseNameLabel.setText(courseName);
        
        if (AuthLoginController.getCurrentUser() != null &&
            AuthLoginController.getCurrentUser().getRole().equals("student")) {
            int studentId = AuthLoginController.getCurrentUser().getId();
            int quizId = quiz.getId();
            
            if (QuizResultService.hasStudentTakenQuiz(studentId, quizId)) {
                showQuizResult(studentId, quizId);
                return;
            }
        }
        
        loadQuestions();
    }
    
    /**
     * Loads all questions for the current quiz and displays the first question.
     */
    private void loadQuestions() {
        if (currentQuiz == null) return;
        
        questions.clear();
        selectedAnswers.clear();
        
        List<Question> quizQuestions = QuestionService.getQuestionsByQuizId(currentQuiz.getId());
        
        if (quizQuestions != null && !quizQuestions.isEmpty()) {
            questions.addAll(quizQuestions);
            loadingQuestionsLabel.setVisible(false);
            answersVBox.setVisible(true);
            showCurrentQuestion();
            updateProgressLabel();
        } else {
            loadingQuestionsLabel.setText("No questions available for this quiz.");
            answersVBox.setVisible(false);
            nextButton.setDisable(true);
        }
    }
    
    /**
     * Updates the progress label with current question number and total questions.
     * Also updates the next button text based on question position.
     */
    private void updateProgressLabel() {
        if (questions.isEmpty()) {
            progressLabel.setText("No questions");
            return;
        }
        
        progressLabel.setText(String.format("Question %d/%d", currentQuestionIndex + 1, questions.size()));
        
        if (currentQuestionIndex >= questions.size() - 1) {
            nextButton.setText("Submit Quiz");
        } else {
            nextButton.setText("Next Question");
        }
    }
    
    /**
     * Shows the current question with a fade transition effect.
     */
    private void showCurrentQuestion() {
        if (questions.isEmpty() || currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) {
            return;
        }
        
        fadeOutContent(() -> {
            Question currentQuestion = questions.get(currentQuestionIndex);
            questionTextLabel.setText(currentQuestion.getQuestionText());
            answersVBox.getChildren().clear();
            
            if (answerGroup != null) {
                answerGroup.selectToggle(null);
            }
            
            createAnswerOptions(currentQuestion);
            fadeInContent();
        });
    }
    
    /**
     * Creates radio button options for the current question's answers.
     *
     * @param question The question for which to create answer options
     */
    private void createAnswerOptions(Question question) {
        List<Answer> answers = AnswerService.getAnswersByQuestionId(question.getId());
        
        if (answers == null || answers.isEmpty()) {
            Label noAnswersLabel = new Label("No answers available for this question.");
            noAnswersLabel.getStyleClass().add("dialog-helper-text");
            answersVBox.getChildren().add(noAnswersLabel);
            return;
        }
        
        for (Answer answer : answers) {
            HBox answerContainer = new HBox();
            answerContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            answerContainer.setSpacing(15);
            answerContainer.setPadding(new javafx.geometry.Insets(8, 0, 8, 0));
            answerContainer.setMaxWidth(Double.MAX_VALUE);
            
            RadioButton radio = new RadioButton();
            radio.setToggleGroup(answerGroup);
            radio.setUserData(answer.getId());
            radio.getStyleClass().add("answer-radio");
            radio.setMinWidth(20);
            radio.setPrefWidth(20);
            radio.setMaxWidth(20);
            
            Integer selectedAnswerId = selectedAnswers.get(question.getId());
            if (selectedAnswerId != null && selectedAnswerId.equals(answer.getId())) {
                radio.setSelected(true);
            }
            
            radio.selectedProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue) {
                    selectedAnswers.put(question.getId(), (Integer) radio.getUserData());
                }
            });
            
            Label answerText = new Label(answer.getAnswerText());
            answerText.setWrapText(true);
            answerText.getStyleClass().add("dialog-input-field");
            HBox.setHgrow(answerText, javafx.scene.layout.Priority.ALWAYS);
            answerText.setPrefWidth(600);
            answerText.setMaxWidth(Double.MAX_VALUE);
            
            answerText.setOnMouseClicked(e -> radio.setSelected(true));
            
            answerContainer.getChildren().clear();
            answerContainer.getChildren().addAll(radio, answerText);
            answersVBox.getChildren().add(answerContainer);
        }
    }
    
    /**
     * Fades out the question content and executes an action afterward.
     *
     * @param afterFadeOut Action to execute after fade out completes
     */
    private void fadeOutContent(Runnable afterFadeOut) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), questionsContainer);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.5);
        fadeOut.setOnFinished(e -> afterFadeOut.run());
        fadeOut.play();
    }
    
    /**
     * Fades in the question content.
     */
    private void fadeInContent() {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), questionsContainer);
        fadeIn.setFromValue(0.5);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
    
    /**
     * Handles the next question or submit quiz button action.
     * Validates that the current question has been answered.
     */
    private void handleNextQuestion() {
        Question currentQuestion = questions.get(currentQuestionIndex);
        
        if (!selectedAnswers.containsKey(currentQuestion.getId())) {
            showAlert(Alert.AlertType.WARNING, "No Answer Selected", "Please select an answer before proceeding.");
            return;
        }
        
        if (currentQuestionIndex >= questions.size() - 1) {
            handleSubmitQuiz();
        } else {
            currentQuestionIndex++;
            showCurrentQuestion();
            updateProgressLabel();
        }
    }
    
    /**
     * Handles the return button action to navigate back to the quizzes view.
     */
    private void handleReturn() {
        try {
            if (teacher != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StudentQuizzes.fxml"));
                Parent quizzesView = loader.load();
                
                StudentQuizzesController controller = loader.getController();
                controller.setTeacher(teacher);
                
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
     * Handles the submit quiz action.
     * Prompts for confirmation if there are unanswered questions,
     * calculates the score, saves student answers, and shows results.
     */
    private void handleSubmitQuiz() {
        int unanswered = 0;
        for (Question question : questions) {
            if (!selectedAnswers.containsKey(question.getId())) {
                unanswered++;
            }
        }
        
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
        
        int correctAnswers = 0;
        int totalQuestions = questions.size();
        int incorrectAnswers = 0;
        List<Integer> userAnswerList = new ArrayList<>();
        List<Answer> correctAnswerList = new ArrayList<>();
        List<StudentAnswer> studentAnswersList = new ArrayList<>();
        
        int quizResultId = -1;
        
        for (Question question : questions) {
            Integer selectedAnswerId = selectedAnswers.get(question.getId());
            userAnswerList.add(selectedAnswerId != null ? selectedAnswerId : -1);
            
            List<Answer> answers = AnswerService.getAnswersByQuestionId(question.getId());
            Answer correctAnswer = findCorrectAnswer(answers);
            correctAnswerList.add(correctAnswer);
            
            boolean isCorrect = false;
            if (selectedAnswerId != null && correctAnswer != null && selectedAnswerId == correctAnswer.getId()) {
                correctAnswers++;
                isCorrect = true;
            } else {
                incorrectAnswers++;
            }
            
            StudentAnswer studentAnswer = new StudentAnswer(
                quizResultId,
                question.getId(),
                selectedAnswerId,
                isCorrect
            );
            studentAnswersList.add(studentAnswer);
        }
        
        int scorePercentage = totalQuestions > 0 ? (correctAnswers * 100 / totalQuestions) : 0;
        
        if (AuthLoginController.getCurrentUser() != null &&
            AuthLoginController.getCurrentUser().getRole().equals("student")) {
            QuizResult result = new QuizResult(
                currentQuiz.getId(),
                AuthLoginController.getCurrentUser().getId(),
                scorePercentage
            );
            boolean resultSaved = QuizResultService.submitQuizResult(result);
            
            if (resultSaved) {
                QuizResult savedResult = QuizResultService.getQuizResult(
                    AuthLoginController.getCurrentUser().getId(),
                    currentQuiz.getId()
                );
                
                if (savedResult != null) {
                    for (StudentAnswer answer : studentAnswersList) {
                        answer.setQuizResultId(savedResult.getId());
                    }
                    
                    StudentAnswerService.saveStudentAnswers(studentAnswersList);
                }
            }
        }
        
        displayQuizResults(correctAnswers, totalQuestions, incorrectAnswers, questions, 
                          userAnswerList, correctAnswerList);
    }
    
    /**
     * Finds the correct answer in a list of answers.
     *
     * @param answers List of answers to search
     * @return The correct answer or null if not found
     */
    private Answer findCorrectAnswer(List<Answer> answers) {
        if (answers == null) return null;
        
        for (Answer answer : answers) {
            if (answer.isCorrect()) {
                return answer;
            }
        }
        return null;
    }
    
    /**
     * Displays the quiz results in the result view.
     *
     * @param correctAnswers Number of correct answers
     * @param totalQuestions Total number of questions
     * @param incorrectAnswers Number of incorrect answers
     * @param questions The list of quiz questions
     * @param userAnswerList List of user's selected answer IDs
     * @param correctAnswerList List of correct answers
     */
    private void displayQuizResults(int correctAnswers, int totalQuestions, int incorrectAnswers,
                                   List<Question> questions, List<Integer> userAnswerList,
                                   List<Answer> correctAnswerList) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuizResult.fxml"));
            Parent resultView = loader.load();
            ViewQuizResultController controller = loader.getController();
            
            controller.setResultData(
                correctAnswers, 
                totalQuestions,
                correctAnswers, 
                incorrectAnswers, 
                null, 
                questions, 
                userAnswerList, 
                correctAnswerList
            );
            controller.disableRetakeQuiz();
            
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
     * Sets the teacher reference for proper navigation.
     *
     * @param teacher The teacher user
     */
    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    /**
     * Shows the result of a previously taken quiz.
     *
     * @param studentId The ID of the student
     * @param quizId The ID of the quiz
     */
    private void showQuizResult(int studentId, int quizId) {
        try {
            QuizResult result = QuizResultService.getQuizResult(studentId, quizId);
            
            if (result == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not retrieve quiz result.");
                return;
            }
            
            if (questions == null || questions.isEmpty()) {
                questions = QuestionService.getQuestionsByQuizId(quizId);
                if (questions == null) {
                    questions = new ArrayList<>();
                }
            }
            
            List<StudentAnswer> studentAnswers = StudentAnswerService.getStudentAnswers(result.getId());
            
            List<Integer> userAnswerList = new ArrayList<>();
            List<Answer> correctAnswerList = new ArrayList<>();
            
            for (Question question : questions) {
                Integer selectedAnswerId = findStudentAnswerId(studentAnswers, question.getId());
                userAnswerList.add(selectedAnswerId != null ? selectedAnswerId : -1);
                
                List<Answer> answers = AnswerService.getAnswersByQuestionId(question.getId());
                Answer correctAnswer = findCorrectAnswer(answers);
                correctAnswerList.add(correctAnswer);
            }
            
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
            
            displayQuizResultView(correctAnswers, totalQuestions, incorrectAnswers,
                                 questions, userAnswerList, correctAnswerList);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load quiz result: " + e.getMessage());
        }
    }
    
    /**
     * Finds the student's selected answer ID for a question.
     *
     * @param studentAnswers List of student answers
     * @param questionId The question ID to find the answer for
     * @return The selected answer ID or null if not found
     */
    private Integer findStudentAnswerId(List<StudentAnswer> studentAnswers, int questionId) {
        for (StudentAnswer ans : studentAnswers) {
            if (ans.getQuestionId() == questionId) {
                return ans.getSelectedAnswerId();
            }
        }
        return null;
    }
    
    /**
     * Displays the quiz result view with metrics.
     *
     * @param correctAnswers Number of correct answers
     * @param totalQuestions Total number of questions
     * @param incorrectAnswers Number of incorrect answers
     * @param questions The list of quiz questions
     * @param userAnswerList List of user's selected answer IDs
     * @param correctAnswerList List of correct answers
     */
    private void displayQuizResultView(int correctAnswers, int totalQuestions, int incorrectAnswers,
                                     List<Question> questions, List<Integer> userAnswerList,
                                     List<Answer> correctAnswerList) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuizResult.fxml"));
            Parent resultView = loader.load();
            ViewQuizResultController controller = loader.getController();
            
            controller.setResultData(
                correctAnswers, 
                totalQuestions, 
                correctAnswers, 
                incorrectAnswers, 
                null, 
                questions, 
                userAnswerList, 
                correctAnswerList
            );
            
            controller.disableRetakeQuiz();
            if (teacher != null) {
                controller.setTeacher(teacher);
            }
            
            StackPane contentArea = findContentArea();
            
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(resultView);
                return;
            }
            
            showAlert(Alert.AlertType.INFORMATION, "Quiz Result", 
                "Score: " + correctAnswers + "/" + totalQuestions + 
                "\nPercentage: " + (totalQuestions > 0 ? (correctAnswers * 100 / totalQuestions) : 0) + "%");
                
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load quiz result: " + e.getMessage());
        }
    }
    
    /**
     * Finds the content area StackPane through various methods.
     *
     * @return The content area StackPane or null if not found
     */
    private StackPane findContentArea() {
        if (quizViewerContainer != null && quizViewerContainer.getScene() != null) {
            StackPane contentArea = (StackPane) quizViewerContainer.getScene().lookup("#contentArea");
            if (contentArea != null) return contentArea;
        }
        
        if (quizViewerContainer != null) {
            Parent parent = quizViewerContainer.getParent();
            while (parent != null) {
                if (parent instanceof StackPane && parent.getId() != null && 
                    parent.getId().equals("contentArea")) {
                    return (StackPane) parent;
                }
                parent = parent.getParent();
            }
        }
        
        return null;
    }
    
    /**
     * Helper method to show alerts.
     *
     * @param alertType The type of alert to show
     * @param title The alert title
     * @param message The alert message
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
