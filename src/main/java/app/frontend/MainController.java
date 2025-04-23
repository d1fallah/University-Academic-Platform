package app.frontend;

import app.backend.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

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

    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get the current logged in user
        currentUser = LoginController.getCurrentUser();
        
        // Update user information in the sidebar if user is available
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getName());
            userEmailLabel.setText(currentUser.getMatricule());
        }
        
        // Set up click listeners for menu items
        setupMenuListeners();
        
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
        loadContent("courses-cards.fxml");
    }
    
    // Load quizzes content
    private void loadQuizzes() {
        setActiveMenuItem(quizzesItem);
        // Will be implemented later
        loadContent("quizzes.fxml");
    }
    
    // Load exercises content
    private void loadExercises() {
        setActiveMenuItem(exercisesItem);
        // Will be implemented later
        loadContent("exercises.fxml");
    }
    
    // Load practical works content
    private void loadPracticalWorks() {
        setActiveMenuItem(practicalWorkItem);
        // Will be implemented later
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
        // Remove active class from all menu items
        dashboardItem.getStyleClass().remove("active-menu-item");
        coursesItem.getStyleClass().remove("active-menu-item");
        quizzesItem.getStyleClass().remove("active-menu-item");
        exercisesItem.getStyleClass().remove("active-menu-item");
        practicalWorkItem.getStyleClass().remove("active-menu-item");
        savedCoursesItem.getStyleClass().remove("active-menu-item");
        myResultsItem.getStyleClass().remove("active-menu-item");
        
        // Add active class to the selected menu item
        menuItem.getStyleClass().add("active-menu-item");
    }
}