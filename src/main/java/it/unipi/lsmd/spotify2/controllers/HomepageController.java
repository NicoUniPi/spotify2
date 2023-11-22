package it.unipi.lsmd.spotify2.controllers;

import it.unipi.lsmd.spotify2.cellFactories.SearchSongCellFactory;
import it.unipi.lsmd.spotify2.configs.Neo4jDBConfig;
import it.unipi.lsmd.spotify2.daos.impl.mongo.MongoSongDaoImpl;
import it.unipi.lsmd.spotify2.daos.impl.neo4j.NeoListenerDaoImpl;
import it.unipi.lsmd.spotify2.daos.impl.neo4j.NeoSongDaoImpl;
import it.unipi.lsmd.spotify2.daos.mongo.MongoSongDao;
import it.unipi.lsmd.spotify2.daos.neo4j.NeoListenerDao;
import it.unipi.lsmd.spotify2.daos.neo4j.NeoSongDao;
import it.unipi.lsmd.spotify2.dtos.ListenerDTO;
import it.unipi.lsmd.spotify2.dtos.PlaylistDTO;
import it.unipi.lsmd.spotify2.dtos.SongDTO;
import it.unipi.lsmd.spotify2.pageManagement.PageManager;
import it.unipi.lsmd.spotify2.pageManagement.PageTypeEnum;
import it.unipi.lsmd.spotify2.services.SearchBarService;
import it.unipi.lsmd.spotify2.services.aggr.neo.NeoAggregationService;
import it.unipi.lsmd.spotify2.utils.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.neo4j.driver.Driver;

import java.util.ArrayList;
import java.util.List;

// Class declaration for HomepageController
public class HomepageController {

    // Fields to store the root page type, selected playlist, and friend
    private PageTypeEnum rootPage = PageTypeEnum.HOMEPAGE;
    private final PlaylistDTO selectedPlaylist;
    private final ListenerDTO friend;

    // FXML annotations to inject elements from the FXML file
    @FXML
    private ToggleButton titleToggleButton;
    @FXML
    private ToggleButton artistToggleButton;
    @FXML
    private ListView<SongDTO> resultListView;
    @FXML
    private VBox mainVBox;
    @FXML
    private TextField searchField;
    @FXML
    private Button backButton;
    @FXML
    private VBox homepageMenu;

    // Default constructor when no playlist or friend is specified
    public  HomepageController(){
        this.selectedPlaylist  = null;
        this.friend = null;
    }

    // Constructor with a specified root page
    public HomepageController(PageTypeEnum rootPage) {
        this.selectedPlaylist  = null;
        this.friend = null;
        this.rootPage = rootPage;
    }

    // Constructor with a specified playlist and friend
    public HomepageController(PlaylistDTO selectedPlaylist, ListenerDTO friend) {
        this.selectedPlaylist = selectedPlaylist;
        this.friend = friend;
        this.rootPage = PageTypeEnum.ADD_SONG_TO_FRIEND;
    }

    // Method called after the FXML file is loaded, initializes the controller
    @FXML
    private void initialize() {
        // Set the cell factory for the resultListView
        resultListView.setCellFactory(new SearchSongCellFactory());

        // Check the root page type and perform specific actions accordingly
        if(rootPage == PageTypeEnum.HOMEPAGE ) {
            List<SongDTO> suggestedSongs = getSongDTOS();
            for (SongDTO song : suggestedSongs) {
                resultListView.getItems().add(song);
            }
        } else if(rootPage == PageTypeEnum.ADD_SONG_TO_FRIEND || rootPage == PageTypeEnum.ADMIN_PAGE) {
            homepageMenu.setManaged(false);
            backButton.setManaged(true);
            homepageMenu.setVisible(false);
        }
    }

    private static List<SongDTO> getSongDTOS() {
        UserSession userSession = UserSession.getInstance();
        Driver neo4jDBConfig = Neo4jDBConfig.getInstance().getDriver();
        NeoSongDao neoSongDao = new NeoSongDaoImpl(neo4jDBConfig);
        NeoListenerDao neoListenerDao = new NeoListenerDaoImpl(neo4jDBConfig);
        NeoAggregationService neoAggregationService = new NeoAggregationService(neoSongDao, neoListenerDao);
        return neoAggregationService.suggestedSongs(userSession.getLoggedInUser());
    }

