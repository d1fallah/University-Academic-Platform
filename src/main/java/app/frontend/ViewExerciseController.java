package app.frontend;

import app.backend.models.Exercise;
import app.backend.models.User;
import app.backend.services.ExerciseService;
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
 * Controller responsible for displaying PDF exercises in the application.
 * This class handles loading, rendering, and navigating PDF files
 * with zoom capabilities and appropriate navigation between different views.
 *
 * @author Sellami Mohamed Odai
 */
public class ViewExerciseController implements Initializable {
    /** UI Components */
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

    /** PDF rendering properties */
    private PDDocument document;
    private PDFRenderer renderer;
    private int currentPage = 0;
    private int totalPages = 0;
    private float zoomFactor = 1.0f;
    
    /** Exercise navigation properties */
    private Exercise currentExercise;
    private int teacherId = -1;

    /**
     * Initializes the controller, setting up button actions and default UI state.
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
        returnButton.setOnAction(e -> returnToExercises());
        
        errorContainer.setVisible(false);
        errorContainer.setManaged(false);    }
    
    /**
     * Sets the exercise to be displayed and loads its PDF content.
     * 
     * @param exercise The exercise object to display
     */
    public void setExercise(Exercise exercise) {
        this.currentExercise = exercise;
        titleLabel.setText(exercise.getTitle());
        
        if (exercise.getTeacherId() > 0) {
            this.teacherId = exercise.getTeacherId();
        }
        
        loadPdf(exercise.getPdfPath());
    }
    
    /**
     * Loads an exercise from its ID and displays it.
     * 
     * @param exerciseId ID of the exercise to load
     */
    public void setExercise(int exerciseId) {
        Exercise exercise = ExerciseService.getExerciseById(exerciseId);
        if (exercise != null) {
            setExercise(exercise);
        } else {
            showError("Exercise not found.");
        }
    }
    
    /**
     * Sets the exercise with an explicit teacher ID for proper navigation.
     * 
     * @param exercise The exercise to display
     * @param teacherId The ID of the teacher who created the exercise
     */
    public void setExercise(Exercise exercise, int teacherId) {
        this.teacherId = teacherId;
        setExercise(exercise);
    }
      
