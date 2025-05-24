package app.frontend;

import app.backend.models.Course;
import app.backend.models.User;
import app.backend.services.FavoriteCoursesService;
import app.backend.services.AuthService;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the Saved Courses view that displays a user's favorite courses.
 * This view allows users to browse their saved courses, search through them,
 * remove courses from favorites, and navigate to view individual course details.
 *
 * @author Sellami Mohamed Odai
 */
public class SavedCoursesController implements Initializable {
    /** Container for displaying course cards */
    @FXML private FlowPane courseCardsContainer;
    
    /** Search field for filtering courses */
    @FXML private TextField searchField;
    
    /** Currently logged in user */
    private User currentUser;
    
    /** List of user's favorite courses */
    private List<Course> favoriteCourses = new ArrayList<>();

    /**
     * Initializes the controller, loads current user's favorite courses
     * and sets up search functionality.
     *
     * @param location The location used to resolve relative paths for the root object
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = AuthLoginController.getCurrentUser();
        if (currentUser == null) return;
        
        loadFavoriteCourses();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> 
            filterCourses(newValue));
    }
    
    /**
     * Loads all favorite courses for the current user
     */
    private void loadFavoriteCourses() {
        favoriteCourses = FavoriteCoursesService.getFavoriteCourses(currentUser.getId());
        displayCourses(favoriteCourses);
    }
    
    /**
     * Displays the provided list of courses as cards.
     * Shows a message if no courses are available.
     *
     * @param courses The list of courses to display
     */
    private void displayCourses(List<Course> courses) {
        courseCardsContainer.getChildren().clear();
        
        if (courses.isEmpty()) {
            Label noCoursesLabel = new Label("You haven't saved any favorite courses yet.");
            noCoursesLabel.getStyleClass().add("no-courses-message");
            noCoursesLabel.setPrefWidth(courseCardsContainer.getPrefWidth());
            noCoursesLabel.setPrefHeight(200);
            noCoursesLabel.setAlignment(Pos.CENTER);
            noCoursesLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
            
            courseCardsContainer.getChildren().add(noCoursesLabel);
        } else {
            for (Course course : courses) {
                User teacher = AuthService.getUserById(course.getTeacherId());
                courseCardsContainer.getChildren().add(createCourseCard(course, teacher));
            }
        }
    }
    
    /**
     * Filters courses by title or description based on search text.
     * Shows all courses if search text is empty.
     *
     * @param searchText The text to search for in course titles and descriptions
     */
    private void filterCourses(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            displayCourses(favoriteCourses);
        } else {
            List<Course> filteredCourses = favoriteCourses.stream()
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
     * Card includes title, description, instructor info, and favorite/view buttons.
     *
     * @param course The course to create a card for
     * @param teacher The teacher who created the course
     * @return A StackPane containing the course card UI
     */
    private StackPane createCourseCard(Course course, User teacher) {
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
            // Image loading failed, continue without background
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

        HBox footerBox = createFooterBox(course, teacher);

        cardContent.getChildren().addAll(headerBox, descriptionLabel, spacer, footerBox);
        cardPane.getChildren().addAll(cardBackground, cardContent);
        cardPane.setOnMouseClicked(e -> handleViewCourseDetails(course));
        
        return cardPane;
    }
    
    /**
     * Creates the header section of a course card with title and logo.
     *
     * @param course The course to create a header for
     * @return HBox containing the header components
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
            // Logo loading failed, continue without logo
        }
        
        logoContainer.getChildren().addAll(courseIcon);
        headerBox.getChildren().addAll(titleContainer, logoContainer);
        
        return headerBox;
    }
    
    /**
     * Creates the description label for a course card.
     * Uses a default message if no description is available.
     *
     * @param course The course to create a description for
     * @return Label containing the course description
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
     * Creates the footer section of a course card with instructor info and action buttons.
     *
     * @param course The course to create a footer for
     * @param teacher The teacher who created the course
     * @return HBox containing the footer components
     */
    private HBox createFooterBox(Course course, User teacher) {
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

        ImageView instructorIcon = new ImageView();
        try {
            Image instructorImg = new Image(getClass().getResourceAsStream("/images/Case.png"));
            instructorIcon.setImage(instructorImg);
            instructorIcon.setFitHeight(20);
            instructorIcon.setFitWidth(20);
            instructorIcon.setPreserveRatio(true);
        } catch (Exception e) {
            // Icon loading failed, continue without icon
        }
        
        Label instructorLabel = new Label("Prof. " + (teacher != null ? teacher.getName() : "Unknown"));
        instructorLabel.getStyleClass().add("instructor-name");
        instructorBox.getChildren().addAll(instructorIcon, instructorLabel);

        // Buttons section
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setSpacing(4);

        // Favorite button
        ToggleButton favoriteButton = new ToggleButton();
        favoriteButton.getStyleClass().add("favorite-button");
        favoriteButton.setFocusTraversable(false);
        
        ImageView starIcon = new ImageView();
        try {
            Image starImage = new Image(getClass().getResourceAsStream("/images/star-active.png"));
            starIcon.setImage(starImage);
            starIcon.setFitWidth(24);
            starIcon.setFitHeight(26);
            favoriteButton.setGraphic(starIcon);
            favoriteButton.setSelected(true); // Always true in saved courses
        } catch (Exception e) {
            // Icon loading failed, continue without icon
        }

        favoriteButton.setOnAction(event -> {
            FavoriteCoursesService.removeFavoriteCourse(currentUser.getId(), course.getId());
            loadFavoriteCourses();
        });

        // View course button
        Button viewButton = new Button("View Course");
        viewButton.getStyleClass().add("view-course-button");
        viewButton.setStyle("-fx-background-color: #65558f; -fx-background-radius: 8px;");
        viewButton.setPrefWidth(110);
        viewButton.setPrefHeight(24);
        viewButton.setOnAction(event -> handleViewCourseDetails(course));

        buttonBox.getChildren().addAll(favoriteButton, viewButton);
        footerBox.getChildren().addAll(instructorBox, buttonBox);
        
        return footerBox;
    }
    
    /**
     * Handles viewing course details when a course card is clicked.
     * Loads the PDF course viewer if a PDF is available.
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
            Parent viewerRoot = loader.load();
            
            ViewCourseController controller = loader.getController();
            controller.setCourse(course);
            
            StackPane contentArea = (StackPane) courseCardsContainer.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(viewerRoot);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open the course viewer.");
        }
    }
    
    /**
     * Displays an alert dialog with the specified type, title, and message.
     *
     * @param alertType The type of alert to display
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