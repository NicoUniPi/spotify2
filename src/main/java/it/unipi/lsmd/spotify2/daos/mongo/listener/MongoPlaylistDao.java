package it.unipi.lsmd.spotify2.daos.mongo.listener;

import com.mongodb.client.ClientSession;
import it.unipi.lsmd.spotify2.dtos.PlaylistDTO;
import it.unipi.lsmd.spotify2.dtos.SongDTO;
import it.unipi.lsmd.spotify2.utils.GenreDistribution;
import it.unipi.lsmd.spotify2.utils.PlaylistStats;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface MongoPlaylistDao {
    void addPlaylist(String username, String playlistName);

    void addSongToPlaylist(String username, SongDTO song, ObjectId idPlaylist);

    List<SongDTO> getSongsFromPlaylist(String username, ObjectId playlistId, ClientSession mongoSession);

    void removeSongFromPlaylist(String username, ObjectId playlistId, ObjectId songId);

    boolean checkSongFromPlaylist(String username, ObjectId playlistId, ObjectId songId);

    void removePlaylist(String username, ObjectId idPlaylist);

    List<PlaylistDTO> getPlaylists(String username);

    PlaylistStats playlistStatistics(String username, LocalDate startDate, LocalDate endDate);

    List<GenreDistribution> genreDistribution();
}
