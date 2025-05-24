package app.frontend;

import app.backend.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.control.Tooltip;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Main controller for the application interface.
 * Handles navigation between different views and manages the sidebar menu.
 * This controller is responsible for loading content into the main area based on user selection
 * and updating the UI to reflect the active menu item.
 */
public class ApplicationController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private HBox dashboardItem;
    @FXML private HBox coursesItem;
    @FXML private HBox quizzesItem;
    @FXML private HBox exercisesItem;
    @FXML private HBox practicalWorkItem;
    @FXML private HBox savedCoursesItem;
    @FXML private HBox myResultsItem;
    @FXML private Label userNameLabel;
    @FXML private Label userEmailLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label portalTypeLabel;
    @FXML private HBox profileContainer;
    
    @FXML private ImageView dashboardActiveDot;
    @FXML private ImageView coursesActiveDot;
    @FXML private ImageView quizzesActiveDot;
    @FXML private ImageView exercisesActiveDot;
    @FXML private ImageView practicalWorkActiveDot;
    @FXML private ImageView savedCoursesActiveDot;
    @FXML private ImageView myResultsActiveDot;

    private User currentUser;

    /**
     * Initializes the controller after its root element has been completely processed.
     * This method sets up the user interface based on the current user's role,
     * configures event handlers for menu items, and loads the default dashboard view.
     *
     * @param location The location used to resolve relative paths for the root object
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = AuthLoginController.getCurrentUser();
        
        if (currentUser != null) {
            setupUserInfoDisplay();
        }
        
        setupMenuListeners();
        profileContainer.setOnMouseClicked(event -> loadProfile());
        loadDashboard();
    }
    
    /**
     * Sets up the user information display in the sidebar.
     * Handles name truncation for long names and configures the appropriate portal type based on user role.
     */
    private void setupUserInfoDisplay() {
        String userName = currentUser.getName();
        String originalName = userName;
        
        Tooltip fullNameTooltip = new Tooltip(originalName);
        Tooltip.install(userNameLabel, fullNameTooltip);
        
        if (userName.length() > 15) {
            String[] nameParts = userName.split(" ");

            if (nameParts.length >= 2) {
                userName = nameParts[0];
                char lastInitial = nameParts[nameParts.length - 1].charAt(0);
                userName += " " + lastInitial + ".";
            } else {
                userName = userName.substring(0, 12) + "...";
            }
        }

        userNameLabel.setText(userName);
        userEmailLabel.setText(currentUser.getMatricule());
        
        String userRole = currentUser.getRole();
        if ("teacher".equals(userRole)) {
            portalTypeLabel.setText("Teacher Portal");
            userRoleLabel.setText("Teacher");
        } else {
            portalTypeLabel.setText("Student Portal");
            userRoleLabel.setText("Student");
        }
    }

    /**
     * Sets up click listeners for all menu items in the sidebar.
     * Each menu item is configured to load its corresponding content.
     */
    private void setupMenuListeners() {
        dashboardItem.setOnMouseClicked(event -> loadDashboard());
        coursesItem.setOnMouseClicked(event -> loadCourses());
        quizzesItem.setOnMouseClicked(event -> loadQuizzes());
        exercisesItem.setOnMouseClicked(event -> loadExercises());
        practicalWorkItem.setOnMouseClicked(event -> loadPracticalWorks());
        savedCoursesItem.setOnMouseClicked(event -> loadSavedCourses());
        myResultsItem.setOnMouseClicked(event -> loadMyResults());
    }

    /**
     * Loads the dashboard content based on the current user's role.
     * Sets the dashboard menu item as active and loads the appropriate dashboard view.
     */
    private void loadDashboard() {
        setActiveMenuItem(dashboardItem);
        
        if (currentUser != null && currentUser.getRole().equals("teacher")) {
            loadContent("TeacherDashboard.fxml");
        } else {
            loadContent("StudentDashboard.fxml");
        }
    }

    /**
     * Loads the courses content view.
     * For students, displays the standard teachers list.
     * For teachers, loads a customized view excluding their own card and showing management options.
     */
    void loadCourses() {
        setActiveMenuItem(coursesItem);

        if (currentUser != null && currentUser.getRole().equals("student")) {
            loadContent("TeachersCards.fxml");
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TeachersCards.fxml"));
                Parent teachersView = loader.load();

                TeachersCardsController controller = loader.getController();
                controller.setExcludeCurrentTeacher(true);
                controller.setShowManageCourseButton(true);

                contentArea.getChildren().clear();
                contentArea.getChildren().add(teachersView);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("❌ Failed to load TeachersCards.fxml");
                loadContent("courses-cards.fxml");
            }
        }
    }

    /**
     * Loads the quizzes content view.
     * Handles different views based on user role (student or teacher).
     */
    void loadQuizzes() {
        setActiveMenuItem(quizzesItem);
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TeachersCards.fxml"));
            Parent teachersView = loader.load();
            
            TeachersCardsController controller = loader.getController();
            controller.setIsQuizView(true);
            
            if (currentUser != null && !currentUser.getRole().equals("student")) {
                controller.setExcludeCurrentTeacher(true);
                controller.setShowManageCourseButton(true);
            }
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(teachersView);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to load TeachersCards.fxml");
            loadContent("quizzes.fxml");
        }
    }

    /**
     * Loads the exercises content view.
     * Configures the view depending on whether the current user is a student or teacher.
     */
    void loadExercises() {
        setActiveMenuItem(exercisesItem);
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TeachersCards.fxml"));
            Parent teachersView = loader.load();
            
            TeachersCardsController controller = loader.getController();
            controller.setIsQuizView(false);
            controller.setIsExerciseView(true);
            
            if (currentUser != null && !currentUser.getRole().equals("student")) {
                controller.setExcludeCurrentTeacher(true);
                controller.setShowManageCourseButton(true);
            }
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(teachersView);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to load TeachersCards.fxml");
        }
    }

    /**
     * Loads the practical works content view.
     * Configures the view based on user role with appropriate settings.
     */
    void loadPracticalWorks() {
        setActiveMenuItem(practicalWorkItem);
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TeachersCards.fxml"));
            Parent teachersView = loader.load();
            
            TeachersCardsController controller = loader.getController();
            controller.setIsQuizView(false);
            controller.setIsExerciseView(false);
            controller.setIsPracticalWorkView(true);
            
            if (currentUser != null && !currentUser.getRole().equals("student")) {
                controller.setExcludeCurrentTeacher(true);
                controller.setShowManageCourseButton(true);
            }
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(teachersView);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to load TeachersCards.fxml");
            if (currentUser != null && !currentUser.getRole().equals("student")) {
                loadContent("TeacherPracticalWorks.fxml");
            }
        }
    }

    /**
     * Loads the saved courses content view.
     * Sets the saved courses menu item as active.
     */
    private void loadSavedCourses() {
        setActiveMenuItem(savedCoursesItem);
        loadContent("SavedCourses.fxml");
    }

    /**
     * Loads the user's results content view.
     * Sets the my results menu item as active.
     */
    private void loadMyResults() {
        setActiveMenuItem(myResultsItem);
        loadContent("UserProfile.fxml");
    }

    /**
     * Loads the user profile content view.
     * Resets all menu selections as profile is not part of the main navigation.
     */
    private void loadProfile() {
        resetAllMenuItems();
        loadContent("UserProfile.fxml");
    }

    /**
     * Helper method to load FXML content into the main content area.
     * Attempts to inject this controller instance into the loaded controller if supported.
     * Also stores references to this controller in the scene and content properties for access.
     *
     * @param fxmlFile The name of the FXML file to load
     */
    private void loadContent(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFile));
            Parent content = loader.load();
            
            injectApplicationControllerReference(loader.getController());
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
            
            content.getProperties().put("parentController", this);
            
            if (contentArea.getScene() != null && contentArea.getScene().getRoot() != null) {
                contentArea.getScene().getRoot().getProperties().put("parentController", this);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to load " + fxmlFile);
        }
    }
    
    /**
     * Injects this controller instance into the loaded controller if supported.
     * Uses direct casting for known controller types or reflection for other controllers.
     *
     * @param controller The controller to inject this reference into
     */
    private void injectApplicationControllerReference(Object controller) {
        if (controller == null) return;
        
        if (controller instanceof StudentDashboardController) {
            ((StudentDashboardController) controller).setApplicationController(this);
        } 
        else if (controller instanceof TeacherDashboardController) {
            try {
                java.lang.reflect.Method setAppController =
                    controller.getClass().getMethod("setApplicationController", ApplicationController.class);
                setAppController.invoke(controller, this);
            } catch (Exception e) {
                System.out.println("Note: setApplicationController method not found on " + controller.getClass().getName());
            }
        }
    }

    /**
     * Sets the specified menu item as active.
     * Updates the styling, activates the indicator dot, and sets the active icon.
     *
     * @param menuItem The menu item to set as active
     */
    private void setActiveMenuItem(HBox menuItem) {
        resetAllMenuItems();
        
        menuItem.getStyleClass().add("active-menu-item");
        showActiveDotForMenuItem(menuItem);
        
        String iconPath = getActiveIconPath(menuItem);
        if (iconPath != null) {
            setMenuItemIcon(menuItem, iconPath);
        }
    }

    /**
     * Resets all menu items to their default state.
     * Removes active styles, hides indicator dots, and resets icons.
     */
    private void resetAllMenuItems() {
        HBox[] menuItems = {
            dashboardItem, coursesItem, quizzesItem, exercisesItem,
            practicalWorkItem, savedCoursesItem, myResultsItem
        };

        ImageView[] activeDots = {
            dashboardActiveDot, coursesActiveDot, quizzesActiveDot, exercisesActiveDot,
            practicalWorkActiveDot, savedCoursesActiveDot, myResultsActiveDot
        };

        for (HBox item : menuItems) {
            item.getStyleClass().remove("active-menu-item");
        }

        for (ImageView dot : activeDots) {
            dot.setVisible(false);
        }

        resetMenuIcons();
    }
    
    /**
     * Resets all menu icons to their default state.
     */
    private void resetMenuIcons() {
        setMenuItemIcon(dashboardItem, "/images/QR Code.png");
        setMenuItemIcon(coursesItem, "/images/Square Academic Cap.png");
        setMenuItemIcon(quizzesItem, "/images/Object Scan.png");
        setMenuItemIcon(exercisesItem, "/images/Ruler Cross Pen.png");
        setMenuItemIcon(practicalWorkItem, "/images/Keyboard.png");
        setMenuItemIcon(savedCoursesItem, "/images/Heart Angle.png");
        setMenuItemIcon(myResultsItem, "/images/Medal Ribbons Star.png");
    }

    /**
     * Shows the active indicator dot for the specified menu item.
     *
     * @param menuItem The menu item to show the active dot for
     */
    private void showActiveDotForMenuItem(HBox menuItem) {
        ImageView dot = getActiveDotForMenuItem(menuItem);
        
        if (dot != null) {
            dot.setImage(new javafx.scene.image.Image(getClass().getResource("/images/ActiveDot.png").toExternalForm()));
            dot.setVisible(true);
        }
    }
    
    /**
     * Gets the corresponding active dot for a menu item.
     *
     * @param menuItem The menu item to get the active dot for
     * @return The ImageView representing the active dot, or null if not found
     */
    private ImageView getActiveDotForMenuItem(HBox menuItem) {
        if (menuItem == dashboardItem) return dashboardActiveDot;
        if (menuItem == coursesItem) return coursesActiveDot;
        if (menuItem == quizzesItem) return quizzesActiveDot;
        if (menuItem == exercisesItem) return exercisesActiveDot;
        if (menuItem == practicalWorkItem) return practicalWorkActiveDot;
        if (menuItem == savedCoursesItem) return savedCoursesActiveDot;
        if (menuItem == myResultsItem) return myResultsActiveDot;
        return null;
    }
    
    /**
     * Gets the active icon path for a menu item.
     *
     * @param menuItem The menu item to get the active icon path for
     * @return The path to the active icon resource
     */
    private String getActiveIconPath(HBox menuItem) {
        if (menuItem == dashboardItem) return "/images/ActiveDashboard.png";
        if (menuItem == coursesItem) return "/images/ActiveCourse.png";
        if (menuItem == quizzesItem) return "/images/ActiveQuizes.png";
        if (menuItem == exercisesItem) return "/images/ActiveExercises.png";
        if (menuItem == practicalWorkItem) return "/images/ActiveKeyboard.png";
        if (menuItem == savedCoursesItem) return "/images/ActiveHeart.png";
        if (menuItem == myResultsItem) return "/images/ActiveMedal.png";
        return null;
    }

    /**
     * Sets the icon for a menu item.
     *
     * @param menuItem The menu item to set the icon for
     * @param iconPath The path to the icon resource
     */
    private void setMenuItemIcon(HBox menuItem, String iconPath) {
        if (menuItem.getChildren().get(0) instanceof ImageView) {
            ImageView imageView = (ImageView) menuItem.getChildren().get(0);
            imageView.setImage(new javafx.scene.image.Image(getClass().getResource(iconPath).toExternalForm()));
            imageView.setFitWidth(35);
            imageView.setFitHeight(35);
            imageView.setPreserveRatio(true);
        }
    }

    /**
     * Loads the teacher courses view directly.
     * Used by other controllers to navigate to the teacher courses screen.
     */
    public void loadTeacherCourses() {
        setActiveMenuItem(coursesItem);
        loadContent("TeacherCourses.fxml");
    }

    /**
     * Loads the teacher quizzes view directly.
     * Used by other controllers to navigate to the teacher quizzes screen.
     */
    public void loadTeacherQuizzes() {
        setActiveMenuItem(quizzesItem);
        loadContent("TeacherQuizzes.fxml");
    }

    /**
     * Loads the teacher exercises view directly.
     * Used by other controllers to navigate to the teacher exercises screen.
     */
    public void loadTeacherExercises() {
        setActiveMenuItem(exercisesItem);
        loadContent("TeacherExercises.fxml");
    }

    /**
     * Loads the teacher practical works view directly.
     * Used by other controllers to navigate to the teacher practical works screen.
     */
    public void loadTeacherPracticalWorks() {
        setActiveMenuItem(practicalWorkItem);
        loadContent("TeacherPracticalWorks.fxml");
    }
}