    // Method to open the playlists page
    @FXML
    private void openPlaylists() {
        Stage stage = (Stage) mainVBox.getScene().getWindow();
        PageManager pageManager = PageManager.getInstance(stage);
        pageManager.switchToPlaylistPage(PageTypeEnum.HOMEPAGE);
    }

    // Method to open the user profile page
    @FXML
    private void openProfile() {
        UserSession userSession = UserSession.getInstance();
        Stage stage = (Stage) mainVBox.getScene().getWindow();
        PageManager pageManager = PageManager.getInstance(stage);
        pageManager.switchToUserDetailsPage(new ListenerDTO(userSession.getLoggedInUser()), PageTypeEnum.HOMEPAGE);
    }

    // Method to log out and switch to the sign-in page
    @FXML
    private void logout() {
        Stage stage = (Stage) mainVBox.getScene().getWindow();
        PageManager pageManager = PageManager.getInstance(stage);
        pageManager.switchToSignIn();
    }

    // Method to open the library page
    @FXML
    private void openLibrary() {
        Stage stage = (Stage) mainVBox.getScene().getWindow();
        PageManager pageManager = PageManager.getInstance(stage);
        pageManager.switchToLibrary();
    }

    // Method to open the social page
    @FXML
    private void openSocial() {
        Stage stage = (Stage) mainVBox.getScene().getWindow();
        PageManager pageManager = PageManager.getInstance(stage);
        pageManager.switchToSocial(PageTypeEnum.HOMEPAGE, PageTypeEnum.SEARCH_LISTENERS_PAGE);
    }

    // Method to perform a search based on the selected toggle button
    @FXML
    private void search() {
        // Get the search query from the searchField
        String searchText = searchField.getText().trim();

        // Clear the existing results
        resultListView.getItems().clear();

        // Perform a search if the search query is not empty
        if (!searchText.isEmpty()) {
            List<SongDTO> songs = getSongDTOS(searchText);

            // Add the matching documents to the resultListView
            for (SongDTO song : songs) {
                resultListView.getItems().add(song);
            }
        }
    }

    private List<SongDTO> getSongDTOS(String searchText) {
        MongoSongDao mongoSongDao = new MongoSongDaoImpl();
        SearchBarService searchBarService = new SearchBarService(mongoSongDao, null);
        List<SongDTO> songs = new ArrayList<>();

        // Perform a search based on the selected toggle button
        if(titleToggleButton.isSelected()) {
            songs = searchBarService.searchSongByTitle(searchText);
        } else if(artistToggleButton.isSelected()) {
            songs = searchBarService.searchSongByArtist(searchText);
        }
        return songs;
    }

    // Method to handle a song click event
    @FXML
    private void handleSongClick() {
        SongDTO selectedSong = resultListView.getSelectionModel().getSelectedItem();
        if (selectedSong != null) {
            Stage stage = (Stage) mainVBox.getScene().getWindow();
            PageManager pageManager = PageManager.getInstance(stage);

            // Switch to the appropriate song page based on the root page type
            if(rootPage == PageTypeEnum.ADMIN_PAGE)
                pageManager.switchToSongPage(selectedSong, rootPage);
            else if(rootPage == PageTypeEnum.HOMEPAGE)
                pageManager.switchToSongPage(selectedSong, PageTypeEnum.HOMEPAGE);
            else {
                pageManager.switchToSongPage(selectedSong, selectedPlaylist, friend, rootPage);
            }
        }
    }

    // Method to handle the title toggle button click event
    @FXML
    private void handleTitleButton() {
        if(titleToggleButton.isSelected())
            artistToggleButton.setSelected(false);
        else
            titleToggleButton.setSelected(true);
    }

    // Method to handle the artist toggle button click event
    @FXML
    private void handleArtistButton() {
        if(artistToggleButton.isSelected())
            titleToggleButton.setSelected(false);
        else
            artistToggleButton.setSelected(true);
    }

    // Method to handle the back button click event
    @FXML
    private void handleBackButton() {
        Stage stage = (Stage) mainVBox.getScene().getWindow();
        PageManager pageManager = PageManager.getInstance(stage);

        // Switch to the appropriate page based on the root page type
        if(rootPage == PageTypeEnum.ADMIN_PAGE)
            pageManager.switchToAdminPage();
        else
            pageManager.switchToSongsInPlaylistPage(selectedPlaylist, friend, PageTypeEnum.FRIENDS_LIST_PAGE);
    }
}
