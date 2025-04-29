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

public class ProfileController implements Initializable {

    @FXML private Label nameLabel;
    @FXML private Label roleLabel;
    @FXML private Label matriculeLabel;
    @FXML private Label phoneLabel;
    @FXML private Hyperlink phoneActionLink;
    @FXML private Label levelLabel;
    @FXML private Label universityLabel;
    
    private User currentUser;
    private boolean hasPhone = true;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current user
        currentUser = LoginController.getCurrentUser();
        
        if (currentUser != null) {
            // Set user information
            nameLabel.setText(currentUser.getName());
            roleLabel.setText(currentUser.getRole().substring(0, 1).toUpperCase() + currentUser.getRole().substring(1));
            matriculeLabel.setText(currentUser.getMatricule());
            
            // Set university information (static for now)
            universityLabel.setText("Mohamed khider biskra");
            
            // Handle level display
            try {
                // Try to determine the level based on matricule prefix if enrollment level is not available
                String level = "Not specified";
                
                // Check the role first
                if ("teacher".equals(currentUser.getRole())) {
                    level = "Teacher";
                } else {
                    // For students, try to get enrollment level via reflection or infer from matricule
                    try {
                        // Try to get enrollmentLevel using reflection
                        java.lang.reflect.Method getEnrollmentLevel = currentUser.getClass().getMethod("getEnrollmentLevel");
                        String enrollmentLevel = (String) getEnrollmentLevel.invoke(currentUser);
                        
                        if (enrollmentLevel != null && !enrollmentLevel.isEmpty()) {
                            switch (enrollmentLevel) {
                                case "L1":
                                    level = "License 1";
                                    break;
                                case "L2":
                                    level = "License 2";
                                    break;
                                case "L3":
                                    level = "License 3";
                                    break;
                                case "M1":
                                    level = "Master 1";
                                    break;
                                case "M2":
                                    level = "Master 2";
                                    break;
                                default:
                                    level = enrollmentLevel;
                            }
                        }
                    } catch (Exception e) {
                        // The enrollmentLevel field or getter doesn't exist
                        // Try to infer from matricule (if it follows a pattern)
                        String matricule = currentUser.getMatricule();
                        if (matricule != null && matricule.length() >= 6) {
                            // Assuming matricule might have level info in it - this is just a fallback
                            // This is a placeholder logic - adjust based on your actual matricule format
                            char levelChar = matricule.charAt(5);
                            if (Character.isDigit(levelChar)) {
                                int levelNum = Character.getNumericValue(levelChar);
                                if (levelNum >= 1 && levelNum <= 3) {
                                    level = "License " + levelNum;
                                } else if (levelNum >= 4 && levelNum <= 5) {
                                    level = "Master " + (levelNum - 3);
                                }
                            }
                        }
                    }
                }
                
                levelLabel.setText(level);
                
            } catch (Exception e) {
                // Fallback if anything goes wrong
                levelLabel.setText("Not specified");
            }
            
            // Phone number handling
            if (hasPhone) {
                phoneLabel.setText("+213 123 45 67 89"); // Static demo value
                phoneActionLink.setText("Change Phone number");
            } else {
                phoneLabel.setText("Not provided");
                phoneActionLink.setText("Add Phone number");
            }
        }
    }
    
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Set current user to null
            LoginController.setCurrentUser(null);
            
            // Load the login view
            Stage stage = (Stage) nameLabel.getScene().getWindow();
            LoginController.loadLoginView(stage);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to logout: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleChangePassword(ActionEvent event) {
        // Show dialog for password change
        showAlert(Alert.AlertType.INFORMATION, "Change Password", 
                "Password change functionality will be implemented in a future update.");
    }
    
    @FXML
    private void handlePhoneAction(ActionEvent event) {
        // Show dialog for phone change/add
        String action = hasPhone ? "change" : "add";
        showAlert(Alert.AlertType.INFORMATION, action + " Phone Number", 
                "Phone number " + action + " functionality will be implemented in a future update.");
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}