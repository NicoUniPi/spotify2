package it.unipi.lsmd.spotify2.cellFactories;

import it.unipi.lsmd.spotify2.configs.Neo4jDBConfig;
import it.unipi.lsmd.spotify2.controllers.playlists.PlaylistPageController;
import it.unipi.lsmd.spotify2.daos.impl.mongo.listener.MongoPlaylistDaoImpl;
import it.unipi.lsmd.spotify2.daos.impl.neo4j.NeoPlaylistDaoImpl;
import it.unipi.lsmd.spotify2.daos.mongo.listener.MongoPlaylistDao;
import it.unipi.lsmd.spotify2.daos.neo4j.NeoPlaylistDao;
import it.unipi.lsmd.spotify2.dtos.PlaylistDTO;
import it.unipi.lsmd.spotify2.dtos.SongDTO;
import it.unipi.lsmd.spotify2.pageManagement.PageTypeEnum;
import it.unipi.lsmd.spotify2.services.playlist.MongoServicePlaylist;
import it.unipi.lsmd.spotify2.services.playlist.SharedPlaylistService;
import it.unipi.lsmd.spotify2.services.playlist.NeoServicePlaylist;
import it.unipi.lsmd.spotify2.utils.AlertMessages;
import it.unipi.lsmd.spotify2.utils.UserSession;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;

// Custom ListCell for displaying information about playlists in a ListView
public class PlaylistCellFactory extends ListCell<PlaylistDTO> {

    // Buttons for deleting, sharing, and adding to playlists
    private final Button deleteButton = new Button("Delete");
    private final ToggleButton shareButton = new ToggleButton("share");
    private final Button addButton = new Button("Add");

    // Singleton instance of UserSession for managing user sessions
    private final UserSession userSession = UserSession.getInstance();

    // Song in the playlist (used for the "Add" button functionality)
    private final SongDTO songInPlaylist;

    // Label for displaying the playlist name
    private final Label playlistLabel = new Label();

    // Type of the root page
    private final PageTypeEnum rootPage;

    // Constructor for playlists without a specific song
    public PlaylistCellFactory(PlaylistPageController playlistPageController, PageTypeEnum rootPage) {

        // Set the action for the deleteButton
        deleteButton.setOnAction(event -> {
            if (AlertMessages.confirmationAlert("Are you sure? All your and your friends' playlist songs will be lost")) {
                PlaylistDTO playlist = getItem();
                if (playlist != null) {
                    // Initialize DAOs and Service for interacting with playlist data
                    MongoPlaylistDao mongoPlaylistDao = new MongoPlaylistDaoImpl();
                    NeoPlaylistDao neoPlaylistDao = new NeoPlaylistDaoImpl(Neo4jDBConfig.getInstance().getDriver());
                    SharedPlaylistService sharedPlaylistService = new SharedPlaylistService(
                            mongoPlaylistDao, neoPlaylistDao
                    );

                    // Delete the playlist and update the page
                    sharedPlaylistService.deletePlaylist(playlist.getIdPlaylist(), userSession.getLoggedInUser());
                    playlistPageController.initialize();
                }
            }
        });

        // Set the action for the shareButton
        shareButton.setOnAction(event -> {
            PlaylistDTO selectedPlaylist = getItem();
            if (selectedPlaylist != null) {
                // Initialize DAOs and Service for interacting with playlist data
                NeoPlaylistDao neoPlaylistDao = new NeoPlaylistDaoImpl(Neo4jDBConfig.getInstance().getDriver());
                MongoPlaylistDao mongoPlaylistDao = new MongoPlaylistDaoImpl();
                SharedPlaylistService sharedPlaylistService = new SharedPlaylistService(mongoPlaylistDao, neoPlaylistDao);

                if (shareButton.isSelected()) {
                    // Share the playlist and update the button text
                    sharedPlaylistService.createPlaylist(selectedPlaylist, userSession.getLoggedInUser());
                    shareButton.setText("shared");
                } else {
                    // Unshare the playlist and update the button text
                    NeoServicePlaylist neoServicePlaylist = new NeoServicePlaylist(neoPlaylistDao);
                    neoServicePlaylist.deletePlaylist(selectedPlaylist.getIdPlaylist().toString(), null);
                    shareButton.setText("share");
                }
            }
        });

        // Initialize properties
        this.rootPage = rootPage;
        this.songInPlaylist = null;
    }

