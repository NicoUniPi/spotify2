package it.unipi.lsmd.spotify2.controllers.admin;

import it.unipi.lsmd.spotify2.configs.Neo4jDBConfig;
import it.unipi.lsmd.spotify2.daos.impl.mongo.MongoSongDaoImpl;
import it.unipi.lsmd.spotify2.daos.impl.neo4j.NeoSongDaoImpl;
import it.unipi.lsmd.spotify2.daos.mongo.MongoSongDao;
import it.unipi.lsmd.spotify2.daos.neo4j.NeoSongDao;
import it.unipi.lsmd.spotify2.models.Artist;
import it.unipi.lsmd.spotify2.models.Song;
import it.unipi.lsmd.spotify2.pageManagement.PageManager;
import it.unipi.lsmd.spotify2.services.song.ManageSongService;
import it.unipi.lsmd.spotify2.utils.AlertMessages;
import it.unipi.lsmd.spotify2.utils.ComboBoxInitializeUtil;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Controller for handling the insertion of a new song in the admin interface
public class InsertNewSongPageController {

    // Reference to the TextField for song image URL
    @FXML
    private TextField songImageTextField;

    // Reference to the Label for displaying error messages
    @FXML
    private Label errorMessageLabel;

    // References to various input fields
    @FXML
    private TextField yearTextField;
    @FXML
    private TextField durationTextField;
    @FXML
    private TextField titleTextField;
    @FXML
    private TextField artistsTextField;
    @FXML
    private ComboBox<String> genresComboBox;

    // Reference to the main VBox in the FXML file
    @FXML
    private VBox vBox;

    // Method called during initialization of the controller
    @FXML
    private void initialize() {
        // Initialize the genres ComboBox with a list of music genres
        List<String> genresList = ComboBoxInitializeUtil.getMusicGenres();
        genresComboBox.getItems().addAll(genresList);
    }

    // Method to handle the action when the "Add Song" button is clicked
    @FXML
    private void addSongButtonClicked() {
        // Retrieve values from input fields
        String title = titleTextField.getText().trim();
        String artists = artistsTextField.getText().trim();
        String genre = genresComboBox.getValue();
        String year = yearTextField.getText().trim();
        String duration = durationTextField.getText().trim();
        String songImage = songImageTextField.getText().trim();

        // Check if any of the fields is empty
        if (title.isEmpty() || artists.isEmpty() || genre == null
                || year.isEmpty() || duration.isEmpty() || songImage.isEmpty()) {
            errorMessageLabel.setText("Please fill in all fields.");
        } else {
            errorMessageLabel.setText(""); // Clear any previous error message

            // Create a new Song object and set its attributes
            Song newSong = new Song();
            newSong.setTitle(title);
            List<Artist> newArtists = new ArrayList<>();
            String[] pairs = artists.split("\\|");

            // Parse the artist information and add to the list
            for (String pair : pairs) {
                String[] tokens = pair.trim().split("\\s+");

                if (tokens.length >= 2) {
                    int id = Integer.parseInt(tokens[0]);
                    String name = String.join(" ", Arrays.copyOfRange(tokens, 1, tokens.length));
                    newArtists.add(new Artist(name, id));
                }
            }

            // Set the attributes of the new song
            newSong.setArtists(newArtists);
            newSong.setGenre(genre);
            newSong.setReleasedYear(Integer.parseInt(year));
            newSong.setDuration(Integer.parseInt(duration));
            newSong.setSongImage(songImage);

            // Create instances of necessary DAOs and services
            NeoSongDao neoSongDao = new NeoSongDaoImpl(Neo4jDBConfig.getInstance().getDriver());
            MongoSongDao mongoSongDao = new MongoSongDaoImpl();
            ManageSongService manageSongService = new ManageSongService(neoSongDao, mongoSongDao);

            // Call the service to create a new song
            manageSongService.createNewSong(newSong);

            // Display an information alert about successful song addition
            AlertMessages.informationAlert("Song added correctly!");

            // Get the stage (window) associated with the VBox
            Stage stage = (Stage) vBox.getScene().getWindow();

            // Obtain the PageManager instance and switch to the page for adding a new song
            PageManager pageManager = PageManager.getInstance(stage);
            pageManager.switchToAddNewSong();
        }
    }

    // Method to handle the action when the "Back" button is clicked
    @FXML
    private void backButtonClicked() {
        // Get the stage (window) associated with the VBox
        Stage stage = (Stage) vBox.getScene().getWindow();

        // Obtain the PageManager instance and switch to the admin page
        PageManager pageManager = PageManager.getInstance(stage);
        pageManager.switchToAdminPage();
    }
}
