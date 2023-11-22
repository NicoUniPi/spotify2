package it.unipi.lsmd.spotify2.controllers.social;

import it.unipi.lsmd.spotify2.configs.Neo4jDBConfig;
import it.unipi.lsmd.spotify2.daos.impl.mongo.listener.MongoListenerDaoImpl;
import it.unipi.lsmd.spotify2.daos.impl.neo4j.NeoListenerDaoImpl;
import it.unipi.lsmd.spotify2.daos.mongo.listener.MongoListenerDao;
import it.unipi.lsmd.spotify2.daos.neo4j.NeoListenerDao;
import it.unipi.lsmd.spotify2.dtos.ListenerDTO;
import it.unipi.lsmd.spotify2.models.Listener;
import it.unipi.lsmd.spotify2.pageManagement.PageManager;
import it.unipi.lsmd.spotify2.pageManagement.PageTypeEnum;
import it.unipi.lsmd.spotify2.services.account.ManageAccountService;
import it.unipi.lsmd.spotify2.services.social.FollowingSocialService;
import it.unipi.lsmd.spotify2.utils.AlertMessages;
import it.unipi.lsmd.spotify2.utils.ComboBoxInitializeUtil;
import it.unipi.lsmd.spotify2.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Objects;

import static it.unipi.lsmd.spotify2.utils.UpdateDocument.setLabelClickHandler;

// Controller class for the Listener Details Page
public class ListenerDetailsPageController {

    // Information about the listener associated with the page
    private final ListenerDTO listener;

    // The root page type, used for navigation
    private final PageTypeEnum rootPage;

    // Reference to the main VBox layout of the listener details page
    @FXML
    private VBox mainVBox;

    // ToggleButton for following/unfollowing the listener
    @FXML
    private ToggleButton followButton;

    // Labels displaying listener information
    @FXML
    private Label userNameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label countryLabel;

    // Button for deleting the listener's account
    @FXML
    private Button deleteAccountButton;

    // ImageView displaying the listener's profile image
    @FXML
    private ImageView listenerImage;

    // Button for saving changes to the listener's information
    @FXML
    private Button saveButton;

    // TextFields and ComboBox for editing listener information
    @FXML
    private TextField usernameTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private ComboBox<String> countryComboBox;

    // Constructor for initializing the listener and root page
    public ListenerDetailsPageController(ListenerDTO listener, PageTypeEnum rootPage) {
        this.listener = listener;
        this.rootPage = rootPage;
    }

    // Method called during initialization of the controller
    @FXML
    private void initialize() {
        // Create DAOs and services for both MongoDB and Neo4j
        MongoListenerDao mongoListenerDao = new MongoListenerDaoImpl();
        // Retrieve the full Listener object from MongoDB
        Listener newListener = mongoListenerDao.getListenerByUsername(listener.getUsername());

        // Set initial values for labels and image
        userNameLabel.setText(newListener.getUsername());
        emailLabel.setText(newListener.getEmail());
        Image image = new Image(newListener.getListenerImage());
        listenerImage.setImage(image);
        countryLabel.setText(newListener.getCountry());

        // Configure UI elements based on the root page
        if (rootPage == PageTypeEnum.SEARCH_LISTENERS_PAGE) {
            configureForSearchListenersPage();
        } else {
            configureForOtherPages();
        }
    }

    // Configure UI elements for the Search Listeners page
    private void configureForSearchListenersPage() {
        // Disable and hide elements not relevant for search listeners page
        saveButton.setDisable(true);
        saveButton.setManaged(false);
        saveButton.setVisible(false);
        deleteAccountButton.setDisable(true);
        deleteAccountButton.setVisible(false);
        deleteAccountButton.setManaged(false);

        // Get the current user session
        UserSession userSession = UserSession.getInstance();

        // Initialize Neo4j DAO and FollowingSocialService for managing follow status
        NeoListenerDao neoListenerDao = new NeoListenerDaoImpl(Neo4jDBConfig.getInstance().getDriver());
        FollowingSocialService followingSocialService = new FollowingSocialService(neoListenerDao);

        // Check if the current user is already following the displayed listener
        if (followingSocialService.checkIfFollowing(userSession.getLoggedInUser(), listener.getUsername())) {
            followButton.setText("unfollow");
            followButton.setSelected(true);
        }
    }

    // Configure UI elements for other pages
    private void configureForOtherPages() {
        // Disable and hide follow button for other pages
        followButton.setDisable(true);
        followButton.setVisible(false);

        // Set click handlers for labels to enable editing
        setLabelClickHandler(userNameLabel, usernameTextField);
        setLabelClickHandler(emailLabel, emailTextField);

        // Initialize and populate the countryComboBox
        List<String> countriesList = ComboBoxInitializeUtil.getCountries();
        countryComboBox.getItems().addAll(countriesList);

        // Set click handler for countryLabel to enable editing through the ComboBox
        setLabelClickHandler(countryLabel, countryComboBox);
    }

