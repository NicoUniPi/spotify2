package it.unipi.lsmd.spotify2.controllers.admin;

import it.unipi.lsmd.spotify2.cellFactories.PlaylistStatsCellFactory;
import it.unipi.lsmd.spotify2.daos.impl.mongo.listener.LibraryDaoImpl;
import it.unipi.lsmd.spotify2.daos.impl.mongo.listener.MongoPlaylistDaoImpl;
import it.unipi.lsmd.spotify2.daos.mongo.listener.LibraryDao;
import it.unipi.lsmd.spotify2.daos.mongo.listener.MongoPlaylistDao;
import it.unipi.lsmd.spotify2.pageManagement.PageManager;
import it.unipi.lsmd.spotify2.services.aggr.mongo.MongoAggregationService;
import it.unipi.lsmd.spotify2.utils.GenreDistribution;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

// Controller for handling genre distributions in the admin interface
public class genreDistributionsController {

    // Reference to the ListView for displaying genre distributions
    @FXML
    private ListView<GenreDistribution> genreDistributions;

    // Reference to the main VBox in the FXML file
    @FXML
    private VBox vBox;

    // Method called during initialization of the controller
    @FXML
    private void initialize() {
        // Set a custom cell factory for the ListView to display genre distributions
        genreDistributions.setCellFactory(new PlaylistStatsCellFactory());

        // Create instances of necessary DAOs and services
        LibraryDao libraryDao = new LibraryDaoImpl();
        MongoPlaylistDao mongoPlaylistDao = new MongoPlaylistDaoImpl();
        MongoAggregationService mongoAggregationService = new MongoAggregationService(libraryDao, mongoPlaylistDao);

        // Retrieve genre distributions from the aggregation service
        List<GenreDistribution> genreDistributions = mongoAggregationService.getGenreDistribution();

        // Add retrieved genre distributions to the ListView
        for (GenreDistribution genre : genreDistributions) {
            this.genreDistributions.getItems().add(genre);
        }
    }

    // Method to handle the action when the back button is clicked
    public void handleBackButtonAction() {
        // Get the stage (window) associated with the VBox
        Stage stage = (Stage) vBox.getScene().getWindow();

        // Obtain the PageManager instance
        PageManager pageManager = PageManager.getInstance(stage);

        // Switch to the admin page
        pageManager.switchToAdminPage();
    }
}
