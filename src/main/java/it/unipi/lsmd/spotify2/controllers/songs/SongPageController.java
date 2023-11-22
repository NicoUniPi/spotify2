package it.unipi.lsmd.spotify2.controllers.songs;

import it.unipi.lsmd.spotify2.configs.Neo4jDBConfig;
import it.unipi.lsmd.spotify2.daos.impl.mongo.MongoSongDaoImpl;
import it.unipi.lsmd.spotify2.daos.impl.mongo.listener.LibraryDaoImpl;
import it.unipi.lsmd.spotify2.daos.impl.mongo.listener.MongoPlaylistDaoImpl;
import it.unipi.lsmd.spotify2.daos.impl.neo4j.NeoPlaylistDaoImpl;
import it.unipi.lsmd.spotify2.daos.impl.neo4j.NeoSongDaoImpl;
import it.unipi.lsmd.spotify2.daos.mongo.MongoSongDao;
import it.unipi.lsmd.spotify2.daos.mongo.listener.LibraryDao;
import it.unipi.lsmd.spotify2.daos.mongo.listener.MongoPlaylistDao;
import it.unipi.lsmd.spotify2.daos.neo4j.NeoPlaylistDao;
import it.unipi.lsmd.spotify2.daos.neo4j.NeoSongDao;
import it.unipi.lsmd.spotify2.dtos.ListenerDTO;
import it.unipi.lsmd.spotify2.dtos.PlaylistDTO;
import it.unipi.lsmd.spotify2.dtos.SongDTO;
import it.unipi.lsmd.spotify2.models.Artist;
import it.unipi.lsmd.spotify2.models.Song;
import it.unipi.lsmd.spotify2.pageManagement.PageManager;
import it.unipi.lsmd.spotify2.pageManagement.PageTypeEnum;
import it.unipi.lsmd.spotify2.services.LibraryService;
import it.unipi.lsmd.spotify2.services.playlist.NeoServicePlaylist;
import it.unipi.lsmd.spotify2.services.playlist.SharedPlaylistService;
import it.unipi.lsmd.spotify2.services.song.ManageSongService;
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

import java.util.ArrayList;
import java.util.List;

import static it.unipi.lsmd.spotify2.utils.UpdateDocument.setLabelClickHandler;

// Controller class for the Song Page
public class SongPageController {

    // DTO representing the current song
    private final SongDTO song;

    // Instance of UserSession for managing user sessions
    private UserSession userSession;

    // Enum representing the root page type for navigation purposes
    private final PageTypeEnum rootPage;

    // DTO representing the current playlist (null if not in the context of a playlist)
    private final PlaylistDTO playlist;

    // DTO representing the friend (null if not in the context of adding a song to a friend's playlist)
    private final ListenerDTO friend;

    // VBox layout of the song page
    @FXML
    private VBox mainVBox;

    // Label displaying the title of the song
    @FXML
    private Label titleLabel;

    // ToggleButton for liking/unliking the song
    @FXML
    private ToggleButton likeToggleButton;

    // Label displaying the artist(s) of the song
    @FXML
    private Label artistLabel;

    // TextField for editing the artist(s) of the song
    @FXML
    private TextField artistTextField;

    // Label displaying the release year of the song
    @FXML
    private Label yearLabel;

    // TextField for editing the release year of the song
    @FXML
    private TextField yearTextField;

    // Label displaying the duration of the song
    @FXML
    private Label durationLabel;

    // TextField for editing the duration of the song
    @FXML
    private TextField durationTextField;

    // Label displaying the genre of the song
    @FXML
    private Label genreLabel;

    // ComboBox for selecting the genre of the song
    @FXML
    private ComboBox<String> genreComboBox;

    // ImageView displaying the image of the song
    @FXML
    private ImageView songImage;

    // ToggleButton for adding/removing the song from a playlist
    @FXML
    private ToggleButton addToPlaylistButton;

    // TextField for editing the title of the song
    @FXML
    private TextField titleTextField;

    // Button for saving changes to the song
    @FXML
    private Button saveButton;

    // Button for deleting the song
    @FXML
    private Button deleteButton;

    // Constructor for a song page without a playlist or friend context
    public SongPageController(SongDTO song, PageTypeEnum rootPage) {
        this.song = song;
        this.playlist = null;
        this.rootPage = rootPage;
        this.friend = null;
    }

    // Constructor for a song page with a playlist context
    public SongPageController(SongDTO song, PlaylistDTO playlist, PageTypeEnum rootPage) {
        this.song = song;
        this.playlist = playlist;
        this.rootPage = rootPage;
        this.friend = null;
    }

