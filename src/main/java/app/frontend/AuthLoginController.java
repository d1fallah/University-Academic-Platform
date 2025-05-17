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
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller class for the login screen.
 * Handles user authentication, password visibility toggling, and navigation to other screens.
 * 
 * @author Sellami Mohamed Odai
 */
public class AuthLoginController implements Initializable {

    /** Password field for secured password input */
    @FXML private PasswordField passwordField;
    
    /** Username/matricule input field */
    @FXML private TextField usernameField;
    
    /** Container for password fields */
    @FXML private HBox passwordContainer;

    /** Text field for showing password in clear text */
    private TextField passwordTextField;
    
    /** Tracks whether password is visible */
    private boolean passwordVisible = false;
    
    /** Current logged-in user */
    private static User currentUser = null;

    /**
     * Returns the current logged-in user.
     * 
     * @return The current user or null if no user is logged in
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Sets the current logged-in user.
     * 
     * @param user The user to set as current user
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }
    
    /**
     * Loads the login view for a given stage.
     * Sets up the scene, configures stage properties and ensures proper layout.
     * 
     * @param stage The stage to load the login view into
     * @throws IOException If the login view cannot be loaded
     */
    public static void loadLoginView(Stage stage) throws IOException {
        Parent loginView = FXMLLoader.load(AuthLoginController.class.getResource("/fxml/AuthLogin.fxml"));
        Scene loginScene = new Scene(loginView, 1920, 1080);
        
        stage.setScene(loginScene);
        stage.setTitle("AOPFE Login");
        stage.setMaximized(true);
        
        loginView.requestLayout();
    }

    /**
     * Initializes the controller.
     * Sets up database connection and configures the password text field.
     *
     * @param url The location used to resolve relative paths
     * @param resourceBundle The resources used by this controller
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        DataBaseConnection.getConnection();
        
        passwordTextField = new TextField();
        passwordTextField.getStyleClass().add("text-field");
        passwordTextField.setPromptText("Password");
        passwordTextField.setManaged(false);
        passwordTextField.setVisible(false);
        HBox.setMargin(passwordTextField, new Insets(0, 0, 0, 5));
        passwordContainer.getChildren().add(2, passwordTextField);
    }

    /**
     * Toggles visibility of the password between masked and clear text.
     * Switches between password field and text field while preserving the input value.
     */
    @FXML
    public void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setManaged(true);
            passwordTextField.setVisible(true);
            passwordField.setManaged(false);
            passwordField.setVisible(false);
            HBox.setHgrow(passwordTextField, Priority.ALWAYS);
        } else {
            passwordField.setText(passwordTextField.getText());
            passwordField.setManaged(true);
            passwordField.setVisible(true);
            passwordTextField.setManaged(false);
            passwordTextField.setVisible(false);
            HBox.setHgrow(passwordField, Priority.ALWAYS);
        }
    }

    /**
     * Handles the login button click event.
     * Validates user input, authenticates credentials and navigates to the appropriate screen.
     *
     * @param event The action event triggered by the login button
     */
    @FXML
    public void handleLogin(ActionEvent event) {
        String matricule = usernameField.getText().trim();
        String password = passwordVisible ? passwordTextField.getText() : passwordField.getText();
        
        if (matricule.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Please enter both matricule and password.");
            return;
        }
        
        if (!DataBaseConnection.isDatabaseConnected()) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Unable to connect to the database. Please make sure MySQL is running and properly configured.");
            return;
        }
        
        User user = AuthService.login(matricule, password);
        
        if (user == null) {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid matricule or password.");
            return;
        }
        
        currentUser = user;
        
        try {
            Parent loadingView = FXMLLoader.load(getClass().getResource("/fxml/AuthLoading.fxml"));
            Scene loadingScene = new Scene(loadingView, 1920, 1080);
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(loadingScene);
            stage.setMaximized(true);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to start the loading screen.");
        }
    }
    
    /**
     * Navigates to the signup screen when the signup button is clicked.
     * 
     * @param event The action event triggered by the signup button
     */
    @FXML
    public void navigateToSignup(ActionEvent event) {
        try {
            Parent signupView = FXMLLoader.load(getClass().getResource("/fxml/AuthSignup.fxml"));
            Scene signupScene = new Scene(signupView, 1920, 1080);
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(signupScene);
            stage.setTitle("AOPFE Sign Up");
            
            signupView.requestLayout();
            stage.setMaximized(true);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to the signup page.");
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
