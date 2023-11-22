package it.unipi.lsmd.spotify2.services.playlist;

import it.unipi.lsmd.spotify2.daos.mongo.listener.MongoPlaylistDao;
import it.unipi.lsmd.spotify2.dtos.PlaylistDTO;
import it.unipi.lsmd.spotify2.dtos.SongDTO;
import org.bson.types.ObjectId;

import java.util.List;

public class MongoServicePlaylist {
    private final MongoPlaylistDao mongoPlaylistDao;

    public MongoServicePlaylist(MongoPlaylistDao mongoPlaylistDao) {
        this.mongoPlaylistDao = mongoPlaylistDao;
    }

    public void addPlaylist(String username, String namePlaylist) {
        mongoPlaylistDao.addPlaylist(username, namePlaylist);
    }

    public List<PlaylistDTO> initializePlaylists(String username) {
        return mongoPlaylistDao.getPlaylists(username);
    }

    public void addSongToPlaylist(String username, SongDTO song, ObjectId playlistId){
        mongoPlaylistDao.addSongToPlaylist(username, song, playlistId);
    }

    public List<SongDTO> getSongsFromPlaylist(String username, ObjectId playlistId) {
        return mongoPlaylistDao.getSongsFromPlaylist(username, playlistId, null);
    }

    public void removeSongFromPlaylist(String username, ObjectId playlistId, ObjectId songId) {
        mongoPlaylistDao.removeSongFromPlaylist(username, playlistId, songId);
    }

    public boolean checkSongFromPlaylist(String username, ObjectId playlistId, ObjectId songId) {
        return mongoPlaylistDao.checkSongFromPlaylist(username, playlistId, songId);
    }
}
