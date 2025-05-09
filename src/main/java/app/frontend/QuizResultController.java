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
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TabPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Tab;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class QuizResultController implements Initializable {

    @FXML private Label resultTitleLabel;
    @FXML private Label courseNameLabel;
    @FXML private Label percentageLabel;
    @FXML private Label scoreTextLabel;
    @FXML private Label correctAnswersLabel;
    @FXML private Label incorrectAnswersLabel;
    @FXML private Label totalQuestionsLabel;
    @FXML private Label performancePercentLabel;
    @FXML private Label performanceMessage;
    @FXML private ProgressBar performanceBar;
    @FXML private VBox questionsReviewContainer;
    @FXML private Button retakeButton;
    @FXML private Button returnButton;
    @FXML private TabPane tabPane;
    @FXML private Button retakeQuizButton;

    private List<Question> questions;
    private List<Integer> userAnswers;
    private List<Answer> correctAnswers;
    private User teacher;
    private int quizId;
    
    // Store user answers text for display
    private Map<Integer, String> userAnswerTexts = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up button actions
        retakeButton.setOnAction(e -> handleRetakeQuiz());
        returnButton.setOnAction(e -> handleReturn());
        
        // Always disable retake button since we don't want students to retake quizzes
        disableRetakeQuiz();
        
        // Also hide the main retake button if it exists
        if (retakeButton != null) {
            retakeButton.setVisible(false);
            retakeButton.setManaged(false);
        }
    }

    public void setResultData(int correctAnswersCount, int totalQuestions, int correctCount, int incorrectCount, 
                            String timeTaken, List<Question> questions, List<Integer> userAnswers, 
                            List<Answer> correctAnswers) {
        // Store the data
        this.questions = questions;
        this.userAnswers = userAnswers;
        this.correctAnswers = correctAnswers;
        
        // Prepare user answer texts
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            Integer userAnswerId = userAnswers.get(i);
            
            if (userAnswerId != null && userAnswerId != -1) {
                // Get all answers for this question to find the selected one
                List<Answer> answers = AnswerService.getAnswersByQuestionId(question.getId());
                for (Answer answer : answers) {
                    if (answer.getId() == userAnswerId) {
                        userAnswerTexts.put(question.getId(), answer.getAnswerText());
                        break;
                    }
                }
            }
        }

        // Store quiz ID if questions exist
        if (questions != null && !questions.isEmpty()) {
            this.quizId = questions.get(0).getQuizId();
            
            // Get quiz info
            Quiz quiz = QuizService.getQuizById(quizId);
            if (quiz != null) {
                // Set quiz title in the header
                resultTitleLabel.setText("Quiz Results");
                
                // Get course info
                Course course = CourseService.getCourseById(quiz.getCourseId());
                if (course != null) {
                    // Set course name
                    courseNameLabel.setText(course.getTitle());
                    
                    // Get teacher
                    this.teacher = AuthService.getUserById(course.getTeacherId());
                }
            }
        }

        // Calculate and display percentage
        int percentage = (correctAnswersCount * 100) / totalQuestions;
        percentageLabel.setText(percentage + "%");
        scoreTextLabel.setText(correctAnswersCount + "/" + totalQuestions);
        
        // Update statistics for overview tab
        correctAnswersLabel.setText(String.valueOf(correctCount));
        incorrectAnswersLabel.setText(String.valueOf(incorrectCount));
        totalQuestionsLabel.setText(String.valueOf(totalQuestions));
        
        // Set performance bar and text
        performancePercentLabel.setText(percentage + "%");
        performanceBar.setProgress((double) percentage / 100);
        
        // Set progress bar color based on score
        String barColor;
        String message;
        if (percentage >= 70) {
            // Green for good performance
            barColor = "#10b981"; 
            message = "Great job!";
        } else if (percentage >= 40) {
            // Yellow/Orange for medium performance
            barColor = "#f59e0b"; 
            message = "Good effort, keep practicing!";
        } else {
            // Red for needs improvement
            barColor = "#f43f5e"; 
            message = "Keep studying, you'll improve!";
        }
        
        performanceBar.setStyle("-fx-accent: " + barColor + ";");
        performanceMessage.setText(message);

        // Populate questions review
        populateQuestionsReview();
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    private void populateQuestionsReview() {
        questionsReviewContainer.getChildren().clear();
        
        // Set constraints on container
        questionsReviewContainer.setMaxWidth(700);
        questionsReviewContainer.setPrefWidth(700);

        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            Integer userAnswerId = userAnswers.get(i);
            Answer correctAnswer = correctAnswers.get(i);

            // Determine if answer is correct
            boolean answered = (userAnswerId != null && userAnswerId != -1);
            boolean correct = answered && (userAnswerId == correctAnswer.getId());
            
            // Create question container
            VBox questionBox = new VBox(10);
            questionBox.getStyleClass().add("question-item");
            // Add appropriate style class based on correctness
            if (correct) {
                questionBox.getStyleClass().add("correct");
            } else {
                questionBox.getStyleClass().add("incorrect");
            }

            // Question header with number and badge
            HBox questionHeader = new HBox();
            questionHeader.setAlignment(Pos.CENTER_LEFT);
            questionHeader.setSpacing(10);
            
            // Question text with number
            Label questionText = new Label((i + 1) + ". " + question.getQuestionText());
            questionText.getStyleClass().add("question-text");
            questionText.setWrapText(true);
            questionText.setMinHeight(Label.USE_PREF_SIZE);
            HBox.setHgrow(questionText, Priority.ALWAYS);
            
            // Add status badge
            Label statusBadge = new Label(correct ? "Correct" : "Incorrect");
            statusBadge.getStyleClass().addAll("status-badge", correct ? "correct-badge" : "incorrect-badge");
            
            questionHeader.getChildren().addAll(questionText, statusBadge);
            questionBox.getChildren().add(questionHeader);
            
            if (answered) {
                // Show user's answer if they answered
                HBox userAnswerBox = new HBox(5);
                userAnswerBox.setAlignment(Pos.CENTER_LEFT);
                userAnswerBox.getStyleClass().add("answer-box");
                
                Label userAnswerLabel = new Label("Your answer:");
                userAnswerLabel.getStyleClass().add("answer-label");
                userAnswerLabel.setMinWidth(100);
                
                // Get answer text from the map we populated earlier
                String answerText = userAnswerTexts.getOrDefault(question.getId(), "Unknown answer");
                
                Label userAnswerText = new Label(answerText);
                userAnswerText.setWrapText(true);
                userAnswerText.getStyleClass().add(correct ? "correct-answer" : "incorrect-answer");
                userAnswerText.setMinHeight(Label.USE_PREF_SIZE);
                
                HBox.setHgrow(userAnswerText, Priority.ALWAYS);
                
                userAnswerBox.getChildren().addAll(userAnswerLabel, userAnswerText);
                questionBox.getChildren().add(userAnswerBox);
            } else {
                // Show that the question was not answered
                HBox noAnswerBox = new HBox();
                noAnswerBox.getStyleClass().add("answer-box");
                
                Label noAnswerLabel = new Label("You did not answer this question");
                noAnswerLabel.getStyleClass().add("no-answer-text");
                
                noAnswerBox.getChildren().add(noAnswerLabel);
                questionBox.getChildren().add(noAnswerBox);
            }

            // Show correct answer if user was wrong or didn't answer
            if (!correct) {
                HBox correctAnswerBox = new HBox(5);
                correctAnswerBox.setAlignment(Pos.CENTER_LEFT);
                correctAnswerBox.getStyleClass().add("answer-box");
                
                Label correctAnswerLabel = new Label("Correct answer:");
                correctAnswerLabel.getStyleClass().add("answer-label");
                correctAnswerLabel.setMinWidth(100);
                
                Label correctAnswerText = new Label(correctAnswer.getAnswerText());
                correctAnswerText.setWrapText(true);
                correctAnswerText.getStyleClass().add("correct-answer");
                correctAnswerText.setMinHeight(Label.USE_PREF_SIZE);
                
                HBox.setHgrow(correctAnswerText, Priority.ALWAYS);
                
                correctAnswerBox.getChildren().addAll(correctAnswerLabel, correctAnswerText);
                questionBox.getChildren().add(correctAnswerBox);
            }
            
            // Add padding to ensure consistent spacing
            questionBox.setPadding(new Insets(12));
            
            questionsReviewContainer.getChildren().add(questionBox);
        }
    }

    private void handleRetakeQuiz() {
        if (quizId > 0) {
            try {
                // Load the quiz viewer
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz-viewer.fxml"));
                Parent quizViewer = loader.load();
                
                // Set the quiz to view
                QuizViewerController controller = loader.getController();
                controller.setQuiz(QuizService.getQuizById(quizId));
                
                // Get the content area and update it
                StackPane contentArea = (StackPane) returnButton.getScene().lookup("#contentArea");
                if (contentArea != null) {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(quizViewer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleReturn() {
        try {
            if (teacher != null) {
                // Navigate back to teacher quizzes view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teacher-quizzes.fxml"));
                Parent teacherQuizzes = loader.load();
                
                // Set the teacher in the controller
                TeacherQuizzesController controller = loader.getController();
                controller.setTeacher(teacher);
                
                // Try different methods to find the content area
                StackPane contentArea = null;
                
                // Method 1: Try through the scene directly if available
                if (returnButton != null && returnButton.getScene() != null) {
                    contentArea = (StackPane) returnButton.getScene().lookup("#contentArea");
                }
                
                // Method 2: Try through the parent hierarchy
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
                
                // If content area was found, update the UI
                if (contentArea != null) {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(teacherQuizzes);
                    return;
                }
            }
            
            // Fall back to regular quizzes view if teacher is null or content area not found
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quizzes.fxml"));
            Parent quizzesView = loader.load();
            
            // Try different methods to find the content area
            StackPane contentArea = null;
            
            // Method 1: Try through the scene directly
            if (returnButton != null && returnButton.getScene() != null) {
                contentArea = (StackPane) returnButton.getScene().lookup("#contentArea");
            }
            
            // Method 2: Try through the parent hierarchy
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
            
            // Update UI if content area was found
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(quizzesView);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Disables the retake quiz button when a student has already taken the quiz
     */
    public void disableRetakeQuiz() {
        if (retakeQuizButton != null) {
            retakeQuizButton.setVisible(false);
            retakeQuizButton.setManaged(false);
        }
    }
} 