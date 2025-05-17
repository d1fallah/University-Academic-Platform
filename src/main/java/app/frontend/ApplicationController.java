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

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

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

    // Active dot indicators
    @FXML private ImageView dashboardActiveDot;
    @FXML private ImageView coursesActiveDot;
    @FXML private ImageView quizzesActiveDot;
    @FXML private ImageView exercisesActiveDot;
    @FXML private ImageView practicalWorkActiveDot;
    @FXML private ImageView savedCoursesActiveDot;
    @FXML private ImageView myResultsActiveDot;

    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get the current logged in user
        currentUser = AuthLoginController.getCurrentUser();
        
        // Update user information in the sidebar if user is available
        if (currentUser != null) {
            // Handle long names in the sidebar
            String userName = currentUser.getName();
            
            // If the name is too long and contains "Mohamed", replace with "M."
            if (userName.contains("Mohamed") && userName.length() > 20) {
                userName = userName.replace("Mohamed", "M.");
            }
            
            // Apply font size reduction for longer names
            if (userName.length() > 20) {
                userNameLabel.setStyle("-fx-font-size: 11px;");
            } else if (userName.length() > 16) {
                userNameLabel.setStyle("-fx-font-size: 13px;");
            } else if (userName.length() > 12) {
                userNameLabel.setStyle("-fx-font-size: 14px;");
            }
            
            userNameLabel.setText(userName);
            userEmailLabel.setText(currentUser.getMatricule());
            
            // Set portal type and user role badge based on user role
            String userRole = currentUser.getRole();
            if ("teacher".equals(userRole)) {
                portalTypeLabel.setText("Teacher Portal");
                userRoleLabel.setText("Teacher");
            } else {
                portalTypeLabel.setText("Student Portal");
                userRoleLabel.setText("Student");
            }
        }
        
        // Set up click listeners for menu items
        setupMenuListeners();
        
        // Set up click listener for the profile section
        profileContainer.setOnMouseClicked(event -> loadProfile());
        
        // Default to dashboard view on startup
        loadDashboard();
    }
    
    private void setupMenuListeners() {
        // Dashboard menu item
        dashboardItem.setOnMouseClicked(event -> loadDashboard());
        
        // Courses menu item
        coursesItem.setOnMouseClicked(event -> loadCourses());
        
        // Quizzes menu item
        quizzesItem.setOnMouseClicked(event -> loadQuizzes());
        
        // Exercises menu item
        exercisesItem.setOnMouseClicked(event -> loadExercises());
        
        // Practical work menu item
        practicalWorkItem.setOnMouseClicked(event -> loadPracticalWorks());
        
        // Saved courses menu item
        savedCoursesItem.setOnMouseClicked(event -> loadSavedCourses());
        
        // My results menu item
        myResultsItem.setOnMouseClicked(event -> loadMyResults());
    }
    
    // Load dashboard content
    private void loadDashboard() {
        setActiveMenuItem(dashboardItem);
        // Will be implemented later
        loadContent("dashboard.fxml");
    }
    
    // Load courses content
    private void loadCourses() {
        setActiveMenuItem(coursesItem);
        
        // If user is a student, show teachers first
        if (currentUser != null && currentUser.getRole().equals("student")) {
            loadContent("teachers-cards.fxml");
        } else if (currentUser != null && currentUser.getRole().equals("teacher")) {
            // For teachers, load the teachers view but exclude their own card
            // and keep the manage courses button visible
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teachers-cards.fxml"));
                Parent teachersView = loader.load();
                
                // Get the controller and set flag to exclude current teacher and show manage button
                TeachersCardsController controller = loader.getController();
                controller.setExcludeCurrentTeacher(true);
                controller.setShowManageCourseButton(true);
                
                contentArea.getChildren().clear();
                contentArea.getChildren().add(teachersView);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("❌ Failed to load teachers-cards.fxml");
                // Fallback to the regular course cards view
                loadContent("courses-cards.fxml");
            }
        } else {
            // For other users or if user is null, show the regular course cards view
            loadContent("courses-cards.fxml");
        }
    }
    
    // Load quizzes content
    private void loadQuizzes() {
        setActiveMenuItem(quizzesItem);
        
        // If user is a student, show teachers first
        if (currentUser != null && currentUser.getRole().equals("student")) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teachers-cards.fxml"));
                Parent teachersView = loader.load();
                
                // Get the controller and set flags
                TeachersCardsController controller = loader.getController();
                controller.setIsQuizView(true); // Set quiz view flag
                
                contentArea.getChildren().clear();
                contentArea.getChildren().add(teachersView);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("❌ Failed to load teachers-cards.fxml");
                loadContent("quizzes.fxml");
            }
        } else if (currentUser != null && currentUser.getRole().equals("teacher")) {
            // For teachers, load the teachers view but exclude their own card
            // and keep the manage courses button visible
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teachers-cards.fxml"));
                Parent teachersView = loader.load();
                
                // Get the controller and set flags
                TeachersCardsController controller = loader.getController();
                controller.setExcludeCurrentTeacher(true);
                controller.setShowManageCourseButton(true);
                controller.setIsQuizView(true); // Set quiz view flag
                
                contentArea.getChildren().clear();
                contentArea.getChildren().add(teachersView);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("❌ Failed to load teachers-cards.fxml");
                loadContent("quizzes.fxml");
            }
        } else {
            // For other users or if user is null, show the regular quizzes view
            loadContent("quizzes.fxml");
        }
    }
    
    // Load exercises content
    private void loadExercises() {
        setActiveMenuItem(exercisesItem);
        
        // For students, load teacher cards filtered by their level
        if (currentUser != null && currentUser.getRole().equals("student")) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teachers-cards.fxml"));
                Parent teachersView = loader.load();
                
                // Get the controller and set flags - make sure isQuizView is set to false first
                TeachersCardsController controller = loader.getController();
                controller.setIsQuizView(false); // Explicitly set quiz view to false first
                controller.setIsExerciseView(true); // Set exercise view flag
                
                contentArea.getChildren().clear();
                contentArea.getChildren().add(teachersView);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("❌ Failed to load teachers-cards.fxml");
                loadContent("exercises.fxml");
            }
        } else if (currentUser != null && currentUser.getRole().equals("teacher")) {
            // For teachers, load teacher cards but exclude their own card
            // and keep the manage exercises button visible
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teachers-cards.fxml"));
                Parent teachersView = loader.load();
                
                // Get the controller and set flags - make sure isQuizView is set to false first
                TeachersCardsController controller = loader.getController();
                controller.setExcludeCurrentTeacher(true);
                controller.setShowManageCourseButton(true);
                controller.setIsQuizView(false); // Explicitly set quiz view to false first
                controller.setIsExerciseView(true); // Set exercise view flag
                
                // Debug - verify flag values
                System.out.println("After setting flags in loadExercises - isExerciseView: " + 
                                  controller.isExerciseView() + ", isQuizView: " + controller.isQuizView());
                
                contentArea.getChildren().clear();
                contentArea.getChildren().add(teachersView);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("❌ Failed to load teachers-cards.fxml");
                loadContent("exercises.fxml");
            }
        } else {
            // For other users or if user is null, show the regular exercises view
            loadContent("exercises.fxml");
        }
    }
    
    // Load practical works content
    private void loadPracticalWorks() {
        setActiveMenuItem(practicalWorkItem);
        loadContent("practical-works.fxml");
    }
    
    // Load saved courses content
    private void loadSavedCourses() {
        setActiveMenuItem(savedCoursesItem);
        // Will be implemented later
        loadContent("saved-courses.fxml");
    }
    
    // Load my results content
    private void loadMyResults() {
        setActiveMenuItem(myResultsItem);
        // Will be implemented later
        loadContent("my-results.fxml");
    }
    
    // Load profile content
    private void loadProfile() {
        // Reset all menu items since profile is not a menu item
        resetAllMenuItems();
        loadContent("profile.fxml");
    }
    
    // Helper method to load FXML content into the content area
    private void loadContent(String fxmlFile) {
        try {
            Parent content = FXMLLoader.load(getClass().getResource("/fxml/" + fxmlFile));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to load " + fxmlFile);
        }
    }
    
    // Helper method to set the active menu item
    private void setActiveMenuItem(HBox menuItem) {
        // Clear all active styles first
        resetAllMenuItems();
        
        // Add active class to the selected menu item
        menuItem.getStyleClass().add("active-menu-item");
        
        // Show the active dot for the selected menu item (except Dashboard)
        if (menuItem != dashboardItem) {
            showActiveDotForMenuItem(menuItem);
        }
        
        // Set active icon for the selected menu item
        if (menuItem == dashboardItem) {
            setMenuItemIcon(menuItem, "/images/ActiveDashboard.png");
        } else if (menuItem == coursesItem) {
            setMenuItemIcon(menuItem, "/images/ActiveCourse.png");
        } else if (menuItem == quizzesItem) {
            setMenuItemIcon(menuItem, "/images/ActiveQuizes.png");
        } else if (menuItem == exercisesItem) {
            setMenuItemIcon(menuItem, "/images/ActiveExercises.png");
        } else if (menuItem == practicalWorkItem) {
            setMenuItemIcon(menuItem, "/images/ActiveKeyboard.png");
        } else if (menuItem == savedCoursesItem) {
            setMenuItemIcon(menuItem, "/images/ActiveHeart.png");
        } else if (menuItem == myResultsItem) {
            setMenuItemIcon(menuItem, "/images/ActiveMedal.png");
        }
    }
    
    // Reset all menu items to their default state
    private void resetAllMenuItems() {
        // List of all menu items
        HBox[] menuItems = {
            dashboardItem, coursesItem, quizzesItem, exercisesItem,
            practicalWorkItem, savedCoursesItem, myResultsItem
        };
        
        // List of all active dots
        ImageView[] activeDots = {
            dashboardActiveDot, coursesActiveDot, quizzesActiveDot, exercisesActiveDot,
            practicalWorkActiveDot, savedCoursesActiveDot, myResultsActiveDot
        };
        
        // Reset styles and hide dots
        for (HBox item : menuItems) {
            item.getStyleClass().remove("active-menu-item");
        }
        
        for (ImageView dot : activeDots) {
            dot.setVisible(false);
        }
        
        // Reset all icons to default
        setMenuItemIcon(dashboardItem, "/images/QR Code.png");
        setMenuItemIcon(coursesItem, "/images/Square Academic Cap.png");
        setMenuItemIcon(quizzesItem, "/images/Object Scan.png");
        setMenuItemIcon(exercisesItem, "/images/Ruler Cross Pen.png");
        setMenuItemIcon(practicalWorkItem, "/images/Keyboard.png");
        setMenuItemIcon(savedCoursesItem, "/images/Heart Angle.png");
        setMenuItemIcon(myResultsItem, "/images/Medal Ribbons Star.png");
    }
    
    // Show the active dot for a specific menu item
    private void showActiveDotForMenuItem(HBox menuItem) {
        ImageView dot = null;
        
        if (menuItem == coursesItem) {
            dot = coursesActiveDot;
        } else if (menuItem == quizzesItem) {
            dot = quizzesActiveDot;
        } else if (menuItem == exercisesItem) {
            dot = exercisesActiveDot;
        } else if (menuItem == practicalWorkItem) {
            dot = practicalWorkActiveDot;
        } else if (menuItem == savedCoursesItem) {
            dot = savedCoursesActiveDot;
        } else if (menuItem == myResultsItem) {
            dot = myResultsActiveDot;
        }
        
        if (dot != null) {
            dot.setImage(new javafx.scene.image.Image(getClass().getResource("/images/ActiveDot.png").toExternalForm()));
            dot.setVisible(true);
        }
    }
    
    // Helper method to set menu item icon
    private void setMenuItemIcon(HBox menuItem, String iconPath) {
        if (menuItem.getChildren().get(0) instanceof ImageView) {
            ImageView imageView = (ImageView) menuItem.getChildren().get(0);
            imageView.setImage(new javafx.scene.image.Image(getClass().getResource(iconPath).toExternalForm()));
            imageView.setFitWidth(35);
            imageView.setFitHeight(35);
            imageView.setPreserveRatio(true);
        }
    }
}