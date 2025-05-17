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
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListCell;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the teacher quizzes interface.
 * Manages the creation, editing, and deletion of quizzes and their questions.
 * 
 * @author Sellami Mohamed Odai
 */
public class TeacherQuizzesController implements Initializable {

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

    /**
     * Initializes the controller by loading teacher quizzes and setting up listeners.
     * Access is restricted to users with the teacher role.
     * 
     * @param location The location used to resolve relative paths
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = AuthLoginController.getCurrentUser();
        
        if (currentUser != null && currentUser.getRole().equals("teacher")) {
            loadTeacherQuizzes();
            
            searchField.textProperty().addListener((observable, oldValue, newValue) -> 
                filterQuizzes(newValue));
            
            if (courseComboBox != null) {
                courseComboBox.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> updateSelectedCourseId(newValue));
                
                loadTeacherCoursesForComboBox();
            }
        } else {
            showAlert(AlertType.WARNING, "Access Denied", "Only teachers can access this page.");
        }
    }
    
    /**
     * Updates the selected course ID based on the course name.
     * 
     * @param courseName The name of the selected course
     */
    private void updateSelectedCourseId(String courseName) {
        if (courseName == null || courseName.isEmpty() || courseName.equals("Select a course")) {
            selectedCourseId = -1;
            return;
        }
        
        for (Course course : coursesList) {
            if (course.getTitle().equals(courseName)) {
                selectedCourseId = course.getId();
                return;
            }
        }
    }
    
    /**
     * Loads quizzes created by the current teacher and displays them as cards.
     */
    private void loadTeacherQuizzes() {
        quizCardsContainer.getChildren().clear();
        
        List<Quiz> teacherQuizzes = QuizService.getQuizzesByTeacherId(currentUser.getId());
        quizzesList.setAll(teacherQuizzes);
        
        if (quizzesList.isEmpty()) {
            Label noQuizzesLabel = new Label("You haven't created any quizzes yet. Click the 'Add new quiz +' button to get started!");
            noQuizzesLabel.getStyleClass().add("no-courses-message");
            noQuizzesLabel.setPadding(new Insets(50, 0, 0, 0));
            quizCardsContainer.getChildren().add(noQuizzesLabel);
        } else {
            quizzesList.forEach(quiz -> 
                quizCardsContainer.getChildren().add(createQuizCard(quiz)));
        }
    }
    
    /**
     * Loads the teacher's courses for the course selection combo box.
     * Disables the combo box if no courses are available.
     */
    private void loadTeacherCoursesForComboBox() {
        List<Course> courses = CourseService.getCoursesByTeacherId(currentUser.getId());
        coursesList.setAll(courses);
        
        courseNamesForComboBox.clear();
        
        if (!courses.isEmpty()) {
            coursesList.forEach(course -> courseNamesForComboBox.add(course.getTitle()));
        } else {
            courseComboBox.setItems(FXCollections.observableArrayList("No courses available"));
            courseComboBox.getSelectionModel().select(0);
            courseComboBox.setDisable(true);
        }
    }

    /**
     * Creates a visual card representation for a quiz with edit/delete options.
     * 
     * @param quiz The quiz to create a card for
     * @return StackPane containing the quiz card UI
     */
    private StackPane createQuizCard(Quiz quiz) {
        StackPane cardPane = new StackPane();
        cardPane.getStyleClass().add("course-card");
        cardPane.setPrefWidth(480);
        cardPane.setPrefHeight(230);

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

        VBox cardContent = new VBox();
        cardContent.getStyleClass().add("card-content");
        cardContent.setSpacing(20);
        cardContent.setPadding(new Insets(20, 20, 20, 20));
        cardContent.setPrefWidth(480);
        cardContent.setPrefHeight(230);

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

        headerBox.getChildren().addAll(titleContainer, logoContainer);

        String description = quiz.getDescription();
        if (description == null || description.isEmpty()) {
            description = "No description available";
        }
        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("card-description");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMinHeight(Region.USE_PREF_SIZE);

        Course course = CourseService.getCourseById(quiz.getCourseId());
        Label courseLabel = new Label("Course: " + (course != null ? course.getTitle() : "Unknown"));
        courseLabel.getStyleClass().add("card-instructor");
        courseLabel.setWrapText(true);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.BOTTOM_LEFT);
        footerBox.setPrefWidth(480);
        footerBox.setPrefHeight(30);
        
