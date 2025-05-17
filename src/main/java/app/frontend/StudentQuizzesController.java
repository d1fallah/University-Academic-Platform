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

/**
 * Controller class responsible for displaying quizzes created by a specific teacher.
 * This class handles quiz browsing, searching, and navigation to take quizzes or view results.
 *
 * @author Sellami Mohamed Odai
 */
public class StudentQuizzesController implements Initializable {

    @FXML private FlowPane quizCardsContainer;
    @FXML private Label teacherNameLabel;
    @FXML private TextField searchField;    
    @FXML private ImageView teacherProfileImage;
    
    /** Currently logged-in user */
    private User currentUser;
    
    /** Teacher whose quizzes are being displayed */
    private User teacher;
    
    /** List of quizzes created by the teacher */
    private List<Quiz> teacherQuizzes = new ArrayList<>();
    
    /**
     * Initializes the controller class and sets up event listeners.
     *
     * @param location The location used to resolve relative paths
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = AuthLoginController.getCurrentUser();
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> 
            filterQuizzes(newValue)
        );
    }
    
    /**
     * Sets the teacher and loads their quizzes.
     * Updates UI elements with teacher's information and loads their created quizzes.
     *
     * @param teacher The teacher whose quizzes should be displayed
     */
    public void setTeacher(User teacher) {
        this.teacher = teacher;

        teacherNameLabel.setText("Prof. " + teacher.getName());
        teacherNameLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");
        
        try {
            Image profileImg = new Image(getClass().getResourceAsStream("/images/profilep.png"));
            teacherProfileImage.setImage(profileImg);
        } catch (Exception e) {
            System.out.println("Failed to load teacher profile image");
        }
        
        loadTeacherQuizzes();
    }
    
    /**
     * Retrieves quizzes created by the current teacher and displays them.
     */
    private void loadTeacherQuizzes() {
        teacherQuizzes = QuizService.getQuizzesByTeacherId(teacher.getId());
        displayQuizzes(teacherQuizzes);
    }
    
    /**
     * Displays the provided list of quizzes as cards in the UI.
     * Shows a message when no quizzes are available.
     *
     * @param quizzes The list of quizzes to display
     */
    private void displayQuizzes(List<Quiz> quizzes) {
        quizCardsContainer.getChildren().clear();
        
        if (quizzes.isEmpty()) {
            Label noQuizzesLabel = new Label("No quizzes available from this teacher yet.");
            noQuizzesLabel.getStyleClass().add("no-courses-message");
            noQuizzesLabel.setPrefWidth(quizCardsContainer.getPrefWidth());
            noQuizzesLabel.setPrefHeight(200);
            noQuizzesLabel.setAlignment(Pos.CENTER);
            noQuizzesLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
            
            quizCardsContainer.getChildren().add(noQuizzesLabel);
        } else {
            quizzes.forEach(quiz -> 
                quizCardsContainer.getChildren().add(createQuizCard(quiz))
            );
        }
    }
    
