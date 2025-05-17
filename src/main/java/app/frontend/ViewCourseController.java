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
import java.util.ResourceBundle;

/**
 * Controller for viewing course PDF content.
 * This class handles displaying PDF documents associated with courses,
 * providing navigation controls for page viewing, zoom functionality,
 * and appropriate navigation back to course listings.
 *
 * @author Sellami Mohamed Odai
 * @version 1.0
 */
public class ViewCourseController implements Initializable {    
    
    /** Main container for the PDF viewer */
    @FXML private BorderPane pdfViewerContainer;
    
    /** Label displaying the course title */
    @FXML private Label titleLabel;
    
    /** Label showing current page number and total pages */
    @FXML private Label pageLabel;
    
    /** Button to navigate to previous page */
    @FXML private Button prevButton;
    
    /** Button to navigate to next page */
    @FXML private Button nextButton;
    
    /** Button to zoom in */
    @FXML private Button zoomInButton;
    
    /** Button to zoom out */
    @FXML private Button zoomOutButton;
    
    /** Button to return to courses listing */
    @FXML private Button returnButton;
    
    /** ImageView displaying the PDF page */
    @FXML private ImageView pdfImageView;
    
    /** Container for error messages */
    @FXML private VBox errorContainer;
    
    /** Label for displaying error text */
    @FXML private Label errorMessage;
    
    /** Container for PDF navigation controls */
    @FXML private HBox controlsContainer;

    /** The PDF document being displayed */
    private PDDocument document;
    
    /** Renderer for the PDF document */
    private PDFRenderer renderer;
    
    /** Current page index (0-based) */
    private int currentPage = 0;
    
    /** Total number of pages in the document */
    private int totalPages = 0;
    
    /** Current zoom level for rendering */
    private float zoomFactor = 1.0f;
    
    /** The course being displayed */
    private Course currentCourse;
    
    /** ID of the course teacher */
    private int teacherId = -1;    
    
    /**
     * Initializes the controller after FXML fields are injected.
     * Sets up button actions and initializes the UI state.
     *
     * @param location The location used to resolve relative paths for resources
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        prevButton.setOnAction(e -> showPreviousPage());
        nextButton.setOnAction(e -> showNextPage());
        zoomInButton.setOnAction(e -> zoomIn());
        zoomOutButton.setOnAction(e -> zoomOut());
        returnButton.setOnAction(e -> returnToCourses());
        
        errorContainer.setVisible(false);
        errorContainer.setManaged(false);
    }    
    
    /**
     * Sets the course to display and loads its PDF.
     *
     * @param course The course to display
     */
    public void setCourse(Course course) {
        this.currentCourse = course;
        titleLabel.setText(course.getTitle());
        
        if (course.getTeacherId() > 0) {
            this.teacherId = course.getTeacherId();
        }
        
        loadPdf(course.getPdfPath());
    }
    
    /**
     * Sets the course to display with an explicit teacher ID for navigation.
     *
     * @param course The course to display
     * @param teacherId The explicit teacher ID for navigation
     */
    public void setCourse(Course course, int teacherId) {
        this.teacherId = teacherId;
        setCourse(course);
    }
    
    /**
     * Sets the course to display by its ID.
     * Fetches course information from the service.
     *
     * @param courseId The ID of the course to display
     */
    public void setCourse(int courseId) {
        Course course = CourseService.getCourseById(courseId);
        if (course != null) {
            setCourse(course);
        } else {
            showError("Course not found.");
        }
    }
    
    /**
     * Loads and displays a PDF document from the specified path.
     * Attempts multiple fallback strategies to find the PDF if the direct path fails.
     *
     * @param pdfPath The path to the PDF file
     */
    private void loadPdf(String pdfPath) {
        closeDocument();
        
        if (pdfPath == null || pdfPath.isEmpty()) {
            showError("No PDF available for this course.");
            return;
        }
        
        try {
            File file = findPdfFile(pdfPath);
            
            if (file == null || !file.exists()) {
                return; // Error already shown in findPdfFile method
            }
            
            System.out.println("Loading PDF from: " + file.getAbsolutePath());
            
            document = PDDocument.load(file);
            renderer = new PDFRenderer(document);
            totalPages = document.getNumberOfPages();
            
            currentPage = 0;
            updatePageLabel();
            renderCurrentPage();
            
            controlsContainer.setVisible(true);
            controlsContainer.setManaged(true);
            errorContainer.setVisible(false);
            errorContainer.setManaged(false);
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load PDF: " + e.getMessage() + "\n\nPath: " + pdfPath);
        }
    }
    
    /**
     * Attempts to find a PDF file using multiple search strategies.
     * 
     * @param pdfPath The initial path to search for
     * @return The found File object or null if not found
     */
    private File findPdfFile(String pdfPath) {
        File file = new File(pdfPath);
        
        if (file.exists()) {
            return file;
        }
        
        String filename = new File(pdfPath).getName();
        File coursesDir = new File("courses");
        
        // Strategy 1: Look for exact filename match in courses directory
        if (coursesDir.exists() && coursesDir.isDirectory()) {
            File[] exactMatches = coursesDir.listFiles((dir, name) -> name.equals(filename));
            if (exactMatches != null && exactMatches.length > 0) {
                return exactMatches[0];
            }
            
            // Strategy 2: Look for partial filename match using timestamp suffix
            if (filename.contains("_")) {
                final String searchPattern = filename.substring(filename.indexOf("_"));
                File[] partialMatches = coursesDir.listFiles((dir, name) -> name.contains(searchPattern));
                if (partialMatches != null && partialMatches.length > 0) {
                    return partialMatches[0];
                }
            }
        }
        
        // No matches found - show error
        showError("PDF file not found: " + pdfPath + "\n\n" +
                  "Please ensure the PDF file exists and check the path in the database.\n" +
                  "Try placing the PDF in the 'courses' folder with name: " + filename);
        
        return null;
    }
      
