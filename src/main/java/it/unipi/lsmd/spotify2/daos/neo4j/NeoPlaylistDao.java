package it.unipi.lsmd.spotify2.daos.neo4j;

import it.unipi.lsmd.spotify2.dtos.PlaylistDTO;
import org.neo4j.driver.Transaction;

import java.util.List;

public interface NeoPlaylistDao {
    void createPlaylist(PlaylistDTO newPlaylist, String ownerPlaylist, List<String> songIds, Transaction tx);
    void addSongToPlaylist(String songId, String playlistId, String loggedUser, Transaction tx);
    void removeSongFromFriendPlaylist(String songId, String playlistId, String loggedUser, Transaction tx);
    void deletePlaylist(String idPlaylist, Transaction tx);
    boolean checkPlaylistPresence(String idPlaylist);
    List<PlaylistDTO> getFriendPlaylists(String friendUsername);
    void addSongToFriendPlaylist(String songId, String songTitle, String playlistId, String loggedUser, Transaction tx);
    int checkSongInPlaylistPresence(String idSong, String idPlaylist, String friend);
    void removeSongFromPlaylist(String songId, String playlistId, Transaction tx);
    String getUsernameWhoAdded(String songId, String playlistId);
}
