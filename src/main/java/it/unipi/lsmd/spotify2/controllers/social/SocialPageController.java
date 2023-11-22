package it.unipi.lsmd.spotify2.controllers.social;

import it.unipi.lsmd.spotify2.cellFactories.SearchListenerCellFactory;
import it.unipi.lsmd.spotify2.configs.Neo4jDBConfig;
import it.unipi.lsmd.spotify2.daos.impl.mongo.listener.MongoListenerDaoImpl;
import it.unipi.lsmd.spotify2.daos.impl.neo4j.NeoListenerDaoImpl;
import it.unipi.lsmd.spotify2.daos.impl.neo4j.NeoSongDaoImpl;
import it.unipi.lsmd.spotify2.daos.mongo.listener.MongoListenerDao;
import it.unipi.lsmd.spotify2.daos.neo4j.NeoListenerDao;
import it.unipi.lsmd.spotify2.daos.neo4j.NeoSongDao;
import it.unipi.lsmd.spotify2.dtos.ListenerDTO;
import it.unipi.lsmd.spotify2.pageManagement.PageManager;
import it.unipi.lsmd.spotify2.pageManagement.PageTypeEnum;
import it.unipi.lsmd.spotify2.services.SearchBarService;
import it.unipi.lsmd.spotify2.services.aggr.neo.NeoAggregationService;
import it.unipi.lsmd.spotify2.utils.AlertMessages;
import it.unipi.lsmd.spotify2.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.neo4j.driver.Driver;

import java.time.LocalDate;
import java.util.List;

// Controller class for the Social Page
public class SocialPageController {

    // Enum representing the root page type for navigation purposes
    private final PageTypeEnum rootPage;

    // ListView displaying search results for friends
    @FXML
    private ListView<ListenerDTO> searchFriendsListView;

    // VBox layout of the social page
    @FXML
    private VBox vBox;

    // TextField for user input in the search bar
    @FXML
    private TextField searchField;

    // Button to navigate to the friends page
    @FXML
    private Button friendsButton;

    // VBox layout containing date pickers for admin functionality
    @FXML
    private VBox datePicker;

    // DatePicker for start date in admin functionality
    @FXML
    private DatePicker startDatePicker;

    // DatePicker for end date in admin functionality
    @FXML
    private DatePicker endDatePicker;

    // Constructor with a specified root page type
    public SocialPageController(PageTypeEnum rootPage) {
        this.rootPage = rootPage;
    }

    // Method called during initialization of the controller
    @FXML
    private void initialize() {
        // Set the cell factory for the searchFriendsListView using a custom SearchListenerCellFactory
        searchFriendsListView.setCellFactory(new SearchListenerCellFactory());

        // Handle initialization based on the root page type
        if (rootPage == PageTypeEnum.ADMIN_PAGE) {
            // Admin-specific initialization
            friendsButton.setManaged(false);
            friendsButton.setVisible(false);
            datePicker.setManaged(true);
            datePicker.setVisible(true);
            return;
        }

        // User-specific initialization
        List<ListenerDTO> suggestedFriends = getListenerDTOS();

        // Add suggested friends to the searchFriendsListView
        for (ListenerDTO friend : suggestedFriends)
            searchFriendsListView.getItems().add(friend);
    }

    private static List<ListenerDTO> getListenerDTOS() {
        UserSession userSession = UserSession.getInstance();
        Driver driver = Neo4jDBConfig.getInstance().getDriver();
        NeoSongDao neoSongDao = new NeoSongDaoImpl(driver);
        NeoListenerDao neoListenerDao = new NeoListenerDaoImpl(driver);
        NeoAggregationService neoAggregationService = new NeoAggregationService(neoSongDao, neoListenerDao);

        // Retrieve suggested friends for the current logged-in user
        return neoAggregationService.suggestFriend(userSession.getLoggedInUser());
    }

    // Event handler for the search bar, initiates a search based on user input
    @FXML
    private void search(KeyEvent actionEvent) {
        // Get the search query from the searchField
        String searchFriends = searchField.getText().trim();

        // Clear the existing results in the searchFriendsListView
        searchFriendsListView.getItems().clear();

        // Perform a search only if the search query is not empty
        if (!searchFriends.isEmpty()) {
            // Initialize MongoDB DAO for listener search
            MongoListenerDao mongoListenerDao = new MongoListenerDaoImpl();
            SearchBarService searchBarService = new SearchBarService(null, mongoListenerDao);

            // Retrieve matching listeners based on the search query
            List<ListenerDTO> listeners = searchBarService.searchListener(searchFriends, UserSession.getInstance().getLoggedInUser());

            // Add the matching listeners to the searchFriendsListView
            for (ListenerDTO listener : listeners) {
                searchFriendsListView.getItems().add(listener);
            }
        }
    }

    // Event handler for clicking on a listener in the search results, navigates to user details or admin functionality
    @FXML
    private void handleListenerClick(MouseEvent mouseEvent) {
        // Get the selected listener from the searchFriendsListView
        ListenerDTO selectedListener = searchFriendsListView.getSelectionModel().getSelectedItem();

        // Navigate to the appropriate page based on the root page type
        if (selectedListener != null) {
            Stage stage = (Stage) vBox.getScene().getWindow();
            PageManager pageManager = PageManager.getInstance(stage);

            // Handle navigation for admin functionality
            if (rootPage == PageTypeEnum.ADMIN_PAGE) {
                // Retrieve start and end dates from date pickers
                LocalDate startDate = startDatePicker.getValue();
                LocalDate endDate = endDatePicker.getValue();

                // Check if both start and end dates are selected and end date is after start date
                if (startDate != null && endDate != null && endDate.isAfter(startDate)) {
                    // Navigate to playlists statistics page with selected dates
                    pageManager.switchToPlaylistsStatisticsPage(selectedListener, startDate, endDate);
                    return;
                }

                // Display an information alert if the selected dates are not correct
                AlertMessages.informationAlert("The selected dates are not correct");
            } else {
                // Navigate to the user details page for regular users
                pageManager.switchToUserDetailsPage(selectedListener, PageTypeEnum.SEARCH_LISTENERS_PAGE);
            }
        }
    }

    // Event handler for clicking the home button, navigates to the admin page or homepage based on root page type
    @FXML
    private void handleHomeClick(ActionEvent actionEvent) {
        Stage stage = (Stage) vBox.getScene().getWindow();
        PageManager pageManager = PageManager.getInstance(stage);

        // Navigate to the appropriate page based on the root page type
        if (rootPage == PageTypeEnum.ADMIN_PAGE) {
            pageManager.switchToAdminPage();
        } else {
            pageManager.switchToHomepage(PageTypeEnum.SIGN_IN_PAGE);
        }
    }

    // Event handler for clicking the friends button, navigates to the list friends page
    @FXML
    private void handleFriendsClick(ActionEvent actionEvent) {
        Stage stage = (Stage) vBox.getScene().getWindow();
        PageManager pageManager = PageManager.getInstance(stage);

        // Navigate to the list friends page
        pageManager.switchToListFriends();
    }
}