    // Event handler for the back button, navigates to the appropriate page based on the root page
    @FXML
    private void handleBackButton(ActionEvent actionEvent) {
        Stage stage = (Stage) mainVBox.getScene().getWindow();
        PageManager pageManager = PageManager.getInstance(stage);

        // Navigate to social or homepage based on the root page
        if (rootPage == PageTypeEnum.SEARCH_LISTENERS_PAGE)
            pageManager.switchToSocial(PageTypeEnum.HOMEPAGE, PageTypeEnum.SEARCH_LISTENERS_PAGE);
        if (rootPage == PageTypeEnum.HOMEPAGE) {
            pageManager.switchToHomepage(PageTypeEnum.SIGN_IN_PAGE);
        }
    }

    // Event handler for the follow/unfollow button
    @FXML
    private void handleFollowButton(ActionEvent actionEvent) {
        // Get database configuration and current user session
        Neo4jDBConfig neo4jDBConfig = Neo4jDBConfig.getInstance();
        UserSession userSession = UserSession.getInstance();

        // Initialize Neo4j DAO and FollowingSocialService
        NeoListenerDao neoListenerDao = new NeoListenerDaoImpl(neo4jDBConfig.getDriver());
        FollowingSocialService followingSocialService = new FollowingSocialService(neoListenerDao);

        // Update follow status based on the button state
        if (followButton.isSelected()) {
            followingSocialService.followUser(userSession.getLoggedInUser(), listener.getUsername());
            followButton.setText("unfollow");
        } else {
            followingSocialService.unfollowUser(userSession.getLoggedInUser(), listener.getUsername());
            followButton.setText("follow");
        }
    }

    // Event handler for the delete account button
    @FXML
    private void handleDeleteAccount() {
        // Display a confirmation alert before proceeding with account deletion
        if (AlertMessages.confirmationAlert("Are you sure you want to delete the account?")) {
            // Get database configuration and current user session
            Neo4jDBConfig neo4jDBConfig = Neo4jDBConfig.getInstance();
            UserSession userSession = UserSession.getInstance();

            // Initialize DAOs and services for both MongoDB and Neo4j
            NeoListenerDao neoListenerDao = new NeoListenerDaoImpl(neo4jDBConfig.getDriver());
            MongoListenerDao mongoListenerDao = new MongoListenerDaoImpl();
            ManageAccountService manageAccountService = new ManageAccountService(mongoListenerDao, neoListenerDao);

            // Delete the account and navigate to the sign-in page
            manageAccountService.deleteAccount(userSession.getLoggedInUser());
            Stage stage = (Stage) mainVBox.getScene().getWindow();
            PageManager pageManager = PageManager.getInstance(stage);
            pageManager.switchToSignIn();
        }
    }

    // Event handler for the save button, updates listener information in the database
    @FXML
    private void handleSaveButton() {
        // Display a confirmation alert before proceeding with the update
        if (AlertMessages.confirmationAlert("You will overwrite the listener information. Do you want to confirm?")) {
            // Initialize DAOs and services for both MongoDB and Neo4j
            MongoListenerDao mongoListenerDao = new MongoListenerDaoImpl();

            // Get the new username from the TextField
            String newUsername = usernameTextField.getText();

            // Check if the new username is the same as the logged-in user's username
            if (Objects.equals(newUsername, UserSession.getInstance().getLoggedInUser())) {
                // Create a new Listener object with updated information
                Listener newListener = new Listener();
                newListener.setUsername(newUsername);
                newListener.setEmail(emailLabel.getText());
                newListener.setCountry(countryLabel.getText());

                // Update the listener information in MongoDB
                mongoListenerDao.updateListener(newListener, newListener.getUsername(), null);

                // Display an information alert about the successful update
                AlertMessages.informationAlert("The listener has been updated");
                return;
            }

            // Check if the new username is already taken
            if (mongoListenerDao.isUsernameTaken(newUsername)) {
                AlertMessages.informationAlert("The username is already taken");
                return;
            }

            // Create a new Listener object with updated information
            Listener newListener = new Listener();
            newListener.setUsername(newUsername);
            newListener.setEmail(emailLabel.getText());
            newListener.setCountry(countryLabel.getText());

            NeoListenerDao neoListenerDao = new NeoListenerDaoImpl(Neo4jDBConfig.getInstance().getDriver());

            // Initialize ManageAccountService for updating the account in both MongoDB and Neo4j
            ManageAccountService manageAccountService = new ManageAccountService(mongoListenerDao, neoListenerDao);

            // Update the account information in both databases
            manageAccountService.updateAccount(newListener, listener.getUsername());

            UserSession.getInstance().setLoggedInUser(newUsername);

            // Display an information alert about the successful update
            AlertMessages.informationAlert("The listener has been updated");
        }
    }
}
