package app.frontend;

import app.backend.models.Course;
import app.backend.models.User;
import app.backend.services.CourseService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.embed.swing.SwingFXUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class CourseViewerController implements Initializable {

    @FXML private BorderPane pdfViewerContainer;
    @FXML private Label titleLabel;
    @FXML private Label pageLabel;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button zoomInButton;
    @FXML private Button zoomOutButton;
    @FXML private Button returnButton;
    @FXML private ImageView pdfImageView;
    @FXML private VBox errorContainer;
    @FXML private Label errorMessage;
    @FXML private HBox controlsContainer;

    private PDDocument document;
    private PDFRenderer renderer;
    private int currentPage = 0;
    private int totalPages = 0;
    private float zoomFactor = 1.0f;
    private Course currentCourse;
    private int teacherId = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize buttons
        prevButton.setOnAction(e -> showPreviousPage());
        nextButton.setOnAction(e -> showNextPage());
        zoomInButton.setOnAction(e -> zoomIn());
        zoomOutButton.setOnAction(e -> zoomOut());
        returnButton.setOnAction(e -> returnToCourses());
        
        // Hide error container by default
        errorContainer.setVisible(false);
        errorContainer.setManaged(false);
    }

    public void setCourse(Course course) {
        this.currentCourse = course;
        titleLabel.setText(course.getTitle());
        
        // Store the teacher ID
        if (course.getTeacherId() > 0) {
            this.teacherId = course.getTeacherId();
        }
        
        // Load the PDF
        loadPdf(course.getPdfPath());
    }
    
    public void setCourse(int courseId) {
        Course course = CourseService.getCourseById(courseId);
        if (course != null) {
            setCourse(course);
        } else {
            showError("Course not found.");
        }
    }
    
    // Set course with explicit teacher ID for proper navigation
    public void setCourse(Course course, int teacherId) {
        this.teacherId = teacherId;
        setCourse(course);
    }
    
    private void loadPdf(String pdfPath) {
        // Close any previous document
        closeDocument();
        
        if (pdfPath == null || pdfPath.isEmpty()) {
            showError("No PDF available for this course.");
            return;
        }
        
        try {
            // Try multiple approaches to find the PDF file
            File file = new File(pdfPath);
            
            // If file doesn't exist with direct path, try different approaches
            if (!file.exists()) {
                // Try to find the file in the courses directory
                String filename = new File(pdfPath).getName();
                File coursesDir = new File("courses");
                if (coursesDir.exists() && coursesDir.isDirectory()) {
                    File[] matchingFiles = coursesDir.listFiles((dir, name) -> name.equals(filename));
                    if (matchingFiles != null && matchingFiles.length > 0) {
                        file = matchingFiles[0];
                    }
                }
                
                // If still not found, look for a file with similar timestamp prefix
                if (!file.exists() && filename.contains("_")) {
                    final String searchPattern = filename.substring(filename.indexOf("_"));
                    File[] matchingFiles = coursesDir.listFiles((dir, name) -> name.contains(searchPattern));
                    if (matchingFiles != null && matchingFiles.length > 0) {
                        file = matchingFiles[0];
                    }
                }
                
                // If still not found, show detailed error
                if (!file.exists()) {
                    showError("PDF file not found: " + pdfPath + "\n\n" +
                              "Please ensure the PDF file exists and check the path in the database.\n" +
                              "Try placing the PDF in the 'courses' folder with name: " + filename);
                    return;
                }
            }
            
            System.out.println("Loading PDF from: " + file.getAbsolutePath());
            
            // Load the document and create renderer
            document = PDDocument.load(file);
            renderer = new PDFRenderer(document);
            totalPages = document.getNumberOfPages();
            
            // Reset current page and update UI
            currentPage = 0;
            updatePageLabel();
            
            // Show the first page
            renderCurrentPage();
            
            // Show controls and hide error
            controlsContainer.setVisible(true);
            controlsContainer.setManaged(true);
            errorContainer.setVisible(false);
            errorContainer.setManaged(false);
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load PDF: " + e.getMessage() + "\n\nPath: " + pdfPath);
        }
    }
    
    private void showPreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            renderCurrentPage();
            updatePageLabel();
        }
    }
    
    private void showNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            renderCurrentPage();
            updatePageLabel();
        }
    }
    
    private void zoomIn() {
        zoomFactor += 0.25f;
        renderCurrentPage();
    }
    
    private void zoomOut() {
        if (zoomFactor > 0.5f) {
            zoomFactor -= 0.25f;
            renderCurrentPage();
        }
    }
    
    private void renderCurrentPage() {
        if (document == null || renderer == null) return;
        
        try {
            // Render the current page
            BufferedImage image = renderer.renderImage(currentPage, zoomFactor);
            Image fxImage = SwingFXUtils.toFXImage(image, null);
            
            // Set the image in the ImageView
            pdfImageView.setImage(fxImage);
            
            // Adjust the fit width/height if needed
            pdfImageView.setPreserveRatio(true);
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to render page: " + e.getMessage());
        }
    }
    
    private void updatePageLabel() {
        pageLabel.setText(String.format("Page %d of %d", currentPage + 1, totalPages));
    }
    
    private void showError(String message) {
        errorMessage.setText(message);
        errorContainer.setVisible(true);
        errorContainer.setManaged(true);
        controlsContainer.setVisible(false);
        controlsContainer.setManaged(false);
    }
    
    private void closeDocument() {
        try {
            if (document != null) {
                document.close();
                document = null;
                renderer = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Call this method when the controller is being destroyed
    public void cleanup() {
        closeDocument();
    }

    /**
     * Returns to the specific teacher's courses page or general courses view
     */
    private void returnToCourses() {
        try {
            // Clean up resources before returning
            cleanup();
            
            // Get the content area from the main layout
            StackPane contentArea = (StackPane) pdfViewerContainer.getScene().lookup("#contentArea");
            
            // If we have a teacher ID, go to that teacher's courses
            if (teacherId > 0) {
                // Get the teacher user object from the teacher ID using AuthService instead of UserService
                User teacher = app.backend.services.AuthService.getUserById(teacherId);
                
                if (teacher != null) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teacher-courses.fxml"));
                    Parent teacherCoursesView = loader.load();
                    
                    // Get the controller and set the teacher
                    TeacherCoursesController controller = loader.getController();
                    controller.setTeacher(teacher);
                    
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(teacherCoursesView);
                } else {
                    // Fallback if the teacher can't be found
                    loadDefaultView(contentArea);
                }
            } 
            // If the user is a teacher, go to my-courses
            else if (LoginController.getCurrentUser() != null && 
                    LoginController.getCurrentUser().getRole().equals("teacher")) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/my-courses.fxml"));
                Parent myCourses = loader.load();
                contentArea.getChildren().clear();
                contentArea.getChildren().add(myCourses);
            } 
            // Otherwise go to the default courses view
            else {
                loadDefaultView(contentArea);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to return to courses view: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to load the default courses view (teachers-cards)
     */
    private void loadDefaultView(StackPane contentArea) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teachers-cards.fxml"));
        Parent teachersView = loader.load();
        contentArea.getChildren().clear();
        contentArea.getChildren().add(teachersView);
    }
}