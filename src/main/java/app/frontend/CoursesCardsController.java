package app.frontend;

import app.backend.models.Course;
import app.backend.models.User;
import app.backend.services.CourseService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CoursesCardsController implements Initializable {

    @FXML private FlowPane courseCardsContainer;
    @FXML private TextField searchField;
    @FXML private Button manageCourseButton;
    
    private User currentUser;
    private ObservableList<Course> coursesList = FXCollections.observableArrayList();
    private final BooleanProperty isTeacher = new SimpleBooleanProperty(false);

    public BooleanProperty isTeacherProperty() {
        return isTeacher;
    }

    public boolean getIsTeacher() {
        return isTeacher.get();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current logged in user
        currentUser = LoginController.getCurrentUser();
        
        // Set teacher property based on user role
        if (currentUser != null) {
            isTeacher.set(currentUser.getRole().equals("teacher"));
        }
        
        // Bind the manage button visibility to isTeacher property
        manageCourseButton.visibleProperty().bind(isTeacher);
        
        // Load all available courses
        loadAllCourses();
        
        // Setup search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterCourses(newValue);
        });
    }
    
    /**
     * Loads all available courses and displays them as cards
     */
    private void loadAllCourses() {
        // Clear the container
        courseCardsContainer.getChildren().clear();
        
        // Get all courses from the service
        List<Course> allCourses = CourseService.getAllCourses();
        coursesList.setAll(allCourses);
        
        // Create and add a card for each course
        for (Course course : coursesList) {
            courseCardsContainer.getChildren().add(createCourseCard(course));
        }
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

        // Instructor info section
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

        String teacherName = "Kerdoudi Mohamed Lamine"; // todo get teacher name from database
        Label instructorLabel = new Label(teacherName);
        instructorLabel.getStyleClass().add("instructor-name");

        instructorBox.getChildren().addAll(instructorIcon, instructorLabel);

        // Button section
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button viewButton = new Button("View Course");
        viewButton.getStyleClass().add("view-course-button");
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

        // Add accessibility features
        cardPane.setAccessibleText("Course: " + course.getTitle() + ", " + description);

        return cardPane;
    }
    
    /**
     * Handles the action when a user clicks on a course card to view details
     */
    private void handleViewCourseDetails(Course course) {
        // Alert for testing - this would navigate to course pdf in full implementation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Course Details");
        alert.setHeaderText("Viewing: " + course.getTitle());
        alert.setContentText(course.getDescription());
        alert.showAndWait();
    }

    /**
     * Handles the search action
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField.getText().trim();
        filterCourses(query);
    }
    
    /**
     * Filters courses based on search query
     */
    private void filterCourses(String query) {
        if (query == null || query.isEmpty()) {
            // If query is empty, show all courses
            courseCardsContainer.getChildren().clear();
            for (Course course : coursesList) {
                courseCardsContainer.getChildren().add(createCourseCard(course));
            }
        } else {
            // Filter courses based on query
            query = query.toLowerCase();
            final String searchQuery = query;
            
            List<Course> filteredList = coursesList.stream()
                .filter(course -> 
                    course.getTitle().toLowerCase().contains(searchQuery) ||
                    course.getDescription().toLowerCase().contains(searchQuery))
                .collect(Collectors.toList());
            
            courseCardsContainer.getChildren().clear();
            for (Course course : filteredList) {
                courseCardsContainer.getChildren().add(createCourseCard(course));
            }
        }
    }
    
    /**
     * Navigates to the course management page
     */
    @FXML
    private void handleManageCourses(ActionEvent event) {
        try {
            // Load the course management view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/courses.fxml"));
            Parent courseManagementView = loader.load();
            
            // Replace the current view with the management view
            courseCardsContainer.getScene().setRoot(courseManagementView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to open course management page.");
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