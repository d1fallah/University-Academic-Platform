package app.frontend;

import app.backend.models.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller responsible for managing the user profile view.
 * Displays user information such as name, role, matricule number,
 * education level, university name, and contact details.
 */
public class UserProfileController implements Initializable {

    @FXML private Label nameLabel;
    @FXML private Label roleLabel;
    @FXML private Label matriculeLabel;
    @FXML private Label phoneLabel;
    @FXML private Hyperlink phoneActionLink;
    @FXML private Label levelLabel;
    @FXML private Label universityLabel;
    
    private User currentUser;
    private boolean hasPhone = true;
    
    /**
     * Initializes the controller and populates the user profile view with data.
     * 
     * @param location The location used to resolve relative paths for the root object
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = AuthLoginController.getCurrentUser();
        
        if (currentUser != null) {
            displayUserBasicInfo();
            displayEducationLevel();
            displayPhoneInfo();
        }
    }
    
    /**
     * Displays the user's basic information in the profile view.
     */
    private void displayUserBasicInfo() {
        nameLabel.setText(currentUser.getName());
        roleLabel.setText(capitalizeFirstLetter(currentUser.getRole()));
        matriculeLabel.setText(currentUser.getMatricule());
        universityLabel.setText(currentUser.getUniversityName());
    }
    
    /**
     * Capitalizes the first letter of a string.
     * 
     * @param text The string to capitalize
     * @return The capitalized string
     */
    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
    
    /**
     * Determines and displays the user's education level.
     * For teachers, displays "Teacher".
     * For students, tries to get the enrollment level via reflection
     * or infers from matricule number.
     */
    private void displayEducationLevel() {
        try {
            String level = "Not specified";
            
            if ("teacher".equals(currentUser.getRole())) {
                level = "Teacher";
            } else {
                level = determineStudentLevel();
            }
            
            levelLabel.setText(level);
        } catch (Exception e) {
            levelLabel.setText("Not specified");
        }
    }
    
    /**
     * Determines a student's education level using reflection or matricule analysis.
     * 
     * @return The determined education level
     */
    private String determineStudentLevel() {
        try {
            java.lang.reflect.Method getEnrollmentLevel = currentUser.getClass().getMethod("getEnrollmentLevel");
            String enrollmentLevel = (String) getEnrollmentLevel.invoke(currentUser);
            
            if (enrollmentLevel != null && !enrollmentLevel.isEmpty()) {
                return formatEnrollmentLevel(enrollmentLevel);
            }
        } catch (Exception e) {
            return inferLevelFromMatricule();
        }
        
        return "Not specified";
    }
    
    /**
     * Formats the enrollment level code into a human-readable form.
     * 
     * @param code The enrollment level code (L1, L2, etc.)
     * @return Formatted enrollment level
     */
    private String formatEnrollmentLevel(String code) {
        switch (code) {
            case "L1": return "License 1";
            case "L2": return "License 2";
            case "L3": return "License 3";
            case "M1": return "Master 1";
            case "M2": return "Master 2";
            default: return code;
        }
    }
    
    /**
     * Attempts to infer the student's level from their matricule number.
     * 
     * @return The inferred level or "Not specified"
     */
    private String inferLevelFromMatricule() {
        String matricule = currentUser.getMatricule();
        if (matricule != null && matricule.length() >= 6) {
            char levelChar = matricule.charAt(5);
            if (Character.isDigit(levelChar)) {
                int levelNum = Character.getNumericValue(levelChar);
                if (levelNum >= 1 && levelNum <= 3) {
                    return "License " + levelNum;
                } else if (levelNum >= 4 && levelNum <= 5) {
                    return "Master " + (levelNum - 3);
                }
            }
        }
        return "Not specified";
    }
    
    /**
     * Displays the user's phone information.
     */
    private void displayPhoneInfo() {
        if (hasPhone) {
            phoneLabel.setText("+213 123 45 67 89");
            phoneActionLink.setText("Change Phone number");
        } else {
            phoneLabel.setText("Not provided");
            phoneActionLink.setText("Add Phone number");
        }
    }
    
    /**
     * Handles the user logout action.
     * 
     * @param event The action event
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            AuthLoginController.setCurrentUser(null);
            
            Stage stage = (Stage) nameLabel.getScene().getWindow();
            AuthLoginController.loadLoginView(stage);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to logout: " + e.getMessage());
        }
    }
    
    /**
     * Handles the password change action.
     * Currently shows a placeholder message.
     * 
     * @param event The action event
     */
    @FXML
    private void handleChangePassword(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Change Password", 
                "Password change functionality will be implemented in a future update.");
    }
    
    /**
     * Handles the phone number action (add or change).
     * Currently shows a placeholder message.
     * 
     * @param event The action event
     */
    @FXML
    private void handlePhoneAction(ActionEvent event) {
        String action = hasPhone ? "change" : "add";
        showAlert(Alert.AlertType.INFORMATION, action + " Phone Number", 
                "Phone number " + action + " functionality will be implemented in a future update.");
    }
    
    /**
     * Shows an alert dialog with the specified type, title, and message.
     * 
     * @param alertType The type of the alert
     * @param title The title of the alert
     * @param message The message to display in the alert
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}