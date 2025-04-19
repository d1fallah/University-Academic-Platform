package app.frontend;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private PasswordField passwordField;
    @FXML private TextField usernameField;
    @FXML private HBox passwordContainer;

    private TextField passwordTextField;
    private boolean passwordVisible = false;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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

        // Store the current children except the text fields
        List<Node> otherNodes = new ArrayList<>();
        Node iconContainer = passwordContainer.getChildren().get(0);
        Node divider = passwordContainer.getChildren().get(1);
        Node eyeIcon = passwordContainer.getChildren().get(passwordContainer.getChildren().size() - 1);

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
        System.out.println("Login button clicked");
    }
}