    /**
     * Attempts to load a PDF file from the given path using various fallback methods.
     * 
     * @param pdfPath Path to the PDF file
     */
    private void loadPdf(String pdfPath) {
        closeDocument();
        
        if (pdfPath == null || pdfPath.isEmpty()) {
            showError("No PDF available for this exercise.");
            return;
        }
        
        try {
            File file = new File(pdfPath);
            
            if (!file.exists()) {
                file = findPdfFile(pdfPath);
                
                if (!file.exists()) {
                    String filename = new File(pdfPath).getName();
                    showError("PDF file not found: " + pdfPath + "\n\n" +
                              "Please ensure the PDF file exists and check the path in the database.\n" +
                              "Try placing the PDF in the 'exercises' folder with name: " + filename);
                    return;
                }
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
     * Attempts to find a PDF file using different search strategies.
     * 
     * @param pdfPath Original path that failed to load
     * @return File object of found PDF or original file if not found
     */
    private File findPdfFile(String pdfPath) {
        String filename = new File(pdfPath).getName();
        File file = new File(pdfPath);
        File exercisesDir = new File("exercises");
        
        if (exercisesDir.exists() && exercisesDir.isDirectory()) {
            // Try exact filename match first
            File[] matchingFiles = exercisesDir.listFiles((dir, name) -> name.equals(filename));
            if (matchingFiles != null && matchingFiles.length > 0) {
                return matchingFiles[0];
            }
            
            // Try timestamp suffix matching
            if (filename.contains("_")) {
                final String searchPattern = filename.substring(filename.indexOf("_"));
                matchingFiles = exercisesDir.listFiles((dir, name) -> name.contains(searchPattern));
                if (matchingFiles != null && matchingFiles.length > 0) {
                    return matchingFiles[0];
                }
            }
        }
        
        return file;
    }
      
    /**
     * Navigates to the previous page if available.
     */
    private void showPreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            renderCurrentPage();
            updatePageLabel();
        }
    }
    
    /**
     * Navigates to the next page if available.
     */
    private void showNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            renderCurrentPage();
            updatePageLabel();
        }
    }
    
    /**
     * Increases the zoom level by 0.25 factor.
     */
    private void zoomIn() {
        zoomFactor += 0.25f;
        renderCurrentPage();
    }
    
    /**
     * Decreases the zoom level by 0.25 factor with a minimum limit of 0.5.
     */
    private void zoomOut() {
        if (zoomFactor > 0.5f) {
            zoomFactor -= 0.25f;
            renderCurrentPage();
        }
    }
    
    /**
     * Renders the current page with the current zoom factor.
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
     * Updates the page label to show current page number and total pages.
     */
    private void updatePageLabel() {
        pageLabel.setText(String.format("Page %d of %d", currentPage + 1, totalPages));
    }
    
    /**
     * Displays an error message and hides the controls container.
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
     * Closes the current PDF document and releases resources.
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
     * Cleans up resources before the controller is destroyed.
     * Should be called when navigating away from this view.
     */
    public void cleanup() {
        closeDocument();
    }    
    
    /**
     * Returns to the appropriate exercises view based on user role and context.
     * Will navigate to one of:
     * 1. Teacher's own exercises view
     * 2. Specific teacher's exercises view (for students)
     * 3. Default teachers listing view
     */
    private void returnToExercises() {
        try {
            cleanup();
            
            StackPane contentArea = (StackPane) pdfViewerContainer.getScene().lookup("#contentArea");
            User currentUser = AuthLoginController.getCurrentUser();
            boolean isTeacher = currentUser != null && currentUser.getRole().equals("teacher");
            
            if (currentExercise != null && isTeacher && currentExercise.getTeacherId() == currentUser.getId()) {
                navigateToTeacherExercises(contentArea);
                return;
            }
            
            if (teacherId > 0 && (!isTeacher || teacherId != currentUser.getId())) {
                navigateToSpecificTeacherExercises(contentArea);
            } 
            else if (isTeacher) {
                navigateToTeacherExercises(contentArea);
            } 
            else {
                loadDefaultView(contentArea);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to return to exercises view: " + e.getMessage());
        }
    }
    
    /**
     * Navigates to the teacher's own exercises view.
     * 
     * @param contentArea The content area to load the view into
     * @throws IOException If loading the view fails
     */
    private void navigateToTeacherExercises(StackPane contentArea) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TeacherExercises.fxml"));
        Parent myExercises = loader.load();
        contentArea.getChildren().clear();
        contentArea.getChildren().add(myExercises);
    }
    
    /**
     * Navigates to a specific teacher's exercises view for students.
     * 
     * @param contentArea The content area to load the view into
     * @throws IOException If loading the view fails
     */
    private void navigateToSpecificTeacherExercises(StackPane contentArea) throws IOException {
        User teacher = app.backend.services.AuthService.getUserById(teacherId);
        
        if (teacher != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StudentExercises.fxml"));
            Parent teacherExercisesView = loader.load();
            
            StudentExercisesController controller = loader.getController();
            controller.setTeacher(teacher);
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(teacherExercisesView);
        } else {
            loadDefaultView(contentArea);
        }
    }
    
    /**
     * Loads the default teachers listing view with exercise view flag set.
     * 
     * @param contentArea The content area to load the view into
     * @throws IOException If loading the view fails
     */
    private void loadDefaultView(StackPane contentArea) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TeachersCards.fxml"));
        Parent teachersView = loader.load();
        
        TeachersCardsController controller = loader.getController();
        controller.setIsExerciseView(true);
        
        contentArea.getChildren().clear();
        contentArea.getChildren().add(teachersView);
    }
} 