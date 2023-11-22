package it.unipi.lsmd.spotify2.services.playlist;

import it.unipi.lsmd.spotify2.daos.neo4j.NeoPlaylistDao;
import it.unipi.lsmd.spotify2.dtos.PlaylistDTO;
import org.neo4j.driver.Transaction;

import java.util.List;

public class NeoServicePlaylist {
    private final NeoPlaylistDao neoPlaylistDao;

    public NeoServicePlaylist(NeoPlaylistDao neoPlaylistDao) {
        this.neoPlaylistDao = neoPlaylistDao;
    }

    public void deletePlaylist(String idPlaylist, Transaction tx) {
        neoPlaylistDao.deletePlaylist(idPlaylist, tx);
    }

    public boolean checkPlaylistPresence(String idPlaylist) {
        return neoPlaylistDao.checkPlaylistPresence(idPlaylist);
    }

    public List<PlaylistDTO> getFriendPlaylists(String friendUsername) {
        return neoPlaylistDao.getFriendPlaylists(friendUsername);
    }

    public int checkSongInPlaylist(String songId, String playlistId, String friend) {
        return neoPlaylistDao.checkSongInPlaylistPresence(songId, playlistId, friend);
    }
}
