package app.frontend;

import app.backend.models.Course;
import app.backend.models.User;
import app.backend.services.CourseService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CoursesController implements Initializable {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private TextArea commentArea;
    @FXML private Button addCourseButton;
    
    @FXML private TableView<Course> coursesTable;
    @FXML private TableColumn<Course, Integer> idColumn;
    @FXML private TableColumn<Course, String> titleColumn;
    @FXML private TableColumn<Course, String> descriptionColumn;
    @FXML private TableColumn<Course, String> dateColumn;
    @FXML private TableColumn<Course, Void> actionsColumn;
    
    private User currentUser;
    private ObservableList<Course> coursesList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current logged in user
        currentUser = LoginController.getCurrentUser();
        
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        
        // Configure the actions column
        setupActionsColumn();
        
        // Load courses for the current teacher
        loadTeacherCourses();
    }
    
    @FXML
    public void handleAddCourse(ActionEvent event) {
        // Validate inputs
        if (titleField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Course title is required");
            return;
        }
        
        // Create new course object
        Course newCourse = new Course(
            titleField.getText(),
            descriptionArea.getText(),
            commentArea.getText(),
            currentUser.getId()
        );
        
        // Add course to database
        boolean success = CourseService.addCourse(newCourse);
        
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Course added successfully");
            clearFields();
            loadTeacherCourses(); // Reload courses to show the new one
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add course");
        }
    }
    
    private void loadTeacherCourses() {
        // Check if user is teacher
        if (currentUser != null && currentUser.getRole().equals("teacher")) {
            List<Course> teacherCourses = CourseService.getCoursesByTeacherId(currentUser.getId());
            coursesList.clear();
            coursesList.addAll(teacherCourses);
            coursesTable.setItems(coursesList);
        }
    }
    
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttonBox = new HBox(5, editButton, deleteButton);

            {
                // Button styling
                editButton.getStyleClass().add("table-button");
                editButton.getStyleClass().add("edit-button");
                deleteButton.getStyleClass().add("table-button");
                deleteButton.getStyleClass().add("delete-button");

                // Center the buttons
                buttonBox.setAlignment(Pos.CENTER);

                // Button actions
                editButton.setOnAction(event -> {
                    Course course = getTableView().getItems().get(getIndex());
                    handleEditCourse(course);
                });

                deleteButton.setOnAction(event -> {
                    Course course = getTableView().getItems().get(getIndex());
                    handleDeleteCourse(course);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
                }
            }
        });
    }
    
    private void handleEditCourse(Course course) {
        // Populate fields with course data for editing
        titleField.setText(course.getTitle());
        descriptionArea.setText(course.getDescription());
        commentArea.setText(course.getComment());
        
        // Change add button to update
        addCourseButton.setText("Update Course");
        addCourseButton.setOnAction(event -> updateCourse(course.getId()));
    }
    
    private void updateCourse(int courseId) {
        // Create updated course object
        Course updatedCourse = new Course();
        updatedCourse.setId(courseId);
        updatedCourse.setTitle(titleField.getText());
        updatedCourse.setDescription(descriptionArea.getText());
        updatedCourse.setComment(commentArea.getText());
        
        // Update in database
        boolean success = CourseService.updateCourse(updatedCourse);
        
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Course updated successfully");
            clearFields();
            loadTeacherCourses(); // Reload courses to show changes
            
            // Reset button to add mode
            addCourseButton.setText("Add Course");
            addCourseButton.setOnAction(this::handleAddCourse);
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update course");
        }
    }
    
    private void handleDeleteCourse(Course course) {
        // Confirm deletion
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete the course: " + course.getTitle() + "?");
        
        // Process the result
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = CourseService.deleteCourse(course.getId());
                
                if (success) {
                    loadTeacherCourses(); // Reload courses after deletion
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Course deleted successfully");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete course");
                }
            }
        });
    }
    
    private void clearFields() {
        titleField.clear();
        descriptionArea.clear();
        commentArea.clear();
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}