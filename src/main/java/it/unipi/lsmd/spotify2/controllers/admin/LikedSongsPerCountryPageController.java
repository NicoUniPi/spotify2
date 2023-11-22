package it.unipi.lsmd.spotify2.controllers.admin;

import it.unipi.lsmd.spotify2.cellFactories.SearchSongCellFactory;
import it.unipi.lsmd.spotify2.daos.impl.mongo.listener.LibraryDaoImpl;
import it.unipi.lsmd.spotify2.daos.impl.mongo.listener.MongoPlaylistDaoImpl;
import it.unipi.lsmd.spotify2.daos.mongo.listener.LibraryDao;
import it.unipi.lsmd.spotify2.daos.mongo.listener.MongoPlaylistDao;
import it.unipi.lsmd.spotify2.dtos.SongDTO;
import it.unipi.lsmd.spotify2.pageManagement.PageManager;
import it.unipi.lsmd.spotify2.services.aggr.mongo.MongoAggregationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

// Controller for handling the liked songs per country page in the admin interface
public class LikedSongsPerCountryPageController {

    // Reference to the main VBox in the FXML file
    @FXML
    private VBox vBox;

    // Reference to the ComboBox for selecting a country
    @FXML
    private ComboBox<String> countryComboBox;

    // Reference to the ListView for displaying top liked songs
    @FXML
    private ListView<SongDTO> topSongsListView;

    // Instance of the MongoAggregationService for retrieving data
    private MongoAggregationService mongoAggregationService;

    // Method called during initialization of the controller
    @FXML
    private void initialize() {
        // Set the cell factory for the ListView to use the SearchSongCellFactory
        topSongsListView.setCellFactory(new SearchSongCellFactory());

        // Create instances of necessary DAOs and services
        LibraryDao libraryDao = new LibraryDaoImpl();
        MongoPlaylistDao mongoPlaylistDao = new MongoPlaylistDaoImpl();
        mongoAggregationService = new MongoAggregationService(libraryDao, mongoPlaylistDao);

        // Get the list of distinct countries and populate the ComboBox
        ObservableList<String> countryOptions = FXCollections.observableArrayList(
                mongoAggregationService.getDistinctCountries()); // Add your country names here
        countryComboBox.setItems(countryOptions);
    }

    // Method to handle the action when the "Back" button is clicked
    @FXML
    private void handleBackButtonAction() {
        // Get the stage (window) associated with the VBox
        Stage stage = (Stage) vBox.getScene().getWindow();

        // Obtain the PageManager instance and switch to the admin page
        PageManager pageManager = PageManager.getInstance(stage);
        pageManager.switchToAdminPage();
    }

    // Method to handle the action when a country is selected from the ComboBox
    @FXML
    private void handleCountrySelection() {
        // Clear the existing items in the ListView
        topSongsListView.getItems().clear();

        // Get the selected country from the ComboBox
        String selectedCountry = countryComboBox.getSelectionModel().getSelectedItem();

        // Get the top liked songs for the selected country and add them to the ListView
        List<SongDTO> topLikedSongs = mongoAggregationService.topLikedSongPerCountry(selectedCountry);
        for (SongDTO song : topLikedSongs) {
            topSongsListView.getItems().add(song);
        }
    }
}
