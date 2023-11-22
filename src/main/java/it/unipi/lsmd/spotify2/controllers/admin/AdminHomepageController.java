package it.unipi.lsmd.spotify2.controllers.admin;

import it.unipi.lsmd.spotify2.pageManagement.PageManager;
import it.unipi.lsmd.spotify2.pageManagement.PageTypeEnum;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// Controller for the admin homepage
public class AdminHomepageController {

    // Reference to the main VBox in the FXML file
    @FXML
    private VBox vBox;

    // Method to handle the button click for top songs per country
    @FXML
    private void handleBtnTopSongsPerCountry() {
        // Get the stage (window) associated with the VBox
        Stage stage = (Stage) vBox.getScene().getWindow();

        // Obtain the PageManager instance
        PageManager pageManager = PageManager.getInstance(stage);

        // Switch to the statistics page for top songs per country
        pageManager.switchToStatsPage();
    }

    // Method to handle the button click for playlists statistics
    @FXML
    private void handlePlaylistsStatistics() {
        Stage stage = (Stage) vBox.getScene().getWindow();
        PageManager pageManager = PageManager.getInstance(stage);

        // Switch to the social page for playlists statistics
        pageManager.switchToSocial(PageTypeEnum.ADMIN_PAGE, PageTypeEnum.SEARCH_LISTENERS_PAGE);
    }

    // Method to handle the button click for genre distributions
    @FXML
    private void handleGenreDistributions() {
        Stage stage = (Stage) vBox.getScene().getWindow();
        PageManager pageManager = PageManager.getInstance(stage);

        // Switch to the page displaying genre distributions
        pageManager.switchToGenreDistributions();
    }

    // Method to handle the button click for inserting a new song
    @FXML
    private void handleBtnInsertSong() {
        Stage stage = (Stage) vBox.getScene().getWindow();
        PageManager pageManager = PageManager.getInstance(stage);

        // Switch to the page for adding a new song
        pageManager.switchToAddNewSong();
    }

    // Method to handle the button click for updating or deleting a song
    @FXML
    private void handleBtnUpdateDeleteSong() {
        Stage stage = (Stage) vBox.getScene().getWindow();
        PageManager pageManager = PageManager.getInstance(stage);

        // Switch to the admin homepage
        pageManager.switchToHomepage(PageTypeEnum.ADMIN_PAGE);
    }

    // Method to handle the button click for logging out
    @FXML
    private void handleBtnLogout() {
        Stage stage = (Stage) vBox.getScene().getWindow();
        PageManager pageManager = PageManager.getInstance(stage);

        // Switch to the sign-in page
        pageManager.switchToSignIn();
    }
}
