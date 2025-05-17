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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize the password toggle functionality
        setupPasswordToggling();
        
        // Ensure the container layout is properly calculated
        Platform.runLater(() -> {
            Parent root = nameField.getScene().getRoot();
            root.requestLayout();
        });
    }
    
    private void setupPasswordToggling() {
        // Setup for the password field
        passwordTextField = new TextField();
        passwordTextField.getStyleClass().add("text-field");
        passwordTextField.setPromptText("Password");
        passwordTextField.setManaged(false);
        passwordTextField.setVisible(false);
        
        HBox.setMargin(passwordTextField, new Insets(0, 0, 0, 5));
        passwordContainer.getChildren().add(2, passwordTextField);
        
        // Setup for the confirm password field
        confirmPasswordTextField = new TextField();
        confirmPasswordTextField.getStyleClass().add("text-field");
        confirmPasswordTextField.setPromptText("Confirm Password");
        confirmPasswordTextField.setManaged(false);
        confirmPasswordTextField.setVisible(false);
        
        HBox.setMargin(confirmPasswordTextField, new Insets(0, 0, 0, 5));
        confirmPasswordContainer.getChildren().add(2, confirmPasswordTextField);
    }

    @FXML
    public void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            // Show password
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setManaged(true);
            passwordTextField.setVisible(true);
            passwordField.setManaged(false);
            passwordField.setVisible(false);

            // Ensure the eye icon stays at the end
            HBox.setHgrow(passwordTextField, javafx.scene.layout.Priority.ALWAYS);
        } else {
            // Hide password
            passwordField.setText(passwordTextField.getText());
            passwordField.setManaged(true);
            passwordField.setVisible(true);
            passwordTextField.setManaged(false);
            passwordTextField.setVisible(false);

            // Ensure the eye icon stays at the end
            HBox.setHgrow(passwordField, javafx.scene.layout.Priority.ALWAYS);
        }
    }
    
    @FXML
    public void toggleConfirmPasswordVisibility() {
        confirmPasswordVisible = !confirmPasswordVisible;

        if (confirmPasswordVisible) {
            // Show password
            confirmPasswordTextField.setText(confirmPasswordField.getText());
            confirmPasswordTextField.setManaged(true);
            confirmPasswordTextField.setVisible(true);
            confirmPasswordField.setManaged(false);
            confirmPasswordField.setVisible(false);

            // Ensure the eye icon stays at the end
            HBox.setHgrow(confirmPasswordTextField, javafx.scene.layout.Priority.ALWAYS);
        } else {
            // Hide password
            confirmPasswordField.setText(confirmPasswordTextField.getText());
            confirmPasswordField.setManaged(true);
            confirmPasswordField.setVisible(true);
            confirmPasswordTextField.setManaged(false);
            confirmPasswordTextField.setVisible(false);

            // Ensure the eye icon stays at the end
            HBox.setHgrow(confirmPasswordField, javafx.scene.layout.Priority.ALWAYS);
        }
    }

    @FXML
    public void handleSignup(ActionEvent event) {
        // Get the input values
        String name = nameField.getText().trim();
        String matricule = matriculeField.getText().trim();
        String password = passwordVisible ? passwordTextField.getText() : passwordField.getText();
        String confirmPassword = confirmPasswordVisible ? confirmPasswordTextField.getText() : confirmPasswordField.getText();
        
        // Determine role based on matricule prefix using the new format
        String role = matricule.toUpperCase().startsWith("UNST") ? "student" : 
                      matricule.toUpperCase().startsWith("UNTS") ? "teacher" : "";
        
        // Validate all inputs
        if (name.isEmpty() || matricule.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Signup Error", "Please fill in all fields.");
            return;
        }
        
        // Validate matricule format
        if (!matricule.toUpperCase().startsWith("UNST") && !matricule.toUpperCase().startsWith("UNTS")) {
            showAlert(Alert.AlertType.ERROR, "Signup Error", 
                    "Invalid matricule format. Student IDs must start with 'UNST' and teacher IDs must start with 'UNTS'.");
            return;
        }
        
        // Validate password match
        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Signup Error", "Passwords do not match.");
            return;
        }
        
        // Check database connection
        if (!DataBaseConnection.isDatabaseConnected()) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Unable to connect to the database. Please make sure MySQL is running and properly configured.");
            return;
        }
        
        // Create a new user object
        User newUser = new User(name, password, matricule, role, null, null);
        
        // Attempt signup using AuthService
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
    
    @FXML
    public void navigateToLogin(ActionEvent event) {
        try {
            // Load the login view
            Parent loginView = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Scene loginScene = new Scene(loginView, 1920, 1080);
            
            // Get current stage
            Stage stage = (Stage) nameField.getScene().getWindow();
            
            // Set new Scene
            stage.setScene(loginScene);
            stage.setTitle("AOPFE Login");
            
            // Ensure it stays maximized
            stage.setMaximized(true);
            
            loginView.requestLayout();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to the login page.");
        }
    }
    
    // Helper method to show alerts
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}