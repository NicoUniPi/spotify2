package it.unipi.lsmd.spotify2.controllers.social;

import it.unipi.lsmd.spotify2.cellFactories.ListFriendsCellFactory;
import it.unipi.lsmd.spotify2.configs.Neo4jDBConfig;
import it.unipi.lsmd.spotify2.daos.impl.neo4j.NeoListenerDaoImpl;
import it.unipi.lsmd.spotify2.daos.neo4j.NeoListenerDao;
import it.unipi.lsmd.spotify2.dtos.ListenerDTO;
import it.unipi.lsmd.spotify2.pageManagement.PageManager;
import it.unipi.lsmd.spotify2.pageManagement.PageTypeEnum;
import it.unipi.lsmd.spotify2.services.social.FollowingSocialService;
import it.unipi.lsmd.spotify2.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

// Controller class for the List Friends Page
public class ListFriendsPageController {

    // ListView displaying the user's friends
    @FXML
    public ListView<ListenerDTO> listFriends;

    // VBox layout of the list friends page
    @FXML
    public VBox vBox;

    // Method called during initialization of the controller
    public void initialize() {
        // Clear existing items in the friends ListView
        listFriends.getItems().clear();

        // Get the user's friends and the current user session
        List<ListenerDTO> friends;
        UserSession userSession = UserSession.getInstance();

        // Initialize Neo4j DAO and FollowingSocialService for managing friends
        NeoListenerDao neoListenerDao = new NeoListenerDaoImpl(Neo4jDBConfig.getInstance().getDriver());
        FollowingSocialService followingSocialService = new FollowingSocialService(neoListenerDao);

        // Retrieve the list of friends for the current logged-in user
        friends = followingSocialService.getListFriends(userSession.getLoggedInUser());

        // Set the cell factory for the friends ListView using a custom ListFriendsCellFactory
        listFriends.setCellFactory(listView -> new ListFriendsCellFactory(this, vBox));

        // Add friends to the ListView
        for (ListenerDTO friend : friends) {
            listFriends.getItems().add(friend);
        }
    }

    // Event handler for the back button, navigates to the social page based on the root page
    @FXML
    private void handleBackButton(ActionEvent actionEvent) {
        Stage stage = (Stage) vBox.getScene().getWindow();
        PageManager pageManager = PageManager.getInstance(stage);

        // Navigate to the social page with the appropriate root page
        pageManager.switchToSocial(PageTypeEnum.HOMEPAGE, PageTypeEnum.SEARCH_LISTENERS_PAGE);
    }
}