    // Constructor for a song page with a friend context
    public SongPageController(SongDTO song, PlaylistDTO playlist, ListenerDTO friend, PageTypeEnum rootPage) {
        this.song = song;
        this.playlist = playlist;
        this.rootPage = rootPage;
        this.friend = friend;
    }

    // Method called during initialization of the controller
    @FXML
    private void initialize() {
        // Initialize UserSession instance
        userSession = UserSession.getInstance();

        // Initialize MongoDB DAO for songs and library
        MongoSongDao mongoSongDao = new MongoSongDaoImpl();
        LibraryDao libraryDao = new LibraryDaoImpl();

        // Retrieve full song details from the database
        Song song = mongoSongDao.getSongById(this.song.getId());

        // Set labels and controls based on the song details
        titleLabel.setText(song.getTitle());
        genreLabel.setText(song.getGenre());
        yearLabel.setText(Integer.toString(song.getReleasedYear()));
        durationLabel.setText(Integer.toString(song.getDuration()));

        // Set artist label based on the list of artists
        List<String> artists = new ArrayList<>();
        for (Artist a : song.getArtists()) {
            artists.add(a.getName());
        }
        artistLabel.setText(String.join(",", artists));

        // Set song image
        Image image = new Image(song.getSongImage());
        songImage.setImage(image);

        // Customize UI based on the root page type
        if (rootPage == PageTypeEnum.ADMIN_PAGE) {
            // Admin page customization
            likeToggleButton.setDisable(true);
            likeToggleButton.setVisible(false);
            likeToggleButton.setManaged(false);
            addToPlaylistButton.setDisable(true);
            addToPlaylistButton.setVisible(false);
            addToPlaylistButton.setManaged(false);

            // Enable editing for various song details
            setLabelClickHandler(titleLabel, titleTextField);
            setLabelClickHandler(artistLabel, artistTextField);

            // Initialize and set items for the genre ComboBox
            List<String> genresList = ComboBoxInitializeUtil.getMusicGenres();
            genreComboBox.getItems().addAll(genresList);
            setLabelClickHandler(genreLabel, genreComboBox);

            // Enable and show save and delete buttons
            saveButton.setManaged(true);
            saveButton.setDisable(false);
            deleteButton.setManaged(true);
            deleteButton.setDisable(false);
            return;
        }

        // Customize UI based on the context of adding a song to a friend's playlist
        if (rootPage == PageTypeEnum.ADD_SONG_TO_FRIEND) {
            likeToggleButton.setManaged(false);
            likeToggleButton.setDisable(true);
            likeToggleButton.setVisible(false);

            // Initialize Neo4j DAO for playlists
            int value = getValue(song);

            if (value == 0) {
                addToPlaylistButton.setText("Remove the song");
                addToPlaylistButton.setSelected(true);
            } else if (value == 1) {
                addToPlaylistButton.setText("Song already in the playlist");
                addToPlaylistButton.setDisable(true);
            } else if (value == -1) {
                addToPlaylistButton.setText("Add to the Playlist!");
            }
            return;
        }

        // Customize UI based on the context of the user's library
        if (libraryDao.checkSongFromLibrary(userSession.getLoggedInUser(), this.song.getId())) {
            likeToggleButton.setText("Liked");
            likeToggleButton.setSelected(true);
        }
    }

    private int getValue(Song song) {
        NeoPlaylistDao neoPlaylistDao = new NeoPlaylistDaoImpl(Neo4jDBConfig.getInstance().getDriver());
        NeoServicePlaylist neoServicePlaylist = new NeoServicePlaylist(neoPlaylistDao);

        // Check if the song is in the playlist and update the button text accordingly
        assert playlist != null;
        return neoServicePlaylist.checkSongInPlaylist(
                song.getId().toString(),
                playlist.getIdPlaylist().toString(),
                UserSession.getInstance().getLoggedInUser()
        );
    }

    // Method called when the likeToggleButton is toggled
    @FXML
    private void toggleLikeButton() {
        // Initialize Library DAO and service
        LibraryDao libraryDao = new LibraryDaoImpl();
        LibraryService libraryService = new LibraryService(libraryDao);

        // Update library based on the toggle state
        if (likeToggleButton.isSelected()) {
            libraryService.addToLibrary(userSession.getLoggedInUser(), song);
            likeToggleButton.setText("Liked");
        } else {
            libraryService.removeFromLibrary(userSession.getLoggedInUser(), song.getId());
            likeToggleButton.setText("Like");
        }
    }