        HBox dateBox = new HBox();
        dateBox.getStyleClass().add("card-date");
        dateBox.setAlignment(Pos.CENTER_LEFT);
        dateBox.setPrefHeight(30);
        dateBox.setSpacing(10);
        HBox.setHgrow(dateBox, Priority.ALWAYS);

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

        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button editButton = new Button();
        editButton.getStyleClass().add("icon-button");
        editButton.setPrefWidth(24);
        editButton.setPrefHeight(24);
        editButton.setOnAction(e -> handleEditQuiz(quiz));
        
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

        Button deleteButton = new Button();
        deleteButton.getStyleClass().add("icon-button");
        deleteButton.setPrefWidth(24);
        deleteButton.setPrefHeight(24);
        deleteButton.setOnAction(e -> handleDeleteQuiz(quiz));
        
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
        
        Button resultsButton = new Button();
        resultsButton.getStyleClass().add("icon-button");
        resultsButton.setPrefWidth(24);
        resultsButton.setPrefHeight(24);
        resultsButton.setOnAction(e -> handleViewResults(quiz));
        
        ImageView resultsIcon = new ImageView();
        try {
            Image chartImage = new Image(getClass().getResourceAsStream("/images/Chart.png"));
            resultsIcon.setImage(chartImage);
            resultsIcon.setFitWidth(15);
            resultsIcon.setFitHeight(15);
            resultsIcon.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Failed to load chart icon");
            resultsButton.setText("📊");
        }
        resultsButton.setGraphic(resultsIcon);

        buttonBox.getChildren().addAll(resultsButton, editButton, deleteButton);

        footerBox.getChildren().addAll(dateBox, buttonBox);

        cardContent.getChildren().addAll(headerBox, descriptionLabel, courseLabel, spacer, footerBox);

        cardPane.getChildren().addAll(cardBackground, cardContent);

        cardPane.setAccessibleText("Quiz: " + quiz.getTitle() + ", " + description);

