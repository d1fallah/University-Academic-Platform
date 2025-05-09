package app.frontend;

import app.backend.models.Quiz;
import app.backend.models.User;
import app.backend.models.Course;
import app.backend.models.Question;
import app.backend.models.Answer;
import app.backend.services.QuizService;
import app.backend.services.CourseService;
import app.backend.services.QuestionService;
import app.backend.services.AnswerService;
import app.backend.services.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class QuizzesController implements Initializable {

    @FXML private FlowPane quizCardsContainer;
    @FXML private TextField searchField;
    @FXML private Button manageQuizzesButton;
    
    // Question entry form components
    @FXML private StackPane addQuestionOverlay;
    @FXML private BorderPane questionDialogContainer;
    @FXML private TextArea questionTextField;
    @FXML private TextField answerField1;
    @FXML private TextField answerField2;
    @FXML private TextField answerField3;
    @FXML private TextField answerField4;
    @FXML private RadioButton radioAnswer1;
    @FXML private RadioButton radioAnswer2;
    @FXML private RadioButton radioAnswer3;
    @FXML private RadioButton radioAnswer4;
    @FXML private ToggleGroup correctAnswerGroup;
    
    private User currentUser;
    private List<Quiz> allQuizzes = new ArrayList<>();
    private Quiz currentQuiz;
    private List<Question> currentQuizQuestions = new ArrayList<>();
    private boolean isNewQuiz;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current logged in user
        currentUser = LoginController.getCurrentUser();
        
        // Show or hide the manage quizzes button based on the user role
        if (currentUser != null && currentUser.getRole().equals("teacher")) {
            manageQuizzesButton.setVisible(true);
            manageQuizzesButton.setManaged(true);
        } else {
            manageQuizzesButton.setVisible(false);
            manageQuizzesButton.setManaged(false);
        }
        
        // Load all quizzes
        loadAllQuizzes();
        
        // Set up search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterQuizzes(newValue);
        });
        
        // Make sure question overlay is initially hidden
        if (addQuestionOverlay != null) {
            addQuestionOverlay.setVisible(false);
        }
    }
    
    /**
     * Loads all quizzes and displays them
     */
    private void loadAllQuizzes() {
        // Check if we have a valid user
        if (currentUser == null) {
            System.out.println("No user logged in, cannot load quizzes");
            return;
        }

        try {
            if (currentUser.getRole().equals("teacher")) {
                // Option 1: Show all quizzes for teachers
                allQuizzes = QuizService.getAllQuizzes();
                
                // Option 2 (alternative): Show only the teacher's own quizzes
                // allQuizzes = QuizService.getQuizzesByTeacherId(currentUser.getId());
            } else if (currentUser.getRole().equals("student")) {
                // Get the student's enrollment level from their profile
                String level = currentUser.getEnrollmentLevel();
                
                // If level is not available, default to L1
                if (level == null || level.isEmpty()) {
                    level = "L1";
                }
                
                // Load quizzes matching the student's level
                allQuizzes = QuizService.getQuizzesByEnrollmentLevel(level);
            }
            
            // Display the quizzes
            displayQuizzes(allQuizzes);
            
        } catch (Exception e) {
            System.out.println("Error loading quizzes: " + e.getMessage());
            e.printStackTrace();
            
            // Initialize an empty list if something goes wrong
            allQuizzes = new ArrayList<>();
            displayQuizzes(allQuizzes);
        }
    }
    
    /**
     * Displays the provided list of quizzes as cards
     */
    private void displayQuizzes(List<Quiz> quizzes) {
        // Clear the container
        quizCardsContainer.getChildren().clear();
        
        if (quizzes.isEmpty()) {
            // Display message if no quizzes
            Label noQuizzesLabel = new Label("No quizzes available yet.");
            noQuizzesLabel.getStyleClass().add("no-quizzes-message");
            noQuizzesLabel.setPrefWidth(quizCardsContainer.getPrefWidth());
            noQuizzesLabel.setPrefHeight(200);
            noQuizzesLabel.setAlignment(Pos.CENTER);
            noQuizzesLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
            
            quizCardsContainer.getChildren().add(noQuizzesLabel);
        } else {
            // Create and add a card for each quiz
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
            // If search is empty, show all quizzes
            displayQuizzes(allQuizzes);
        } else {
            // Filter quizzes whose titles contain the search text (case insensitive)
            List<Quiz> filteredQuizzes = allQuizzes.stream()
                .filter(quiz -> quiz.getTitle().toLowerCase().contains(searchText.toLowerCase()))
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
     * Creates a visual card for a quiz
     */
    private StackPane createQuizCard(Quiz quiz) {
        // Main card container
        StackPane cardPane = new StackPane();
        cardPane.getStyleClass().add("course-card"); // Reusing course card styling
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

        // Top section with quiz title and icon
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

        // Quiz icon
        StackPane iconContainer = new StackPane();
        iconContainer.setMinWidth(50);
        iconContainer.setMaxWidth(50);
        iconContainer.setPrefHeight(50);
        iconContainer.setAlignment(Pos.TOP_CENTER);
        iconContainer.getStyleClass().add("logo-container");

        ImageView quizIcon = new ImageView();
        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/Object Scan.png"));
            quizIcon.setImage(logo);
        } catch (Exception e) {
            System.out.println("Failed to load icon for quiz: " + quiz.getTitle());
        }
        quizIcon.setFitWidth(50);
        quizIcon.setFitHeight(50);
        quizIcon.getStyleClass().add("course-icon");

        iconContainer.getChildren().add(quizIcon);

        // Add title and icon to header box
        headerBox.getChildren().addAll(titleContainer, iconContainer);

        // Quiz description
        String description = quiz.getDescription();
        if (description == null || description.isEmpty()) {
            description = "No description available";
        }
        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("card-description");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMinHeight(Region.USE_PREF_SIZE);

        // Get associated course
        Course course = CourseService.getCourseById(quiz.getCourseId());
        String courseName = course != null ? course.getTitle() : "Unknown Course";
        
        // Create course info label
        Label courseLabel = new Label("Course: " + courseName);
        courseLabel.getStyleClass().add("card-instructor");
        courseLabel.setWrapText(true);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Button section
        Button viewButton = new Button("Take Quiz");
        viewButton.getStyleClass().add("view-course-button");
        viewButton.setOnAction(e -> handleViewQuiz(quiz));

        // Add all sections to the card
        cardContent.getChildren().addAll(headerBox, descriptionLabel, courseLabel, spacer, viewButton);

        // Stack the background and content
        cardPane.getChildren().addAll(cardBackground, cardContent);

        // Make the entire card clickable
        cardPane.setOnMouseClicked(e -> handleViewQuiz(quiz));

        // Add accessibility features
        cardPane.setAccessibleText("Quiz: " + quiz.getTitle() + ", " + description);

        return cardPane;
    }
    
    /**
     * Handles the action when a user clicks on a quiz card
     */
    private void handleViewQuiz(Quiz quiz) {
        try {
            // Load the quiz viewer view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz-viewer.fxml"));
            Parent quizView = loader.load();
            
            // Get the controller and set the quiz
            QuizViewerController controller = loader.getController();
            controller.setQuiz(quiz);
            
            // Get the main layout's content area and set the quiz view
            StackPane contentArea = (StackPane) quizCardsContainer.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(quizView);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Failed to load quiz viewer: " + e.getMessage());
        }
    }

    /**
     * Handles the click on "Manage my quizzes" button
     */
    @FXML
    private void handleManageQuizzes(ActionEvent event) {
        try {
            // Load the my-quizzes view for managing quizzes
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/my-quizzes.fxml"));
            Parent myQuizzes = loader.load();
            
            // Get the main layout's content area and set the my quizzes view
            StackPane contentArea = (StackPane) quizCardsContainer.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(myQuizzes);
            
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load my-quizzes.fxml: " + e.getMessage());
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