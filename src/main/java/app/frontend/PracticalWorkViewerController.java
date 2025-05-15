package app.frontend;

import app.backend.models.PracticalWork;
import app.backend.models.User;
import app.backend.services.PracticalWorkService;
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

public class PracticalWorkViewerController implements Initializable {

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
    
    private PDDocument document;
    private PDFRenderer renderer;
    private int currentPage = 0;
    private int totalPages = 0;
    private float zoomFactor = 1.0f;
    private PracticalWork currentPracticalWork;
    private User currentTeacher;
    private int teacherId = -1;
    private boolean isViewingOwnContent = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize buttons
        prevButton.setOnAction(e -> showPreviousPage());
        nextButton.setOnAction(e -> showNextPage());
        zoomInButton.setOnAction(e -> zoomIn());
        zoomOutButton.setOnAction(e -> zoomOut());
        backButton.setOnAction(e -> handleBack());
        
        // Hide error container by default
        errorContainer.setVisible(false);
        errorContainer.setManaged(false);
    }
    
    /**
     * Sets the practical work to display and updates the UI
     */
    public void setPracticalWork(PracticalWork practicalWork) {
        this.currentPracticalWork = practicalWork;
        this.teacherId = practicalWork.getTeacherId();
        updateUI();
    }
    
    /**
     * Sets the practical work and teacher info for better navigation
     */
    public void setPracticalWork(PracticalWork practicalWork, int teacherId) {
        this.currentPracticalWork = practicalWork;
        this.teacherId = teacherId;
        
        // Check if the logged-in user is the teacher who owns this practical work
        User currentUser = LoginController.getCurrentUser();
        if (currentUser != null && currentUser.getRole().equals("teacher") && currentUser.getId() == teacherId) {
            isViewingOwnContent = true;
        }
        
        updateUI();
    }

    /**
     * Sets the practical work, teacher, and explicitly specifies if viewing own content
     */
    public void setPracticalWork(PracticalWork practicalWork, User teacher, boolean isViewingOwnContent) {
        this.currentPracticalWork = practicalWork;
        this.currentTeacher = teacher;
        this.teacherId = teacher.getId();
        this.isViewingOwnContent = isViewingOwnContent;
        updateUI();
    }
    
    /**
     * Updates the UI with practical work details
     */
    private void updateUI() {
        if (currentPracticalWork == null) {
            return;
        }
        
        // Set title label
        practicalWorkTitleLabel.setText(currentPracticalWork.getTitle());
        
        // Set up back button text based on user role and context
        User currentUser = LoginController.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.getRole().equals("teacher")) {
                if (isViewingOwnContent) {
                    backButton.setText("Back to My Practical Works");
                } else {
                    backButton.setText("Back to Teacher's Practical Works");
                }
            } else {
                backButton.setText("Back to Practical Works");
            }
        }
        
        // Load the PDF if it exists
        loadPdf(currentPracticalWork.getPdfPath());
    }
    
    private void loadPdf(String pdfPath) {
        // Close any previous document
        closeDocument();
        
        if (pdfPath == null || pdfPath.isEmpty()) {
            showError("No PDF available for this practical work.");
            return;
        }
        
        try {
            // Try multiple approaches to find the PDF file
            File file = new File(pdfPath);
            
            // If file doesn't exist with direct path, try different approaches
            if (!file.exists()) {
                // Try to find the file in the practical_works directory
                String filename = new File(pdfPath).getName();
                File practicalWorksDir = new File("practical_works");
                if (practicalWorksDir.exists() && practicalWorksDir.isDirectory()) {
                    File[] matchingFiles = practicalWorksDir.listFiles((dir, name) -> name.equals(filename));
                    if (matchingFiles != null && matchingFiles.length > 0) {
                        file = matchingFiles[0];
                    }
                }
                
                // If still not found, look for a file with similar timestamp prefix
                if (!file.exists() && filename.contains("_")) {
                    final String searchPattern = filename.substring(filename.indexOf("_"));
                    File[] matchingFiles = practicalWorksDir.listFiles((dir, name) -> name.contains(searchPattern));
                    if (matchingFiles != null && matchingFiles.length > 0) {
                        file = matchingFiles[0];
                    }
                }
                
                // If still not found, show detailed error
                if (!file.exists()) {
                    showError("PDF file not found: " + pdfPath + "\n\n" +
                              "Please ensure the PDF file exists and check the path in the database.\n" +
                              "Try placing the PDF in the 'practical_works' folder with name: " + filename);
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
            
            // Hide error
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
     * Handles the back button action
     */
    @FXML
    private void handleBack() {
        try {
            // Clean up resources before returning
            cleanup();
            
            // Get the content area from the main layout
            StackPane contentArea = (StackPane) pdfViewerContainer.getScene().lookup("#contentArea");
            
            User currentUser = LoginController.getCurrentUser();
            boolean isTeacher = currentUser != null && currentUser.getRole().equals("teacher");
            
            // Check if a teacher is viewing their own practical work
            if (currentPracticalWork != null && isTeacher && currentPracticalWork.getTeacherId() == currentUser.getId()) {
                System.out.println("Teacher viewing their own practical work - returning to my-practical-works.fxml");
                
                // This is a teacher viewing their own practical work, return to my-practical-works
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/my-practical-works.fxml"));
                Parent myPracticalWorks = loader.load();
                contentArea.getChildren().clear();
                contentArea.getChildren().add(myPracticalWorks);
                return;
            }
            
            // If we have a teacher ID and it's not the current user, go to that teacher's practical works
            if (teacherId > 0 && (!isTeacher || teacherId != currentUser.getId())) {
                // Get the teacher user object from the teacher ID
                User teacher = app.backend.services.AuthService.getUserById(teacherId);
                
                if (teacher != null) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teacher-practical-works-view.fxml"));
                    Parent teacherPracticalWorksView = loader.load();
                    
                    // Get the controller and set the teacher
                    TeacherPracticalWorksViewController controller = loader.getController();
                    controller.setTeacher(teacher);
                    
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(teacherPracticalWorksView);
                } else {
                    // Fallback if the teacher can't be found
                    loadDefaultView(contentArea);
                }
            }
            // If the user is a teacher, go to my-practical-works (if we didn't already handle it above)
            else if (isTeacher) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/my-practical-works.fxml"));
                Parent myPracticalWorks = loader.load();
                contentArea.getChildren().clear();
                contentArea.getChildren().add(myPracticalWorks);
            }
            // Otherwise go to the default practical works view
            else {
                loadDefaultView(contentArea);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to return to practical works view: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to load the default practical works view (teachers-cards)
     */
    private void loadDefaultView(StackPane contentArea) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teachers-cards.fxml"));
        Parent teachersView = loader.load();
        
        // Get the controller and set the practical work view flag
        TeachersCardsController controller = loader.getController();
        controller.setIsPracticalWorkView(true);
        
        contentArea.getChildren().clear();
        contentArea.getChildren().add(teachersView);
    }
} 