        return cardPane;
    }
    
    /**
     * Filters quizzes based on search query.
     * 
     * @param query The search term to filter quizzes
     */
    private void filterQuizzes(String query) {
        if (query == null || query.isEmpty()) {
            loadTeacherQuizzes();
        } else {
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
     * Handles the action when the Add New Quiz button is clicked.
     * Sets up and displays the course selection dialog.
     * 
     * @param event The ActionEvent
     */
    @FXML
    private void handleAddNewQuiz(ActionEvent event) {
        courseComboBox.getItems().clear();
        courseComboBox.getItems().add("Select a course");
        
        for (Course course : coursesList) {
            courseComboBox.getItems().add(course.getTitle());
        }
        
        courseComboBox.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    
                    if (item.equals("Select a course")) {
                        setDisable(true);
                        setStyle("-fx-opacity: 0.7;");
                    } else {
                        setDisable(false);
                        setStyle("");
                    }
                }
            }
        });
        
        courseComboBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText("Select a course");
                } else if (item.equals("Select a course")) {
                    setText("Select a course");
                } else {
                    setText(item);
                }
            }
        });
        
        courseComboBox.getSelectionModel().select(0);
        selectedCourseId = -1;
        
        addQuizOverlay.setVisible(true);
        
        javafx.application.Platform.runLater(() -> {
            courseComboBox.requestFocus();
            System.out.println("ComboBox items: " + courseComboBox.getItems());
        });
    }
    
    /**
     * Handles the cancel button action in the dialog.
     * Hides the overlay and clears selections.
     * 
     * @param event The ActionEvent
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        addQuizOverlay.setVisible(false);
        courseComboBox.getItems().clear();
    }
    
    /**
     * Handles the save button action in the dialog.
     * Validates inputs and creates a temporary quiz for question entry.
     * 
     * @param event The ActionEvent
     */
    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }
        
        Course selectedCourseObj = null;
        for (Course course : coursesList) {
            if (course.getId() == selectedCourseId) {
                selectedCourseObj = course;
                break;
            }
        }
        
        if (selectedCourseObj == null) {
            showAlert(AlertType.ERROR, "Error", "Could not find the selected course.");
            return;
        }
        
        Quiz tempQuiz = new Quiz();
        tempQuiz.setTitle("Quiz for " + selectedCourseObj.getTitle());
        tempQuiz.setDescription("");
        tempQuiz.setComment("");
        tempQuiz.setCourseId(selectedCourseId);
        
        courseComboBox.getItems().clear();
        selectedCourseId = -1;
        
        addQuizOverlay.setVisible(false);
        
        openQuestionEntryForm(tempQuiz, true);
    }
    
    /**
     * Opens the question entry form for a specific quiz.
     * 
     * @param quiz The quiz to add questions to
     * @param isNewQuiz Flag indicating if this is a new quiz (not yet saved to database)
     */
    private void openQuestionEntryForm(Quiz quiz, boolean isNewQuiz) {
        try {
            showAddQuestionDialog(quiz, isNewQuiz);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to open question entry form: " + e.getMessage());
            
            loadTeacherQuizzes();
        }
    }
    
    /**
     * Validates the input fields in the add quiz dialog.
     * 
     * @return true if all inputs are valid, false otherwise
     */
    private boolean validateInputs() {
        if (courseComboBox.getSelectionModel().getSelectedItem() == null || 
            courseComboBox.getSelectionModel().getSelectedItem().equals("No courses available") ||
            courseComboBox.getSelectionModel().getSelectedItem().equals("Select a course") ||
            selectedCourseId <= 0) {
            showAlert(AlertType.WARNING, "Validation Error", "Please select a valid course.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Handles editing an existing quiz.
     * Loads all questions and answers for the selected quiz.
     * 
     * @param quiz The quiz to edit
     */
    private void handleEditQuiz(Quiz quiz) {
        loadQuizForEditing(quiz);
    }

    /**
     * Loads a quiz for editing with all of its questions and answers.
     * Sets up the edit dialog with the quiz data.
     * 
     * @param quiz The quiz to be edited
     */
    private void loadQuizForEditing(Quiz quiz) {
        try {
            quizBeingEdited = quiz;
            
            editQuizTitleLabel.setText("Edit: " + quiz.getTitle());
            
            Course course = CourseService.getCourseById(quiz.getCourseId());
            editCourseNameLabel.setText("Course: " + (course != null ? course.getTitle() : "Unknown"));
            
            List<Question> questions = QuestionService.getQuestionsByQuizId(quiz.getId());
            
            if (questions == null || questions.isEmpty()) {
                showAlert(AlertType.WARNING, "No Questions", "This quiz doesn't have any questions yet.");
                return;
            }
            
            editingQuestions.clear();
            questionAnswers.clear();
            currentQuestionIndex = 0;
            
            editingQuestions.addAll(questions);
            
            for (Question question : editingQuestions) {
                List<Answer> answers = AnswerService.getAnswersByQuestionId(question.getId());
                if (answers != null) {
                    questionAnswers.put(question.getId(), answers);
                }
            }
            
            showQuestionForEditing(currentQuestionIndex);
            
            deleteQuestionButton.setOnAction(e -> handleDeleteQuestion());
            
            editQuizOverlay.setVisible(true);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to load quiz for editing: " + e.getMessage());
        }
    }
    
    /**
     * Shows a specific question for editing in the edit dialog.
     * Loads question text and answers into the form.
     * 
     * @param index The index of the question to show
     */
    private void showQuestionForEditing(int index) {
        if (index < 0 || index >= editingQuestions.size()) {
            return;
        }
        
        Question question = editingQuestions.get(index);
        
        editProgressLabel.setText("Question " + (index + 1) + "/" + editingQuestions.size());
        
        editQuestionTextArea.setText(question.getQuestionText());
        
        editAnswerField1.clear();
        editAnswerField2.clear();
        editAnswerField3.clear();
        editAnswerField4.clear();
        editCorrectAnswerGroup.selectToggle(null);
        
        List<Answer> answers = questionAnswers.get(question.getId());
        
        if (answers != null && !answers.isEmpty()) {
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
        
        prevQuestionButton.setDisable(index == 0);
        
        if (index == editingQuestions.size() - 1) {
            nextQuestionButton.setText("Add Question");
            nextQuestionButton.setOnAction(e -> handleAddNewQuestion());
        } else {
            nextQuestionButton.setText("Next Question");
            nextQuestionButton.setOnAction(e -> handleNextQuestion());
        }
    }
    
    /**
     * Handles moving to the previous question.
     * Saves current question changes if valid.
     */
    @FXML
    private void handlePrevQuestion() {
        if (validateEditQuestionForm()) {
            saveCurrentEditQuestion();
            currentQuestionIndex--;
            showQuestionForEditing(currentQuestionIndex);
        }
    }
    
    /**
     * Handles moving to the next question.
     * Saves current question changes if valid.
     */
    @FXML
    private void handleNextQuestion() {
        if (validateEditQuestionForm()) {
            saveCurrentEditQuestion();
            currentQuestionIndex++;
            showQuestionForEditing(currentQuestionIndex);
        }
    }
    
    /**
     * Validates the question form in the edit dialog.
     * 
     * @return true if all inputs are valid, false otherwise
     */
    private boolean validateEditQuestionForm() {
        if (editQuestionTextArea.getText() == null || editQuestionTextArea.getText().trim().isEmpty()) {
            showAlert(AlertType.WARNING, "Validation Error", "Please enter a question.");
            return false;
        }
        
        if (editAnswerField1.getText().trim().isEmpty() || editAnswerField2.getText().trim().isEmpty()) {
            showAlert(AlertType.WARNING, "Validation Error", "Please provide at least two answer options.");
            return false;
        }
        
        if (editCorrectAnswerGroup.getSelectedToggle() == null) {
            showAlert(AlertType.WARNING, "Validation Error", "Please select the correct answer.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Handles deleting the current question.
     * Shows a confirmation dialog before deletion.
     */
    @FXML
    private void handleDeleteQuestion() {
        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete this question?");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteCurrentQuestion();
            }
        });
    }
    
    /**
     * Deletes the current question and its answers from the database.
     * Updates the question list and UI.
     */
    private void deleteCurrentQuestion() {
        try {
            Question question = editingQuestions.get(currentQuestionIndex);
            
            boolean success = QuestionService.deleteQuestion(question.getId());
            
            if (!success) {
                showAlert(AlertType.ERROR, "Error", "Failed to delete the question.");
                return;
            }
            
            editingQuestions.remove(currentQuestionIndex);
            questionAnswers.remove(question.getId());
            
            if (editingQuestions.isEmpty()) {
                editQuizOverlay.setVisible(false);
                showAlert(AlertType.INFORMATION, "No Questions", 
                    "All questions have been deleted. You may add new ones from the Add Question dialog.");
                return;
            }
            
            if (currentQuestionIndex >= editingQuestions.size()) {
                currentQuestionIndex = editingQuestions.size() - 1;
            }
            
            showQuestionForEditing(currentQuestionIndex);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to delete the question: " + e.getMessage());
        }
    }
    
    /**
     * Handles saving all quiz edits and closing the dialog.
     */
    @FXML
    private void handleSaveQuizEdits() {
        if (validateEditQuestionForm()) {
            saveCurrentEditQuestion();
            
            editQuizOverlay.setVisible(false);
            
            showAlert(AlertType.INFORMATION, "Success", "All changes have been saved successfully!");
            
            loadTeacherQuizzes();
        }
    }
    
    /**
     * Handles canceling the edit dialog.
     * Shows a confirmation dialog if changes might be lost.
     */
    @FXML
    private void handleCancelEdit() {
        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Cancel Editing");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to cancel? Any unsaved changes will be lost.");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                editQuizOverlay.setVisible(false);
                
                loadTeacherQuizzes();
            }
        });
    }
    
    /**
     * Handles updating an existing quiz.
     * 
     * @param quizId The ID of the quiz to update
     */
    private void handleUpdateQuiz(int quizId) {
        if (!validateInputs()) {
            return;
        }
        
        Quiz existingQuiz = quizzesList.stream()
                                      .filter(q -> q.getId() == quizId)
                                      .findFirst()
                                      .orElse(null);
                                      
        if (existingQuiz == null) {
            showAlert(AlertType.ERROR, "Error", "Could not find the quiz to update.");
            return;
        }
        
        Quiz updatedQuiz = new Quiz();
        updatedQuiz.setId(quizId);
        updatedQuiz.setTitle(existingQuiz.getTitle());
        updatedQuiz.setDescription(existingQuiz.getDescription());
        updatedQuiz.setComment(existingQuiz.getComment());
        updatedQuiz.setCourseId(selectedCourseId);
        
        boolean success = QuizService.updateQuiz(updatedQuiz);
        
        if (success) {
            addQuizOverlay.setVisible(false);
            
            if (saveButton != null) {
                saveButton.setOnAction(this::handleSave);
            }
            
            loadTeacherQuizzes();
            
            showAlert(AlertType.INFORMATION, "Success", "Quiz updated successfully!");
        } else {
            showAlert(AlertType.ERROR, "Error", "Failed to update the quiz.");
        }
    }

    /**
     * Handles the cancel button action in the question dialog.
     * 
     * @param event The ActionEvent
     */
    @FXML
    private void handleCancelQuestion(ActionEvent event) {
        if (addQuestionOverlay != null) {
            addQuestionOverlay.setVisible(false);
        }
        
        courseComboBox.getItems().clear();
        
        loadTeacherQuizzes();
    }

    /**
     * Handles adding another question to the quiz.
     * Validates and saves the current question, then resets the form.
     * 
     * @param event The ActionEvent
     */
    @FXML
    private void handleAddAnotherQuestion(ActionEvent event) {
        if (validateQuestionForm()) {
            saveCurrentQuestion();
            resetQuestionForm();
            showAlert(AlertType.INFORMATION, "Success", "Question added successfully! Add another question or click Done when finished.");
        }
    }
    
    /**
     * Handles completing the question entry process.
     * Saves the current question if valid and finalizes the quiz creation.
     * 
     * @param event The ActionEvent
     */
    @FXML
    private void handleDoneAddingQuestions(ActionEvent event) {
        if (validateQuestionForm()) {
            saveCurrentQuestion();
        }
        
        if (currentQuizQuestions.isEmpty()) {
            showAlert(AlertType.WARNING, "No Questions", "Please add at least one question before finishing.");
            return;
        }
        
        if (isNewQuiz) {
            boolean success = QuizService.addQuiz(currentQuiz);
            if (!success) {
                showAlert(AlertType.ERROR, "Error", "Failed to save the quiz to the database.");
                return;
            }
            
            List<Quiz> latestQuizzes = QuizService.getQuizzesByTeacherId(currentUser.getId());
            Quiz createdQuiz = null;
            
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
            
            currentQuiz.setId(createdQuiz.getId());
            
            saveAllQuestionsAndAnswers();
        }
        
        addQuestionOverlay.setVisible(false);
        
        courseComboBox.getItems().clear();
        
        if (isNewQuiz) {
            showAlert(AlertType.INFORMATION, "Quiz Complete", 
                "Quiz has been created and questions have been saved successfully!");
        } else {
            showAlert(AlertType.INFORMATION, "Questions Saved", 
                "All questions have been saved successfully!");
        }
        
        loadTeacherQuizzes();
        
        resetQuestionForm();
        currentQuizQuestions.clear();
    }

    /**
     * Shows the question entry form for adding questions to a quiz.
     * 
     * @param quiz The quiz to add questions to
     * @param isNewQuiz Flag indicating if this is a new quiz that hasn't been saved yet
     */
    public void showAddQuestionDialog(Quiz quiz, boolean isNewQuiz) {
        this.currentQuiz = quiz;
        this.isNewQuiz = isNewQuiz;
        resetQuestionForm();
        addQuestionOverlay.setVisible(true);
    }
    
    /**
     * Validates the question form inputs.
     * 
     * @return true if all inputs are valid, false otherwise
     */
    private boolean validateQuestionForm() {
        if (questionTextField.getText() == null || questionTextField.getText().trim().isEmpty()) {
            showAlert(AlertType.WARNING, "Validation Error", "Please enter a question.");
            return false;
        }
        
        if (answerField1.getText().trim().isEmpty() || answerField2.getText().trim().isEmpty()) {
            showAlert(AlertType.WARNING, "Validation Error", "Please provide at least two answer options.");
            return false;
        }
        
        if (correctAnswerGroup.getSelectedToggle() == null) {
            showAlert(AlertType.WARNING, "Validation Error", "Please select the correct answer.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Resets the question form fields.
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
     * Saves the current question and its answers.
     * For new quizzes, stores questions temporarily until the quiz is created.
     */
    private void saveCurrentQuestion() {
        try {
            if (isNewQuiz) {
                Question question = new Question();
                question.setId(-1 * (currentQuizQuestions.size() + 1));
                question.setQuestionText(questionTextField.getText().trim());
                
                currentQuizQuestions.add(question);
                
                storeAnswersForQuestion(question);
                
                return;
            }
            
            Question question = new Question();
            question.setQuizId(currentQuiz.getId());
            question.setQuestionText(questionTextField.getText().trim());
            
            int questionId = QuestionService.addQuestion(question);
            
            if (questionId <= 0) {
                showAlert(AlertType.ERROR, "Error", "Failed to save the question to the database.");
                return;
            }
            
            question.setId(questionId);
            
            currentQuizQuestions.add(question);
            
            saveAnswers(questionId);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to save the question: " + e.getMessage());
        }
    }
    
    /**
     * Stores answers for a question in memory for new quizzes.
     * 
     * @param question The question to store answers for
     */
    private void storeAnswersForQuestion(Question question) {
        RadioButton selectedRadio = (RadioButton) correctAnswerGroup.getSelectedToggle();
        
        if (!answerField1.getText().trim().isEmpty()) {
            Answer answer = new Answer();
            answer.setQuestionId(question.getId());
            answer.setAnswerText(answerField1.getText().trim());
            answer.setCorrect(selectedRadio == radioAnswer1);
            question.addAnswer(answer);
        }
        
        if (!answerField2.getText().trim().isEmpty()) {
            Answer answer = new Answer();
            answer.setQuestionId(question.getId());
            answer.setAnswerText(answerField2.getText().trim());
            answer.setCorrect(selectedRadio == radioAnswer2);
            question.addAnswer(answer);
        }
        
        if (!answerField3.getText().trim().isEmpty()) {
            Answer answer = new Answer();
            answer.setQuestionId(question.getId());
            answer.setAnswerText(answerField3.getText().trim());
            answer.setCorrect(selectedRadio == radioAnswer3);
            question.addAnswer(answer);
        }
        
        if (!answerField4.getText().trim().isEmpty()) {
            Answer answer = new Answer();
            answer.setQuestionId(question.getId());
            answer.setAnswerText(answerField4.getText().trim());
            answer.setCorrect(selectedRadio == radioAnswer4);
            question.addAnswer(answer);
        }
    }
    
    /**
     * Saves the answers for a question to the database.
     * 
     * @param questionId The ID of the question to save answers for
     */
    private void saveAnswers(int questionId) {
        RadioButton selectedRadio = (RadioButton) correctAnswerGroup.getSelectedToggle();
        
        if (!answerField1.getText().trim().isEmpty()) {
            Answer answer = new Answer();
            answer.setQuestionId(questionId);
            answer.setAnswerText(answerField1.getText().trim());
            answer.setCorrect(selectedRadio == radioAnswer1);
            AnswerService.addAnswer(answer);
        }
        
        if (!answerField2.getText().trim().isEmpty()) {
            Answer answer = new Answer();
            answer.setQuestionId(questionId);
            answer.setAnswerText(answerField2.getText().trim());
            answer.setCorrect(selectedRadio == radioAnswer2);
            AnswerService.addAnswer(answer);
        }
        
        if (!answerField3.getText().trim().isEmpty()) {
            Answer answer = new Answer();
            answer.setQuestionId(questionId);
            answer.setAnswerText(answerField3.getText().trim());
            answer.setCorrect(selectedRadio == radioAnswer3);
            AnswerService.addAnswer(answer);
        }
        
        if (!answerField4.getText().trim().isEmpty()) {
            Answer answer = new Answer();
            answer.setQuestionId(questionId);
            answer.setAnswerText(answerField4.getText().trim());
            answer.setCorrect(selectedRadio == radioAnswer4);
            AnswerService.addAnswer(answer);
        }
    }
    
    /**
     * Saves all temporarily stored questions and their answers to the database.
     */
    private void saveAllQuestionsAndAnswers() {
        if (currentQuizQuestions.isEmpty() || currentQuiz == null || currentQuiz.getId() <= 0) {
            return;
        }
        
        try {
            for (Question question : currentQuizQuestions) {
                if (question.getId() > 0) {
                    continue;
                }
                
                Question dbQuestion = new Question();
                dbQuestion.setQuizId(currentQuiz.getId());
                dbQuestion.setQuestionText(question.getQuestionText());
                
                int questionId = QuestionService.addQuestion(dbQuestion);
                
                if (questionId <= 0) {
                    System.out.println("Failed to save question: " + question.getQuestionText());
                    continue;
                }
                
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
     * Handles deleting a quiz after confirmation.
     * 
     * @param quiz The quiz to delete
     */
    private void handleDeleteQuiz(Quiz quiz) {
        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete the quiz: " + quiz.getTitle() + "?");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = QuizService.deleteQuiz(quiz.getId());
                
                if (success) {
                    loadTeacherQuizzes();
                    showAlert(AlertType.INFORMATION, "Success", "Quiz deleted successfully");
                } else {
                    showAlert(AlertType.ERROR, "Error", "Failed to delete quiz");
                }
            }
        });
    }
    
    /**
     * Handles the search action.
     * 
     * @param event The ActionEvent
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField.getText().trim();
        filterQuizzes(query);
    }
    
    /**
     * Shows an alert dialog with the specified type, title, and message.
     * 
     * @param alertType The type of alert to show
     * @param title The title of the alert
     * @param message The message to display
     */
    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Handles adding a new question to the quiz.
     * Creates a new empty question and displays it for editing.
     */
    private void handleAddNewQuestion() {
        if (validateEditQuestionForm()) {
            saveCurrentEditQuestion();
            
            Question newQuestion = new Question();
            newQuestion.setQuizId(quizBeingEdited.getId());
            newQuestion.setQuestionText("New Question");
            
            int questionId = QuestionService.addQuestion(newQuestion);
            if (questionId > 0) {
                newQuestion.setId(questionId);
                
                editingQuestions.add(newQuestion);
                questionAnswers.put(questionId, new ArrayList<>());
                
                currentQuestionIndex = editingQuestions.size() - 1;
                showQuestionForEditing(currentQuestionIndex);
            } else {
                showAlert(AlertType.ERROR, "Error", "Failed to add new question.");
            }
        }
    }

    /**
     * Handles viewing quiz results.
     * Loads and displays the quiz results view.
     * 
     * @param quiz The quiz to view results for
     */
    private void handleViewResults(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TeacherQuizResults.fxml"));
            Parent resultsView = loader.load();
            
            TeacherQuizResultsController controller = loader.getController();
            controller.setQuiz(quiz);
            
            StackPane contentArea = (StackPane) quizCardsContainer.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(resultsView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to load quiz results view: " + e.getMessage());
        }
    }

    /**
     * Saves changes to the current question being edited.
     * Updates the question text and answers in the database.
     */
    private void saveCurrentEditQuestion() {
        try {
            Question question = editingQuestions.get(currentQuestionIndex);
            
            question.setQuestionText(editQuestionTextArea.getText().trim());
            
            QuestionService.updateQuestion(question);
            
            RadioButton selectedRadio = (RadioButton) editCorrectAnswerGroup.getSelectedToggle();
            
            List<Answer> existingAnswers = questionAnswers.get(question.getId());
            if (existingAnswers == null) {
                existingAnswers = new ArrayList<>();
            }
            
            List<Answer> updatedAnswers = new ArrayList<>();
            
            if (!editAnswerField1.getText().trim().isEmpty()) {
                Answer answer = findOrCreateAnswer(existingAnswers, 0);
                answer.setQuestionId(question.getId());
                answer.setAnswerText(editAnswerField1.getText().trim());
                answer.setCorrect(selectedRadio == editRadioAnswer1);
                updatedAnswers.add(answer);
            }
            
            if (!editAnswerField2.getText().trim().isEmpty()) {
                Answer answer = findOrCreateAnswer(existingAnswers, 1);
                answer.setQuestionId(question.getId());
                answer.setAnswerText(editAnswerField2.getText().trim());
                answer.setCorrect(selectedRadio == editRadioAnswer2);
                updatedAnswers.add(answer);
            }
            
            if (!editAnswerField3.getText().trim().isEmpty()) {
                Answer answer = findOrCreateAnswer(existingAnswers, 2);
                answer.setQuestionId(question.getId());
                answer.setAnswerText(editAnswerField3.getText().trim());
                answer.setCorrect(selectedRadio == editRadioAnswer3);
                updatedAnswers.add(answer);
            }
            
            if (!editAnswerField4.getText().trim().isEmpty()) {
                Answer answer = findOrCreateAnswer(existingAnswers, 3);
                answer.setQuestionId(question.getId());
                answer.setAnswerText(editAnswerField4.getText().trim());
                answer.setCorrect(selectedRadio == editRadioAnswer4);
                updatedAnswers.add(answer);
            }
            
            for (Answer existingAnswer : existingAnswers) {
                boolean stillExists = false;
                for (Answer updatedAnswer : updatedAnswers) {
                    if (updatedAnswer.getId() == existingAnswer.getId()) {
                        stillExists = true;
                        break;
                    }
                }
                if (!stillExists) {
                    AnswerService.deleteAnswer(existingAnswer.getId());
                }
            }
            
            for (Answer answer : updatedAnswers) {
                if (answer.getId() > 0) {
                    AnswerService.updateAnswer(answer);
                } else {
                    AnswerService.addAnswer(answer);
                }
            }
            
            questionAnswers.put(question.getId(), updatedAnswers);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to save question changes: " + e.getMessage());
        }
    }
    
    /**
     * Finds an existing answer or creates a new one.
     * 
     * @param existingAnswers List of existing answers
     * @param index The index of the answer to find
     * @return An existing answer or a new one if not found
     */
    private Answer findOrCreateAnswer(List<Answer> existingAnswers, int index) {
        if (existingAnswers != null && index < existingAnswers.size()) {
            return existingAnswers.get(index);
        }
        return new Answer();
    }
}