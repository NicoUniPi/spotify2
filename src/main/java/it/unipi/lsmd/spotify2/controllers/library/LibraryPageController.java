package it.unipi.lsmd.spotify2.controllers.library;

import it.unipi.lsmd.spotify2.cellFactories.LibraryCellFactory;
import it.unipi.lsmd.spotify2.daos.impl.mongo.listener.LibraryDaoImpl;
import it.unipi.lsmd.spotify2.daos.mongo.listener.LibraryDao;
import it.unipi.lsmd.spotify2.dtos.SongDTO;
import it.unipi.lsmd.spotify2.pageManagement.PageManager;
import it.unipi.lsmd.spotify2.pageManagement.PageTypeEnum;
import it.unipi.lsmd.spotify2.services.LibraryService;
import it.unipi.lsmd.spotify2.utils.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

// Controller class for the Library Page
public class LibraryPageController {

    // Reference to the ListView that displays the user's library of songs
    @FXML
    private ListView<SongDTO> libraryListView;

    // Reference to the VBox layout of the library page
    @FXML
    private VBox vBox;

    // Enum representing the type of the library page
    private final PageTypeEnum libraryPage = PageTypeEnum.LIBRARY_PAGE;

    // Method called during initialization of the controller
    public void initialize() {
        // Clear the existing items in the library ListView
        libraryListView.getItems().clear();

        // Get the currently logged-in user's session
        UserSession userSession = UserSession.getInstance();

        // Initialize Data Access Object (DAO) for library using MongoDB
        LibraryDao libraryDao = new LibraryDaoImpl();

        // Initialize LibraryService with the DAO
        LibraryService libraryService = new LibraryService(libraryDao);

        // Retrieve the user's library of songs
        List<SongDTO> songs = libraryService.getLibrary(userSession.getLoggedInUser());

        // Set the cell factory for the ListView using a custom LibraryCellFactory
        libraryListView.setCellFactory(listView -> new LibraryCellFactory(this));

        // Add the songs to the ListView
        for (SongDTO song : songs) {
            libraryListView.getItems().add(song);
        }
    }

    // Event handler for the Home button, switches to the homepage
    @FXML
    private void handleHomeButton() {
        // Get the stage (window) associated with the VBox
        Stage stage = (Stage) vBox.getScene().getWindow();

        // Get the singleton instance of the PageManager
        PageManager pageManager = PageManager.getInstance(stage);

        // Switch to the homepage (Sign In Page)
        pageManager.switchToHomepage(PageTypeEnum.SIGN_IN_PAGE);
    }

    // Event handler for when a song in the library is clicked, switches to the Song Page
    @FXML
    private void handleSongClick() {
        // Get the selected song from the ListView
        SongDTO selectedSong = libraryListView.getSelectionModel().getSelectedItem();

        // Get the stage (window) associated with the VBox
        Stage stage = (Stage) vBox.getScene().getWindow();

        // Get the singleton instance of the PageManager
        PageManager pageManager = PageManager.getInstance(stage);

        // Switch to the Song Page for the selected song
        pageManager.switchToSongPage(selectedSong, libraryPage);
    }
}