    // Constructor for playlists with a specific song
    public PlaylistCellFactory(SongDTO song, PageTypeEnum rootPage) {
        this.songInPlaylist = song;

        // Set the action for the addButton
        addButton.setOnAction(event -> {
            PlaylistDTO playlist = getItem();
            if (playlist != null) {
                // Initialize DAOs and Service for interacting with playlist data
                NeoPlaylistDao neoPlaylistDao = new NeoPlaylistDaoImpl(Neo4jDBConfig.getInstance().getDriver());
                NeoServicePlaylist neoServicePlaylist = new NeoServicePlaylist(neoPlaylistDao);

                // Check if the playlist is in Neo4j or MongoDB and add the song accordingly
                if (neoServicePlaylist.checkPlaylistPresence(playlist.getIdPlaylist().toString())) {
                    MongoPlaylistDao mongoPlaylistDao = new MongoPlaylistDaoImpl();
                    SharedPlaylistService sharedPlaylistService = new SharedPlaylistService(mongoPlaylistDao, neoPlaylistDao);
                    sharedPlaylistService.addSongToPlaylist(song, playlist.getIdPlaylist(), userSession.getLoggedInUser());
                } else {
                    MongoPlaylistDao mongoPlaylistDao = new MongoPlaylistDaoImpl();
                    MongoServicePlaylist mongoServicePlaylist = new MongoServicePlaylist(mongoPlaylistDao);
                    mongoServicePlaylist.addSongToPlaylist(userSession.getLoggedInUser(), song, playlist.getIdPlaylist());
                }

                // Disable the button and set its text to "Added"
                addButton.setDisable(true);
                addButton.setText("Added");

                // Display an information alert
                AlertMessages.informationAlert("The song has been added to the playlist " + playlist.getName() + "!");
            }
        });

        // Initialize properties
        this.rootPage = rootPage;
    }

    // This method is called whenever the item in the list changes
    @Override
    protected void updateItem(PlaylistDTO playlist, boolean empty) {
        super.updateItem(playlist, empty);

        // If the cell is empty or the playlist is null, set the graphic to null
        if (empty || playlist == null) {
            setGraphic(null);
        } else if (rootPage == PageTypeEnum.HOMEPAGE) {
            // Display the playlist with options for sharing and deleting
            NeoPlaylistDao neoPlaylistDao = new NeoPlaylistDaoImpl(Neo4jDBConfig.getInstance().getDriver());
            NeoServicePlaylist neoServicePlaylist = new NeoServicePlaylist(neoPlaylistDao);

            // Check if the playlist is shared and update the button text accordingly
            if (neoServicePlaylist.checkPlaylistPresence(playlist.getIdPlaylist().toString())) {
                shareButton.setText("shared");
                shareButton.setSelected(true);
            }

            // Set the playlist name and create an HBox to hold the label and buttons
            playlistLabel.setText(playlist.getName());
            HBox playlistContainer = new HBox(20);
            playlistContainer.setAlignment(Pos.CENTER);
            playlistContainer.getChildren().addAll(playlistLabel, shareButton, deleteButton);
            setGraphic(playlistContainer);
        } else if (rootPage == PageTypeEnum.ADD_TO_PLAYLIST_PAGE) {
            // Display the playlist with an "Add" button
            MongoPlaylistDao mongoPlaylistDao = new MongoPlaylistDaoImpl();
            MongoServicePlaylist mongoServicePlaylist = new MongoServicePlaylist(mongoPlaylistDao);

            // Check if the song is already in the playlist and update the button text accordingly
            if (mongoServicePlaylist.checkSongFromPlaylist(userSession.getLoggedInUser(), playlist.getIdPlaylist(), songInPlaylist.getId())) {
                addButton.setText("Added");
                addButton.setDisable(true);
            }

            // Set the playlist name and create an HBox to hold the label and the "Add" button
            playlistLabel.setText(playlist.getName());
            HBox playlistContainer = new HBox(20);
            playlistContainer.setAlignment(Pos.CENTER);
            playlistContainer.getChildren().addAll(playlistLabel, addButton);
            setGraphic(playlistContainer);
        } else {
            // For other pages, simply set the text of the cell to the playlist name
            setText(playlist.getName());
        }
    }
}
