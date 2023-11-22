package it.unipi.lsmd.spotify2.controllers.admin;

import it.unipi.lsmd.spotify2.daos.impl.mongo.listener.LibraryDaoImpl;
import it.unipi.lsmd.spotify2.daos.impl.mongo.listener.MongoPlaylistDaoImpl;
import it.unipi.lsmd.spotify2.daos.mongo.listener.LibraryDao;
import it.unipi.lsmd.spotify2.daos.mongo.listener.MongoPlaylistDao;
import it.unipi.lsmd.spotify2.dtos.ListenerDTO;
import it.unipi.lsmd.spotify2.pageManagement.PageManager;
import it.unipi.lsmd.spotify2.pageManagement.PageTypeEnum;
import it.unipi.lsmd.spotify2.services.aggr.mongo.MongoAggregationService;
import it.unipi.lsmd.spotify2.utils.PlaylistStats;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;

// Controller for displaying statistics of playlists for a listener in the admin interface
public class PlaylistsStatisticsController {

    // Labels for displaying listener and date range information
    @FXML
    private Label listenerLabel;
    @FXML
    private Label startDateLabel;
    @FXML
    private Label endDateLabel;

    // Labels for displaying playlist statistics
    @FXML
    private Label totalPlaylistsLabel;
    @FXML
    private Label totalSongsLabel;
    @FXML
    private Label totalDurationLabel;
    @FXML
    private Label averageSongLengthLabel;

    // Reference to the main VBox in the FXML file
    @FXML
    private VBox vBox;

    // Listener information
    private final String listenerUsername;
    private final LocalDate startDate;
    private final LocalDate endDate;

    // Constructor to initialize listener and date range information
    public PlaylistsStatisticsController(ListenerDTO listener, LocalDate startDate, LocalDate endDate) {
        this.listenerUsername = listener.getUsername();
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Method called during initialization of the controller
    public void initialize() {
        // Create instances of necessary DAOs and services
        LibraryDao libraryDao = new LibraryDaoImpl();
        MongoPlaylistDao mongoPlaylistDao = new MongoPlaylistDaoImpl();
        MongoAggregationService mongoAggregationService = new MongoAggregationService(libraryDao, mongoPlaylistDao);

        // Get the playlist statistics for the listener and date range
        PlaylistStats playlistStats = mongoAggregationService.getPlaylistsStatsOfUser(listenerUsername, startDate, endDate);

        // Display the retrieved statistics in the labels
        if (playlistStats != null) {
            listenerLabel.setText(playlistStats.getUsername());
            startDateLabel.setText(playlistStats.getStartDate().toString());
            endDateLabel.setText(playlistStats.getEndDate().toString());
            totalPlaylistsLabel.setText(String.valueOf(playlistStats.getTotalPlaylists()));
            totalSongsLabel.setText(String.valueOf(playlistStats.getTotalSongs()));
            totalDurationLabel.setText(String.valueOf(playlistStats.getTotalDuration()));
            averageSongLengthLabel.setText(Double.toString(playlistStats.getAverageNumber()));
        }
    }

    // Method to handle the action when the "Back" button is clicked
    @FXML
    private void handleBackButton() {
        // Get the stage (window) associated with the VBox
        Stage stage = (Stage) vBox.getScene().getWindow();

        // Obtain the PageManager instance and switch to the search listeners page in the admin interface
        PageManager pageManager = PageManager.getInstance(stage);
        pageManager.switchToSocial(PageTypeEnum.ADMIN_PAGE, PageTypeEnum.SEARCH_LISTENERS_PAGE);
    }
}
