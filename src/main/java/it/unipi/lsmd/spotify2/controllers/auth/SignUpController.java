package it.unipi.lsmd.spotify2.controllers.auth;

import it.unipi.lsmd.spotify2.configs.Neo4jDBConfig;
import it.unipi.lsmd.spotify2.daos.impl.mongo.listener.MongoListenerDaoImpl;
import it.unipi.lsmd.spotify2.daos.impl.neo4j.NeoListenerDaoImpl;
import it.unipi.lsmd.spotify2.daos.mongo.listener.MongoListenerDao;
import it.unipi.lsmd.spotify2.daos.neo4j.NeoListenerDao;
import it.unipi.lsmd.spotify2.models.Listener;
import it.unipi.lsmd.spotify2.pageManagement.PageManager;
import it.unipi.lsmd.spotify2.services.account.SignUpService;
import it.unipi.lsmd.spotify2.utils.AlertMessages;
import it.unipi.lsmd.spotify2.utils.ComboBoxInitializeUtil;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

// Controller for the sign-up page
public class SignUpController {

    // Reference to the error message label
    @FXML
    private Label errorMessageLabel;

    // Fields for entering new user information
    @FXML
    private TextField newEmailField;
    @FXML
    private ComboBox<String> countryComboBox;
    @FXML
    private TextField photoTextField;
    @FXML
    private VBox signUpVBox;
    @FXML
    private TextField newUsernameField;
    @FXML
    private PasswordField newPasswordField;

    // Method called during initialization to populate the country ComboBox
    @FXML
    public void initialize() {
        // Get the list of countries and add them to the country ComboBox
        List<String> countryList = ComboBoxInitializeUtil.getCountries();
        countryComboBox.getItems().addAll(countryList);
    }

    // Method called when the sign-up button is clicked
    @FXML
    private void signUp() {
        // Extract user input from the form fields
        String username = newUsernameField.getText().trim();
        String password = newPasswordField.getText().trim();
        String email = newEmailField.getText().trim();
        String country = countryComboBox.getValue();
        String photo = photoTextField.getText().trim();

        // Check if any field is empty and display an error message if necessary
        if (username.isEmpty() || password.isEmpty() || email.isEmpty()
                || country == null || photo.isEmpty()) {
            errorMessageLabel.setText("Please fill in all fields.");
        } else {
            // Clear any previous error message
            errorMessageLabel.setText("");

            // Create instances of DAOs and services
            MongoListenerDao mongoListenerDao = new MongoListenerDaoImpl();
            NeoListenerDao neoListenerDao = new NeoListenerDaoImpl(Neo4jDBConfig.getInstance().getDriver());
            SignUpService signUpService = new SignUpService(mongoListenerDao, neoListenerDao);

            // Create a new Listener instance with entered information
            Listener newListener = new Listener();
            newListener.setCountry(country);
            newListener.setEmail(email);
            newListener.setListenerImage(photo);
            newListener.setUsername(username);
            newListener.setPassword(password);

            // Attempt to sign up a new user
            if (signUpService.signup(newListener)) {
                // Display an information alert for a successful sign-up
                AlertMessages.informationAlert("Sign-up successful!");
                // Get the stage associated with the VBox
                Stage stage = (Stage) signUpVBox.getScene().getWindow();
                // Obtain the PageManager instance and switch to the sign-in page
                PageManager pageManager = PageManager.getInstance(stage);
                pageManager.switchToSignIn();
            } else {
                // Display an information alert for a failed sign-up attempt (due to a username conflict)
                AlertMessages.informationAlert("Sign-up failed. Username already used.");
                // Clear the entered username and password
                newUsernameField.clear();
                newPasswordField.clear();
            }
        }
    }

    // Method called when the "Sign In" button is clicked to switch to the sign-in page
    @FXML
    private void switchToSignIn() {
        // Get the stage associated with the VBox
        Stage stage = (Stage) signUpVBox.getScene().getWindow();
        // Obtain the PageManager instance and switch to the sign-in page
        PageManager pageManager = PageManager.getInstance(stage);
        pageManager.switchToSignIn();
    }
}