    /**
     * Navigates to the previous page in the PDF if available.
     */
    private void showPreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            renderCurrentPage();
            updatePageLabel();
        }
    }
    
    /**
     * Navigates to the next page in the PDF if available.
     */
    private void showNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            renderCurrentPage();
            updatePageLabel();
        }
    }
    
    /**
     * Increases the zoom level and refreshes the view.
     */
    private void zoomIn() {
        zoomFactor += 0.25f;
        renderCurrentPage();
    }
    
    /**
     * Decreases the zoom level and refreshes the view,
     * preventing zoom from going below 0.5.
     */
    private void zoomOut() {
        if (zoomFactor > 0.5f) {
            zoomFactor -= 0.25f;
            renderCurrentPage();
        }
    }
      
    /**
     * Renders the current page with the current zoom factor.
     * Updates the ImageView with the rendered PDF page.
     */
    private void renderCurrentPage() {
        if (document == null || renderer == null) return;
        
        try {
            BufferedImage image = renderer.renderImage(currentPage, zoomFactor);
            Image fxImage = SwingFXUtils.toFXImage(image, null);
            
            pdfImageView.setImage(fxImage);
            pdfImageView.setPreserveRatio(true);
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to render page: " + e.getMessage());
        }
    }
    
    /**
     * Updates the page label to reflect current page and total pages.
     */
    private void updatePageLabel() {
        pageLabel.setText(String.format("Page %d of %d", currentPage + 1, totalPages));
    }
    
    /**
     * Displays an error message and hides the PDF controls.
     *
     * @param message The error message to display
     */
    private void showError(String message) {
        errorMessage.setText(message);
        errorContainer.setVisible(true);
        errorContainer.setManaged(true);
        controlsContainer.setVisible(false);
        controlsContainer.setManaged(false);
    }
    
    /**
     * Closes the PDF document and releases resources.
     */
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
    
    /**
     * Cleans up resources when the controller is being destroyed.
     * Should be called when transitioning away from this view.
     */
    public void cleanup() {
        closeDocument();
    }    
    
    /**
     * Returns to the appropriate courses view based on user role and context.
     * Navigation logic:
     * 1. Teacher viewing their own course -> TeacherCourses.fxml
     * 2. Any user viewing another teacher's course -> StudentCourses.fxml with that teacher
     * 3. Teacher not viewing specific teacher -> TeacherCourses.fxml
     * 4. Default -> TeachersCards.fxml
     */
    private void returnToCourses() {
        try {
            cleanup();
            
            StackPane contentArea = (StackPane) pdfViewerContainer.getScene().lookup("#contentArea");
            User currentUser = AuthLoginController.getCurrentUser();
            boolean isTeacher = currentUser != null && currentUser.getRole().equals("teacher");
            
            if (currentCourse != null && isTeacher && currentCourse.getTeacherId() == currentUser.getId()) {
                navigateToTeacherCoursesView(contentArea);
                return;
            }
            
            if (teacherId > 0 && (!isTeacher || teacherId != currentUser.getId())) {
                navigateToSpecificTeacherView(contentArea);
            } 
            else if (isTeacher) {
                navigateToTeacherCoursesView(contentArea);
            } 
            else {
                loadDefaultView(contentArea);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to return to courses view: " + e.getMessage());
        }
    }
    
    /**
     * Navigates to the teacher's own courses view.
     * 
     * @param contentArea The content area to update
     * @throws IOException If loading the FXML fails
     */
    private void navigateToTeacherCoursesView(StackPane contentArea) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TeacherCourses.fxml"));
        Parent myCourses = loader.load();
        contentArea.getChildren().clear();
        contentArea.getChildren().add(myCourses);
    }
    
    /**
     * Navigates to a specific teacher's courses view.
     * 
     * @param contentArea The content area to update
     * @throws IOException If loading the FXML fails
     */
    private void navigateToSpecificTeacherView(StackPane contentArea) throws IOException {
        User teacher = app.backend.services.AuthService.getUserById(teacherId);
        
        if (teacher != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StudentCourses.fxml"));
            Parent teacherCoursesView = loader.load();
            
            StudentCoursesController controller = loader.getController();
            controller.setTeacher(teacher);
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(teacherCoursesView);
        } else {
            loadDefaultView(contentArea);
        }
    }
    
    /**
     * Loads the default teachers cards view.
     *
     * @param contentArea The content area to update
     * @throws IOException If loading the FXML fails
     */
    private void loadDefaultView(StackPane contentArea) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TeachersCards.fxml"));
        Parent teachersView = loader.load();
        contentArea.getChildren().clear();
        contentArea.getChildren().add(teachersView);
    }
}