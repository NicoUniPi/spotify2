package it.unipi.lsmd.spotify2.cellFactories;

import it.unipi.lsmd.spotify2.controllers.library.LibraryPageController;
import it.unipi.lsmd.spotify2.daos.impl.mongo.listener.LibraryDaoImpl;
import it.unipi.lsmd.spotify2.daos.mongo.listener.LibraryDao;
import it.unipi.lsmd.spotify2.dtos.SongDTO;
import it.unipi.lsmd.spotify2.services.LibraryService;
import it.unipi.lsmd.spotify2.utils.UserSession;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

// This class extends ListCell of the library and specifies how each cell in the ListView should be rendered
public class LibraryCellFactory extends ListCell<SongDTO> {

    // Button for removing a song from the library
    private final Button removeButton = new Button("Remove");

    // Singleton instance of UserSession for managing user sessions
    private final UserSession userSession = UserSession.getInstance();

    // Label for displaying the song title in the cell
    private final Label playlistLabel = new Label();

    // Constructor that takes a LibraryPageController for interacting with the library page
    public LibraryCellFactory(LibraryPageController libraryPageController) {

        // Set the action for the removeButton
        removeButton.setOnAction(event -> {
            // Get the selected song
            SongDTO song = getItem();
            if (song != null) {
                // Initialize a DAO and Service for interacting with the library
                LibraryDao libraryDao = new LibraryDaoImpl();
                LibraryService libraryService = new LibraryService(libraryDao);

                // Remove the song from the library
                libraryService.removeFromLibrary(userSession.getLoggedInUser(), song.getId());

                // Reinitialize the library page to reflect the changes
                libraryPageController.initialize();
            }
        });
    }

    // This method is called whenever the item in the list changes
    @Override
    protected void updateItem(SongDTO song, boolean empty) {
        super.updateItem(song, empty);

        // If the cell is empty or the song is null, set the graphic to null
        if (empty || song == null) {
            setGraphic(null);
        } else {
            // Set the text of the label to the song title
            playlistLabel.setText(song.getTitle());

            // Create an HBox to hold the label and removeButton, and set its alignment
            HBox songContainer = new HBox(40);
            songContainer.setAlignment(Pos.CENTER);

            // Add the label and removeButton to the HBox
            songContainer.getChildren().addAll(playlistLabel, removeButton);

            // Set the graphic of the cell to the HBox
            setGraphic(songContainer);
        }
    }
}
