package it.unipi.lsmd.spotify2.pageManagement;

import it.unipi.lsmd.spotify2.controllers.HomepageController;
import it.unipi.lsmd.spotify2.controllers.admin.PlaylistsStatisticsController;
import it.unipi.lsmd.spotify2.controllers.playlists.PlaylistPageController;
import it.unipi.lsmd.spotify2.controllers.social.SocialPageController;
import it.unipi.lsmd.spotify2.controllers.songs.SongsInPlaylistPageController;
import it.unipi.lsmd.spotify2.controllers.social.ListenerDetailsPageController;
import it.unipi.lsmd.spotify2.controllers.songs.SongPageController;
import it.unipi.lsmd.spotify2.controllers.utils.PopupDialogController;
import it.unipi.lsmd.spotify2.dtos.ListenerDTO;
import it.unipi.lsmd.spotify2.dtos.PlaylistDTO;
import it.unipi.lsmd.spotify2.dtos.SongDTO;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PageManager {
    private static PageManager instance;
    private final Stage primaryStage;

    private static final Logger logger = LoggerFactory.getLogger(PageManager.class);
    public PageManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public static PageManager getInstance(Stage stage) {
        if (instance == null) {
            instance = new PageManager(stage);
        }
        return instance;
    }

    public void switchToSignIn() {
        loadPage("/views/auth/SignIn.fxml", "Sign In");
    }

    public void switchToSignUp() {
        loadPage("/views/auth/SignUp.fxml", "Sign Up");
    }

    public void switchToLibrary() {loadPage("/views/library/LibraryPage.fxml", "Library");}

    public void switchToListFriends() {loadPage("/views/social/ListFriends.fxml", "ListFriends");}

    public void switchToAddNewSong() {loadPage("/views/admin/InsertNewSongPage.fxml", "Insert New song");}

    public void switchToAdminPage() {loadPage("/views/admin/AdminHomepage.fxml", "Insert New song");}

    public void switchToStatsPage() {loadPage("/views/admin/statsPages/LikedSongsPerCountryPage.fxml", "Stats page");}

    public void switchToGenreDistributions() {
        loadPage("/views/admin/statsPages/GenreDistributionsPage.fxml", "Diversity stats page");
    }

    public void switchToSongPage(SongDTO song, PageTypeEnum rootPage) {
        loadPage("/views/songs/SongPage.fxml", "SongPage", song, null, rootPage);
    }

    public void switchToSongPage(SongDTO song, PlaylistDTO playlist, PageTypeEnum rootPage) {
        List<Object> dtos = new ArrayList<>();
        dtos.add(song);
        dtos.add(playlist);
        loadPage("/views/songs/SongPage.fxml", "SongPage", dtos, PageTypeEnum.SONG_PAGE, rootPage);
    }

    public void switchToSongPage(SongDTO song, PlaylistDTO playlist, ListenerDTO friend, PageTypeEnum rootPage) {
        List<Object> dtos = new ArrayList<>();
        dtos.add(song);
        dtos.add(playlist);
        dtos.add(friend);
        loadPage("/views/songs/SongPage.fxml", "SongPage", dtos, null, rootPage);
    }

    public void switchToUserDetailsPage(ListenerDTO listener, PageTypeEnum rootPage) {
        loadPage("/views/social/ListenerDetailsPage.fxml", "ListenerDetailsPage", listener, PageTypeEnum.LISTENER_DETAILS_PAGE, rootPage);
    }

    public void switchToPlaylistPage(PageTypeEnum rootPage) {
        loadPage("/views/playlists/PlaylistPage.fxml", "PlaylistPage", (Object) null, null, rootPage);
    }

    public void switchToSocial(PageTypeEnum rootPage, PageTypeEnum page) {
        loadPage("/views/social/SocialPage.fxml", "Search for a listener", (Object) null, page, rootPage);
    }

    public void switchToPlaylistsStatisticsPage(ListenerDTO selectedListener, LocalDate startDate, LocalDate endDate) {
        List<Object> dtos = new ArrayList<>();
        dtos.add(selectedListener);
        dtos.add(startDate);
        dtos.add(endDate);
        loadPage("/views/admin/statsPages/PlaylistsStatisticsPage.fxml", "Playlists statistics page", dtos, null, PageTypeEnum.SEARCH_LISTENERS_PAGE);
    }

    public void switchToPlaylistPage(SongDTO song, PageTypeEnum rootPage, PageTypeEnum page) {
        loadPage("/views/playlists/PlaylistPage.fxml", "PlaylistPage", song, page, rootPage);
    }

    public void switchToPlaylistPage(ListenerDTO listener, PageTypeEnum rootPage) {
        loadPage("/views/playlists/PlaylistPage.fxml", "PlaylistPage", listener, PageTypeEnum.PLAYLISTS_PAGE, rootPage);
    }

    public void switchToSongsInPlaylistPage(PlaylistDTO playlist, PageTypeEnum rootPage) {
        loadPage("/views/songs/SongsInPlaylistPage.fxml", "SongsInPlaylistPage", playlist, null, rootPage);
    }

    public void switchToSongsInPlaylistPage(PlaylistDTO playlist, ListenerDTO friend, PageTypeEnum rootPage) {
        List<Object> dtos = new ArrayList<>();
        dtos.add(playlist);
        dtos.add(friend);
        loadPage("/views/songs/SongsInPlaylistPage.fxml", "SongsInPlaylistPage", dtos, PageTypeEnum.SONGS_IN_PLAYLIST_PAGE, rootPage);
    }

    public void switchToHomepage(PageTypeEnum rootPage) {
        loadPage("/views/homepage.fxml", "Homepage", (Object) null, null, rootPage);
    }

    public void switchToHomepage(PlaylistDTO playlist, ListenerDTO friend, PageTypeEnum page) {
        List<Object> dtos = new ArrayList<>();
        dtos.add(playlist);
        dtos.add(friend);
        loadPage("/views/homepage.fxml", "AddSongToPlaylist", dtos, page, null);
    }

    public void openPlaylistPopup(PlaylistPageController playlistPageController) {
        openPopup("/views/utils/PopupDialog.fxml", "PlaylistPopup", playlistPageController);
    }

    private void loadPage(String fxmlFileName, String title) {
        loadPage(fxmlFileName, title, (Object) null, PageTypeEnum.NATIVE_PAGE, PageTypeEnum.NATIVE_PAGE);
    }

    private void loadPage(String fxmlFileName, String title, Object dto, PageTypeEnum page, PageTypeEnum rootPage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));

            if (page != PageTypeEnum.NATIVE_PAGE) {
                Object pageController = getController(dto, page, rootPage);
                if (pageController != null) {
                    loader.setController(pageController);
                }
            }
            loadFxml(title, loader);
        } catch (Exception e) {
            logger.error("An error occurred:", e);
        }
    }

    private void loadFxml(String title, FXMLLoader loader) throws java.io.IOException {
        Parent root = loader.load();
        Scene scene = new Scene(root, 700, 700);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/styles.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.show();
    }

    private void loadPage(String fxmlFileName, String title, List<Object> dto, PageTypeEnum page, PageTypeEnum rootPage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));

            if (dto != null) {

                if (page == PageTypeEnum.SONGS_IN_PLAYLIST_PAGE) {
                    PlaylistDTO firstParam = (PlaylistDTO) dto.get(0);
                    ListenerDTO secondParam = (ListenerDTO) dto.get(1);
                    SongsInPlaylistPageController songsInPlaylistPageController = new SongsInPlaylistPageController(firstParam, secondParam, rootPage);
                    loader.setController(songsInPlaylistPageController);
                } else if(rootPage == PageTypeEnum.SEARCH_LISTENERS_PAGE) {
                    ListenerDTO firstParam = (ListenerDTO) dto.get(0);
                    LocalDate secondParam = (LocalDate) dto.get(1);
                    LocalDate thirdParam = (LocalDate) dto.get(2);
                    PlaylistsStatisticsController playlistsStatisticsController = new PlaylistsStatisticsController(firstParam, secondParam, thirdParam);
                    loader.setController(playlistsStatisticsController);
                } else if(page == PageTypeEnum.SONG_PAGE) {
                    SongDTO firstParam = (SongDTO) dto.get(0);
                    PlaylistDTO secondParam = (PlaylistDTO) dto.get(1);
                    SongPageController songPageController = new SongPageController(firstParam, secondParam, rootPage);
                    loader.setController(songPageController);
                } else if (rootPage == PageTypeEnum.ADD_SONG_TO_FRIEND) {
                    SongDTO fistParam = (SongDTO) dto.get(0);
                    PlaylistDTO secondParam = (PlaylistDTO) dto.get(1);
                    ListenerDTO thirdParam = (ListenerDTO) dto.get(2);
                    SongPageController songPageController = new SongPageController(fistParam, secondParam, thirdParam, rootPage);
                    loader.setController(songPageController);
                } else if (page == PageTypeEnum.ADD_SONG_TO_FRIEND) {
                    PlaylistDTO firstParam = (PlaylistDTO) dto.get(0);
                    ListenerDTO secondParam = (ListenerDTO) dto.get(1);
                    HomepageController homepageController = new HomepageController(firstParam, secondParam);
                    loader.setController(homepageController);
                }
            }
            loadFxml(title, loader);
        } catch (Exception e) {
            logger.error("An error occurred:", e);
        }
    }

    private Object getController(Object dto, PageTypeEnum page, PageTypeEnum rootPage) {
        Object pageController = null;

        if(dto instanceof SongDTO && rootPage == PageTypeEnum.ADD_TO_PLAYLIST_PAGE) {
            pageController = new PlaylistPageController((SongDTO) dto, rootPage, page);
        }else if (page == PageTypeEnum.SEARCH_LISTENERS_PAGE){
            pageController = new SocialPageController(rootPage);
        }else if (dto instanceof SongDTO && rootPage == PageTypeEnum.SONGS_IN_PLAYLIST_PAGE) {
            pageController = new SongPageController((SongDTO) dto, PageTypeEnum.HOMEPAGE);
        } else if (dto instanceof PlaylistDTO && rootPage == PageTypeEnum.HOMEPAGE) {
            pageController = new SongsInPlaylistPageController((PlaylistDTO) dto, rootPage);
        } else if (dto instanceof SongDTO && rootPage == PageTypeEnum.HOMEPAGE) {
            pageController = new SongPageController((SongDTO) dto, rootPage);
        } else if (dto instanceof ListenerDTO && rootPage == PageTypeEnum.HOMEPAGE) {
            pageController = new ListenerDetailsPageController((ListenerDTO) dto, rootPage);
        }else if (rootPage == PageTypeEnum.HOMEPAGE) {
            pageController = new PlaylistPageController(rootPage);
        } else if (rootPage == PageTypeEnum.SIGN_IN_PAGE) {
            pageController = new HomepageController();
        }else if (dto instanceof SongDTO && rootPage == PageTypeEnum.LIBRARY_PAGE) {
            pageController = new SongPageController((SongDTO) dto, rootPage);
        } else if (dto instanceof ListenerDTO && page == PageTypeEnum.FRIENDS_LIST_PAGE) {
            pageController = new PlaylistPageController((ListenerDTO) dto, rootPage);
        } else if ((dto instanceof ListenerDTO || dto == null) && page == PageTypeEnum.PLAYLISTS_PAGE) {
            pageController = new PlaylistPageController((ListenerDTO) dto, rootPage);
        } else if (dto instanceof ListenerDTO && page == PageTypeEnum.LISTENER_DETAILS_PAGE) {
            pageController = new ListenerDetailsPageController((ListenerDTO) dto, rootPage);
        } else if(dto instanceof SongDTO && rootPage == PageTypeEnum.ADMIN_PAGE){
            pageController = new SongPageController((SongDTO) dto, rootPage);
        } else if(rootPage == PageTypeEnum.ADMIN_PAGE)
            //pageController = new AddSongToFriendController(rootPage);
            pageController = new HomepageController(rootPage);
        return pageController;
    }

    public <T> void openPopup(String fxmlFileName, String title, T controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            VBox popupPage = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.UTILITY);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(popupPage);
            dialogStage.setTitle(title);
            dialogStage.setScene(scene);

            PopupDialogController popupController = loader.getController();
            popupController.setDialogStage(dialogStage);

            if (controller instanceof PlaylistPageController) {
                popupController.setPlaylistPageController((PlaylistPageController) controller);
            }

            dialogStage.showAndWait();
        } catch (Exception e) {
            logger.error("An error occurred:", e);
        }
    }
}