    // Method called when the addToPlaylistButton is clicked
    @FXML
    private void handlePlaylistButton(ActionEvent actionEvent) {
        // Context: Adding a song to a friend's playlist
        if (rootPage == PageTypeEnum.ADD_SONG_TO_FRIEND) {
            // Initialize Neo4j DAO for playlists
            NeoPlaylistDao neoPlaylistDao = new NeoPlaylistDaoImpl(Neo4jDBConfig.getInstance().getDriver());

            // Initialize user session and MongoDB DAO for playlists
            UserSession userSession = UserSession.getInstance();
            MongoPlaylistDao mongoPlaylistDao = new MongoPlaylistDaoImpl();

            // Initialize SharedPlaylistService
            SharedPlaylistService sharedPlaylistService = new SharedPlaylistService(mongoPlaylistDao, neoPlaylistDao);

            // Check the state of the addToPlaylistButton and perform the corresponding action
            assert friend != null;
            assert playlist != null;
            if (addToPlaylistButton.isSelected()) {
                sharedPlaylistService.addSongToFriendPlaylist(
                        song,
                        playlist.getIdPlaylist(),
                        friend.getUsername(),
                        userSession.getLoggedInUser()
                );
                addToPlaylistButton.setText("Added");
            } else {
                // Remove the song from the playlist
                sharedPlaylistService.removeSongFromPlaylist(song.getId(), playlist.getIdPlaylist(), friend.getUsername(), userSession.getLoggedInUser());
                addToPlaylistButton.setText("Add to the Playlist!");
            }
        } else {
            // Context: Adding a song to a user's playlist
            Stage stage = (Stage) mainVBox.getScene().getWindow();
            PageManager pageManager = PageManager.getInstance(stage);
            pageManager.switchToPlaylistPage(song, PageTypeEnum.ADD_TO_PLAYLIST_PAGE, rootPage);
        }
    }

    // Method called when the back button is clicked
    @FXML
    private void handleBackButton(ActionEvent actionEvent) {
        Stage stage = (Stage) mainVBox.getScene().getWindow();
        PageManager pageManager = PageManager.getInstance(stage);

        // Navigate to the appropriate page based on the rootPage type
        if (rootPage == PageTypeEnum.HOMEPAGE)
            pageManager.switchToHomepage(PageTypeEnum.SIGN_IN_PAGE);
        else if (rootPage == PageTypeEnum.LIBRARY_PAGE)
            pageManager.switchToLibrary();
        else if (rootPage == PageTypeEnum.SONGS_IN_PLAYLIST_PAGE)
            pageManager.switchToSongsInPlaylistPage(playlist, PageTypeEnum.HOMEPAGE);
        else if (rootPage == PageTypeEnum.ADD_SONG_TO_FRIEND)
            pageManager.switchToHomepage(playlist, friend, rootPage);
        else if (rootPage == PageTypeEnum.ADMIN_PAGE)
            pageManager.switchToHomepage(rootPage);
    }

    // Method called when the saveButton is clicked
    @FXML
    private void handleSaveButton() {
        // Confirmation alert for updating song information
        if (AlertMessages.confirmationAlert("You will overwrite the song information. Do you want to confirm?")) {
            // Create a new Song object with updated information
            Song newSong = new Song();
            newSong.setId(song.getId());
            newSong.setReleasedYear(Integer.parseInt(yearLabel.getText()));
            newSong.setDuration(Integer.parseInt(durationLabel.getText()));
            newSong.setGenre(genreLabel.getText());
            newSong.setTitle(titleLabel.getText());

            // Initialize and call the ManageSongService to update the song
            MongoSongDao mongoSongDao = new MongoSongDaoImpl();
            NeoSongDao neoSongDao = new NeoSongDaoImpl(Neo4jDBConfig.getInstance().getDriver());
            ManageSongService manageSongService = new ManageSongService(neoSongDao, mongoSongDao);
            manageSongService.updateSong(newSong);

            // Display information alert about the successful update
            AlertMessages.informationAlert("The song has been updated");
        }
    }

    // Method called when the deleteButton is clicked
    @FXML
    private void handleDeleteButton() {
        // Confirmation alert for deleting the song
        if (AlertMessages.confirmationAlert("You will delete the song. Do you want to confirm?")) {
            MongoSongDao mongoSongDao = new MongoSongDaoImpl();
            NeoSongDao neoSongDao = new NeoSongDaoImpl(Neo4jDBConfig.getInstance().getDriver());
            ManageSongService manageSongService = new ManageSongService(neoSongDao, mongoSongDao);
            manageSongService.deleteSong(song.getId());
            AlertMessages.informationAlert("The song has been deleted");
            Stage stage = (Stage) mainVBox.getScene().getWindow();
            PageManager pageManager = PageManager.getInstance(stage);
            pageManager.switchToHomepage(rootPage);
        }
    }
}
