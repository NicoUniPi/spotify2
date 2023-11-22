package it.unipi.lsmd.spotify2.cellFactories;

import it.unipi.lsmd.spotify2.configs.Neo4jDBConfig;
import it.unipi.lsmd.spotify2.controllers.songs.SongsInPlaylistPageController;
import it.unipi.lsmd.spotify2.daos.impl.mongo.listener.MongoPlaylistDaoImpl;
import it.unipi.lsmd.spotify2.daos.impl.neo4j.NeoPlaylistDaoImpl;
import it.unipi.lsmd.spotify2.daos.mongo.listener.MongoPlaylistDao;
import it.unipi.lsmd.spotify2.daos.neo4j.NeoPlaylistDao;
import it.unipi.lsmd.spotify2.dtos.SongDTO;
import it.unipi.lsmd.spotify2.pageManagement.PageTypeEnum;
import it.unipi.lsmd.spotify2.services.playlist.MongoServicePlaylist;
import it.unipi.lsmd.spotify2.services.playlist.SharedPlaylistService;
import it.unipi.lsmd.spotify2.services.playlist.NeoServicePlaylist;
import it.unipi.lsmd.spotify2.utils.UserSession;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import org.bson.types.ObjectId;

import java.util.Objects;

// Custom ListCell for displaying information about songs in a playlist
public class SongsInPlaylistCellFactory extends ListCell<SongDTO> {

    // Button for removing a song from the playlist
    private final Button removeButton = new Button("Remove");

    // Singleton instance of UserSession for managing user sessions
    private final UserSession userSession = UserSession.getInstance();

    // Label for displaying the song title
    private final Label playlistLabel = new Label();

    // Type of the root page
    private final PageTypeEnum rootPage;

    // Constructor for SongsInPlaylistCellFactory
    public SongsInPlaylistCellFactory(SongsInPlaylistPageController songsInPlaylistPageController, ObjectId selectedPlaylist, PageTypeEnum rootPage) {

        // Set the action for the removeButton
        removeButton.setOnAction(event -> {
            SongDTO song = getItem();
            if (song != null) {
                // Initialize DAOs and Service for interacting with playlist data
                NeoPlaylistDao neoPlaylistDao = new NeoPlaylistDaoImpl(Neo4jDBConfig.getInstance().getDriver());
                NeoServicePlaylist neoServicePlaylist = new NeoServicePlaylist(neoPlaylistDao);

                // Check if the playlist is in Neo4j or MongoDB and remove the song accordingly
                if (neoServicePlaylist.checkPlaylistPresence(selectedPlaylist.toString())) {
                    MongoPlaylistDao mongoPlaylistDao = new MongoPlaylistDaoImpl();
                    SharedPlaylistService sharedPlaylistService = new SharedPlaylistService(mongoPlaylistDao, neoPlaylistDao);
                    String usernameWhoAdded = neoPlaylistDao.getUsernameWhoAdded(song.getId().toString(), selectedPlaylist.toString());
                    if(Objects.equals(userSession.getLoggedInUser(), usernameWhoAdded))
                        sharedPlaylistService.removeSongFromPlaylist(song.getId(), selectedPlaylist, userSession.getLoggedInUser(), null);
                    else
                        sharedPlaylistService.removeSongFromPlaylist(song.getId(), selectedPlaylist, userSession.getLoggedInUser(), usernameWhoAdded);
                } else {
                    MongoPlaylistDao mongoPlaylistDao = new MongoPlaylistDaoImpl();
                    MongoServicePlaylist mongoServicePlaylist = new MongoServicePlaylist(mongoPlaylistDao);
                    mongoServicePlaylist.removeSongFromPlaylist(userSession.getLoggedInUser(), selectedPlaylist, song.getId());
                }

                // Update the page
                songsInPlaylistPageController.initialize();
            }
        });

        // Initialize properties
        this.rootPage = rootPage;
    }

    // This method is called whenever the item in the list changes
    @Override
    protected void updateItem(SongDTO song, boolean empty) {
        super.updateItem(song, empty);

        // If the cell is empty or the song is null, set the graphic to null
        if (empty || song == null) {
            setGraphic(null);
        } else {
            // Depending on the root page, either set the text or create an HBox with a label and remove button
            if (rootPage == PageTypeEnum.FRIENDS_LIST_PAGE)
                setText(song.getTitle());
            else {
                playlistLabel.setText(song.getTitle());
                HBox playlistContainer = new HBox(20);
                playlistContainer.setAlignment(Pos.CENTER);
                playlistContainer.getChildren().addAll(playlistLabel, removeButton);
                setGraphic(playlistContainer);
            }
        }
    }
}
