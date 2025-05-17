package app.frontend;

import app.backend.database.DataBaseConnection;
import app.backend.models.User;
import app.backend.services.AuthService;
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

public class AuthLoginController implements Initializable {

    @FXML private PasswordField passwordField;
    @FXML private TextField usernameField;
    @FXML private HBox passwordContainer;

    private TextField passwordTextField;
    private boolean passwordVisible = false;
    private static User currentUser = null;

    
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Sets the current user
     * 
     * @param user The user to set as current user
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }
    
    /**
     * Loads the login view for a given stage
     * 
     * @param stage The stage to load the login view into
     * @throws IOException If the login view cannot be loaded
     */
    public static void loadLoginView(Stage stage) throws IOException {
        // Load the login view
        Parent loginView = FXMLLoader.load(AuthLoginController.class.getResource("/fxml/login.fxml"));
        Scene loginScene = new Scene(loginView, 1920, 1080);
        
        // Set new Scene
        stage.setScene(loginScene);
        stage.setTitle("AOPFE Login");
        
        // Ensure it stays maximized
        stage.setMaximized(true);
        
        loginView.requestLayout();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize the database connection when the login screen loads
        DataBaseConnection.getConnection();
        
        passwordTextField = new TextField();
        passwordTextField.getStyleClass().add("text-field");
        passwordTextField.setPromptText("Password");
        passwordTextField.setManaged(false);
        passwordTextField.setVisible(false);

        HBox.setMargin(passwordTextField, new Insets(0, 0, 0, 5));

        passwordContainer.getChildren().add(2, passwordTextField);
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
    public void handleLogin(ActionEvent event) {
        // Get the input values
        String matricule = usernameField.getText().trim();
        String password = passwordVisible ? passwordTextField.getText() : passwordField.getText();
        
        // Validate input
        if (matricule.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Please enter both matricule and password.");
            return;
        }
        
        // Check database connection
        if (!DataBaseConnection.isDatabaseConnected()) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Unable to connect to the database. Please make sure MySQL is running and properly configured.");
            return;
        }
        
        // Attempt login
        User user = AuthService.login(matricule, password);
        
        if (user == null) {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid matricule or password.");
            return;
        }
        
        // Login successful
        // Store the current user
        currentUser = user;
        
        // Load the main screen based on user role
        try {
            // Load the loading screen
            Parent loadingView = FXMLLoader.load(getClass().getResource("/fxml/loading.fxml"));
            Scene loadingScene = new Scene(loadingView, 1920, 1080);
            
            // Get current stage
            Stage stage = (Stage) usernameField.getScene().getWindow();
            
            // Set new Scene
            stage.setScene(loadingScene);
            stage.setMaximized(true);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to start the loading screen.");
        }
    }
    
    @FXML
    public void navigateToSignup(ActionEvent event) {
        try {
            // Load the signup view
            Parent signupView = FXMLLoader.load(getClass().getResource("/fxml/signup.fxml"));
            Scene signupScene = new Scene(signupView, 1920, 1080);
            
            // Get current stage
            Stage stage = (Stage) usernameField.getScene().getWindow();
            
            // Set new Scene
            stage.setScene(signupScene);
            stage.setTitle("AOPFE Sign Up");
            
            // Force layout recalculation to apply proper padding
            signupView.requestLayout();

            // Ensure it stays maximized
            stage.setMaximized(true);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to the signup page.");
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
