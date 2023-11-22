package it.unipi.lsmd.spotify2.cellFactories;

import it.unipi.lsmd.spotify2.configs.Neo4jDBConfig;
import it.unipi.lsmd.spotify2.controllers.social.ListFriendsPageController;
import it.unipi.lsmd.spotify2.daos.impl.neo4j.NeoListenerDaoImpl;
import it.unipi.lsmd.spotify2.daos.neo4j.NeoListenerDao;
import it.unipi.lsmd.spotify2.dtos.ListenerDTO;
import it.unipi.lsmd.spotify2.pageManagement.PageManager;
import it.unipi.lsmd.spotify2.pageManagement.PageTypeEnum;
import it.unipi.lsmd.spotify2.services.social.FollowingSocialService;
import it.unipi.lsmd.spotify2.utils.UserSession;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// Custom ListCell for displaying information about friends in a ListView
public class ListFriendsCellFactory extends ListCell<ListenerDTO> {

    // Buttons for viewing playlists and unfollowing a friend
    private final Button playlistsFriendButton = new Button("playlists");
    private final Button unfollowButton = new Button("unfollow");

    // Singleton instance of UserSession for managing user sessions
    private final UserSession userSession = UserSession.getInstance();

    // Label for displaying the friend's username
    private final Label listenerLabel = new Label();

    // Constructor that takes a ListFriendsPageController and a VBox for interacting with the friends list page
    public ListFriendsCellFactory(ListFriendsPageController listFriendsPageController, VBox vBox) {

        // Set the action for the unfollowButton
        unfollowButton.setOnAction(event -> {
            ListenerDTO listener = getItem();
            if (listener != null) {
                // Initialize a DAO and Service for interacting with the social network data
                NeoListenerDao neoListenerDao = new NeoListenerDaoImpl(Neo4jDBConfig.getInstance().getDriver());
                FollowingSocialService followingSocialService = new FollowingSocialService(neoListenerDao);

                // Unfollow the selected friend
                followingSocialService.unfollowUser(userSession.getLoggedInUser(), listener.getUsername());

                // Reinitialize the friends list page to reflect the changes
                listFriendsPageController.initialize();
            }
        });

        // Set the action for the playlistsFriendButton
        playlistsFriendButton.setOnAction(actionEvent -> {
            ListenerDTO listener = getItem();
            if(listener != null) {
                // Get the stage and switch to the playlist page for the selected friend
                Stage stage = (Stage) vBox.getScene().getWindow();
                PageManager pageManager = PageManager.getInstance(stage);
                pageManager.switchToPlaylistPage(listener, PageTypeEnum.FRIENDS_LIST_PAGE);
            }
        });
    }

    // This method is called whenever the item in the list changes
    @Override
    protected void updateItem(ListenerDTO friend, boolean empty) {
        super.updateItem(friend, empty);

        // If the cell is empty or the friend is null, set the graphic to null
        if (empty || friend == null) {
            setGraphic(null);
        } else {
            // Set the text of the label to the friend's username
            listenerLabel.setText(friend.getUsername());

            // Create an HBox to hold the label and buttons, and set its alignment
            HBox playlistContainer = new HBox(20);
            playlistContainer.setAlignment(Pos.CENTER);

            // Set a minimum width for the unfollowButton
            unfollowButton.setStyle("-fx-min-width: 110px;");

            // Add the label and buttons to the HBox
            playlistContainer.getChildren().addAll(listenerLabel, playlistsFriendButton, unfollowButton);

            // Set the graphic of the cell to the HBox
            setGraphic(playlistContainer);
        }
    }
}
