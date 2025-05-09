package app.frontend;

import app.backend.models.Quiz;
import app.backend.models.Course;
import app.backend.models.User;
import app.backend.models.Question;
import app.backend.models.Answer;
import app.backend.services.QuizService;
import app.backend.services.CourseService;
import app.backend.services.QuestionService;
import app.backend.services.AnswerService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.layout.*;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class MyQuizzesController implements Initializable {

    @FXML private FlowPane quizCardsContainer;
    @FXML private TextField searchField;
    @FXML private Button addQuizButton;
    
    // Dialog components
    @FXML private StackPane addQuizOverlay;
    @FXML private StackPane addQuestionOverlay;
    @FXML private BorderPane dialogContainer;
    @FXML private ComboBox<String> courseComboBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    // Question form components
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
    
    // Edit Quiz Dialog components
    @FXML private StackPane editQuizOverlay;
    @FXML private BorderPane editQuizContainer;
    @FXML private Label editQuizTitleLabel;
    @FXML private Label editCourseNameLabel;
    @FXML private Label editProgressLabel;
    @FXML private TextArea editQuestionTextArea;
    @FXML private TextField editAnswerField1;
    @FXML private TextField editAnswerField2;
    @FXML private TextField editAnswerField3;
    @FXML private TextField editAnswerField4;
    @FXML private RadioButton editRadioAnswer1;
    @FXML private RadioButton editRadioAnswer2;
    @FXML private RadioButton editRadioAnswer3;
    @FXML private RadioButton editRadioAnswer4;
    @FXML private ToggleGroup editCorrectAnswerGroup;
    @FXML private Button deleteQuestionButton;
    @FXML private Button prevQuestionButton;
    @FXML private Button nextQuestionButton;
    @FXML private Button cancelEditButton;
    @FXML private Button saveEditButton;
    
    private User currentUser;
    private ObservableList<Quiz> quizzesList = FXCollections.observableArrayList();
    private ObservableList<Course> coursesList = FXCollections.observableArrayList();
    private ObservableList<String> courseNamesForComboBox = FXCollections.observableArrayList();
    private Quiz currentQuiz;
    private List<Question> currentQuizQuestions = new ArrayList<>();
    private boolean isNewQuiz;

    private int selectedCourseId = -1;

    // Variables for quiz editing
    private int currentQuestionIndex = 0;
    private List<Question> editingQuestions = new ArrayList<>();
    private Map<Integer, List<Answer>> questionAnswers = new HashMap<>();
    private Quiz quizBeingEdited;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current logged in user
        currentUser = LoginController.getCurrentUser();
        
        // Ensure user is a teacher
        if (currentUser != null && currentUser.getRole().equals("teacher")) {
            // Load the teacher's quizzes
            loadTeacherQuizzes();
            
            // Setup search functionality
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterQuizzes(newValue);
            });
            
            // Setup dialog components
            if (courseComboBox != null) {
                // Load teacher's courses for the course combo box
                loadTeacherCoursesForComboBox();
                
                // Add a listener to update the selected course ID when selection changes
                courseComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                    updateSelectedCourseId(newValue);
                });
            }
        } else {
            // If not a teacher, show message or redirect
            showAlert(AlertType.WARNING, "Access Denied", "Only teachers can access this page.");
        }
    }
    
    /**
     * Updates the selected course ID based on the course name selection
     */
    private void updateSelectedCourseId(String courseName) {
        if (courseName == null || courseName.isEmpty() || courseName.equals("No courses available")) {
            selectedCourseId = -1;
            return;
        }
        
        // Find the course by name
        for (Course course : coursesList) {
            if (course.getTitle().equals(courseName)) {
                selectedCourseId = course.getId();
                return;
            }
        }
    }
    
    /**
     * Loads all quizzes created by the current teacher and displays them as cards
     */
    private void loadTeacherQuizzes() {
        // Clear the container
        quizCardsContainer.getChildren().clear();
        
        // Get all quizzes from the service for this teacher
        List<Quiz> teacherQuizzes = QuizService.getQuizzesByTeacherId(currentUser.getId());
        quizzesList.setAll(teacherQuizzes);
        
        // If no quizzes, show a message
        if (quizzesList.isEmpty()) {
            Label noQuizzesLabel = new Label("You haven't created any quizzes yet. Click the 'Add new quiz +' button to get started!");
            noQuizzesLabel.getStyleClass().add("no-courses-message");
            noQuizzesLabel.setPadding(new Insets(50, 0, 0, 0));
            quizCardsContainer.getChildren().add(noQuizzesLabel);
        } else {
            // Create and add a card for each quiz
            for (Quiz quiz : quizzesList) {
                quizCardsContainer.getChildren().add(createQuizCard(quiz));
            }
        }
    }
    
    /**
     * Loads the teacher's courses for the course selection combo box
     */
    private void loadTeacherCoursesForComboBox() {
        // Get all courses from the service for this teacher
        List<Course> courses = CourseService.getCoursesByTeacherId(currentUser.getId());
        coursesList.setAll(courses);
        
        // Create list of course names for the combo box
        courseNamesForComboBox.clear();
        for (Course course : coursesList) {
            courseNamesForComboBox.add(course.getTitle());
        }
        
        // Set the items in the combo box
        if (!courseNamesForComboBox.isEmpty()) {
            courseComboBox.setItems(courseNamesForComboBox);
            courseComboBox.getSelectionModel().select(0);
            // Update the selected course ID for the initially selected course
            updateSelectedCourseId(courseComboBox.getSelectionModel().getSelectedItem());
        } else {
            // If no courses, add a default option
            courseComboBox.setItems(FXCollections.observableArrayList("No courses available"));
            courseComboBox.getSelectionModel().select(0);
            // Disable the combo box if there are no courses
            courseComboBox.setDisable(true);
        }
    }

    /**
     * Creates a visual card representation for a quiz with edit/delete options
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

        // Quiz logo/icon
        StackPane logoContainer = new StackPane();
        logoContainer.setMinWidth(50);
        logoContainer.setMaxWidth(50);
        logoContainer.setPrefHeight(50);
        logoContainer.setAlignment(Pos.TOP_CENTER);
        logoContainer.getStyleClass().add("logo-container");

        ImageView quizIcon = new ImageView();
        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/Object Scan.png"));
            quizIcon.setImage(logo);
        } catch (Exception e) {
            System.out.println("Failed to load logo for quiz: " + quiz.getTitle());
        }
        quizIcon.setFitWidth(50);
        quizIcon.setFitHeight(50);
        quizIcon.getStyleClass().add("course-icon");

        logoContainer.getChildren().add(quizIcon);

        // Add title and logo to header box
        headerBox.getChildren().addAll(titleContainer, logoContainer);

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
        Label courseLabel = new Label("Course: " + (course != null ? course.getTitle() : "Unknown"));
        courseLabel.getStyleClass().add("card-instructor");
        courseLabel.setWrapText(true);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Footer section with creation date and buttons
        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.BOTTOM_LEFT);
        footerBox.setPrefWidth(480);
        footerBox.setPrefHeight(30);
        
        // Creation date info
        HBox dateBox = new HBox();
        dateBox.getStyleClass().add("card-date");
        dateBox.setAlignment(Pos.CENTER_LEFT);
        dateBox.setPrefHeight(30);
        dateBox.setSpacing(10);
        HBox.setHgrow(dateBox, Priority.ALWAYS);

        // Date icon
        ImageView calendarIcon = new ImageView();
        try {
            Image calendarImg = new Image(getClass().getResourceAsStream("/images/Case.png"));
            calendarIcon.setImage(calendarImg);
            calendarIcon.setFitHeight(20);
            calendarIcon.setFitWidth(20);
            calendarIcon.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Failed to load calendar icon");
        }

        String createdAt = "Created: " + (quiz.getCreatedAt() != null ? 
                           quiz.getCreatedAt().toString().substring(0, 10) : "Unknown");
        Label dateLabel = new Label(createdAt);
        dateLabel.getStyleClass().add("date-label");

        dateBox.getChildren().addAll(calendarIcon, dateLabel);

        // Buttons section
        HBox buttonBox = new HBox(8); // Spacing between buttons
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Edit button - with icon
        Button editButton = new Button();
        editButton.getStyleClass().add("icon-button");
        editButton.setPrefWidth(24);
        editButton.setPrefHeight(24);
        editButton.setOnAction(e -> handleEditQuiz(quiz));
        
        // Edit icon
        ImageView editIcon = new ImageView();
        try {
            Image penImage = new Image(getClass().getResourceAsStream("/images/Pen.png"));
            editIcon.setImage(penImage);
            editIcon.setFitWidth(15);
            editIcon.setFitHeight(15);
            editIcon.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Failed to load pen icon");
        }
        editButton.setGraphic(editIcon);

        // Delete button - with icon
        Button deleteButton = new Button();
        deleteButton.getStyleClass().add("icon-button");
        deleteButton.setPrefWidth(24);
        deleteButton.setPrefHeight(24);
        deleteButton.setOnAction(e -> handleDeleteQuiz(quiz));
        
        // Delete icon
        ImageView deleteIcon = new ImageView();
        try {
            Image trashImage = new Image(getClass().getResourceAsStream("/images/Trash.png"));
            deleteIcon.setImage(trashImage);
            deleteIcon.setFitWidth(15);
            deleteIcon.setFitHeight(15);
            deleteIcon.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Failed to load trash icon");
        }
        deleteButton.setGraphic(deleteIcon);

        buttonBox.getChildren().addAll(editButton, deleteButton);

        // Add date and buttons to footer
        footerBox.getChildren().addAll(dateBox, buttonBox);

        // Add all sections to the card
        cardContent.getChildren().addAll(headerBox, descriptionLabel, courseLabel, spacer, footerBox);

        // Stack the background and content
        cardPane.getChildren().addAll(cardBackground, cardContent);

        // Add accessibility features
        cardPane.setAccessibleText("Quiz: " + quiz.getTitle() + ", " + description);

        return cardPane;
    }
    
    /**
     * Filters quizzes based on search query
     */
    private void filterQuizzes(String query) {
        if (query == null || query.isEmpty()) {
            // If query is empty, show all quizzes
            loadTeacherQuizzes();
        } else {
            // Filter quizzes based on query
            query = query.toLowerCase();
            final String searchQuery = query;
            
            List<Quiz> filteredList = quizzesList.stream()
                .filter(quiz -> 
                    quiz.getTitle().toLowerCase().contains(searchQuery) ||
                    quiz.getDescription().toLowerCase().contains(searchQuery))
                .collect(Collectors.toList());
            
            quizCardsContainer.getChildren().clear();
            
            if (filteredList.isEmpty()) {
                Label noResultsLabel = new Label("No quizzes match your search criteria.");
                noResultsLabel.getStyleClass().add("no-courses-message");
                noResultsLabel.setPadding(new Insets(50, 0, 0, 0));
                quizCardsContainer.getChildren().add(noResultsLabel);
            } else {
                for (Quiz quiz : filteredList) {
                    quizCardsContainer.getChildren().add(createQuizCard(quiz));
                }
            }
        }
    }
    
    /**
     * Handles the action when the Add New Quiz button is clicked
     */
    @FXML
    private void handleAddNewQuiz(ActionEvent event) {
        // Reset form fields
        resetFormFields();
        
        // Show the overlay
        addQuizOverlay.setVisible(true);
    }
    
    /**
     * Reset all form fields
     */
    private void resetFormFields() {
        if (courseComboBox != null && !courseNamesForComboBox.isEmpty()) {
            courseComboBox.getSelectionModel().select(0);
            updateSelectedCourseId(courseComboBox.getSelectionModel().getSelectedItem());
        }
        
        // Reset selected course ID
        selectedCourseId = -1;
    }
    
    /**
     * Handles the cancel button action in the dialog
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        // Hide the overlay
        addQuizOverlay.setVisible(false);
    }
    
    /**
     * Handles the save button action in the dialog
     */
    @FXML
    private void handleSave(ActionEvent event) {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }
        
        // Get the selected course
        Course selectedCourse = null;
        for (Course course : coursesList) {
            if (course.getId() == selectedCourseId) {
                selectedCourse = course;
                break;
            }
        }
        
        if (selectedCourse == null) {
            showAlert(AlertType.ERROR, "Error", "Could not find the selected course.");
            return;
        }
        
        // Create temporary quiz object (not saved to database yet)
        Quiz tempQuiz = new Quiz();
        // Use course title as quiz title
        tempQuiz.setTitle("Quiz for " + selectedCourse.getTitle());
        tempQuiz.setDescription(""); // Empty description
        tempQuiz.setComment(""); // Not using comment field
        tempQuiz.setCourseId(selectedCourseId);
        
        // Hide the course selection dialog
        addQuizOverlay.setVisible(false);
        
        // Show the question entry form for the new quiz without saving it to the database yet
        openQuestionEntryForm(tempQuiz, true);
    }
    
    /**
     * Opens the question entry form for a specific quiz
     * @param quiz The quiz to add questions to
     * @param isNewQuiz Flag indicating if this is a new quiz (not yet saved to database)
     */
    private void openQuestionEntryForm(Quiz quiz, boolean isNewQuiz) {
        try {
            // Call our own showAddQuestionDialog method with the quiz
            showAddQuestionDialog(quiz, isNewQuiz);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to open question entry form: " + e.getMessage());
            
            // Fall back to just refreshing the quizzes list
            loadTeacherQuizzes();
        }
    }
    
    private boolean validateInputs() {
        // Validate course selection
        if (courseComboBox.getSelectionModel().getSelectedItem() == null || 
            courseComboBox.getSelectionModel().getSelectedItem().equals("No courses available")) {
            showAlert(AlertType.WARNING, "Validation Error", "Please create a course first before adding quizzes.");
            return false;
        }
        
        // Make sure the course ID was properly set
        if (selectedCourseId <= 0) {
            showAlert(AlertType.WARNING, "Validation Error", "Please select a valid course.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Handles editing a quiz
     */
    private void handleEditQuiz(Quiz quiz) {
        // Load all questions and answers for this quiz for editing
        loadQuizForEditing(quiz);
    }

    /**
     * Loads a quiz for editing with all of its questions and answers
     */
    private void loadQuizForEditing(Quiz quiz) {
        try {
            // Store the quiz being edited
            quizBeingEdited = quiz;
            
            // Set the quiz title in the dialog
            editQuizTitleLabel.setText("Edit: " + quiz.getTitle());
            
            // Get the course name
            Course course = CourseService.getCourseById(quiz.getCourseId());
            editCourseNameLabel.setText("Course: " + (course != null ? course.getTitle() : "Unknown"));
            
            // Get all questions for this quiz
            List<Question> questions = QuestionService.getQuestionsByQuizId(quiz.getId());
            
            // If there are no questions, show message
            if (questions == null || questions.isEmpty()) {
                showAlert(AlertType.WARNING, "No Questions", "This quiz doesn't have any questions yet.");
                return;
            }
            
            // Clear any previous data
            editingQuestions.clear();
            questionAnswers.clear();
            currentQuestionIndex = 0;
            
            // Store questions for editing
            editingQuestions.addAll(questions);
            
            // Load answers for each question
            for (Question question : editingQuestions) {
                List<Answer> answers = AnswerService.getAnswersByQuestionId(question.getId());
                if (answers != null) {
                    questionAnswers.put(question.getId(), answers);
                }
            }
            
            // Show the first question
            showQuestionForEditing(currentQuestionIndex);
            
            // Set up the delete question button handler
            deleteQuestionButton.setOnAction(e -> handleDeleteQuestion());
            
            // Show the edit dialog
            editQuizOverlay.setVisible(true);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to load quiz for editing: " + e.getMessage());
        }
    }
    
    /**
     * Shows a specific question for editing
     */
    private void showQuestionForEditing(int index) {
        if (index < 0 || index >= editingQuestions.size()) {
            return;
        }
        
        // Get the question at the specified index
        Question question = editingQuestions.get(index);
        
        // Update progress label
        editProgressLabel.setText("Question " + (index + 1) + "/" + editingQuestions.size());
        
        // Set question text
        editQuestionTextArea.setText(question.getQuestionText());
        
        // Clear answer fields and radio selections
        editAnswerField1.clear();
        editAnswerField2.clear();
        editAnswerField3.clear();
        editAnswerField4.clear();
        editCorrectAnswerGroup.selectToggle(null);
        
        // Get answers for this question
        List<Answer> answers = questionAnswers.get(question.getId());
        
        if (answers != null && !answers.isEmpty()) {
            // Fill in answer fields (up to 4 answers)
            int answerCount = Math.min(answers.size(), 4);
            
            for (int i = 0; i < answerCount; i++) {
                Answer answer = answers.get(i);
                
                switch (i) {
                    case 0:
                        editAnswerField1.setText(answer.getAnswerText());
                        if (answer.isCorrect()) {
                            editCorrectAnswerGroup.selectToggle(editRadioAnswer1);
                        }
                        break;
                    case 1:
                        editAnswerField2.setText(answer.getAnswerText());
                        if (answer.isCorrect()) {
                            editCorrectAnswerGroup.selectToggle(editRadioAnswer2);
                        }
                        break;
                    case 2:
                        editAnswerField3.setText(answer.getAnswerText());
                        if (answer.isCorrect()) {
                            editCorrectAnswerGroup.selectToggle(editRadioAnswer3);
                        }
                        break;
                    case 3:
                        editAnswerField4.setText(answer.getAnswerText());
                        if (answer.isCorrect()) {
                            editCorrectAnswerGroup.selectToggle(editRadioAnswer4);
                        }
                        break;
                }
            }
        }
        
        // Update button states based on position
        prevQuestionButton.setDisable(index == 0);
        
        // Check if this is the last question
        if (index == editingQuestions.size() - 1) {
            nextQuestionButton.setText("Add Question");
            nextQuestionButton.setOnAction(e -> handleAddNewQuestion());
        } else {
            nextQuestionButton.setText("Next Question");
            nextQuestionButton.setOnAction(e -> handleNextQuestion());
        }
    }
    
    /**
     * Handles moving to the previous question
     */
    @FXML
    private void handlePrevQuestion() {
        // Save current question changes
        if (validateEditQuestionForm()) {
            saveCurrentEditQuestion();
            currentQuestionIndex--;
            showQuestionForEditing(currentQuestionIndex);
        }
    }
    
    /**
     * Handles moving to the next question
     */
    @FXML
    private void handleNextQuestion() {
        // Save current question changes
        if (validateEditQuestionForm()) {
            saveCurrentEditQuestion();
            currentQuestionIndex++;
            showQuestionForEditing(currentQuestionIndex);
        }
    }
    
    /**
     * Validates the edit question form
     */
    private boolean validateEditQuestionForm() {
        // Check question text
        if (editQuestionTextArea.getText() == null || editQuestionTextArea.getText().trim().isEmpty()) {
            showAlert(AlertType.WARNING, "Validation Error", "Please enter a question.");
            return false;
        }
        
        // Check that at least 2 answers are provided
        if (editAnswerField1.getText().trim().isEmpty() || editAnswerField2.getText().trim().isEmpty()) {
            showAlert(AlertType.WARNING, "Validation Error", "Please provide at least two answer options.");
            return false;
        }
        
        // Check that a correct answer is selected
        if (editCorrectAnswerGroup.getSelectedToggle() == null) {
            showAlert(AlertType.WARNING, "Validation Error", "Please select the correct answer.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Saves changes to the current question being edited
     */
    private void saveCurrentEditQuestion() {
        try {
            // Get the current question
            Question question = editingQuestions.get(currentQuestionIndex);
            
            // Update question text
            question.setQuestionText(editQuestionTextArea.getText().trim());
            
            // Update in database
            QuestionService.updateQuestion(question);
            
            // Determine which radio button is selected
            RadioButton selectedRadio = (RadioButton) editCorrectAnswerGroup.getSelectedToggle();
            
            // Get existing answers
            List<Answer> existingAnswers = questionAnswers.get(question.getId());
            if (existingAnswers == null) {
                existingAnswers = new ArrayList<>();
            }
            
            // Create new answers list for this question
            List<Answer> updatedAnswers = new ArrayList<>();
            
            // Process answer 1
            if (!editAnswerField1.getText().trim().isEmpty()) {
                Answer answer = findOrCreateAnswer(existingAnswers, 0);
                answer.setQuestionId(question.getId());
                answer.setAnswerText(editAnswerField1.getText().trim());
                answer.setCorrect(selectedRadio == editRadioAnswer1);
                updatedAnswers.add(answer);
            }
            
            // Process answer 2
            if (!editAnswerField2.getText().trim().isEmpty()) {
                Answer answer = findOrCreateAnswer(existingAnswers, 1);
                answer.setQuestionId(question.getId());
                answer.setAnswerText(editAnswerField2.getText().trim());
                answer.setCorrect(selectedRadio == editRadioAnswer2);
                updatedAnswers.add(answer);
            }
            
            // Process answer 3
            if (!editAnswerField3.getText().trim().isEmpty()) {
                Answer answer = findOrCreateAnswer(existingAnswers, 2);
                answer.setQuestionId(question.getId());
                answer.setAnswerText(editAnswerField3.getText().trim());
                answer.setCorrect(selectedRadio == editRadioAnswer3);
                updatedAnswers.add(answer);
            }
            
            // Process answer 4
            if (!editAnswerField4.getText().trim().isEmpty()) {
                Answer answer = findOrCreateAnswer(existingAnswers, 3);
                answer.setQuestionId(question.getId());
                answer.setAnswerText(editAnswerField4.getText().trim());
                answer.setCorrect(selectedRadio == editRadioAnswer4);
                updatedAnswers.add(answer);
            }
            
            // Handle deletion of existing answers that are no longer needed
            for (Answer existingAnswer : existingAnswers) {
                boolean stillExists = false;
                for (Answer updatedAnswer : updatedAnswers) {
                    if (updatedAnswer.getId() == existingAnswer.getId()) {
                        stillExists = true;
                        break;
                    }
                }
                if (!stillExists) {
                    // Delete this answer
                    AnswerService.deleteAnswer(existingAnswer.getId());
                }
            }
            
            // Save or update each answer
            for (Answer answer : updatedAnswers) {
                if (answer.getId() > 0) {
                    // Update existing answer
                    AnswerService.updateAnswer(answer);
                } else {
                    // Create new answer
                    AnswerService.addAnswer(answer);
                }
            }
            
            // Update the stored answers for this question
            questionAnswers.put(question.getId(), updatedAnswers);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to save question changes: " + e.getMessage());
        }
    }
    
    /**
     * Finds an existing answer or creates a new one
     */
    private Answer findOrCreateAnswer(List<Answer> existingAnswers, int index) {
        if (existingAnswers != null && index < existingAnswers.size()) {
            return existingAnswers.get(index);
        }
        return new Answer();
    }
    
    /**
     * Handles deleting the current question
     */
    private void handleDeleteQuestion() {
        // Confirm deletion
        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete this question?");
        
        // Process the result
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteCurrentQuestion();
            }
        });
    }
    
    /**
     * Deletes the current question
     */
    private void deleteCurrentQuestion() {
        try {
            // Get the current question
            Question question = editingQuestions.get(currentQuestionIndex);
            
            // Delete from database
            boolean success = QuestionService.deleteQuestion(question.getId());
            
            if (!success) {
                showAlert(AlertType.ERROR, "Error", "Failed to delete the question.");
                return;
            }
            
            // Remove from our lists
            editingQuestions.remove(currentQuestionIndex);
            questionAnswers.remove(question.getId());
            
            // Check if we still have questions
            if (editingQuestions.isEmpty()) {
                // If no more questions, close the dialog
                editQuizOverlay.setVisible(false);
                showAlert(AlertType.INFORMATION, "No Questions", "All questions have been deleted. You may add new ones from the Add Question dialog.");
                return;
            }
            
            // Adjust current index if needed
            if (currentQuestionIndex >= editingQuestions.size()) {
                currentQuestionIndex = editingQuestions.size() - 1;
            }
            
            // Show the current question
            showQuestionForEditing(currentQuestionIndex);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to delete the question: " + e.getMessage());
        }
    }
    
    /**
     * Handles saving all quiz edits and closing the dialog
     */
    @FXML
    private void handleSaveQuizEdits() {
        // Save current question changes
        if (validateEditQuestionForm()) {
            saveCurrentEditQuestion();
            
            // Hide the dialog
            editQuizOverlay.setVisible(false);
            
            // Show success message
            showAlert(AlertType.INFORMATION, "Success", "All changes have been saved successfully!");
            
            // Refresh the quizzes list
            loadTeacherQuizzes();
        }
    }
    
    /**
     * Handles canceling the edit dialog
     */
    @FXML
    private void handleCancelEdit() {
        // Confirm cancellation if changes might be lost
        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Cancel Editing");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to cancel? Any unsaved changes will be lost.");
        
        // Process the result
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Hide the dialog
                editQuizOverlay.setVisible(false);
                
                // Refresh the quizzes list
                loadTeacherQuizzes();
            }
        });
    }
    
    /**
     * Handles updating an existing quiz
     */
    private void handleUpdateQuiz(int quizId) {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }
        
        // Get existing quiz
        Quiz existingQuiz = quizzesList.stream()
                                      .filter(q -> q.getId() == quizId)
                                      .findFirst()
                                      .orElse(null);
                                      
        if (existingQuiz == null) {
            showAlert(AlertType.ERROR, "Error", "Could not find the quiz to update.");
            return;
        }
        
        // Create updated quiz object
        Quiz updatedQuiz = new Quiz();
        updatedQuiz.setId(quizId);
        updatedQuiz.setTitle(existingQuiz.getTitle());
        updatedQuiz.setDescription(existingQuiz.getDescription());
        updatedQuiz.setComment(existingQuiz.getComment());
        
        // Get selected course ID from our instance variable
        updatedQuiz.setCourseId(selectedCourseId);
        
        // Update in database
        boolean success = QuizService.updateQuiz(updatedQuiz);
        
        if (success) {
            // Hide dialog
            addQuizOverlay.setVisible(false);
            
            // Reset save button to add mode for future uses
            if (saveButton != null) {
                saveButton.setOnAction(this::handleSave);
            }
            
            // Refresh the quizzes list
            loadTeacherQuizzes();
            
            // Show success message
            showAlert(AlertType.INFORMATION, "Success", "Quiz updated successfully!");
        } else {
            showAlert(AlertType.ERROR, "Error", "Failed to update the quiz.");
        }
    }

    /**
     * Handles the cancel button action in the question dialog
     */
    @FXML
    private void handleCancelQuestion(ActionEvent event) {
        // Hide the question overlay
        if (addQuestionOverlay != null) {
            addQuestionOverlay.setVisible(false);
        }
        
        // Refresh the quizzes list
        loadTeacherQuizzes();
    }

    /**
     * Handles adding another question to the quiz
     */
    @FXML
    private void handleAddAnotherQuestion(ActionEvent event) {
        // Validate and save the current question
        if (validateQuestionForm()) {
            saveCurrentQuestion();
            // Reset the form for the next question
            resetQuestionForm();
            // Show success message
            showAlert(AlertType.INFORMATION, "Success", "Question added successfully! Add another question or click Done when finished.");
        }
    }
    
    /**
     * Handles completing the question entry process
     */
    @FXML
    private void handleDoneAddingQuestions(ActionEvent event) {
        // Check if we have at least entered the current question
        if (validateQuestionForm()) {
            saveCurrentQuestion();
        }
        
        // Check if we have any questions to save
        if (currentQuizQuestions.isEmpty()) {
            showAlert(AlertType.WARNING, "No Questions", "Please add at least one question before finishing.");
            return;
        }
        
        // If this is a new quiz, we need to save it to the database first
        if (isNewQuiz) {
            boolean success = QuizService.addQuiz(currentQuiz);
            if (!success) {
                showAlert(AlertType.ERROR, "Error", "Failed to save the quiz to the database.");
                return;
            }
            
            // Get the newly created quiz ID so we can associate questions with it
            List<Quiz> latestQuizzes = QuizService.getQuizzesByTeacherId(currentUser.getId());
            Quiz createdQuiz = null;
            
            // Find the quiz we just created (should be the most recent one with matching title and course ID)
            if (latestQuizzes != null && !latestQuizzes.isEmpty()) {
                for (Quiz quiz : latestQuizzes) {
                    if (quiz.getTitle().equals(currentQuiz.getTitle()) && 
                        quiz.getCourseId() == currentQuiz.getCourseId()) {
                        createdQuiz = quiz;
                        break;
                    }
                }
            }
            
            if (createdQuiz == null) {
                showAlert(AlertType.ERROR, "Error", "Failed to retrieve the created quiz.");
                return;
            }
            
            // Update the currentQuiz with the database-assigned ID
            currentQuiz.setId(createdQuiz.getId());
            
            // Now that we have a valid quiz ID, save all the questions and answers
            saveAllQuestionsAndAnswers();
        }
        
        // Hide the form
        addQuestionOverlay.setVisible(false);
        
        // Show success message
        if (isNewQuiz) {
            showAlert(AlertType.INFORMATION, "Quiz Complete", 
                "Quiz has been created and questions have been saved successfully!");
        } else {
            showAlert(AlertType.INFORMATION, "Questions Saved", 
                "All questions have been saved successfully!");
        }
        
        // Refresh the quizzes list
        loadTeacherQuizzes();
        
        // Clear the form and questions list for future use
        resetQuestionForm();
        currentQuizQuestions.clear();
    }

    /**
     * Shows the question entry form
     * @param quiz The quiz to add questions to
     * @param isNewQuiz Flag indicating if this is a new quiz that hasn't been saved to the database yet
     */
    public void showAddQuestionDialog(Quiz quiz, boolean isNewQuiz) {
        this.currentQuiz = quiz;
        this.isNewQuiz = isNewQuiz; // Store if this is a new quiz or not
        resetQuestionForm();
        addQuestionOverlay.setVisible(true);
    }
    
    /**
     * Validates the question form inputs
     */
    private boolean validateQuestionForm() {
        // Check question text
        if (questionTextField.getText() == null || questionTextField.getText().trim().isEmpty()) {
            showAlert(AlertType.WARNING, "Validation Error", "Please enter a question.");
            return false;
        }
        
        // Check that at least 2 answers are provided
        if (answerField1.getText().trim().isEmpty() || answerField2.getText().trim().isEmpty()) {
            showAlert(AlertType.WARNING, "Validation Error", "Please provide at least two answer options.");
            return false;
        }
        
        // Check that a correct answer is selected
        if (correctAnswerGroup.getSelectedToggle() == null) {
            showAlert(AlertType.WARNING, "Validation Error", "Please select the correct answer.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Resets the question form fields
     */
    private void resetQuestionForm() {
        if (questionTextField != null) questionTextField.clear();
        if (answerField1 != null) answerField1.clear();
        if (answerField2 != null) answerField2.clear();
        if (answerField3 != null) answerField3.clear();
        if (answerField4 != null) answerField4.clear();
        if (correctAnswerGroup != null) correctAnswerGroup.selectToggle(null);
    }

    /**
     * Saves the current question and its answers
     */
    private void saveCurrentQuestion() {
        try {
            // For new quizzes, we need to store questions temporarily until the quiz is created
            if (isNewQuiz) {
                // Create Question object but don't save it to database yet
                Question question = new Question();
                // Use a temporary ID until the quiz is saved to database
                question.setId(-1 * (currentQuizQuestions.size() + 1)); // Use negative IDs for temp questions
                question.setQuestionText(questionTextField.getText().trim());
                
                // Add to our list of questions for this quiz
                currentQuizQuestions.add(question);
                
                // Store answers in memory - we'll save them to DB after the quiz is created
                storeAnswersForQuestion(question);
                
                // Don't try to save to the database yet
                return;
            }
            
            // If we're here, the quiz already exists in the database
            
            // Create Question object
            Question question = new Question();
            question.setQuizId(currentQuiz.getId());
            question.setQuestionText(questionTextField.getText().trim());
            
            // Save question to database
            int questionId = QuestionService.addQuestion(question);
            
            // Check if question was saved successfully
            if (questionId <= 0) {
                showAlert(AlertType.ERROR, "Error", "Failed to save the question to the database.");
                return;
            }
            
            question.setId(questionId);
            
            // Add to our list of questions for this quiz
            currentQuizQuestions.add(question);
            
            // Save the answers
            saveAnswers(questionId);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to save the question: " + e.getMessage());
        }
    }
    
    /**
     * Stores answers for a question in memory (for new quizzes before they're saved to DB)
     */
    private void storeAnswersForQuestion(Question question) {
        // Determine which radio button is selected
        RadioButton selectedRadio = (RadioButton) correctAnswerGroup.getSelectedToggle();
        
        // Process answer 1
        if (!answerField1.getText().trim().isEmpty()) {
            Answer answer = new Answer();
            answer.setQuestionId(question.getId()); // Use the temporary question ID
            answer.setAnswerText(answerField1.getText().trim());
            answer.setCorrect(selectedRadio == radioAnswer1);
            question.addAnswer(answer); // Store answer in the question object
        }
        
        // Process answer 2
        if (!answerField2.getText().trim().isEmpty()) {
            Answer answer = new Answer();
            answer.setQuestionId(question.getId()); // Use the temporary question ID
            answer.setAnswerText(answerField2.getText().trim());
            answer.setCorrect(selectedRadio == radioAnswer2);
            question.addAnswer(answer); // Store answer in the question object
        }
        
        // Process answer 3
        if (!answerField3.getText().trim().isEmpty()) {
            Answer answer = new Answer();
            answer.setQuestionId(question.getId()); // Use the temporary question ID
            answer.setAnswerText(answerField3.getText().trim());
            answer.setCorrect(selectedRadio == radioAnswer3);
            question.addAnswer(answer); // Store answer in the question object
        }
        
        // Process answer 4
        if (!answerField4.getText().trim().isEmpty()) {
            Answer answer = new Answer();
            answer.setQuestionId(question.getId()); // Use the temporary question ID
            answer.setAnswerText(answerField4.getText().trim());
            answer.setCorrect(selectedRadio == radioAnswer4);
            question.addAnswer(answer); // Store answer in the question object
        }
    }
    
    /**
     * Saves the answers for the current question
     */
    private void saveAnswers(int questionId) {
        // Determine which radio button is selected
        RadioButton selectedRadio = (RadioButton) correctAnswerGroup.getSelectedToggle();
        
        // Process answer 1
        if (!answerField1.getText().trim().isEmpty()) {
            Answer answer = new Answer();
            answer.setQuestionId(questionId);
            answer.setAnswerText(answerField1.getText().trim());
            answer.setCorrect(selectedRadio == radioAnswer1);
            AnswerService.addAnswer(answer);
        }
        
        // Process answer 2
        if (!answerField2.getText().trim().isEmpty()) {
            Answer answer = new Answer();
            answer.setQuestionId(questionId);
            answer.setAnswerText(answerField2.getText().trim());
            answer.setCorrect(selectedRadio == radioAnswer2);
            AnswerService.addAnswer(answer);
        }
        
        // Process answer 3
        if (!answerField3.getText().trim().isEmpty()) {
            Answer answer = new Answer();
            answer.setQuestionId(questionId);
            answer.setAnswerText(answerField3.getText().trim());
            answer.setCorrect(selectedRadio == radioAnswer3);
            AnswerService.addAnswer(answer);
        }
        
        // Process answer 4
        if (!answerField4.getText().trim().isEmpty()) {
            Answer answer = new Answer();
            answer.setQuestionId(questionId);
            answer.setAnswerText(answerField4.getText().trim());
            answer.setCorrect(selectedRadio == radioAnswer4);
            AnswerService.addAnswer(answer);
        }
    }
    
    /**
     * Saves all temporarily stored questions and their answers to the database
     */
    private void saveAllQuestionsAndAnswers() {
        if (currentQuizQuestions.isEmpty() || currentQuiz == null || currentQuiz.getId() <= 0) {
            return;
        }
        
        try {
            // Save each question and its answers
            for (Question question : currentQuizQuestions) {
                // Skip questions that have already been saved (positive IDs)
                if (question.getId() > 0) {
                    continue;
                }
                
                // Create a new question to save
                Question dbQuestion = new Question();
                dbQuestion.setQuizId(currentQuiz.getId());
                dbQuestion.setQuestionText(question.getQuestionText());
                
                // Save to database
                int questionId = QuestionService.addQuestion(dbQuestion);
                
                if (questionId <= 0) {
                    System.out.println("Failed to save question: " + question.getQuestionText());
                    continue;
                }
                
                // Save all answers for this question
                if (question.getAnswers() != null) {
                    for (Answer answer : question.getAnswers()) {
                        Answer dbAnswer = new Answer();
                        dbAnswer.setQuestionId(questionId);
                        dbAnswer.setAnswerText(answer.getAnswerText());
                        dbAnswer.setCorrect(answer.isCorrect());
                        AnswerService.addAnswer(dbAnswer);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to save some questions: " + e.getMessage());
        }
    }
    
    /**
     * Handles deleting a quiz
     */
    private void handleDeleteQuiz(Quiz quiz) {
        // Confirm deletion
        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete the quiz: " + quiz.getTitle() + "?");
        
        // Process the result
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = QuizService.deleteQuiz(quiz.getId());
                
                if (success) {
                    loadTeacherQuizzes(); // Reload quizzes after deletion
                    showAlert(AlertType.INFORMATION, "Success", "Quiz deleted successfully");
                } else {
                    showAlert(AlertType.ERROR, "Error", "Failed to delete quiz");
                }
            }
        });
    }
    
    /**
     * Handles the search action
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField.getText().trim();
        filterQuizzes(query);
    }
    
    /**
     * Helper method to show alerts
     */
    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Handles adding a new question to the quiz
     */
    private void handleAddNewQuestion() {
        // Save current question changes
        if (validateEditQuestionForm()) {
            saveCurrentEditQuestion();
            
            // Create a new question
            Question newQuestion = new Question();
            newQuestion.setQuizId(quizBeingEdited.getId());
            newQuestion.setQuestionText("New Question"); // Default text
            
            // Add to database
            int questionId = QuestionService.addQuestion(newQuestion);
            if (questionId > 0) {
                newQuestion.setId(questionId);
                
                // Add to our lists
                editingQuestions.add(newQuestion);
                questionAnswers.put(questionId, new ArrayList<>());
                
                // Move to the new question
                currentQuestionIndex = editingQuestions.size() - 1;
                showQuestionForEditing(currentQuestionIndex);
            } else {
                showAlert(AlertType.ERROR, "Error", "Failed to add new question.");
            }
        }
    }
}