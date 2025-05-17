package app.frontend;

import app.backend.models.Quiz;
import app.backend.models.User;
import app.backend.models.Question;
import app.backend.models.Answer;
import app.backend.models.StudentAnswer;
import app.backend.models.QuizResult;
import app.backend.services.QuizService;
import app.backend.services.QuizResultService;
import app.backend.services.QuestionService;
import app.backend.services.AnswerService;
import app.backend.services.StudentAnswerService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class StudentQuizzesController implements Initializable {

    @FXML private FlowPane quizCardsContainer;
    @FXML private Label teacherNameLabel;
    @FXML private TextField searchField;
    @FXML private ImageView teacherProfileImage;
    
    private User currentUser;
    private User teacher;
    private List<Quiz> teacherQuizzes = new ArrayList<>();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current logged in user
        currentUser = AuthLoginController.getCurrentUser();
        
        // Set up search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterQuizzes(newValue);
        });
    }
    
    /**
     * Set the teacher and load their quizzes
     */
    public void setTeacher(User teacher) {
        this.teacher = teacher;

        // Update the UI with teacher information
        teacherNameLabel.setText("Prof. " + teacher.getName());
        teacherNameLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");
        
        // Set the teacher profile image
        try {
            Image profileImg = new Image(getClass().getResourceAsStream("/images/profilep.png"));
            teacherProfileImage.setImage(profileImg);
        } catch (Exception e) {
            System.out.println("Failed to load teacher profile image");
        }
        
        // Load quizzes for this teacher
        loadTeacherQuizzes();
    }
    
    /**
     * Loads quizzes for this teacher
     */
    private void loadTeacherQuizzes() {
        // Get quizzes for this teacher
        teacherQuizzes = QuizService.getQuizzesByTeacherId(teacher.getId());
        
        // Display the quizzes
        displayQuizzes(teacherQuizzes);
    }
    
    /**
     * Displays the provided list of quizzes as cards
     */
    private void displayQuizzes(List<Quiz> quizzes) {
        // Clear the container
        quizCardsContainer.getChildren().clear();
        
        // Create and add a card for each quiz
        if (quizzes.isEmpty()) {
            // Display "No quizzes available" message
            Label noQuizzesLabel = new Label("No quizzes available from this teacher yet.");
            noQuizzesLabel.getStyleClass().add("no-courses-message");
            noQuizzesLabel.setPrefWidth(quizCardsContainer.getPrefWidth());
            noQuizzesLabel.setPrefHeight(200);
            noQuizzesLabel.setAlignment(Pos.CENTER);
            noQuizzesLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
            
            quizCardsContainer.getChildren().add(noQuizzesLabel);
        } else {
            for (Quiz quiz : quizzes) {
                quizCardsContainer.getChildren().add(createQuizCard(quiz));
            }
        }
    }
    
    /**
     * Filters quizzes by title based on search text
     */
    private void filterQuizzes(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            // If search is empty, show all teacher's quizzes
            displayQuizzes(teacherQuizzes);
        } else {
            // Filter quizzes based on title containing search text
            List<Quiz> filteredQuizzes = teacherQuizzes.stream()
                .filter(quiz -> 
                    quiz.getTitle().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
            
            displayQuizzes(filteredQuizzes);
        }
    }
    
    /**
     * Handles the search action when Enter is pressed
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        filterQuizzes(searchField.getText());
    }
    
    /**
     * Creates a visual card representation for a quiz
     */
    private StackPane createQuizCard(Quiz quiz) {
        // Main card container
        StackPane cardPane = new StackPane();
        cardPane.getStyleClass().add("course-card");
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
            System.out.println("Failed to load background image for quiz card");
        }

        // Card layout container
        VBox cardContent = new VBox();
        cardContent.getStyleClass().add("card-content");
        cardContent.setSpacing(20);
        cardContent.setPadding(new Insets(20, 20, 20, 20));
        cardContent.setPrefWidth(480);
        cardContent.setPrefHeight(230);

        // Top section with quiz title and logo
        HBox headerBox = new HBox();
        headerBox.getStyleClass().add("card-header");
        headerBox.setAlignment(Pos.TOP_LEFT);
        headerBox.setPrefWidth(480);
        headerBox.setSpacing(20);

        // Create container for title
        VBox titleContainer = new VBox();
        titleContainer.setAlignment(Pos.TOP_LEFT);
        titleContainer.setPrefWidth(390);
        HBox.setHgrow(titleContainer, Priority.ALWAYS);

        // Title on left side
        Label titleLabel = new Label(quiz.getTitle());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);
        titleLabel.setMinHeight(Region.USE_PREF_SIZE);

        titleContainer.getChildren().add(titleLabel);

        // Quiz logo/icon
        ImageView quizIcon = new ImageView();
        try {
            Image iconImg = new Image(getClass().getResourceAsStream("/images/Object Scan.png"));
            quizIcon.setImage(iconImg);
            quizIcon.setFitHeight(40);
            quizIcon.setFitWidth(40);
            quizIcon.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Failed to load quiz icon");
        }

        headerBox.getChildren().addAll(titleContainer, quizIcon);

        // Description label
        Label descriptionLabel = new Label(quiz.getDescription());
        descriptionLabel.getStyleClass().add("card-description");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxHeight(60);

        // Spacer
        Region spacer = new Region();
        spacer.setPrefHeight(20);
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Footer section
        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.CENTER_LEFT);
        footerBox.setSpacing(20);

        // Instructor box
        HBox instructorBox = new HBox();
        instructorBox.setAlignment(Pos.CENTER_LEFT);
        instructorBox.setSpacing(10);
        HBox.setHgrow(instructorBox, Priority.ALWAYS);

        // Instructor icon
        ImageView instructorIcon = new ImageView();
        try {
            Image instructorImg = new Image(getClass().getResourceAsStream("/images/Case.png"));
            instructorIcon.setImage(instructorImg);
            instructorIcon.setFitHeight(20);
            instructorIcon.setFitWidth(20);
            instructorIcon.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Failed to load instructor icon");
        }
        
        Label instructorLabel = new Label("Prof. " + teacher.getName());
        instructorLabel.getStyleClass().add("instructor-name");

        instructorBox.getChildren().addAll(instructorIcon, instructorLabel);

        // Button section
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Check if the current user is a student and has already taken this quiz
        String buttonText = "Take Quiz";
        boolean quizAlreadyTaken = false;
        
        if (currentUser != null && currentUser.getRole().equals("student")) {
            quizAlreadyTaken = QuizResultService.hasStudentTakenQuiz(currentUser.getId(), quiz.getId());
            if (quizAlreadyTaken) {
                buttonText = "View Result";
            }
        }

        Button viewButton = new Button(buttonText);
        viewButton.getStyleClass().add("view-course-button");
        viewButton.setStyle("-fx-background-color: #0095ff;");
        viewButton.setPrefWidth(110);
        viewButton.setPrefHeight(24);
        viewButton.setOnAction(e -> handleViewQuiz(quiz));

        buttonBox.getChildren().add(viewButton);

        // Add instructor and button to footer
        footerBox.getChildren().addAll(instructorBox, buttonBox);

        // Add all sections to the card
        cardContent.getChildren().addAll(headerBox, descriptionLabel, spacer, footerBox);

        // Stack the background and content
        cardPane.getChildren().addAll(cardBackground, cardContent);

        // Make the entire card clickable
        cardPane.setOnMouseClicked(e -> handleViewQuiz(quiz));

        return cardPane;
    }
    
    /**
     * Handles the action when a user clicks on a quiz card to view it
     */
    private void handleViewQuiz(Quiz quiz) {
        try {
            // Check if the current user is a student and has already taken this quiz
            boolean quizAlreadyTaken = false;
            
            if (currentUser != null && currentUser.getRole().equals("student")) {
                quizAlreadyTaken = QuizResultService.hasStudentTakenQuiz(currentUser.getId(), quiz.getId());
                
                if (quizAlreadyTaken) {
                    // Load the quiz result view directly instead of quiz viewer
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz-result.fxml"));
                    Parent resultView = loader.load();
                    
                    // Get the controller
                    ViewQuizResultController controller = loader.getController();
                    
                    // Retrieve the existing quiz result
                    QuizResult result = QuizResultService.getQuizResult(currentUser.getId(), quiz.getId());
                    
                    if (result != null) {
                        // Get questions for this quiz
                        List<Question> questions = QuestionService.getQuestionsByQuizId(quiz.getId());
                        if (questions == null) questions = new ArrayList<>();
                        
                        // Get student answers
                        List<StudentAnswer> studentAnswers = StudentAnswerService.getStudentAnswers(result.getId());
                        
                        // Prepare data for the result controller
                        List<Integer> userAnswerList = new ArrayList<>();
                        List<Answer> correctAnswerList = new ArrayList<>();
                        
                        int correctAnswers = 0;
                        int incorrectAnswers = 0;
                        
                        for (Question question : questions) {
                            // Find the student's answer for this question
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
                            
                            if (!foundCorrect) incorrectAnswers++;
                            
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
                        
                        // Set the data and teacher in the controller
                        controller.setResultData(correctAnswers, questions.size(), correctAnswers, incorrectAnswers, 
                            null, questions, userAnswerList, correctAnswerList);
                        controller.setTeacher(teacher);
                        controller.disableRetakeQuiz();
                        
                        // Get the content area and set the result view
                        StackPane contentArea = (StackPane) teacherNameLabel.getScene().lookup("#contentArea");
                        contentArea.getChildren().clear();
                        contentArea.getChildren().add(resultView);
                        
                        return;
                    }
                }
            }
            
            // If not already taken or result couldn't be found, load the quiz viewer
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz-viewer.fxml"));
            Parent quizView = loader.load();
            
            // Get the controller and set the quiz
            ViewQuizController controller = loader.getController();
            controller.setQuiz(quiz);
            controller.setTeacher(teacher);
            
            // Get the main layout's content area and set the quiz view
            StackPane contentArea = (StackPane) teacherNameLabel.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(quizView);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load quiz.");
        }
    }
    
    /**
     * Handles the back button action to return to teachers view
     */
    @FXML
    private void handleBackToTeachers(ActionEvent event) {
        try {
            // Load the teachers cards view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teachers-cards.fxml"));
            Parent teachersView = loader.load();
            
            // Get the controller and set flags
            TeachersCardsController controller = loader.getController();
            if (currentUser != null && currentUser.getRole().equals("teacher")) {
                controller.setExcludeCurrentTeacher(true);
                controller.setShowManageCourseButton(true);
            }
            controller.setIsQuizView(true);
            
            // Get the main layout's content area and set the teachers view
            StackPane contentArea = (StackPane) teacherNameLabel.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(teachersView);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to go back to teachers view.");
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