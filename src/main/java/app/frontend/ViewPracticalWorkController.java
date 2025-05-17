package app.frontend;

import app.backend.models.PracticalWork;
import app.backend.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
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
 * Controller for viewing practical work PDFs with zoom and navigation capabilities.
 * This controller handles the display of PDF documents associated with practical works,
 * providing functionality for page navigation, zooming, and returning to previous views.
 * 
 * @author Sellami Mohamed Odai
 */
public class ViewPracticalWorkController implements Initializable {    
    
    /** UI Components */
    @FXML private BorderPane pdfViewerContainer;
    @FXML private Label practicalWorkTitleLabel;
    @FXML private Label pageLabel;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button zoomInButton;
    @FXML private Button zoomOutButton;
    @FXML private Button backButton;
    @FXML private ImageView pdfImageView;
    @FXML private VBox errorContainer;
    @FXML private Label errorMessage;
    
    /** PDF rendering properties */
    private PDDocument document;
    private PDFRenderer renderer;
    private int currentPage = 0;
    private int totalPages = 0;
    private float zoomFactor = 1.0f;

    /** Practical work navigation properties */
    private PracticalWork currentPracticalWork;
    private int teacherId = -1;
    private boolean isViewingOwnContent = false;    
    
    /**
     * Initializes the controller by setting up UI controls and event handlers.
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
        backButton.setOnAction(e -> handleBack());
        
        errorContainer.setVisible(false);
        errorContainer.setManaged(false);
    }
      
    /**
     * Sets the practical work to display and updates the UI.
     * 
     * @param practicalWork The practical work to display
     */
    public void setPracticalWork(PracticalWork practicalWork) {
        this.currentPracticalWork = practicalWork;
        this.teacherId = practicalWork.getTeacherId();
        updateUI();
    }
    
    /**
     * Sets the practical work and teacher ID for better navigation.
     * 
     * @param practicalWork The practical work to display
     * @param teacherId The ID of the teacher who created this practical work
     */
    public void setPracticalWork(PracticalWork practicalWork, int teacherId) {
        this.currentPracticalWork = practicalWork;
        this.teacherId = teacherId;
        
        User currentUser = AuthLoginController.getCurrentUser();
        if (currentUser != null && currentUser.getRole().equals("teacher") && currentUser.getId() == teacherId) {
            isViewingOwnContent = true;
        }
        
        updateUI();
    }

    /**
     * Sets the practical work, teacher, and explicitly specifies if viewing own content.
     * 
     * @param practicalWork The practical work to display
     * @param teacher The teacher who created this practical work
     * @param isViewingOwnContent Whether the current user is viewing their own content
     */
    public void setPracticalWork(PracticalWork practicalWork, User teacher, boolean isViewingOwnContent) {
        this.currentPracticalWork = practicalWork;
        this.teacherId = teacher.getId();
        this.isViewingOwnContent = isViewingOwnContent;
        updateUI();
    }
    
