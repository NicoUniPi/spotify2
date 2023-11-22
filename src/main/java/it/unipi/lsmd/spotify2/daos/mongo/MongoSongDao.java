package it.unipi.lsmd.spotify2.daos.mongo;

import com.mongodb.client.ClientSession;
import it.unipi.lsmd.spotify2.dtos.SongDTO;
import it.unipi.lsmd.spotify2.models.Song;
import org.bson.types.ObjectId;

import java.util.List;

public interface MongoSongDao {
    void createSong(Song song, ClientSession session);

    List<SongDTO> getSongByTitle(String title);

    Song getSongById(ObjectId id);

    void updateSong(Song song, ClientSession session);

    void deleteSong(ObjectId id, ClientSession session);

    List<SongDTO> getSongByArtist(String artist);
}
