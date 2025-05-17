package app.frontend;

import app.backend.database.DataBaseConnection;
import app.backend.models.User;
import app.backend.services.AuthService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Controller class for the signup view of the application.
 * Handles user registration functionality including form validation
 * and toggle password visibility.
 *
 * @author Sellami Mohamed Odai
 */
public class AuthSignupController implements Initializable {

    @FXML private TextField nameField;
    @FXML private TextField matriculeField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private HBox passwordContainer;
    @FXML private HBox confirmPasswordContainer;
    
    private TextField passwordTextField;
    private TextField confirmPasswordTextField;
    private boolean passwordVisible = false;
    private boolean confirmPasswordVisible = false;
    
    // Password validation pattern: minimum 8 characters, at least one uppercase letter,
    // one lowercase letter, one digit, and one special character
    private static final Pattern PASSWORD_PATTERN = 
                                Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");    
        
    /**
     * Initializes the controller after its root element has been completely processed.
     * Sets up password toggling and ensures proper layout calculation.
     * 
     * @param url The location used to resolve relative paths for the root object
     * @param resourceBundle The resources used to localize the root object
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupPasswordToggling();
        
        Platform.runLater(() -> {
            Parent root = nameField.getScene().getRoot();
            root.requestLayout();
        });
    }
    
    /**
     * Sets up the password toggle functionality by creating text fields
     * that can be swapped with password fields to show/hide passwords.
     */
    private void setupPasswordToggling() {
        configureTextField(passwordTextField = new TextField(), "Password", passwordContainer);
        configureTextField(confirmPasswordTextField = new TextField(), "Confirm Password", confirmPasswordContainer);
    }
    
    /**
     * Configures a text field for password visibility toggling.
     * 
     * @param textField The text field to configure
     * @param promptText The prompt text to display
     * @param container The container to add the text field to
     */
    private void configureTextField(TextField textField, String promptText, HBox container) {
        textField.getStyleClass().add("text-field");
        textField.setPromptText(promptText);
        textField.setManaged(false);
        textField.setVisible(false);
        
        HBox.setMargin(textField, new Insets(0, 0, 0, 5));
        container.getChildren().add(2, textField);
    }    
    
    /**
     * Toggles the visibility of the password field.
     * Switches between the password field and text field to show/hide the password.
     */
    @FXML
    public void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        toggleFieldVisibility(passwordVisible, passwordField, passwordTextField);
    }
    
    /**
     * Toggles the visibility of the confirm password field.
     * Switches between the password field and text field to show/hide the password.
     */
    @FXML
    public void toggleConfirmPasswordVisibility() {
        confirmPasswordVisible = !confirmPasswordVisible;
        toggleFieldVisibility(confirmPasswordVisible, confirmPasswordField, confirmPasswordTextField);
    }
    
    /**
     * Helper method to toggle field visibility between password and text fields.
     * 
     * @param showText Whether to show the text field (true) or password field (false)
     * @param passwordField The password field to toggle
     * @param textField The text field to toggle
     */
    private void toggleFieldVisibility(boolean showText, PasswordField passwordField, TextField textField) {
        if (showText) {
            textField.setText(passwordField.getText());
            textField.setManaged(true);
            textField.setVisible(true);
            passwordField.setManaged(false);
            passwordField.setVisible(false);
            HBox.setHgrow(textField, javafx.scene.layout.Priority.ALWAYS);
        } else {
            passwordField.setText(textField.getText());
            passwordField.setManaged(true);
            passwordField.setVisible(true);
            textField.setManaged(false);
            textField.setVisible(false);
            HBox.setHgrow(passwordField, javafx.scene.layout.Priority.ALWAYS);
        }
    }    
    
    /**
     * Handles the signup process when the signup button is clicked.
     * Validates inputs, creates a new user, and attempts registration.
     * 
     * @param event The action event triggered by the signup button
     */
    @FXML
    public void handleSignup(ActionEvent event) {
        String name = nameField.getText().trim();
        String matricule = matriculeField.getText().trim();
        String password = passwordVisible ? passwordTextField.getText() : passwordField.getText();
        String confirmPassword = confirmPasswordVisible ? confirmPasswordTextField.getText() : confirmPasswordField.getText();
        
        // Determine role based on matricule prefix
        String role = matricule.toUpperCase().startsWith("UNST") ? "student" : 
                      matricule.toUpperCase().startsWith("UNTS") ? "teacher" : "";
        
        if (!validateInputFields(name, matricule, password, confirmPassword)) {
            return;
        }
        
        if (!DataBaseConnection.isDatabaseConnected()) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Unable to connect to the database. Please make sure MySQL is running and properly configured.");
            return;
        }
        
        User newUser = new User(name, password, matricule, role, null, null);
        boolean signupSuccess = AuthService.signup(newUser);
        
        if (signupSuccess) {
            showAlert(Alert.AlertType.INFORMATION, "Signup Successful", 
                    "Your account has been created successfully as a " + role + ".\nYou can now login.");
            navigateToLogin(event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Signup Failed", 
                    "Failed to create account. Your matricule may be invalid or already in use.");
        }
    }
    
    /**
     * Validates all input fields for signup form.
     * 
     * @param name User's full name
     * @param matricule User's matricule/ID
     * @param password User's password
     * @param confirmPassword Password confirmation
     * @return true if all inputs are valid, false otherwise
     */
    private boolean validateInputFields(String name, String matricule, String password, String confirmPassword) {
        if (name.isEmpty() || matricule.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Signup Error", "Please fill in all fields.");
            return false;
        }
        
        if (!matricule.toUpperCase().startsWith("UNST") && !matricule.toUpperCase().startsWith("UNTS")) {
            showAlert(Alert.AlertType.ERROR, "Signup Error", 
                    "Invalid matricule format. Student IDs must start with 'UNST' and teacher IDs must start with 'UNTS'.");
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Signup Error", "Passwords do not match.");
            return false;
        }
        
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            showAlert(Alert.AlertType.ERROR, "Password Error", 
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
     * Navigates to the login screen after successful signup or when 
     * the user clicks the login link.
     * 
     * @param event The action event that triggered the navigation
     */
    @FXML
    public void navigateToLogin(ActionEvent event) {
        try {
            Parent loginView = FXMLLoader.load(getClass().getResource("/fxml/AuthLogin.fxml"));
            Scene loginScene = new Scene(loginView, 1920, 1080);
            
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(loginScene);
            stage.setTitle("AOPFE Login");
            stage.setMaximized(true);
            
            loginView.requestLayout();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to the login page.");
        }
    }
    
    /**
     * Displays an alert dialog with the specified type, title, and message.
     * 
     * @param alertType The type of alert to display
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