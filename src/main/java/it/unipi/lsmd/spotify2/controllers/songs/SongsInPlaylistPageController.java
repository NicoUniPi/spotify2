// Package declaration indicating the location of this class
package it.unipi.lsmd.spotify2.controllers.songs;

// Import statements for classes used in this file
import it.unipi.lsmd.spotify2.daos.impl.mongo.listener.MongoPlaylistDaoImpl;
import it.unipi.lsmd.spotify2.daos.mongo.listener.MongoPlaylistDao;
import it.unipi.lsmd.spotify2.dtos.ListenerDTO;
import it.unipi.lsmd.spotify2.pageManagement.PageManager;
import it.unipi.lsmd.spotify2.cellFactories.SongsInPlaylistCellFactory;
import it.unipi.lsmd.spotify2.dtos.PlaylistDTO;
import it.unipi.lsmd.spotify2.dtos.SongDTO;
import it.unipi.lsmd.spotify2.pageManagement.PageTypeEnum;
import it.unipi.lsmd.spotify2.services.playlist.MongoServicePlaylist;
import it.unipi.lsmd.spotify2.utils.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

// Class declaration for SongsInPlaylistPageController
public class SongsInPlaylistPageController {

    // Fields to store the selected playlist, friend, and root page type
    private final PlaylistDTO selectedPlaylist;
    private final ListenerDTO friend;
    private final PageTypeEnum rootPage;

    // FXML annotations to inject elements from the FXML file
    @FXML
    private VBox vBox;
    @FXML
    private ListView<SongDTO> songsInPlaylistListView;
    @FXML
    private Button addSongButton;

    // Constructor for the controller when a friend is not specified
    public  SongsInPlaylistPageController(PlaylistDTO selectedPlaylist, PageTypeEnum rootPage) {
        this.selectedPlaylist = selectedPlaylist;
        this.friend = null;
        this.rootPage = rootPage;
    }

    // Constructor for the controller when a friend is specified
    public  SongsInPlaylistPageController(PlaylistDTO selectedPlaylist, ListenerDTO friend, PageTypeEnum rootPage) {
        this.selectedPlaylist = selectedPlaylist;
        this.friend = friend;
        this.rootPage = rootPage;
    }

    // Method called after the FXML file is loaded, initializes the controller
    @FXML
    public void initialize() {
        // Clear the list view
        songsInPlaylistListView.getItems().clear();

        // Get the user session and set up MongoDB-related objects
        UserSession userSession = UserSession.getInstance();
        MongoPlaylistDao mongoPlaylistDao = new MongoPlaylistDaoImpl();
        MongoServicePlaylist mongoServicePlaylist = new MongoServicePlaylist(mongoPlaylistDao);

        // Determine whether the page is for a friend's playlist or the user's playlist
        List<SongDTO> songs;
        if(rootPage == PageTypeEnum.FRIENDS_LIST_PAGE) {
            assert friend != null;
            songs = mongoServicePlaylist.getSongsFromPlaylist(friend.getUsername(), selectedPlaylist.getIdPlaylist());
        } else {
            songs = mongoServicePlaylist.getSongsFromPlaylist(userSession.getLoggedInUser(), selectedPlaylist.getIdPlaylist());
            // Disable and hide the add song button if not on the friend's playlist page
            addSongButton.setDisable(true);
            addSongButton.setVisible(false);
        }

        // Set the cell factory for the list view
        songsInPlaylistListView.setCellFactory(listView ->
                new SongsInPlaylistCellFactory(this, selectedPlaylist.getIdPlaylist(), rootPage));

        // Populate the list view with songs
        for (SongDTO song : songs) {
            songsInPlaylistListView.getItems().add(song);
        }
    }

    // Method to handle the add song button click event
    @FXML
    private void handleAddSongButton() {
        // Get the stage and page manager, then switch to the add song page
        Stage stage = (Stage) vBox.getScene().getWindow();
        PageManager pageManager = PageManager.getInstance(stage);
        pageManager.switchToHomepage(selectedPlaylist, friend, PageTypeEnum.ADD_SONG_TO_FRIEND);
    }

    // Method to handle the song click event
    @FXML
    private void handleSongClick() {
        // If on the homepage, get the selected song and switch to the song page
        if(rootPage == PageTypeEnum.HOMEPAGE) {
            SongDTO selectedSong = songsInPlaylistListView.getSelectionModel().getSelectedItem();
            Stage stage = (Stage) vBox.getScene().getWindow();
            PageManager pageManager = PageManager.getInstance(stage);
            pageManager.switchToSongPage(selectedSong, selectedPlaylist, PageTypeEnum.SONGS_IN_PLAYLIST_PAGE);
        }
    }

    // Method to handle the back button click event
    @FXML
    private void handleBackButton(){
        // Get the stage and page manager, then switch to the appropriate playlist page
        Stage stage = (Stage) vBox.getScene().getWindow();
        PageManager pageManager = PageManager.getInstance(stage);
        if (rootPage == PageTypeEnum.FRIENDS_LIST_PAGE)
            pageManager.switchToPlaylistPage(friend, rootPage);
        else
            pageManager.switchToPlaylistPage(rootPage);
    }
}
