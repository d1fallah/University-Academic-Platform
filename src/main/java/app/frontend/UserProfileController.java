package app.frontend;

import app.backend.models.User;
import app.backend.services.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Controller responsible for managing the user profile view.
 * Displays user information such as name, role, matricule number,
 * education level, university name, and contact details.
 */
public class UserProfileController implements Initializable {

    @FXML private Label nameLabel;
    @FXML private Label roleLabel;
    @FXML private Label matriculeLabel;
    @FXML private Label levelLabel;
    @FXML private Label universityLabel;
    
    @FXML private StackPane passwordChangeOverlay;
    @FXML private BorderPane passwordDialogContainer;
    @FXML private PasswordField currentPasswordField;
    @FXML private TextField currentPasswordTextField;
    @FXML private PasswordField newPasswordField;
    @FXML private TextField newPasswordTextField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordTextField;
    @FXML private Button currentPasswordEyeButton;
    @FXML private Button newPasswordEyeButton;
    @FXML private Button confirmPasswordEyeButton;
    @FXML private Button changePasswordButton;
    @FXML private Button cancelPasswordButton;
    
    private User currentUser;
    private boolean currentPasswordVisible = false;
    private boolean newPasswordVisible = false;
    private boolean confirmPasswordVisible = false;
    
    // Password validation pattern: minimum 8 characters, at least one uppercase letter,
    // one lowercase letter, one digit, and one special character
    private static final Pattern PASSWORD_PATTERN = 
                                Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    
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
     * Shows the password change dialog overlay.
     * 
     * @param event The action event
     */
    @FXML
    private void handleChangePassword(ActionEvent event) {
        showPasswordChangeDialog();
    }
    
    /**
     * Shows the password change dialog overlay.
     */
    private void showPasswordChangeDialog() {
        clearPasswordFields();
        passwordChangeOverlay.setVisible(true);
        passwordChangeOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
    }
    
    /**
     * Clears all password fields in the form.
     */
    private void clearPasswordFields() {
        currentPasswordField.clear();
        currentPasswordTextField.clear();
        newPasswordField.clear();
        newPasswordTextField.clear();
        confirmPasswordField.clear();
        confirmPasswordTextField.clear();
        
        resetPasswordVisibility();
    }
    
    /**
     * Resets all password fields to masked state.
     */
    private void resetPasswordVisibility() {
        if (currentPasswordVisible) {
            toggleCurrentPasswordVisibility();
        }
        if (newPasswordVisible) {
            toggleNewPasswordVisibility();
        }
        if (confirmPasswordVisible) {
            toggleConfirmPasswordVisibility();
        }
    }
    
    /**
     * Toggles the visibility of the current password field.
     */
    @FXML
    private void toggleCurrentPasswordVisibility() {
        currentPasswordVisible = !currentPasswordVisible;
        
        if (currentPasswordVisible) {
            currentPasswordTextField.setText(currentPasswordField.getText());
            currentPasswordTextField.setVisible(true);
            currentPasswordTextField.setManaged(true);
            currentPasswordField.setVisible(false);
            currentPasswordField.setManaged(false);
        } else {
            currentPasswordField.setText(currentPasswordTextField.getText());
            currentPasswordField.setVisible(true);
            currentPasswordField.setManaged(true);
            currentPasswordTextField.setVisible(false);
            currentPasswordTextField.setManaged(false);
        }
    }
    
    /**
     * Toggles the visibility of the new password field.
     */
    @FXML
    private void toggleNewPasswordVisibility() {
        newPasswordVisible = !newPasswordVisible;
        
        if (newPasswordVisible) {
            newPasswordTextField.setText(newPasswordField.getText());
            newPasswordTextField.setVisible(true);
            newPasswordTextField.setManaged(true);
            newPasswordField.setVisible(false);
            newPasswordField.setManaged(false);
        } else {
            newPasswordField.setText(newPasswordTextField.getText());
            newPasswordField.setVisible(true);
            newPasswordField.setManaged(true);
            newPasswordTextField.setVisible(false);
            newPasswordTextField.setManaged(false);
        }
    }
    
    /**
     * Toggles the visibility of the confirm password field.
     */
    @FXML
    private void toggleConfirmPasswordVisibility() {
        confirmPasswordVisible = !confirmPasswordVisible;
        
        if (confirmPasswordVisible) {
            confirmPasswordTextField.setText(confirmPasswordField.getText());
            confirmPasswordTextField.setVisible(true);
            confirmPasswordTextField.setManaged(true);
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
        } else {
            confirmPasswordField.setText(confirmPasswordTextField.getText());
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            confirmPasswordTextField.setVisible(false);
            confirmPasswordTextField.setManaged(false);
        }
    }
    
    /**
     * Handles the password change form submission.
     * 
     * @param event The action event
     */
    @FXML
    private void handlePasswordChangeSubmit(ActionEvent event) {
        try {
            String currentPassword = currentPasswordVisible ? currentPasswordTextField.getText() : currentPasswordField.getText();
            String newPassword = newPasswordVisible ? newPasswordTextField.getText() : newPasswordField.getText();
            String confirmPassword = confirmPasswordVisible ? confirmPasswordTextField.getText() : confirmPasswordField.getText();
            
            if (!validatePasswordChangeInputs(currentPassword, newPassword, confirmPassword)) {
                return;
            }
            
            boolean success = AuthService.updatePassword(currentUser.getMatricule(), currentPassword, newPassword);
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Password Changed", 
                        "Your password has been successfully updated.");
                hidePasswordChangeDialog();
            } else {
                showAlert(Alert.AlertType.ERROR, "Password Change Failed", 
                        "Failed to update password. Please verify your current password and try again.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        }
    }
    
    /**
     * Handles the password change form cancellation.
     * 
     * @param event The action event
     */
    @FXML
    private void handlePasswordChangeCancel(ActionEvent event) {
        hidePasswordChangeDialog();
    }
    
    /**
     * Hides the password change dialog overlay.
     */
    private void hidePasswordChangeDialog() {
        passwordChangeOverlay.setVisible(false);
        clearPasswordFields();
    }
    
    /**
     * Validates password change inputs.
     * 
     * @param currentPassword Current password
     * @param newPassword New password
     * @param confirmPassword Confirmation password
     * @return true if inputs are valid, false otherwise
     */
    private boolean validatePasswordChangeInputs(String currentPassword, String newPassword, String confirmPassword) {
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill in all password fields.");
            return false;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "New password and confirmation do not match.");
            return false;
        }
        
        if (currentPassword.equals(newPassword)) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "New password must be different from current password.");
            return false;
        }
        
        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            showAlert(Alert.AlertType.ERROR, "Password Requirements", 
                    "Password must be at least 8 characters long and include:\n" +
                    "• At least one uppercase letter\n" +
                    "• At least one lowercase letter\n" +
                    "• At least one digit\n" +
                    "• At least one special character (@$!%*?&)");
            return false;
        }
        
        return true;
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