    /**
     * Updates the UI with practical work details including title, back button text,
     * and loads the PDF document.
     */
    private void updateUI() {
        if (currentPracticalWork == null) {
            return;
        }
        
        practicalWorkTitleLabel.setText(currentPracticalWork.getTitle());
        
        User currentUser = AuthLoginController.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.getRole().equals("teacher")) {
                backButton.setText(isViewingOwnContent ? 
                    "Back to My Practical Works" : "Back to Teacher's Practical Works");
            } else {
                backButton.setText("Back to Practical Works");
            }
        }
        
        loadPdf(currentPracticalWork.getPdfPath());
    }
    
    /**
     * Attempts to load a PDF from the given path, trying various fallback methods
     * if the direct path does not exist.
     *
     * @param pdfPath Path to the PDF file to load
     */
    private void loadPdf(String pdfPath) {
        closeDocument();
        
        if (pdfPath == null || pdfPath.isEmpty()) {
            showError("No PDF available for this practical work.");
            return;
        }
        
        try {
            File file = new File(pdfPath);
            
            if (!file.exists()) {
                String filename = new File(pdfPath).getName();
                File practicalWorksDir = new File("practical_works");
                
                if (practicalWorksDir.exists() && practicalWorksDir.isDirectory()) {
                    // First try exact filename match
                    File[] matchingFiles = practicalWorksDir.listFiles((dir, name) -> name.equals(filename));
                    if (matchingFiles != null && matchingFiles.length > 0) {
                        file = matchingFiles[0];
                    }
                    
                    // If still not found, try matching by pattern
                    if (!file.exists() && filename.contains("_")) {
                        final String searchPattern = filename.substring(filename.indexOf("_"));
                        matchingFiles = practicalWorksDir.listFiles((dir, name) -> name.contains(searchPattern));
                        if (matchingFiles != null && matchingFiles.length > 0) {
                            file = matchingFiles[0];
                        }
                    }
                }
                
                if (!file.exists()) {
                    showError("PDF file not found: " + pdfPath + "\n\n" +
                              "Please ensure the PDF file exists and check the path in the database.\n" +
                              "Try placing the PDF in the 'practical_works' folder with name: " + filename);
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
            
            errorContainer.setVisible(false);
            errorContainer.setManaged(false);
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load PDF: " + e.getMessage() + "\n\nPath: " + pdfPath);
        }
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
     * Increases the zoom level of the document and refreshes the view.
     */
    private void zoomIn() {
        zoomFactor += 0.25f;
        renderCurrentPage();
    }
    
    /**
     * Decreases the zoom level of the document and refreshes the view,
     * with a minimum zoom factor of 0.5.
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
     * Updates the page label with current page number and total pages.
     */
    private void updatePageLabel() {
        pageLabel.setText(String.format("Page %d of %d", currentPage + 1, totalPages));
    }
    
    /**
     * Displays an error message in the error container.
     *
     * @param message The error message to display
     */
    private void showError(String message) {
        errorMessage.setText(message);
        errorContainer.setVisible(true);
        errorContainer.setManaged(true);
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
     * Releases resources when the controller is being destroyed.
     * Should be called when navigating away from this view.
     */
    public void cleanup() {
        closeDocument();
    }
    
    /**
     * Handles the back button action by navigating to the appropriate view
     * based on user role and context.
     */
    @FXML
    private void handleBack() {
        try {
            cleanup();
            
            StackPane contentArea = (StackPane) pdfViewerContainer.getScene().lookup("#contentArea");
            
            User currentUser = AuthLoginController.getCurrentUser();
            boolean isTeacher = currentUser != null && currentUser.getRole().equals("teacher");
            
            // Teacher viewing their own practical work
            if (currentPracticalWork != null && isTeacher && currentPracticalWork.getTeacherId() == currentUser.getId()) {
                navigateToTeacherPracticalWorks(contentArea);
                return;
            }
            
            // Navigate to specific teacher's practical works
            if (teacherId > 0 && (!isTeacher || teacherId != currentUser.getId())) {
                User teacher = app.backend.services.AuthService.getUserById(teacherId);
                
                if (teacher != null) {
                    navigateToStudentPracticalWorks(contentArea, teacher);
                } else {
                    loadDefaultView(contentArea);
                }
            }
            // Teacher navigating to their own practical works
            else if (isTeacher) {
                navigateToTeacherPracticalWorks(contentArea);
            }
            // Default view for students
            else {
                loadDefaultView(contentArea);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to return to practical works view: " + e.getMessage());
        }
    }
    
    /**
     * Navigates to the teacher's practical works view.
     * 
     * @param contentArea The content area to load the view into
     * @throws IOException If loading the FXML fails
     */
    private void navigateToTeacherPracticalWorks(StackPane contentArea) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TeacherPracticalWorks.fxml"));
        Parent view = loader.load();
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
    }
    
    /**
     * Navigates to a student's view of a specific teacher's practical works.
     * 
     * @param contentArea The content area to load the view into
     * @param teacher The teacher whose practical works to display
     * @throws IOException If loading the FXML fails
     */
    private void navigateToStudentPracticalWorks(StackPane contentArea, User teacher) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StudentPracticalWorks.fxml"));
        Parent view = loader.load();
        
        StudentPracticalWorksController controller = loader.getController();
        controller.setTeacher(teacher);
        
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
    }
    
    /**
     * Loads the default teachers card view for practical works.
     * 
     * @param contentArea The content area to load the view into
     * @throws IOException If loading the FXML fails
     */
    private void loadDefaultView(StackPane contentArea) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TeachersCards.fxml"));
        Parent teachersView = loader.load();
        
        TeachersCardsController controller = loader.getController();
        controller.setIsPracticalWorkView(true);
        
        contentArea.getChildren().clear();
        contentArea.getChildren().add(teachersView);
    }
} 