    /**
     * Filters quizzes by title based on search text and displays matching results.
     *
     * @param searchText The text to search for in quiz titles
     */
    private void filterQuizzes(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            displayQuizzes(teacherQuizzes);
        } else {
            String searchLower = searchText.toLowerCase();
            List<Quiz> filteredQuizzes = teacherQuizzes.stream()
                .filter(quiz -> quiz.getTitle().toLowerCase().contains(searchLower))
                .collect(Collectors.toList());
            
            displayQuizzes(filteredQuizzes);
        }
    }
    
    /**
     * Handles the search action when Enter is pressed in the search field.
     *
     * @param event The action event triggered by pressing Enter
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        filterQuizzes(searchField.getText());
    }
    
    
    /**
     * Creates a visual card representation for a quiz.
     * The card displays quiz information, instructor name, and a button to take or view results.
     *
     * @param quiz The quiz to create a card for
     * @return A styled StackPane containing the quiz card UI
     */
    private StackPane createQuizCard(Quiz quiz) {
        StackPane cardPane = new StackPane();
        cardPane.getStyleClass().add("course-card");
        cardPane.setPrefWidth(480);
        cardPane.setPrefHeight(230);

        ImageView cardBackground = createCardBackground();

        VBox cardContent = new VBox();
        cardContent.getStyleClass().add("card-content");
        cardContent.setSpacing(20);
        cardContent.setPadding(new Insets(20));
        cardContent.setPrefWidth(480);
        cardContent.setPrefHeight(230);

        HBox headerBox = createHeaderBox(quiz);
        
        Label descriptionLabel = new Label(quiz.getDescription());
        descriptionLabel.getStyleClass().add("card-description");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxHeight(60);

        Region spacer = new Region();
        spacer.setPrefHeight(20);
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox footerBox = createFooterBox(quiz);

        cardContent.getChildren().addAll(headerBox, descriptionLabel, spacer, footerBox);
        cardPane.getChildren().addAll(cardBackground, cardContent);
        cardPane.setOnMouseClicked(e -> handleViewQuiz(quiz));

        return cardPane;
    }
    
    /**
     * Creates the card background with appropriate styling.
     * 
     * @return The ImageView for the card background
     */
    private ImageView createCardBackground() {
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
        return cardBackground;
    }
    
    /**
     * Creates the header section of the quiz card.
     * 
     * @param quiz The quiz to create a header for
     * @return HBox containing the header elements
     */
    private HBox createHeaderBox(Quiz quiz) {
        HBox headerBox = new HBox();
        headerBox.getStyleClass().add("card-header");
        headerBox.setAlignment(Pos.TOP_LEFT);
        headerBox.setPrefWidth(480);
        headerBox.setSpacing(20);

        VBox titleContainer = new VBox();
        titleContainer.setAlignment(Pos.TOP_LEFT);
        titleContainer.setPrefWidth(390);
        HBox.setHgrow(titleContainer, Priority.ALWAYS);

        Label titleLabel = new Label(quiz.getTitle());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);
        titleLabel.setMinHeight(Region.USE_PREF_SIZE);
        titleContainer.getChildren().add(titleLabel);

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
        return headerBox;
    }
    
    /**
     * Creates the footer section of the quiz card with instructor info and button.
     * 
     * @param quiz The quiz to create a footer for
     * @return HBox containing the footer elements
     */
    private HBox createFooterBox(Quiz quiz) {
        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.CENTER_LEFT);
        footerBox.setSpacing(20);

        HBox instructorBox = createInstructorBox();
        HBox buttonBox = createButtonBox(quiz);

        footerBox.getChildren().addAll(instructorBox, buttonBox);
        return footerBox;
    }
    
    /**
     * Creates the instructor information display for the card footer.
     * 
     * @return HBox containing instructor information
     */
    private HBox createInstructorBox() {
        HBox instructorBox = new HBox();
        instructorBox.setAlignment(Pos.CENTER_LEFT);
        instructorBox.setSpacing(10);
        HBox.setHgrow(instructorBox, Priority.ALWAYS);

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
        return instructorBox;
    }
    
    /**
     * Creates the button section for the quiz card.
     * 
     * @param quiz The quiz to create a button for
     * @return HBox containing the action button
     */
    private HBox createButtonBox(Quiz quiz) {
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        String buttonText = "Take Quiz";
        
        if (currentUser != null && currentUser.getRole().equals("student")) {
            boolean quizAlreadyTaken = QuizResultService.hasStudentTakenQuiz(currentUser.getId(), quiz.getId());
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
        return buttonBox;
    }
    
    /**
     * Handles the action when a user clicks on a quiz card to view or take it.
     * If the student has already taken the quiz, shows the result view.
     * Otherwise, shows the quiz viewer for taking the quiz.
     *
     * @param quiz The quiz to view or take
     */
    private void handleViewQuiz(Quiz quiz) {
        try {
            boolean quizAlreadyTaken = false;
            
            if (currentUser != null && currentUser.getRole().equals("student")) {
                quizAlreadyTaken = QuizResultService.hasStudentTakenQuiz(currentUser.getId(), quiz.getId());
                
                if (quizAlreadyTaken) {
                    displayQuizResult(quiz);
                    return;
                }
            }
            
            displayQuizViewer(quiz);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load quiz.");
        }
    }
    
    /**
     * Displays the quiz result view for a completed quiz.
     *
     * @param quiz The quiz to display results for
     * @throws IOException If there's an error loading the view
     */
    private void displayQuizResult(Quiz quiz) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuizResult.fxml"));
        Parent resultView = loader.load();
        
        ViewQuizResultController controller = loader.getController();
        QuizResult result = QuizResultService.getQuizResult(currentUser.getId(), quiz.getId());
        
        if (result == null) {
            displayQuizViewer(quiz);
            return;
        }
        
        // Process quiz results
        List<Question> questions = QuestionService.getQuestionsByQuizId(quiz.getId());
        if (questions == null) questions = new ArrayList<>();
        
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
        
        // Set the data in the controller
        controller.setResultData(correctAnswers, questions.size(), correctAnswers, incorrectAnswers, 
            null, questions, userAnswerList, correctAnswerList);
        controller.setTeacher(teacher);
        controller.disableRetakeQuiz();
        
        // Display the result view
        StackPane contentArea = (StackPane) teacherNameLabel.getScene().lookup("#contentArea");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(resultView);
    }
  
    /**
     * Displays the quiz viewer for taking a quiz.
     *
     * @param quiz The quiz to take
     * @throws IOException If there's an error loading the FXML
     */
    private void displayQuizViewer(Quiz quiz) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuizViewer.fxml"));
        Parent quizView = loader.load();
        
        ViewQuizController controller = loader.getController();
        controller.setQuiz(quiz);
        controller.setTeacher(teacher);
        
        StackPane contentArea = (StackPane) teacherNameLabel.getScene().lookup("#contentArea");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(quizView);
    }
    
    /**
     * Handles the back button action to return to the teachers view.
     * Loads the teachers cards view and sets appropriate flags based on user role.
     * 
     * @param event The action event triggered by pressing the back button
     */
    @FXML
    private void handleBackToTeachers(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TeachersCards.fxml"));
            Parent teachersView = loader.load();
            
            TeachersCardsController controller = loader.getController();
            if (currentUser != null && currentUser.getRole().equals("teacher")) {
                controller.setExcludeCurrentTeacher(true);
                controller.setShowManageCourseButton(true);
            }
            controller.setIsQuizView(true);
            
            StackPane contentArea = (StackPane) teacherNameLabel.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(teachersView);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to go back to teachers view.");
        }
    }
    
    /**
     * Displays an alert dialog with the specified type, title, and message.
     *
     * @param alertType The type of alert to display (e.g., ERROR, INFORMATION)
     * @param title The title of the alert dialog
     * @param message The content message to display in the alert
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 