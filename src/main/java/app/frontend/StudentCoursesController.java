package app.frontend;

import app.backend.models.Course;
import app.backend.models.User;
import app.backend.services.CourseService;
import app.backend.services.FavoriteCoursesService;
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
 * Controller for the student courses view that displays a teacher's courses.
 * This view allows students to browse courses offered by a specific teacher,
 * search for courses, and navigate to view individual course details.
 *
 * @author Sellami Mohamed Odai
 */
public class StudentCoursesController implements Initializable {

    @FXML private FlowPane courseCardsContainer;
    @FXML private Label teacherNameLabel;
    @FXML private TextField searchField;
    @FXML private ImageView teacherProfileImage;
    
    private User currentUser;
    private User teacher;
    private List<Course> teacherCourses = new ArrayList<>();
    
    /**
     * Initializes the controller.
     * Sets up the current user and search field listener.
     *
     * @param location The location used to resolve relative paths for the root object
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = AuthLoginController.getCurrentUser();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterCourses(newValue));
    }
    
    /**
     * Sets the teacher and loads their courses.
     * Updates the UI with teacher information and loads courses appropriate for the student's level.
     *
     * @param teacher The teacher whose courses to display
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
        
        loadTeacherCourses();
    }
    
    /**
     * Loads courses for this teacher filtered by student level.
     * Retrieves courses based on teacher ID and student enrollment level.
     */
    private void loadTeacherCourses() {
        if (currentUser != null && currentUser.getRole().equals("student")) {
            teacherCourses = CourseService.getCoursesByTeacherAndLevel(teacher.getId(), currentUser.getEnrollmentLevel());
        } else {
            teacherCourses = CourseService.getCoursesByTeacherId(teacher.getId());
        }
        
        displayCourses(teacherCourses);
    }
    
    /**
     * Displays the provided list of courses as cards.
     * If no courses are available, displays a message instead.
     *
     * @param courses The list of courses to display
     */
    private void displayCourses(List<Course> courses) {
        courseCardsContainer.getChildren().clear();
        
        if (courses.isEmpty()) {
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
     * Filters courses by title or description based on search text.
     *
     * @param searchText The text to search for in course titles and descriptions
     */
    private void filterCourses(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            displayCourses(teacherCourses);
        } else {
            List<Course> filteredCourses = teacherCourses.stream()
                .filter(course -> 
                    course.getTitle().toLowerCase().contains(searchText.toLowerCase()) ||
                    (course.getDescription() != null && course.getDescription().toLowerCase().contains(searchText.toLowerCase())))
                .collect(Collectors.toList());
            
            displayCourses(filteredCourses);
        }
    }
    
    /**
     * Handles the search action when Enter is pressed.
     *
     * @param event The action event
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        filterCourses(searchField.getText());
    }
    
    /**
     * Creates a visual card representation for a course.
     * The card includes title, description, teacher information, and a button to view details.
     *
     * @param course The course to create a card for
     * @return A StackPane containing the course card UI
     */
    private StackPane createCourseCard(Course course) {
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
            System.out.println("Failed to load background image for course card");
        }

        VBox cardContent = new VBox();
        cardContent.getStyleClass().add("card-content");
        cardContent.setSpacing(20);
        cardContent.setPadding(new Insets(20, 20, 20, 20));
        cardContent.setPrefWidth(480);
        cardContent.setPrefHeight(230);

        HBox headerBox = createHeaderBox(course);

        Label descriptionLabel = createDescriptionLabel(course);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox footerBox = createFooterBox(course);

        cardContent.getChildren().addAll(headerBox, descriptionLabel, spacer, footerBox);
        cardPane.getChildren().addAll(cardBackground, cardContent);
        cardPane.setOnMouseClicked(e -> handleViewCourseDetails(course));

        return cardPane;
    }
    
    /**
     * Creates the header box for a course card.
     *
     * @param course The course to create the header for
     * @return An HBox containing the course title and logo
     */
    private HBox createHeaderBox(Course course) {
        HBox headerBox = new HBox();
        headerBox.getStyleClass().add("card-header");
        headerBox.setAlignment(Pos.TOP_LEFT);
        headerBox.setPrefWidth(480);
        headerBox.setSpacing(20);

        VBox titleContainer = new VBox();
        titleContainer.setAlignment(Pos.TOP_LEFT);
        titleContainer.setPrefWidth(390);
        HBox.setHgrow(titleContainer, Priority.ALWAYS);

        Label titleLabel = new Label(course.getTitle());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);
        titleLabel.setMinHeight(Region.USE_PREF_SIZE);
        titleContainer.getChildren().add(titleLabel);

        VBox logoContainer = new VBox();
        logoContainer.setSpacing(10);
        logoContainer.setAlignment(Pos.TOP_CENTER);

