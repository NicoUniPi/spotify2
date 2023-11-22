package it.unipi.lsmd.spotify2.controllers.utils;

// Import statements for classes used in this file
import it.unipi.lsmd.spotify2.controllers.playlists.PlaylistPageController;
import it.unipi.lsmd.spotify2.daos.impl.mongo.listener.MongoPlaylistDaoImpl;
import it.unipi.lsmd.spotify2.daos.mongo.listener.MongoPlaylistDao;
import it.unipi.lsmd.spotify2.services.playlist.MongoServicePlaylist;
import it.unipi.lsmd.spotify2.utils.AlertMessages;
import it.unipi.lsmd.spotify2.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

// Class declaration for PopupDialogController
public class PopupDialogController {

    // FXML annotations to inject elements from the FXML file
    @FXML
    private TextField textField;
    @FXML
    private Stage dialogStage;

    // Fields to store the playlist name and associated PlaylistPageController
    private String namePlaylist;
    private PlaylistPageController playlistPageController;

    // Method to set the dialog stage
    @FXML
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    // Method to set the PlaylistPageController
    public void setPlaylistPageController(PlaylistPageController playlistPageController) {
        this.playlistPageController = playlistPageController;
    }

    // Method to handle the confirm button action
    @FXML
    private void confirmButtonAction(ActionEvent event) {
        // Get the playlist name from the text field
        namePlaylist = textField.getText();

        // Check if the playlist name is not empty
        if(!namePlaylist.isEmpty()) {
            // Set up MongoDB-related objects and get the current user session
            MongoPlaylistDao mongoPlaylistDao = new MongoPlaylistDaoImpl();
            MongoServicePlaylist mongoServicePlaylist = new MongoServicePlaylist(mongoPlaylistDao);
            UserSession userSession = UserSession.getInstance();

            // Add the playlist to the user's account and close the dialog
            mongoServicePlaylist.addPlaylist(userSession.getLoggedInUser(), namePlaylist);
            closeDialog();

            // If a PlaylistPageController is associated, initialize it
            if(playlistPageController != null)
                playlistPageController.initialize();
        } else {
            // Display an information alert if the playlist name is empty
            AlertMessages.informationAlert("Please insert a playlist name");
        }
    }

    // Method to handle the cancel button action
    @FXML
    private void cancelButtonAction(ActionEvent event) {
        // Set the playlist name to null and close the dialog
        namePlaylist = null;
        closeDialog();
    }

    // Method to close the dialog
    private void closeDialog() {
        // Check if the dialog stage is not null, then close it
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}
