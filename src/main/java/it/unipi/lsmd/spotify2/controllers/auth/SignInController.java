package it.unipi.lsmd.spotify2.controllers.auth;

import it.unipi.lsmd.spotify2.daos.impl.mongo.listener.MongoListenerDaoImpl;
import it.unipi.lsmd.spotify2.daos.mongo.listener.MongoListenerDao;
import it.unipi.lsmd.spotify2.models.Listener;
import it.unipi.lsmd.spotify2.pageManagement.PageManager;
import it.unipi.lsmd.spotify2.pageManagement.PageTypeEnum;
import it.unipi.lsmd.spotify2.services.account.SignInService;
import it.unipi.lsmd.spotify2.utils.AlertMessages;
import it.unipi.lsmd.spotify2.utils.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// Controller for the sign-in page
public class SignInController {

    // Reference to the main VBox in the FXML file
    public VBox signInVBox;

    // Fields for entering username and password
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    // Method called when the sign-in button is clicked
    @FXML
    private void signIn() {
        // Remove any leading or trailing whitespaces from the username
        String username = usernameField.getText().replace(" ", "");
        String password = passwordField.getText();

        // Create instances of DAOs and services
        MongoListenerDao mongoListenerDao = new MongoListenerDaoImpl();
        SignInService loginService = new SignInService(mongoListenerDao);

        // Create a new Listener instance with entered username and password
        Listener newListener = new Listener();
        newListener.setUsername(username);
        newListener.setPassword(password);

        // Set the logged-in user in the UserSession
        UserSession.getInstance().setLoggedInUser(username);

        // Get the stage associated with the VBox
        Stage stage = (Stage) signInVBox.getScene().getWindow();
        // Obtain the PageManager instance
        PageManager pageManager = PageManager.getInstance(stage);

        // Attempt to sign in the user and get the access level
        int access = loginService.login(newListener);

        // Switch to the appropriate page based on the access level
        if (access == -1) {
            // Admin access
            pageManager.switchToAdminPage();
        } else if (access == 1) {
            // Normal user access
            pageManager.switchToHomepage(PageTypeEnum.SIGN_IN_PAGE);
        } else {
            // Display an information alert for a failed sign-in attempt
            AlertMessages.informationAlert("Sign-in failed. Username or password is incorrect.");
            // Clear the entered username and password
            usernameField.clear();
            passwordField.clear();
        }
    }

    // Method called when the "Sign Up" button is clicked to switch to the sign-up page
    @FXML
    private void switchToSignUp() {
        // Get the stage associated with the VBox
        Stage stage = (Stage) signInVBox.getScene().getWindow();
        // Obtain the PageManager instance and switch to the sign-up page
        PageManager pageManager = PageManager.getInstance(stage);
        pageManager.switchToSignUp();
    }
}
