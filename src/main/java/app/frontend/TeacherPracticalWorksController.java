package app.frontend;

import app.backend.models.Course;
import app.backend.models.PracticalWork;
import app.backend.models.User;
import app.backend.services.CourseService;
import app.backend.services.PracticalWorkService;
import app.backend.services.AuthService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TeacherPracticalWorksController implements Initializable {

    @FXML private FlowPane teacherCardsContainer;
    @FXML private Button backToCoursesButton;
    @FXML private TextField searchField;
    @FXML private Button managePracticalWorksButton;
    @FXML private Label viewTitleLabel;
    @FXML private StackPane addPracticalWorkOverlay;
    @FXML private Button addPracticalWorkButton;
    
    private User currentUser;
    private List<User> allTeachers = new ArrayList<>();
    private boolean excludeCurrentTeacher = false;
    private boolean showManageButton = false;
    private User lastViewedTeacher = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current logged in user
        currentUser = LoginController.getCurrentUser();
        
        // Load all teachers
        loadAllTeachers();
        
        // Set up search field listener
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                    filterTeachers(newValue);
        });
        
        // Update view title
        if (viewTitleLabel != null) {
            viewTitleLabel.setText("Practical Works");
        }
        
        // Style the date picker
        setupDatePicker();
    }
    
    /**
     * Loads all teachers and stores them
     */
    private void loadAllTeachers() {
        // Get all teachers and store for filtering
        allTeachers = AuthService.getAllTeachers();
        
        // Always exclude the current teacher if they are a teacher
        if (currentUser != null && currentUser.getRole().equals("teacher")) {
            allTeachers = allTeachers.stream()
                .filter(teacher -> teacher.getId() != currentUser.getId())
                .collect(Collectors.toList());
        }
        // If additionally requested to exclude current teacher (redundant for teachers)
        else if (excludeCurrentTeacher && currentUser != null) {
            allTeachers = allTeachers.stream()
                .filter(teacher -> teacher.getId() != currentUser.getId())
                .collect(Collectors.toList());
        }
        
        // Display the teachers
        displayTeachers(allTeachers);
        
        // Update manage button visibility
        if (managePracticalWorksButton != null) {
            boolean isTeacher = currentUser != null && currentUser.getRole().equals("teacher");
            managePracticalWorksButton.setVisible(isTeacher);
            managePracticalWorksButton.setManaged(isTeacher);
        }
    }
    
    /**
     * Displays the provided list of teachers as cards
     */
    private void displayTeachers(List<User> teachers) {
        // Clear the container
        teacherCardsContainer.getChildren().clear();
        
        if (teachers.isEmpty()) {
            // Display message if no teachers
            Label noTeachersLabel = new Label("No teachers available yet.");
            noTeachersLabel.getStyleClass().add("no-teachers-message");
            noTeachersLabel.setPrefWidth(teacherCardsContainer.getPrefWidth());
            noTeachersLabel.setPrefHeight(200);
            noTeachersLabel.setAlignment(Pos.CENTER);
            noTeachersLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
            
            teacherCardsContainer.getChildren().add(noTeachersLabel);
        } else {
            // Create and add a card for each teacher
            for (User teacher : teachers) {
                teacherCardsContainer.getChildren().add(createTeacherCard(teacher));
            }
        }
    }
    
    /**
     * Filters teachers by name based on search text
     */
    private void filterTeachers(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            // If search is empty, show all teachers
            displayTeachers(allTeachers);
        } else {
            // Filter teachers whose names contain the search text (case insensitive)
            List<User> filteredTeachers = allTeachers.stream()
                .filter(teacher -> teacher.getName().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
            
            displayTeachers(filteredTeachers);
        }
    }
    
    /**
     * Creates a visual card for a teacher
     */
    private StackPane createTeacherCard(User teacher) {
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
            System.out.println("Failed to load background image for teacher card");
        }

        // Card layout container
        VBox cardContent = new VBox();
        cardContent.getStyleClass().add("card-content");
        cardContent.setSpacing(20);
        cardContent.setPadding(new Insets(10, 10, 10, 10));
        cardContent.setPrefWidth(480);
        cardContent.setPrefHeight(230);
        cardContent.setAlignment(Pos.CENTER);

        // Profile image
        ImageView profileImage = new ImageView();
        try {
            Image profileImg = new Image(getClass().getResourceAsStream("/images/profilep.png"));
            profileImage.setImage(profileImg);
            profileImage.setFitWidth(80);
            profileImage.setFitHeight(80);
            profileImage.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Failed to load profile image for teacher: " + teacher.getName());
        }

        // Teacher name
        Label nameLabel = new Label("Prof. " + teacher.getName());
        nameLabel.getStyleClass().add("card-title");
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        nameLabel.setAlignment(Pos.CENTER);

        // View Practical Works button
        Button viewButton = new Button("View Practical Works");
        viewButton.getStyleClass().add("view-course-button");
        viewButton.setStyle("-fx-background-color: #d97706;");
        viewButton.setPrefWidth(150);
        viewButton.setPrefHeight(30);
        viewButton.setOnAction(e -> handleViewTeacherPracticalWorks(teacher));

        // Add all sections to the card
        cardContent.getChildren().addAll(profileImage, nameLabel, viewButton);

        // Stack the background and content
        cardPane.getChildren().addAll(cardBackground, cardContent);

        // Add accessibility features
        cardPane.setAccessibleText("Teacher: " + teacher.getName());

        return cardPane;
    }

    /**
     * Handles the action when a user clicks on a teacher card to view practical works
     */
    private void handleViewTeacherPracticalWorks(User teacher) {
        try {
            // Load the teacher practical works view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teacher-practical-works-view.fxml"));
            Parent practicalWorksView = loader.load();
            
            // Get the controller and set the teacher
            TeacherPracticalWorksViewController controller = loader.getController();
            controller.setTeacher(teacher);
            
            // Store the last viewed teacher
            lastViewedTeacher = teacher;
            
            // Get the main layout's content area and set the practical works view
            StackPane contentArea = (StackPane) teacherCardsContainer.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(practicalWorksView);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load teacher practical works: " + e.getMessage());
        }
    }
    
    /**
     * Handles the manage button click action
     */
    @FXML
    private void handleManagePracticalWorks(ActionEvent event) {
        try {
            // Load My Practical Works view
            Parent contentView = FXMLLoader.load(getClass().getResource("/fxml/my-practical-works.fxml"));
            
            // Get the content area and set the view
            StackPane contentArea = (StackPane) managePracticalWorksButton.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(contentView);
            
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load management view");
        }
    }

    /**
     * Set whether to exclude the current teacher from the displayed teachers
     */
    public void setExcludeCurrentTeacher(boolean excludeCurrentTeacher) {
        this.excludeCurrentTeacher = excludeCurrentTeacher;
        // If we're already initialized, reload teachers
        if (teacherCardsContainer != null) {
            loadAllTeachers();
        }
    }
    
    /**
     * Set whether to show the manage practical works button
     */
    public void setShowManageButton(boolean showManageButton) {
        this.showManageButton = showManageButton;
        // If button and flag exist, update visibility
        if (managePracticalWorksButton != null) {
            managePracticalWorksButton.setVisible(showManageButton);
            managePracticalWorksButton.setManaged(showManageButton);
        }
    }

    /**
     * Get the last viewed teacher
     */
    public User getLastViewedTeacher() {
        return lastViewedTeacher;
    }

    @FXML
    private void handleSelectFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select PDF File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        
        File selectedFile = fileChooser.showOpenDialog(
            ((Button) event.getSource()).getScene().getWindow()
        );
        
        if (selectedFile != null) {
            // Update the label to show the selected file name
            Label selectedFileLabel = (Label) ((VBox) ((StackPane) addPracticalWorkOverlay.lookup("#dropArea")).getChildren().get(0)).getChildren().get(0);
            selectedFileLabel.setText(selectedFile.getName());
        }
    }

    /**
     * Handles cancel button click in the add/edit practical work dialog
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        // Clear all fields
        clearPracticalWorkFields();
        
        // Hide the overlay
        addPracticalWorkOverlay.setVisible(false);
    }
    
    /**
     * Handles save button click in the add/edit practical work dialog
     */
    @FXML
    private void handleSave(ActionEvent event) {
        // TO DO: Implement saving of practical work
        
        // For now, just hide the overlay
        addPracticalWorkOverlay.setVisible(false);
    }
    
    /**
     * Clears all fields in the practical work dialog
     */
    private void clearPracticalWorkFields() {
        // Find and clear all input fields
        TextField titleField = (TextField) addPracticalWorkOverlay.lookup("#practicalWorkNameField");
        if (titleField != null) {
            titleField.clear();
        }
        
        TextArea descriptionField = (TextArea) addPracticalWorkOverlay.lookup("#practicalWorkDescriptionField");
        if (descriptionField != null) {
            descriptionField.clear();
        }
        
        ComboBox<?> courseCombo = (ComboBox<?>) addPracticalWorkOverlay.lookup("#courseComboBox");
        if (courseCombo != null) {
            courseCombo.getSelectionModel().clearSelection();
        }
        
        ComboBox<?> levelCombo = (ComboBox<?>) addPracticalWorkOverlay.lookup("#levelComboBox");
        if (levelCombo != null) {
            levelCombo.getSelectionModel().clearSelection();
        }
        
        DatePicker deadlinePicker = (DatePicker) addPracticalWorkOverlay.lookup("#deadlinePicker");
        if (deadlinePicker != null) {
            deadlinePicker.setValue(null);
        }
        
        // Reset file selection label
        Label fileLabel = (Label) addPracticalWorkOverlay.lookup("#selectedFileLabel");
        if (fileLabel != null) {
            fileLabel.setText("No file selected");
        }
    }
    
    /**
     * Sets up the date picker with proper styling
     */
    private void setupDatePicker() {
        try {
            // Find the date picker in the dialog
            DatePicker deadlinePicker = (DatePicker) addPracticalWorkOverlay.lookup("#deadlinePicker");
            if (deadlinePicker != null) {
                // Set default date to today
                deadlinePicker.setValue(LocalDate.now());
                
                // Ensure the StyleClass contains dialog-input-field
                if (!deadlinePicker.getStyleClass().contains("dialog-input-field")) {
                    deadlinePicker.getStyleClass().add("dialog-input-field");
                }
                
                // Disable past dates
                deadlinePicker.setDayCellFactory(picker -> new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        
                        if (empty || date == null) {
                            setDisable(true);
        } else {
                            // Disable all dates before today
                            LocalDate today = LocalDate.now();
                            setDisable(date.isBefore(today));
                            
                            if (date.isBefore(today)) {
                                setStyle("-fx-background-color: #1a1a1a;");
                                getStyleClass().add("disabled");
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            System.out.println("Failed to style date picker: " + e.getMessage());
        }
    }
} 