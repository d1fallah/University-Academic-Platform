package app.frontend;

import app.backend.models.Course;
import app.backend.models.User;
import app.backend.services.AuthService;
import app.backend.services.CourseService;
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

public class TeacherCoursesController implements Initializable {

    @FXML private FlowPane courseCardsContainer;
    @FXML private Label teacherNameLabel;
    @FXML private TextField searchField;
    @FXML private ImageView teacherProfileImage;
    @FXML private TextField searchTextField;
    @FXML private Button showAllButton;
    @FXML private Button showMyCoursesButton;
    
    private User currentUser;
    private User teacher;
    private List<Course> teacherCourses = new ArrayList<>();

    private Button backToTeachersButton;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current logged in user
        currentUser = LoginController.getCurrentUser();
        
        // Set up search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterCourses(newValue);
        });
    }
    
    /**
     * Set the teacher and load their courses
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
        
        // Load courses for this teacher filtered by student level
        loadTeacherCourses();
    }
    
    /**
     * Loads courses for this teacher filtered by student level
     */
    private void loadTeacherCourses() {
        // Get courses filtered by teacher ID and student enrollment level
        if (currentUser != null && currentUser.getRole().equals("student")) {
            teacherCourses = CourseService.getCoursesByTeacherAndLevel(teacher.getId(), currentUser.getEnrollmentLevel());
        } else {
            teacherCourses = CourseService.getCoursesByTeacherId(teacher.getId());
        }
        
        // Display the courses
        displayCourses(teacherCourses);
    }
    
    /**
     * Displays the provided list of courses as cards
     */
    private void displayCourses(List<Course> courses) {
        // Clear the container
        courseCardsContainer.getChildren().clear();
        
        // Create and add a card for each course
        if (courses.isEmpty()) {
            // Display "No courses available" message
            Label noCoursesLabel = new Label("No courses available for your enrollment level from this teacher yet.");
            noCoursesLabel.getStyleClass().add("no-courses-message");
            noCoursesLabel.setPrefWidth(courseCardsContainer.getPrefWidth());
            noCoursesLabel.setPrefHeight(200);
            noCoursesLabel.setAlignment(Pos.CENTER);
            noCoursesLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
            
            courseCardsContainer.getChildren().add(noCoursesLabel);
        } else {
            for (Course course : courses) {
                courseCardsContainer.getChildren().add(createCourseCard(course));
            }
        }
    }
    
    /**
     * Filters courses by title based on search text
     */
    private void filterCourses(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            // If search is empty, show all teacher's courses
            displayCourses(teacherCourses);
        } else {
            // Filter courses based on title or description containing search text
            List<Course> filteredCourses = teacherCourses.stream()
                .filter(course -> 
                    course.getTitle().toLowerCase().contains(searchText.toLowerCase()) ||
                    (course.getDescription() != null && course.getDescription().toLowerCase().contains(searchText.toLowerCase())))
                .collect(Collectors.toList());
            
            displayCourses(filteredCourses);
        }
    }
    
    /**
     * Handles the search action when Enter is pressed
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        filterCourses(searchField.getText());
    }
    
    /**
     * Creates a visual card representation for a course
     */
    private StackPane createCourseCard(Course course) {
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
            System.out.println("Failed to load background image for course card");
        }

        // Card layout container
        VBox cardContent = new VBox();
        cardContent.getStyleClass().add("card-content");
        cardContent.setSpacing(20);
        cardContent.setPadding(new Insets(20, 20, 20, 20));
        cardContent.setPrefWidth(480);
        cardContent.setPrefHeight(230);

        // Top section with course title and logo
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
        Label titleLabel = new Label(course.getTitle());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);
        titleLabel.setMinHeight(Region.USE_PREF_SIZE);

        titleContainer.getChildren().add(titleLabel);

        // Course logo/icon
        StackPane logoContainer = new StackPane();
        logoContainer.setMinWidth(50);
        logoContainer.setMaxWidth(50);
        logoContainer.setPrefHeight(50);
        logoContainer.setAlignment(Pos.TOP_CENTER);
        logoContainer.getStyleClass().add("logo-container");

        ImageView courseIcon = new ImageView();
        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/courseCardLogo.png"));
            courseIcon.setImage(logo);
        } catch (Exception e) {
            System.out.println("Failed to load logo for course: " + course.getTitle());
        }
        courseIcon.setFitWidth(50);
        courseIcon.setFitHeight(50);
        courseIcon.getStyleClass().add("course-icon");

        logoContainer.getChildren().add(courseIcon);

        // Add title and logo to header box
        headerBox.getChildren().addAll(titleContainer, logoContainer);

        // Course description
        String description = course.getDescription();
        if (description == null || description.isEmpty()) {
            description = "No description available";
        }
        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("card-description");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMinHeight(Region.USE_PREF_SIZE);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Footer section with instructor info and button
        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.BOTTOM_LEFT);
        footerBox.setPrefWidth(480);

        // Instructor info section - now showing the teacher's name
        HBox instructorBox = new HBox();
        instructorBox.getStyleClass().add("card-instructor");
        instructorBox.setAlignment(Pos.CENTER_LEFT);
        instructorBox.setPrefHeight(30);
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

        Button viewButton = new Button("View Course");
        viewButton.getStyleClass().add("view-course-button");
        viewButton.setStyle("-fx-background-color: #65558f;");
        viewButton.setPrefWidth(110);
        viewButton.setPrefHeight(24);
        viewButton.setOnAction(e -> handleViewCourseDetails(course));

        buttonBox.getChildren().add(viewButton);

        // Add instructor and button to footer
        footerBox.getChildren().addAll(instructorBox, buttonBox);

        // Add all sections to the card
        cardContent.getChildren().addAll(headerBox, descriptionLabel, spacer, footerBox);

        // Stack the background and content
        cardPane.getChildren().addAll(cardBackground, cardContent);

        // Make the entire card clickable
        cardPane.setOnMouseClicked(e -> handleViewCourseDetails(course));

        return cardPane;
    }
    
    /**
     * Handles the action when a user clicks on a course card to view details
     */
    private void handleViewCourseDetails(Course course) {
        try {
            // Check if the course has a PDF file
            if (course.getPdfPath() == null || course.getPdfPath().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "No PDF Available", 
                    "This course does not have a PDF file attached.");
                return;
            }
            
            // Load the course viewer view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/course-viewer.fxml"));
            Parent courseViewerParent = loader.load();
            
            // Get the controller and set the course
            CourseViewerController controller = loader.getController();
            controller.setCourse(course);
            
            // Get the main layout's content area and set the course viewer
            StackPane contentArea = (StackPane) teacherNameLabel.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(courseViewerParent);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load course viewer: " + e.getMessage());
        }
    }
    
    /**
     * Navigate back to the teachers view
     */
    @FXML
    private void handleBackToTeachers(ActionEvent event) {
        try {
            // Load the teachers cards view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teachers-cards.fxml"));
            Parent teachersView = loader.load();
            
            // Get the controller and set flag to exclude current teacher if user is a teacher
            TeachersCardsController controller = loader.getController();
            if (currentUser != null && currentUser.getRole().equals("teacher")) {
                controller.setExcludeCurrentTeacher(true);
                controller.setShowManageCourseButton(true);
            }
            
            // Check if we're in the Exercises section by looking at the viewTitleLabel's text or URL
            boolean isExerciseSection = false;
            try {
                Label viewTitle = (Label) teacherNameLabel.getScene().lookup("#viewTitleLabel");
                if (viewTitle != null && viewTitle.getText().contains("Exercises")) {
                    isExerciseSection = true;
                }
            } catch (Exception e) {
                // Fallback to checking the URL in case the label lookup fails
                String url = teacherNameLabel.getScene().getWindow().toString();
                isExerciseSection = url != null && url.contains("exercise");
            }
            
            // Set appropriate view flag
            if (isExerciseSection) {
                controller.setIsExerciseView(true);
            }
            
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