package it.unipi.lsmd.spotify2.controllers.playlists;

import it.unipi.lsmd.spotify2.configs.Neo4jDBConfig;
import it.unipi.lsmd.spotify2.daos.impl.mongo.listener.MongoPlaylistDaoImpl;
import it.unipi.lsmd.spotify2.daos.impl.neo4j.NeoPlaylistDaoImpl;
import it.unipi.lsmd.spotify2.daos.mongo.listener.MongoPlaylistDao;
import it.unipi.lsmd.spotify2.daos.neo4j.NeoPlaylistDao;
import it.unipi.lsmd.spotify2.dtos.ListenerDTO;
import it.unipi.lsmd.spotify2.dtos.SongDTO;
import it.unipi.lsmd.spotify2.pageManagement.PageManager;
import it.unipi.lsmd.spotify2.cellFactories.PlaylistCellFactory;
import it.unipi.lsmd.spotify2.dtos.PlaylistDTO;
import it.unipi.lsmd.spotify2.pageManagement.PageTypeEnum;
import it.unipi.lsmd.spotify2.services.playlist.MongoServicePlaylist;
import it.unipi.lsmd.spotify2.services.playlist.NeoServicePlaylist;
import it.unipi.lsmd.spotify2.utils.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

// Controller class for the Playlist Page
public class PlaylistPageController {

    // Reference to the VBox layout of the playlist page
    @FXML
    private VBox vBox;

    // Button for creating a new playlist
    @FXML
    private Button newButton;

    // ListView displaying the user's playlists
    @FXML
    private ListView<PlaylistDTO> playlistListView;

    // Information about the listener associated with the page
    private final ListenerDTO listener;

    // Information about the song associated with the page
    private final SongDTO song;

    // The root page type, used for navigation
    private final PageTypeEnum rootPage;

    // The specific page type, used for navigation
    private final PageTypeEnum page;

    // Constructor for cases where no specific listener, song, or page type is associated
    public PlaylistPageController(PageTypeEnum rootPage) {
        this.rootPage = rootPage;
        this.listener = null;
        this.song = null;
        this.page = null;
    }

    // Constructor for cases where a listener is associated with the page
    public PlaylistPageController(ListenerDTO listener, PageTypeEnum rootPage) {
        this.listener = listener;
        this.rootPage = rootPage;
        this.song = null;
        this.page = null;
    }

    // Constructor for cases where a song and specific page type are associated with the page
    public PlaylistPageController(SongDTO song, PageTypeEnum rootPage, PageTypeEnum page) {
        this.song = song;
        this.rootPage = rootPage;
        this.listener = null;
        this.page = page;
    }

    // Method called during initialization of the controller
    public void initialize() {
        // Clear the existing items in the playlist ListView
        playlistListView.getItems().clear();

        // Get the playlists and the current user session
        List<PlaylistDTO> playlists;
        UserSession userSession = UserSession.getInstance();

        // Determine the type of DAO and service to use based on the root page
        if (rootPage == PageTypeEnum.FRIENDS_LIST_PAGE) {
            // Disable and hide the new playlist button for friend playlists
            newButton.setDisable(true);
            newButton.setVisible(false);

            // Use Neo4j DAO and service to get friend playlists
            NeoPlaylistDao neoPlaylistDao = new NeoPlaylistDaoImpl(Neo4jDBConfig.getInstance().getDriver());
            NeoServicePlaylist neoServicePlaylist = new NeoServicePlaylist(neoPlaylistDao);

            // Get friend playlists for the specific listener
            assert listener != null;
            playlists = neoServicePlaylist.getFriendPlaylists(listener.getUsername());
        } else {
            // Use MongoDB DAO and service to initialize user playlists
            MongoPlaylistDao mongoPlaylistDao = new MongoPlaylistDaoImpl();
            MongoServicePlaylist mongoServicePlaylist = new MongoServicePlaylist(mongoPlaylistDao);

            // Get user playlists for the current logged-in user
            playlists = mongoServicePlaylist.initializePlaylists(userSession.getLoggedInUser());
        }

        // Set the cell factory for the playlist ListView using a custom PlaylistCellFactory
        if (rootPage == PageTypeEnum.ADD_TO_PLAYLIST_PAGE)
            playlistListView.setCellFactory(listView -> new PlaylistCellFactory(song, rootPage));
        else
            playlistListView.setCellFactory(listView -> new PlaylistCellFactory(this, rootPage));

        // Add the playlists to the ListView
        for (PlaylistDTO playlist : playlists) {
            playlistListView.getItems().add(playlist);
        }
    }

    // Event handler for the new playlist button, opens a popup for creating a new playlist
    @FXML
    private void handleNewButton() {
        Stage stage = (Stage) vBox.getScene().getWindow();
        PageManager pageManager = PageManager.getInstance(stage);
        pageManager.openPlaylistPopup(this);
    }

    // Event handler for the cancel button, navigates to the appropriate page based on the root page
    @FXML
    private void handleCancelButton() {
        Stage stage = (Stage) vBox.getScene().getWindow();
        PageManager pageManager = PageManager.getInstance(stage);

        // Navigate to the sign-in page if the root page is the homepage
        if (rootPage == PageTypeEnum.HOMEPAGE)
            pageManager.switchToHomepage(PageTypeEnum.SIGN_IN_PAGE);
            // Navigate to the friends list page if the root page is the friends list page
        else if (rootPage == PageTypeEnum.FRIENDS_LIST_PAGE)
            pageManager.switchToListFriends();
            // Navigate to the song page if the root page is the add to playlist page
        else if (rootPage == PageTypeEnum.ADD_TO_PLAYLIST_PAGE)
            pageManager.switchToSongPage(song, page);
    }

    // Event handler for when a playlist in the ListView is clicked
    @FXML
    private void handlePlaylistClick() {
        // Check if the root page is not the add to playlist page
        if (rootPage != PageTypeEnum.ADD_TO_PLAYLIST_PAGE) {
            // Get the selected playlist from the ListView
            PlaylistDTO selectedPlaylist = playlistListView.getSelectionModel().getSelectedItem();

            // Check if a playlist is selected
            if (selectedPlaylist != null) {
                // Get the stage (window) associated with the VBox
                Stage stage = (Stage) vBox.getScene().getWindow();

                // Get the singleton instance of the PageManager
                PageManager pageManager = PageManager.getInstance(stage);

                // Navigate to the songs in playlist page based on the root page type
                if (rootPage == PageTypeEnum.FRIENDS_LIST_PAGE)
                    pageManager.switchToSongsInPlaylistPage(selectedPlaylist, listener, rootPage);
                else
                    pageManager.switchToSongsInPlaylistPage(selectedPlaylist, rootPage);
            }
        }
    }
}