        ImageView courseIcon = new ImageView();
        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/courseCardLogo.png"));
            courseIcon.setImage(logo);
            courseIcon.setFitWidth(50);
            courseIcon.setFitHeight(50);
            courseIcon.getStyleClass().add("course-icon");
        } catch (Exception e) {
            System.out.println("Failed to load logo for course: " + course.getTitle());
        }

        logoContainer.getChildren().addAll(courseIcon);
        headerBox.getChildren().addAll(titleContainer, logoContainer);
        return headerBox;
    }

    /**
     * Creates the description label for a course card.
     *
     * @param course The course to create the description for
     * @return A Label containing the course description
     */
    private Label createDescriptionLabel(Course course) {
        String description = course.getDescription();
        if (description == null || description.isEmpty()) {
            description = "No description available";
        }
        
        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("card-description");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMinHeight(Region.USE_PREF_SIZE);
        
        return descriptionLabel;
    }

    /**
     * Creates the footer box for a course card.
     *
     * @param course The course to create the footer for
     * @return An HBox containing the instructor info and view button
     */
    private HBox createFooterBox(Course course) {
        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.BOTTOM_LEFT);
        footerBox.setPrefWidth(480);

        HBox instructorBox = new HBox();
        instructorBox.getStyleClass().add("card-instructor");
        instructorBox.setAlignment(Pos.CENTER_LEFT);
        instructorBox.setPrefHeight(30);
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

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        ToggleButton favoriteButton = new ToggleButton();
        favoriteButton.getStyleClass().add("favorite-button");
        favoriteButton.setFocusTraversable(false);
        
        ImageView starIcon = new ImageView();
        try {
            boolean isFavorite = FavoriteCoursesService.isFavoriteCourse(currentUser.getId(), course.getId());
            Image starImage = new Image(getClass().getResourceAsStream(isFavorite ? "/images/star-active.png" : "/images/star.png"));
            starIcon.setImage(starImage);
            starIcon.setFitWidth(24);
            starIcon.setFitHeight(26);
            favoriteButton.setGraphic(starIcon);
            favoriteButton.setSelected(isFavorite);
        } catch (Exception e) {
            System.out.println("Failed to load star icon for course: " + course.getTitle());
        }

        favoriteButton.setOnAction(event -> {
            boolean isNowFavorite = favoriteButton.isSelected();
            boolean success;
            
            if (isNowFavorite) {
                success = FavoriteCoursesService.addFavoriteCourse(currentUser.getId(), course.getId());
            } else {
                success = FavoriteCoursesService.removeFavoriteCourse(currentUser.getId(), course.getId());
            }
            
            if (success) {
                try {
                    Image newStarImage = new Image(getClass().getResourceAsStream(
                        isNowFavorite ? "/images/star-active.png" : "/images/star.png"
                    ));
                    starIcon.setImage(newStarImage);
                } catch (Exception e) {
                    System.out.println("Failed to update star icon");
                }
            } else {
                favoriteButton.setSelected(!isNowFavorite);
            }
        });

        Button viewButton = new Button("View Course");
        viewButton.getStyleClass().add("view-course-button");
        viewButton.setStyle("-fx-background-color: #65558f; -fx-background-radius: 8px;");
        viewButton.setPrefWidth(110);
        viewButton.setPrefHeight(24);
        viewButton.setOnAction(e -> handleViewCourseDetails(course));

        buttonBox.setSpacing(4);
        buttonBox.getChildren().addAll(favoriteButton, viewButton);

        footerBox.getChildren().addAll(instructorBox, buttonBox);
        return footerBox;
    }
    
    /**
     * Handles the action when a user clicks on a course card to view details.
     * Checks if the course has a PDF file and loads the appropriate viewer.
     *
     * @param course The course to view details for
     */
    private void handleViewCourseDetails(Course course) {
        try {
            if (course.getPdfPath() == null || course.getPdfPath().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "No PDF Available", 
                    "This course does not have a PDF file attached.");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PdfCourseViewer.fxml"));
            Parent courseViewerParent = loader.load();
            
            ViewCourseController controller = loader.getController();
            controller.setCourse(course);
            
            StackPane contentArea = (StackPane) teacherNameLabel.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(courseViewerParent);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load course viewer: " + e.getMessage());
        }
    }
    
    /**
     * Navigate back to the teachers view.
     * Loads the TeachersCards view and sets appropriate flags based on user role.
     *
     * @param event The action event
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
             
            StackPane contentArea = (StackPane) teacherNameLabel.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(teachersView);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to go back to teachers view.");
        }
    }
    
    /**
     * Helper method to show alerts.
     *
     * @param alertType The type of alert (e.g., ERROR, WARNING)
     * @param title The title of the alert
     * @param message The message to display in the alert
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}